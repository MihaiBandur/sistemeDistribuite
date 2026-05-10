package com.sd.laborator

import io.micronaut.function.FunctionBean
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.function.Consumer
import javax.xml.parsers.DocumentBuilderFactory
@FunctionBean("xckd-consumer")
class XckdConsumer: Consumer<String> {
    private val LOG = LoggerFactory.getLogger(XckdConsumer::class.java)
    override fun accept(t: String) {
        LOG.info("S-a intrat in consumator")
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val doc = factory.newDocumentBuilder().parse(InputSource(StringReader(t)))
            val entries = doc.getElementsByTagName("entry")

            for(i in 0 until entries.length){
                val  node = entries.item(i) as? Element
                if(node != null){
                    val title = node.getElementsByTagName("title").item(0)?.textContent ?: "Fără titlu"
                    val href = (node.getElementsByTagName("link").item(0) as? Element)?.getAttribute("href") ?: "Fără link"
                    println("<$title, $href>")
                }
            }
        }catch (e: Exception){
            LOG.error(e.message)
        }
        LOG.info("S-a terminat consumaotorul ")


    }
}