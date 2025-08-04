-- Test queries to verify the analytics tables are working correctly
-- Run these after creating the tables to ensure everything is set up properly

USE analytics;

-- Test 1: Check if all tables exist
SELECT 'Table existence check:' as test;
SHOW TABLES IN analytics;

-- Test 2: Check table structures
SELECT 'Page view events structure:' as test;
DESCRIBE analytics.page_view_events;

SELECT 'Click events structure:' as test;
DESCRIBE analytics.click_events;

SELECT 'Scroll events structure:' as test;
DESCRIBE analytics.scroll_events;

SELECT 'Session events structure:' as test;
DESCRIBE analytics.session_events;

-- Test 3: Test basic SELECT queries (should return empty results but no errors)
SELECT 'Testing page view events query:' as test;
SELECT count() as page_view_count FROM analytics.page_view_events;

SELECT 'Testing click events query:' as test;
SELECT count() as click_count FROM analytics.click_events;

SELECT 'Testing scroll events query:' as test;
SELECT count() as scroll_count FROM analytics.scroll_events;

SELECT 'Testing session events query:' as test;
SELECT count() as session_count FROM analytics.session_events;

-- Test 4: Test the exact query from getTopPages method
SELECT 'Testing getTopPages query format:' as test;
SELECT 
    page_url,
    page_title,
    count() as views,
    uniq(session_id) as unique_sessions,
    avg(page_load_time) as avg_load_time,
    avg(time_on_page) as avg_time_on_page,
    countIf(is_bounce = 1) as bounces,
    (countIf(is_bounce = 1) / count()) * 100 as bounce_rate
FROM analytics.page_view_events 
WHERE timestamp >= now() - INTERVAL 24 HOUR
GROUP BY page_url, page_title 
ORDER BY views DESC 
LIMIT 20;

SELECT 'All tests completed successfully!' as result;
