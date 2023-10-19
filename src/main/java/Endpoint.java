
import java.util.logging.Logger;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/fault")
public class Endpoint {

    /* Coloca no console as mensagens de erro */
    private static final Logger LOGGER = Logger.getLogger(Endpoint.class.getName());

    private static final String FALL_BACK_MESSAGE = "FallbackMethod: ";

    @GET
    @Path("/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Retry(maxRetries = 3, delay = 2000)
    @Fallback(fallbackMethod = "recover")
    @Timeout(7000)
    public String getName(@PathParam("name") String name) {

        // Use esse trecho para simular um timeout
        
        try {
        this.sleep();
        } catch (InterruptedException e) {
        LOGGER.info("Timeout");
        }

        if (name.equalsIgnoreCase("error")) {
            ResponseBuilderImpl builder = new ResponseBuilderImpl();
            builder.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("The requested was an error");
            Response response = builder.build();
            throw new WebApplicationException(response);
        }

        return name;
    }

    /**
     * Método usado para se recuperar de uma falha
     *
     * @param name O valor da url
     * @return uma mensagem de erro juntamente com o parâmetro de entrada
     */
    public String recover(String name) {
        return FALL_BACK_MESSAGE + name;
    }

    /**
     * Para testar o Bulkhead instale a ferramenta k6:
     *
     * https://k6.io/docs/
     *
     * Logo, na raiz desse projeto execute o comando:
     *
     * k6 run k6.js
     *
     * @param name
     * @return
     */
    @GET
    @Path("/bulkhead/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Bulkhead(2)
    public String bulkhead(@PathParam("name") String name) {
        LOGGER.info(name);
        return name;
    }

    /**
     * Interrompe a thread por 10 segundos
     *
     * @throws InterruptedException
     */
    private void sleep() throws InterruptedException {
        Thread.sleep(10000);
    }

}