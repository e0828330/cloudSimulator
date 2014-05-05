package utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
	 * Sorts the given list @vmList by priority descending (Highest priority first).
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
}
