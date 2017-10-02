/**
 * @author: Vivek Kumar
 * @creationDate: 13/08/2017
 * @lastModifiedDate: 13/08/2017
 * @description: Used to denote coordinates on the canvas
 */

package application;

public class Coordinate {
	
	private Integer xValue;
	private Integer yValue;
	
	public Coordinate(Integer xValue, Integer yValue) {
		this.xValue = xValue;
		this.yValue = yValue;
	}
	
	public Integer getxValue() {
		return xValue;
	}
	
	public void setxValue(Integer xValue) {
		this.xValue = xValue;
	}
	
	public Integer getyValue() {
		return yValue;
	}
	
	public void setyValue(Integer yValue) {
		this.yValue = yValue;
	}
	
}
