# AWS QuickSight Integration Guide

This guide explains how to integrate your Spring Boot Analytics Service with AWS QuickSight through S3.

## 🏗️ Architecture Overview

```
ClickHouse → Spring Boot Analytics Service → AWS S3 → AWS QuickSight
```

Your analytics data flows from ClickHouse to S3 in CSV format, which QuickSight can use as a data source.

## 🔧 Prerequisites

1. **AWS Account** with appropriate permissions
2. **S3 Bucket** for storing analytics data
3. **AWS IAM User** with S3 and QuickSight permissions
4. **QuickSight Account** (Standard or Enterprise)

## 📋 Setup Steps

### Step 1: Configure AWS Credentials

Update your `application.properties`:

```properties
# AWS Configuration
aws.access-key-id=YOUR_AWS_ACCESS_KEY_ID
aws.secret-access-key=YOUR_AWS_SECRET_ACCESS_KEY
aws.region=us-east-1
aws.s3.bucket-name=your-analytics-bucket
aws.s3.quicksight-prefix=quicksight/analytics/
```

### Step 2: Create S3 Bucket

```bash
# Create the S3 bucket
aws s3 mb s3://your-analytics-bucket

# Set up folder structure
aws s3api put-object --bucket your-analytics-bucket --key quicksight/analytics/
```

### Step 3: IAM Permissions

