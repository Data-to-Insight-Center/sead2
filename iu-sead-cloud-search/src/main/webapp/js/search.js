var rowNum = 1;
var apiprefix = "./ro";

$(document).ready(function ()
	{
		//loadRO();
	var xmlhttp = new XMLHttpRequest();
	var url = apiprefix + "/researchobjects";

	xmlhttp.onreadystatechange=function() {
		if (xmlhttp.readyState == 4 ) {
			myFunction(xmlhttp.responseText);
		}
	}
	xmlhttp.open("GET", url, true);
	xmlhttp.send();			
		
	//function loadRO(){
//	$.ajax({
//			url: apiprefix + "/researchobjects",
//			type: "GET",
//			cache: false,
//			dataType: 'json',
//			contentType : 'application/json; charset=utf-8',
//			success: function (data, status, jqXHR) {
//				alert("Hi");
//				// do something
//			 },
//			 error: function (jqXHR, status) {
//				 alert("Bye");
//			 }
//			});
//	}

});		
	
function myFunction(response) {
	arr = JSON.parse(response);
	var ros = [];
	
	for(var i = 0; i < arr.length; i++) {
		var creator= arr[i].Creator;
		var abstract= arr[i].Abstract;
		var title= arr[i].Title;
		var identifier= arr[i].Identifier;
		var pub_date= arr[i].PublicationDate;
		var create_date= arr[i].CreationDate;
		var doi= arr[i].DOI;
	
	ros.push( { "Creator":creator, "Abstract": abstract, "Title": title, "Identifier": identifier, 
			"PublicationDate":pub_date, "CreationDate":create_date,"DOI":doi});
	}

$("#viewRowTemplate").tmpl(ros).appendTo("#CRUDthisTable");

}



