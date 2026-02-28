import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MonitoringServlet implements ServletContextListener {
    private EntityManagerFactory emf;
    private Thread threadMonitorizareVarsta;
    private Thread threadMonitorizareId;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        emf = Persistence.createEntityManagerFactory("bazaDeDateSQLite");

        MonitoringThread thread_1 = new MonitoringThread("varsta", 18, 65, emf);
        threadMonitorizareVarsta = new Thread(thread_1);
        threadMonitorizareVarsta.start();

        MonitoringThread thread_2 = new MonitoringThread("id", 1, 1000, emf);
        threadMonitorizareId = new Thread(thread_2);
        threadMonitorizareId.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        if (threadMonitorizareVarsta != null) threadMonitorizareVarsta.interrupt();
        if (threadMonitorizareId != null) threadMonitorizareId.interrupt();
        if (emf != null) emf.close();
        System.out.println("=== Oprire monitorizare baza de date ===");
    }
}