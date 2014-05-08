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
		ArrayList<PhysicalMachine> pm_offline = new ArrayList<PhysicalMachine>();
		ArrayList<PhysicalMachine> pm_online = new ArrayList<PhysicalMachine>();
		/*
		 * System.out.println(dc.getName()); // TODO Auto-generated method stub
		 * for(PhysicalMachine pm : dc.getPhysicalMachines()) {
		 * System.out.println(pm.getIdleStateEnergyUtilization() + " / " +
		 * pm.getPowerConsumption() + ": VMs Online: " +
		 * pm.getOnlineVMs().size()); }
		 * System.out.println("______________________________________\n");
		 */

		System.out.println(dc.getName());
		for (PhysicalMachine pm : dc.getPhysicalMachines()) {
			if (pm.isRunning()) {
				pm_online.add(pm);
			} else {
				pm_offline.add(pm);
			}

			double usedCPU = 0.0;
			double usedMem = 0.0;
			double usedBandwidth = 0.0;

			System.out.println("Bandwidth = " + pm.getBandwithUtilization() * 100);
			System.out.println("CPU = " + pm.getCPULoad() * 100);
			System.out.println("Memory = " + pm.getMemoryUsage() * 100);
			System.out.println("Number of CPUS = " + pm.getCpus());

			// if CPU load of PM is 100%
			if (pm.getCPULoad() >= 1.) {

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
					System.out.println("VM Nr. " + (i++) + " usedCPUS = " + vm.getUsedCPUs() + ", NrCpus= " + vm.getCpus());
				}
				System.out.println("Needed: " + usedVMCPUs);

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
						// Remove from physcial machine
						pm.getVirtualMachines().remove(migVM);
						migVM.setOnline(false);
						// TODO: Calculate time
						dc.queueAddVirtualMachine(migVM, Utils.getMigrationTime(pm.getBandwith() * (1. - pm.getBandwithUtilization()), migVM.getSize()));
					}

				}

				System.out.println("New needed = " + usedVMCPUs + " / " + pm.getCpus());
				System.out.println("Migrated VMs = " + migrationList.size());

			}
			System.out.println("###");

		}
		System.out.println("______________________________________\n");
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
			if (Utils.VMfitsOnPM(pm, vm) && pm.getPowerConsumption() < result.getPowerConsumption()) {
				result = pm;
			}
		}

		if (result == null) {
			// TODO: Shut down one VM with lower prio
		}

		return result;
	}

}
