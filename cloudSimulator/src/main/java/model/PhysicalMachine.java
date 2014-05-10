package model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class PhysicalMachine {
	
	private boolean running;
	
	/* Available resources */
	private int size;
	private int memory;
	private int cpus;
	private int bandwidth;
	
	/* Max energy consumption of CPU, Memory and Networkcard in watt */
	private int cpuPowerConsumption;
	private int memPowerConsumption;
	private int networkPowerConsumption;
	
	private double idleStateEnergyUtilization;
	
	
	/* Allocated virtual machines */
	private List<VirtualMachine> virtualMachines = new ArrayList<VirtualMachine>(8);
	
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
	 * Returns the current CPU used size based on the load of the running VMs
	 * @return
	 */
	public double getSizeUsage() {
		double usedSize = 0;
		for(VirtualMachine vm : virtualMachines) {
			if (vm.isOnline()) {
				usedSize += vm.getSize();
			}
		}
		return Math.min(1., usedSize / size);
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
	 * Returns the current bandwidth usage based on the load of the running VMs
	 * @return
	 */
	public double getBandwidthUtilization() {
		double bandwidthUsed = 0;
		for(VirtualMachine vm : virtualMachines) {
			if (vm.isOnline()) {
				bandwidthUsed += vm.getBandwidth() * vm.getUsedBandwidth();
			}
		}

		return Math.min(1., bandwidthUsed / bandwidth);
	}
	
	/**
	 * Returns the total power consumption of this physical machine
	 * @return
	 */
	public double getPowerConsumption() {
		if (!isRunning()) {
			return 0.0;
		}
		double totalEnergyUsed = idleStateEnergyUtilization + cpuPowerConsumption * getCPULoad()
				+ memPowerConsumption * getMemoryUsage() + networkPowerConsumption * getBandwidthUtilization();
		return totalEnergyUsed;
	}
	
	/**
	 * Returns a list of all VMs with state Online
	 * @return
	 */
	public ArrayList<VirtualMachine> getOnlineVMs() {
		ArrayList<VirtualMachine> tmp = new ArrayList<VirtualMachine>(virtualMachines.size());
		for (VirtualMachine vm : virtualMachines) {
			if (vm.isOnline()) {
				tmp.add(vm);
			}
		}
		return tmp;
	}
	
}
