<html xmlns:jsp="http://java.sun.com/JSP/Page">
	<head>
		<title>Actualizare student</title>
		<meta charset="UTF-8" />
	</head>
	<body>
		<h3>Formular actualizare student</h3>
		Introduceti noile datele despre student:
		<form action="./update-student" method="post">
		    <input type="hidden" name="id" value="<%= request.getParameter("id") %>" />
		    <br />
			Nume: <input type="text" name="nume" value= "<%= request.getParameter("nume")%>" />
			<br />
			Prenume: <input type="text" name="prenume" value= "<%= request.getParameter("prenume")%>" />
			<br />
			Varsta: <input type="number" name="varsta" value= "<%= request.getParameter("varsta")%>" />
			<br />
			<br />
			<button type="submit" name="submit">Trimite</button>
		</form>
		<br / >
		<a href = "./fetch-student-list">Alege alt student</a>
		<br / >
		<a href = "./">Alege alt student</a>
	</body>
</html>