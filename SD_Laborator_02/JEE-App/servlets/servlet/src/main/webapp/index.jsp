<%@ page contentType="text/html; charset=UTF-8" %>
<html>
	<head>
		<title>Meniu principal</title>
		<meta charset="utf-8" />
	</head>
	<body>
		<h1>Meniu principal</h1>
		<hr />
		<h3>Gestiune cont bancar</h3>
		<form action="./process-bank-operation" method="post">
			<fieldset label="operatiuni">
				<legend>Alegeti operatiunea dorita:</legend>
				<select name="operation">
					<option value="deposit">Depunere numerar</option>
					<option value="withdraw">Retragere numerar</option>
					<option value="balance">Interogare sold</option>
				</select>
				<br />
				<br />
				Introduceti suma: <input type="number" name="amount" />
				<br />
				<br />
				<button type="submit">Efectuare</button>
			</fieldset>
		</form>
		<hr />
		<h3>Baza de date cu studenti</h3>
		<a href="./formular.jsp">Adaugare student</a>
		<br />
		<a href="./fetch-student-list">Afisare lista studenti</a>
	</body>
</html>