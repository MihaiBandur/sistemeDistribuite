# Teme Sisteme Distribuite

## Laboratorul 1
* **Application Architecture**: Developed a Java-based web application following a client-server model.
* **Packaging**: Bundled the client-side interfaces and web components into a single WAR (Web ARchive) file for streamlined deployment.
* **Server Deployment**: Deployed and hosted the WAR application on a GlassFish application server.
* **Data Persistence**: Integrated a MySQL database to securely store, manage, and retrieve the required application information.

## Laboratorul 2

* **Enterprise Packaging**: Developed the business logic tier and packaged the complete application as an EAR (Enterprise ARchive) file for enterprise-level deployment.
* **Database Integration**: Transitioned data persistence to an SQLite database for lightweight, file-based storage.
* **Concurrency & Monitoring**: Implemented a dedicated background thread to continuously monitor specific numeric parameters, ensuring they remain within a safe range.
* **Alerting System**: Integrated an alerting mechanism to proactively notify the user if the monitored parameters exceed the defined thresholds.
* **EJB Implementation**: Gained practical knowledge of Enterprise JavaBeans (EJB) by implementing and differentiating between **Stateless** and **Stateful** session beans to manage application state and business processes.

## Laboratorul 3

* **Geographic Access Control**: Implemented a location-based security filter that reads the host operating system's locale to block service access for specific blacklisted geographic zones.
* **Service Chaining Architecture**: Designed and implemented a pipeline-style distributed pattern where each service is tightly coupled to the next, sequentially passing data down the chain (Filtering -> Geocoding -> Weather).
* **Service Orchestration Architecture**: Reprojected the application using a centralized "Orchestrator" (Dirijor) pattern on a separate Git branch. This completely decoupled the underlying services, allowing the Orchestrator to independently manage the workflow and decision-making process.
