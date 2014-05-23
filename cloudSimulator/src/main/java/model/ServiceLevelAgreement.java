package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import lombok.Data;

@Data
public class ServiceLevelAgreement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8606765698485478883L;

	@Id
	private String id;
	
	private int size;
	private int memory;
	private int cpus;
	private int bandwidth;
	private double maxDowntime;
	private int priority;
	private int downtime = 0;

	@Transient
	private Set<VirtualMachine> vms = new HashSet<VirtualMachine>(8);
	
	public void incrementDowntime() {
		downtime++;
	}
	
	public double getDownTimeInPercent(int minute) {
		return Math.min(1., (this.downtime / (double) minute));
	}
}