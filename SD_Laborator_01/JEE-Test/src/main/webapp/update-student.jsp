<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
       <title>Formular update student</title>
    </head>
    <body>
       <h3>Formular update student</h3>
       <p>Introduceti datele despre student:</p>

       <form action="./update-student" method="post">
           <p>
                Nume: <input type="text" name="nume" value="Numele studentului" />
           </p>
           <p>
                Prenume: <input type="text" name="prenume" value="Prenumele studentului" />
           </p>

           <p>
                Varsta: <input type="number" name="varsta" value="Noua varsta" />
           </p>
           <br />
           <br />
          <button type="submit" name="submit">Update</button>
       </form>
    </body>
</html>