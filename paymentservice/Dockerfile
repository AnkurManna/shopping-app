FROM openjdk:17-jdk-slim

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} paymentservice.jar

#To execute command while building the image use CMD [ ]
#To execute command after starting the container use ENTRYPOINT

ENTRYPOINT ["java" , "-jar" ,"/paymentservice.jar" ]

EXPOSE 8083