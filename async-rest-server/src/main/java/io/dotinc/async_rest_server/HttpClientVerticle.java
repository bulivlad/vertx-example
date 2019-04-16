package io.dotinc.async_rest_server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static io.dotinc.async_rest_server.enums.Constants.FILE_READER_EVENT_ADDRESS;
import static io.dotinc.async_rest_server.enums.Constants.HTTP_CLIENT_EVENT_ADDRESS;

/**
 * @author vladclaudiubulimac on 2019-04-15.
 */

@Slf4j
public class HttpClientVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        vertx.eventBus().consumer(HTTP_CLIENT_EVENT_ADDRESS.getValue(), messagePath -> {
            Future<JsonObject> fileReaderFuture = Future.future();
            fileReaderFuture.setHandler(handler -> {

                JsonObject responseFile = handler.result();
                String endpoint = responseFile.getString("endpoint");
                String host = StringUtils.substringBetween(endpoint, "//", ":");
                int port = Integer.valueOf(endpoint.substring(endpoint.lastIndexOf(":") + 1));
                String method = responseFile.getString("method");
                String uri = responseFile.getString("uri");

                WebClientOptions webClientOptions = new WebClientOptions().setSsl(endpoint.startsWith("https"));
                WebClient webClient = WebClient.create(vertx, webClientOptions);
                webClient.request(HttpMethod.valueOf(method.toUpperCase()), port, host, uri)
                        .sendJson(responseFile.getJsonObject("body"), response -> {
                    if(response.succeeded()) {
                        messagePath.reply(response.result() != null && response.result().body() != null ? response.result().body().toString() : "");
                    } else {
                        messagePath.fail(1, (response.result() != null && response.result().body() != null ? response.result().body().toString() : "Null"));
                    }
                });
            });
            vertx.eventBus().send(FILE_READER_EVENT_ADDRESS.getValue(), messagePath.body(), reply -> {
                if(reply.succeeded()){
                    fileReaderFuture.complete((JsonObject) reply.result().body());
                }
            });
        });
    }
}
