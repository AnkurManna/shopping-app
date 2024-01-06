FROM openjdk:17-jdk-slim

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} productservice.jar

#To execute command while building the image use CMD [ ]
#To execute command after starting the container use ENTRYPOINT

ENTRYPOINT ["java" , "-jar" ,"/productservice.jar" ]

EXPOSE 8080