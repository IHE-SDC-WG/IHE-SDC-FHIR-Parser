#
# Build stage
#
FROM maven:3.5.0-jdk-8 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Run Stage
#
FROM jetty:9.4.44-jre8
COPY --from=build /home/app/target/sdcparser.war /var/lib/jetty/webapps/root.war
EXPOSE 8080