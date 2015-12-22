SEAD 2.0 visualization v2.0

The folder consists of various files (specific details can be found in individual files):

-css folder: Includes 'networkViz.css' file which includes the styling for all the visualization files. The colors schemes can be manipulated from this file.
-data folder: Includes all the JSON data files used for the visualization. These files can be replaced with other similar JSON data files with similar structure.
-js folder:
        -jquery fodler: consists of required JQuery library files
        -d3.js: Javacript library file for D3
        -dataBuilder_V1.2.js: This file includes function call for manipulating the created node data for network visualization as per the required structure. Data received from JSON is converted as per network visualization structure.
        -networkViz_V1.2.js: This files includes function to create the network graph visualization
-NetworkVizInitialDummyData.html: HTML file that renders the network visualization using dummy data
-NetworkVizLatestStaticData.html: HTML file that renders the network visualization using data from JSON files
-NetworkVizWebServices.html: HTML file that renders the network visualization using data from the Web services URL 

These files need to be deployed via a server (like Apache Tomcat) to host it as a web project. The data files, functionalities and themes can be manipulated as long as the current structure is maintained.

