package nl.umcg.webapp.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Patient;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.Base64Variants;

@RestController
public class PatientController {

	private RestTemplate basicAuthTemplate = new RestTemplate();
	private String patientServiceUrl = "http://opoliplus3.umcg.nl:8888/Patient/@{id}";

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
	public PatientBean getPatient(@PathVariable String id) throws Exception {
		String patientString = basicAuthTemplate.getForObject(
				patientServiceUrl, String.class, id);
		InputStream is = new StringBufferInputStream(patientString);
		Patient patient = (Patient) new JsonParser().parse(is);
		return new PatientBean(patient);
	}

}
