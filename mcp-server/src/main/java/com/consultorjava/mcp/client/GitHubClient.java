package com.consultorjava.mcp.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "github-api")
@Path("/repos")
public interface GitHubClient {

    @GET
    @Path("/{owner}/{repo}")
    Response getRepository(@PathParam("owner") String owner,
                           @PathParam("repo") String repo);
}
