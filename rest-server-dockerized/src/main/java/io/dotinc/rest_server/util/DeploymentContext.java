package io.dotinc.rest_server.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

/**
 * @author vladbulimac on 2019-04-14.
 */

public class DeploymentContext {

    private DeploymentOptions deploymentOptions;
    private Vertx vertxContext;

    private DeploymentContext(DeploymentContextBuilder deploymentContextBuilder) {
        this.deploymentOptions = deploymentContextBuilder.deploymentOptions;
        this.vertxContext = deploymentContextBuilder.vertxContext;
    }

    public DeploymentOptions getDeploymentOptions() {
        return deploymentOptions;
    }

    public Vertx getVertxContext() {
        return vertxContext;
    }

    public static class DeploymentContextBuilder {

        private DeploymentOptions deploymentOptions;
        private Vertx vertxContext;

        public DeploymentContextBuilder withDeploymentOptions(DeploymentOptions deploymentOptions){
            this.deploymentOptions = deploymentOptions;
            return this;
        }

        public DeploymentContextBuilder withVertx(Vertx vertx) {
            this.vertxContext = vertx;
            return this;
        }

        public DeploymentContext build() {
            return new DeploymentContext(this);
        }
    }

}
