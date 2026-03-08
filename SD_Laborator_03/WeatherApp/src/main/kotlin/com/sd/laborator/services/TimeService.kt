package com.sd.laborator.services

import com.sd.laborator.interfaces.TimeServiceInterface
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*

@Service
class TimeService: TimeServiceInterface {
    override fun getCurrentTime():String {
        val formatter =  SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return formatter.format(Date())
    }
}