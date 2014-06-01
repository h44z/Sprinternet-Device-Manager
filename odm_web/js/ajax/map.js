function initMap() {
	resizeMap();
	
	var mapOptions = {
		center: new google.maps.LatLng(48.2199333, 12.0980843),
		zoom: 5
	};
	global_map = new google.maps.Map(document.getElementById("map_layer"), mapOptions); // global map object
	global_markers = []; // global object for map markers
}

function addMarker(datarow) {
	// if on mobile
	if($('#togglecontrols').is(":visible")) {
		hideControls();
	}
	
	var posLatlng = new google.maps.LatLng(datarow.latitude,datarow.longitude);
	
	var marker = new google.maps.Marker({
		position: posLatlng,
		map: global_map,
		animation: google.maps.Animation.DROP,
		title: 'Last position'
	});
	
	var contentString = '<strong>' + datarow.timestamp + '</strong><br/>'+
		'<p>Locationtype: ' + datarow.type + '<br/>'+
		'Accuracy: ' + datarow.accuracy + '<br/>'+
		'Longitude: ' + datarow.longitude + '<br/>'+
		'Latitude: ' + datarow.latitude + '<br/>';
		
	if(datarow.altitude != "" && parseFloat(datarow.altitude) != 0) {
		contentString += 'Altitude: ' + datarow.altitude + '</p>';
	} else {
		contentString += '</p>';
	}
	
	var infowindow = new google.maps.InfoWindow({
		content: contentString,
		maxWidth: 300
	});
	
	google.maps.event.addListener(marker, 'click', function() {
		infowindow.open(global_map,marker);
	});
	
	global_markers.push(marker);
		
	marker.setMap(global_map);
	
	return marker;
}

function removeAllMarkers() {
	for (var i = 0; i < global_markers.length; i++ ) {
		global_markers[i].setMap(null);
	}
	global_markers.length = 0;
}

function resizeMap() {
	var heightoffet = 72; // offset from header and footer
	var h = $(window).height();
	var w = $(window).width();
	h = h - heightoffet;
	
	$("#map_layer").height(h);
}

// param: datarow
function buildMapOfJSON(data, detailed) {
	var t = data.timestamp.split(/[- :]/);
	var date = new Date(t[0], t[1]-1, t[2], t[3], t[4], t[5]);
	var today = new Date();
	console.log(data);
	var h = date.getHours();
	var m = date.getMinutes();
	if (m < 10)
		m = "0"+m.toString();
	var day = date.getDate();
	var month = date.getMonth();
	var year = date.getFullYear();
	
	if (daysBetween(date, today) == 0) {
		curlocation = "Located <b>today</b> at <b>"+h+":"+m;
	} else {
		curlocation = "Located on <b>"+(month+1)+"/"+day+"/"+year+"</b> at <b>"+h+":"+m;
	}
	
	curlocation_mapped = "Latitude: "+data.latitude+" Longitude: "+data.longitude;
	
	var tmpMarker = addMarker(data);
	
	if(detailed === true) {
		setStatusText(curlocation, curlocation_mapped);
		
		// zoom to marker
		global_map.setZoom(17);
		global_map.panTo(tmpMarker.position);
	}
}

// param: deviceinfo->id
function loadPrevMap(i) {
	var url = 'ajax/connector.php?cmd=dev_getmessageresponse';
	$.post(url, {id: devId, messageid: i}, loadResponse);
	lastCommandType = "location";
}