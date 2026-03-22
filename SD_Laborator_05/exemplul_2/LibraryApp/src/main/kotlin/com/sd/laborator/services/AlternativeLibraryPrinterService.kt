package com.sd.laborator.services

import com.sd.laborator.interfaces.LibraryPrinter
import com.sd.laborator.model.Book
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class AlternativeLibraryPrinterService: LibraryPrinter{
    init {
        println("=====================================================================")
        println(">>> INFO: S-a instanțiat AlternativeLibraryPrinterService! <<<")
        println(">>> Dacă vezi asta, LSP funcționează și s-a înlocuit implementarea! <<<")
        println("=====================================================================")
    }
    override fun printHTML(books: Set<Book>): String {
        return buildString {
            append("<html><head><title>Alternative HTML Library</title></head><body>\n")
            append("<h2>Catalogul Bibliotecii (Format Alternativ)</h2>\n<ul>\n")
            books.forEach { book ->
                append("<li>\n")
                append("  <strong>Titlu:</strong> ${book.name} <br/>\n")
                append("  <em>Autor:</em> ${book.author} <br/>\n")
                append("  <u>Editura:</u> ${book.publisher} <br/>\n")
                append("  <blockquote>${book.content}</blockquote>\n")
                append("</li><hr/>\n")
            }
            append("</ul>\n</body></html>")
        }
    }

    override fun printJSON(books: Set<Book>): String {
        val jsonElements = books.joinToString(separator = ",\n"){ book->
            """
            |    {
            |      "Titlu": "${book.name}",
            |      "Autor": "${book.author}",
            |      "Editura": "${book.publisher}",
            |      "Text": "${book.content}"
            |    }
            """.trimMargin()
        }

        return "[\n$jsonElements\n]"
    }

    override fun printRaw(books: Set<Book>): String {
        return buildString {
            append("=== CATALOG BIBLIOTECA (RAW) ===\n\n")
            books.forEach { book ->
                append("Titlu: ${book.name}\n")
                append("Autor: ${book.author}\n")
                append("Editura: ${book.publisher}\n")
                append("Continut: ${book.content}\n")
                append("-----------------------------------\n")
            }
        }
    }

    override fun printXML(books: Set<Book>): String {
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<books>\n")
            books.forEach { book ->
                append("  <book>\n")
                append("    <title>${book.name}</title>\n")
                append("    <author>${book.author}</author>\n")
                append("    <publisher>${book.publisher}</publisher>\n")
                append("    <content>${book.content}</content>\n")
                append("  </book>\n")
            }
            append("</books>")
        }
    }
}