<%@ page import="java.util.Map" %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>SDA Research Object Information</title>
</head>
<body>

<%
    Map<String, String> properties = (Map<String, String>) request.getAttribute("roProperties");
%>

<div id="wrapper">
    <div id="page-wrapper">
        <div class="container">
            <h3>Research Object Metadata</h3>

            <form role='form' action="sda" method="get">
                <table class='table' style="width: 70%">
                    <%
                        for (String key : properties.keySet()) {
                    %>
                    <tr>
                        <td><%= key%></td>
                        <td><%= properties.get(key)%></td>
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
