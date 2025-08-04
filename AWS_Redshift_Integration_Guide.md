# AWS Redshift Integration Guide

This document provides instructions for setting up and configuring AWS Redshift integration with the Analytics Service.

## Prerequisites

1. AWS Account with Redshift access
2. Redshift cluster created and running
3. Proper IAM permissions for Redshift access
4. Network connectivity from your application to Redshift

## 1. Redshift Cluster Setup

### Create a Redshift Cluster

1. Go to AWS Redshift Console
2. Click "Create cluster"
3. Configure cluster settings:
   - **Cluster identifier**: `lugx-analytics-cluster`
   - **Database name**: `analytics`
   - **Master username**: `admin`
   - **Master password**: Choose a secure password
   - **Node type**: `dc2.large` (for development) or `ra3.xlplus` (for production)
   - **Number of nodes**: 1 (for development) or 2+ (for production)

### Network Configuration

1. **VPC Security Group**: Create or use existing security group
2. **Inbound Rules**: 
   - Type: Redshift
   - Port: 5439
   - Source: Your application's IP range or VPC CIDR
3. **Publicly accessible**: Enable if connecting from outside VPC

## 2. Database Schema Setup

### Connect to Redshift

Use any PostgreSQL-compatible client:
- **Host**: `lugx-analytics-cluster.xxxxxxxxx.us-east-1.redshift.amazonaws.com`
- **Port**: `5439`
- **Database**: `analytics`
- **Username**: `admin`
- **Password**: Your chosen password

### Create Tables

Run the SQL script provided in `redshift_schema.sql`:

```bash
psql -h your-cluster-endpoint -p 5439 -d analytics -U admin -f redshift_schema.sql
```

Or use the Redshift Query Editor in AWS Console to execute the DDL statements.

## 3. IAM Permissions

### Create IAM Role for Redshift Access

Create an IAM policy with the following permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "redshift:DescribeClusters",
                "redshift:GetClusterCredentials"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "redshift-data:ExecuteStatement",
                "redshift-data:DescribeStatement",
                "redshift-data:GetStatementResult",
                "redshift-data:ListStatements"
            ],
            "Resource": "*"
        }
    ]
}
```

### Attach Role to EC2 Instance or Use Access Keys

- **Option 1**: Attach IAM role to EC2 instance running the application
- **Option 2**: Use IAM user access keys (set in environment variables)

## 4. Application Configuration

### Environment Variables

Set the following environment variables:

```bash
# Enable Redshift integration
REDSHIFT_ENABLED=true

# Redshift cluster configuration
REDSHIFT_CLUSTER_ID=lugx-analytics-cluster
REDSHIFT_DATABASE=analytics
REDSHIFT_ENDPOINT=lugx-analytics-cluster.xxxxxxxxx.us-east-1.redshift.amazonaws.com
REDSHIFT_PORT=5439
REDSHIFT_USERNAME=admin
REDSHIFT_PASSWORD=YourSecurePassword123
REDSHIFT_SCHEMA=public

# Redshift performance settings
REDSHIFT_ASYNC_ENABLED=true
REDSHIFT_BATCH_SIZE=1000
REDSHIFT_CONNECTION_TIMEOUT=30
REDSHIFT_QUERY_TIMEOUT=300

