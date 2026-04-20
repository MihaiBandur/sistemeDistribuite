FROM amazoncorretto:8-alpine

RUN mkdir /db

ADD target/DatabaseMicroservice-1.0-SNAPSHOT-jar-with-dependencies.jar DatabaseMicroservice.jar

ENTRYPOINT ["java","-jar", "DatabaseMicroservice.jar"]