package cloudSimulator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.PhysicalMachine;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import simulation.DataCenter;

public class ConfigParser {
	
	/* The ini file */
	private Ini ini;
	
	/* Resulting datacenters */
	private List<DataCenter> dataCenters = new ArrayList<DataCenter>();
	
	public List<DataCenter> getDataCenters() {
		return dataCenters;
	}
	
	public void doParse(String path) throws InvalidFileFormatException, IOException {
		ini = new Ini(new File(path));
		
		NormalDistribution pmND = new NormalDistribution(Double.parseDouble(ini.get("PMs", "mean")), Double.parseDouble(ini.get("PMs", "sd")));
		NormalDistribution vmND = new NormalDistribution(Double.parseDouble(ini.get("VMs", "mean")), Double.parseDouble(ini.get("VMs", "sd")));

		NormalDistribution cpuPowerND = new NormalDistribution(Double.parseDouble(ini.get("CPUPower", "mean")), Double.parseDouble(ini.get("CPUPower", "sd")));
		NormalDistribution memPowerND = new NormalDistribution(Double.parseDouble(ini.get("MemoryPower", "mean")), Double.parseDouble(ini.get("MemoryPower", "sd")));
		NormalDistribution netPowerND = new NormalDistribution(Double.parseDouble(ini.get("NetworkPower", "mean")), Double.parseDouble(ini.get("NetworkPower", "sd")));

		NormalDistribution cpuCoresND = new NormalDistribution(Double.parseDouble(ini.get("CPUCores", "mean")), Double.parseDouble(ini.get("CPUCores", "sd")));
		NormalDistribution memoryND = new NormalDistribution(Double.parseDouble(ini.get("Memory", "mean")), Double.parseDouble(ini.get("Memory", "sd")));
		NormalDistribution bandwithND = new NormalDistribution(Double.parseDouble(ini.get("Bandwidth", "mean")), Double.parseDouble(ini.get("Bandwidth", "sd")));
		NormalDistribution diskspaceND = new NormalDistribution(Double.parseDouble(ini.get("Diskspace", "mean")), Double.parseDouble(ini.get("Diskspace", "sd")));

		
		for (String key : ini.get("DataCenter").keySet()) {
			DataCenter dc = new DataCenter();
			dc.setName(ini.get("DataCenter", key));
			
			// Phsyical Machines for DataCenter
			int numPMs = (int) pmND.sample();
			List <PhysicalMachine> pms = new ArrayList<PhysicalMachine>();
			for (int i = 0; i < numPMs; i++) {
				PhysicalMachine pm = new PhysicalMachine();
				pm.setCpus((int)cpuCoresND.sample());
				pm.setMemory((int)memoryND.sample());
				pm.setSize((int)diskspaceND.sample());
				pm.setBandwith((int)bandwithND.sample());
		
				pm.setCpuPowerConsumption((int)cpuPowerND.sample());
				pm.setMemPowerConsumption((int)memPowerND.sample());
				pm.setNetworkPowerConsumption((int)netPowerND.sample());
				pm.setIdleStateEnergyUtilization(0.1 * (pm.getCpuPowerConsumption() + pm.getMemPowerConsumption() + pm.getNetworkPowerConsumption()));

				System.out.println(pm);
				pms.add(pm);
			}
			dc.setPhysicalMachines(pms);
			
	
			dataCenters.add(dc);
		}
	}
}
