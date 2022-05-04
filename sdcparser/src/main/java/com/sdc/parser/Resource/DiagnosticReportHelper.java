package com.sdc.parser.Resource;

import java.util.ArrayList;
import java.util.List;

import com.sdc.parser.Config.ConfigValues;
import com.sdc.parser.Config.PatientConfig;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.context.FhirContext;

public class DiagnosticReportHelper {
    private static final String PROFILE_URL = "http://hl7.org/fhir/us/cancer-reporting/StructureDefinition/us-pathology-diagnostic-report";

    public static DiagnosticReport createDiagnosticReport(FhirContext ctx, String sdcForm,
            String patientUUID, ArrayList<Observation> observations, ConfigValues configValues) {

        DiagnosticReport diagReport = new DiagnosticReport();
        
        //id 
        
        // meta 
        diagReport.getMeta().addProfile(PROFILE_URL);
        //category 
        diagReport.getCategoryFirstRep().addCoding().setCode("LP7839-6").setSystem("http://loinc.org").setDisplay("Pathology"); 
        //code
        diagReport.getCode().getCodingFirstRep().setCode("60568-3").setSystem("http://loinc.org").setDisplay("Pathology Synoptic report"); 
        //subject
        //diagReport.setSubject(new Reference(patientUUID));
        PatientConfig patientConfig = configValues.getPatientConfig();
        diagReport.getSubject().setDisplay(patientConfig.getFullName()).setReference("Patient/"
            + patientConfig.getSystem().values().toArray()[0]); 
        
        //status

        diagReport.setStatus(DiagnosticReportStatus.FINAL);
        // effective date time
        // diagReport.setEffective(new DateTimeType());
        diagReport.getEffectiveDateTimeType().setValueAsString("2021-01-01T21:39:30.000Z");

        // result
        // diagReport.getResultFirstRep().setReference("Adrenal.Bx.Res.129_3.002.011.RC1_sdcFDF3d1c4fe4-09c3-4a7e-877f-9ddb160da6db/ver1#2118.100004300");
        List<Reference> myList = diagReport.getResult();
        for (Observation ob : observations) {
            Reference obRef = new Reference(ob.getIdentifierFirstRep().getValue().replaceAll("/", "."));
            myList.add(obRef);
        }
        diagReport.setResult(myList);
        for (Reference r : diagReport.getResult()) {
            r.getIdentifier().setSystem("https://example.org/").setValue(r.getReference());
        }

        // presented form in Base64
        Attachment attachment = new Attachment();
        attachment.setContentType("application/xml");
        attachment.setDataElement(new Base64BinaryType(sdcForm.getBytes()));
        diagReport.addPresentedForm(attachment);

        return diagReport;
    }
}
