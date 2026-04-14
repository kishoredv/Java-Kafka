package com.example.kafka.fileprocessor.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
@Configuration
public class H2ServerConfiguration {

    private Server h2Server;

    @EventListener(ApplicationReadyEvent.class)
    public void startH2Server() {
        try {
            log.info("🚀 Starting H2 TCP Server...");
            h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9093");
            h2Server.start();
            log.info("✅ H2 TCP Server started successfully on port 9093");
            log.info("📡 Publisher can connect via: jdbc:h2:tcp://localhost:9093/mem:fileprocessordb");
        } catch (SQLException e) {
            log.error("❌ Failed to start H2 TCP Server on port 9093", e);
            log.error("🔍 Error details: {}", e.getMessage());
            // Don't throw exception - let application continue with embedded H2 only
        }
    }

    @PreDestroy
    public void stopH2Server() {
        if (h2Server != null && h2Server.isRunning(true)) {
            log.info("🛑 Stopping H2 TCP Server...");
            h2Server.stop();
            log.info("✅ H2 TCP Server stopped");
        }
    }
}

