FROM maven:3-jdk-8

USER root


# Add scripts used to configure the image
COPY * /tmp/radtests

# Adding jmx by default

RUN /tmp/radtests/install-env.sh && /tmp/radtests/overwritecfg.sh  

# Switch to the user 185 for OpenShift usage
USER 185

# Make the default PWD somewhere that the user can write. This is
# useful when connecting with 'oc run' and starting a 'spark-shell',
# which will likely try to create files and directories in PWD and
# error out if it cannot.
WORKDIR /tmp/radtests


# Start the main process
CMD ["mvn clean test -Ptest-s3source"]
