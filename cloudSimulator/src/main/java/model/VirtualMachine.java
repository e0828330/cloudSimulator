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
	private int bandwith;

	/* Used resources in percent */
	private double usedCPUs;
	private double usedMemory;
	private double usedBandwith;
	
	/* Whether the VM is on or down */
	private boolean online;
	
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
		/* TODO: Implement */
	}
}
