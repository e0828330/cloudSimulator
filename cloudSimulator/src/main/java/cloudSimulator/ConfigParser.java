package cloudSimulator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VMType;
import model.VirtualMachine;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import simulation.DataCenter;
import utils.Utils;
import algorithms.DataCenterManagement;
import algorithms.DataCenterMigration;
import cloudSimulator.repo.DataCenterRepository;
import cloudSimulator.repo.PhysicalMachineRepository;
import cloudSimulator.repo.VirtualMachineRepository;
import cloudSimulator.weather.Forecast;
import cloudSimulator.weather.Location;

@Service
public class ConfigParser {

	static Logger logger = LoggerFactory.getLogger(ConfigParser.class);

	/* The ini file */
	private Ini ini;

	/* Helper */
	private ArrayList<PhysicalMachine> totalPMs = new ArrayList<PhysicalMachine>(64);
	private ArrayList<ServiceLevelAgreement> slaList;
	private ArrayList<VirtualMachine> vmList;

	@Autowired
	private ApplicationContext appContext;

	@Autowired
	private DataCenterRepository repo;

	@Autowired
	private VirtualMachineRepository vmRepo;

	@Autowired
	private PhysicalMachineRepository pmRepo;

	@Autowired
	private Forecast forecastService;

	/* Resulting datacenters */
	private List<DataCenter> dataCenters = new ArrayList<DataCenter>();

	private DataCenterMigration migrationAlgorithm;

	public List<DataCenter> getDataCenters() {
		return dataCenters;
	}

	public DataCenterMigration getMigrationAlgorithm() {
		return migrationAlgorithm;
	}

	private Long randomSeed;

	/**
	 * Returns the NormalDistribution of the mean and sd values given by @key in
	 * the config.ini file
	 * 
	 * @param key
	 * @return
	 */
	private NormalDistribution getDistribution(String key) {
		return new NormalDistribution(Double.parseDouble(ini.get(key, "mean")), Double.parseDouble(ini.get(key, "sd")));
	}

	/**
	 * Initializes the SLAs given by the config.ini file
	 * 
	 * @param numSLAs
	 */
	private void initSLAs(int numSLAs) {
		NormalDistribution slaSize = getDistribution("SLASize");
		NormalDistribution slaMemory = getDistribution("SLAMemory");
		NormalDistribution slaCpus = getDistribution("SLACpus");
		NormalDistribution slaBandwidth = getDistribution("SLABandwidth");
		NormalDistribution slaPriority = getDistribution("SLAPriority");
		NormalDistribution slaMaxDowntime = getDistribution("SLAMaxDowntime");

		for (int i = 0; i < numSLAs; i++) {
			ServiceLevelAgreement sla = new ServiceLevelAgreement();
			sla.setBandwidth((int) slaBandwidth.sample());
			sla.setCpus((int) slaCpus.sample() + 1);
			sla.setMemory((int) slaMemory.sample());
			sla.setSize((int) slaSize.sample() + 2);
			sla.setMaxDowntime(slaMaxDowntime.sample());
			int priority = Math.max(1, (int) slaPriority.sample());
			sla.setPriority(priority < 0 ? 0 : priority);
			slaList.add(sla);
		}
	}

	/**
	 * Initializes the VMs given by the config.ini file
	 * 
	 * @param numVMs
	 */
	private void initVMs(int web, int hpc, int mixed) {
		// VMS
		Iterator<ServiceLevelAgreement> iter = slaList.iterator();
		for (int i = 0; i < web; i++) {
			VirtualMachine vm = new VirtualMachine();
			vm.setType(VMType.WEB);

			if (!iter.hasNext()) {
				iter = slaList.iterator();
			}

			// if (iter.hasNext()) {
			ServiceLevelAgreement sla = iter.next();
			// iter.remove();
			sla.getVms().add(vm);
			vm.setSla(sla);

			vm.setBandwidth((int) (sla.getBandwidth()));
			int initCPUS = (int) (sla.getCpus());
			vm.setCpus(initCPUS < 1 ? 1 : initCPUS);
			int initMemory = (int) (sla.getMemory());
			vm.setMemory(initMemory < 1 ? 1 : initMemory);
			vm.setOnline(true);
			vm.setSize((int) (sla.getSize()));
			/*
			 * } else { vm.setOnline(false); }
			 */
			vm.buildLoadMaps();
			vmList.add(vm);
		}

		for (int i = 0; i < hpc; i++) {
			VirtualMachine vm = new VirtualMachine();
			vm.setType(VMType.HPC);

			if (!iter.hasNext()) {
				iter = slaList.iterator();
			}

			// if (iter.hasNext()) {
			ServiceLevelAgreement sla = iter.next();
			// iter.remove();
			sla.getVms().add(vm);
			vm.setSla(sla);

			vm.setBandwidth((int) (sla.getBandwidth()));
			int initCPUS = (int) (sla.getCpus());
			vm.setCpus(initCPUS < 1 ? 1 : initCPUS);
			int initMemory = (int) (sla.getMemory());
			vm.setMemory(initMemory < 1 ? 1 : initMemory);
			vm.setOnline(true);
			vm.setSize((int) (sla.getSize()));
			/*
			 * } else { vm.setOnline(false); }
			 */
			vm.buildLoadMaps();
			vmList.add(vm);
		}

		for (int i = 0; i < hpc; i++) {
			VirtualMachine vm = new VirtualMachine();
			vm.setType(VMType.MIXED);

			if (!iter.hasNext()) {
				iter = slaList.iterator();
			}

			// if (iter.hasNext()) {
			ServiceLevelAgreement sla = iter.next();
			// iter.remove();
			sla.getVms().add(vm);
			vm.setSla(sla);

			vm.setBandwidth((int) (sla.getBandwidth()));
			int initCPUS = (int) (sla.getCpus());
			vm.setCpus(initCPUS < 1 ? 1 : initCPUS);
			int initMemory = (int) (sla.getMemory());
			vm.setMemory(initMemory < 1 ? 1 : initMemory);
			vm.setOnline(true);
			vm.setSize((int) (sla.getSize()));
			/*
			 * } else { vm.setOnline(false); }
			 */
			vm.buildLoadMaps();
			vmList.add(vm);
		}
	}

