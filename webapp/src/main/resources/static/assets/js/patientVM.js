function PatientZoekViewModel() {
	var self = this;
	self.zoekveld = ko.observable();
	self.patient = ko.observable();
	self.zoek = function() {
		$.get('/patient/@' + self.zoekveld(),{}, self.patient);
	};
}

ko.applyBindings(new PatientZoekViewModel());