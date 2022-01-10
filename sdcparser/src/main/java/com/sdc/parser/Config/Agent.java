package com.sdc.parser.Config;

import java.util.HashMap;

public abstract class Agent {
    private String identifier;
    private String firstName;
    private String lastName;
    private HashMap<String,String> system;
    private String address;
    private String telecom;

    public Agent(String identifier, String firstName, String lastName, HashMap<String,String> system, String address, String telecom) {
        this.identifier = identifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.system = system;
        this.address = address;
        this.telecom = telecom;
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
    public HashMap<String,String> getSystem() {
        return system;
    }

    /**
     * @param system the system to set
     */
    public void setSystem(HashMap<String,String> system) {
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
