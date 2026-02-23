<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
       <title>Formular stergere student</title>
    </head>
    <body>
       <h3>Formular stergere student</h3>
       <p>Introduceti datele despre student:</p>

       <form action="./delete-student" method="post">
           <p>
              Nume: <input type="text" name="nume" value="Numele studentului" />
           </p>
           <p>
              Prenume: <input type="text" name="prenume" value="Prenumele studentului" />
           </p>
          <button type="submit" name="submit">Sterge</button>
       </form>
    </body>
</html>