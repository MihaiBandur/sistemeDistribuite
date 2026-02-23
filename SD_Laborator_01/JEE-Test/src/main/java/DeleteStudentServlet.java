import beans.StudentBean;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;

public class DeleteStudentServlet extends  HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        String nume = request.getParameter("nume");
        String prenume = request.getParameter("prenume");

        boolean succes = DatabaseManager.deleteStudent(nume, prenume);
        if(succes) {
            request.setAttribute("nume", nume);
            request.setAttribute("prenume", prenume);
            request.getRequestDispatcher("./confirm_delete.jsp").forward(request, response);
        }
        else {
            request.getRequestDispatcher("./eroare_stergere.jsp").forward(request, response);
        }
    }
}
