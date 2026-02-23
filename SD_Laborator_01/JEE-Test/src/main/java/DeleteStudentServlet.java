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
        int varsta = Integer.parseInt(request.getParameter("varsta"));

        /*
        procesarea datelor - calcularea anului nasterii
         */
        int anCurent = Year.now().getValue();
        int anNastere = anCurent - varsta;

        String folder_name = nume + "_" + prenume + "_" + Integer.toString(anNastere);


        Path studentPath = AppConfig.getStudentsPath().resolve(Paths.get(folder_name + "/student.xml"));
        Path folderPath = studentPath.getParent(); // Extragem calea folderului

        File file = studentPath.toFile();
        File folder = folderPath.toFile();


        if (!file.exists()) {
            response.sendError(404, "Nu a fost gasit niciun student serializat pe disc!");
            return;
        }

        try{
            if(file.exists()){
                Files.delete(studentPath);
            }

            Files.delete(folderPath);

            request.setAttribute("nume", nume);
            request.setAttribute("prenume", prenume);
            request.setAttribute("varsta", varsta);
            request.setAttribute("anulNasterii", anNastere);
            request.getRequestDispatcher("./confirm_delete.jsp").forward(request, response);


        }catch (IOException e){
            response.sendError(500,"\"A aparut o eroare la stergerea fisierului sau a folderului.\"" );
        }



    }
}
