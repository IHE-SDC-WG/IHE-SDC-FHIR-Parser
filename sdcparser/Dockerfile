FROM tomcat:jdk15-openjdk-oracle
COPY target/sdcparser.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]