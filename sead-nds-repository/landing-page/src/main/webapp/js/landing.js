var seadData = {};
var id = null;

seadData.getId = function() {
	if (id == null) {
		id = String(window.location);
		var index = id.indexOf("#");
		if (index != -1) {
			id = id.substring(id.indexOf("#") + 1);
		} else {
			id = "";
		}
	}
	return decodeURIComponent(id);
}

seadData.getMapAjax = function() {

	return $.ajax({
		type : "GET",
		timeout : '10000',
		url : "./api/researchobjects/" + seadData.getId(),
		dataType : "json"
	});
}

seadData.buildGrid = function(map) {
	$('#Title').append(map.describes.Title);

	var pubdate = map["Publication Date"];
	if (pubdate == null) {
		// Backwards compatibility - use map creation date if pub date is not
		// recorded by repo.
		pubdate = map["Creation Date"];
	}
	$('#Date').append(pubdate);

	$('#contacts').append(seadData.formatPeople(map.describes.Contact));
	$('#abstract').append(map.describes.Abstract);

	var p = seadData.formatPeople(map.describes.Creator);
	$('#creators').append(p);
	$('#ID').append(
			$('<div/>').text(map.describes["External Identifier"]));
	$('#keywords').append(seadData.formatKeywords(map.describes.Keyword));
	var lic = map.describes.License;
	var lic = map.describes.License;
	if (lic != null) {
		if (lic.startsWith("http")) {
			var i = lic.indexOf('://creativecommons.org/licenses/');
			if (i != -1) {
				var type = lic.substring(i
						+ '://creativecommons.org/licenses/'.length);
				var end = type.indexOf("/");
				if (end != -1) {
				}
				type = type.substring(0, end);
				$('#license').append(
						$('<a/>').attr('href', lic).attr('target','_blank').append(
								$('<img/>').attr('src',
										'./images/' + type + ".png").attr(
										'alt', 'CC-' + type)));
			} else {
				i = lic.indexOf('://creativecommons.org/publicdomain/zero/1.0');
				if (i != -1) {
					$('#license').append(
							$('<a/>').attr('href', lic).attr('target','_blank').append(
									$('<img/>')
											.attr('src', './images/zero.png')
											.attr('alt', 'CC0')));
				} else {
					i = lic
							.indexOf('://creativecommons.org/publicdomain/mark/1.0');
					if (i != -1) {
						$('#license').append(
								$('<a/>').attr('href', lic).attr('target','_blank').append(
										$('<img/>').attr('src',
												'./images/publicdomain.png')
												.attr('alt',
														'Public Domain Mark')));
					} else {
						$('#license').append(
								$('<a/>').attr('href', lic).text(lic));
					}
				}
			}
		}
	}
	$("#datatable").append(
			$('<table/>').addClass("treetable").append(
					$('<thead/>').append(
							$('<tr/>').html('<th>Name</th><th>Size</th>')))
					.append($('<tbody/>')));
	// Using timeout Allows metadata to display
	setTimeout(function() {
		seadData.loadChildren(map.describes, map.describes, null,
				map.describes.Title);
		activateTable();
	}, 2);
	// seadData.calcTotalSize(map.describes.aggregates);
	// seadData.calcTotalSize(map.describes.aggregates);
	var liveCopy = map.describes["Is Version Of"];
	// Clowder build 113 kludge
	if (liveCopy == null) {
		liveCopy = map.describes["Is Version of"];
	}

	// 1.5 Kludge
	if (!liveCopy.startsWith('http')) {
		var similar = map.describes.similarTo;
		similar = similar.substring(0, similar.indexOf('/resteasy'));
		liveCopy = similar + '#collection?uri=' + liveCopy;
	}
	$('#project').attr('href', map.describes["Publishing Project"]).text(
			map.describes["Publishing Project Name"]);

	$('#livecopy').attr('href', liveCopy).attr('target','_blank');
	$('#actions').append(
			($('<a/>').attr('href', './api/researchobjects/' + seadData.getId()
					+ '/bag')).attr('target','_blank').attr('download',
					seadData.getId().replace(/\W+/g, "_") + '.zip').append(
					$('<div/>').attr('id', 'download').attr('class',
							'btn btn-primary col-xs-6').text(
							'Download Whole Publication')));
	$('#actions').append(
			($('<a/>').attr('href', './api/researchobjects/' + seadData.getId()
					+ '/meta/oremap.jsonld.txt')).attr('target','_blank').attr('download',
					seadData.getId().replace(/\W+/g, "_") + 'oremap.json')
					.append(
							$('<div/>').attr('id', 'map').attr('class',
									'btn btn-primary col-xs-6').text(
									'Download Metadata Only')));
}

seadData.formatPeople = function(people) {
	var p;
	if (Array.isArray(people)) {
		p = $('<div>');
		for (var i = 0; i < people.length; i++) {
			p.append(seadData.formatPerson(people[i]));
		}
	} else {
		p = seadData.formatPerson(people);
	}
	return p;
}

