package algorithms;

import simulation.ElasticityManager;

public interface DataCenterMigration {
	/**
	 * Gets a reference to the ElasticityManager and decides
	 * whether a migration is needed, if yes it finds a target
	 * dataCenter and calls em.migrate(vm, src, dest)
	 * 
	 * @param em
	 */
	public void manageVirtualMachines(ElasticityManager em, int minute);
}
