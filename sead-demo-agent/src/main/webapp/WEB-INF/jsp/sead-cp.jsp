<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<link rel="icon" href="../../favicon.ico">
<title>Sead Agent</title>
<!-- Bootstrap core CSS -->
<link href="./resources/scripts/bootstrap/css/bootstrap.min.css"
	rel="stylesheet">
<style>
.drop-panel {
	background-color: #E6E6E6;
	border: thin;
	border-style: dotted;
	border-width: 1px;
	padding-top: 100px;
	padding-bottom: 100px;
}

.container-padding {
	padding-top: 50px;
	padding-bottom: 10px;
}
</style>
</head>
<body id="myDropzone">
	<nav class="navbar navbar-inverse navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed"
					data-toggle="collapse" data-target="#navbar" aria-expanded="false"
					aria-controls="navbar">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href=".">Demo SEAD Agent</a>
			</div>
			<div id="navbar" class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<li><a href="#about">About</a></li>
					<li><a href="#contact">Contact</a></li>
				</ul>
				<form class="navbar-form navbar-right">
					<button type="submit" class="btn btn-success">Sign in</button>
				</form>
			</div>

			<div id="navbar" class="collapse navbar-collapse">
				</ul>
			</div>

			<!--/.navbar-collapse -->
		</div>
	</nav>
	<br>
	<br>
	<br>
	<br>
	<br>
	<div class="container">
		<table>
			<caption>Files will be downloaded and saved to location
				"/temp/sead/downloads"</caption>
			<tbody>
				<tr>
					<td><form action="invoke" target="_invokeSead">
							Repository URL:<input type="text" size="100"
								name="researchObjectUrl"
								value="https://sead-test.ncsa.illinois.edu/sead-cp/cp/repositories/openicpsr/researchobjects">
							<button type="submit" class="btn btn-primary">go</button>
							<br>Send received status?:<input type="checkbox" name="ack" />
							<br>Send dummy DOI?:<input type="checkbox" name="doi" />

						</form></td>
				</tr>
			</tbody>
		</table>
	</div>
	<script src="resources/scripts/jquery/jquery-1.11.2.min.js"></script>
	<script src="resources/scripts/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>