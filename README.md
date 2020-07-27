# IHE-SDC-FHIR-Parser
The IHE SDC Parser takes a static IHE SDC Form and Converts each question and answer pair into FHIR Observations
The FHIR Implementation Guide can be found here: https://simplifier.net/sdconfhir

## Project Type
sdcparser is a maven project (https://maven.apache.org/). The pom.xml contains the project structure and dependencies. Maven will package this project as a WAR file that can be deployed on to any application server such as Apache Tomcat (http://tomcat.apache.org/)
## Generating a WAR file
To generate a WAR file, execute the following on the command line:
```
C:\sdcparser>mvn package
```
The WAR file will be geenerated to the /target folder
## Libraries used
https://hapifhir.io/
