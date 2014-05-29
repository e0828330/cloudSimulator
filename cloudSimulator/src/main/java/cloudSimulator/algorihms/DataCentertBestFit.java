package cloudSimulator.algorihms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import lombok.Data;
import model.PhysicalMachine;
import model.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import simulation.DataCenter;
import utils.Utils;
import algorithms.DataCenterManagement;

@Service(value = "managementBestFit")
@Data
public class DataCentertBestFit implements DataCenterManagement {

	static Logger logger = LoggerFactory.getLogger(DataCentertBestFit.class);

	private double threshold = 1.;

	public void scaleVirtualMachines(DataCenter dc) {

		scaleDC(dc, threshold);

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

	/**
	 * Scales the Datacenter @dc First we get the total resources used in the dc
	 * and calculate the percentage. If the percentage is bigger than the
	 * @threshold, we get all online VMs running and sort them by the priority
	 * (lowest first). Then we remove one VM after another, until we drop under @threshold
	 * with the dc utilization. After this, we look if we can again, set VMs
	 * which are offline to running.
	 * 
	 * @param dc
	 * @param threshold
	 */
	private void scaleDC(DataCenter dc, double threshold) {
		System.out.println("_____________________________________________");
		System.out.println(dc.getName());
		System.out.println("Online PMs: " + dc.getOnlinePMs().size());
		System.out.println("Offline PMs: " + dc.getOfflinePMs().size());
		ArrayList<VirtualMachine> totalOnlineVMList = new ArrayList<VirtualMachine>(
				128);
		ArrayList<VirtualMachine> totalOfflineVMList = new ArrayList<VirtualMachine>(
				128);

		double dcTotalMemory = 0.;
		double dcTotalCPUs = 0.;
		double dcTotalBandwidth = 0.;

		double dcUsedMemory = 0.;
		double dcUsedCPUs = 0.;
		double dcUsedBandwidth = 0.;

		// Store the ressources needed by the datacenter
		for (PhysicalMachine pm : dc.getPhysicalMachines()) {
			dcTotalMemory += pm.getMemory();
			dcTotalCPUs += pm.getCpus();
			dcTotalBandwidth += pm.getBandwidth();

			if (pm.isRunning()) {
				dcUsedMemory += pm.getMemory() * pm.getMemoryUsage();
				dcUsedCPUs += pm.getCpus() * pm.getCPULoad();
				dcUsedBandwidth += pm.getBandwidth()
						* pm.getBandwidthUtilization();
			}

			totalOnlineVMList.addAll(pm.getOnlineVMs());
			totalOfflineVMList.addAll(pm.getOfflineVMs());
		}
		


		Utils.orderVMsByPriorityAscending(totalOnlineVMList);
		int size = totalOnlineVMList.size();

		
		System.out.println("Offline vms total: " + totalOfflineVMList.size());
		System.out.println("Online vms total: " + totalOnlineVMList.size());
		while (dcUsedBandwidth / dcTotalBandwidth > threshold
				|| dcUsedCPUs / dcTotalCPUs > threshold
				|| dcUsedMemory / dcTotalMemory > threshold) {

			if (size == 0)
				break;

			VirtualMachine vm = totalOnlineVMList.remove(0);
			vm.setOnline(false);
			dcUsedBandwidth -= vm.getBandwidth() * vm.getUsedBandwidth();
			dcUsedCPUs -= vm.getCpus() * vm.getUsedCPUs();
			dcUsedMemory -= vm.getMemory() * vm.getUsedMemory();
			size--;
			System.out.println("Datacenter intern: Set VM running false.");
		}

		// Turn on offline vms if they fit
		size = totalOfflineVMList.size();
		Utils.orderVMsByPriorityDescending(totalOfflineVMList);

		while (dcUsedBandwidth / dcTotalBandwidth <= threshold
				|| dcUsedCPUs / dcTotalCPUs <= threshold
				|| dcUsedMemory / dcTotalMemory <= threshold) {

			if (size == 0)
				break;

			VirtualMachine vm = totalOfflineVMList.remove(0);

			double nextVMBandwidth = vm.getBandwidth() * vm.getUsedBandwidth();
			double nextVMCPUs = vm.getCpus() * vm.getUsedCPUs();
			double nextVMMemory = vm.getMemory() * vm.getUsedMemory();

			if ((nextVMBandwidth + dcUsedBandwidth) / dcTotalBandwidth <= threshold
					&& (nextVMCPUs + dcUsedCPUs) / dcTotalCPUs <= threshold
					&& (nextVMMemory + dcUsedMemory) / dcTotalMemory <= threshold) {
				if (vm.getPm().isRunning() == false) {
					vm.getPm().setRunning(true);
				}
				vm.setOnline(true);
				System.out.println("Datacenter intern: Set VM running true.");

			}
			dcUsedBandwidth += nextVMBandwidth;
			dcUsedCPUs += nextVMCPUs;
			dcUsedMemory += nextVMMemory;
			size--;
		}

		// Check if PMs are running with no VMs
		for (PhysicalMachine pm : dc.getPhysicalMachines()) {
			if (pm.getOnlineVMs().size() == 0) {
				pm.setRunning(false);
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

		logger.debug("Needed Bandwidth: " + usedVMBandwidth + " but available "
				+ pm.getBandwidth());

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
				if (Utils.getFutureEnergyConsumption(pm, vm) < Utils
						.getFutureEnergyConsumption(result, vm)) {
					result = pm;
					continue;
				}
			}
		}

		/* Does not fit anywhere just give the lowest power consumption */
		if (result == null) {
			for (PhysicalMachine pm : dc.getPhysicalMachines()) {
				if (result == null) {
					result = pm;
					continue;
				}
				if (Utils.getFutureEnergyConsumption(pm, vm) < Utils
						.getFutureEnergyConsumption(result, vm)) {
					result = pm;
				}
			}
		}

		/*
		 * if (result == null) { // Shut down one VM with lower prio
		 * ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> map =
		 * dc.getPMWithLowerPriorityVMList(dc, vm); if (map == null) { return
		 * null; } else { // should only have 1 iteration // set all lower
		 * priority vms offline and then return pm for (Entry<PhysicalMachine,
		 * ArrayList<VirtualMachine>> entry : map.entrySet()) { for
		 * (VirtualMachine tmpVM : entry.getValue()) { tmpVM.setOnline(false); }
		 * return entry.getKey(); } } }
		 */
		return result;
	}
}
