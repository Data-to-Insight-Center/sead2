<%@ page import="java.util.Map" %>
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
    Map<String, String> properties = (Map<String, String>) request.getAttribute("roProperties");
	Map<String, String> downloadList = (Map<String, String>) request.getAttribute("downloadList");
	String tag = (String) request.getAttribute("obTag");	
%>



<div id="wrapper" align = "center">
	<div style = "display : inline-block">
  		<img align = "left" src="http://sead-data.net/wp-content/uploads/2014/06/logo.png"  style="position: relative; float: left; width: 50%; height: 50%;"/>
  		<img align = "right"  src="http://ovpitnews.iu.edu/pub/libs/images/usr/13443.jpg"  style="position: relative; float: right; width: 19%; height: 19%"/>
	</div>
    
    <div id="page-wrapper" align = "center">
    	<h2>IU SDA LandingPage for SEAD</h2>
        <div class="container float" align = "center">

                <table class='table table-condensed' style="width: 70%" >
                    <%
                        for (String key : properties.keySet()) {
                        	 String val = properties.get(key);
                    %>
                    <tr>
                        <td style="width: 20%"><b><%= key%></b></td>
                       
	                        <%
	                            if (val.startsWith("http")) {
	                        %>
	                            <td><a href="<%= val%>"><%= val%></a></td>
	                        <%
	                            } else {
	                        %>
	                            <td><%= val%></td>
	                        <%
	                            }
	                        %>
	              		<% } %>
	           
                    </tr>
                </table>
              
            <td style="line-height:20px;" colspan=3>&nbsp;</td>

            
            <div style="width:450px;">
			<div style="float: left; width: 225px"> 
			 <form action="sda/<%= properties.get("Label")%>" method="get">
			    <button type="submit" class="btn btn-primary">Download Full Dataset</button>
			</form>
			</div>
			<div style="float: right; width: 225px"> 
			    <form method="get" action="sda/list=<%= tag%>">
				<button type="submit" class="btn btn-primary">Files in this dataset</button>
			    </form>
			</div>
			</div>
			
			
			<td style="line-height:20px;" colspan=3>&nbsp;</td>

            <table id = "list" class='table table-striped' style="width: 70%">
                    <%
                        for (String key : downloadList.keySet()) {
                            String val = downloadList.get(key);
                    %>
                    <tr>
                        <td style="width: 50%"><b><%= key%></b></td>
                        <td style="width: 15%"><%= val%></td>
                        <td style="width: 5%">
	                    <form method="get" action="sda/<%= key %>">
						<button type="submit" class="btn btn-primary">Download</button>
						</form>
        				</td>
                    </tr>
                    <%
                      }
                    %>
            </table>
        </div>
    </div>
</div>

</body>
</html>
