<html xmlns:jsp="http://java.sun.com/JSP/Page">
	<head>
		<title>Formular student</title>
		<meta charset="UTF-8" />
	</head>
	<body>
		<h3>Formular student</h3>
		    Introduceti datele despre student:
		    <form action="./process-student" method="post">
		    <p>
			    Nume: <input type="text" name="nume" />
			    <br />
		    </p>
		    <p>
			    Prenume: <input type="text" name="prenume" />
			    <br />
			</p>
			<p>
			    Varsta: <input type="number" name="varsta" />
			</p>
			<br />
			<br />
			<button type="submit" name="submit">Trimite</button>
		</form>
	</body>
</html>