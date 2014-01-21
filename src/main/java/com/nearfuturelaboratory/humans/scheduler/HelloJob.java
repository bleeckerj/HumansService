package com.nearfuturelaboratory.humans.scheduler;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HelloJob implements Job {
    final static Logger logger = LogManager
            .getLogger(HelloJob.class);

	public void execute(JobExecutionContext context)
	throws JobExecutionException {
        logger.info("Hello Quartz! "+context+" We're here now "+new Date());
		System.out.println("Hello Quartz! "+ context+" " +new Date());	
 
	}
	
	
}
