package simulation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Data;
import model.PhysicalMachine;
import model.VirtualMachine;
import algorithms.DataCenterManagement;
import cloudSimulator.weather.Location;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@Data
public class DataCenter implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7769086177008294418L;

	@Id
	private String id;
	
	private String name;
    private List<PhysicalMachine> physicalMachines;
    
    @Transient
    private DataCenterManagement algorithm;

    private float energyPriceDay;
    private float energyPriceNight;
    private int timezoneOffset;
    private Location location;

    @Transient
    private HashMap<VirtualMachine, Integer> migrationQueue = new HashMap<VirtualMachine, Integer>();

    /**
     * Gets called on every simulated minute. Here VM allocation and load
     * updating should be done
     */
    public void simulate(int minute) {
        handleMigrations(minute);
        for (PhysicalMachine pm : physicalMachines) {
            if (pm.isRunning()) {
                pm.updateLoads();
            }
        }
        algorithm.scaleVirtualMachines(this);
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
        migrationQueue.put(vm, targetTime);
    }

    /**
     * Handle incoming migrations by looping over the queue and find the ones
     * that have arrived yet (i.e the target time is reached)
     * 
     * TODO: LIVE VM Migration?
     */
    private void handleMigrations(int minute) {
        Iterator<Map.Entry<VirtualMachine, Integer>> iter = migrationQueue.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<VirtualMachine, Integer> entry = iter.next();
            if (entry.getValue() >= minute) {
                VirtualMachine vm = entry.getKey();
                PhysicalMachine pm = algorithm.findPMForMigration(this, vm);
                pm.getVirtualMachines().add(vm);
                vm.setOnline(true);
                iter.remove();
            }
        }
    }

    public float getCurrentEneryPrice(Date date) {
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, this.timezoneOffset); // adds one hour
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= 8 && hour < 20 ? this.energyPriceDay : this.energyPriceNight;
    }

    /**
     * Returns the averange prive per
     *
     * @param date
     * @return
     */
    public double getAveragePricePerVM(Date date) {
        if(this.getVirtualMachineCount() == 0)
            return .0;
        return this.getPowerConsumption() / (double) this.getVirtualMachineCount() * this.getCurrentEneryPrice(date);
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
    
    public double getHighestAvailableFreeMemory(){
        double mem = 0;
        for(PhysicalMachine pm :  getPhysicalMachines()){
            if((pm.getMemory() * (1- pm.getMemoryUsage())) > mem){
                mem = pm.getMemory() * (1- pm.getMemoryUsage());
            }
        }
        return mem;
    }

    /**
	 * Returns a list of all PMs with state Online
	 * @return
	 */
	public ArrayList<PhysicalMachine> getOnlinePMs() {
		ArrayList<PhysicalMachine> tmp = new ArrayList<PhysicalMachine>(physicalMachines.size());
		for (PhysicalMachine pm : physicalMachines) {
			if (pm.isRunning()) {
				tmp.add(pm);
			}
		}
		return tmp;
	}
}
