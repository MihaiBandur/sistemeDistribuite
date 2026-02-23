<html xmlns:jsp="http://java.sun.com/JSP/Page">
<head>
<title>Update reusit</title>
</head>
<body>
    <h3>Varsta a fost actualizata cu succes!</h3>
        <p>Studentul <strong><%= request.getAttribute("nume") %> <%= request.getAttribute("prenume") %></strong> are acum varsta de <strong><%= request.getAttribute("nouaVarsta") %></strong> ani.</p>
        <br />
            <a href="index.jsp">Meniul principal</a>
        </body>
</html>