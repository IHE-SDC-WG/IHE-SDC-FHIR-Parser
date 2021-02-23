package com.sdc.parser.Resource;

import static com.sdc.parser.Constants.Constants.SYSTEM_NAME;
import static com.sdc.parser.ParserHelper.getFormID;

import java.util.Date;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.w3c.dom.Document;

import ca.uhn.fhir.context.FhirContext;

public class DocReferenceHelper {
	public static DocumentReference createDocReference(FhirContext ctx, String sdcForm, Document form,
			String patientUUID) {
		DocumentReference docRef = new DocumentReference();
		Narrative narry = new Narrative();
		//narry.setDivAsString("<div>This DocumentReference was created by the Infoway Parser for form " + getFormID(form) +"</div>");
		docRef.setStatus(DocumentReferenceStatus.CURRENT);
		docRef.setText(narry);
		docRef.getMasterIdentifier().setSystem(SYSTEM_NAME).setValue(getFormID(form));
		docRef.setSubject(new Reference(patientUUID));
		docRef.setDate(new Date());
		DocumentReferenceContentComponent drcc = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		attachment.setContentType("text/plain");
		attachment.setDataElement(new Base64BinaryType(sdcForm.getBytes()));
		drcc.setAttachment(attachment);
		docRef.addContent(drcc);
		return docRef;
	}
}
