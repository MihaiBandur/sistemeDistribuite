import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

public  class MonitoringThread implements  Runnable{
    private String row_to_watch;
    private int low_boundary;
    private int high_boundary;
    private EntityManagerFactory entityManager;

   public MonitoringThread(String row_to_watch, int low_boundary, int high_boundary, EntityManagerFactory em){
       this.row_to_watch = row_to_watch;
       this.low_boundary = low_boundary;
       this.high_boundary = high_boundary;
       this.entityManager = em;
   }

   @Override
    public  void run(){
       while (Thread.currentThread().isAlive()){
           try {
               EntityManager em = entityManager.createEntityManager();
               String sql = "SELECT s." + row_to_watch + " FROM StudentEntity s WHERE s." + row_to_watch + " < :min OR s." + row_to_watch + " > :max";
               TypedQuery<Number> query = em.createQuery(sql, Number.class);
               query.setParameter("min", low_boundary);
               query.setParameter("max", high_boundary);

               List<Number>  valori_invalida = query.getResultList();

               for(Number val: valori_invalida){
                   String mesajAlarma = "ALARMA! Campul '" + row_to_watch + "' a iesit din intervalul [" + low_boundary + ", " + high_boundary + "]. Valoare gasita: " + val;
                   if(!AlarmStore.alarme.contains(mesajAlarma)){
                       AlarmStore.alarme.add(mesajAlarma);
                   }
                   Thread.sleep(10000);
               }
               em.close();
           }catch (InterruptedException e){
               Thread.currentThread().interrupt();
               break;
           }catch (Exception e){
               e.printStackTrace();
           }

       }
   }

}
