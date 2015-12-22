/*
 *This function is call for manipulating created node data for Network Viz as per the required structure
 * Data received from JSON is converted as per network viz structure
 * 
 */


function manipulateNodes(){
	
var tempRepoName ;
var repositoryArr = [];
var peopleArr = []; // array to store people
var peopleCreatorIds = []; //  array to store people IDs
settings.rosArrData.forEach(function(ro,index){
	
	if($.inArray(ro.Repository,repositoryArr) == -1){
		repositoryArr.push(ro.Repository);
		
	}
	if(ro.Aggregation.Creator != undefined){
		if($.inArray(ro.Aggregation.Creator,peopleCreatorIds) == -1){
		peopleCreatorIds.push(ro.Aggregation.Creator);
		var person = settings.peopleArrData.persons.filter(function(obj){
						if(obj["@id"] == ro.Aggregation.Creator){
							return obj;
						}
						
					});
		ro.Aggregation.givenName = person[0].givenName;
		peopleArr.push(person[0].givenName);
		
		}else{
			var person = settings.peopleArrData.persons.filter(function(obj){
				if(obj["@id"] == ro.Aggregation.Creator){
					return obj;
				}
				
			});
		ro.Aggregation.givenName = person[0].givenName;
			
		}
		
	}
	
});

var nodesArray = []; // array to store nodes
var roNodesArr = [];// array to store ROs
var indexNodesObj = {}; // array to store array index number
var tempNode = {};
var returnIndex;
settings.rosArrData.forEach(function(ro,index){
	
	tempNode.name = ro.Aggregation.Title;
	tempNode.group = "research object";
	tempNode.target = ro.Repository; 
	if(ro.Aggregation.Creator != undefined){
		tempNode.personTarget = ro.Aggregation.givenName; 
		
	}
	
	nodesArray.push(tempNode);
	roNodesArr.push(tempNode);
	tempNode = {};
	
});

repositoryArr.forEach(function(repository,index){
	
	tempNode.name = repository;
	tempNode.group = "repository";
	returnIndex = nodesArray.push(tempNode) - 1;
	tempNode = {};
	indexNodesObj[repository] = returnIndex;
});

peopleArr.forEach(function(person,index){
	
	tempNode.name = person;
	tempNode.group = "people";
	returnIndex = nodesArray.push(tempNode) - 1;
	tempNode = {};
	indexNodesObj[person] = returnIndex;
});

var linksArray = [];
var tempLink = {};
roNodesArr.forEach(function(node,index){
	
	tempLink.source = index;// setting source target values
	tempLink.target = indexNodesObj[node.target];
	linksArray.push(tempLink);
	tempLink = {};
	if(node.personTarget != undefined){
		
		tempLink.source = index;
		tempLink.target = indexNodesObj[node.personTarget];
		linksArray.push(tempLink);
		tempLink = {};
	}
	
});

settings.nodesArray = nodesArray;
settings.linksArray = linksArray;

}