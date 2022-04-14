package com.sdc.parser.Resource;

import java.util.ArrayList;

import com.sdc.parser.Config.ConfigValues;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.context.FhirContext;

public class DiagnosticReportHelper {
    private static final String CODING_DISPLAY = "Pathology Synoptic report";
    private static final String CODING_CODE = "60568-3";
    private static final String CATEGORY_DISPLAY = "Pathology";
    private static final String SYSTEM_URL = "http://loinc.org";
    private static final String CATEGORY_CODE = "LP7839-6";
    private static final String PROFILE_URL = "http://hl7.org/fhir/us/cancer-reporting/StructureDefinition/us-pathology-diagnostic-report";

    public static DiagnosticReport createDiagnosticReport(FhirContext ctx, String sdcForm,
            String patientUUID, ArrayList<Observation> observations, ConfigValues configValues) {

        DiagnosticReport diagReport = new DiagnosticReport();

        diagReport.getMeta().addProfile(PROFILE_URL);
        diagReport.getCategoryFirstRep().addCoding().setCode(CATEGORY_CODE).setSystem(SYSTEM_URL)
                .setDisplay(CATEGORY_DISPLAY);
        diagReport.getCode().getCodingFirstRep().setCode(CODING_CODE).setSystem(SYSTEM_URL).setDisplay(CODING_DISPLAY);
        diagReport.getSubject().setDisplay(
                configValues.getPatientConfig().getFullName()).setReference(
                        "Patient/"
                                + configValues.getPatientConfig().getSystem().values().toArray()[0]);
        diagReport.setStatus(DiagnosticReportStatus.FINAL);
        diagReport.getEffectiveDateTimeType().setValueAsString("2021-01-01T21:39:30.000Z");
        diagReport.getResult().addAll(
                observations.stream().map(ob -> {
                    Reference reference = new Reference(ob.getIdentifierFirstRep().getValue().replaceAll("/", "."));
                    reference.getIdentifier().setSystem("https://example.org/").setValue(reference.getReference());
                    return reference;
                }).toList());

        Attachment attachment = new Attachment();
        attachment.setContentType("application/xml");
        attachment.setDataElement(new Base64BinaryType(sdcForm.getBytes()));
        diagReport.addPresentedForm(attachment);

        return diagReport;
    }
}
