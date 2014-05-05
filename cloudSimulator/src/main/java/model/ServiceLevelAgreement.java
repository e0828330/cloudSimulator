package model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class ServiceLevelAgreement {
	private int size;
	private int memory;
	private int cpus;
	private int bandwith;
	private double maxDowntime;
	private int priority;
	private Set<VirtualMachine> vms = new HashSet<VirtualMachine>(32);
}