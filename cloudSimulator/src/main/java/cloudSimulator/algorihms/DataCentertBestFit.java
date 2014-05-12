package cloudSimulator.algorihms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import model.PhysicalMachine;
import model.VirtualMachine;

import org.springframework.stereotype.Service;

import simulation.DataCenter;
import utils.Utils;
import algorithms.DataCenterManagement;

@Service(value = "managementBestFit")
public class DataCentertBestFit implements DataCenterManagement {

	public void scaleVirtualMachines(DataCenter dc) {

		for (PhysicalMachine pm : dc.getPhysicalMachines()) {
			/*
			 * System.out.println(dc.getName());
			 * System.out.println("Bandwidth = " + pm.getBandwidthUtilization()
			 * 100); System.out.println("CPU = " + pm.getCPULoad() * 100);
			 * System.out.println("Memory = " + pm.getMemoryUsage() * 100);
			 * System.out.println("Number of CPUS = " + pm.getCpus());
			 */

			// First migrate by Memory, because swapping is painful
			// if Memory usage of PM is 100%

			if (pm.getMemoryUsage() >= 1.) {
				System.out.println("Memory Usage > 100% in DC " + dc.getName());
				this.migrationByMemoryUsage(pm, dc);
			}

			// if CPU load of PM is 100%

			if (pm.getCPULoad() >= 1.) {
				System.out.println("CPU Load > 100% in DC " + dc.getName());
				this.migrationByCPULoad(pm, dc);
			}

			// if Bandwidth load of PM is 100%

			if (pm.getBandwidthUtilization() >= 1.) {
				System.out.println("Bandwidth > 100% in DC " + dc.getName());
				this.migrationByBandwidthUsage(pm, dc);
			}
			// System.out.println("###");

		}
		// System.out.println("______________________________________\n");
	}

	private void migrationByBandwidthUsage(PhysicalMachine pm, DataCenter dc) {
		ArrayList<VirtualMachine> migrationList = new ArrayList<VirtualMachine>();

		// Sort by bandwidth usage ascending
		ArrayList<VirtualMachine> onlineVMs = pm.getOnlineVMs();
		Collections.sort(onlineVMs, new Comparator<VirtualMachine>() {
			public int compare(VirtualMachine vm1, VirtualMachine vm2) {
				return (int) ((vm1.getUsedBandwidth() - vm2.getUsedBandwidth()) * 100);

			}
		});

		int i = 1;
		double usedVMBandwidth = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMBandwidth += vm.getUsedBandwidth() * vm.getBandwidth();

			/*
			 * System.out.println("VM Nr. " + (i++) + " usedBandwidth = " +
			 * vm.getUsedBandwidth() + ", Bandwidth-Total= " +
			 * vm.getBandwidth());
			 */
		}

		// System.out.println("Needed: " + usedVMBandwidth);
		
		 System.out.println("Needed Bandwidth: " + usedVMBandwidth + " but available " + pm.getBandwidth());

		// more bandwidth needed than available on PM
		if (usedVMBandwidth > pm.getBandwidth()) {
			for (VirtualMachine vm : onlineVMs) {

				usedVMBandwidth -= vm.getUsedBandwidth() * vm.getBandwidth();
				migrationList.add(vm);
				if (usedVMBandwidth <= pm.getBandwidth()) {
					break;
				}
			}

			// Migrate
			for (VirtualMachine migVM : migrationList) {
				// Remove from physical machine
				pm.getVirtualMachines().remove(migVM);
				migVM.setOnline(false);
				dc.queueAddVirtualMachine(migVM,
						Utils.getMigrationTime(
								pm.getBandwidth()
										* (1. - pm.getBandwidthUtilization()),
								migVM.getSize() + migVM.getMemory() * migVM.getUsedMemory()));
			}

		}

		/*
		 * System.out.println("New needed = " + usedVMBandwidth + " / " +
		 * pm.getBandwidth()); System.out.println("Migrated VMs = " +
		 * migrationList.size());
		 */
		
