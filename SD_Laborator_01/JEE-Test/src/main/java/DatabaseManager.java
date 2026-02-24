import beans.StudentBean;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {

    private static Connection global_conection;

    private static final String DB_URL =
    private static final String DB_USER =
    private static final String DB_PASSWORD =

    private static String sql_insert_query = "INSERT INTO studenti (nume, prenume, varsta, anul_nasterii) VALUES (?, ?, ?, ?)";
    private static String sql_update_query = "UPDATE studenti SET varsta = ? WHERE nume = ? AND prenume = ?";
    private static String sql_delete_query = "DELETE FROM studenti WHERE nume = ? AND prenume = ?";
    private static String sql_read_all = "SELECT * FROM studenti";
    private static String sql_read_one_student = "SELECT * FROM studenti WHERE nume = ? and prenume = ?";


    public static Connection getConection() {
        try {
            if (global_conection == null || global_conection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                global_conection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return global_conection;
    }

    public static boolean insertStudent(StudentBean student) {
        Connection conn = getConection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql_insert_query)) {
            ps.setString(1, student.getNume());
            ps.setString(2, student.getPrenume());
            ps.setInt(3, student.getVarsta());
            ps.setInt(4, student.getAnulNasterii());

            int rows_affected = ps.executeUpdate();
            return rows_affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateStudent(String nume_student, String prenume_student, int noua_varsta) {
        Connection conn = getConection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql_update_query)) {
            ps.setInt(1, noua_varsta);
            ps.setString(2, nume_student);
            ps.setString(3, prenume_student);

            int rows_affected = ps.executeUpdate();
            return rows_affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteStudent(String numeStudent, String prenumeStudent) {
        Connection conn = getConection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql_delete_query)) {
            ps.setString(1, numeStudent);
            ps.setString(2, prenumeStudent);

            int rows_affected = ps.executeUpdate();
            return rows_affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<StudentBean> readAllStudents() {
        List<StudentBean> listaStudenti = new ArrayList<>();
        Connection conn = getConection();
        if (conn == null) return listaStudenti;

        try (PreparedStatement ps = conn.prepareStatement(sql_read_all);
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

    public static StudentBean findStudent(String nume, String prenume) {
        StudentBean studentGasit = null;
        Connection conn = getConection();
        if (conn == null) return null;

        try (PreparedStatement ps = conn.prepareStatement(sql_read_one_student)) {
            ps.setString(1, nume);
            ps.setString(2, prenume);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    studentGasit = new StudentBean();
                    studentGasit.setNume(rs.getString("nume"));
                    studentGasit.setPrenume(rs.getString("prenume"));
                    studentGasit.setVarsta(rs.getInt("varsta"));
                    studentGasit.setAnulNasterii(rs.getInt("anul_nasterii"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentGasit;
    }
}