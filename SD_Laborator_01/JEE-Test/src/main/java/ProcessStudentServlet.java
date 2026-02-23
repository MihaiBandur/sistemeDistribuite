import beans.StudentBean;

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

        String nume = request.getParameter("nume");
        String prenume = request.getParameter("prenume");
        int varsta = Integer.parseInt(request.getParameter("varsta"));

        int anCurent = Year.now().getValue();
        int anNastere = anCurent - varsta;

        StudentBean studentNou = new StudentBean();
        studentNou.setNume(nume);
        studentNou.setPrenume(prenume);
        studentNou.setVarsta(varsta);
        studentNou.setAnulNasterii(anNastere);

        boolean succes = DatabaseManager.insertStudent(studentNou);


        if (succes) {
            request.setAttribute("nume", nume);
            request.setAttribute("prenume", prenume);
            request.setAttribute("varsta", varsta);
            request.setAttribute("anulNasterii", anNastere);
            request.getRequestDispatcher("./info-student.jsp").forward(request, response);

        } else {
            response.sendError(500, "Ne pare rau, a aparut o eroare la salvarea in baza de date.");
        }
    }
}