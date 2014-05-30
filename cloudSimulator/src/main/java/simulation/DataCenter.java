package simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import utils.Utils;
import algorithms.DataCenterManagement;
import cloudSimulator.weather.Forecast;
import cloudSimulator.weather.Location;
import cloudSimulator.weather.Weather;

@Data
public class DataCenter implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7769086177008294418L;

	@Id
	private String id;
	
	private String name;
    private List<PhysicalMachine> physicalMachines = new ArrayList<PhysicalMachine>();
    
    private boolean isOverloaded = false;
    
    @Transient
    private DataCenterManagement algorithm;

    private float energyPriceDay;
    private float energyPriceNight;
    private int timezoneOffset;
    private Location location;

    @Transient
    private List<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
    
    @Transient
    private HashMap<String, Integer> migrationQueue = new HashMap<String, Integer>();

    @Transient
    private Forecast forecastService;
    
    @Transient
    static Logger logger = LoggerFactory.getLogger(DataCenter.class);
    
    int currentTime = 0;
    
    /**
     * Gets called on every simulated minute. Here VM allocation and load
     * updating should be done
     */
    public void simulate(int minute) {
    	currentTime = minute;
        handleMigrations(minute);
        for (PhysicalMachine pm : physicalMachines) {
            if (pm.isRunning()) {
                pm.updateLoads(minute);
            }
        }
        algorithm.scaleVirtualMachines(this);
        
        // Check if a sla is down
        ArrayList<ServiceLevelAgreement> slaList = getSLAs();
        for (ServiceLevelAgreement sla : slaList) {
        	boolean allVMsOnline = false;
        	

        	
        	for (VirtualMachine vm : sla.getVms()) {
        		allVMsOnline |= vm.isOnline();
        	}
 
        	if (!allVMsOnline) {
        		sla.incrementDowntime();
        	}
        }
        
		// System.out.printf("[%s] - Simulated times is %s\n", name,
		// Utils.getCurrentTime(minute));
	}

	/**
	 * Called when a VM gets migrated to this dataCenter
	 * 
	 * @param vm
	 * @param targetTime
	 */
	public void queueAddVirtualMachine(VirtualMachine vm, int targetTime) {
		if (vm.getPm().getDataCenter() == this) {
			PhysicalMachine pm = algorithm.findPMForMigration(this, vm);
			pm.setRunning(true);
			pm.getVirtualMachines().add(vm);
			vm.setPm(pm);
			vm.setOnline(true);
		}
		else if (!migrationQueue.containsKey(vm.getId())) {
			vmList.add(vm);
			migrationQueue.put(vm.getId(), currentTime + targetTime);
		}
	}

	/**
	 * Handle incoming migrations by looping over the queue and find the ones
	 * that have arrived yet (i.e the target time is reached)
	 * 
	 * TODO: LIVE VM Migration?
	 */
	private void handleMigrations(int minute) {
		Iterator<VirtualMachine> iter = vmList.iterator();
		while (iter.hasNext()) {
			VirtualMachine vm = iter.next();
			Integer time = migrationQueue.get(vm.getId());
			if (time >= minute) {
				PhysicalMachine pm = algorithm.findPMForMigration(this, vm);
				iter.remove();
				migrationQueue.remove(vm.getId());
				pm.setRunning(true);
				pm.getVirtualMachines().add(vm);
				vm.setPm(pm);
				vm.setOnline(true);
				//System.out.printf("VM[%s] at time (%d) arrived at DC : [%s]\n", vm.getId(), minute, vm.getPm().getDataCenter().getName());
			}
		}
	}

	/**
	 * Returns the current energy price for a given date
	 * 
	 * @param date
	 * @return
	 */
	public float getCurrentEneryPrice(Date date) {
		Calendar cal = Calendar.getInstance(); // creates calendar
		cal.setTime(new Date()); // sets calendar time/date
		cal.add(Calendar.HOUR_OF_DAY, this.timezoneOffset); // adds one hour
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		return hour >= 8 && hour < 20 ? this.energyPriceDay
				: this.energyPriceNight;
	}

	/**
	 * Returns the current energy costs
	 * 
	 * @param minute
	 * @return
	 */
	public double getCurrentEnergyCosts(int minute) {
		Date currentTime = Utils.getCurrentTime(minute);
		Weather currentWeather = forecastService.getForecast(currentTime, location, true);
		double powerConsumption = 0.;
		for (PhysicalMachine pm : getOnlinePMs()) {
			powerConsumption += pm.getPowerConsumption();
		}
		powerConsumption /= 1000; // convert to kw
		return Utils.getCoolingEnergyFactor(currentWeather.getCurrentTemperature()) * powerConsumption * getCurrentEneryPrice(currentTime);
	}
	
	/**
	 * Returns the averange prive per
	 * 
	 * @param date
	 * @return
	 */
	public double getAveragePricePerVM(Date date) {
		if (this.getVirtualMachineCount() == 0)
			return .0;
		return this.getPowerConsumption()
				/ (double) this.getVirtualMachineCount()
				* this.getCurrentEneryPrice(date);
	}

	public int getVirtualMachineCount() {
		int i = 0;
		for (PhysicalMachine pm : this.getPhysicalMachines()) {
			i += pm.getVirtualMachines().size();
		}
		return i;
	}

	/**
	 * Returns the PowerConsumption of the data center in Watt
	 * 
	 * @return
	 */
	public double getPowerConsumption() {
		double result = 0.;
		for (PhysicalMachine pm : this.getPhysicalMachines()) {
			if (pm.isRunning()) {
				result += pm.getPowerConsumption();
			}
		}
		return result;
	}

	public double getHighestAvailableFreeMemory() {
		double mem = 0;
		for (PhysicalMachine pm : getPhysicalMachines()) {
			if ((pm.getMemory() * (1 - pm.getMemoryUsage())) > mem) {
				mem = pm.getMemory() * (1 - pm.getMemoryUsage());
			}
		}
		return mem;
	}

	/**
	 * Returns a list of all PMs with state Online
	 * 
	 * @return
	 */
	public ArrayList<PhysicalMachine> getOnlinePMs() {
		ArrayList<PhysicalMachine> tmp = new ArrayList<PhysicalMachine>(
				physicalMachines.size());
		for (PhysicalMachine pm : physicalMachines) {
			if (pm.isRunning()) {
				tmp.add(pm);
			}
		}
		return tmp;
	}
	
	/**
	 * Returns a list of all PMs with state Offline
	 * 
	 * @return
	 */
	public ArrayList<PhysicalMachine> getOfflinePMs() {
		ArrayList<PhysicalMachine> tmp = new ArrayList<PhysicalMachine>(
				physicalMachines.size());
		for (PhysicalMachine pm : physicalMachines) {
			if (!pm.isRunning()) {
				tmp.add(pm);
			}
		}
		return tmp;
	}	

	/**
	 * This function should be called, if no resources in the dc are available,
	 * but a higher priority vm needs to run in the datacenter @dc
	 * This algorithm iterates through all physical machines, and looks for the
	 * PM which has the best power consumption after shutting down low priority
	 * VMs and adding the new VM @vm
	 * @param dc The datacenter
	 * @param vm The new VM which should run in the datacenter
	 * @return Returns a Map with the PM as key and the VMs with lower priority which
	 * 		   should be shut down, so that the new virtual machine (@vm) with higher priority has place
	 */
	public ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> getPMWithLowerPriorityVMList(DataCenter dc, VirtualMachine vm) {
		ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> tmpList = new ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>>();
		ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> result = new ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>>(1);
		
		for (PhysicalMachine pm : dc.getPhysicalMachines()) {
			
			// First check if vm already has place
			// First fit PM is returned where we can add our higher priority
			// PM
			if (Utils.VMfitsOnPM(pm, vm)) {
				result.put(pm, new ArrayList<VirtualMachine>());
				return result;
			}
			
			// Get copy of vms running
			ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(pm.getOnlineVMs());
			Utils.orderVMsByPriorityDescending(vms);
			
			// get resources of current VMs running on PM
			double currentMemory = Utils.getVMsMemory(vms);
			double currentSize = Utils.getVMsSize(vms); 
			double currentCPUs = Utils.getVMsCPULoad(vms);
			double currentBandwidth = Utils.getVMsBandwidth(vms);
			
			Iterator<VirtualMachine> it = vms.iterator();
			while(it.hasNext()) {
			    VirtualMachine tmp = it.next();
			    if (tmp.getSla().getPriority() < vm.getSla().getPriority()) {
			    	if (!tmpList.containsKey(pm)) {
			    		tmpList.put(pm, new ArrayList<VirtualMachine>());
			    	}
			    	tmpList.get(pm).add(tmp);
			    	
			    	// Reduce resources if current VM would be shut down
			    	currentMemory -= tmp.getMemory();
			    	//currentSize -= tmp.getSize(); // TODO?
			    	currentCPUs -= tmp.getCpus();
			    	currentBandwidth -= tmp.getBandwidth();
			    	
			    	
			    	// Check if higher prio vm has now place
			    	if (currentMemory + vm.getMemory() <= pm.getMemory() &&
			    			currentSize + vm.getSize() <= pm.getSize() &&
			    			currentCPUs + vm.getCpus() <= pm.getCpus() &&
			    			currentBandwidth + vm.getBandwidth() <= pm.getBandwidth()) {
			    		// Leave vm iteration
			    		break;
			    	}
			    	else {
			    		// If we reached the end of the vm-list, and we have still no place
			    		// on this pm, remove the complete entry from the tmplist
			    		if (it.hasNext() == false) {
			    			tmpList.remove(pm);
			    		}
			    	}
			    }
			}
		}
		
		// No PMs are in the list where we can shut down lower priority VMs
		if (tmpList.size() == 0) {
			return null;
		}
		
		// Iterate through our list and get the PM with the lowest power consumption
		PhysicalMachine bestUtilityPM = null;
		double bestPMtotalEnergyUsed = 0.;
		for (Entry<PhysicalMachine, ArrayList<VirtualMachine>> entry : tmpList.entrySet()) {
			
			// Make copy of current running VMS and remove the low priority vms of the current tmpList
			// related to the PM.
			// Then add the VM with higher priority and caluclate Utility
			ArrayList<VirtualMachine> newVMOnlineList = new ArrayList<VirtualMachine>(entry.getKey().getOnlineVMs());
			newVMOnlineList.removeAll(entry.getValue());
			newVMOnlineList.add(vm);
			
			if (bestUtilityPM == null) {
				bestUtilityPM = entry.getKey();
				double bestUtilityPM_currentBandwidth = Utils.getVMsBandwidth(newVMOnlineList) / entry.getKey().getBandwidth();
				double bestUtilityPM_currentCPUs = Utils.getVMsCPULoad(newVMOnlineList) / entry.getKey().getCpus();
				double bestUtilityPM_currentMemory = Utils.getVMsMemory(newVMOnlineList) / entry.getKey().getMemory();
				
				bestPMtotalEnergyUsed = entry.getKey().getIdleStateEnergyUtilization() + 
						entry.getKey().getCpuPowerConsumption() * bestUtilityPM_currentCPUs +
						entry.getKey().getMemPowerConsumption() * bestUtilityPM_currentMemory + 
						entry.getKey().getNetworkPowerConsumption() * bestUtilityPM_currentBandwidth;
				continue;
			}
			
			// Take the pm with best power consumption
			double nextPM_currentBandwidth = Utils.getVMsBandwidth(newVMOnlineList) / entry.getKey().getBandwidth();
			double nextPM_currentCPUs = Utils.getVMsCPULoad(newVMOnlineList) / entry.getKey().getCpus();
			double nextPM_currentMemory = Utils.getVMsMemory(newVMOnlineList) / entry.getKey().getMemory();
			
			double nexttotalEnergyUsed = entry.getKey().getIdleStateEnergyUtilization() + 
					entry.getKey().getCpuPowerConsumption() * nextPM_currentCPUs +
					entry.getKey().getMemPowerConsumption() * nextPM_currentMemory + 
					entry.getKey().getNetworkPowerConsumption() * nextPM_currentBandwidth;
			
			if (nexttotalEnergyUsed < bestPMtotalEnergyUsed) {
				bestUtilityPM = entry.getKey();
				bestPMtotalEnergyUsed = nexttotalEnergyUsed;
			}
			
		}
		
		if (bestUtilityPM!= null) {
			result.put(bestUtilityPM, tmpList.get(bestUtilityPM));
			return result;
		}
				
		return null;
	}
	
	public ArrayList<ServiceLevelAgreement> getSLAs() {
		ArrayList<ServiceLevelAgreement> slaList = new ArrayList<ServiceLevelAgreement>(
				32);
		for (PhysicalMachine pm : physicalMachines) {
			for (VirtualMachine vm : pm.getVirtualMachines()) {
				slaList.add(vm.getSla());
			}
		}
		return slaList;
	}
	
	
}
