// analytics-service.js - Express.js Microservice for Analytics
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const rateLimit = require('express-rate-limit');
const { ClickHouse } = require('@clickhouse/client');
const geoip = require('geoip-lite');
const useragent = require('useragent');

const app = express();
const PORT = process.env.PORT || 3000;

// ClickHouse client configuration
let clickhouse;
try {
    clickhouse = new ClickHouse({
        host: process.env.CLICKHOUSE_HOST || 'localhost:8123',
        username: process.env.CLICKHOUSE_USER || 'default',
        password: process.env.CLICKHOUSE_PASSWORD || '',
        database: process.env.CLICKHOUSE_DB || 'analytics',
        clickhouse_settings: {
            async_insert: 1,
            wait_for_async_insert: 0
        }
    });
} catch (error) {
    console.error('Failed to initialize ClickHouse client:', error);
    process.exit(1);
}

// Middleware
app.use(helmet());
app.use(compression());
app.use(cors({
    origin: process.env.ALLOWED_ORIGINS?.split(',') || '*',
    methods: ['GET', 'POST'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));

// Rate limiting
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 1000, // limit each IP to 1000 requests per windowMs
    message: 'Too many requests from this IP'
});
app.use('/analytics', limiter);

app.use(express.json({ limit: '10mb' }));

// Health check endpoint
app.get('/health', async (req, res) => {
    try {
        await clickhouse.ping();
        res.json({ status: 'healthy', timestamp: new Date().toISOString() });
    } catch (error) {
        res.status(503).json({ status: 'unhealthy', error: error.message });
    }
});

// Get client IP helper
function getClientIP(req) {
    return req.headers['x-forwarded-for']?.split(',')[0] ||
           req.headers['x-real-ip'] ||
           req.connection.remoteAddress ||
           req.socket.remoteAddress ||
           req.ip;
}

// Generate UUID helper
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// Enrich event data
function enrichEvent(event, req) {
    const clientIP = getClientIP(req);
    const geo = geoip.lookup(clientIP);
    const agent = useragent.parse(event.user_agent || req.headers['user-agent']);
    
    return {
        ...event,
        // Ensure required fields have defaults
        event_id: event.event_id || generateUUID(),
        timestamp: event.timestamp || new Date().toISOString(),
        
        // Geographic data
        country: geo?.country || 'Unknown',
        city: geo?.city || 'Unknown',
        
        // Enhanced browser/device info
        browser: agent.family || event.browser || 'Unknown',
        browser_version: agent.toVersion() || '',
        os: agent.os.family || event.os || 'Unknown',
        os_version: agent.os.toVersion() || '',
        
        // Server-side timestamp for accuracy
        server_timestamp: new Date().toISOString(),
        
        // Clean up data types
        page_load_time: parseInt(event.page_load_time) || 0,
        scroll_depth: parseInt(event.scroll_depth) || 0,
        max_scroll_depth: parseInt(event.max_scroll_depth) || 0,
        session_duration: parseInt(event.session_duration) || 0,
        page_count: parseInt(event.page_count) || 1,
        click_x: parseInt(event.click_x) || 0,
        click_y: parseInt(event.click_y) || 0,
        screen_width: parseInt(event.screen_width) || 0,
        screen_height: parseInt(event.screen_height) || 0,
        viewport_width: parseInt(event.viewport_width) || 0,
        viewport_height: parseInt(event.viewport_height) || 0,
        
        // Ensure strings are not null
        element_id: event.element_id || '',
        element_class: event.element_class || '',
        element_tag: event.element_tag || '',
        referrer: event.referrer || '',
        page_title: event.page_title || '',
        user_agent: event.user_agent || req.headers['user-agent'] || ''
    };
}

// Validate event data
function validateEvent(event) {
    const required = ['session_id', 'event_type', 'page_url'];
    const missing = required.filter(field => !event[field]);
    
    if (missing.length > 0) {
        throw new Error(`Missing required fields: ${missing.join(', ')}`);
    }
    
    // Validate event type
    const validTypes = ['page_view', 'click', 'scroll', 'session_start', 'session_end', 'page_exit', 'custom', 'session_update'];
    if (!validTypes.includes(event.event_type)) {
        throw new Error(`Invalid event type: ${event.event_type}`);
    }
    
    return true;
}

