package io.dotinc.async_rest_server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import static io.dotinc.async_rest_server.enums.Constants.FILE_READER_EVENT_ADDRESS;
import static io.dotinc.async_rest_server.enums.Constants.RESPONSES_FILE_PATH;

/**
 * @author vladbulimac on 2019-04-14.
 */

@Slf4j
public class FileReaderVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        log.info("Read file event received at '{}'", FILE_READER_EVENT_ADDRESS.getValue());
        vertx.eventBus()
            .consumer(FILE_READER_EVENT_ADDRESS.getValue(), handler -> {
                    String filePath = handler.body().toString().toLowerCase() + ".json";
                    vertx.fileSystem().readFile(filePath,
                    fileHandler -> {
                        if(fileHandler.result() != null) {
                            log.info("File '{}' exists and have content", filePath);
                            fileHandler.succeeded();
                            handler.reply(fileHandler.result().toJsonObject());
                        } else {
                            log.warn("The file '{}' does not exists or does not have content", filePath);
                            fileHandler.failed();
                            handler.fail(1, "no file");
                        }
                    });
                }
            );
    }
}
