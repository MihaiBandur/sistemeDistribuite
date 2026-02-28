import ejb.StudentEntity;

import javax.persistence.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class DeleteStudentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id_student_sters = Integer.parseInt(request.getParameter("id"));

            EntityManagerFactory factory = Persistence.createEntityManagerFactory("bazaDeDateSQLite");
            EntityManager em = factory.createEntityManager();

            EntityTransaction transaction = em.getTransaction();
            transaction.begin();

            Query query = em.createQuery("DELETE FROM StudentEntity s WHERE s.id = :id");
            query.setParameter("id", id_student_sters);
            int randuriSterse = query.executeUpdate();

            transaction.commit();

            em.close();
            factory.close();

            response.setContentType("text/html");
            if (randuriSterse == 0){
                response.getWriter().println("S-a produs o cerere studentul nu exista pe disk ca sa fie sters" +
                                "<br /><br /><a href='fetch-student-list'>Inapoi la lista</a>");
        }else{
                response.getWriter().println("Studentul a fost sters cu succes!" +
                        "<br /><br /><a href='fetch-student-list'>Inapoi la lista</a>");
        }
    }catch (IOException e){
            e.printStackTrace();
        }




    }
}
