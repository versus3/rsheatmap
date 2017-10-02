/**
 * @author: Vivek Kumar
 * @creationDate: 01/10/2017
 * @lastModifiedDate: 01/10/2017
 * @description: Used to store the coordinates of the dynamic data point to be traversed
 */

package application;

import java.util.ArrayList;

public class DynamicRectangle {

	ArrayList<Coordinate> coordinates;
	
	public ArrayList<Coordinate> getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(ArrayList<Coordinate> coordinates) {
		this.coordinates = coordinates;
	}
	
	public DynamicRectangle() {
		coordinates = new ArrayList<Coordinate>();
	}
}
