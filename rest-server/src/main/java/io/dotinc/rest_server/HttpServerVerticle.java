package io.dotinc.rest_server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author vladbulimac on 2019-04-14.
 */

@Slf4j
public class HttpServerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        Integer httpPort = config().getInteger("http.port");
        vertx.createHttpServer().requestHandler(req -> {

            vertx.eventBus().send("verticle.file-reader", req.path().toLowerCase(), handler -> {
                if(handler.succeeded()) {
                    log.info("File reading ended successfully");
                    buildResponse(req.response(), (JsonObject) handler.result().body());
                } else {
                    log.error("File reading did not succeeded");
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .setStatusCode(404)
                            .end(handler.cause().getMessage());
                }
            });
        }).listen(httpPort, http -> {
            if (http.succeeded()) {
                startFuture.complete();
                log.info("HTTP server started on port " + httpPort);
            } else {
                log.error("HTTP server failed to start");
                startFuture.fail(http.cause());
            }
        });
    }

    private HttpServerResponse buildResponse(HttpServerResponse response, JsonObject handler) {
        JsonObject headers = handler.getJsonObject("headers");
        headers.fieldNames().forEach(e -> response.putHeader(e, headers.getString(e)));
        response.setStatusCode(handler.getInteger("status"));
        response.setStatusMessage(handler.getString("statusMessage") != null ? handler.getString("statusMessage") : "" );
        response.end(handler.getJsonObject("body").toString());

        return response;
    }
}
