package model;

import java.util.List;

import lombok.Data;

@Data
public class PhysicalMachine {
	
	private boolean running;
	
	/* Available resources */
	private int size;
	private int memory;
	private int cpus;
	private int bandwith;
	
	/* Allocated virtual machines */
	private List<VirtualMachine> virtualMachines;
	
	/**
	 * Updates the load of the current running VMs,
	 * called periodically by the simulator
	 */
	public void updateLoads() {
		for(VirtualMachine vm : virtualMachines) {
			if (vm.isOnline()) {
				vm.updateLoad();
			}
		}
	}
	
	/* Used resources */

	/**
	 * Returns the current CPU load based on the load of the running VMs
	 * @return
	 */
	public double getCPULoad() {
		double usedCpus = 0;
		for(VirtualMachine vm : virtualMachines) {
			if (vm.isOnline()) {
				usedCpus += vm.getCpus() * vm.getUsedCPUs();
			}
		}

		return Math.min(1., usedCpus / cpus);
	}
	
	/**
	 * Returns the current memory usage based on the load of the running VMs
	 * @return
	 */
	public double getMemoryUsage() {
		double memoryUsed = 0;
		for(VirtualMachine vm : virtualMachines) {
			if (vm.isOnline()) {
				memoryUsed += vm.getMemory() * vm.getUsedMemory();
			}
		}

		return Math.min(1., memoryUsed / memory);
	}
	
	/**
	 * Returns the current memory usage based on the load of the running VMs
	 * @return
	 */
	public double getBandwithUtilization() {
		double bandwithUsed = 0;
		for(VirtualMachine vm : virtualMachines) {
			if (vm.isOnline()) {
				bandwithUsed += vm.getBandwith() * vm.getUsedBandwith();
			}
		}

		return Math.min(1., bandwithUsed / bandwith);
	}
	
}
