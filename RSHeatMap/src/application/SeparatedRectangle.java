/**
 * @author: Vivek Kumar
 * @creationDate: 13/08/2017
 * @lastModifiedDate: 13/08/2017
 * @description: Used to denote the rectangles to be drawn
 */

package application;

import java.awt.Rectangle;

public class SeparatedRectangle {

	private Rectangle rectangle;
	private int intersectionCount;
	
	public SeparatedRectangle (Rectangle rectangle) {
		this.rectangle = rectangle;
	}

	public Rectangle getRectangle() {
		return rectangle;
	}
	
	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
	
	public int getIntersectionCount() {
		return intersectionCount;
	}
	
	public void setIntersectionCount(int intersectionCount) {
		this.intersectionCount = intersectionCount;
	}
	
}
