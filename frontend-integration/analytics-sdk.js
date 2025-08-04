/**
 * Simple Analytics SDK for Frontend Integration
 * 
 * This is a minimal analytics library that sends simplified events to your Spring Boot analytics service.
 * Usage:
 * 1. Include this script in your HTML
 * 2. Initialize with Analytics.init({apiUrl: 'http://localhost:3000/analytics/events'})
 * 3. Track events using the provided methods
 */

class AnalyticsSDK {
    constructor() {
        this.apiUrl = '';
        this.sessionId = this.generateSessionId();
        this.userId = localStorage.getItem('user_id') || null;
        this.queue = [];
        this.isInitialized = false;
    }

    init(config) {
        this.apiUrl = config.apiUrl;
        this.isInitialized = true;
        
        // Auto-track page views
        this.trackPageView();
        
        // Setup auto-tracking for clicks and scrolls
        this.setupAutoTracking();
        
        console.log('Analytics SDK initialized');
    }

    generateSessionId() {
        return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }

    setUserId(userId) {
        this.userId = userId;
        localStorage.setItem('user_id', userId);
    }

    // Track page view (automatically called on init)
    trackPageView(customData = {}) {
        const event = {
            session_id: this.sessionId,
            user_id: this.userId,
            page_url: window.location.href,
            page_title: document.title,
            referrer: document.referrer,
            load_time: performance.timing ? performance.timing.loadEventEnd - performance.timing.navigationStart : 0,
            timestamp: new Date().toISOString(),
            ...customData
        };

        this.sendEvent('page_views', [event]);
    }

    // Track click events
    trackClick(element, customData = {}) {
        const rect = element.getBoundingClientRect();
        const event = {
            session_id: this.sessionId,
            user_id: this.userId,
            page_url: window.location.href,
            element_id: element.id || '',
            element_text: element.textContent || element.innerText || '',
            click_x: Math.round(rect.left + rect.width / 2),
            click_y: Math.round(rect.top + rect.height / 2),
            timestamp: new Date().toISOString(),
            ...customData
        };

        this.sendEvent('clicks', [event]);
    }

    // Track scroll events
    trackScroll(customData = {}) {
        const scrollDepth = Math.round(window.scrollY);
        const scrollPercentage = Math.round((window.scrollY / (document.body.scrollHeight - window.innerHeight)) * 100);
        
        const event = {
            session_id: this.sessionId,
            user_id: this.userId,
            page_url: window.location.href,
            scroll_depth: scrollDepth,
            scroll_percentage: Math.min(scrollPercentage, 100),
            timestamp: new Date().toISOString(),
            ...customData
        };

        this.sendEvent('scrolls', [event]);
    }

    // Track session events
    trackSession(eventType, customData = {}) {
        const event = {
            session_id: this.sessionId,
            user_id: this.userId,
            page_url: window.location.href,
            event_type: eventType, // 'session_start', 'session_end', 'session_update'
            duration: 0,
            page_count: 1,
            timestamp: new Date().toISOString(),
            ...customData
        };

        this.sendEvent('sessions', [event]);
    }

    // Setup automatic event tracking
    setupAutoTracking() {
        // Auto-track clicks
        document.addEventListener('click', (e) => {
            // Only track clicks on interactive elements
            if (e.target.tagName === 'BUTTON' || e.target.tagName === 'A' || e.target.onclick) {
                this.trackClick(e.target);
            }
        });

        // Auto-track scroll (throttled)
        let scrollTimeout;
        window.addEventListener('scroll', () => {
            clearTimeout(scrollTimeout);
            scrollTimeout = setTimeout(() => {
                this.trackScroll();
            }, 500); // Throttle scroll events
        });

        // Track session start
        this.trackSession('session_start');

        // Track session end on page unload
        window.addEventListener('beforeunload', () => {
            this.trackSession('session_end');
        });
    }

    // Send events to the analytics service
    sendEvent(eventType, events) {
        if (!this.isInitialized) {
            console.warn('Analytics SDK not initialized');
            return;
        }

        const payload = {};
        payload[eventType] = events;

        fetch(this.apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log(`Successfully tracked ${events.length} ${eventType} event(s)`);
            } else {
                console.error('Analytics tracking failed:', data.message);
            }
        })
        .catch(error => {
            console.error('Analytics request failed:', error);
        });
    }

    // Custom event tracking for specific business logic
    track(eventName, properties = {}) {
        // You can extend this method to handle custom events
        console.log(`Custom event: ${eventName}`, properties);
    }
}

// Global Analytics instance
const Analytics = new AnalyticsSDK();

// Export for module systems
if (typeof module !== 'undefined' && module.exports) {
    module.exports = Analytics;
}

// Make available globally
window.Analytics = Analytics;
