package cloudSimulator;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import simulation.DataCenter;
import simulation.SimulatorJob;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Simulator implements CommandLineRunner {

	private final int siumlatedMinutes = 525600; // One year
	private final int lengthOfSimulatedMinute = 2; // ms
	
	public static void main(String[] args) {
		System.out.println("Manager - Console");
		SpringApplication.run(Simulator.class, args);
	}

	
	public void run(String... arg0) throws Exception {
		System.out.println("Started");
		
		/* This is just a test */
    	DataCenter dc1 = new DataCenter();
    	dc1.setName("DataCenter Vienna");
    	
    	DataCenter dc2 = new DataCenter();
    	dc2.setName("DataCenter New York");
    	
    	DataCenter dc3 = new DataCenter();
    	dc3.setName("DataCenter Tokio");
    	
    	
    	Scheduler scheduler = new StdSchedulerFactory().getScheduler();
    	scheduler.start();
    	
    	Date startTime = new Date(System.currentTimeMillis() + 1000);
    	
    	startDataCenter(scheduler, dc1, startTime);
    	startDataCenter(scheduler, dc2, startTime);
    	startDataCenter(scheduler, dc3, startTime);
	}
	
	/**
	 * Schedules a dataCenter to get started
	 * 
	 * @param scheduler
	 * @param dc
	 * @param startTime
	 * @throws SchedulerException
	 */
	private void startDataCenter(Scheduler scheduler, DataCenter dc, Date startTime) throws SchedulerException {
		SimpleTrigger trigger = new SimpleTrigger();
		trigger.setStartTime(startTime);
    	trigger.setRepeatCount(siumlatedMinutes); // One Year
    	trigger.setRepeatInterval(lengthOfSimulatedMinute);
    	trigger.setName("Trigger - " + dc.getName());
    	scheduler.scheduleJob(getJobDetail(dc), trigger);
	}
	
	/**
	 * Builds and returns the job detail for a dataCenter
	 */
	private JobDetail getJobDetail(DataCenter dc) {
		JobDetail jobDetail = new JobDetail();
		jobDetail.setName(dc.getName());
		jobDetail.setJobClass(SimulatorJob.class); 	
		jobDetail.getJobDataMap().put("datacenter", dc);
    	return jobDetail;
	}
}
