package model;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@Data
@EqualsAndHashCode(exclude={"vms"})
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
	private ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(8);
	
	public void incrementDowntime() {
		downtime++;
	}
	
	public double getDownTimeInPercent(int minute) {
		return Math.min(1., (this.downtime / (double) minute));
	}
}