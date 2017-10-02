/**
 * @author: Vivek Kumar
 * @creationDate: 13/08/2017
 * @lastModifiedDate: 13/08/2017
 * @description: Used to store the rectangle with additional information regarding the center points
 */

package application;

import java.awt.Rectangle;

public class CustomRectangle {

	private Integer xCordinateCenter;
	private Integer yCordinateCenter;
	private Integer xLeft;
	private Integer xRight;
	private Integer yTop;
	private Integer yBottom;
	private Rectangle rectangle;
	
	public CustomRectangle () {
	}
	
	public CustomRectangle (Integer xCordinate, Integer yCordinate) {
		this.xCordinateCenter = xCordinate;
		this.yCordinateCenter = yCordinate;
		this.rectangle = new Rectangle();
		rectangle.height = Constants.RECTANGLE_HEIGHT;
		rectangle.width = Constants.RECTANGLE_WIDTH;

		this.xLeft = xCordinateCenter - (rectangle.width/2);
		if(this.xLeft < 0) {
			this.xLeft = 0;
			rectangle.width = 2 * (xCordinateCenter - this.xLeft);
		}
		this.xRight = xCordinateCenter + (rectangle.width/2);
		this.yTop = yCordinateCenter - (rectangle.height/2);
		if(this.yTop < 0) {
			this.yTop = 0;
			rectangle.height = 2 * (yCordinateCenter - this.yTop);
		}
		this.yBottom = yCordinateCenter + (rectangle.height/2);
		
		this.rectangle.x = xCordinateCenter - (rectangle.width/2);
		this.rectangle.y = yCordinateCenter - (rectangle.height/2);
	}
	
	public CustomRectangle (Integer xCordinate, Integer yCordinate, 
			Integer height, Integer width, Boolean reverse) {
		this.xLeft = xCordinate;
		this.yTop = yCordinate;
		
		this.rectangle = new Rectangle();
		if(height.equals(0)) {
			rectangle.height = Constants.RECTANGLE_HEIGHT;
		} else {
			rectangle.height = height;
		}
		if(width.equals(0)) {
			rectangle.width = Constants.RECTANGLE_WIDTH;
		} else {
			rectangle.width = width;
		}
		
		this.xCordinateCenter = this.xLeft + (rectangle.width/2);
		this.yCordinateCenter = this.yTop + (rectangle.height/2);
		this.xRight = xLeft + rectangle.width;
		this.yBottom = yTop + rectangle.height;
		
		this.rectangle.x = xLeft;
		this.rectangle.y = yTop;
	}

	public Integer getxCordinateCenter() {
		return xCordinateCenter;
	}

	public void setxCordinateCenter(Integer xCordinateCenter) {
		this.xCordinateCenter = xCordinateCenter;
	}

	public Integer getyCordinateCenter() {
		return yCordinateCenter;
	}

	public void setyCordinateCenter(Integer yCordinateCenter) {
		this.yCordinateCenter = yCordinateCenter;
	}

	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
	
	public Rectangle getRectangle () {
		return rectangle;
	}
	

	public Integer getxLeft() {
		return xLeft;
	}

	public void setxLeft(Integer xLeft) {
		this.xLeft = xLeft;
	}

	public Integer getxRight() {
		return xRight;
	}

	public void setxRight(Integer xRight) {
		this.xRight = xRight;
	}

	public Integer getyTop() {
		return yTop;
	}

	public void setyTop(Integer yTop) {
		this.yTop = yTop;
	}

	public Integer getyBottom() {
		return yBottom;
	}

	public void setyBottom(Integer yBottom) {
		this.yBottom = yBottom;
	}
	
	public void changeDimensions() {
		this.rectangle = new Rectangle();
		rectangle.height = Constants.RECTANGLE_HEIGHT;
		rectangle.width = Constants.RECTANGLE_WIDTH;

		this.xLeft = xCordinateCenter - (rectangle.width/2);
		if(this.xLeft < 0) {
			this.xLeft = 0;
			rectangle.width = 2 * (xCordinateCenter - this.xLeft);
		}
		this.xRight = xCordinateCenter + (rectangle.width/2);
		this.yTop = yCordinateCenter - (rectangle.height/2);
		if(this.yTop < 0) {
			this.yTop = 0;
			rectangle.height = 2 * (yCordinateCenter - this.yTop);
		}
		this.yBottom = yCordinateCenter + (rectangle.height/2);
		
		this.rectangle.x = xCordinateCenter - (rectangle.width/2);
		this.rectangle.y = yCordinateCenter - (rectangle.height/2);
	}
	
}
