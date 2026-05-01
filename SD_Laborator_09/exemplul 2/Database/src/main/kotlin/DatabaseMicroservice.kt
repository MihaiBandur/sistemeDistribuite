package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct

data class Client(val nume: String, val adresa: String)
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
                cantitate INTEGER
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
}

fun main(args: Array<String>) {
    runApplication<DatabaseMicroservice>(*args)
}
