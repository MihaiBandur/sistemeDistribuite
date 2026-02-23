<html xmlns:jsp="http://java.sun.com/JSP/Page">
	<head>
		<title>Formular selecteaza student</title>
		<meta charset="UTF-8" />
	</head>
	<body>
		<h3>Formular selecteaza student</h3>
		Introduceti datele despre student:
		<form action="./select-student" method="post">
			Nume: <input type="text" name="nume" />
			<br />
			Prenume: <input type="text" name="prenume" />
			<br />
			Varsta: <input type="number" name="varsta" />
			<br />
			<br />
			<button type="submit" name="submit">Trimite</button>
		</form>
	</body>
</html>