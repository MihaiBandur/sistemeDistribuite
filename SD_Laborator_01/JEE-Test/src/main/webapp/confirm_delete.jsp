<html xmlns:jsp="http://java.sun.com/JSP/Page">
	<head>
		<title>Stergere reusita</title>
		<meta charset="UTF-8" />
	</head>
	<body>

    		<jsp:useBean id="studentBean" class="beans.StudentBean" scope="request" />
    		<jsp:setProperty name="studentBean" property="nume" value='<%= request.getAttribute("nume") %>'/>
    		<jsp:setProperty name="studentBean" property="prenume" value='<%= request.getAttribute("prenume") %>'/>
    		<jsp:setProperty name="studentBean" property="varsta" value='<%= request.getAttribute("varsta") %>'/>
    		<jsp:setProperty name="studentBean" property="anulNasterii" value='<%= request.getAttribute("anulNasterii") %>'/>

    		<!-- folosirea bean-ului pentru afisarea informatiilor -->
    		<p>Studentul cu urmatoarele date a fost sters:</p>
    		<ul type="bullet">
    			<li>Nume: <%=request.getAttribute("nume")%></li>
    			<li>Prenume: <jsp:getProperty name="studentBean" property="prenume" /></li>
    			<li>Varsta: <jsp:getProperty name="studentBean" property="varsta" /></li>
                <li>Anul Nasterii: <jsp:getProperty name = "studentBean" property="anulNasterii" /></li>
	</body>
</html>