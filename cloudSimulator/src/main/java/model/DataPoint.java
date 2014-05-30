package model;

import lombok.Data;

@Data
public class DataPoint {
	int x;
	double y;
	
	public DataPoint(int x, double y) {
		this.x = x;
		this.y = y;
	}
}