	/**
	 * Initializes the PMs + Datacenters given by the config.ini file
	 */
	private void initPMs() {

		DataCenterManagement algorithm = (DataCenterManagement) appContext.getBean("management" + ini.get("Algorithms", "dataCenterManagement"));

		NormalDistribution cpuPowerND = getDistribution("CPUPower");
		NormalDistribution memPowerND = getDistribution("MemoryPower");
		NormalDistribution netPowerND = getDistribution("NetworkPower");

		NormalDistribution cpuCoresND = getDistribution("CPUCores");
		NormalDistribution memoryND = getDistribution("Memory");
		NormalDistribution bandwidthND = getDistribution("Bandwidth");
		NormalDistribution diskspaceND = getDistribution("Diskspace");

		NormalDistribution pmND = getDistribution("PMs");

		for (String key : ini.get("DataCenter").keySet()) {
			DataCenter dc = new DataCenter();
			String[] name = ini.get("DataCenter", key).split(",");
			dc.setName(name[0]);
			dc.setLocation(new Location(Double.parseDouble(name[1]), Double.parseDouble(name[2])));
			dc.setTimezoneOffset(Integer.parseInt(name[3]));
			dc.setEnergyPriceDay(Float.parseFloat(name[4]));
			dc.setEnergyPriceNight(Float.parseFloat(name[5]));
			dc.setAlgorithm(algorithm);

			// Phsyical Machines for DataCenter
			int numPMs = (int) pmND.sample();
			if (numPMs < 1)
				numPMs = 1;
			List<PhysicalMachine> pms = new ArrayList<PhysicalMachine>(numPMs);
			for (int i = 0; i < numPMs; i++) {
				PhysicalMachine pm = new PhysicalMachine();
				pm.setDataCenter(dc);
				pm.setRunning(false);
				pm.setCpus((int) cpuCoresND.sample());
				pm.setMemory((int) memoryND.sample());
				pm.setSize((int) diskspaceND.sample());
				pm.setBandwidth((int) bandwidthND.sample());

				pm.setCpuPowerConsumption((int) cpuPowerND.sample());
				pm.setMemPowerConsumption((int) memPowerND.sample());
				pm.setNetworkPowerConsumption((int) netPowerND.sample());
				pm.setIdleStateEnergyUtilization(0.1 * (pm.getCpuPowerConsumption() + pm.getMemPowerConsumption() + pm.getNetworkPowerConsumption()));
				pm.setVirtualMachines(new ArrayList<VirtualMachine>());

				// System.out.println(pm);
				pms.add(pm);
				totalPMs.add(pm);
			}
			dc.setPhysicalMachines(pms);
			dc.setForecastService(forecastService);
			dataCenters.add(dc);
		}
	}

