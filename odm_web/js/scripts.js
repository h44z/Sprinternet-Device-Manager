$(document).ready(function() {
	/**
	 * initialize click handler
	 */
	$("#device_button").off('click').on('click', toggleDevices);
	$("#button").off('click').on('click', toggleCommands);
	$("#cancelwait").off('click').on('click', cancelWait);
	$("#togglecontrols").off('click').on('click', toggleControls);
	
	if(typeof devId !== "undefined" && typeof sendCustomCommand !== "undefined") {
		$("#cmd_loc").off('click').on('click', {id: devId, cmd: 'Type=location:Command:GetLocation'}, sendCustomCommand);
		$("#cmd_gps").off('click').on('click', {id: devId, cmd: 'Type=location:Command:GetLocationGPS'}, sendCustomCommand);
		$("#cmd_lck").off('click').on('click', {id: devId, cmd: 'Type=lock:Command:Lock'}, sendCustomCommand);
		$("#cmd_pas").off('click').on('click', {id: devId}, sendLockPass);
		$("#cmd_rea").off('click').on('click', {id: devId, cmd: 'Type=photo:Command:RearPhoto'}, sendCustomCommand);
		$("#cmd_fro").off('click').on('click', {id: devId, cmd: 'Type=photo:Command:FrontPhoto'}, sendCustomCommand);
		$("#cmd_rng").off('click').on('click', {id: devId, cmd: 'Type=ring:Command:StartRing'}, sendCustomCommand);
		$("#cmd_nrn").off('click').on('click', {id: devId, cmd: 'Type=ring:Command:StopRing'}, sendCustomCommand);
		$("#cmd_sms").off('click').on('click', {id: devId}, sendSMS);
		$("#cmd_wpe").off('click').on('click', {id: devId}, sendWipe);
		$("#cmd_ntf").off('click').on('click', {id: devId}, sendNotification);
		$("#cmd_sys").off('click').on('click', {id: devId, cmd: 'Type=info:Command:SystemInfo'}, sendCustomCommand);
		$("#cmd_aud").off('click').on('click', {id: devId}, sendCaptureAudio);
		$("#cmd_rem").off('click').on('click', {id: devId}, removeDevice);
	}
	/**
	 * Display inital map and load previos messages
	 */
	initMap();
	if(typeof devId !== "undefined" && typeof sendCustomCommand !== "undefined") {
		loadMessages(10);
	}
	
	$(window).on("resize", resizeMap);
});

var lastCommandId = null;
var lastCommandType = null;