
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="ICPSR Web Deposit System">
<meta name="author" content="ICPSR">
<link rel="icon" href="../../favicon.ico">
<title>ICPSR Sead Agent</title>
<!-- Bootstrap core CSS -->
<link href="./resources/scripts/bootstrap/css/bootstrap.min.css"
	rel="stylesheet">
<!-- Custom styles for this template -->
<link href="./resources/scripts/bootstrap/css/jumbotron.css"
	rel="stylesheet">
</head>
<body>
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
				<a class="navbar-brand" href="#">Demo SEAD Agent</a>
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

	<!-- Main jumbotron for a primary marketing message or call to action -->
	<div class="jumbotron">
		<div class="container">
			<h2>Sead Agent!</h2>
			<p>Demonstrates integration between repositories and SEAD
				Matchmaker.</p>
			<p>
				<a class="btn btn-primary btn-lg" href="sead-cp" role="button">Run
					Agent &raquo;</a>
			</p>
		</div>
	</div>

	<div class="container">
		<!-- Example row of columns -->
		<div class="row">
			<div class="col-md-4">
				<h2>Real-time Integration</h2>
				<p>Listens on a RabbitMQ Queue configured by SEAD match maker,
					messages are picked and processed as soon as they arrive.</p>
			</div>
			<div class="col-md-4">
				<h2>System Integration</h2>
				<p>ICPSR technology provides Open REST API to deposit data and
					perform various operations. Please contact technical team, click
					here for currently available API documentation.</p>
				<p>
					<a class="btn btn-default" href="#" role="button">View details
						&raquo;</a>
				</p>
			</div>
			<div class="col-md-4">
				<h2>Query the status</h2>
				<p>Query status of deposits using REST API or provided search
					capabilities.</p>
				<p>
					<a class="btn btn-default" href="#" role="button">View &raquo;</a>
				</p>
			</div>
		</div>

		<hr>

		<footer>
			<p>&copy; Inter-university Consortium for Political and Social
				Research</p>
		</footer>
	</div>
	<!-- /container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="./resources/scripts/jquery/jquery-1.11.2.min.js"></script>
	<script src="./resources/scripts/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>
