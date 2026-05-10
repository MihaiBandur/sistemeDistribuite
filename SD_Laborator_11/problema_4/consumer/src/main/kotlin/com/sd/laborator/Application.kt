package com.sd.laborator
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.runtime.Micronaut
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn

object Application {
    @JvmStatic
    fun main(args: Array<String>){
        Micronaut.run(Application::class.java, *args)
    }

    @Controller
    @ExecuteOn(TaskExecutors.BLOCKING)


    class LambdaController{
        companion object {
            private val handler = XckdConsumer()
        }

        @Post(consumes = ["text/plain"])
        fun execute(@Body xml: String){
            handler.accept(xml)
        }
    }
}