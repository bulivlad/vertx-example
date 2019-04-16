package io.dotinc.async_rest_server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import static io.dotinc.async_rest_server.enums.Constants.*;

/**
 * @author vladbulimac on 2019-04-14.
 */

@Slf4j
public class HttpServerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        Integer httpPort = config().getInteger(HTTP_PORT.getValue());
        vertx.createHttpServer()
            .requestHandler(req -> vertx.eventBus()
                .send(FILE_READER_EVENT_ADDRESS.getValue(),
                        RESPONSES_FILE_PATH.getValue() + req.path().toLowerCase(),
                        handler -> handleFileReaderResponse(req, handler))
                .send(HTTP_CLIENT_EVENT_ADDRESS.getValue(),
                        REQUESTS_FILE_PATH.getValue() + req.path().toLowerCase(),
                        handler -> handleHttpClientResponse(req, handler)))
            .listen(httpPort, http -> {
                if (http.succeeded()) {
                    startFuture.complete();
                    log.info("HTTP server started on port " + httpPort);
                } else {
                    log.error("HTTP server failed to start");
                    startFuture.fail(http.cause());
                }
            });
    }

    private void handleFileReaderResponse(HttpServerRequest req, AsyncResult<Message<Object>> handler) {
        if(handler.succeeded()) {
            log.info("File reading ended successfully");
            buildResponse(req.response(), (JsonObject) handler.result().body());
            log.info("Response successfully sent to {}", req.path());
        } else {
            log.error("File reading did not succeeded");
            req.response()
                    .putHeader("content-type", "text/plain")
                    .setStatusCode(404)
                    .end(handler.cause().getMessage());
        }
    }

    private void buildResponse(HttpServerResponse response, JsonObject handler) {
        JsonObject headers = handler.getJsonObject("headers");
        headers.fieldNames().forEach(e -> response.putHeader(e, headers.getString(e)));
        response.setStatusCode(handler.getInteger("status"));
        response.setStatusMessage(handler.getString("statusMessage") != null ? handler.getString("statusMessage") : "" );
        response.end(handler.getJsonObject("body").toString());
    }

    private void handleHttpClientResponse(HttpServerRequest req, AsyncResult<Message<Object>> handler) {
        if(handler.succeeded()) {
            log.info("Request to {} sent", req.path());
        } else {
            log.error("Failed to send request to {}", req.path());
        }
    }
}
