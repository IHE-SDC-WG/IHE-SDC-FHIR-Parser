package com.sdc.parser.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BaseDateTimeType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.r4.model.Observation;

import ca.uhn.fhir.context.FhirContext;


public class DiagnosticReportHelper {
	public static DiagnosticReport createDiagnosticReport(FhirContext ctx, String sdcForm,
                                                       String patientUUID, ArrayList<Observation> observations) {

        DiagnosticReport diagReport = new DiagnosticReport();
        //Can we generate this identifier? 
        diagReport.addIdentifier().setSystem("https://example.org/").setValue("DiagRepIHESDC0"); 
        //category 
        diagReport.getCategoryFirstRep().addCoding().setCode("LP7839-6").setSystem("http://loinc.org").setDisplay("Pathology"); 
        //code
        diagReport.getCode().getCodingFirstRep().setCode("60568-3").setSystem("http://loinc.org").setDisplay("Pathology Synoptic report"); 
        //subject
        //diagReport.setSubject(new Reference(patientUUID));
        diagReport.getSubject().setDisplay("Meredith Shepard").setReference("Patient/6547");
        //practitioner 
        diagReport.getPerformerFirstRep().setDisplay("Dr. Alex Karev").setReference("Practitioner/pathpract1");
        //status
        diagReport.setStatus(DiagnosticReportStatus.FINAL);
        //effective date time
        //diagReport.setEffective(new DateTimeType()); 
        diagReport.getEffectiveDateTimeType().setValueAsString("2021-01-01T21:39:30.000Z"); 
        
        //result
        //diagReport.getResultFirstRep().setReference("Adrenal.Bx.Res.129_3.002.011.RC1_sdcFDF3d1c4fe4-09c3-4a7e-877f-9ddb160da6db/ver1#2118.100004300") replace / with .;
        List<Reference>  myList = diagReport.getResult();
        for(Observation ob: observations) {
        	Reference obRef = new Reference(ob.getIdentifierFirstRep().getValue().replaceAll("/", "."));
        	myList.add(obRef);
        } 
        diagReport.setResult(myList);
        for (Reference r : diagReport.getResult()) {
        	r.getIdentifier().setSystem("https://example.org/").setValue(r.getReference()); 
        }
        //TODO figure out how to remove references in the DiagnosticReports 
        //presented form in Base64 
        Attachment attachment = new Attachment();
        attachment.setContentType("application/xml");
        attachment.setDataElement(new Base64BinaryType(sdcForm.getBytes()));
        diagReport.addPresentedForm(attachment);

        return diagReport;
    }
}