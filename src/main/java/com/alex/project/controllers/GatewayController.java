package com.alex.project.controllers;


import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;


@Path("/api/{service}")
@ApplicationScoped
public class GatewayController {

    @Inject
    WebClient webClient;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("{path: .*}")
    @Authenticated
    public Uni<Response> get(@Context RoutingContext context, @PathParam("service") String service) {
        return proxy(context, service, null);
    }

    @POST
    @Path("{path: .*}")
    @Authenticated
    public Uni<Response> post(@Context RoutingContext context, @PathParam("service") String service, byte[] body,
                              @CookieParam("JwtToken") String token) {
        System.out.println("POSTTTTTTTTT " + token);
        return proxy(context, service, body);
    }

    @PUT
    @Path("{path: .*}")
    @Authenticated
    public Uni<Response> put(@Context RoutingContext context, @PathParam("service") String service, byte[] body) {
        return proxy(context, service, body);
    }

    @DELETE
    @Path("{path: .*}")
    @Authenticated
    public Uni<Response> delete(@Context RoutingContext context,
    @PathParam("service") String service) {
        return proxy(context, service, null);
    }

    private Uni<Response> proxy(RoutingContext context, String service, byte[] body) {
        String path = context.request().path().replaceFirst("/api/" + service, "");

        String baseUrl = switch (service) {
            case "users" -> "http://localhost:8082";
            default -> throw new RuntimeException("Unknown service");
        };

        HttpMethod method = context.request().method();
        HttpRequest<Buffer> req = webClient.requestAbs(method, baseUrl + path);

        req.putHeader("X-USER-EMAIL", jwt.getSubject());
        req.putHeader("Content-Type", MediaType.APPLICATION_JSON);



        Buffer bodyBuffer = body != null ? Buffer.buffer(body) : Buffer.buffer();

        return req.sendBuffer(bodyBuffer)
                .onItem().transform(resp -> {
                    context.response().setStatusCode(resp.statusCode());
                    resp.headers().forEach(h -> context.response().putHeader(h.getKey(), h.getValue()));

                    String responseBody = resp.bodyAsString();

                    if (responseBody != null) {
                        context.response().end(responseBody);
                    } else {
                        context.response().end();
                    }
                    return Response.ok().build();
                });
    }
}
