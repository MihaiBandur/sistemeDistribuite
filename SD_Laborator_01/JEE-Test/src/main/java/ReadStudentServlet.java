import beans.StudentBean;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadStudentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // deserializare student din fisierul XML de pe disc
        Path studentPath = AppConfig.getStudentsPath().resolve(Paths.get("Popescu Ion/student.xml"));
        File file = studentPath.toFile();

        if (!file.exists()) {
            response.sendError(404, "Nu a fost gasit niciun student serializat pe disc!");
            return;
        }

        XmlMapper xmlMapper = new XmlMapper();
        StudentBean bean = xmlMapper.readValue(file, StudentBean.class);

        request.setAttribute("nume", bean.getNume());
        request.setAttribute("prenume", bean.getPrenume());
        request.setAttribute("varsta", bean.getVarsta());
        request.setAttribute("anulNasterii", bean.getAnulNasterii());

        // redirectionare date catre pagina de afisare a informatiilor studentului
        request.getRequestDispatcher("./info-student.jsp").forward(request, response);
    }
}
