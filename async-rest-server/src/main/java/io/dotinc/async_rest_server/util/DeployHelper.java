package io.dotinc.async_rest_server.util;


import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * @author vladbulimac on 2019-04-14.
 */

@Slf4j
public class DeployHelper {

    public static Future<Void> deployVerticle(String name, DeploymentContext deploymentContext){
        Future<Void> future = Future.future();
        Vertx vertx = deploymentContext.getVertxContext();
        vertx.deployVerticle(name, deploymentContext.getDeploymentOptions(), res -> {
            if(res.succeeded()){
                log.info("The verticle '{}' deployed successfully", name);
                future.complete();
            } else {
                log.error("The verticle '{}' did not deployed successfully", name);
                future.failed();
            }
        });
        return future;
    }

}
