package io.dotinc.rest_client;

import io.dotinc.rest_client.util.DeployHelper;
import io.dotinc.rest_client.util.DeploymentContext;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    private static final String CONFIG_LOCATION = "src/main/resources/config.json";

    @Override
    public void start(Future<Void> startFuture) {

        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", CONFIG_LOCATION));

        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions().addStore(fileStore);
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, retrieverOptions);

        configRetriever.getConfig(res -> {
            if(res.succeeded()){
                JsonObject fileConfig = res.result();
                config().mergeIn(fileConfig);

                DeploymentContext deploymentContext = new DeploymentContext.DeploymentContextBuilder()
                        .withDeploymentOptions(new DeploymentOptions().setConfig(fileConfig))
                        .withVertx(vertx)
                        .build();

                CompositeFuture.all(DeployHelper.deployVerticle(HttpServerVerticle.class.getName(), deploymentContext),
                        DeployHelper.deployVerticle(FileReaderVerticle.class.getName(), deploymentContext))
                        .setHandler(handler -> {
                    if(handler.succeeded()){
                        log.info("All verticles successfully deployed");
                        startFuture.complete();
                    } else {
                        log.error("At least one verticle did not successfully deployed", handler.cause());
                        startFuture.fail(handler.cause());
                    }
                });
            } else {
                log.error("Unable to retrieve the config from file '{}'", CONFIG_LOCATION);
                startFuture.failed();
            }
        });
    }

}
