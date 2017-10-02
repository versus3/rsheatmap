/**
 * @author: Vivek Kumar
 * @creationDate: 13/08/2017
 * @lastModifiedDate: 13/08/2017
 * @description: Used to store the height and width of datapoints
 */

package application;

public class Constants {

	public static Integer RECTANGLE_WIDTH = 100;
	public static Integer RECTANGLE_HEIGHT = 100;
	
	public static void setWidth(Integer width) {
		Constants.RECTANGLE_WIDTH = width;
	}
	
	public static void setHeight(Integer height) {
		Constants.RECTANGLE_HEIGHT = height;
	}
}
