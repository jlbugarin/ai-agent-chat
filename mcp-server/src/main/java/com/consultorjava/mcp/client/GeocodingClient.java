package com.consultorjava.mcp.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "geocoding-api")
@Path("/v1/search")
public interface GeocodingClient {

    @GET
    Response search(@QueryParam("name") String name,
                    @QueryParam("count") int count,
                    @QueryParam("language") String language,
                    @QueryParam("format") String format);
}
