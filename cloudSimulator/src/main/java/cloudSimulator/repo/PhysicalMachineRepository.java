package cloudSimulator.repo;

import model.PhysicalMachine;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhysicalMachineRepository extends MongoRepository<PhysicalMachine, String> {

}
