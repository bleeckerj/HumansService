package com.nearfuturelaboratory.servlets;

/**
 *  $Id: LoggerServlet.java,v 1.2 2006/05/16 00:36:48 julian Exp $ 
 */
import java.io.File;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
// import servlet packages
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import com.nearfuturelaboratory.humans.scheduler.ScheduledStatusFetcher;
import com.nearfuturelaboratory.util.Constants;
import org.apache.commons.configuration.ConfigurationException;
// import log4j packages
import org.apache.logging.log4j.*;
import org.quartz.*;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;

import java.util.*;

/**
 * This servlet performs the task of setting up the log4j configuration.
 * <p>
 * This servlet is loaded on startup by specifying the load on startup
 * property in the web.xml. On load, it performs the following activities:
 * <ul> Looks for the log4j configuration file.
 * <ul> Configures the PropertyConfigurator using the log4j configuration
 * file if it finds it, otherwise throws an Error on your
 * Tomcat screen. So make sure to check the Tomcat screen once it starts up.
 * However, you will still be able to access the main application, but wont get
 * any log statements as you would expect.
 * NOTE: This illustrates an important point about Log4J. That it is fail safe.
 * The application will not stop running because Log4J could not be set up.
 *
 * @author julian@techkwondo.com
 */

//@SuppressWarnings("unused")

//@WebServlet(
//		name="logger", 
//		urlPatterns={"/logger"}
//		)
@Path("/admin")
public class StartupServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    final static Logger logger = LogManager.getLogger(com.nearfuturelaboratory.servlets.StartupServlet.class);


    public void init(ServletConfig config) throws ServletException {
        /////////////////////////////super.init(config);
        // next load up the properties
        ServletContext context = config.getServletContext();
        String foo = context.getRealPath("/");

        String props = foo+config.getInitParameter("props-files");



        try {
            System.out.println(this+" bad debugging.. constants="+config.getInitParameter("constants")+"\nfoo="+foo+"\nprops="+props);
            String constants = config.getInitParameter("constants");
            com.nearfuturelaboratory.util.Constants.load(foo+constants);
        }
        catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            System.err.println(ioe.getMessage());
            throw new ServletException("IOException trying to load application constants");
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        if(props == null || props.length() == 0 ||
                !(new File(props)).isFile()){
            //logger.error("Cannot read the configuration file. Please check the path of the config init param in web.xml");
            System.err.println(
                    "ERROR: Cannot read the configuration file. " +
                            "Please check the path of the config init param in web.xml");
            throw new ServletException();
        }

        logger.debug(System.getProperty("catalina.home")+" IS THE PROPERTY");

        String baz = System.getProperty("catalina.home");

        if(Constants.getBoolean("IS_REFRESH_SERVER", false)) {
            logger.info("This is a refresh server.");
            try {
                StdSchedulerFactory stdSchedulerFactory = (StdSchedulerFactory) context
                        .getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);

                Scheduler scheduler = stdSchedulerFactory.getScheduler();
                JobKey jobKey = new JobKey("ScheduledStatusFetcher");
                scheduler.triggerJob(jobKey);



                JobKey jobKey_2 = new JobKey("FriendsPrefetcher");
                scheduler.triggerJob(jobKey_2);


            } catch (SchedulerException e) {
                logger.warn(e);
            }

        }

        // look up another init parameter that tells whether to watch this
        // configuration file for changes.
        String watch = config.getInitParameter("watch");

        // since we have not yet set up our log4j environment,
        // we will use System.err for some basic information
        System.err.println("Using properties file: " + props);
        System.err.println("Watch is set to: " + watch);

        // use the props file to load up configuration parameters for log4j
//		if(watch != null && watch.equalsIgnoreCase("true")) {
//			PropertyConfigurator.configureAndWatch(props);
//		} else {
//			PropertyConfigurator.configure(props);
//		}
//		// once configured, we can start using the Looger now
//
//		Logger log = Logger.getLogger(com.nearfuturelaboratory.servlets.LoggerServlet.class);
//
//		Logger root = Logger.getRootLogger();
//		root.setLevel(Level.OFF);
//		root.error("--> "+root.getLevel());
//		root.error("--> "+root.getLoggerRepository());

    }


//	public void destroy(){
//		super.destroy();
//	}

    @GET
    @Path("/gettyup")
    @Produces({"application/json"})
    public Response buildCaches(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        // TODO Auto-generated method stub
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("ScheduledStatusFetcher").startNow().build();
        return Response.ok("{ok:ok}", MediaType.APPLICATION_JSON).build();

    }
}

