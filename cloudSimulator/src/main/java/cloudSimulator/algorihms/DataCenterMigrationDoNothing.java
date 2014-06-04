package cloudSimulator.algorihms;

import org.springframework.stereotype.Service;

import simulation.ElasticityManager;
import algorithms.DataCenterMigration;

@Service(value = "migrationDoNothing")
public class DataCenterMigrationDoNothing implements DataCenterMigration {

  public void manageVirtualMachines(ElasticityManager em, int minute) {

  }

}
