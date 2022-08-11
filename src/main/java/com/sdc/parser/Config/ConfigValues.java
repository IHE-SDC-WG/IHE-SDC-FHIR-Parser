package com.sdc.parser.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class ConfigValues {
    String result = "";
    InputStream inputStream;
    private PatientConfig patientConfig;
    private PractitionerConfig practitionerConfig;
    private String systemName;

    public ConfigValues() throws IOException {

        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            HashMap<String, String> patientSystem = new HashMap<String, String>();
            patientSystem.put(prop.getProperty("patient.urn.key"), prop.getProperty("patient.urn.value"));

            patientConfig = new PatientConfig(prop.getProperty("patient.identifier"),
                    prop.getProperty("patient.first.name"), prop.getProperty("patient.last.name"),
                    patientSystem, prop.getProperty("patient.address"), prop.getProperty("patient.telecom"));

            HashMap<String, String> practitionerSystem = new HashMap<String, String>();
            practitionerSystem.put(prop.getProperty("practitioner.someIdentifier.key"),
                    prop.getProperty("practitioner.someIdentifier.value"));
            practitionerSystem.put(prop.getProperty("practitioner.hl7.key"),
                    prop.getProperty("practitioner.hl7.value"));

            practitionerConfig = new PractitionerConfig(prop.getProperty("practitioner.identifier"),
                    prop.getProperty("practitioner.first.name"),
                    prop.getProperty("practitioner.last.name"), practitionerSystem,
                    prop.getProperty("practitioner.address"),
                    prop.getProperty("practitioner.telecom"),
                    prop.getProperty("practitioner.hl7.key"),
                    prop.getProperty("practitioner.hl7.value")
                    );

            systemName = prop.getProperty("system.name");

        } catch (Exception e) {

        } finally {
            inputStream.close();
        }
    }

    /**
     * @return PatientConfig return the patientConfig
     */
    public PatientConfig getPatientConfig() {
        return patientConfig;
    }

    /**
     * @param patientConfig the patientConfig to set
     */
    public void setPatientConfig(PatientConfig patientConfig) {
        this.patientConfig = patientConfig;
    }

    /**
     * @return PractitionerConfig return the practitionerConfig
     */
    public PractitionerConfig getPractitionerConfig() {
        return practitionerConfig;
    }

    /**
     * @param practitionerConfig the practitionerConfig to set
     */
    public void setPractitionerConfig(PractitionerConfig practitionerConfig) {
        this.practitionerConfig = practitionerConfig;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
}
