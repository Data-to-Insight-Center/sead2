
function loadPeople() {
$('#people>div>table>tbody').empty();

$.ajax({
		type : "GET",
		url : "./api/people",
		dataType : "json",
		success : updatePeople,
		error : errorAlert
	});

}


function loadRepos() {
$('#repositories>div>table>tbody').empty();

$.ajax({
		type : "GET",
		url : "./api/repositories",
		dataType : "json",
		success : updateRepos,
		error : errorAlert
	});

}

function errorAlert(msg) {
alert("fail");
alert(msg);
}


function updatePeople(people) {


(function ($) {

        $('#peoplefilter').keyup(function () {

            var rex = new RegExp($(this).val(), 'i');
            $('#people .searchable tr').hide();
            $('#people .searchable tr').filter(function () {
                return rex.test($(this).text());
            }).show();

        })

    }(jQuery));

people=people.persons;
for(var x in people) {

$('#people>div>table')
.append($('<tr/>')
	.append($('<td/>').append($('<a/>').text(people[x].givenName + people[x].familyName).attr('href','./api/people/' + people[x]['@id'])))
	.append($('<td/>').append($('<a/>').text(people[x].email).attr('href',"mailTo:" + people[x].email)))
.append($('<td/>').append($('<a/>').text(people[x].PersonalProfileDocument).attr('href',people[x].PersonalProfileDocument)))
.append($('<td/>').text(people[x].affiliation)));
}

}


function updateRepos(repos) {


(function ($) {

        $('#repofilter').keyup(function () {

            var rex = new RegExp($(this).val(), 'i');
            $('#repositories .searchable tr').hide();
            $('#repositories .searchable tr').filter(function () {
                return rex.test($(this).text());
            }).show();

        })

    }(jQuery));

for(var x in repos) {

$('#repositories>div>table')
.append($('<tr/>')
	.append($('<td/>').append($('<a/>').text(repos[x].repositoryName).attr('href','./api/repositories/' + repos[x].orgidentifier)))
	.append($('<td/>').append($('<a/>').text(repos[x].repositoryURL).attr('href',repos[x].repositoryURL)))
.append($('<td/>').text(repos[x].lastUpdate)).attr('onmouseover','showDets(\"./api/repositories/' + repos[x].orgidentifier +'\", ' + '\"#repositoriesDetails\")')
);
}

}

function loadRequests(stage, desired, anchor) {
$(anchor + '>div>table>tbody').empty();
$.ajax({
		type : "GET",
		url : "./api/researchobjects",
		dataType : "json",
		success : function (response) {
		         updateRequests(response, stage, desired, anchor)},
		error : errorAlert
	});

}


function updateRequests(requests, stage, desired, anchor) {

(function ($) {

        $(anchor + 'filter').keyup(function () {

            var rex = new RegExp($(this).val(), 'i');
            $('#requests .searchable tr').hide();
            $('#requests .searchable tr').filter(function () {
                return rex.test($(this).text());
            }).show();

        })

    }(jQuery));


for(var x in requests) {
if(requests[x].Aggregation != null) {
var statusArray = requests[x].Status;
statusArray.sort(function(a,b){
  // Turn your strings into dates, and then subtract them
  // to get a value that is either negative, positive, or zero.

  return new Date(a.date) - new Date(b.date);
});
if((statusArray[statusArray.length-1].stage==stage)==desired) {
$(anchor + '>div>table').append($('<tr/>').append($('<td/>')
	.append($('<div/>').text(requests[x].Aggregation.Title))).append($('<td/>').append($('<div/>').text(statusArray[0].date)))
.append($('<td/>').append($('<div/>').text(statusArray[statusArray.length-1].date)))
	.append($('<td/>').append($('<div/>').text(requests[x].Repository))).attr('onmouseover','showDets(\"./api/researchobjects/' + requests[x].Aggregation.Identifier +'\", ' + '\"' + anchor + 'Details\")'));
}

}
}
}
var map={};

function showDets(item, anchor) {
if(map[anchor]==item) {
return;
} else {

$.ajax({
		type : "GET",
		url : item,
		dataType : "json",
		success : function (response) {
		         showRequest(response, anchor)},
		error : errorAlert
	});
map[anchor] = item;
}

}



function showRequest(item, anchor) {
item = whap(item);

if(!($(anchor + ' .jqtree-tree').length)) {
    $(anchor).tree({
        data: item,
		autoOpen: 1,
       onCreateLi: function(node, $li) {
            // Append a link to the jqtree-element div.
           var text = $li.find('.jqtree-title').text();
		var i = text.indexOf(":");
		
		$li.find('.jqtree-title').html("<div class='key'>" + text.substring(0,i) + "</div><div class='value'>" + text.substring(i) + "</div>");

       }

});
} else {

    $(anchor).tree('loadData', item);
}
}

function whap(rawtree) {
//Build a tree as jqTree expects
var tree = [];
if(rawtree instanceof Array) {
	for(var i in rawtree) {
		addNodes(tree, i, rawtree[i]);
	}
} else {
	for(var i in rawtree) {
	if(rawtree.hasOwnProperty(i)) {

if (typeof rawtree[i] === 'object') {
		addNode(tree, i, rawtree[i]);
	} else {
	      var child = {};
		child['label'] = i + " " + rawtree[i];
		tree.push(child);
	}

}
}
}
return tree;
}

function addNode(tree, label, rawobject) {
  var node = {};
	node['label']= label;

	var children = [];
var prefix="";
if(rawobject instanceof Array) {
prefix=label + "_";
}
	for(var i in rawobject) {
	if(rawobject.hasOwnProperty(i)) {
	if (typeof rawobject[i] === 'object') {
		addNode(children, prefix+i, rawobject[i]);
	} else {

	      var child = {};
		child['label'] = prefix+i + ": " + rawobject[i];
		children.push(child);
	}

}

}
if(children.length>0) {
node['children']=children;
}
	tree.push(node);
}
 
