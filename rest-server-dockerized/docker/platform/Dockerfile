FROM centos:7

#Upgrad and install basic packages
RUN yum -y update && \
  yum -y makecache && \
  yum -y --enablerepo=extras install epel-release && \
  yum -y update && \
  yum -y makecache && \
  yum -y --enablerepo=extras install curl python-pip

# Install AWS CLI
RUN pip install awscli --no-cache-dir --ignore-installed six

# Clean up
RUN yum -y remove python-pip && \
	yum clean all && \
	rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

#Embed version label
ARG VERSION
LABEL platform-base.version ${VERSION}
