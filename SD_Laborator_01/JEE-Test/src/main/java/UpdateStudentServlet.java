import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UpdateStudentServlet extends   HttpServlet {
    @Override
    public  void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException{

        String nume = request.getParameter("nume");
        String prenume = request.getParameter("prenume");
        int noua_varsta = Integer.parseInt(request.getParameter("varsta"));

        boolean succes = DatabaseManager.updateStudent(nume, prenume, noua_varsta);
        if(succes){
            request.setAttribute("nume", nume);
            request.setAttribute("prenume", prenume);
            request.setAttribute("nouaVarsta", noua_varsta);
            request.getRequestDispatcher("./confirm-update.jsp").forward(request,response);
        }else {
            request.getRequestDispatcher("./eroare_update.jsp").forward(request,response);
        }
    }
}
