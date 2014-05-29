package cloudSimulator.repo;

import model.VirtualMachine;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VirtualMachineRepository extends MongoRepository<VirtualMachine, String> {

}
