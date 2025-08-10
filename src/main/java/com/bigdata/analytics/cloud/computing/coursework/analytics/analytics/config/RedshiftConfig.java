package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshiftdata.RedshiftDataClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(name = "aws.redshift.enabled", havingValue = "true", matchIfMissing = false)
public class RedshiftConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedshiftConfig.class);

    @Value("${aws.region:eu-north-1}")
    private String awsRegion;

    @Value("${aws.redshift.cluster.endpoint}")
    private String redshiftEndpoint;

    @Value("${aws.redshift.port:5439}")
    private int redshiftPort;

    @Value("${aws.redshift.database}")
    private String redshiftDatabase;

    @Value("${aws.redshift.username}")
    private String redshiftUsername;

    @Value("${aws.redshift.password}")
    private String redshiftPassword;

    @Value("${aws.redshift.connection.timeout:30}")
    private int connectionTimeout;

    @Bean
    public RedshiftClient redshiftClient() {
        try {
            RedshiftClient client = RedshiftClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            
            logger.info("Redshift client initialized successfully for region: {}", awsRegion);
            return client;
        } catch (Exception e) {
            logger.error("Failed to initialize Redshift client", e);
            throw new RuntimeException("Failed to initialize Redshift client", e);
        }
    }

    @Bean
    public RedshiftDataClient redshiftDataClient() {
        try {
            RedshiftDataClient client = RedshiftDataClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            
            logger.info("Redshift Data API client initialized successfully for region: {}", awsRegion);
            return client;
        } catch (Exception e) {
            logger.error("Failed to initialize Redshift Data API client", e);
            throw new RuntimeException("Failed to initialize Redshift Data API client", e);
        }
    }

    @Bean(name = "redshiftConnection")
    public Connection redshiftConnection() {
        try {
            String jdbcUrl;
            
            if (redshiftEndpoint.startsWith("jdbc:redshift://")) {
                // For Redshift Serverless, use the Amazon Redshift JDBC driver
                Class.forName("com.amazon.redshift.jdbc.Driver");
                jdbcUrl = redshiftEndpoint + "/" + redshiftDatabase;
                logger.info("Using Amazon Redshift JDBC driver for Redshift Serverless");
            } else {
                // For regular Redshift cluster endpoints, use PostgreSQL driver
                Class.forName("org.postgresql.Driver");
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", 
                    redshiftEndpoint, redshiftPort, redshiftDatabase);
                logger.info("Using PostgreSQL JDBC driver for Redshift cluster");
            }
            
            Properties props = new Properties();
            props.setProperty("user", redshiftUsername);
            props.setProperty("password", redshiftPassword);
            props.setProperty("ssl", "true");
            
            // Different SSL settings for different drivers
            if (redshiftEndpoint.startsWith("jdbc:redshift://")) {
                props.setProperty("sslMode", "require");
            } else {
                props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
            }
            
            props.setProperty("loginTimeout", String.valueOf(connectionTimeout));
            props.setProperty("socketTimeout", String.valueOf(connectionTimeout * 1000));
            props.setProperty("tcpKeepAlive", "true");
            
            Connection connection = DriverManager.getConnection(jdbcUrl, props);
            connection.setAutoCommit(false); // Use transactions for better performance
            
            logger.info("Redshift JDBC connection established successfully to: {}", jdbcUrl);
            return connection;
            
        } catch (ClassNotFoundException e) {
            logger.error("JDBC driver not found", e);
            throw new RuntimeException("JDBC driver not found", e);
        } catch (SQLException e) {
            logger.error("Failed to connect to Redshift: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to connect to Redshift", e);
        }
    }

    @Bean
    public RedshiftConnectionProperties redshiftConnectionProperties() {
        return new RedshiftConnectionProperties(
            redshiftEndpoint,
            redshiftPort,
            redshiftDatabase,
            redshiftUsername,
            redshiftPassword
        );
    }

    public static class RedshiftConnectionProperties {
        private final String endpoint;
        private final int port;
        private final String database;
        private final String username;
        private final String password;

        public RedshiftConnectionProperties(String endpoint, int port, String database, 
                                          String username, String password) {
            this.endpoint = endpoint;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
        }

        public String getEndpoint() { return endpoint; }
        public int getPort() { return port; }
        public String getDatabase() { return database; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        
        public String getJdbcUrl() {
            if (endpoint.startsWith("jdbc:redshift://")) {
                // For Redshift Serverless, use the full URL
                return endpoint + "/" + database;
            } else {
                // For regular Redshift cluster endpoints
                return String.format("jdbc:postgresql://%s:%d/%s", endpoint, port, database);
            }
        }
    }
}
