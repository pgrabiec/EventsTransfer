FROM openjdk:latest

EXPOSE 8080

COPY build/libs/TAI_Project.jar /usr/src/server.jar

CMD java -jar /usr/src/server.jar