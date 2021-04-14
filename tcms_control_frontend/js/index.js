new Vue({
  el: '#app',
  data: {
    tcms_url: "http://192.168.99.101:30742/",
    tcms_url_input: '',
    invalidTcmsUrl: false,
    vehicle_url: "http://192.168.99.101:30938/",
    vehicle_url_input: '',
    invalidVehicleUrl: false,
    light_request_name: '',
    light: new Light("fake", "unknown"),
    lights: [],
    showSpinner: true
  },

  methods: {
    itemClicked: function(light) {
      this.light = light;
    },
    showTcmsUrl: function() {
      this.invalidTcmsUrl = false;
      $("#tcms-url-input").removeClass("is-invalid");
      $("#tcms-url-modal").modal('show');
    },
    setTcmsUrl: function() {
      if(this.validURL(this.tcms_url_input)) {
        const url = this.tcms_url_input;
        this.tcms_url_input = '';
        this.tcms_url = url;
        this.fetchLights();
        $("#tcms-url-modal").modal('hide');
      } else {
        this.invalidTcmsUrl = true;
        $("#tcms-url-input").addClass("is-invalid");
      }
    },
    showVehicleUrl: function() {
      this.invalidVehicleUrl = false;
      $("#vehicle-url-input").removeClass("is-invalid");
      $("#vehicle-url-modal").modal('show');
    },
    setVehicleUrl: function() {
      if(this.validURL(this.vehicle_url_input)) {
        const url = this.vehicle_url_input;
        this.vehicle_url_input = '';
        this.vehicle_url = url;
        $("#tcms-url-modal").modal('hide');
      } else {
        this.invalidVehicleUrl = true;
        $("#vehicle-url-input").addClass("is-invalid");
      }
    },
    showLightRequestModal: function() {
      $("#light-request-modal").modal('show');
    },
    fetchLights: function() {
      console.log("fetching lights")
      $.ajaxSetup({ cache: false });
      $.getJSON(this.tcms_url + "/state", function(data) {
        this.setLights(data);
      }.bind(this));
      setTimeout(function(){
        this.fetchLights();
      }.bind(this), 1000);
    },
    setLights: function(lights) {
      this.lights = Object.entries(lights).map(([name, state]) => new Light(name, state));
      this.lights.sort((a, b) => {
        if (a.name < b.name) return -1
        return a.name > b.name ? 1 : 0
      })
    },
    triggerEmergency: function() {
      console.log("triggering Emergency")
      $.ajaxSetup({ cache: false });
      $.getJSON(this.tcms_url + "/emergency", function(data) {
        console.log(data);
      });
    },
    endEmergency: function() {
      console.log("ending Emergency")
      $.ajaxSetup({ cache: false });
      $.getJSON(this.tcms_url + "/emergency_end", function(data) {
        console.log(data);
      });
    },
    requestGreenLight: function(light) {
      console.log(`requesting green light for ${light}`)
      $.ajaxSetup({ cache: false });
      console.log('url: ' + this.vehicle_url + "/requestgreen/" + light)
      $.getJSON(this.vehicle_url + "/requestgreen/" + light, function(data) {
        console.log("DONE requesting green!");
      });
      this.light_request_name = '';
      $("#light-request-modal").modal('hide');
    },
    validURL: function(str) {
      var pattern = new RegExp('^(https?:\\/\\/)?'+ // protocol
        '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|'+ // domain name
        '((\\d{1,3}\\.){3}\\d{1,3}))'+ // OR ip (v4) address
        '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*'+ // port and path
        '(\\?[;&a-z\\d%_.~+=-]*)?'+ // query string
        '(\\#[-a-z\\d_]*)?$','i'); // fragment locator
      return !!pattern.test(str);
    }
  },

  mounted: function() {
    setTimeout(function(){
      this.fetchLights();
    }.bind(this), 100);
  }
});