		System.out.println("Migrated VMs = " + migrationList.size());

	}

	private void migrationByMemoryUsage(PhysicalMachine pm, DataCenter dc) {
		ArrayList<VirtualMachine> migrationList = new ArrayList<VirtualMachine>();

		// Sort by memory usage ascending
		ArrayList<VirtualMachine> onlineVMs = pm.getOnlineVMs();
		Collections.sort(onlineVMs, new Comparator<VirtualMachine>() {
			public int compare(VirtualMachine vm1, VirtualMachine vm2) {
				return (int) ((vm1.getUsedMemory() - vm2.getUsedMemory()) * 100);

			}
		});

		int i = 1;
		double usedVMMemory = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMMemory += vm.getUsedMemory() * vm.getMemory();
			/*
			 * System.out.println("VM Nr. " + (i++) + " usedMemory = " +
			 * vm.getUsedMemory() + ", Memory-Total= " + vm.getMemory());
			 */
		}

		// System.out.println("Needed: " + usedVMMemory);
		
		System.out.println("Needed Memory: " + usedVMMemory + " but available " + pm.getMemory());

		// more memory needed than available on PM
		if (usedVMMemory > pm.getMemory()) {
			for (VirtualMachine vm : onlineVMs) {

				usedVMMemory -= vm.getUsedMemory() * vm.getMemory();
				migrationList.add(vm);
				if (usedVMMemory <= pm.getMemory()) {
					break;
				}
			}

			// Migrate
			for (VirtualMachine migVM : migrationList) {
				// Remove from physical machine
				pm.getVirtualMachines().remove(migVM);
				migVM.setOnline(false);
				dc.queueAddVirtualMachine(migVM,
						Utils.getMigrationTime(
								pm.getBandwidth()
										* (1. - pm.getBandwidthUtilization()),
								migVM.getSize() + migVM.getMemory() * migVM.getUsedMemory()));
			}

		}

		/*
		 * System.out.println("New needed = " + usedVMMemory + " / " +
		 * pm.getMemory()); System.out.println("Migrated VMs = " +
		 * migrationList.size());
		 */
		
		System.out.println("Migrated VMs = " + migrationList.size());

	}

	private void migrationByCPULoad(PhysicalMachine pm, DataCenter dc) {
		ArrayList<VirtualMachine> migrationList = new ArrayList<VirtualMachine>();

		// Sort by usedCpus ascending
		ArrayList<VirtualMachine> onlineVMs = pm.getOnlineVMs();
		Collections.sort(onlineVMs, new Comparator<VirtualMachine>() {
			public int compare(VirtualMachine vm1, VirtualMachine vm2) {
				return (int) ((vm1.getUsedCPUs() - vm2.getUsedCPUs()) * 100);

			}
		});

		int i = 1;
		double usedVMCPUs = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMCPUs += vm.getUsedCPUs() * vm.getCpus();
			/*
			 * System.out.println("VM Nr. " + (i++) + " usedCPUS = " +
			 * vm.getUsedCPUs() + ", NrCpus= " + vm.getCpus());
			 */
		}
		 System.out.println("Needed CPUs: " + usedVMCPUs + " but available " + pm.getCpus());

		// more cpus needed than available on PM
		if (usedVMCPUs > pm.getCpus()) {
			for (VirtualMachine vm : onlineVMs) {

				usedVMCPUs -= vm.getUsedCPUs() * vm.getCpus();
				migrationList.add(vm);
				if (usedVMCPUs <= pm.getCpus()) {
					break;
				}
			}

			// Migrate
			for (VirtualMachine migVM : migrationList) {
				// Remove from physical machine
				pm.getVirtualMachines().remove(migVM);
				migVM.setOnline(false);
				dc.queueAddVirtualMachine(migVM,
						Utils.getMigrationTime(
								pm.getBandwidth()
										* (1. - pm.getBandwidthUtilization()),
								migVM.getSize() + migVM.getMemory() * migVM.getUsedMemory()));
			}

		}

		/*
		 * System.out.println("New needed = " + usedVMCPUs + " / " +
		 * pm.getCpus()); System.out.println("Migrated VMs = " +
		 * migrationList.size());
		 */
		
		System.out.println("Migrated VMs = " + migrationList.size());
	}

	/**
	 * Finds the PM where the VM fits and has the lowest power consumption
	 * 
	 */
	public PhysicalMachine findPMForMigration(DataCenter dc, VirtualMachine vm) {
		PhysicalMachine result = null;

		for (PhysicalMachine pm : dc.getPhysicalMachines()) {
			if (result == null && Utils.VMfitsOnPM(pm, vm)) {
				result = pm;
				continue;
			}
			if (Utils.VMfitsOnPM(pm, vm)
					&& pm.getPowerConsumption() < result.getPowerConsumption()) {
				result = pm;
			}
		}

		if (result == null) {
			// TODO: Shut down one VM with lower prio
		}

		return result;
	}

}
