<?php
global $current_page;
$current_page = "main";
if(isset($_GET["p"]) && !empty($_GET["p"])) {
	$current_page = $_GET["p"];
}

function currPage($pname = "main") {
	global $current_page;
	
	if($pname == $current_page) {
		echo " w--current";
	}
}
?>
<!DOCTYPE html>
<html data-wf-site="<?php echo $current_page; ?>">
<head>
  <meta charset="utf-8">
  <title><?php echo _("title");?></title>
  <meta name="description" content="<?php echo _("index_meta_description");?>">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" type="text/css" href="css/normalize.css">
  <link rel="stylesheet" type="text/css" href="css/webflow.css">
  <link rel="stylesheet" type="text/css" href="css/sprinternet-device-manager.webflow.css">
  <script type="text/javascript" src="js/jquery.min.js"></script>
  <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDsDnQDzeemwozaPgGbNW99qL2Ag7ySKsM&sensor=false"></script>
  <script src="js/webfont.js"></script>
  <script src="js/ajax/misc.js"></script>
  <script src="js/ajax/gui.js"></script>
  <script src="js/ajax/map.js"></script>
  <?php 
	if($current_page != "locationhistory") {
  ?>
	<script src="js/ajax/image.js"></script>
	<script src="js/ajax/sysinfo.js"></script>
	<script src="js/ajax/audio.js"></script>
  <?php
	}
  ?>
  <script src="js/ajax/ajax.js"></script>
  <script>
    WebFont.load({
      google: {
        families: ["Bitter:400,700","Montserrat:400,700"]
      }
    });
  </script>
  <script>
    if (/mobile/i.test(navigator.userAgent)) document.documentElement.className += ' w-mobile';
  </script>
  <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
  <!--[if lt IE 9]><script src="js/html5shiv.min.js"></script><![endif]-->
</head>
<body>
  <div class="full_overlay"></div>
  <div id="pPrompt"> 
	<!-- custom javascript prompts! -->
	<!-- up to 3 fields =) -->
	<form id="pForm"> 
		<input id="pForm_field1" class="w-input" type="text" placeholder="Field 1" name="field1" data-name="field1"></input>
		<input id="pForm_field2" class="w-input" type="text" placeholder="Field 2" name="field2" data-name="field2"></input>
		<input id="pForm_field3" class="w-input" type="text" placeholder="Field 3" name="field3" data-name="field3"></input>
		<input type="button" id="pForm_ok" value="<?php echo _("btn_ok"); ?>" onclick="promptDefaultCallbackOK()" />
		<input type="button" id="pForm_cancel" value="<?php echo _("btn_cancel"); ?>" onclick="promptDefaultCallbackCancel()" /> 
	</form> 
  </div> 
  <div class="w-container header">
    <div class="w-nav sdm_navbar" data-collapse="medium" data-animation="default" data-duration="400" data-contain="1">
      <div class="w-container">
        <a class="w-nav-brand sdm_brand" href="index.php"></a>
        <nav class="w-nav-menu" role="navigation">
			<a class="w-nav-link sdm_navlink<?php currPage();?>" href="odm.php"><?php echo _("btn_home"); ?></a>
			<?php if (isset($_SESSION['user_id'])) { ?>
				<a class="w-nav-link sdm_navlink<?php currPage("locationhistory");?>" href="odm.php?p=locationhistory"><?php echo _("btn_locationhistory"); ?></a>
				<a class="w-nav-link sdm_navlink<?php currPage("changepassword");?>" href="odm.php?p=changepassword"><?php echo _("btn_changepassword"); ?></a>
				<a class="w-nav-link sdm_navlink" href="ajax/connector.php?cmd=logout"><?php echo _("btn_logout"); ?></a>
			<?php } ?>			
        </nav>
        <div class="w-nav-button">
          <div class="w-icon-nav-menu"></div>
        </div>
      </div>
    </div>
  </div>