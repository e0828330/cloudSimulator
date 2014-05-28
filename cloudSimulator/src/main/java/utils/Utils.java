package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import model.PhysicalMachine;
import model.VirtualMachine;
import simulation.DataCenter;

public class Utils {
	/**
	 * Gets the current simulated date based on the current minute
	 * 
	 * @param minute
	 * @return
	 */
	public static Date getCurrentTime(int minute) {
		long start = 1356994800l; // 01.01.2013 00:00
		return new Date((start + minute * 60) * 1000);
	}

	/**
	 * Sorts the given list @vmList by priority descending (Highest priority
	 * first).
	 * 
	 * @param vmList
	 */
	public static void orderVMsByPriority(List<VirtualMachine> vmList) {
		Collections.sort(vmList, new Comparator<VirtualMachine>() {
			public int compare(VirtualMachine vm1, VirtualMachine vm2) {
				if (vm1.getSla() == null && vm2.getSla() == null) {
					return 0;
				}
				if (vm1.getSla() != null && vm2.getSla() != null) {
					return vm2.getSla().getPriority() - vm1.getSla().getPriority();
				}
				return 0;
			}
		});
	}

	/**
	 * Checks if the virtual machine @vm has place on the physical machine @pm
	 * 
	 * @param pm
	 *            The physical machine running * VMs
	 * @param vm
	 *            The virtual machine which should run on the PM
	 * @return
	 */
	public static boolean VMfitsOnPM(PhysicalMachine pm, VirtualMachine vm) {
		double sizePM = pm.getSizeUsage() * pm.getSize();
		double cpusPM = pm.getCPULoad() * pm.getCpus();
		double memoryPM = pm.getMemoryUsage() * pm.getMemory();
		double bandwidthPM = pm.getBandwidthUtilization() * pm.getBandwidth();

		if (sizePM + (double) vm.getSize() <= pm.getSize() && cpusPM + (double) vm.getCpus() <= pm.getCpus() && memoryPM + (double) vm.getMemory() <= pm.getMemory()
				&& bandwidthPM + (double) vm.getBandwidth() <= pm.getBandwidth()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the datacenter is available to take the vm
	 * @param dc The datacenter where the virtual machine should be migrated to
	 * @param vm The virtual machine for migration
	 * @return
	 */
	public static boolean VMisTransferable(DataCenter dc, VirtualMachine vm) {
		for (PhysicalMachine pm : dc.getPhysicalMachines()) {
			if (Utils.VMfitsOnPM(pm, vm)) {
				return true;
			}
		}		
		return false;
	}

	/**
	 * Migrates @vm to @pm
	 * 
	 * @param pm
	 *            The physical machine
	 * @param vm
	 *            The virtual machine
	 */
	public static void migrateVM2PM(PhysicalMachine pm, VirtualMachine vm) {
        vm.setPm(pm);
		pm.getVirtualMachines().add(vm);
	}

	/**
	 * Generates a random value betweet min and max
	 * 
	 * @return
	 */
	public static double getRandomValue(double min, double max) {
		return (min + Math.random() * max - min);
	}
	
	/**
	 * Adds the consumption of @vm to the current one of @pm and returns the used
	 * energy, if the vm would be added to this physical machine
	 * @param pm The current physical machine
	 * @param vm The virtual machine, which would be added
	 * @return
	 */
	public static double getFutureEnergyConsumption(PhysicalMachine pm, VirtualMachine vm) {
		double currentCPUsUsed = pm.getCPULoad() * pm.getCpus();
		double currentMemoryUsed = pm.getMemoryUsage() * pm.getMemory();
		double currentBandwidthUsed = pm.getBandwidthUtilization() * pm.getBandwidth();
		
		double futureCPULoad = currentCPUsUsed + vm.getCpus() * vm.getUsedCPUs();
		double futureMemoryUsage = currentMemoryUsed + vm.getMemory() * vm.getUsedMemory();
		double futureBandwidthUtilization = currentBandwidthUsed + vm.getBandwidth() * vm.getUsedBandwidth();
		
		double futureEnergyUsed = pm.getIdleStateEnergyUtilization() + pm.getCpuPowerConsumption() * futureCPULoad
				+ pm.getMemPowerConsumption() * futureMemoryUsage + pm.getNetworkPowerConsumption() * futureBandwidthUtilization;
		
		return futureEnergyUsed;
	}
	
	/**
	 * Calculates the used CPUs of all VMs in the list
	 * @param vms
	 * @return
	 */
	public static double getVMsCPULoad(List<VirtualMachine> vms) {
		double usedCpus = 0;
		for(VirtualMachine vm : vms) {
			if (vm.isOnline()) {
				usedCpus += vm.getCpus() * vm.getUsedCPUs();
			}
		}
		return usedCpus;
	}
	
	/**
	 * Calculates the used size of all VMs in the list
	 * @param vms
	 * @return
	 */	
	public static double getVMsSize(List<VirtualMachine> vms) {
		double usedSize = 0;
		for(VirtualMachine vm : vms) {
			//if (vm.isOnline()) {
				usedSize += vm.getSize();
			//}
		}
		return usedSize;
	}
	
	/**
	 * Calculates the used memory of all VMs in the list
	 * @param vms
	 * @return
	 */		
	public static double getVMsMemory(List<VirtualMachine> vms) {
		double memoryUsed = 0;
		for(VirtualMachine vm : vms) {
			if (vm.isOnline()) {
				memoryUsed += vm.getMemory() * vm.getUsedMemory();
			}
		}
		return memoryUsed;
	}
	
	/**
	 * Calculates the used bandwidth of all VMs in the list
	 * @param vms
	 * @return
	 */		
	public static double getVMsBandwidth(List<VirtualMachine> vms) {
		double bandwidthUsed = 0;
		for(VirtualMachine vm : vms) {
			if (vm.isOnline()) {
				bandwidthUsed += vm.getBandwidth() * vm.getUsedBandwidth();
			}
		}
		return bandwidthUsed;
	}	

	/**
	 * Calculates the time for transmitting a file of @size by bandwidth @availableBandwidth
	 * 
	 * @param bandwidth
	 *            in Mbit/s
	 * @param size
	 *            in GB
	 * @return Time for migration in minutes
	 */
	public static int getMigrationTime(double availableBandwidth, double size) {
		double mbit = size * 1024 * 8;
		return (int) Math.ceil((mbit / availableBandwidth / 60));
	}

	/**
	 * Returns the current factor for cooling efficiency as per
	 * "H. Xu, C. Feng, B. Li. Temperature aware workload management in geo-distributed datacenters"
	 * 
	 * Temperature input is in Celsius
	 * 
	 * @param temp
	 * @return
	 */
	public static double getCoolingEnergyFactor(double temp) {
		return 7.1705 * Math.pow(10., -5) * Math.pow(temp, 2) + 0.0041 * temp + 1.0743;
	}
    
    /**
     * Gets the pPUE of a PM. A smaller value indicates a more energy efficient system.
     * 
     * @param pm
     * @param temp
     * @return 
     */
    public static double getPartialPowerUsageEffectivness(PhysicalMachine pm, double temp){ // pPUE
        return pm.getPowerConsumption() > 0 ? (pm.getPowerConsumption() * getCoolingEnergyFactor(temp)) / pm.getPowerConsumption() : .0;
    }
    
    public static double getPartialPowerUsageEffectivness(DataCenter dc, double temp){ // pPUE
        double pPUE = 0;
        for(PhysicalMachine pm : dc.getPhysicalMachines()){
            pPUE = getPartialPowerUsageEffectivness(pm, temp);
        }
        return dc.getPhysicalMachines().size() > 0 ? pPUE / dc.getPhysicalMachines().size() : 0;
    }
    
    public static double getTotalDataCenterEnergyByWorkload(DataCenter dc, double workload, double temp){ // Ej(Wj)
        double cp = 0;
        double pp = 0;
        for(PhysicalMachine pm : dc.getPhysicalMachines()){
            cp += pm.getCpuPowerConsumption() * pm.getIdleStateEnergyUtilization();
            pp += pm.getCpuPowerConsumption() - (pm.getCpuPowerConsumption() * pm.getIdleStateEnergyUtilization());
        }
        pp = pp / dc.getPhysicalMachines().size();
        
        return (cp+pp*workload)*getPartialPowerUsageEffectivness(dc, temp);
    }
    
    public static double getTotalEnergyCost(DataCenter dc, double workload, double temp, float currentEnergyPrice){
        return currentEnergyPrice * getTotalDataCenterEnergyByWorkload(dc, workload, temp);
    }
    
    public static double getCapacityAllocationUtilityLoss(DataCenter dc){ // Vj(betaj)
        double r  = 1.0; // utility price that converts the loss to monetary terms.
        return r * (Math.log(dc.getPhysicalMachines().size()) - Math.log(dc.getOnlinePMs().size()));
    }
    
    
    
    /**
     * Checks if the physical machine @pm has less memory than used by the vms running on it
     * @param pm
     * @return
     */
    public static boolean PMMemoryIsViolated(PhysicalMachine pm) {
    	ArrayList<VirtualMachine> onlineVMs = pm.getOnlineVMs();
		double usedVMMemory = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMMemory += vm.getUsedMemory() * vm.getMemory();
		}
		if (usedVMMemory > pm.getMemory()) {
			return true;
		}
    	return false;
    }
    
    /**
     * Checks if the physical machine @pm has less cpus than used by the vms running on it
     * @param pm
     * @return
     */    
    public static boolean PMCPUsIsViolated(PhysicalMachine pm) {
    	ArrayList<VirtualMachine> onlineVMs = pm.getOnlineVMs();
		double usedVMCPUs = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMCPUs += vm.getUsedCPUs() * vm.getCpus();
		}
		if (usedVMCPUs > pm.getCpus()) {
			return true;
		}
    	return false;
    } 
    
    /**
     * Checks if the physical machine @pm has less bandwidth than used by the vms running on it
     * @param pm
     * @return
     */    
    public static boolean PMBandwidthIsViolated(PhysicalMachine pm) {
    	ArrayList<VirtualMachine> onlineVMs = pm.getOnlineVMs();
		double usedVMBandwidth = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMBandwidth += vm.getUsedBandwidth() * vm.getBandwidth();
		}
		if (usedVMBandwidth > pm.getBandwidth()) {
			return true;
		}
    	return false;
    }     
    

}
