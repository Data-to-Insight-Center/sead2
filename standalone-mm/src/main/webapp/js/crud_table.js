        var rowNum = 1;
        var rowRemovedNum;
        var rowRemovedContents;
        var apiprefix = "./mm";

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
            rule.LHSID = row.find('.lhsId').val();
            rule.LHSOBJECT = row.find('.lhsObject').val();
			rule.LHSFULL = row.find('.lhsFull').val();
            rule.RHS = row.find('.rhs').val();

            return rule;
        }

        // Read a row in View Mode into a rule Object
        function GetViewRowObject(rowNum)
        {
            var row = $('#CRUDthisTable tbody tr').eq(rowNum);

            var rule = {};
            rule.RuleName = row.find('td:eq(1)').text();
            rule.LHSID = row.find('td:eq(2)').text();
            rule.LHSOBJECT = row.find('td:eq(3)').text();
			rule.LHSFULL = row.find('td:eq(4)').text();
            rule.RHS = row.find('td:eq(5)').text();

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
				
				//var rules1 = "[";
				//for(var i = 0; i < arr.rules.length; i++) { 
            	//var rules1 +=	"{ 'RuleName': " + arr.rules[i].name + ", 'LHSID': '1SS355TE-17', 'LHSOBJECT': '', 'RHS': ''}," +
				//"];";
				//}
				
				
				var rules1 = [];
				for(var i = 0; i < arr.rules.length; i++) {
					var lhs_ids = [];
					var lhs_objs = [];
					var lhs = [];
					for(var j = 0; j < arr.rules[i].lhs.length; j++){ 
						//rules1.push( { "RuleName": arr.rules[i].name, "LHSID": arr.rules[i].lhs[j].id, "LHSOBJECT": arr.rules[i].lhs[j].objType, "RHS": arr.rules[i].rhs[0].rhs_val});
						if (arr.rules[i].lhs[j].id !== undefined){
							lhs_ids.push(arr.rules[i].lhs[j].id);
						}if (arr.rules[i].lhs[j].objType !== undefined){
							lhs_objs.push( arr.rules[i].lhs[j].objType);
						}
				 		lhs.push(arr.rules[i].lhs[j].id + ":" + arr.rules[i].lhs[j].objType + "()");
					}
					
					var lhs_id_val = "";
					var lhs_obj_val = "";
					var lhs_full_val = "";
					var rhs_full_val = "";
					for (var s=0; s< lhs_ids.length; s++){						
						lhs_id_val += "&nbsp;" + lhs_ids[s] + "\r\n";
					}for (var l=0; l< lhs_objs.length; l++){						
						lhs_obj_val += "&nbsp;" + lhs_objs[l] + "\r\n";
					}for (var m=0; m< lhs.length; m++){						
						lhs_full_val += "&nbsp;" + lhs[m] + "\r\n";
					}
					var rhs_list = arr.rules[i].rhs[0].rhs_val.slice(1, -1).split("),");
					for(var k = 0; k < rhs_list.length-1; k++){
						rhs_full_val +=  rhs_list[k] + ");\r\n&nbsp;";
					}
					rules1.push( { "RuleName": arr.rules[i].name, "LHSID": lhs_id_val, "LHSOBJECT": lhs_obj_val,
					"LHSFULL": lhs_full_val, "RHS": rhs_full_val});
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
		 
			var data = GetViewRowObject(row);
		 
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
			var new_lhs = savedData.LHSID;
			var new_lhsid = savedData.LHSID;
			var new_lhsoject = savedData.LHSOBJECT;
			var new_rhs = savedData.RHS;
			
			var string = new_lhsid;
			var string_obj = new_lhsoject;
			var	new_string = string.split(/\b\s+(?!$)/);
			var	new_string_obj = string_obj.split(/\b\s+(?!$)/);
			var lhs_array = [];
			
			if (new_string.length === new_string_obj.length){
	
				for(var i =0; i < new_string.length; i++){
					lhs_array.push({
						"id" : new_string[i],
						"objType" : new_string_obj[i]
					});
				}
			
			}
			
			var J = arr;

			function addRule(lhs_array, name, rhs_val, rule_array) {
    			rule_array.push({
					"lhs": lhs_array,
        			"name": name,
					"rhs": [{"rhs_val":rhs_val}]
    			});
			}

			for(var b=0; b<1; b++) {
    			addRule(lhs_array, new_rulename, new_rhs, J.rules);
			}
			var new_arr = J;
			
			
			var xhr = new XMLHttpRequest();
			var url_post = apiprefix + "/rest/rules";
  			xhr.open("POST", url_post, true);
  			xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');

  			// send the collected data as JSON
  			xhr.send(new_arr);

  			xhr.onloadend = function () {


  			var new_rules = [];
            for(var i = 0; i < new_arr.rules.length; i++) {
                var new_lhs_ids = [];
                var new_lhs_objs = [];
                var new_lhs = [];
                for(var j = 0; j < new_arr.rules[i].lhs.length; j++){
                    //rules1.push( { "RuleName": arr.rules[i].name, "LHSID": arr.rules[i].lhs[j].id, "LHSOBJECT": arr.rules[i].lhs[j].objType, "RHS": arr.rules[i].rhs[0].rhs_val});
                    if (new_arr.rules[i].lhs[j].id !== undefined){
                        lhs_ids.push(new_arr.rules[i].lhs[j].id);
                    }if (new_arr.rules[i].lhs[j].objType !== undefined){
                        lhs_objs.push( new_arr.rules[i].lhs[j].objType);
                    }
                    lhs.push(new_arr.rules[i].lhs[j].id + ":" + new_arr.rules[i].lhs[j].objType + "()");
                }

                var new_lhs_id_val = "";
                var new_lhs_obj_val = "";
                var new_lhs_full_val = "";
                var new_rhs_full_val = "";
                for (var s=0; s< new_lhs_ids.length; s++){
                    new_lhs_id_val += "&nbsp;" + new_lhs_ids[s] + "\r\n";
                }for (var l=0; l< new_lhs_objs.length; l++){
                    new_lhs_obj_val += "&nbsp;" + new_lhs_objs[l] + "\r\n";
                }for (var m=0; m< new_lhs.length; m++){
                    new_lhs_full_val += "&nbsp;" + new_lhs[m] + "\r\n";
                }
                var new_rhs_list = new_arr.rules[i].rhs[0].rhs_val.slice(1, -1).split("),");
                for(var k = 0; k < new_rhs_list.length-1; k++){
                    new_rhs_full_val +=  new_rhs_list[k] + ");\r\n&nbsp;";
                }
                rules1.push( { "RuleName": new_arr.rules[i].name, "LHSID": new_lhs_id_val, "LHSOBJECT": new_lhs_obj_val,
                                "LHSFULL": new_lhs_full_val, "RHS": new_rhs_full_val});
            }

            $("#viewRowTemplate").tmpl(new_rules).appendTo("#CRUDthisTable");

            }
    		// Need to do post request here and pass new_arr variable to the java side.s

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
			$(this).parent().parent().remove();
		});
	});