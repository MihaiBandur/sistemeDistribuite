package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct

data class Client(val nume: String, val adresa: String)
data class ComenziAmanate(val numeClient: String, val produs: String, val cantitate: Int)

@SpringBootApplication
@RestController
@RequestMapping("/api")
class DatabaseMicroservice (val jdbcTemplate: JdbcTemplate){

    @PostConstruct
    fun initDb() {

        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS clienti (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nume TEXT,
                adresa TEXT
            )
        """
        )

        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS stoc (
                produs TEXT PRIMARY KEY,
                cantitate INTEGER,
                prag_reaprovizionare INTEGER DEFAULT 3
            )
        """
        )

        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS comenzi (
                id INTEGER PRIMARY KEY,
                date_comanda TEXT
            )
        """
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS facturi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                detalii TEXT
            )
        """
        )
        jdbcTemplate.execute(
            """
            CREATE TABLE IF NOT EXISTS livrari (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                detalii TEXT
            )
        """
        )

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS comenzi_amanate(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nume_client TEXT,
                produs TEXT,
                cantitate INTEGER,
                data_amanare TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)


        // Populam stocul initial
        val countStoc = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stoc", Int::class.java)
        if (countStoc == 0) {
            jdbcTemplate.update("INSERT INTO stoc (produs, cantitate) VALUES ('Masca protectie', 1000)")
            jdbcTemplate.update("INSERT INTO stoc (produs, cantitate) VALUES ('Vaccin anti-COVID-19', 500)")
            jdbcTemplate.update("INSERT INTO stoc (produs, cantitate) VALUES ('Combinezon', 300)")
            jdbcTemplate.update("INSERT INTO stoc (produs, cantitate) VALUES ('Manusa chirurgicala', 2000)")
        }

    }

    // --- Endpoint-uri ---

    @PostMapping("/comenzi/{id}")
    fun salveazaComanda(@PathVariable id: Int, @RequestBody dateComanda: String){
        jdbcTemplate.update("INSERT INTO comenzi (id, date_comanda) VALUES (?, ?)", id, dateComanda)
    }

    @GetMapping("/comenzi/{id}")
    fun getComanda(@PathVariable id: Int): String? = try {
        jdbcTemplate.queryForObject("SELECT date_comanda FROM comenzi WHERE id = ?", String::class.java, id)
    } catch (e: Exception) { null }

    @GetMapping("/stoc")
    fun verificaStoc(@RequestParam produs: String): Int = try {
        jdbcTemplate.queryForObject("SELECT cantitate FROM stoc WHERE produs = ?", Int::class.java, produs) ?: 0
    } catch (e: Exception) { 0 }

    @PostMapping("/stoc/extrage")
    fun extrageStoc(@RequestParam produs: String, @RequestParam cantitate: Int){
        jdbcTemplate.update("UPDATE stoc SET cantitate = cantitate - ? WHERE produs = ?", cantitate, produs)
    }

    @PostMapping("/facturi")
    fun salveazaFactura(@RequestBody detalii: String){
        jdbcTemplate.update("INSERT INTO facturi (detalii) VALUES (?)", detalii)
    }

    @PostMapping("/livrari")
    fun salveazaLivrare(@RequestBody detalii: String){
        jdbcTemplate.update("INSERT INTO livrari (detalii) VALUES (?)", detalii)
    }

    @GetMapping("/clienti")
    fun getClienti(): List<Map<String, Any>> {
        return jdbcTemplate.queryForList("SELECT * FROM clienti")
    }

    @PostMapping("/clienti")
    fun adaugaClient(@RequestBody client: Client){
        jdbcTemplate.update("INSERT INTO clienti (nume, adresa) VALUES (?, ?)", client.nume, client.adresa)
    }
    @GetMapping("/comenzi")
    fun getComenzi(): List<Map<String, Any>>{
       return jdbcTemplate.queryForList("SELECT * FROM comenzi")
    }

    @GetMapping("/stoc/toate")
    fun getEntireStoc(): List<Map<String, Any>>{
        return jdbcTemplate.queryForList("SELECT * FROM stoc")
    }

    @PostMapping("/comenzi-amanate")
    fun salveazaComandaAmanata(@RequestBody req: ComenziAmanate){
        jdbcTemplate.update(
            "INSERT INTO comenzi_amanate (nume_client, produs, cantitate) VALUES (?, ?, ?)",
            req.numeClient, req.produs, req.cantitate
        )
        println("DB: Comanda amanata salvata pentru clientul ${req.numeClient}")
    }

    @GetMapping("/comenzi-amanate/{client}")
    fun getComandaAmanataPentruClinet(@PathVariable client: String): List<Map<String, Any>>{
        return jdbcTemplate.queryForList("SELECT * FROM comenzi_amanate WHERE nume_client = ?", client)
    }

    @GetMapping("/stoc/verificare-aprovizionare")
    fun verificareAprovizionare(@RequestParam produs: String): Boolean{
        val comenziAmanate = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM comenzi_amanate WHERE produs = ?",
            Int::class.java,
            produs
        ) ?: 0

        val prag = jdbcTemplate.queryForObject(
            "SELECT prag_reaprovizionare FROM stoc WHERE produs = ?",
            Int::class.java,
            produs
        ) ?: 3

        return comenziAmanate >= prag
    }

    @PostMapping("/stoc/aprovizionare")
    fun reaprovizionareStoc(@RequestParam produs: String, @RequestParam cantitate: Int){

        jdbcTemplate.update(
            "UPDATE stoc SET cantitate = cantitate + ? WHERE produs = ?",
            cantitate, produs
        )
        println("DB: Am adaugat $cantitate bucati de $produs la stocul existent.")

        jdbcTemplate.update("DELETE FROM comenzi_amanate WHERE produs = ?", produs)
    }

}

fun main(args: Array<String>) {
    runApplication<DatabaseMicroservice>(*args)
}