Create an IAM policy with the following permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:PutObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::your-analytics-bucket",
                "arn:aws:s3:::your-analytics-bucket/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "quicksight:CreateDataSet",
                "quicksight:CreateDataSource",
                "quicksight:UpdateDataSet",
                "quicksight:UpdateDataSource"
            ],
            "Resource": "*"
        }
    ]
}
```

## 📊 Available Export Endpoints

### Manual Exports

1. **Export All Data**
   ```
   POST /analytics/export/all?timeRange=24h
   ```

2. **Export Specific Data Types**
   ```
   POST /analytics/export/page-views?timeRange=24h
   POST /analytics/export/top-pages?timeRange=24h
   POST /analytics/export/click-analytics?timeRange=24h
   POST /analytics/export/scroll-analytics?timeRange=24h
   POST /analytics/export/session-analytics?timeRange=24h
   POST /analytics/export/realtime-metrics
   POST /analytics/export/performance-metrics
   ```

3. **Export User Journey**
   ```
   POST /analytics/export/user-journey/{sessionId}
   ```

### Automated Exports

The system automatically exports data:
- **Every 15 minutes**: Real-time metrics
- **Every hour**: Complete analytics (24h data)
- **Every 6 hours**: Daily trends (7d data)
- **Every Sunday**: Weekly summaries (30d data)

## 🎯 QuickSight Data Sources

### 1. Create S3 Data Source in QuickSight

1. **Login to QuickSight Console**
2. **Go to Manage Data → New Data Set**
3. **Select S3 as data source**
4. **Configure S3 connection:**
   - Data source name: `Analytics Data`
   - S3 bucket: `your-analytics-bucket`
   - Folder: `quicksight/analytics/`

### 2. Data Sets for Different Analytics

#### Page View Analytics
- **File Pattern**: `page_view_analytics_*.csv`
- **Fields**: hour, page_views, unique_sessions, unique_users, avg_load_time, avg_time_on_page
- **Use Case**: Traffic trends, performance monitoring

#### Top Pages
- **File Pattern**: `top_pages_*.csv`
- **Fields**: page_url, page_title, views, unique_sessions, avg_load_time, avg_time_on_page, bounces, bounce_rate
- **Use Case**: Content performance, popular pages

#### Click Analytics
- **File Pattern**: `click_analytics_*.csv`
- **Fields**: page_url, element_tag, element_id, element_class, clicks, unique_sessions, avg_x, avg_y, double_clicks
- **Use Case**: User interaction heatmaps, UI optimization

#### Session Analytics
- **File Pattern**: `session_analytics_*.csv`
- **Fields**: avg_session_duration, avg_pages_per_session, avg_clicks_per_session, total_bounces, total_sessions, bounce_rate
- **Use Case**: User engagement, conversion analysis

#### Real-time Metrics
- **File Pattern**: `realtime_metrics_*.csv`
- **Fields**: page_views_last_hour, clicks_last_hour, scroll_events_last_hour, active_sessions, active_users
- **Use Case**: Live monitoring dashboards

## 📈 Recommended QuickSight Visualizations

### Dashboard 1: Traffic Overview
- **Line Chart**: Page views over time (hourly)
- **KPI Cards**: Total sessions, unique users, bounce rate
- **Bar Chart**: Top 10 pages by views
- **Donut Chart**: Traffic by device type

### Dashboard 2: User Engagement
- **Histogram**: Session duration distribution
- **Heat Map**: Click positions on popular pages
- **Scatter Plot**: Page load time vs bounce rate
- **Line Chart**: Scroll depth trends

### Dashboard 3: Performance Monitoring
- **Gauge Charts**: Page load times (P50, P95)
- **Bar Chart**: Slowest pages
- **Time Series**: Performance trends
- **Table**: Detailed performance metrics

### Dashboard 4: Real-time Monitoring
- **KPI Cards**: Live metrics (auto-refresh every 5 minutes)
- **Line Chart**: Real-time activity
- **Map**: Geographic distribution of users
- **Alert Widgets**: Performance thresholds

## 🔄 Data Refresh Strategy

### Option 1: SPICE (In-Memory)
- **Pros**: Fast query performance
- **Cons**: Manual refresh required, additional cost
- **Best for**: Historical analysis, complex aggregations

### Option 2: Direct Query
- **Pros**: Always current data, no additional storage cost
- **Cons**: Slower performance for large datasets
- **Best for**: Real-time dashboards, simple visualizations

### Option 3: Hybrid Approach
- **Real-time data**: Direct query from latest files
- **Historical data**: SPICE for faster aggregations
- **Scheduled refresh**: Daily for historical datasets

## 🚨 Monitoring and Alerts

### Set up QuickSight Alerts for:
1. **Traffic Anomalies**: Sudden drops or spikes in page views
2. **Performance Issues**: Page load times above threshold
3. **Error Rates**: High bounce rates or failed requests
4. **User Engagement**: Low session duration or page views

### CloudWatch Integration:
```bash
# Monitor S3 export success/failure
aws logs create-log-group --log-group-name /analytics/s3-exports
```

## 💡 Best Practices

### Data Organization
- Use date partitioning: `yyyy/MM/dd/HH/` format
- Include metadata in file names
- Compress large files (gzip)
- Use consistent CSV formatting

### Performance Optimization
- Aggregate data at source when possible
- Use columnar formats (Parquet) for large datasets
- Implement data lifecycle policies
- Monitor S3 costs and optimize storage classes

### Security
- Enable S3 bucket encryption
- Use IAM roles instead of access keys in production
- Implement VPC endpoints for private connectivity
- Enable CloudTrail for audit logging

## 🧪 Testing Your Integration

### 1. Test Data Export
```bash
# Test manual export
curl -X POST "http://localhost:3000/analytics/export/all?timeRange=1h"
```

### 2. Verify S3 Files
```bash
# List exported files
aws s3 ls s3://your-analytics-bucket/quicksight/analytics/ --recursive
```

### 3. Test QuickSight Connection
1. Create a test dataset in QuickSight
2. Verify data appears correctly
3. Create a simple visualization
4. Test refresh functionality

## 📞 Troubleshooting

### Common Issues

1. **Permission Denied**
   - Check IAM permissions
   - Verify S3 bucket policy
   - Ensure QuickSight has S3 access

2. **Empty Datasets**
   - Verify data exists in ClickHouse
   - Check export service logs
   - Validate CSV format

3. **QuickSight Import Errors**
   - Check CSV headers
   - Verify data types
   - Review QuickSight limits

### Useful Commands

```bash
# Check S3 export logs
kubectl logs -f deployment/analytics-service | grep S3Export

# Monitor QuickSight usage
aws quicksight describe-data-set --aws-account-id YOUR_ACCOUNT_ID --data-set-id YOUR_DATASET_ID

# Test S3 connectivity
aws s3 ls s3://your-analytics-bucket/
```

## 🎉 Next Steps

1. **Set up automated exports** by deploying the application
2. **Create QuickSight data sources** following the guide above
3. **Build your first dashboard** using the recommended visualizations
4. **Set up alerts and monitoring** for critical metrics
5. **Optimize performance** based on usage patterns

Your analytics data will now be available in QuickSight for powerful business intelligence and visualization!
