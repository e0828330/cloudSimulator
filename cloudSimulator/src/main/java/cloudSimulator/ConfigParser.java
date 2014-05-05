package cloudSimulator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import simulation.DataCenter;
import utils.Utils;

public class ConfigParser {

	/* The ini file */
	private Ini ini;
	
	/* Helper */
	private ArrayList<PhysicalMachine> totalPMs = new ArrayList<PhysicalMachine>(64);
	
	private ArrayList<ServiceLevelAgreement> slaList;
	private ArrayList<VirtualMachine> vmList;	

	/* Resulting datacenters */
	private List<DataCenter> dataCenters = new ArrayList<DataCenter>();

	private final double INITIAL_VM_RES_MULTIPLICATOR = 0.8;

	private NormalDistribution getDistribution(String key) {
		return new NormalDistribution(Double.parseDouble(ini.get(key, "mean")), Double.parseDouble(ini.get(key, "sd")));
	}

	public void doParse(String path) throws InvalidFileFormatException, IOException {
		ini = new Ini(new File(path));

		NormalDistribution pmND = getDistribution("PMs");
		NormalDistribution vmND = getDistribution("VMs");
		NormalDistribution slaND = getDistribution("SLAs");

		NormalDistribution cpuPowerND = getDistribution("CPUPower");
		NormalDistribution memPowerND = getDistribution("MemoryPower");
		NormalDistribution netPowerND = getDistribution("NetworkPower");

		NormalDistribution cpuCoresND = getDistribution("CPUCores");
		NormalDistribution memoryND = getDistribution("Memory");
		NormalDistribution bandwithND = getDistribution("Bandwidth");
		NormalDistribution diskspaceND = getDistribution("Diskspace");

		int numSLAs = (int) slaND.sample();
		int numVMs = (int) vmND.sample();

		// If generated data generates more SLAs than VMs, set max to VMs
		if (numSLAs > numVMs) {
			numSLAs = numVMs;
		}

		slaList = new ArrayList<ServiceLevelAgreement>(numSLAs);
		vmList = new ArrayList<VirtualMachine>(numVMs);

		// SLAs

		NormalDistribution slaSize = getDistribution("SLASize");
		NormalDistribution slaMemory = getDistribution("SLAMemory");
		NormalDistribution slaCpus = getDistribution("SLACpus");
		NormalDistribution slaBandwidth = getDistribution("SLABandwidth");
		NormalDistribution slaPriority = getDistribution("SLAPriority");
		NormalDistribution slaMaxDowntime = getDistribution("SLAMaxDowntime");

		for (int i = 0; i < numSLAs; i++) {
			ServiceLevelAgreement sla = new ServiceLevelAgreement();
			sla.setBandwith((int) slaBandwidth.sample());
			sla.setCpus((int) slaCpus.sample() + 1);
			sla.setMemory((int) slaMemory.sample());
			sla.setSize((int) slaSize.sample() + 2);
			sla.setMaxDowntime(slaMaxDowntime.sample());
			int priority = (int) slaPriority.sample();
			sla.setPriority(priority < 0 ? 0 : priority);
			slaList.add(sla);
		}

		// VMS
		Iterator<ServiceLevelAgreement> iter = slaList.iterator();
		for (int i = 0; i < numVMs; i++) {
			VirtualMachine vm = new VirtualMachine();

			if (iter.hasNext()) {
				ServiceLevelAgreement sla = iter.next();
				iter.remove();
				sla.getVms().add(vm);
				vm.setSla(sla);
				vm.setBandwidth((int) (sla.getBandwith() * INITIAL_VM_RES_MULTIPLICATOR));
				int initCPUS = (int) (sla.getCpus() * INITIAL_VM_RES_MULTIPLICATOR);
				vm.setCpus(initCPUS < 1 ? 1 : initCPUS);
				int initMemory = (int) (sla.getMemory() * INITIAL_VM_RES_MULTIPLICATOR);
				vm.setMemory(initMemory < 1 ? 1 : initMemory);
				vm.setOnline(true);
				vm.setSize((int) (sla.getSize() * INITIAL_VM_RES_MULTIPLICATOR));
			} else {
				vm.setOnline(false);
			}
			vmList.add(vm);
		}

		// PMS
		for (String key : ini.get("DataCenter").keySet()) {
			DataCenter dc = new DataCenter();
			dc.setName(ini.get("DataCenter", key));

			// Phsyical Machines for DataCenter
			int numPMs = (int) pmND.sample();
			List<PhysicalMachine> pms = new ArrayList<PhysicalMachine>(numPMs);
			for (int i = 0; i < numPMs; i++) {
				PhysicalMachine pm = new PhysicalMachine();
				pm.setCpus((int) cpuCoresND.sample());
				pm.setMemory((int) memoryND.sample());
				pm.setSize((int) diskspaceND.sample());
				pm.setBandwith((int) bandwithND.sample());

				pm.setCpuPowerConsumption((int) cpuPowerND.sample());
				pm.setMemPowerConsumption((int) memPowerND.sample());
				pm.setNetworkPowerConsumption((int) netPowerND.sample());
				pm.setIdleStateEnergyUtilization(0.1 * (pm.getCpuPowerConsumption() + pm.getMemPowerConsumption() + pm.getNetworkPowerConsumption()));

				// System.out.println(pm);
				pms.add(pm);
				totalPMs.add(pm);
			}
			dc.setPhysicalMachines(pms);
			dataCenters.add(dc);
		}

		// Order VMs by priority
		Utils.orderVMsByPriority(vmList);
		
		// Set VM -> PM
		// Shuffle PMs
		Collections.shuffle(totalPMs);
		
		Iterator<PhysicalMachine> PMiter = totalPMs.iterator();
		Iterator<VirtualMachine> VMiter = vmList.iterator();
		
		VirtualMachine nextVM = null;
		int j = 0;
		while (PMiter.hasNext()) {
			PhysicalMachine pm = PMiter.next();
			j++;
			while (nextVM != null || VMiter.hasNext()) {
				if (nextVM == null) {
					nextVM = VMiter.next();
					nextVM.updateLoad();
				}
				if (Utils.VMfits2PM(pm, nextVM)) {
					Utils.migrateVM2PM(pm, nextVM);
					VMiter.remove();
					nextVM = null;
				}
				else {
					break;
				}
			}		
		}
		
		/*
		System.out.println(vmList.size() + " VMs not migrated to PMs");
		for (DataCenter dc : dataCenters) {
			int i = 1;
			for (PhysicalMachine pm : dc.getPhysicalMachines()) {
				System.out.println(dc.getName() + ": PM " + i + " with " + pm.getVirtualMachines().size() + " VMs");
				System.out.println("DATA: Size = " + pm.getSize() +", Memory = " + pm.getMemory() + ", CPUS = " + pm.getCpus());
				for (VirtualMachine v : pm.getVirtualMachines()) {
					System.out.println("VM has size " + v.getSize() + ", cpus = " + v.getCpus() +", memory = " + v.getMemory());
				}
				i++;
			}
			
		}
		*/

		
	}
}
