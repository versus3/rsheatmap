/**
 * @author: Vivek Kumar
 * @creationDate: 18/09/2017
 * @lastModifiedDate: 18/09/2017
 * @description: Used to store the coordinates of the drones to be traversed
 */

package application;

import java.util.ArrayList;

public class Drone {

	private Integer id;
	ArrayList<Coordinate> coordinates;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public ArrayList<Coordinate> getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(ArrayList<Coordinate> coordinates) {
		this.coordinates = coordinates;
	}
	
	public Drone() {
		coordinates = new ArrayList<Coordinate>();
	}
}
