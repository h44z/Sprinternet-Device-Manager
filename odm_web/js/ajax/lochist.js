function loadLocationHistory(daystart, dayend) {
	if (typeof(devId) !== 'undefined') {
		var url = 'ajax/connector.php?cmd=dev_lochist';
		$.post(url, {id: devId, daystart: daystart, dayend: dayend}, gotLocationHistory);
	}
}

function gotLocationHistory(data) {
	removeAllMarkers();
	if(typeof global_route !== "undefined")
		global_route.setMap(null);
		
	if(data && data.result == true && data.hasdata == true) {
		var location = data.datarow;
		var bounds = new google.maps.LatLngBounds();
		var routeCoords = [];
		
		if(location) {
			for (var i = 0; i < location.length; i++) {
				addMarker(location[i]);
				var latLng = new google.maps.LatLng(location[i].latitude, location[i].longitude);
				bounds.extend(latLng);
				routeCoords.push(latLng);
			}
		}
		
		global_route = new google.maps.Polyline({
			path: routeCoords,
			geodesic: true,
			strokeColor: '#FF0000',
			strokeOpacity: 1.0,
			strokeWeight: 2
		});
		
		global_route.setMap(global_map);
		global_map.fitBounds(bounds);
	}
}

function pad(num, size) {
    var s = num+"";
    while (s.length < size) s = "0" + s;
    return s;
}

function initLocDateChooser() {
	var today = new Date();
	var tomorrow = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
	
	var todayString = today.getFullYear() + "-" + (pad(today.getMonth()+1, 2)) + "-" + pad(today.getDate(), 2);
	var tomorrowString = tomorrow.getFullYear() + "-" + (pad(tomorrow.getMonth()+1, 2)) + "-" + pad(tomorrow.getDate(), 2);
	
	$('#loc_startdate').val(todayString);
	$('#loc_enddate').val(tomorrowString);
}

$(document).ready(function() {
	var today = new Date();
	var tomorrow = new Date(new Date().getTime() + 24 * 60 * 60 * 1000);
	
	var todayString = today.getFullYear() + "-" + (pad(today.getMonth()+1, 2)) + "-" + pad(today.getDate(), 2);
	var tomorrowString = tomorrow.getFullYear() + "-" + (pad(tomorrow.getMonth()+1, 2)) + "-" + pad(tomorrow.getDate(), 2);

	initLocDateChooser();
	loadLocationHistory(todayString, tomorrowString);
	
	$('#loc_startdate').Zebra_DatePicker({
		onSelect: function(format, yyyymmdd, date, obj) {
			var nextDay = new Date(date.getTime() + 24 * 60 * 60 * 1000);
			var nextDayString = nextDay.getFullYear() + "-" + (pad(nextDay.getMonth()+1, 2)) + "-" + pad(nextDay.getDate(), 2);
			$('#loc_enddate').val(nextDayString);
			
			var datepicker = $('#loc_enddate').data('Zebra_DatePicker');
			datepicker.update();
		},
		direction: 0,
		format: 'Y-m-d',
		pair: $('#loc_enddate')
	});

	$('#loc_enddate').Zebra_DatePicker({
		format: 'Y-m-d',
		direction: 1
	});
	
	$('#loc_date_submit').off('click').on('click',  function() {
		hideControls();
		loadLocationHistory($('#loc_startdate').val(), $('#loc_enddate').val());
	});
});