var rowNum = 1;
var apiprefix = "./ro";

$(document).ready(function ()
	{
	bool_pagination = true;
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
	var curr_url = window.location.href;
	if (curr_url.indexOf("search.html?q=") > -1){
		var search_string = curr_url.split("=")[1];
		$("input:text").first().val(search_string);
		var res_html = document.getElementById("CRUDthisTable").innerHTML + pagination();
		var res_title = document.title;
		var url_path = '/iu-sead-cloud-search/search.html';
		window.history.pushState({"html":res_html,"pageTitle":res_title},"", url_path);
	}else{
		arr = JSON.parse(response);
		ros = [];

		for(var i = 0; i < arr.length; i++) {
			var creator_results = Array();
			var creator= arr[i].Creator;
			var new_creator = "";
			if($.isArray(creator)) {
				for (var c=0; c < creator.length; c++){
					var creator_str;
					if(c==creator.length-1) {
						creator_str = '<span>' + creator[c] + '</span>';
						} else {
							creator_str = '<span>' + creator[c] + '</br></span>';
							}
					creator_results.push(creator_str);
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
					var abstract_str;
					if(a==abstract.length-1) {
						abstract_str = '<span>' + abstract[a] + '</span>';
						} else {
							abstract_str = '<span>' + abstract[a] + '</br></span>';
							}
					abstract_results.push(abstract_str);
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
}



