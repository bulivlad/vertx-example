package io.dotinc.rest_server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

/**
 * @author vladbulimac on 2019-04-14.
 */

@Slf4j
public class FileReaderVerticle extends AbstractVerticle {

    private static final String RESPONSES_FILE_PATH = "src/main/resources/responses";
    private static final String FILE_READER_EVENT_ADDRESS = "verticle.file-reader";

    @Override
    public void start(Future<Void> startFuture) {
        log.info("Read file event received at '{}'", FILE_READER_EVENT_ADDRESS);
        vertx.eventBus()
            .consumer(FILE_READER_EVENT_ADDRESS, handler -> {
                    String filePath = RESPONSES_FILE_PATH + handler.body().toString().toLowerCase() + ".json";
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