# AWS credentials (if not using IAM role)
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
```

### Application Properties

Update `application.properties` with your Redshift configuration:

```properties
# AWS Redshift Configuration
aws.redshift.enabled=true
aws.redshift.cluster.id=lugx-analytics-cluster
aws.redshift.database=analytics
aws.redshift.cluster.endpoint=lugx-analytics-cluster.xxxxxxxxx.us-east-1.redshift.amazonaws.com
aws.redshift.port=5439
aws.redshift.username=admin
aws.redshift.password=YourSecurePassword123
aws.redshift.schema=public
aws.redshift.async.enabled=true
aws.redshift.batch.size=1000
aws.redshift.connection.timeout=30
aws.redshift.query.timeout=300
```

## 5. Testing the Integration

### Health Check

Test the integration using the health endpoint:

```bash
curl http://localhost:5000/analytics-service/health
```

Expected response:
```json
{
    "status": "healthy",
    "timestamp": "2024-01-20T10:30:00",
    "service": "analytics",
    "databases": {
        "clickhouse": {
            "status": "healthy",
            "database": "default"
        },
        "redshift": {
            "status": "healthy",
            "database": "analytics",
            "enabled": true
        }
    },
    "features": {
        "clickhouse": true,
        "redshift": true,
        "s3_export": true
    }
}
```

### Send Test Events

Send analytics events to verify data is being written to both ClickHouse and Redshift:

```bash
curl -X POST http://localhost:5000/analytics-service/events \
  -H "Content-Type: application/json" \
  -d '{
    "page_views": [{
      "session_id": "test-session-123",
      "user_id": "test-user-456",
      "page_url": "https://example.com/test",
      "page_title": "Test Page",
      "timestamp": "2024-01-20T10:30:00Z"
    }]
  }'
```

### Verify Data in Redshift

Connect to Redshift and verify data was inserted:

```sql
SELECT COUNT(*) FROM public.page_view_events WHERE session_id = 'test-session-123';
SELECT * FROM public.page_view_events ORDER BY timestamp DESC LIMIT 10;
```

## 6. Monitoring and Maintenance

### Performance Monitoring

1. **CloudWatch Metrics**: Monitor Redshift cluster performance
2. **Query Performance**: Use Redshift console to monitor query execution
3. **Connection Monitoring**: Check application logs for connection issues

### Maintenance Tasks

1. **Vacuum Tables**: Regularly vacuum tables to reclaim space
   ```sql
   VACUUM public.page_view_events;
   VACUUM public.click_events;
   VACUUM public.scroll_events;
   VACUUM public.session_events;
   ```

2. **Analyze Tables**: Update table statistics
   ```sql
   ANALYZE public.page_view_events;
   ANALYZE public.click_events;
   ANALYZE public.scroll_events;
   ANALYZE public.session_events;
   ```

3. **Refresh Materialized Views**:
   ```sql
   REFRESH MATERIALIZED VIEW public.daily_analytics;
   ```

## 7. Troubleshooting

### Common Issues

1. **Connection Timeout**
   - Check security group rules
   - Verify network connectivity
   - Check cluster status

2. **Authentication Errors**
   - Verify username/password
   - Check IAM permissions
   - Ensure cluster is publicly accessible if needed

3. **Performance Issues**
   - Increase batch size for bulk inserts
   - Check cluster capacity
   - Monitor concurrent connections

### Logging

Enable debug logging for detailed troubleshooting:

```properties
logging.level.com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.service.RedshiftService=DEBUG
logging.level.com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.config.RedshiftConfig=DEBUG
```

### Support

- AWS Redshift Documentation: https://docs.aws.amazon.com/redshift/
- PostgreSQL JDBC Driver: https://jdbc.postgresql.org/
- AWS SDK for Java: https://docs.aws.amazon.com/sdk-for-java/

## 8. Cost Optimization

### Development Environment
- Use `dc2.large` nodes with 1 node
- Enable automatic pause for non-production clusters
- Use scheduled scaling for predictable workloads

### Production Environment
- Use `ra3` instance types for better performance
- Configure automatic scaling based on workload
- Set up reserved instances for cost savings
- Monitor usage with AWS Cost Explorer

## 9. Security Best Practices

1. **Network Security**
   - Use VPC with private subnets
   - Restrict security group rules to necessary ports/IPs
   - Enable VPC Flow Logs

2. **Data Encryption**
   - Enable encryption at rest
   - Use SSL/TLS for data in transit
   - Manage encryption keys with AWS KMS

3. **Access Control**
   - Use IAM roles instead of access keys when possible
   - Implement least privilege access
   - Regular rotate credentials
   - Enable CloudTrail for audit logging

4. **Monitoring**
   - Set up CloudWatch alarms for unusual activity
   - Monitor failed login attempts
   - Track data access patterns
