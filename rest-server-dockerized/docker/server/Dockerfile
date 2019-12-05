ARG BASE_TAG=lastest
FROM bootcamp/java-image:${BASE_TAG}

ARG DIGILEAP_DIR="/opt/myserver"

# Version Label
ARG VERSION
LABEL server.version=${VERSION}

#Make dirs
RUN mkdir -p ${DIGILEAP_DIR} ${DIGILEAP_DIR}/bin

# Entrypoint
COPY docker/server/files/docker-entrypoint.sh ${DIGILEAP_DIR}/bin/docker-entrypoint.sh

# Users and Groups
ARG SERVICE_NAME="rest-server-dockerized"
RUN groupadd -g 12000 ${SERVICE_NAME} && \
            useradd -m -d /home/${SERVICE_NAME} ${SERVICE_NAME} -u 12000 -g 12000

#App arguments
ARG SERVICE_VERSION="1.0.0-SNAPSHOT"
ARG BUILD_DIR
ARG SERVICE_PORT

# Application binary
COPY ${BUILD_DIR}/${SERVICE_NAME}-${SERVICE_VERSION}-fat.jar ${DIGILEAP_DIR}/lib/${SERVICE_NAME}.jar

# Permissions
ENV SERVICE_NAME="${SERVICE_NAME}"
ENV DIGILEAP_DIR="${DIGILEAP_DIR}"
ENV HOME="/home/${SERVICE_NAME}"
RUN chown -R ${SERVICE_NAME}:${SERVICE_NAME} /mnt /opt /var/log && \
            chmod +x ${DIGILEAP_DIR}/bin/*.sh

EXPOSE ${SERVICE_PORT}
USER ${SERVICE_NAME}

WORKDIR ${DIGILEAP_DIR}

ENTRYPOINT [ "bin/docker-entrypoint.sh" ]