<html>
	<head>
		<script>
            var getUrl = window.location;
			window.parent.postMessage("3DS.Notification.Received", getUrl.protocol + "//" + getUrl.host);
		</script>
    </head>
	<body></body>
</html>