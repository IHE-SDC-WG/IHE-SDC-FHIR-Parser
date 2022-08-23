package com.sdc.parser.Config;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.AbstractMap.SimpleEntry;

public abstract class Agent {
    private String identifier;
    private String firstName;
    private String lastName;
    private HashMap<String, String> system;
    private String address;
    private String telecom;
    private AbstractMap.SimpleEntry<String, String> hl7;

    public AbstractMap.SimpleEntry<String, String> getHl7() {
        return hl7;
    }

    public void setHl7(AbstractMap.SimpleEntry<String, String> hl7) {
        this.hl7 = hl7;
    }

    protected Agent(String identifier, String firstName, String lastName, HashMap<String, String> system, String address,
            String telecom, String hl7Key, String hl7Value) {
        this.identifier = identifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.system = system;
        this.address = address;
        this.telecom = telecom;
        this.hl7 = new SimpleEntry<String,String>(hl7Key, hl7Value);
    }

    /**
     * @return String return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return String return the telecom
     */
    public String getTelecom() {
        return telecom;
    }

    /**
     * @param telecom the telecom to set
     */
    public void setTelecom(String telecom) {
        this.telecom = telecom;
    }

    /**
     * @return String return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return String return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return HashMap<String,String> return the system
     */
    public HashMap<String, String> getSystem() {
        return system;
    }

    /**
     * @param system the system to set
     */
    public void setSystem(HashMap<String, String> system) {
        this.system = system;
    }

    /**
     * @return String return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

}