seadData.formatKeywords = function(keywords) {
	var p;
	if (Array.isArray(keywords)) {
		p = $('<div>');
		for (var i = 0; i < keywords.length; i++) {
			var k = keywords[i];
			// Kludge for 1.5 until it removes tag ID info
			// var index = k.indexOf("tag:cet.ncsa.uiuc.edu,2008:/tag#");
			// if (index != -1) {
			// k = k.substring(index + 32);
			// k = k.replace(/\+/g, ' ');
			// }
			if (i > 0) {
				k = ", " + k;
			}
			p.text(p.text() + k);
		}
	} else {
		p = keywords;
	}
	return p;
}

seadData.formatPerson = function(person) {
	if (typeof person == 'string') {
		return $('<div>').text(person);
	} else {
		var title = "Email not available";

		if (person) {
			if (person.email != null) {
				title = person.email;
			}
			return $('<div>').html(
					'<a href="' + person['@id'] + '" target= "_blank" title="' + title + '">'
							+ person.familyName + ', ' + person.givenName
							+ '</a>');
		}
	}
}

seadData.init = function() {

	$.when(

	seadData.getMapAjax()).done(function(map) {

		seadData.buildGrid(map);
	}).fail(function(xhr, textStatus, errorThrown) {
		seadData.problem(xhr, textStatus, errorThrown);
	});
}

seadData.problem = function(xhr, textStatus, errorThrown) {
	if (xhr.status == '404') {
		$('#Title')
				.append(
						"<p>No Data Found for this identifier: "
								+ seadData.getId()
								+ "</p><p>Please use the Contact link below if you believe this is an error.</p>");
	} else if (xhr.status == '500') {
		$('#Title')
				.append(
						"<p>Server Error for this identifier: "
								+ seadData.getId()
								+ "</p><p>Please use the Contact link below to report this problem.</p>");
	}

}

seadData.init();

seadData.loadChildren = function loadChildren(agg, parent, parentid, parentpath) {
	var children = parent["Has Part"];
	if (Array.isArray(children)) {
		for (var i = 0; i < children.length; i++) {
			var child = $.grep(agg.aggregates, function(e) {
				return e['Identifier'] === children[i];
			});
			child = child[0];
			if (seadData.isCollection(child)) {
				$('#datatable tbody').append(
						getCollectionRow(parentid, i, child.Title,
								child.Identifier));
				var newId = i;
				if (parentid != null) {
					newId = parentid + '-' + newId;
				}
				seadData.loadChildren(agg, child, newId, parentpath + '%2F'
						+ child.Title);

			} else {

				var fileSize = child.Size;

				// Clowder Build 113 kludge
				if (fileSize == null) {
					fileSize = child.size;
				}
				$('#datatable tbody').append(
						getDataRow(parentid, i, child.Title, parentpath + '%2F'
								+ child.Title, fileSize));
			}

		}
	}

}

seadData.calcTotalSize = function calcTotalSize(list) {
	var total = 0;
	for (var i = 0; i < list.length; i++) {
		if (list[i].hasOwnProperty("Size")) {
			total += parseInt(list[i].Size);
		}
	}
	alert(total);
}

seadData.isCollection = function isCollection(item) {
	if (item.hasOwnProperty('Has Part'))
		return true;
	var type = item['@type'];
	if (typeof type == 'string') {
		return ((type === "http://cet.ncsa.uiuc.edu/2007/Collection")||(type === 'http://cet.ncsa.uiuc.edu/2016/Folder'));
	} else {
		for (var i = 0; i < type.length; i++) {
			if ((type[i] === "http://cet.ncsa.uiuc.edu/2007/Collection")||(type === 'http://cet.ncsa.uiuc.edu/2016/Folder'))
				return true;
		}
	}
	return false;
}

function getDataRow(parentId, childId, name, uri, size) {
	var newRow = $('<tr/>');
	if (size == null) {
		size = 0;
	}
	if (parentId != null) {
		childId = parentId + '-d' + childId;
	} else {
		childId = 'd' + childId;
	}
	newRow.attr('data-tt-id', childId);
	if (parentId != null) {
		newRow.attr('data-tt-parent-id', parentId);
	}

	var id = seadData.getId();
	// id = id.replace(/\W+/g, "_");

	newRow.append($('<td/>').append(
			$('<span/>').addClass('file').append(
					$('<a/>').attr('href',
							'./api/researchobjects/' + id + '/data/' + uri).attr('target','_blank')
							.html(name))));
	newRow.append($('<td/>').html(filesize(parseInt(size), {
		base : 10
	})));
	return (newRow);
}

function getCollectionRow(parentId, childId, name, uri, size) {
	var newRow = $('<tr/>');
	if (parentId != null) {
		childId = parentId + '-' + childId;
	}
	newRow.attr('data-tt-id', childId);
	if (parentId != null) {
		newRow.attr('data-tt-parent-id', parentId);
	}
	newRow
			.append($('<td/>').append(
					$('<span/>').addClass('folder').text(name)));
	newRow.append($('<td/>').html('--'));
	// return newRow;
	return newRow.add($('<tr/>').attr('data-tt-id', childId + "-0").attr(
			'data-tt-parent-id', childId));
}

function activateTable() {
	var table = $('#datatable table');
	table.treetable({
		expandable : true

	});

}
