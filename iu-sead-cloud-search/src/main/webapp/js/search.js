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
		var creator= arr[i].Creator;
		var new_creator = "";
		if($.isArray(creator)) {
			for (var c=0; c < creator.length; c++){
				if (c=creator.length-1){
					new_creator += creator[c];
				}else{
					new_creator += creator[c] + ",\n";
				}
			}
		} else {
			new_creator = creator;
		}

		var abstract= arr[i].Abstract;
		var new_abstract = "";
		if($.isArray(abstract)) {
			for (var a=0; c < abstract.length; a++){
				if (a=abstract.length-1){
					new_abstract += abstract[a];
				}else{
					new_abstract += abstract[a] + ",\n";
				}
			}
		} else {
			new_abstract = abstract;
		}
		var title= arr[i].Title;
		var identifier= arr[i].Identifier;
		var pub_date= arr[i]["Publication Date"];
		var create_date= arr[i]["Creation Date"];
		var doi= arr[i].DOI;

	ros.push( { "Creator":new_creator, "Abstract": new_abstract, "Title": title, "Identifier": identifier,
			"PublicationDate":pub_date, "CreationDate":create_date,"DOI":doi});
	}

$("#viewRowTemplate").tmpl(ros).appendTo("#CRUDthisTable");

}



