package simulation;

import java.util.List;

import lombok.Data;
import model.PhysicalMachine;

@Data
public class DataCenter {
		
	private String name;
	private List<PhysicalMachine> physicalMachines;

	/**
	 * Gets called on every simulated minute.
	 * Here VM allocation and load updating should be done
	 */
	public void simulate() {
		System.out.printf("[%s] - time is %d\n", name, System.currentTimeMillis());
	}
}
