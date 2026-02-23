import beans.StudentBean;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@WebListener
public  class DatabaseManager implements ServletContextListener{
    private static Connection global_conection;

    private static final String DB_URL = ;
    private static final String DB_USER =
    private static final String DB_PASSWORD = ;


    private static String sql_insert_query = "INSERT INTO studenti (nume, prenume, varsta, anul_nasterii) VALUES (?, ?, ?, ?)";
    private static String sql_update_query = "UPDATE studenti SET varsta = ? WHERE nume = ? AND prenume = ?";
    private static String sql_delete_query = "DELETE FROM studenti WHERE nume = ? AND prenume = ?";
    private static String sql_read_all = "SELECT * FROM studenti";
    private static String sql_read_one_student = "SELECT * FROM studenti WHERE nume = ? and prenume = ?";

    @Override
    public  void contextInitialized(ServletContextEvent sce){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            global_conection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        }catch (ClassNotFoundException e){
            e.printStackTrace();
            System.err.println("EROARE: Driver-ul MySQL nu a fost gasit in proiect!");
        }
        catch (SQLException e){
            e.printStackTrace();
            System.err.println("EROARE: Nu s-a putut conecta la baza de date!");
        }
    }

    @Override
    public  void contextDestroyed(ServletContextEvent sce){
        try{
            if(global_conection != null && !global_conection.isClosed()) {
                global_conection.close();
            }
        }catch (SQLException e){
            System.err.println("EROARE: Problema la inchiderea conexiunii cu baza de date.");
            e.printStackTrace();
        }
    }

    public static Connection getConection(){
        return global_conection;
    }
    public static  boolean insertStudent(StudentBean student){
        try(PreparedStatement ps = global_conection.prepareStatement(sql_insert_query)){
            ps.setString(1, student.getNume());
            ps.setString(2, student.getPrenume());
            ps.setInt(3, student.getVarsta());
            ps.setInt(4, student.getAnulNasterii());

            int rows_affected = ps.executeUpdate();

            return  rows_affected > 0;
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public  static  boolean updateStudent(String nume_student, String prenume_student, int noua_varsta){
        try(PreparedStatement ps = global_conection.prepareStatement(sql_update_query)) {

            ps.setInt(1, noua_varsta);
            ps.setString(2, nume_student);
            ps.setString(3, prenume_student);

            int rows_affected = ps.executeUpdate();

            return  rows_affected > 0;

        }catch (SQLException e){
            e.printStackTrace();
            return  false;
        }
    }

    public  static boolean deleteStudent(String numeStudent, String prenumeStudnet){
        try(PreparedStatement ps = global_conection.prepareStatement(sql_delete_query)) {
            ps.setString(1, numeStudent);
            ps.setString(2, prenumeStudnet);

            int rows_affected = ps.executeUpdate();
            return  rows_affected > 0;

        }catch (SQLException e){
            e.printStackTrace();
            return  false;
        }
    }

    public static List<StudentBean> readAllStudents() {
        List<StudentBean> listaStudenti = new ArrayList<>();
        try (PreparedStatement ps = global_conection.prepareStatement(sql_read_all);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StudentBean student = new StudentBean();
                student.setNume(rs.getString("nume"));
                student.setPrenume(rs.getString("prenume"));
                student.setVarsta(rs.getInt("varsta"));
                student.setAnulNasterii(rs.getInt("anul_nasterii"));

                listaStudenti.add(student);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return listaStudenti;
    }

    public  static  StudentBean findStudent(String nume, String prenume){
        StudentBean studentGăsit = null;
        try (PreparedStatement ps = global_conection.prepareStatement(sql_read_one_student)){

            ps.setString(1, nume);
            ps.setString(2, nume);

            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                studentGăsit = new StudentBean();
                studentGăsit.setNume(rs.getString("nume"));
                studentGăsit.setPrenume(rs.getString("prenume"));
                studentGăsit.setVarsta(rs.getInt("varsta"));
                studentGăsit.setAnulNasterii(rs.getInt("anul_nasterii"));
            }
        }catch (SQLException e){
            e.printStackTrace();

        }
        return  studentGăsit;

    }
}
