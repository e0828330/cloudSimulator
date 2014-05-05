package model;

import lombok.Data;

@Data
public class VirtualMachine {
	
	/* SLA parameters agreed with the customer */
	private ServiceLevelAgreement sla;
	
	/* Allocated resources */
	private int size;
	private int memory;
	private int cpus;
	private int bandwidth;

	/* Used resources in percent */
	private double usedCPUs;
	private double usedMemory;
	private double usedBandwidth;
	
	/* Whether the VM is on or down */
	private boolean online;
	
	/* Stores the physical machine (PM) where this VM is running */
	private PhysicalMachine pm;
	
	/**
	 * Returns the page dirty rate in pages / second
	 * @return
	 */
	public int getPageDirtyRate() {
		/* TODO: Implement */
		return 0;
	}
	
	/**
	 * Updates the current load, called periodically by the simulator
	 */
	public void updateLoad() {
		// TODO: Type + OS
		usedCPUs = 1;//Utils.getRandomValue(0, cpus) / cpus;
		usedMemory = 1;//= Utils.getRandomValue(0, memory) / memory;
		usedBandwidth = 1;//Utils.getRandomValue(0, bandwidth) / bandwidth;
	}
}
