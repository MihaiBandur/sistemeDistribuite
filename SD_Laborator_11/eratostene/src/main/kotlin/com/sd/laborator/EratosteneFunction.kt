package com.sd.laborator
import io.micronaut.function.FunctionBean
import io.micronaut.function.executor.FunctionInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Function
import jakarta.inject.Inject

@FunctionBean("eratostene")
class EratosteneFunction: FunctionInitializer(), Function<EratosteneRequest, EratosteneResponse> {
    @Inject
    private lateinit var eratosteneSieveService: EratosteneSieveService

    private val LOG: Logger = LoggerFactory.getLogger(EratosteneFunction::class.java)

    override fun apply(t: EratosteneRequest): EratosteneResponse {
        val number = t.get_Number()

        val response = EratosteneResponse()

        if(number >= eratosteneSieveService.MAX_SIZE){
            LOG.error("Parametru prea mare! $number > maximul de ${eratosteneSieveService.MAX_SIZE}")

            response.setMessage("Se accepta doar parametri mai mici ca" + eratosteneSieveService.MAX_SIZE)
            return response
        }

        LOG.info("Se calculeaza primele $number numere prime ...")

        response.setPrimes(eratosteneSieveService.findPrimesLessThan(number))
        response.setMessage("Calcul efectuat cu succes!")


        LOG.info("Calcul incheiat!")
        return response
    }
}

fun main(args: Array<String>){
    val function = EratosteneFunction()
    function.run(args, {
        context -> function.apply(context.get(EratosteneRequest::class.java))})
}