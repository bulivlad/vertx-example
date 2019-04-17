package io.dotinc.async_rest_server;

import io.dotinc.async_rest_server.util.DeployHelper;
import io.dotinc.async_rest_server.util.DeploymentContext;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import static io.dotinc.async_rest_server.enums.Constants.CONFIG_LOCATION;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        String configLocation = getFileConfigLocation();
        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", configLocation));
        ConfigStoreOptions envStore = new ConfigStoreOptions()
                .setType("env");

        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions().addStore(fileStore).addStore(envStore);
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, retrieverOptions);

        configRetriever.getConfig(res -> {
            if(res.succeeded()){
                JsonObject fileConfig = res.result();
                config().mergeIn(fileConfig);

                DeploymentContext deploymentContext = new DeploymentContext.DeploymentContextBuilder()
                        .withDeploymentOptions(new DeploymentOptions().setConfig(fileConfig))
                        .withVertx(vertx)
                        .build();

                CompositeFuture.all(
                        DeployHelper.deployVerticle(HttpServerVerticle.class.getName(), deploymentContext),
                        DeployHelper.deployVerticle(HttpClientVerticle.class.getName(), deploymentContext),
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
                log.error("Unable to retrieve the config from file '{}'", CONFIG_LOCATION.getValue());
                startFuture.failed();
            }
        });
    }

    private String getFileConfigLocation() {
        String configLocation = System.getenv("CONFIG_LOCATION");
        if(configLocation != null) {
            return configLocation;
        }
        return CONFIG_LOCATION.getValue();
    }

}
