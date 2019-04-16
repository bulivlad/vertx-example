package io.dotinc.async_rest_server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import static io.dotinc.async_rest_server.enums.Constants.FILE_READER_EVENT_ADDRESS;

/**
 * @author vladbulimac on 2019-04-14.
 */

@Slf4j
public class FileReaderVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        log.info("Read file event received at '{}'", FILE_READER_EVENT_ADDRESS.getValue());
        vertx.eventBus().consumer(FILE_READER_EVENT_ADDRESS.getValue(), this::handleReaderReply);
    }

    private void handleReaderReply(Message<Object> handler) {
        String filePath = handler.body().toString().toLowerCase() + ".json";
        vertx.fileSystem().readFile(filePath, fileHandler -> handleFileRead(handler, fileHandler));
    }

    private void handleFileRead(Message<Object> handler, AsyncResult<Buffer> fileHandler) {
        String filePath = handler.body().toString().toLowerCase() + ".json";
        if(fileHandler.result() != null) {
            log.info("File '{}' exists and have content", filePath);
            fileHandler.succeeded();
            handler.reply(fileHandler.result().toJsonObject());
        } else {
            log.warn("The file '{}' does not exists or does not have content", filePath);
            fileHandler.failed();
            handler.fail(1, "no file");
        }
    }
}
