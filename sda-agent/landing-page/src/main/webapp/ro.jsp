<%@ page import="java.util.*" %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" integrity="sha512-dTfge/zgoMYpP7QbHy4gWMEGsbsdZeCXz7irItjcC3sPUFtf0kuFbDz/ixG7ArTxmDjLXDmezHubeNikyKGVyQ==" crossorigin="anonymous">
    <title>SDA Research Object Information</title>
</head>
<body>

<!-- Bootstrap core JavaScript -->
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script src="bootstrap/js/bootstrap.js"></script>
<%--<script src="js/bootbox.min.js"></script>--%>

<%
	String sdaUrl = (String) request.getAttribute("landingPageUrl") + "/home.html";
    String roExists = (String) request.getAttribute("roExists");
    if ("false".equals(roExists)) {
%>

<div id="wrapper" align = "center">
    <div style="display : inline-block;padding: 15px 45px 15px 45px;" class="container">
        <div>
            <a href="https://www.indiana.edu/" target="_blank"><img align="left" src="img/iu-logo.jpg" style="width: 30%;height: 30%;"></a>
        </div>
        <div>
            <a href="http://sead-data.net/" target="_blank"><img align="right" src="img/sead-logo.png" style="width: 45%;height: 45%;"></a>
        </div>
	</div>
	<div>
        <h1 style="cursor: pointer;"><a style="color: #333; text-decoration: none;" href="<%= sdaUrl%>">IU SEAD Cloud</a></h1>
	</div>
    <div id="page-wrapper" align = "center">
        <div class="container float" align = "center">
            <div class="container float" align="center" style="background-color: #e0e0e0;margin-top: 50px;">
            	<h1>Page Not Found <small><font face="Tahoma" color="red">Error 404</font></small></h1>
            	<br>
            	<p>The page you requested could not be found</p>
            </div>
        </div>
    </div>
</div>

<%
    } else {
    Map<String, List<String>> properties = (Map<String, List<String>>) request.getAttribute("roProperties");
	Map<String, String> downloadList = (Map<String, String>) request.getAttribute("downloadList");
	Map<String, String> linkedHashMap = (Map<String, String>) request.getAttribute("linkedHashMap");
	String tag = (String) request.getAttribute("obTag");
%>

<div id="wrapper" align = "center">
    <div style="display : inline-block;padding: 15px 45px 15px 45px;" class="container">
        <div>
            <a href="https://www.indiana.edu/" target="_blank"><img align="left" src="img/iu-logo.jpg" style="width: 30%;height: 30%;"></a>
        </div>
        <div>
            <a href="http://sead-data.net/" target="_blank"><img align="right" src="img/sead-logo.png" style="width: 45%;height: 45%;"></a>
        </div>
	</div>
	<div>
        <h1 style="cursor: pointer;"><a style="color: #333; text-decoration: none;" href="<%= sdaUrl%>">IU SEAD Cloud</a></h1>
	</div>
    
    <div id="page-wrapper" align = "center">
    <% List<String> label = properties.get("Title"); %>
    	<%--<h2>Dataset : <%= label.get(0)%></h2>--%>
    	<h2>Research Object Landing Page</h2>
        <div class="container float" align = "center">

                <table class='table table-condensed' style="width: 70%" >
                <tr>
                    <td style="width: 20%"><b>Title</b></td>
                    <td><%= properties.get("Title") != null ? properties.get("Title").get(0) : ""%>
                        </br></td>
                </tr>
                    <%
                        for (String key : properties.keySet()) {
                            if(key.equals("Title")) {
                                continue;
                            }
                        	List<String> vals = properties.get(key);
                    %>
                    <tr>
                        <td style="width: 20%"><b><%= key%></b></td>                        	
                            <td> 
                            <%
                            int count=0;
                            for(String val :vals){
                                count++;
								
								if(key.equals("Full Metadata")) { %>
                                	<div style="float: left;width:62%;word-wrap: break-word;"><%
                                    if (val.startsWith("http")) {
                                %>
                                    <a href="<%= val%>"><%= val%></a>
                                <%
                                    } else {
                                %>
                                    <%= val%>
                                <%
                                    }
                                    if(count != vals.size()) {
                                    %>
                                    </br>
                                    <%
                                    }
                                %></a></div>
									<div style="float:right;width:36%;valign:top;font-size:11.5px;color:#757575;"><b><i>(Note: Full metadata is in JSON-LD format. To view it properly download a formatting plugin for your browser such as JSON-View)</b></i></div>
                                	<% continue;
                            	}
								%>

                                <%if(key.equals("Creator") || key.equals("Contact")) { %>
                                    <div style="float: left;width:62%;word-wrap: break-word;">
                                <%
                                    String[] creatorInfo = val.split("\\|");
                                    if (creatorInfo.length == 1) {
                                %>
                                    <%= val%>
                                <%
                                    } else if (creatorInfo.length == 2) {
                                %>
                                    <a href="<%= creatorInfo[1]%>"><%= creatorInfo[0]%></a>
                                <%
                                    } else if (creatorInfo.length == 3) {
                                %>
                                    <a href="<%= creatorInfo[1]%>" title="<%= creatorInfo[2]%>"><%= creatorInfo[0]%></a>

                                <%
                                    }

                                    if(count != vals.size()) {
                                    %>
                                    </br>
                                    <%
                                    }
                                %></div>
                                    <% continue;
                                }
                                %>
                                
                                <%
                                    if (val.startsWith("http")) {
                                %>
                                    <a href="<%= val%>"><%= val%></a>
                                <%
                                    } else {
                                %>
                                    <%= val%>
                                <%
                                    }
                                    if(count != vals.size()) {
                                    %>
                                    </br>
                                    <%
                                    }
                                %>                               
                                                       
                            <%}%>
	                        </td>
	              		<% } %>

                    </tr>
                </table>
              
            <td style="line-height:20px;" colspan=3>&nbsp;</td>

            
            <div style="width:450px;">

                <%
                    String roName = properties.get("Title").get(0);
                    if (request.getAttribute("bagExists") != null) {
                        String bagExists = (String) request.getAttribute("bagExists");
                        if ("true".equals(bagExists)) {
                            roName = tag;
                        }
                    }

                    boolean restricted = false;
                    if (request.getAttribute("accessRestricted") != null) {
                        String restr = (String) request.getAttribute("accessRestricted");
                        if ("true".equals(restr)) {
                            restricted = true;
                        }
                    }

                    if (restricted) {
                %>
                <p><span style="color: red;">This data set has restricted access. Please use the Contact information above
                    to learn more about the conditions for use of this data and to request access.</span></p>
                <% } else { %>

            <div style="float: left; width: 225px">
			 <form action="sda/<%= roName%>" method="get">
			    <button type="submit" class="btn btn-primary">Download Full Research Object</button>
			</form>
			</div>
			<div style="float: right; width: 225px"> 
			    <form method="get" action="sda/list=<%= tag%>">
				<button type="submit" class="btn btn-primary">Files in this Research Object</button>
			    </form>
			</div>

                <% } %>
			</div>

            <% if (!restricted) { %>
			<td style="line-height:20px;" colspan=3>&nbsp;</td>

            <table id = "list" class='table table-striped' style="width: 70%">
                    <%
                        for (String key : linkedHashMap.keySet()) {
                            String val = linkedHashMap.get(key);
                    %>
                    <tr>
                        
                       <% if (downloadList.get(key) == null){ %>
                      		<td style="width: 15%" > <Font color = "#2ECCFA"><b><%= val%></b></Font></td>
                        	<td style="width: 15%" ><%= "Folder"%></td>
                       		
                       <% }else{ %>
                        	<td style="width: 50%" ><b><%= val %></b></td>
                        	<td style="width: 15%"><%=downloadList.get(key)%></td>
                        	<td style="width: 5%">
	                    	<form method="get" action="sda/<%= tag+"/data/"+key %>">
							<button type="submit" class="btn btn-primary">Download</button>
							</form>
							</td>
                       <% } %>
                    </tr>
                    <%
                      }
                    %>
            </table>
            <% } %>

            <div style="width: 70%">
                <div style="float: left; font-size:11px">
                    <a href="<%= sdaUrl%>">IU SEAD Cloud</a>
                </div>
                <div style="float: right; font-size:11px">
                    <a href="http://sead-data.net/">SEAD</a>
                </div>
            </div>
        </div>
    </div>
</div>

<%}%>

</body>
</html>
