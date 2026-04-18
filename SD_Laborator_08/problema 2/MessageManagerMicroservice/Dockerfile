FROM amazoncorretto:8-alpine
ADD target/MessageManagerMicroservice-1.0-SNAPSHOT-jar-with-dependencies.jar MessageManagerMicroservice.jar

ENTRYPOINT ["java","-jar", "MessageManagerMicroservice.jar"]