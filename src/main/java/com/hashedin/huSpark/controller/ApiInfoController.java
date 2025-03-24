package com.hashedin.huSpark.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller for health check and basic API information.
 * Provides endpoints for checking the API's status and retrieving its metadata.
 */
@RestController
@RequestMapping("/api")
@Api(tags = "API Information")
public class ApiInfoController {

    private final Logger log = LoggerFactory.getLogger(ApiInfoController.class);

    /**
     * Basic health check endpoint.
     * Checks if the API is running and returns a status message.
     * @return ResponseEntity containing the status message.
     */
    @GetMapping("/health")
    @ApiOperation("Check API health")
    public ResponseEntity<String> healthCheck() {
        log.info("ApiInfoController: healthCheck - API Health Checked.");
        return ResponseEntity.ok("SOCIO API is up and running!");
    }

    /**
     * Retrieves basic API information.
     * Returns the API's name, version, and description.
     * @return ResponseEntity containing API information as a JSON object.
     */
    @GetMapping("/info")
    @ApiOperation("Get API information")
    public ResponseEntity<Object> apiInfo() {
        log.info("ApiInfoController: info - API information requested.");
        try {
            Object apiInfo = new Object() {
                public final String name = "SOCIO Social Network API";
                public final String version = "1.0";
                public final String description = "A comprehensive social networking platform API";
            };
            return ResponseEntity.ok(apiInfo);
        } catch (Exception e) {
            log.error("ApiInfoController: info - Error retrieving API Info: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }

    }
}