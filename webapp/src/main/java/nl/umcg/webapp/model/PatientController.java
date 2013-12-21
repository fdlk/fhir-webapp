package nl.umcg.webapp.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Address.AddressUse;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier.IdentifierUse;
import org.hl7.fhir.instance.model.Narrative;
import org.hl7.fhir.instance.model.Narrative.NarrativeStatus;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.Base64Variants;

@RestController
public class PatientController {

	private RestTemplate basicAuthTemplate = new RestTemplate();
	private String patientServiceUrl = "http://opoliplus3.umcg.nl:8888/patient/@{id}";

	class HeaderSetter implements ClientHttpRequestInterceptor {
		private final Map<String, String> headerMap;

		public HeaderSetter(Map<String, String> headerMap) {
			this.headerMap = headerMap;
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body,
				ClientHttpRequestExecution execution) throws IOException {
			HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
			for (String name : headerMap.keySet()) {
				String value = headerMap.get(name);
				requestWrapper.getHeaders().set(name, value);
			}
			return execution.execute(requestWrapper, body);
		}
	}

	public PatientController() {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(
				"Authorization",
				"Basic "
						+ Base64Variants.getDefaultVariant().encode(
								"6914:pp".getBytes()));
		headersMap.put("Accept", "*/*");
		ClientHttpRequestInterceptor headerSetter = new HeaderSetter(headersMap);
		basicAuthTemplate.setInterceptors(Collections
				.singletonList(headerSetter));
	}

	@RequestMapping("/patient/@{id}")
	@ResponseBody
	public PatientBean getPatient(@PathVariable String id) throws Exception {
		// String patientString = basicAuthTemplate.getForObject(
		// patientServiceUrl, String.class, id);
		String patientString = "{  \"resourceType\": \"Patient\",  \"text\": {    \"status\": \"generated\",    \"div\": \"<div>Wierenga, Romke 8888880</div>\"  },"
				+ "  \"identifier\": [    {      \"use\": \"official\",      \"label\": \"UMCG nummer\",      \"system\": \"http://umcg.nl/mrn\",      \"value\": \"8888880\","
				+ "      \"assigner\": {        \"reference\": \"UMCG\"      } } ],\"name\": [   {     \"text\": \"Wierenga, Romke\",   \"family\": [      \"Wierenga\"    ],    "
				+ "\"given\": [      \"Romke\",      \".\""
				+ "    ] }],\"telecom\": [  {    \"system\": \"phone\",    \"value\": \"0512-352017\",    \"use\": \"home\"   } ],"
				+ "  \"gender\": {    \"coding\": [      {        \"system\": \"http://hl7.org/fhir/v3/AdministrativeGender\",        \"code\": \"M\",        \"display\": \"Male\" "
				+ "     }    ],    \"text\": \"Mannelijk\"  },  \"birthDate\": \"1943-07-07\",  \"deceasedBoolean\": true,  \"address\": [    {      \"use\": \"home\",      \"line\":"
				+ " [        \"Deutechemstr 10\"      ],      \"city\": \"Surhuizum\","
				+ "      \"zip\": \"9283 VA\"    }  ],  \"multipleBirthBoolean\": false,  \"active\": true}";
		InputStream is = new StringBufferInputStream(patientString);
		Patient patient = (Patient) new JsonParser().parse(is);
		org.hl7.fhir.instance.model.Boolean multipleBirth = (org.hl7.fhir.instance.model.Boolean)patient.getMultipleBirth();
		System.out.println(multipleBirth.getValue());
		return new PatientBean(patient);
	}

	private Patient janFictief(String id) {
		Patient patient = new Patient();
		ResourceReference umcg = new ResourceReference()
				.setReferenceSimple("UMCG");
		patient.addIdentifier().setValueSimple(id)
				.setSystemSimple("http://umcg.nl/mrn")
				.setLabelSimple("UMCG nummer")
				.setUseSimple(IdentifierUse.usual).setAssigner(umcg);
		patient.addIdentifier().setValueSimple("123412343")
				.setSystemSimple("http://staat.nl/bsn").setLabelSimple("BSN")
				.setUseSimple(IdentifierUse.official);
		patient.setActiveSimple(true);
		org.hl7.fhir.instance.model.Boolean deceased = new org.hl7.fhir.instance.model.Boolean();
		deceased.setValue(false);
		patient.setDeceased(deceased);
		CodeableConcept gender = new CodeableConcept();
		gender.setTextSimple("Mannelijk");
		Coding genderCoding = gender.addCoding();
		genderCoding.setCodeSimple("M");
		genderCoding.setDisplaySimple("Male");
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
		Contact telco = patient.addTelecom();
		telco.setValueSimple("050-1223434");
		return patient;
	}

}
