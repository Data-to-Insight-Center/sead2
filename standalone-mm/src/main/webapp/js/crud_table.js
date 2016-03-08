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
            rule.RHS = row.find('td:eq(4)').text();

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
					}
					
					var lhs_id_val = "";
					var lhs_obj_val = "";
					var rhs_full_val = "";
					for (var s=0; s< lhs_ids.length; s++){						
						lhs_id_val += "&nbsp;" + lhs_ids[s] + "\r\n";
					}for (var l=0; l< lhs_objs.length; l++){						
						lhs_obj_val += "&nbsp;&nbsp;&nbsp;&nbsp;" + lhs_objs[l] + "\r\n";
					}
					var rhs_list = arr.rules[i].rhs[0].rhs_val.slice(1, -1).split("),");
					for(var k = 0; k < rhs_list.length-1; k++){
						rhs_full_val +=  rhs_list[k] + ");\r\n&nbsp;";
					}
					rules1.push( { "RuleName": arr.rules[i].name, "LHSID": lhs_id_val, "LHSOBJECT": lhs_obj_val,
					"RHS": rhs_full_val});
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
			var new_lhsid = savedData.LHSID;
			var new_lhsoject = savedData.LHSOBJECT;
			var new_rhs = savedData.RHS;

			var string = new_lhsid;
			var string_obj = new_lhsoject;
			var	new_string = string.split(/\b\s+(?!$)/);
			var	new_string_obj = string_obj.split(/\s\s/);
			var lhs_array = [];

			if (new_string.length <= new_string_obj.length){

				for(var i =0; i < new_string_obj.length; i++){
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

			var del_val = $(this).parent().parent()[0].textContent.split(/\b\s+(?!$)/)[2];
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
		});
	});