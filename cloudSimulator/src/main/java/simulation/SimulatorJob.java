package simulation;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SimulatorJob implements Job {

	/**
	 * Gets called on every simulation tick which means 1 minute in the simulated time
	 */
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		DataCenter dc = (DataCenter) ctx.getJobDetail().getJobDataMap().get("datacenter");
		dc.simulate();
	}

}
