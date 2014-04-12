package cloudSimulator;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
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

	private final int siumlatedMinutes = 3600; // Should be 525600 (One year)
	private final int lengthOfSimulatedMinute = 5; // ms

	public static void main(String[] args) {
		System.out.println("Manager - Console");
		SpringApplication.run(Simulator.class, args);
	}

	public void run(String... arg0) throws Exception {
		System.out.println("Started");

		/* This is just a test */
		/* TODO: Read config file and build from there */
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

		Thread.sleep(1100 + siumlatedMinutes * lengthOfSimulatedMinute);
		
		scheduler.shutdown(true);
		System.out.println("Simulation ended!");
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
		SimpleScheduleBuilder schedule = simpleSchedule().withIntervalInMilliseconds(lengthOfSimulatedMinute).withRepeatCount(siumlatedMinutes - 1);
		Trigger trigger = newTrigger().startAt(startTime).withSchedule(schedule).build();
		scheduler.scheduleJob(getJobDetail(dc), trigger);
	}

	/**
	 * Builds and returns the job detail for a dataCenter
	 */
	private JobDetail getJobDetail(DataCenter dc) {
		JobDetail jobDetail = newJob(SimulatorJob.class).withIdentity(dc.getName()).build();
		jobDetail.getJobDataMap().put("datacenter", dc);
		return jobDetail;
	}
}
