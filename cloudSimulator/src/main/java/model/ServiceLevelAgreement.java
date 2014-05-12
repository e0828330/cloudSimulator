package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class ServiceLevelAgreement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8606765698485478883L;

	private int size;
	private int memory;
	private int cpus;
	private int bandwith;
	private double maxDowntime;
	private int priority;
	private Set<VirtualMachine> vms = new HashSet<VirtualMachine>(8);
}