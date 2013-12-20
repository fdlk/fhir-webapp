package nl.umcg.webapp;

import java.text.ParseException;

import nl.umcg.webapp.model.PatientBean;

import org.hl7.fhir.instance.model.Address.AddressUse;
import org.hl7.fhir.instance.model.Boolean;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier.IdentifierUse;
import org.hl7.fhir.instance.model.Narrative;
import org.hl7.fhir.instance.model.Narrative.NarrativeStatus;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PatientController {

	@RequestMapping("/patient/@{id}")
	public PatientBean getPatient(@PathVariable String id) {
		return new PatientBean(janFictief(id));
	}

	private Patient janFictief(String id) {
		Patient patient = new Patient();
		ResourceReference umcg = new ResourceReference()
				.setReferenceSimple("UMCG");
		patient.addIdentifier().setValueSimple(id)
				.setSystemSimple("http://umcg.nl/mrn")
				.setLabelSimple("UMCG nummer")
				.setUseSimple(IdentifierUse.official).setAssigner(umcg);
		patient.setActiveSimple(true);
		Boolean deceased = new Boolean();
		deceased.setValue(false);
		patient.setDeceased(deceased);
		CodeableConcept gender = new CodeableConcept();
		gender.setTextSimple("Male");
		Coding genderCoding = gender.addCoding();
		genderCoding.setCodeSimple("M");
		genderCoding.setDisplaySimple("Mannelijk");
		genderCoding
				.setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender");
		patient.setGender(gender);
		DateAndTime birthDate;
		try {
			birthDate = new DateAndTime("2013-08");
			patient.setBirthDateSimple(birthDate);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		patient.addAddress().setUseSimple(AddressUse.home)
				.setZipSimple("9700 AB").setCitySimple("Groningen")
				.addLineSimple("Hanzeplein 1");
		HumanName name = patient.addName();
		name.addGivenSimple("Jan");
		name.addGivenSimple("J.");
		name.addGivenSimple("F.");
		name.addFamilySimple("Fictief");
		name.setTextSimple("Jan J.F. Fictief");
		XhtmlNode narrativeNode;
		try {
			narrativeNode = new XhtmlParser().parseFragment("<div>"
					+ name.getTextSimple() + " " + id + "</div>");
			patient.setText(new Narrative().setStatusSimple(
					NarrativeStatus.generated).setDiv(narrativeNode));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return patient;
	}
}
