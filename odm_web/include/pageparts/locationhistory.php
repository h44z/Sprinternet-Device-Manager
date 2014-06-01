<?php

include 'include/checklogin.php';

$DEVMAN = DeviceManager::getInstance();
$nrOfDevices = $DEVMAN->current_user->getDeviceCount();

if ($nrOfDevices > 0) {
	$count = 0;
	$dropdown = "";
	$first_name = "";
	$first_gcm_regid = "";
	$first_created_at = "";
	$first_id = 0;
	foreach ($DEVMAN->current_user->device as $device) {
		$phpdate = strtotime($device->created_at);
		$formated_created_at = date('d/m/Y', $phpdate);
		if (($count == 0 && !isset($id)) || (isset($id) && $id == $device->id)) {
			$first_name = $device->name;
			$first_created_at = $formated_created_at;
			$first_id = $device->id;
			$dropdown .= '<div class="device-summary selected-tab" onclick="selectDevice(\''.$device->id.'\')">';
		} else {
			$dropdown .= '<div class="device-summary" onclick="selectDevice(\''.$device->id.'\')">';
		}
		$dropdown .= '	<div class="summary-text">';
		$dropdown .= '		<div class="device-name" title="' . _("main_choose_device") . ' '.$device->name.'">'.$device->name.'</div>';
		$dropdown .= '		<div class="device-registered">' . _("main_registered_at") . ' '.$formated_created_at.'</div>';
		$dropdown .= '	</div>';
		$dropdown .= '</div>';
		$count++;
	}
?>
  <link rel="stylesheet" type="text/css" href="css/zebra_metallic.css" />
  <script type="text/javascript" src="js/zebra_datepicker.js"></script>

  <div class="dev_overlay dev_mainoverlay">
    <div class="w-row dev_row">
      <div class="w-col w-col-8 dev_top_selector">		
        <div id="device_button" class="dev_device_dropdown" title="<?php echo _("main_choose_a_device"); ?>">
          <h3><?php echo $first_name; ?></h3>
          <h6><?php echo _("main_registered_at"); ?> <?php echo $first_created_at; ?></h6>
        </div>
        <div class="dev_device_selector" id="device-dropdown">
			<?php echo $dropdown; ?>
		</div>
      </div>
    </div>
	<script type="text/javascript">
		var devId = "<?php echo $first_id; ?>"; // global javascript variable = current device
	</script>
	<script src="js/ajax/lochist.js"></script>
	<div id="loc_date">
		<label for="loc_startdate"><?php echo _("loc_startdate"); ?></label>
		<input class="w-input" name="loc_startdate" id="loc_startdate" type="text" />
		<label for="loc_enddate"><?php echo _("loc_enddate"); ?></label>
		<input class="w-input" name="loc_enddate" id="loc_enddate" type="text" />
		<input id="loc_date_submit" class="w-button button" type="submit" value="<?php echo _("loc_get_location");?>" data-wait="<?php echo _("please_wait");?>"></input>
	</div>
  </div>
  <div class="w-hidden-main w-hidden-medium dev_togglecontrols dev_togglecontrolsup" id="togglecontrols">
	<!-- toggle the whole div -->
  </div>
<?php
	} else {
?>
	<div class="dev_overlay dev_mainoverlay">
		<h2><?php echo _("main_no_devices");?></h2>		
		<h4><?php echo _("newdev_scan_qr");?></h4>
		<img src="images/qrcode-latest-apk.png" alt="qrcode-latest-apk.png">
		<h4><?php echo _("newdev_download_app");?></h4><a class="dev_link" href="<?php echo $GOOGLE_PLAY_STORE;?>"><?php echo $GOOGLE_PLAY_STORE;?></a>
	</div>
<?php
	}
?>