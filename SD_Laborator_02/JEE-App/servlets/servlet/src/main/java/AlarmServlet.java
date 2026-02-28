import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AlarmServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)  {
        try {
            response.setContentType("text/html");

            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>Pagina de Alarme</title></head><body>");
            html.append("<h2>Alerte Monitorizare Baza de Date</h2>");

            if (AlarmStore.alarme.isEmpty()) {
                html.append("<p style='color:green;'><b>Totul este în regula. Nicio valoare nu a depasit limitele!</b></p>");
            } else {
                html.append("<ul>");
                for (String alarma : AlarmStore.alarme) {
                    html.append("<li style='color:red;'><b>").append(alarma).append("</b></li>");
                }
                html.append("</ul>");
            }

            html.append("<br/><br/><a href='./'>Inapoi la meniul principal</a>");
            html.append("</body></html>");

            response.getWriter().print(html.toString());
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}