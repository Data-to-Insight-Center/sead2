        var rowNum = 1;
        var rowRemovedNum;
        var rowRemovedContents;
        var apiprefix = "./mm";

		$('tr.editRow').addClass('even');

		$.ajaxSetup({
			contentType: "application/json; charset=utf-8",
			dataType: "json"
		});

		$(document).ready(function () {
			$('#match').click(function () {
				var send = document.getElementById('inputText').value;
				//alert(send);
				$.ajax({
					url: apiprefix + "/rest",

					type: "POST",
					data: send,
					success: function (response, status, jqXHR) {
						document.getElementById("match_response").innerHTML = JSON.stringify(response,null,2);
						document.getElementById("success").innerHTML = '<div id="success_output">Hooray!!! Successfully Matched.</div>';
					},

					error: function (jqXHR, status) {
						document.getElementById("error").innerHTML = '<div id="error_output">Ooops!!! Invalid Json Code.</div>';
						//alert(JSON.stringify(jqXHR));
					}
				});
				return false;
			});
		});
        // Post all rows to the server and put into Cache
        function PostTable()
        {
            var ruleId = 1;
            var jsonRequest = { rules: GetAllViewRowsAsRuleObjects(), ruleId: ruleId };

            $.ajax({
                type: 'POST',
                url: apiprefix + "/rest/rules",
                data: JSON.stringify(jsonRequest),
                contentType: 'application/json; charset=utf-8',
                dataType: 'json',
                success: function (data, text)
                {
                    return true;
                },
                error: function (request, status, error)
                {
                    return false;
                }
            });
        }
        // Read a row in Edit Mode into a rule Object
        function GetEditRowObject()
        {
            var row = $('#CRUDthisTable tbody tr.editRow');

            var rule = {};
            rule.RuleName = row.find('.ruleName').val();
			rule.LHSFULL = row.find('.lhsFull').val();
            rule.RHS = row.find('.rhs').val();
            rule.DESC = row.find('.desc').val();

            return rule;
        }

        // Read a row in View Mode into a rule Object
        function GetViewRowObject(rowNum)
        {
            var row = $('#CRUDthisTable tbody tr').eq(rowNum);

            var rule = {};
            rule.RuleName = row.find('td:eq(1)').text();
			rule.LHSFULL = row.find('td:eq(2)').text();
            rule.RHS = row.find('td:eq(3)').text();
            rule.DESC = row.find('td:eq(4)').text();

            return rule;
        }

        // Read all rows into rule Object Array
        function GetAllViewRowsAsRuleObjects()
        {
            var ruleTableRows = [];

            $('#CRUDthisTable tbody tr').each(function (index, value)
            {
                var row = GetViewRowObject(index);
                ruleTableRows.push(row);
            });

            return ruleTableRows;
        }

        // Check if any rows are in Edit Mode
        function IsExistingRowInEditMode()
        {
            var rowsInEditMode = $('#CRUDthisTable tbody tr.editRow').length;

            if (rowsInEditMode > 0) {
                alert('You have a row in Edit mode, please save or cancel the row changes before you continue.');
                return true;
            }

            return false;
        }

        $(document).ready(function ()
       		{
			var xmlhttp = new XMLHttpRequest();
			var url = apiprefix + "/rest/rules";

			xmlhttp.onreadystatechange=function() {
    			if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
        			myFunction(xmlhttp.responseText);
    			}
			}
				xmlhttp.open("GET", url, true);
				xmlhttp.send();

			function myFunction(response) {
				arr = JSON.parse(response);
				var rules1 = [];

				for(var i = 0; i < arr.rules.length; i++) {
					var lhs_ids = [];
					var lhs_objs = [];
					var lhs = [];
					for(var j = 0; j < arr.rules[i].lhs.length; j++){
						if (arr.rules[i].lhs[j].id !== undefined){
							lhs_ids.push(arr.rules[i].lhs[j].id);
						}if (arr.rules[i].lhs[j].objType !== undefined){
							lhs_objs.push( arr.rules[i].lhs[j].objType);
						}
					}

					var lhs_id_val = "";
					var lhs_obj_val = "";
					var lhs_full_val = "";
					var rhs_full_val = "";
					for (var s=0; s< lhs_ids.length; s++){
						lhs_id_val += "&nbsp;" + lhs_ids[s] + "\r\n";
					}for (var l=0; l< lhs_objs.length; l++){
						lhs_obj_val += "&nbsp;" + lhs_objs[l] + "\r\n";
					}for (var m=0; m< arr.rules[i].lhs.length; m++){

						var str = arr.rules[i].lhs[m].objType;
						var	reg = /[*|-|+|=|!|<|>]|null| or /ig;

						//fixing a bit
						var toStr = String(reg);
						var color = (toStr.replace('\/g', '|')).substring(1);

						//split it baby
						var colors = color.split("|");

						if (colors.indexOf("+") > -1) {
							str = str.replace(/[+]/g, '<span style="color:blue;">+</span>');
						}if (colors.indexOf("-") > -1) {
							str = str.replace(/[-]/g, '<span style="color:blue;">-</span>');
						}if (colors.indexOf("=") > -1) {
							str = str.replace(/[=]/g, '<span style="color:blue;">=</span>');
						}if (colors.indexOf("!") > -1) {
							str = str.replace(/[!]/g, '<span style="color:blue;">!</span>');
						}if (colors.indexOf("<") > -1) {
							str = str.replace(/[!]/g, '<span style="color:blue;"><</span>');
						}if (colors.indexOf(">") > -1) {
							str = str.replace(/[!]/g, '<span style="color:blue;">></span>');
						}if (colors.indexOf("null") > -1) {
							str = str.replace(/null/g, '<span style="color:red;">null</span>');
						}if (colors.indexOf(" or ") > -1) {
							str = str.replace(/ or /g, '<span style="color:red;"> or </span>');
						}

						if(arr.rules[i].lhs[m].id == "" || arr.rules[i].lhs[m].id == undefined){
							lhs_full_val += "<span style='color:#A0522D !important;'>" + str + "</span>&nbsp;\r\n".replace("\n", "<br /><br />");
						}else{
							lhs_full_val += "<span style='color:#FF8C00 !important;'>" + arr.rules[i].lhs[m].id + " </span>: " + "<span style='color:#A0522D !important;'>" + str + "</span>&nbsp\r\n".replace("\n", "<br /><br />");
						}
					}
					var rhs_list = arr.rules[i].rhs[0].rhs_val.slice(1, -1).split(/;,|;/);
					for(var k = 0; k < rhs_list.length-1; k++){

						var rhs_str = rhs_list[k].trim();
						var	rhs_reg = /[*|+|<]|null| or |"(.*?)"/ig;

						var rhs_match = rhs_str.match(/"(.*?)"/);

						var rhstoStr = String(rhs_reg);
						var rhs_color = (rhstoStr.replace('\/g', '|')).substring(1);


						//var str = 'System.out.println("\n---Total size is not acceptable for " +repo.getRepositoryName() + "---\n");';
						var singleQuoted = $.map(rhs_str.split('"'), function(substr, i) {
						   return (i % 2) ? substr : null;
						});
						var dfdsf ="";
						for (var col=0; col<singleQuoted.length; col++){
							dfdsf = singleQuoted[col];

						}
						alert(dfdsf);

						//split it content
						var rhs_colors = rhs_color.split("|");
						if (rhs_colors.indexOf("null") > -1) {
							rhs_str = rhs_str.replace(/null/g, '<span style="color:red;">null</span>');
						}if (rhs_colors.indexOf(" or ") > -1) {
							rhs_str = rhs_str.replace(/ or /g, '<span style="color:red;"> or </span>');
						}if (rhs_colors.indexOf("+") > -1) {
							rhs_str = rhs_str.replace(/[+]/g, '<span style="color:blue;">+</span>');
						}if (rhs_colors.indexOf('"(.*?)"') > -1) {
							for (var col=0; col<singleQuoted.length; col++){
								var dfdsf = singleQuoted[col];
								rhs_str = rhs_str.replace('"' + dfdsf + '"', '<span style="color:#24AD24 !important;">' + '"' + dfdsf + '"' + '</span>');
							}
						}
						rhs_full_val +=  rhs_str + ";\r\n".replace("\n", "<br /><br />");
					}
					rules1.push( { "RuleName": arr.rules[i].name, "LHSID": lhs_id_val, "LHSOBJECT": lhs_obj_val,
					"LHSFULL": lhs_full_val,
					"RHS": rhs_full_val, "DESC": arr.rules[i].desc });
				}

			$("#viewRowTemplate").tmpl(rules1).appendTo("#CRUDthisTable");

			}

		// Events
		$('.AddRow').click(function()
		{
			if (IsExistingRowInEditMode()){
				return;}

			rowRemovedNum = 0;

			var data = { data: 1 };
			var output = $("#editRowTemplate").tmpl(data).html();

			$('#CRUDthisTable tbody').prepend('<tr class="editRow">' + output + '</tr>');
			//var $rowEdit = $('#CRUDthisTable tbody tr.editRow');

			$('#CRUDthisTable tbody tr:first')[0].scrollIntoView();
		});

		$('.EditRow').live('click', function(e)
		{
			if (IsExistingRowInEditMode()){
				return;  }

			var row = $(this).parent().parent().parent().children().index($(this).parent().parent());

			data = GetViewRowObject(row);

			var output = $("#editRowTemplate").tmpl(data).html();

			rowRemovedNum = row;
			rowRemovedContents = $('#CRUDthisTable tbody tr').eq(row).html();

			$('#CRUDthisTable tbody tr').eq(row).after('<tr class="editRow">' + output + '</tr>');
		 	//var applyDateDefault = $('#ApplyDateDefault').val();

			//var $editRow = $('#CRUDthisTable tbody tr.editRow');

			$('#CRUDthisTable tbody tr').eq(row).remove();
		});

		$('.SaveRow').live('click', function(e)
		{
			// Good place to add validation, don't allow save until the row has valid data!
			// var isValid = ValidateNestedControls("#CRUDthisTable");
			// if (!isValid)
			//     return;
			var savedData = GetEditRowObject();

			var row = $(this).parent().parent().parent().children().index($(this).parent().parent());

			var output = $("#viewRowTemplate").tmpl(savedData).html();

			var tableRows = $('#CRUDthisTable tbody tr').length;

			if (tableRows === 0 || row === 0) {
				$('#CRUDthisTable tbody').prepend('<tr style="height:150px;">' + output + '</tr>');
			}
			else {
				$('#CRUDthisTable tbody tr').eq(row - 1).after('<tr style="height:150px;">' + output + '</tr>');
			}


			$('#CRUDthisTable tbody tr').eq(row + 1).remove();

			var new_rulename = savedData.RuleName;
			var new_lhs_full = savedData.LHSFULL;
			var new_rhs = savedData.RHS;
			var new_desc = savedData.DESC;

			var new_string_rhs = new_rhs.split("\n");
			var new_string_lhs_full = new_lhs_full.split("\n");
			var lhs_array = [];

            for(var i =0; i < new_string_lhs_full.length; i++){
				var lhs_full_color = "<span style='color:#A0522D !important;'>" + new_string_lhs_full[i].trim() + "</span>";
                lhs_array.push({
                    "lhsFull" : lhs_full_color
                });
            }

			var new_rhs_val = "";
			for(var r =0; r < new_string_rhs.length; r++){
					new_rhs_val += new_string_rhs[r].trim();
				}

			var J = arr;

			function addRule(lhs_array, name, rhs_val, desc, rule_array) {
    			rule_array.push({
					"lhs": lhs_array,
        			"name": name,
					"rhs": [{"rhs_val":rhs_val}],
					"desc": desc
    			});
			}

			for(var b=0; b<1; b++) {
    			addRule(lhs_array, new_rulename, new_rhs_val, new_desc, J.rules);
			}
			var new_arr = J;


			if (rowRemovedNum !== 0){
				alert("Edit Mode");
				var edit_del_val = data.RuleName;
				var edit_rule_name_array = {"rules":[{"name" : edit_del_val}]};
				var edit_del_new_val=JSON.stringify(edit_rule_name_array);
				var edit_del_request = $.ajax({
								url: apiprefix + "/rest/rules/" + edit_del_val,
								type: "delete",
								data: edit_del_new_val,
								cache: false,
								dataType: 'text',
								contentType : 'application/json'
								});

				edit_del_request.done(function (response){
							console.log("Response from server: " + response);
							alert(response);
						});

			}else{

			var myData=JSON.stringify(new_arr);
			var request = $.ajax({
							url: apiprefix + "/rest/rules",
							type: "post",
							data: myData,
							cache: false,
							dataType: 'text',
							contentType : 'application/json'
							});

			request.done(function (response){
						console.log("Response from server: " + response);
						alert(response);

			});
			}
			if(rowRemovedNum !== 0){
				var edit_data=JSON.stringify(new_arr);
                var edit_request = $.ajax({
                                url: apiprefix + "/rest/rules",
                                type: "post",
                                data: edit_data,
                                cache: false,
                                dataType: 'text',
                                contentType : 'application/json'
                                });

                edit_request.done(function (response){
                            console.log("Response from server: " + response);
                            alert(response);

			});
			}
		location.reload(true);
		});


		$('.CancelRow').live('click', function(e)
		{
			var row = $(this).parent().parent().parent().children().index($(this).parent().parent());

			$('#CRUDthisTable tbody tr').eq(row).remove();

			var tableRows = $('#CRUDthisTable tbody tr').length;

			if (rowRemovedContents) {
				if (tableRows === 0 || row === 0) {
					$('#CRUDthisTable tbody').prepend('<tr style="height:150px;">' + rowRemovedContents + '</tr>');
				}
				else {
					$('#CRUDthisTable tbody tr').eq(row - 1).after('<tr style="height:150px;">' + rowRemovedContents + '</tr>');
				}
			}

			rowRemovedContents = null;
		});

		$('.DeleteRow').live('click', function(e)
		{
			e.preventDefault();

			var del_val = $(this).parent().parent()[0].textContent.split(/\s\s\s/)[14].trim();
			var rule_name_array = {"rules":[{"name" : del_val}]};
			var del_new_val=JSON.stringify(rule_name_array);
            var request = $.ajax({
                            url: apiprefix + "/rest/rules/" + del_val,
                            type: "delete",
                            data: del_new_val,
                            cache: false,
                            dataType: 'text',
                            contentType : 'application/json'
                            });

            request.done(function (response){
                        console.log("Response from server: " + response);
                        alert(response);
                    });

            $(this).parent().parent().remove();

			location.reload(true);
		});

});