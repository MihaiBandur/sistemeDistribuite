<html xmlns:jsp="http://java.sun.com/JSP/Page">
	<head>
		<title>Formular stergere student</title>
		<meta charset="UTF-8" />
	</head>
	<body>
		<h3>Formular stergere student</h3>
		Introduceti datele despre student:
		    <form action="./delete-student" method="post">
		    <p>
			    Nume: <input type="text" name="nume", value = "nume student" />
			    <br />
			</p>
			<br />
			<br />
			<button type="submit" name="submit">Trimite</button>
		</form>
	</body>
</html>