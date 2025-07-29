package com.bigdata.analytics.cloud.computing.coursework.analytics.analytics.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class ClickHouseConfig {

    private static final Logger logger = LoggerFactory.getLogger(ClickHouseConfig.class);

    @Value("${clickhouse.url}")
    private String clickhouseUrl;

    @Value("${clickhouse.username}")
    private String username;

    @Value("${clickhouse.password}")
    private String password;

    @Value("${clickhouse.database}")
    private String database;

    @Bean
    public Connection clickHouseConnection() throws SQLException {
        try {
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");

            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("compress", "0");
            props.setProperty("decompress", "0");
            props.setProperty("socket_timeout", "30000");
            props.setProperty("connection_timeout", "10000");

            logger.info("Connecting to ClickHouse at: {} with database: {}", clickhouseUrl, database);
            Connection connection = DriverManager.getConnection(clickhouseUrl, props);

            logger.info("Successfully connected to ClickHouse database: {}", database);

            return connection;
        } catch (ClassNotFoundException e) {
            logger.error("ClickHouse JDBC driver not found", e);
            throw new RuntimeException("ClickHouse JDBC driver not found", e);
        } catch (SQLException e) {
            logger.error("Failed to connect to ClickHouse: {}", e.getMessage(), e);
            throw e;
        }
    }
}
