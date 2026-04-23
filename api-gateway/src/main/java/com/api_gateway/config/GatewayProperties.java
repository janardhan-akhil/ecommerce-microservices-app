package com.api_gateway.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    /** Ordered list of path-prefix → service URL mappings. */
    private List<RouteDefinition> routes = new ArrayList<>();

    /**
     * Paths that bypass JWT validation entirely.
     * Supports exact matches and Ant-style wildcards via AntPathMatcher.
     */
    private List<String> publicPaths = new ArrayList<>();

    /** Maximum requests allowed per user (or IP) per minute. Default: 60. */
    private int rateLimitPerMinute = 60;
}