<html xmlns:jsp="http://java.sun.com/JSP/Page">
<html>
<head>
    <title>Studentul a fost modificat</title>
</head>
<body>
    <h3>Studentul <%= request.getAttribute("nume") %> <%= request.getAttribute("prenume") %> a fost modificat cu succes!</h3>

    <br>
    <a href="index.jsp">Inapoi la meniul principal</a>
</body>
</html>