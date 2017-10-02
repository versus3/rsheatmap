/**
 * @author: Vivek Kumar
 * @creationDate: 13/08/2017
 * @lastModifiedDate: 13/08/2017
 * @description: Used to store the x values of data points for crest algorithm
 */

package application;

public class SweepCoordinate implements Comparable<SweepCoordinate> {

	private Integer xValue;
	private CustomRectangle rectangle;
	private Boolean endPoint;
	
	public SweepCoordinate(Integer xValue, CustomRectangle rectangle, Boolean endPoint) {
		this.xValue = xValue;
		this.rectangle = rectangle;
		this.endPoint = endPoint;
	}
	
	public Integer getxValue() {
		return xValue;
	}
	
	public void setxValue(Integer xValue) {
		this.xValue = xValue;
	}

	public Boolean getEndPoint() {
		return endPoint;
	}
	
	public void setEndPoint(Boolean endPoint) {
		this.endPoint = endPoint;
	}
	
	public CustomRectangle getRectangle() {
		return rectangle;
	}

	public void setRectangle(CustomRectangle rectangle) {
		this.rectangle = rectangle;
	}

	@Override
	public int compareTo(SweepCoordinate coordinate) {
	   return xValue.compareTo(coordinate.xValue);
	}
}
