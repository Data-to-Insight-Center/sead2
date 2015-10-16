<%@ page import="java.util.Map" %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <link href="bootstrap/css/bootstrap.css" rel="stylesheet" media="screen">
    <title>SDA Research Object Information</title>
</head>
<body>

<!-- Bootstrap core JavaScript -->
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script src="bootstrap/js/bootstrap.js"></script>
<%--<script src="js/bootbox.min.js"></script>--%>

<%
    Map<String, String> properties = (Map<String, String>) request.getAttribute("roProperties");
%>

<div id="wrapper">
    <h2 align="center">IU SDA Landing Page for SEAD</h2>
    <div id="page-wrapper">
        <div class="container">
            <h3>Research Object Metadata</h3>

            <form role='form' action="sda/<%= properties.get("Title")%>" method="get">
                <table class='table' style="width: 90%">
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
                    </tr>
                    <%
                        }
                    %>
                </table>
                <button type="submit" class="btn btn-primary">Download RO</button>
            </form>
        </div>
    </div>
</div>

</body>
</html>
