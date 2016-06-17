var seadData = {};
var id = null;
var uriRoot = "./api/researchobjects/";

$body = $("body");

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

seadData.getAggregationAjax = function() {

	return $.ajax({
		type : "GET",
		timeout : '15000',
		url : uriRoot + seadData.getId() + "/metadata",
		dataType : "json"
	});
}

seadData.getDescriptionOnlyAjax = function() {

	return $.ajax({
		type : "GET",
		timeout : '15000',
		url : uriRoot + seadData.getId(),
		dataType : "json"
	});
}

seadData.getRepositoryInfoAjax = function() {

	return $.ajax({
		type : "GET",
		timeout : '15000',
		url : "./api/repository",
		dataType : "json"
	});
}

seadData.buildGrid = function(describes) {

	seadData.addDataTable(describes);
	$('.loading').hide();

}

seadData.fillInMetadata = function(describes) {
	$('#Title').append(describes.Title);

	var pubdate = describes["Publication Date"];

	$('#Date').append(pubdate);

	$('#contacts').append(seadData.formatPeople(describes.Contact));
	$('#abstract').append($('<pre/>').text(he.decode(describes.Abstract)));

	var p = seadData.formatPeople(describes.Creator);
	$('#creators').append(p);
	$('#ID').append($('<div/>').text(describes["External Identifier"]));
	$('#keywords').append(seadData.formatKeywords(describes.Keyword));
	var lic = describes.License;

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
						$('<a/>').attr('href', lic).attr('target', '_blank')
								.append(
										$('<img/>').attr('src',
												'./images/' + type + ".png")
												.attr('alt', 'CC-' + type)));
			} else {
				i = lic.indexOf('://creativecommons.org/publicdomain/zero/1.0');
				if (i != -1) {
					$('#license').append(
							$('<a/>').attr('href', lic)
									.attr('target', '_blank').append(
											$('<img/>').attr('src',
													'./images/zero.png').attr(
													'alt', 'CC0')));
				} else {
					i = lic
							.indexOf('://creativecommons.org/publicdomain/mark/1.0');
					if (i != -1) {
						$('#license').append(
								$('<a/>').attr('href', lic).attr('target',
										'_blank').append(
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

	var stats = describes['Aggregation Statistics'];
	if (stats != null) {
		var statdiv = $('<div/>').attr("id", "stats");
		statdiv.append($('<div/>').append(
				"Total Size (unzipped): "
						+ filesize(parseInt(stats["Total Size"]), {
							base : 10
						})));
		statdiv.append($('<div/>').append(
				"Number of Files: " + stats["Number of Datasets"]));
		statdiv.append($('<div/>').append(
				"Largest File: "
						+ filesize(parseInt(stats["Max Dataset Size"]), {
							base : 10
						})));
		statdiv.append($('<div/>').append(
				"Number of Folders: " + stats["Number of Collections"]));
		statdiv.append($('<div/>').append(
				"Longest Folder Path: "
						+ (parseInt(stats["Max Collection Depth"]) + 1)));
		var types = stats["Data Mimetypes"];
		if (Array.isArray(types)) {
			var typestring = "";
			for (var i = 0; i < types.length; i++) {
				typestring = typestring + types[i];
				if (i < types.length - 1) {
					typestring = typestring + ", ";
				}
			}
		}
		statdiv.append($('<div/>').append("Data Mimetypes: " + typestring));
		$('#contents').prepend(statdiv);
	}
}

seadData.addDataTable = function(describes) {
	$("#datatable").append(
			$('<table/>').addClass("treetable").append(
					$('<thead/>').append(
							$('<tr/>').html('<th>Name</th><th>Size</th>')))
					.append($('<tbody/>')));
	// Using timeout Allows metadata to display
	setTimeout(function() {
		seadData.loadChildren(describes, null, describes.Title);
		activateTable();
		$('.loading').hide();
	}, 1);
}

seadData.addDownloadLinks = function(describes) {

	var liveCopy = describes["Is Version Of"];
	// Clowder build 113 kludge
	if (liveCopy == null) {
		liveCopy = describes["Is Version of"];
	}

	// 1.5 Kludge
	if (!liveCopy.startsWith('http')) {
		var similar = describes.similarTo;
		similar = similar.substring(0, similar.indexOf('/resteasy'));
		liveCopy = similar + '#collection?uri=' + liveCopy;
	}
	$('#project').attr('href', describes["Publishing Project"]).text(
			describes["Publishing Project Name"]);

	$('#livecopy').attr('href', liveCopy).attr('target', '_blank');
	$('#actions').append(
			($('<a/>').attr('href', uriRoot + seadData.getId() + '/bag')).attr(
					'target', '_blank').attr('download',
					seadData.getId().replace(/\W+/g, "_") + '.zip').append(
					$('<div/>').attr('id', 'download').attr('class',
							'btn btn-primary col-xs-6').text(
							'Download Whole Publication')));
	$('#actions').append(
			($('<a/>').attr('href', uriRoot + seadData.getId()
					+ '/meta/oremap.jsonld.txt')).attr('target', '_blank')
					.attr(
							'download',
							seadData.getId().replace(/\W+/g, "_")
									+ 'oremap.json').append(
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
					'<a href="' + person['@id'] + '" target= "_blank" title="'
							+ title + '">' + person.familyName + ', '
							+ person.givenName + '</a>');
		}
	}
}

var aggTitle = "";

seadData.init = function() {
	$('.loading').show();
	$.when(seadData.getDescriptionOnlyAjax()).done(
			function(describes) {
				// Analytics tracking
				aggTitle = describes.Title;
				ga('send', 'event', aggTitle + '::' + seadData.getId(),
						'View Publication', aggTitle);

				seadData.fillInMetadata(describes);
				seadData.addDownloadLinks(describes);
			}).fail(function(xhr, textStatus, errorThrown) {
		seadData.problem(xhr, textStatus, errorThrown);
	});
	$.when(seadData.getAggregationAjax()).done(
			function(describes) {
				// Analytics tracking
				aggTitle = describes.Title;
				ga('send', 'event', aggTitle + '::' + seadData.getId(),
						'View Publication', aggTitle);

				seadData.buildGrid(describes);
			}).fail(function(xhr, textStatus, errorThrown) {
		seadData.problem(xhr, textStatus, errorThrown);
	});
	seadData
			.getRepositoryInfoAjax()
			.done(
					function(repojson) {
						$('title').text(repojson.repositoryName);
						$('#heading').text(repojson.repositoryName);
						$('#about').text("About: " + repojson.repositoryName);
						$('#repo').text(repojson.repositoryName).attr('href',
								repojson.repositoryURL);
						if (repojson.subject) {
							if (typeof repojson.subject === 'string') {
								$('#subject').text(repojson.subject);
							} else {
								$('#subject').text(repojson.subject.join(', '));

							}
						}
						if (repojson.institution) {
							$('#institution').text(repojson.institution);
						}
						if (repojson.description) {
							if (repojson.description.content) {
								$('#repodesc').append(
										repojson.description.content);
							} else {
								$('#repodesc').append(repojson.description);
							}
						}

						if (repojson.repositoryContact) {
							$('#repocontact').text(repojson.repositoryContact);
							if (repositoryContact.indexOf('@')) {
								$('#repocontact')
										.append(
												$('<a/>')
														.text(
																repojson.repositoryName)
														.attr(
																'href',
																'mailto:'
																		+ repojson.repositoryContact));
							} else {
								$('#repocontact')
										.append(
												$('<a/>')
														.text(
																repojson.repositoryName)
														.attr(
																'href',
																repojson.repositoryContact));
							}

						} else {
							$('#repocontact').append(
									$('<a/>').text('SEAD').attr('href',
											'mailto:SEADdatanet@umich.edu'));
						}

					});

}

seadData.problem = function(xhr, textStatus, errorThrown) {
	$('.loading').hide();
	if (xhr.status == '404') {
		$('#Title')
				.append(
						"<div class='warning'><p>No Data Found for this identifier: "
								+ seadData.getId()
								+ "</p><p>Please use the Repository Contact link at the bottom if you believe this is an error.</p></div>");
	} else if (xhr.status == '500') {
		$('#Title')
				.append(
						"<div class='warning'><p>Server Error for this identifier: "
								+ seadData.getId()
								+ "</p><p>Please use the Repository Contact link at the bottom to report this problem.</p></div>");
	} else if (textStatus == "timeout") {

		$('#datatable')
				.html(
						"<div class='warning'><p>The server is busy temporarily and cannot retrieve the Content Listing for this publication."
								+ "</p><p>If refreshing this page does not solve the issue, please use the Repository Contact link at the bottom to report this problem.</p></div>");
	}

}

seadData.init();

seadData.getChildAjax = function(id) {

	return $.ajax({
		async : true,
		type : "GET",
		timeout : '150000',
		url : uriRoot + seadData.getId() + '/metadata/'
				+ encodeURIComponent(id),
		dataType : "json"
	});
}

seadData.loadChildren = function loadChildren(agg, parentid, parentPath) {
	var children = agg["Has Part"];
	if (Array.isArray(children)) {
		for (var i = 0; i < children.length; i++) {
			seadData.loadChild(agg, children[i], i, parentid, parentPath);

		}

	}

}

seadData.loadChild = function loadChild(agg, childId, id, parentid, parentpath) {
	for (var j = 0; j < agg.aggregates.length; j++) {
		if (agg.aggregates[j].Identifier == childId) {
			var child = agg.aggregates[j];
			if (seadData.isCollection(child)) {
				$('#datatable tbody').append(
						getCollectionRow(parentid, id, child.Title,
								child.Identifier, parentpath));
			} else {
				var fileSize = child.Size;
				// Clowder Build 113
				// kludge
				if (fileSize == null) {
					fileSize = child.size;
				}
				$('#datatable tbody').append(
						getDataRow(parentid, id, child.Title, parentpath
								+ '%2F' + child.Title, fileSize));
			}
		}
	}
}

seadData.isCollection = function isCollection(item) {
	if (item.hasOwnProperty('Has Part'))
		return true;
	var type = item['@type'];
	if (typeof type == 'string') {
		return ((type === "http://cet.ncsa.uiuc.edu/2007/Collection") || (type === 'http://cet.ncsa.uiuc.edu/2016/Folder'));
	} else {
		for (var i = 0; i < type.length; i++) {
			if ((type[i] === "http://cet.ncsa.uiuc.edu/2007/Collection")
					|| (type === 'http://cet.ncsa.uiuc.edu/2016/Folder'))
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

	// with Analytics tracking

	newRow.append($('<td/>').append(
			$('<span/>').addClass('file').append(
					$('<a/>').attr('href',
							'./api/researchobjects/' + id + '/data/' + uri)
							.attr('target', '_blank').attr(
									"onclick",
									"ga('send', 'event', '" + aggTitle + '::'
											+ id + "', 'File Download', '" + id
											+ "/data/" + uri + "');")
							.html(name))));

	newRow.append($('<td/>').html(filesize(parseInt(size), {
		base : 10
	})));
	return (newRow);
}

function getCollectionRow(parentId, childId, name, uri, parentpath) {
	var newRow = $('<tr/>');
	if (parentId != null) {
		childId = parentId + '-' + childId;
	}
	newRow.attr('data-tt-id', childId);
	if (parentId != null) {
		newRow.attr('data-tt-parent-id', parentId);
	}
	newRow.append($('<td/>').append(
			$('<span/>').addClass('folder').attr('iid', uri).attr('path',
					parentpath + '%2F' + name).text(name)));
	newRow.append($('<td/>').html('--'));
	// return newRow;
	return newRow.add($('<tr/>').attr('data-tt-id', childId + "-0").attr(
			'data-tt-parent-id', childId));
}

function activateTable() {
	var table = $('#datatable table');
	table
			.treetable({
				expandable : true,
				onNodeCollapse : function() {
					var node = this;
					table.treetable("unloadBranch", node);
				},
				onNodeExpand : function() {
					$('.loading').show();

					var node = this;
					var code = node.row[0].innerHTML;
					var test = code.substring(code.indexOf('iid') + 5);
					var tagID = test.substring(0, test.indexOf('"'));
					var pathString = code.substring(code.indexOf('path') + 6);
					var parentpath = pathString.substring(0, pathString
							.indexOf('"'));

					$
							.when(seadData.getChildAjax(tagID))
							.done(
									function(aggresource) {

										table.treetable("unloadBranch", node);
										// Analytics tracking
										ga('send', 'event', aggTitle + '::'
												+ seadData.getId(),
												'View Contents',
												aggresource.Title + '::'
														+ tagID);

										var rows = $();
										var children = aggresource["Has Part"];
										if (Array.isArray(children)) {
											for (var i = children.length - 1; i >= 0; i--) {
												var child = $
														.grep(
																aggresource.aggregates,
																function(e) {
																	return e['Identifier'] === children[i];
																});
												child = child[0];
												if (seadData
														.isCollection(child)) {
													var newrow = getCollectionRow(
															node.id, i,
															child.Title,
															child.Identifier,
															parentpath);
													rows = rows.add(newrow);
												} else {
													var fileSize = child.Size;
													// Clowder Build 113
													// kludge
													if (fileSize == null) {
														fileSize = child.size;
													}
													rows = rows
															.add(getDataRow(
																	node.id,
																	i,
																	child.Title,
																	parentpath
																			+ '%2F'
																			+ child.Title,
																	fileSize));
												}
												if (i % 100 == 0) {
													$(
															'#datatable table tr[data-tt-id="'
																	+ node.id
																	+ '"]')
															.after(rows);
													$('#datatable table')
															.treetable(
																	"loadBranch",
																	node, rows);
													rows = $();
												}
											}
										}
										$('.loading').hide();
									}).fail(
									function(xhr, textStatus, errorThrown) {
										seadData.problem(xhr, textStatus,
												errorThrown);
									});
				}

			});
}
