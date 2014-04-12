package model;

import lombok.Data;

@Data
public class ServiceLevelAgreement {
	int size;
	int memory;
	int cpus;
	int bandwith;
	int maxDowntime;
}
