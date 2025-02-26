package com.hashedin.huSpark.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for health check and basic API info
 */
@RestController
@RequestMapping("/api")
@Api(tags = "API Information")
public class ApiInfoController {

    Logger log = LoggerFactory.getLogger(ApiInfoController.class);

    /**
     * Basic health check endpoint
     * @return Status message
     */
    @GetMapping("/health")
    @ApiOperation("Check API health")
    public ResponseEntity<String> healthCheck() {
        
        log.info("ApiInfoController: healthCheck");

        return ResponseEntity.ok("SOCIO API is up and running!");
    }

    /**
     * Get basic API information
     * @return API information
     */
    @GetMapping("/info")
    @ApiOperation("Get API information")
    public ResponseEntity<Object> apiInfo() {
        log.info("ApiInfoController: info");

        return ResponseEntity.ok(new Object() {
            public final String name = "SOCIO Social Network API";
            public final String version = "1.0";
            public final String description = "A comprehensive social networking platform API";
        });
    }
}