package io.dotinc.async_rest_server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.Optional;

import static io.dotinc.async_rest_server.enums.Constants.*;

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
                HttpRequest<Buffer> request = webClient.request(HttpMethod.valueOf(method.toUpperCase()), port, host, uri);
                handleBasicAuth(config(), (String) messagePath.body(), request)
                        .sendJson(responseFile.getJsonObject("body"), response -> handleHttpResponse(messagePath, response));
            });
            vertx.eventBus().send(FILE_READER_EVENT_ADDRESS.getValue(), messagePath.body(), reply -> {
                if(reply.succeeded()){
                    fileReaderFuture.complete((JsonObject) reply.result().body());
                } else {
                    log.error("Failed to read from file {}", messagePath.body(), reply.cause());
                    fileReaderFuture.fail(reply.cause());
                }
            });
        });
    }

    private HttpRequest<Buffer> handleBasicAuth(JsonObject config, String filePath, HttpRequest<Buffer> request) {
        String messagePath = filePath.replace(config().getString(REQUESTS_FILE_PATH.getValue()), "");
        Optional<JsonObject> maybeAuth = Optional.ofNullable(config.getJsonObject("auth"));
        String authUsername = maybeAuth.map(e -> e.getJsonObject(messagePath)).map(e -> e.getString("username")).orElse(null);
        String authPassword = maybeAuth.map(e -> e.getJsonObject(messagePath)).map(e -> e.getString("password")).orElse(null);

        if(authUsername != null && authPassword != null) {
            String credentials = new String(Base64.getEncoder().encode((authUsername + ":" + authPassword).getBytes()));
            request.putHeader(HttpHeaders.AUTHORIZATION.toString(), "Basic " + credentials);
            return request;
        }
        return request;
    }

    private void handleHttpResponse(Message<Object> messagePath, AsyncResult<HttpResponse<Buffer>> response) {
        if(response.succeeded()) {
            messagePath.reply(response.result() != null && response.result().body() != null ? response.result().body().toString() : "");
        } else {
            log.error("Failed to send HTTP Request to {}", messagePath.body(), response.cause());
            messagePath.fail(1, (response.result() != null && response.result().body() != null ? response.result().body().toString() : "Null"));
        }
    }
}
