package com.consultorjava.mcp.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "weather-api")
@Path("/v1/forecast")
public interface WeatherClient {

    @GET
    Response forecast(@QueryParam("latitude") double latitude,
                      @QueryParam("longitude") double longitude,
                      @QueryParam("current_weather") boolean currentWeather,
                      @QueryParam("timezone") String timezone);
}