// Main analytics endpoint
app.post('/analytics/events', async (req, res) => {
    try {
        const { events } = req.body;
        
        if (!Array.isArray(events) || events.length === 0) {
            return res.status(400).json({ 
                error: 'Events array is required and must not be empty' 
            });
        }
        
        if (events.length > 1000) {
            return res.status(400).json({ 
                error: 'Too many events in single request (max 1000)' 
            });
        }
        
        // Process and validate events
        const enrichedEvents = [];
        const errors = [];
        
        for (let i = 0; i < events.length; i++) {
            try {
                const event = events[i];
                validateEvent(event);
                const enrichedEvent = enrichEvent(event, req);
                enrichedEvents.push(enrichedEvent);
            } catch (error) {
                errors.push({ index: i, error: error.message });
            }
        }
        
        if (enrichedEvents.length === 0) {
            return res.status(400).json({ 
                error: 'No valid events to process',
                validation_errors: errors
            });
        }

        // Batch insert to ClickHouse
        await clickhouse.insert({
            table: 'analytics_events',
            values: enrichedEvents,
            format: 'JSONEachRow'
        });
        
        console.log(`Processed ${enrichedEvents.length} analytics events`);
        
        res.status(200).json({ 
            success: true,
            processed: enrichedEvents.length,
            errors: errors.length > 0 ? errors : undefined
        });
        
    } catch (error) {
        console.error('Analytics processing error:', error);
        res.status(500).json({ 
            error: 'Failed to process events',
            details: process.env.NODE_ENV === 'development' ? error.message : undefined
        });
    }
});

// Dashboard data endpoint for QuickSight
app.get('/analytics/dashboard', async (req, res) => {
    try {
        const { timeRange = '24h', timezone = 'UTC' } = req.query;
        
        // Parse time range
        let interval;
        switch (timeRange) {
            case '1h': interval = '1 HOUR'; break;
            case '24h': interval = '24 HOUR'; break;
            case '7d': interval = '7 DAY'; break;
            case '30d': interval = '30 DAY'; break;
            default: interval = '24 HOUR';
        }
        
        const queries = await Promise.all([
            // Page views by hour
            clickhouse.query({
                query: `
                    SELECT 
                        toStartOfHour(timestamp) as hour,
                        count() as page_views,
                        uniq(session_id) as unique_sessions,
                        uniq(user_id) as unique_users
                    FROM analytics_events 
                    WHERE event_type = 'page_view' 
                        AND timestamp >= now() - INTERVAL ${interval}
                    GROUP BY hour 
                    ORDER BY hour
                `,
                format: 'JSONEachRow'
            }),
            
            // Top pages
            clickhouse.query({
                query: `
                    SELECT 
                        page_url,
                        count() as views,
                        uniq(session_id) as unique_sessions,
                        avg(page_load_time) as avg_load_time,
                        avg(max_scroll_depth) as avg_scroll_depth
                    FROM analytics_events 
                    WHERE event_type = 'page_view'
                        AND timestamp >= now() - INTERVAL ${interval}
                    GROUP BY page_url 
                    ORDER BY views DESC 
                    LIMIT 20
                `,
                format: 'JSONEachRow'
            }),
            
            // Device and browser breakdown
            clickhouse.query({
                query: `
                    SELECT 
                        device_type,
                        browser,
                        count() as sessions,
                        avg(session_duration) as avg_duration
                    FROM analytics_events 
                    WHERE event_type IN ('page_view', 'session_start')
                        AND timestamp >= now() - INTERVAL ${interval}
                    GROUP BY device_type, browser
                    ORDER BY sessions DESC
                `,
                format: 'JSONEachRow'
            }),
            
            // Geographic data
            clickhouse.query({
                query: `
                    SELECT 
                        country,
                        city,
                        count() as sessions,
                        uniq(user_id) as unique_users
                    FROM analytics_events 
                    WHERE timestamp >= now() - INTERVAL ${interval}
                    GROUP BY country, city
                    ORDER BY sessions DESC
                    LIMIT 50
                `,
                format: 'JSONEachRow'
            }),
            
            // Click heatmap data
            clickhouse.query({
                query: `
                    SELECT 
                        page_url,
                        element_tag,
                        element_id,
                        element_class,
                        count() as clicks,
                        avg(click_x) as avg_x,
                        avg(click_y) as avg_y
                    FROM analytics_events 
                    WHERE event_type = 'click'
                        AND timestamp >= now() - INTERVAL ${interval}
                    GROUP BY page_url, element_tag, element_id, element_class
                    HAVING clicks > 5
                    ORDER BY clicks DESC
                    LIMIT 100
                `,
                format: 'JSONEachRow'
            })
        ]);
        
        const [hourlyData, topPages, deviceData, geoData, clickData] = queries;
        
        res.json({
            timeRange,
            generatedAt: new Date().toISOString(),
            data: {
                hourlyViews: await hourlyData.json(),
                topPages: await topPages.json(),
                deviceBreakdown: await deviceData.json(),
                geographic: await geoData.json(),
                clickHeatmap: await clickData.json()
            }
        });
        
    } catch (error) {
        console.error('Dashboard data error:', error);
        res.status(500).json({ 
            error: 'Failed to fetch dashboard data',
            details: process.env.NODE_ENV === 'development' ? error.message : undefined
        });
    }
});

