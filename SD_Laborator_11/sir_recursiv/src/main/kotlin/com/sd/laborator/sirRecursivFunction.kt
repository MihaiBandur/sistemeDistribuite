package com.sd.laborator
import io.micronaut.function.FunctionBean
import io.micronaut.function.executor.FunctionInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Function
import jakarta.inject.Inject

@FunctionBean("sir_recursiv")
class sirRecursivFunction: FunctionInitializer(), Function<sirRecursivRequest, sirRecursivResponse> {
    @Inject
    private lateinit var  sirRecursivService: sirRecursivService

    private var LOG: Logger = LoggerFactory.getLogger(sirRecursivFunction::class.java)

    override fun apply(t: sirRecursivRequest): sirRecursivResponse {
        val my_number = t.get_number()

        val response = sirRecursivResponse()

        if(my_number > sirRecursivService.MAX_SIZE){
            LOG.error("Parametrul prea mare! $my_number > macumul de ${sirRecursivService.MAX_SIZE}")

            response.setMessage("Se accepta doar parametri mai mici ca" + sirRecursivService.MAX_SIZE)

            return response
        }

        LOG.info("Se calculeaza numarul $my_number din sirul recursiv ...")

        response.setTargetNumber(sirRecursivService.calculateTerm(my_number))
        response.setMessage("Calcul efectuat cu succes!")

        LOG.info("Calcul incheiat!")
        return  response
    }
}

fun main(args: Array<String>){
    val function = sirRecursivFunction()
    function.run(args, {
            context -> function.apply(context.get(sirRecursivRequest::class.java))})
}