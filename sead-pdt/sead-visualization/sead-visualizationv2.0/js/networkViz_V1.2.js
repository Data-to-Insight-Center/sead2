/****************************
    File Name:networkViz JS
    Description:JS for Network Viz
    Creation Date:10/12/2015
    Version:1.2
****************************/


//(function($) {
var settings;	
var globalBool = false;
var formedNodes;
    /*
     * This function is used to create Network Graph
     * @data - data
     * @containerId - id of container div 
     */
	$.fn.createNetworkViz = function(options) {
		
		
		
	   settings = $.extend({		// setting the default option
		 	rosFilePath:"data/webServices/researchobjects.json",
	      	peopleFilePath:"data/webServices/people.json",
	      	nodeSize:{"research object":7,"people":7,"repository":10},
	      	colorArray : {"research object":"#B1D9FA","people":"#EDDC7F","repository":"F3BAB3"}, //colors for line
       }, options);
	   
	   settings.containerId = this[0].id;
     	
	   $.get(settings.rosFilePath, function(rosArrData, error){ // getting research objects JSON data

		   settings.rosArrData = rosArrData;
				   
		  // $.get(settings.repositoriesFilePath, function(repositoriesArrData,error) {
		
					   
							   d3.json(settings.peopleFilePath, function(error, peopleArrData) {// getting people data from json
				
								   settings.peopleArrData = peopleArrData;
								   createNetworkForceLayout();
								   
							   });
				   
				 //  });
		   
				 
	   
	   });
		
};

function createNetworkForceLayout(){

manipulateNodes(); // calling function to get required structure data
var width = $("#"+settings.containerId).width(); // get width of container
var height = $("#"+settings.containerId).height(); // get height of container
var svg = d3.select("#"+settings.containerId).append("svg") //appending svg to the container
			.attr("viewBox", "0 0 " + width + " " + height ); 

createLegends(svg,height,width); // calling the function to create legends
var color = d3.scale.category20();

var force = d3.layout.force()
		     .gravity(0.1) // define force layout
			 .charge(-100)
			 .linkDistance(40)
		     .size([width, height]);

var graph = {
		
		"nodes":settings.nodesArray, //assign nodes 
		"links":settings.linksArray //assign links
}

var link = svg.selectAll(".link") // link placeholder selections
			  .data(graph.links)
			  .enter().append("line")
			  .attr("class", "link")
			  .style("stroke-width",2);

var gnode = svg.selectAll(".gnode") // node placeholder selections
			 .data(graph.nodes) .enter().append("g")
		     .on("mouseenter", mouseover)
		     .on("mouseleave", mouseout)
  			 .call(force.drag); // calling force directed layout
  
force
  .nodes(graph.nodes)
  .links(graph.links)
  .start();
var node = gnode.append("circle") // append circles 
		      .attr("class", "node")
		      .attr("r",function(d){ return settings.nodeSize[d.group]; }) // set height as per input data
		      .style("fill", function(d) { return settings.colorArray[d.group]; });
  				
gnode.append("text").style("fill","#8f8f8f")
	  .attr("class",function(d){if(d.group == "research object"){return "researchObject"; }})//check the condition to see whether it is RO or not
	  .attr("dy", ".35em")
	  .text(function(d) { return d.name; }) // set names
	  .style("font-family","Helvetica");
  
node.append("title") // set title
     .text(function(d) { return d.name; });

/*
 * This function is called when setting x and y coordinates of links and nodes 
 * 
 */

force.on("tick", function() { // providing the x and y coordinates as per layout
	  
// transforming the node by providing x and y coordinates
gnode.attr("transform", function(d) { d.x = Math.max(20, Math.min(width - 20, d.x)); d.y = Math.max(20, Math.min(height - 20, d.y)); return "translate(" + d.x  + "," + d.y + ")"; });
link.attr("x1", function(d) { return d.source.x; })// setting coordinates for links
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; });
  });

  svg.append("g").attr("class","hoverTextG").append("text").attr("dy",".35em").attr("class","hoverTextNode");// create a group tag to show text for mouse hover
	
	
}
/*
 * This function is called when hovering mouse over nodes or text  
 * To increase the size of current node
 * To highlight the text of current node
 */
function mouseover(d) {
	
var currentEle = d3.select(this).select("circle");
var textNodehover = d3.select(".hoverTextG");
textNodehover.style("display","block");
var currentText = d3.select(this).select("text");
var preRadius = currentEle.attr("r");
currentEle.attr("preRadius",preRadius).transition()
	      .duration(250)
	      .attr("r", 16);
currentText.style("display","none");
textNodehover.attr("transform",d3.select(this).attr("transform"));
textNodehover.select("text").attr("dy","-.5em").text(currentText.text());

}
/*
 * This function is called during mouse-out to bring node at normal size 
 * 
 */
function mouseout(d) {
var textNodehover = d3.select(".hoverTextG");
textNodehover.style("display","none");
var currentEle = d3.select(this).select("circle");
var currentText = d3.select(this).select("text");
var preRadius = settings.nodeSize[d.group];
currentEle.transition()
  .duration(500)
  .attr("r", preRadius);
if(d.group == "research object"){ // checking the condition whether it is RO or not
	currentText.style("display","none");
}else{
	currentText.style("display","block");
}
	
}
/*
 * This function is called to create legends as per input data
 * @svg - svg element
 * @height - height of main container
 * @width - width of main container
 */

function createLegends(svg,height,width){
var colorDomain = [];// initialize th color domain array
var colorRange = []; // initialize th color range array
var colorObj = settings.colorArray;
for (var property in colorObj) {
    if (colorObj.hasOwnProperty(property)) {
    	colorDomain.push(property); // set the values from input data
    	colorRange.push(colorObj[property]);
    	// do stuff
    }
}

var colorScale = d3.scale.ordinal()
    .domain(colorDomain)
    .range(colorRange);

var legendRectSize = 12;
var legendSpacing = 12;

var legend = svg.append("g").attr("transform","translate("+ (width-200) +","+ (height-200) +")").selectAll('.legend')
				.data(colorScale.domain())
				.enter()
				.append('g')
				.attr('class', 'legend')
				.attr('transform', function(d, i) {
				  var height = legendRectSize + legendSpacing;
				  var offset =  height * colorScale.domain().length / 2;
				  var horz = -2 * legendRectSize;
				  var vert = i * height - offset;
				  return 'translate(' + horz + ',' + vert + ')';
				});
legend.append('rect')
		.attr('width', legendRectSize)
		.attr('height', legendRectSize)
		.style('fill', colorScale)
		.style('stroke', colorScale);
		legend.append('text')
		.attr('x', legendRectSize + legendSpacing)
		.attr('y', legendRectSize-2)
		.text(function(d) { return d; });
}

