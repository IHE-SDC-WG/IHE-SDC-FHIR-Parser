package com.sdc.parser.Resource;

import java.util.List;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;

import com.sdc.parser.ParserHelper;
import com.sdc.parser.Config.ConfigValues;

import ca.uhn.fhir.context.FhirContext;

public class DiagnosticReportHelper {
    private static final String PROFILE_URL = "http://hl7.org/fhir/us/cancer-reporting/StructureDefinition/us-pathology-diagnostic-report";

    public static DiagnosticReport createDiagnosticReport(FhirContext ctx, String sdcForm,
            Reference patientReference, List<Observation> observations, ConfigValues configValues) {

        DiagnosticReport diagReport = new DiagnosticReport();
      
        // meta 
        diagReport.getMeta().addProfile(PROFILE_URL);
        // narrative
        diagReport.getText().setStatus(org.hl7.fhir.r4.model.Narrative.NarrativeStatus.GENERATED);
        diagReport.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">fake Observation</div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>6547</td></tr></tbody></table></div>");
        //category 
        diagReport.getCategoryFirstRep().addCoding().setCode("LP7839-6").setSystem("http://loinc.org").setDisplay("Pathology"); 
        //code
        diagReport.getCode().getCodingFirstRep().setCode("60568-3").setSystem("http://loinc.org").setDisplay("Pathology Synoptic report"); 
        //subject
        diagReport.getSubject().setReference(patientReference.getReference()).setDisplay(patientReference.getDisplay());
        
        //status

        diagReport.setStatus(DiagnosticReportStatus.FINAL);
        // effective date time
        diagReport.getEffectiveDateTimeType().setValueAsString("2021-01-01T21:39:30.000Z");

        // result
        List<Reference> myList = diagReport.getResult();
        for (Observation ob : observations) {
            String obsIdenValue = ob.getIdentifierFirstRep().getValue();
            Reference obRef = new Reference(
                    ParserHelper.createReferenceString(ob.getResourceType(), obsIdenValue));
            obRef.getIdentifier().setSystem(configValues.getSystemName()).setValue(obsIdenValue);
            myList.add(obRef);
        }
        diagReport.setResult(myList);

        // presented form in Base64
        Attachment attachment = new Attachment();
        attachment.setContentType("application/xml");
        attachment.setDataElement(new Base64BinaryType(sdcForm.getBytes()));
        diagReport.addPresentedForm(attachment);

        return diagReport;
    }
}
