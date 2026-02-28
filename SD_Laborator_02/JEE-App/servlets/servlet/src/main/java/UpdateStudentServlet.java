import javax.persistence.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UpdateStudentServlet extends  HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request,HttpServletResponse response ) {
        try {


            int idStudent = Integer.parseInt(request.getParameter("id"));
            String numeNou = request.getParameter("nume");
            String prenumeNou = request.getParameter("prenume");
            int varstaNoua = Integer.parseInt(request.getParameter("varsta"));

            EntityManagerFactory factory = Persistence.createEntityManagerFactory("bazaDeDateSQLite");
            EntityManager em = factory.createEntityManager();


            EntityTransaction transaction = em.getTransaction();
            transaction.begin();

            Query query = em.createQuery("UPDATE StudentEntity s SET s.nume = :nume, s.prenume = :prenume, s.varsta = :varsta WHERE s.id = :id");
            query.setParameter("nume", numeNou);
            query.setParameter("prenume", prenumeNou);
            query.setParameter("varsta", varstaNoua);
            query.setParameter("id", idStudent);

            int randuriAfectate = query.executeUpdate();

            transaction.commit();
            em.close();
            factory.close();
            response.setContentType("text/html");
            if (randuriAfectate == 0) {
                response.getWriter().println("Nu s-a modificat nimic in baza de data studentul respectiv nu exista" +
                        "<br /> <a href = 'fetch-student-list'>Modifica din nou studentul</a>");
            } else {
                response.getWriter().println("S-a modificat studentul cerut in baza de date" +
                        "<br /> <a href = 'fetch-student-list'>Vizualizeaza lista cu studenti</a>"+
                        "<br /> <a href = './'>Meniul principal</a>");
            }
        }catch (IOException e){
            e.printStackTrace();
        }


    }
}