	public Long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * Parses the config file to setup the cloud
	 * 
	 * @param path
	 *            Path to the config.ini file
	 * @throws InvalidFileFormatException
	 * @throws IOException
	 */
	public void doParse(String path) throws InvalidFileFormatException, IOException {
		ini = new Ini(new File(path));

		// Migration algorithm
		migrationAlgorithm = (DataCenterMigration) appContext.getBean("migration" + ini.get("Algorithms", "dataCenterMigration"));

		if (Long.parseLong(ini.get("Random", "seed")) > 0) {
			randomSeed = Long.parseLong(ini.get("Random", "seed"));
		} else {
			randomSeed = System.currentTimeMillis();
		}

		if (ini.get("DataSource", "useDatabase").equals("1")) {
			loadFromDB();
			return;
		} else {
			logger.info("Generating new random config");
		}

		NormalDistribution vmND = getDistribution("VMs");
		NormalDistribution slaND = getDistribution("SLAs");

		int numSLAs = (int) slaND.sample();
		int numVMs = (int) vmND.sample();

		int webVMPercent = Integer.parseInt(ini.get("VMTypes", "web"));
		int hpcPercent = Integer.parseInt(ini.get("VMTypes", "hpc"));
		
		int webVmNum = (int)(numVMs * (double)(webVMPercent / 100.));
		int hpcVmNum = (int)(numVMs * (double)(hpcPercent) / 100.);
		int mixedVms = numVMs - webVmNum - hpcVmNum;

		// If generated data generates more SLAs than VMs, set max to VMs
		if (numSLAs > numVMs) {
			numSLAs = numVMs;
		}

		slaList = new ArrayList<ServiceLevelAgreement>(numSLAs);
		vmList = new ArrayList<VirtualMachine>(numVMs);

		// SLAs
		this.initSLAs(numSLAs);

		// VMs
		this.initVMs(webVmNum, hpcVmNum, mixedVms);

		// PMS
		this.initPMs();

		// Order VMs by priority
		Utils.orderVMsByPriorityDescending(vmList);

		// Assignment
		this.assignVM2PM();

		// Process last VMs if no PMs can take them
		for (VirtualMachine vm : vmList) {
			vm.setOnline(false);
		}

		saveToDB();
		
		// printInitialAllocation();
	}

	/**
	 * Rebuilds the datacenter list from the database
	 */
	private void loadFromDB() {
		DataCenterManagement algorithm = (DataCenterManagement) appContext.getBean("management" + ini.get("Algorithms", "dataCenterManagement"));
		dataCenters = repo.findAll();
		for (DataCenter dc : dataCenters) {
			dc.setAlgorithm(algorithm);
			dc.setForecastService(forecastService);
			for (PhysicalMachine pm : dc.getPhysicalMachines()) {
				pm.setDataCenter(dc);
				for (VirtualMachine vm : pm.getVirtualMachines()) {
					vm.setPm(pm);
					if (vm.getSla() != null) {
						vm.getSla().getVms().add(vm);
					}
				}
			}
		}

		logger.info("Loaded data from database.");
	}

	/**
	 * Saves the datacenter list to the database
	 */
	private void saveToDB() {
		repo.deleteAll();
		pmRepo.deleteAll();
		vmRepo.deleteAll();

		for (DataCenter dc : dataCenters) {
			for (PhysicalMachine pm : dc.getPhysicalMachines()) {
				for (VirtualMachine vm : pm.getVirtualMachines()) {
					vmRepo.save(vm);
				}
				pmRepo.save(pm);
			}
			repo.save(dc);
		}
		logger.info("Saved data to database.");
	}

	/**
	 * Assigns the virtual machines (VMs) to the physical machines (PMs)
	 */
	private void assignVM2PM() {
		// Set VM -> PM
		// Shuffle PMs to avoid clustered allocation

		logger.info("NR PMS " + totalPMs.size());
		logger.info("NR VMs " + vmList.size());
		Collections.shuffle(totalPMs);

		//Iterator<PhysicalMachine> PMiter = totalPMs.iterator();
		Iterator<VirtualMachine> VMiter = vmList.iterator();

		VirtualMachine nextVM = null;

		int i = 0;
		int mod = totalPMs.size();
		boolean pmFound = false;
		while (VMiter.hasNext()) {
			if (nextVM == null) {
				nextVM = VMiter.next();
			}
			pmFound = false;
			for (PhysicalMachine pm : totalPMs) {
				if (Utils.VMfitsOnPM(pm, nextVM)) {
					if (pm.getVirtualMachines().size() == 0 && nextVM.isOnline()) {
						pm.setRunning(true);
					}
					nextVM.setPm(pm);
					pm.getVirtualMachines().add(nextVM);
					VMiter.remove();
					nextVM = null;
					pmFound = true;
					break;
				}
			}
			if (pmFound) {
				continue;
			}
			// no pm found
			PhysicalMachine tmpPM = totalPMs.get(i % mod);
			nextVM.setPm(tmpPM);
			tmpPM.getVirtualMachines().add(nextVM);
			nextVM.setOnline(false);	
			VMiter.remove();
			nextVM = null;
			i++;
		}
	}

	/**
	 * Prints the assignment of each PM with relating VMs
	 */
	public void printInitialAllocation() {
		for (DataCenter dc : dataCenters) {
			int i = 1;
			for (PhysicalMachine pm : dc.getPhysicalMachines()) {
				System.out.println(dc.getName() + ": PM " + i + " with " + pm.getVirtualMachines().size() + " VMs, isRunning = " + pm.isRunning());
				System.out.println("DATA: Size = " + pm.getSize() + ", Memory = " + pm.getMemory() + ", CPUS = " + pm.getCpus());
				for (VirtualMachine v : pm.getVirtualMachines()) {
					System.out.println("VM has size " + v.getSize() + ", cpus = " + v.getCpus() + ", memory = " + v.getMemory() + ", online: " + v.isOnline());
				}
				i++;
			}

		}
	}
}
