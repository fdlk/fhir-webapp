package nl.umcg.webapp.model;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Boolean;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.String_;

/**
 * Simpele bean om tegenaan te binden.
 */
public class PatientBean {
	private String umcgNummer;
	private final String naam;
	private final boolean meerling;
	private String bsn;
	private final String geslacht;
	private final boolean overleden;
	private final List<String> adres;
	private String telefoon;
	private final String geboortedatum;

	public PatientBean(Patient patient) {
		for (Identifier identifier : patient.getIdentifier()) {
			// TODO: dit deugt aan beide kanten voor geen meter!
			if ("http://umcg.nl/mrn".equals(identifier.getSystemSimple())) {
				umcgNummer = identifier.getValueSimple();
			} else {
				// TODO: BSN system checken
				bsn = identifier.getValueSimple();
			}
		}
		naam = patient.getName().get(0).getTextSimple();
		meerling = patient.getMultipleBirth() != null
				&& ((org.hl7.fhir.instance.model.Boolean) patient
						.getMultipleBirth()).getValue();
		geslacht = patient.getGender().getTextSimple();
		if (patient.getDeceased() == null) {
			overleden = false;
		} else {
			// TODO: overlijdensdatum!?!
			Boolean deceasedBoolean = (Boolean) patient.getDeceased();
			overleden = deceasedBoolean.getValue().booleanValue();
		}
		adres = new ArrayList<String>();
		Address address = patient.getAddress().get(0);
		for (String_ line : address.getLine()) {
			adres.add(line.getValue());
		}
		StringBuffer lastLine = new StringBuffer();
		if (address.getZipSimple() != null) {
			lastLine.append(address.getZipSimple());
			lastLine.append(' ');
		}
		if (address.getCitySimple() != null) {
			lastLine.append(address.getCitySimple());
			lastLine.append(' ');
		}
		adres.add(lastLine.toString());
		if (patient.getTelecom() != null && patient.getTelecom().size() > 0) {
			telefoon = patient.getTelecom().get(0).getValueSimple();
		}
		geboortedatum = patient.getBirthDateSimple().toString();
	}

	public String getUmcgNummer() {
		return umcgNummer;
	}

	public String getNaam() {
		return naam;
	}

	public boolean getMeerling() {
		return meerling;
	}

	public String getBsn() {
		return bsn;
	}

	public String getGeslacht() {
		return geslacht;
	}

	public boolean isOverleden() {
		return overleden;
	}

	public List<String> getAdres() {
		return adres;
	}

	public String getTelefoon() {
		return telefoon;
	}

	public String getGeboortedatum() {
		return geboortedatum;
	}

}
