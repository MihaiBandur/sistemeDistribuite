import beans.StudentBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class Jsonservlet extends  HttpServlet {
    @Override
    public  void  doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException{

        List<StudentBean> listaStudenti = DatabaseManager.readAllStudents();

        //response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        //response.setHeader("Content-Disposition", "attachment; filename=\"baza_de_date_studenti.json\"");

        ObjectMapper mapper = new ObjectMapper();

        mapper.writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), listaStudenti);
    }
}
