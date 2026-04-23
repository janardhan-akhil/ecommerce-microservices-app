package com.api_gateway.config;

import lombok.Data;

@Data
public class RouteDefinition {

    /** URL path prefix — e.g. /api/v1/users */
    private String path;

    /** Base URL of the downstream service — e.g. http://localhost:8081 */
    private String url;
}
