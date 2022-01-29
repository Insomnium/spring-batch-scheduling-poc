FROM openjdk:11-jre-slim
COPY target/batch-distributed-schedulling-poc.jar ./
CMD 'java -jar /batch-distributed-schedulling-poc.jar'
