FROM jetty:9.4.44-jre8
COPY ./target/sdcparser.war /var/lib/jetty/webapps/root.war
EXPOSE 8080