ARG BASE_TAG=latest
FROM bootcamp/platform-image:${BASE_TAG}

ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

#Install java
ARG JAVA_VERSION=1.8.0
RUN yum -y install java-${JAVA_VERSION}-openjdk

# Clean up
RUN yum -y remove wget unzip && \
  yum clean all && \
  rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

#Embed version label
ARG VERSION
LABEL java-base.version=${VERSION}