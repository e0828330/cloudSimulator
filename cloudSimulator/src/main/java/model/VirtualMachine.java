package model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import utils.Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude={"pm", "sla"})
public class VirtualMachine implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 828356080039466875L;

	@Id
	private String id;
	
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
	@Transient
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
		usedCPUs = Utils.getRandomValue(0, cpus) / cpus;
		usedMemory = Utils.getRandomValue(0, memory) / memory;
		usedBandwidth = Utils.getRandomValue(0, bandwidth) / bandwidth;
	}
}
