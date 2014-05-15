package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import simulation.DataCenter;
import utils.Utils;

@Data
@EqualsAndHashCode(exclude={"dataCenter", "virtualMachines"})
public class PhysicalMachine implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2692450316461386079L;

	@Id
	private String id;
	
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
    
    /* Stores the DataCenter (DC) where this PM belongs to */
	@Transient
    private DataCenter dataCenter;

	/* Allocated virtual machines */
	private List<VirtualMachine> virtualMachines = new ArrayList<VirtualMachine>(8);
	
	/**
	 * Updates the load of the current running VMs,
	 * called periodically by the simulator
	 */
	public void updateLoads(int minute) {
		for(VirtualMachine vm : virtualMachines) {
			if (vm.isOnline()) {
				vm.updateLoad(minute);
			}
		}
	}
	
	/* Used resources */

	/**
	 * Returns the current CPU load based on the load of the running VMs
	 * @return
	 */
	public double getCPULoad() {
		return Math.min(1., Utils.getVMsCPULoad(virtualMachines) / cpus);
	}
	
	/**
	 * Returns the size based on the load of the running VMs
	 * @return
	 */
	public double getSizeUsage() {
		return Math.min(1., Utils.getVMsSize(virtualMachines) / size);
	}	
	
	/**
	 * Returns the current memory usage based on the load of the running VMs
	 * @return
	 */
	public double getMemoryUsage() {
		return Math.min(1., Utils.getVMsMemory(virtualMachines) / memory);
	}
	
	/**
	 * Returns the current bandwidth usage based on the load of the running VMs
	 * @return
	 */
	public double getBandwidthUtilization() {
		return Math.min(1., Utils.getVMsBandwidth(virtualMachines) / bandwidth);
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
