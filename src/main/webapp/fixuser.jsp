<%@ page import="com.nearfuturelaboratory.humans.dao.HumansUserDAO" %>
<%@ page import="com.nearfuturelaboratory.humans.entities.HumansUser" %>
<%--
  Created by IntelliJ IDEA.
  User: julian
  Date: 1/17/14
  Time: 22:17 PST
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    HumansUserDAO _dao = new HumansUserDAO("humans");

    HumansUser user = _dao.findOneByUsername(request.getParameter("username"));

    if(user != null) {
        user.setPassword(request.getParameter("password"));
        _dao.save(user);
    }


%>

<html>
<head>
    <title></title>
</head>
<body>
<%= user %>
</body>
</html>
