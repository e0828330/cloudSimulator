package utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import model.PhysicalMachine;
import model.VirtualMachine;

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
	 * Migrates @vm to @pm
	 * 
	 * @param pm
	 *            The physical machine
	 * @param vm
	 *            The virtual machine
	 */
	public static void migrateVM2PM(PhysicalMachine pm, VirtualMachine vm) {
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
	 * Calculates the time for transmitting a file of @size by bandwidth @availableBandwidth
	 * 
	 * @param bandwidth
	 *            in Mbit/s
	 * @param size
	 *            in GB
	 * @return Time for migration in minutes
	 */
	public static int getMigrationTime(double availableBandwidth, int size) {
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

}
