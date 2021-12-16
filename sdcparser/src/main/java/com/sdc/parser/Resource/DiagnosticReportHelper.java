package com.sdc.parser.Resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.BaseDateTimeType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.utilities.ucum.Canonical;
import org.hl7.fhir.r4.model.Observation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Validate.Profile;

public class DiagnosticReportHelper {
    public static DiagnosticReport createDiagnosticReport(FhirContext ctx, String sdcForm,
            String patientUUID, ArrayList<Observation> observations) {

        DiagnosticReport diagReport = new DiagnosticReport();

        // category
        diagReport.getCategoryFirstRep().addCoding().setCode("LP7839-6").setSystem("http://loinc.org")
                .setDisplay("Pathology");
        // code
        diagReport.getCode().getCodingFirstRep().setCode("60568-3").setSystem("http://loinc.org")
                .setDisplay("Pathology Synoptic report");
        // subject
        // diagReport.setSubject(new Reference(patientUUID));
        diagReport.getSubject().setDisplay("Jose Rodriguez").setReference("Patient/JoelAlexPatient");

        // status
        diagReport.setStatus(DiagnosticReportStatus.FINAL);
        // effective date time
        // diagReport.setEffective(new DateTimeType());
        diagReport.getEffectiveDateTimeType().setValueAsString("2021-01-01T21:39:30.000Z");



        //Profile
        String profile = (String) Arrays.asList(diagReport.getClass().getAnnotations()).stream().map(a -> {
            Class<? extends Annotation> type = a.annotationType();
            Map<String, Method> methods = Arrays.asList(type.getDeclaredMethods()).stream().map(m -> {
                return new ImmutablePair<String, Method>(m.getName(), m);
            }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            return new ImmutablePair<Annotation, Map<String, Method>>(a, methods);
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight)).entrySet().stream().map(
                e -> {
                    Set<String> methodNames = e.getValue().keySet();
                    try {
                        return methodNames.contains("profile")
                                ? e.getValue().get("profile").invoke(e.getKey(), (Object[]) null)
                                : "";
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                        e1.printStackTrace();
                    }
                    return "";
                }).findFirst().orElse("");

        // Meta
        Meta diagMeta = new Meta();
        CanonicalType diagProfile = new CanonicalType(profile);
        List<CanonicalType> of = new ArrayList<>();
        of.add(diagProfile);
        diagMeta.setProfile(of);
        diagReport.setMeta(diagMeta);

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
        attachment.setContentType("text/plain");
        attachment.setDataElement(new Base64BinaryType(sdcForm.getBytes()));
        diagReport.addPresentedForm(attachment);

        return diagReport;
    }
}