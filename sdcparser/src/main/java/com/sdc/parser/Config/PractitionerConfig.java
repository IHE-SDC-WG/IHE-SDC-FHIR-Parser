package com.sdc.parser.Config;

import java.util.HashMap;

public class PractitionerConfig extends Agent {

    public PractitionerConfig(String identifier, String firstName, String lastName, HashMap<String, String> system,
            String address, String telecom) {
        super(identifier, firstName, lastName, system, address, telecom);
    }
}
