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
            .requestHandler(req -> {
                Future<Void> readerFuture = Future.future();
                sendEventToFileReader(req, readerFuture);
                sendEventToHttpClient(req, readerFuture);
            })
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

    private void sendEventToFileReader(HttpServerRequest req, Future<Void> readerFuture) {
        vertx.eventBus().send(FILE_READER_EVENT_ADDRESS.getValue(),
                RESPONSES_FILE_PATH.getValue() + req.path().toLowerCase(),
                handler -> handleFileReaderResponse(req, handler, readerFuture));
    }

    private void handleFileReaderResponse(HttpServerRequest req, AsyncResult<Message<Object>> handler, Future<Void> future) {
        if(handler.succeeded()) {
            log.info("File reading ended successfully");
            buildResponse(req.response(), (JsonObject) handler.result().body());
            log.info("Response successfully sent to {}", req.path());
            future.complete();
        } else {
            log.error("File reading did not succeeded");
            req.response()
                    .putHeader("content-type", "text/plain")
                    .setStatusCode(404)
                    .end(handler.cause().getMessage());
            future.fail("File reading did not succeeded");
        }
    }

    private void buildResponse(HttpServerResponse response, JsonObject handler) {
        JsonObject headers = handler.getJsonObject("headers");
        headers.fieldNames().forEach(e -> response.putHeader(e, headers.getString(e)));
        response.setStatusCode(handler.getInteger("status"));
        response.setStatusMessage(handler.getString("statusMessage") != null ? handler.getString("statusMessage") : "" );
        response.end(handler.getJsonObject("body").toString());
    }

    private void sendEventToHttpClient(HttpServerRequest request, Future<Void> readerFuture) {
        readerFuture.setHandler(fileHand -> {
            if(fileHand.succeeded()) {
                vertx.setTimer(config().getInteger(REQUEST_DELAY.getValue()),
                        hand -> vertx.eventBus().send(HTTP_CLIENT_EVENT_ADDRESS.getValue(),
                                REQUESTS_FILE_PATH.getValue() + request.path().toLowerCase(),
                                handler -> handleHttpClientResponse(request, handler)));
            } else {
                log.error("File future failed!", fileHand.cause());
            }
        });
    }

    private void handleHttpClientResponse(HttpServerRequest request, AsyncResult<Message<Object>> handler) {
        if(handler.succeeded()) {
            log.info("Request to {} sent", request.path());
        } else {
            log.error("Failed to send request to {}", request.path());
        }
    }
}
