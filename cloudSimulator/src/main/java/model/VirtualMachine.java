package model;

import java.io.Serializable;
import java.util.HashMap;

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
	
	private HashMap<Integer, Double> cpuLoadMap;
	private HashMap<Integer, Double> memLoadMap;
	private HashMap<Integer, Double> bwLoadMap;
	
	/**
	 * Builds and stores load maps, used for non DB runs
	 * 
	 */
	public void buildLoadMaps() {
		cpuLoadMap = new HashMap<Integer, Double>();
		memLoadMap = new HashMap<Integer, Double>();
		bwLoadMap = new HashMap<Integer, Double>();
		
		/* Build it for one day we cycle through those values */
		for (int i = 0; i < 1440; i++) {
			cpuLoadMap.put(i, Utils.getRandomValue(0, cpus) / cpus);
			memLoadMap.put(i, Utils.getRandomValue(0, memory) / memory);
			bwLoadMap.put(i, Utils.getRandomValue(0, bandwidth) / bandwidth);
		}

		updateLoad(0);
	}
	
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
	public void updateLoad(int minute) {
		// TODO: Type + OS
		usedCPUs = cpuLoadMap.get(minute % 1440);
		usedMemory = memLoadMap.get(minute % 1440);
		usedBandwidth = bwLoadMap.get(minute % 1440);
	}
}
