package io.orchestra.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Orchestra Cloud - Enterprise Edition Application Entry Point
 * 
 * This module provides enterprise features including:
 * - Distributed Redis-based caching and locking
 * - Multi-tenant database support
 * - Production-grade monitoring and metrics
 * - Payment gateway integrations
 */
@SpringBootApplication(scanBasePackages = {"io.orchestra.core", "io.orchestra.cloud"})
public class OrchestraApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrchestraApplication.class, args);
	}

}
