import beans.StudentBean;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SelectStudentServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException{
        String nume = request.getParameter("nume");
        String prenume = request.getParameter("prenume");

        StudentBean studentGasit = DatabaseManager.findStudent(nume, prenume);
        if(studentGasit != null) {

            request.setAttribute("nume", studentGasit.getNume());
            request.setAttribute("prenume", studentGasit.getPrenume());
            request.setAttribute("varsta", studentGasit.getVarsta());
            request.setAttribute("anulNasterii", studentGasit.getAnulNasterii());
            request.getRequestDispatcher("./info-student.jsp").forward(request, response);
        }else {
            request.getRequestDispatcher("./eroare_read.jsp").forward(request, response);
        }
    }

}
