FROM fabric8/java-alpine-openjdk8-jre

EXPOSE 8080

# Copy dependencies
COPY target/dependency/* /deployment/libs/

# Copy classes
COPY target/classes /deployment/classes
COPY target/test-classes /deployment/test-classes

# Copy Keystore
COPY keystore.jceks /deployment/keystore.jceks

ENV JAVA_APP_DIR=/deployment
ENV JAVA_LIB_DIR=/deployment/libs
ENV JAVA_CLASSPATH=${JAVA_APP_DIR}/classes:${JAVA_LIB_DIR}/*
ENV JAVA_MAIN_CLASS="tcms.Tcms"
