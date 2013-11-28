package com.nearfuturelaboratory.humans.rest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("myresource/{text}")
public class MyResource {
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt(@PathParam("text") String text) {
        return "Got it For Good! "+text;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getItJson() {
    	return "{hello:you}";
    }
}
