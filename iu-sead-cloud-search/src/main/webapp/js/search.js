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

});

$("#filter-search-button").click(function () {
	$("#CRUDthisTable").empty();
});		
	
function myFunction(response) {
	arr = JSON.parse(response);
	var ros = [];
	
	for(var i = 0; i < arr.length; i++) {
		var creator_results = Array();
		var creator= arr[i].Creator;
		var new_creator = "";
		if($.isArray(creator)) {
			for (var c=0; c < creator.length; c++){
				creator_results.push('<span>' + creator[c] + '</br></span>');
			}
			new_creator += creator_results.join("");
		} else {
			new_creator += creator;
		}
		var new_creator_out = new_creator;

		var abstract_results = Array();
		var abstract= arr[i].Abstract;
		var new_abstract = "";
		if($.isArray(abstract)) {
			for (var a=0; a < abstract.length; a++){
				abstract_results.push('<span>' + abstract[a] + '</br></span>');
			}
			new_abstract += abstract_results.join("");
		} else {
			new_abstract += abstract;
		}
		var new_abstract_out = new_abstract;

		var title= arr[i].Title;
		var identifier= arr[i].Identifier;
		var pub_date= arr[i]["Publication Date"];
		var create_date= arr[i]["Creation Date"];
		var doi= arr[i].DOI;

	ros.push( { "Creator":new_creator_out, "Abstract": new_abstract_out, "Title": title, "Identifier": identifier,
			"PublicationDate":pub_date, "CreationDate":create_date,"DOI":doi});
	}

$("#viewRowTemplate").tmpl(ros).appendTo("#CRUDthisTable");

}



