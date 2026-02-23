<html xmlns:jsp="http://java.sun.com/JSP/Page">
	<head>
		<title>Informatii student</title>
		<meta charset="UTF-8" />
	</head>
	<body>
		<h3>Informatii student</h3>

		<!-- populare bean cu informatii din cererea HTTP -->
		<jsp:useBean id="studentBean" class="beans.StudentBean" scope="request" />
		<jsp:setProperty name="studentBean" property="nume" value='<%= request.getAttribute("nume") %>'/>
		<jsp:setProperty name="studentBean" property="prenume" value='<%= request.getAttribute("prenume") %>'/>
		<jsp:setProperty name="studentBean" property="varsta" value='<%= request.getAttribute("varsta") %>'/>
		<jsp:setProperty name="studentBean" property="anulNasterii" value='<%= request.getAttribute("anulNasterii") %>'/>

		<!-- folosirea bean-ului pentru afisarea informatiilor -->
		<p>Urmatoarele informatii au fost introduse:</p>
		<ul type="bullet">
			<li>Nume: <%=request.getAttribute("nume")%></li>
			<li>Prenume: <jsp:getProperty name="studentBean" property="prenume" /></li>
			<li>Varsta: <jsp:getProperty name="studentBean" property="varsta" /></li>
            <li>Anul Nasterii: <jsp:getProperty name = "studentBean" property="anulNasterii" /></li>
		</ul>

		<form action="./process-student" method="post">
        	Nume: <input type="text" name="nume" property="nume" value='<%= request.getAttribute("nume") %>' />
        	<br />
        	Prenume: <input type="text" name="prenume" property="prenume" value='<%= request.getAttribute("prenume") %>' />
        	<br />
        	Varsta: <input type="number" name="varsta" property="varsta" value='<%= request.getAttribute("varsta") %>' />
        	<br />
        	<br />
        	<button type="submit" name="submit">Trimite</button>
        	</form>
	</body>
</html>