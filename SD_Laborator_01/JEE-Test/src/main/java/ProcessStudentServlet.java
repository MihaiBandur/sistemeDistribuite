import beans.StudentBean; // Nu uita pachetul!

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;

public class ProcessStudentServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Preluam datele primite din formularul HTML
        String nume = request.getParameter("nume");
        String prenume = request.getParameter("prenume");
        int varsta = Integer.parseInt(request.getParameter("varsta"));

        int anCurent = Year.now().getValue();
        int anNastere = anCurent - varsta;

        // 2. Impachetam aceste date intr-un obiect StudentBean
        StudentBean studentNou = new StudentBean();
        studentNou.setNume(nume);
        studentNou.setPrenume(prenume);
        studentNou.setVarsta(varsta);
        studentNou.setAnulNasterii(anNastere);

        // 3. Apelam clasa noastra de baza de date pentru a face Inserarea
        boolean succes = DatabaseManager.insertStudent(studentNou);

        // 4. Verificam rezultatul si raspundem utilizatorului
        if (succes) {
            // Totul a mers bine! Trimitem datele catre JSP pentru afisare
            request.setAttribute("nume", nume);
            request.setAttribute("prenume", prenume);
            request.setAttribute("varsta", varsta);
            request.setAttribute("anulNasterii", anNastere);
            request.getRequestDispatcher("./info-student.jsp").forward(request, response);

        } else {
            // A aparut o eroare la salvare (ex: baza de date oprita)
            response.sendError(500, "Ne pare rau, a aparut o eroare la salvarea in baza de date.");
        }
    }
}