// Real-time metrics endpoint
app.get('/analytics/realtime', async (req, res) => {
    try {
        const result = await clickhouse.query({
            query: `
                SELECT 
                    count() as active_sessions,
                    uniq(user_id) as active_users,
                    countIf(event_type = 'page_view') as page_views_last_hour,
                    countIf(event_type = 'click') as clicks_last_hour
                FROM analytics_events 
                WHERE timestamp >= now() - INTERVAL 1 HOUR
            `,
            format: 'JSONEachRow'
        });
        
        const metrics = await result.json();
        res.json({
            realtime: true,
            timestamp: new Date().toISOString(),
            metrics: metrics[0] || {}
        });
        
    } catch (error) {
        console.error('Real-time metrics error:', error);
        res.status(500).json({ error: 'Failed to fetch real-time metrics' });
    }
});

// User journey analysis
app.get('/analytics/user-journey/:sessionId', async (req, res) => {
    try {
        const { sessionId } = req.params;
        
        const result = await clickhouse.query({
            query: `
                SELECT 
                    timestamp,
                    event_type,
                    page_url,
                    page_title,
                    element_tag,
                    element_id,
                    scroll_depth,
                    session_duration
                FROM analytics_events 
                WHERE session_id = {sessionId:String}
                ORDER BY timestamp
            `,
            query_params: { sessionId },
            format: 'JSONEachRow'
        });
        
        const journey = await result.json();
        res.json({
            sessionId,
            events: journey,
            summary: {
                totalEvents: journey.length,
                duration: journey.length > 0 ? 
                    Math.max(...journey.map(e => e.session_duration)) : 0,
                pagesVisited: [...new Set(journey
                    .filter(e => e.event_type === 'page_view')
                    .map(e => e.page_url))].length
            }
        });
        
    } catch (error) {
        console.error('User journey error:', error);
        res.status(500).json({ error: 'Failed to fetch user journey' });
    }
});

// Performance metrics
app.get('/analytics/performance', async (req, res) => {
    try {
        const result = await clickhouse.query({
            query: `
                SELECT 
                    page_url,
                    avg(page_load_time) as avg_load_time,
                    quantile(0.5)(page_load_time) as median_load_time,
                    quantile(0.95)(page_load_time) as p95_load_time,
                    count() as samples
                FROM analytics_events 
                WHERE event_type = 'page_view' 
                    AND page_load_time > 0
                    AND timestamp >= now() - INTERVAL 24 HOUR
                GROUP BY page_url
                HAVING samples >= 10
                ORDER BY avg_load_time DESC
            `,
            format: 'JSONEachRow'
        });
        
        const performance = await result.json();
        res.json({
            timestamp: new Date().toISOString(),
            pagePerformance: performance
        });
        
    } catch (error) {
        console.error('Performance metrics error:', error);
        res.status(500).json({ error: 'Failed to fetch performance metrics' });
    }
});

// Error handling middleware
app.use((error, req, res, next) => {
    console.error('Unhandled error:', error);
    res.status(500).json({ 
        error: 'Internal server error',
        requestId: req.headers['x-request-id'] || generateUUID()
    });
});

// 404 handler
app.use((req, res) => {
    res.status(404).json({ error: 'Endpoint not found' });
});

// Graceful shutdown
process.on('SIGTERM', async () => {
    console.log('Received SIGTERM, shutting down gracefully');
    await clickhouse.close();
    process.exit(0);
});

process.on('SIGINT', async () => {
    console.log('Received SIGINT, shutting down gracefully');
    await clickhouse.close();
    process.exit(0);
});

// Start server
app.listen(PORT, () => {
    console.log(`Analytics service running on port ${PORT}`);
    console.log(`Environment: ${process.env.NODE_ENV || 'development'}`);
    console.log(`ClickHouse host: ${process.env.CLICKHOUSE_HOST || 'localhost:8123'}`);
});

module.exports = app;