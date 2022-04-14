package com.sdc.parser.Resource;


import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;

abstract class ResourceHelper<T extends Resource> {
    public T create(FhirContext ctx) {
        T resource = initializeResource(ctx);
        return resource;
    }

    public abstract T initializeResource(FhirContext ctx);
}