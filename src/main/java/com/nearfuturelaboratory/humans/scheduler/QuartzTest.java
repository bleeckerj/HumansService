package com.nearfuturelaboratory.humans.scheduler;

import java.util.Date;

import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.TriggerBuilder.*; 


public class QuartzTest {

    public static void main(String[] args) {

        try {
    		JobDetail job = JobBuilder.newJob(HelloJob.class)
    				.withIdentity("dummyJobName", "group1").build();
    	 
    			Trigger trigger = TriggerBuilder
    				.newTrigger()
    				.withIdentity("dummyTriggerName", "group1")
    				.withSchedule(
    					SimpleScheduleBuilder.simpleSchedule()
    						.withIntervalInSeconds(5).repeatForever())
    				.build();
    	 
    			// schedule it
    			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
    			scheduler.start();
    			scheduler.scheduleJob(job, trigger);
        	
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
    
}

