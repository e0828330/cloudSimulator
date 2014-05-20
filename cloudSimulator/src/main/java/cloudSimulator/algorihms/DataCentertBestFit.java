package cloudSimulator.algorihms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import model.PhysicalMachine;
import model.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import simulation.DataCenter;
import utils.Utils;
import algorithms.DataCenterManagement;

@Service(value = "managementBestFit")
public class DataCentertBestFit implements DataCenterManagement {

	static Logger logger = LoggerFactory.getLogger(DataCentertBestFit.class);
	
	public void scaleVirtualMachines(DataCenter dc) {

		for (PhysicalMachine pm : dc.getPhysicalMachines()) {
			// First migrate by Memory, because swapping is painful
			// if Memory usage of PM is 100%
			if (pm.getMemoryUsage() >= 1.) {
				logger.debug("Memory Usage > 100% in DC " + dc.getName());
				this.migrationByMemoryUsage(pm, dc);
			}

			// if CPU load of PM is 100%
			if (pm.getCPULoad() >= 1.) {
				logger.debug("CPU Load > 100% in DC " + dc.getName());
				this.migrationByCPULoad(pm, dc);
			}

			// if Bandwidth load of PM is 100%
			if (pm.getBandwidthUtilization() >= 1.) {
				logger.debug("Bandwidth > 100% in DC " + dc.getName());
				this.migrationByBandwidthUsage(pm, dc);
			}
		}
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

		double usedVMBandwidth = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMBandwidth += vm.getUsedBandwidth() * vm.getBandwidth();
		}

		logger.debug("Needed Bandwidth: " + usedVMBandwidth
				+ " but available " + pm.getBandwidth());

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
								migVM.getSize() + migVM.getMemory()
										* migVM.getUsedMemory()));
			}

		}

		logger.debug("Migrated VMs = " + migrationList.size());

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

		double usedVMMemory = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMMemory += vm.getUsedMemory() * vm.getMemory();
		}

		logger.debug("Needed Memory: " + usedVMMemory + " but available "
				+ pm.getMemory());

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
								migVM.getSize() + migVM.getMemory()
										* migVM.getUsedMemory()));
			}

		}
		logger.debug("Migrated VMs = " + migrationList.size());
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

		double usedVMCPUs = 0.;
		for (VirtualMachine vm : onlineVMs) {
			usedVMCPUs += vm.getUsedCPUs() * vm.getCpus();
		}
		logger.debug("Needed CPUs: " + usedVMCPUs + " but available "
				+ pm.getCpus());

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
								migVM.getSize() + migVM.getMemory()
										* migVM.getUsedMemory()));
			}

		}
		logger.debug("Migrated VMs = " + migrationList.size());
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
			if (Utils.VMfitsOnPM(pm, vm)) {				
				if (Utils.getFutureEnergyConsumption(pm, vm) < Utils.getFutureEnergyConsumption(result, vm)) {
					result = pm;
					continue;
				}
			}
		}
		if (result == null) {
			// TODO: Shut down one VM with lower prio
			ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> map = dc.getPMWithLowerPriorityVMList(dc, vm);
			if (map == null) {
				return null;
			}
			else {
				// should only have 1 iteration
				// set all lower priority vms offline and then return pm
				for (Entry<PhysicalMachine, ArrayList<VirtualMachine>> entry : map.entrySet()) {
					for (VirtualMachine tmpVM : entry.getValue()) {
						tmpVM.setOnline(false);
					}
					return entry.getKey();
				}
			}
		}
		return result;
	}
}
