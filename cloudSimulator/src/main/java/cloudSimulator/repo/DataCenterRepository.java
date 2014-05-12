package cloudSimulator.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import simulation.DataCenter;

public interface DataCenterRepository extends MongoRepository<DataCenter, String> {

}
