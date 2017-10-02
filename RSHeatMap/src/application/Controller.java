/**
 * @author: Vivek Kumar
 * @creationDate: 31/07/2017
 * @lastModifiedDate: 02/10/2017
 * @description: The class is the main controller for the application. The main 
 * purpose of this class is the compute the crest algorithm during various events like:
 *  1. Bulk load of data points
 *  2. Individual addition of data points
 *  3. Removal of data points
 *  4. Selection of either static or dynamic algorithm based on the number of intersections
 *  5. Simulation of drones across the canvas
 *  6. Loading data points from Open Load Map
 */

package application;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class Controller {

	@FXML private TextField filePath;
	@FXML private Canvas canvas;
	@FXML private ListView<String> pointsList;
	@FXML private TextField xCordinate;
	@FXML private TextField yCordinate;
	@FXML private TextField height;
	@FXML private TextField width;
	
	// For Crest algorithm
	private ArrayList<CustomRectangle> rectangles;
	private ArrayList<SeparatedRectangle> smallerRectangles;
	private ArrayList<SweepCoordinate> sweepCoordinates;
	
	// For Dynamic Crest Algorithm
	private ArrayList<CustomRectangle> newRectangles;
	private ArrayList<SweepCoordinate> tempSweepCoordinates;
	private double maximumIntersection;
	private RTree<SeparatedRectangle> smallerRectanglesRtree;
	
	// For drone simulation
	private ArrayList<Drone> drones;
	private Boolean addingDrone;
	private Boolean addingCordinates;
	private Boolean droneSimulation;
	private Image droneImage;
	
	// For dynamic point simluation
	private ArrayList<DynamicRectangle> dynamicRectangles;
	private ArrayList<Coordinate> previousPoints;
	private Boolean addingDynamicRectangle;
	private Boolean addingDynamicRectangleCordinates;
	private Boolean dynamicRectangleSimulation;
	
	private String imagePath;
	private double opacity;
	
	public Controller() throws IOException {
		this.maximumIntersection = 0;	
		this.imagePath = "";
		this.opacity = 0.75;
		if (smallerRectanglesRtree == null) {
			smallerRectanglesRtree = new RTree<SeparatedRectangle>();
		}
		if (rectangles == null) {
			rectangles = new ArrayList<CustomRectangle>();
		}
		if (smallerRectangles == null) {
			smallerRectangles = new ArrayList<SeparatedRectangle>();
		}
		if(sweepCoordinates == null) {
			sweepCoordinates = new ArrayList<SweepCoordinate>();
		}
		if(tempSweepCoordinates == null) {
			tempSweepCoordinates = new ArrayList<SweepCoordinate>();
		}
		if(newRectangles == null) {
			newRectangles = new ArrayList<CustomRectangle>();
		}
		if(drones == null) {
			drones = new ArrayList<Drone>();
		}
		this.addingDrone = false;
		this.addingCordinates = false;
		this.droneSimulation = false;
		if(dynamicRectangles == null) {
			dynamicRectangles = new ArrayList<DynamicRectangle>();
		}
		if(previousPoints == null) {
			previousPoints = new ArrayList<Coordinate>();
		}
		this.addingDynamicRectangle = false;
		this.addingDynamicRectangleCordinates = false;
		this.dynamicRectangleSimulation = false;
		BufferedImage image = ImageIO.read(getClass().getResource("/image/drone.png"));
		droneImage = SwingFXUtils.toFXImage(image, null);
	}
	
	public void initialiseSweep() {
		newRectangles = new ArrayList<CustomRectangle>();
		tempSweepCoordinates = new ArrayList<SweepCoordinate>();
	}
	
	// removes all points from the canvas and any data associated with it
	public void resetCanvas() {
		this.maximumIntersection = 0;
		smallerRectanglesRtree = new RTree<SeparatedRectangle>();
		rectangles = new ArrayList<CustomRectangle>();
		smallerRectangles = new ArrayList<SeparatedRectangle>();
		sweepCoordinates = new ArrayList<SweepCoordinate>();
		tempSweepCoordinates = new ArrayList<SweepCoordinate>();
		newRectangles = new ArrayList<CustomRectangle>();
		pointsList.getItems().clear();
		drawSmallerRectangles();
	}
	
	/* removes all data associated with the points in the canvas
	 * the points are not removed */
	public void resetCanvasForChangeInDimension() {
		this.maximumIntersection = 0;
		smallerRectanglesRtree = new RTree<SeparatedRectangle>();
		smallerRectangles = new ArrayList<SeparatedRectangle>();
		sweepCoordinates = new ArrayList<SweepCoordinate>();
		tempSweepCoordinates = new ArrayList<SweepCoordinate>();
		newRectangles = new ArrayList<CustomRectangle>();
		pointsList.getItems().clear();
	}
	
	// used to upload a file which contains the data points
	public void uploadFile (ActionEvent event) {
		
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extentionFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extentionFilter);
		
		String userDirectoryString = System.getProperty("user.home") + "/Desktop";
		File userDirectory = new File(userDirectoryString);
		fileChooser.setInitialDirectory(userDirectory);
		
		File selectedFile = fileChooser.showOpenDialog(null);
		
		if(selectedFile != null) {
			filePath.setText(selectedFile.getAbsolutePath());
		}
		
		try {
			createRectanglesFromFile(filePath.getText());
		} catch (Exception ex) {
			smallerRectangles.clear();
			rectangles.clear();
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			pointsList.getItems().clear();
			Alert alert = new Alert(AlertType.ERROR, "No file found! Please provide a valid document path", ButtonType.YES);
			alert.showAndWait();
		}
	}
	
	/* adds the x axis of the data points to the list (for crest algorithm). 
	 * the list of points are also update in this function */
	public void addToList (CustomRectangle rectangle, Integer xCordinate, Integer yCordinate) {
		ObservableList<String> points = FXCollections.observableArrayList();
		points = pointsList.getItems();
		rectangles.add(rectangle);
		SweepCoordinate leftSweep = new SweepCoordinate(rectangle.getxLeft(), rectangle, false);
		SweepCoordinate rightSweep = new SweepCoordinate(rectangle.getxRight(), rectangle, true);
		sweepCoordinates.add(leftSweep);
		sweepCoordinates.add(rightSweep);
		Collections.sort(sweepCoordinates);
		points.add(xCordinate + ", " + yCordinate);
		Collections.sort(points);
		pointsList.setItems(points);
	}
	
	/* the x axis of data points are stored in a temp list for the use of 
	 *  dynamic crest algorithm */
	public void addToTempList (CustomRectangle rectangle) {
		SweepCoordinate leftSweep = new SweepCoordinate(rectangle.getxLeft(), rectangle, false);
		SweepCoordinate rightSweep = new SweepCoordinate(rectangle.getxRight(), rectangle, true);
		tempSweepCoordinates.add(leftSweep);
		tempSweepCoordinates.add(rightSweep);
		Collections.sort(tempSweepCoordinates);
	}
	
	/* extract data points from the file and compute. Based on the number of intersection 
	 * with the existing rectangles, either dynamic crest algorithm or static is selected */
	public void createRectanglesFromFile (String path) throws Exception {
		BufferedReader br = null;
		FileReader fr = null;
		fr = new FileReader(path);
		br = new BufferedReader(fr);
		String currentLine;
		initialiseSweep();
		int intersectionCount = 0;
		while ((currentLine = br.readLine()) != null) {
			String[] coordinates = currentLine.split(",");
			Integer xCordinate = Integer.parseInt(coordinates[0]);
			Integer yCordinate = Integer.parseInt(coordinates[1]);
			Boolean existingRectangle = checkIfAlreadyExist(xCordinate, yCordinate);
			if(!existingRectangle) {
				CustomRectangle customRectangle;
				customRectangle = new CustomRectangle(xCordinate, yCordinate);
				newRectangles.add(customRectangle);
				
				float[] topLeftCordinates = new float[] {customRectangle.getxLeft(), customRectangle.getyTop()};
				float[] dimension = new float[] {(float) customRectangle.getRectangle().getHeight(),
						(float) customRectangle.getRectangle().getWidth()};
				int tempIntersectionCount = smallerRectanglesRtree.search(topLeftCordinates, dimension).size();
				intersectionCount = intersectionCount + tempIntersectionCount;				
			}
		}
		
		// computing the complexity
		double worstCaseRegular = (newRectangles.size() + rectangles.size()) * (newRectangles.size() + rectangles.size()) *
				Math.log(newRectangles.size() + rectangles.size());
		double worstCaseDynamic = (newRectangles.size() + intersectionCount) * (newRectangles.size() + intersectionCount) *
				Math.log(newRectangles.size() + intersectionCount) + Math.log(1 + smallerRectanglesRtree.size());
		
		if(worstCaseDynamic >= worstCaseRegular) {
			Iterator<CustomRectangle> rectangleIterator = newRectangles.iterator();
			while(rectangleIterator.hasNext()) {
				CustomRectangle customRectangle = rectangleIterator.next();
				addToList(customRectangle, customRectangle.getxCordinateCenter(), 
						customRectangle.getyCordinateCenter());
			}
			computeCrestAlgorithm();
		} else {
			tempSweepCoordinates.clear();
			Iterator<CustomRectangle> rectangleIterator = newRectangles.iterator();
			while(rectangleIterator.hasNext()) {
				CustomRectangle rectangle = rectangleIterator.next();
				addToList(rectangle, rectangle.getxCordinateCenter(), rectangle.getyCordinateCenter());
				addToTempList(rectangle);
				
				float[] topLeftCordinates = new float[] {rectangle.getxLeft(), rectangle.getyTop()};
				float[] dimension = new float[] {(float) rectangle.getRectangle().width, 
						(float) rectangle.getRectangle().height};
				List<SeparatedRectangle> intersectingRectangles = 
						smallerRectanglesRtree.search(topLeftCordinates, dimension);
				
				Iterator<SeparatedRectangle> listIterator = intersectingRectangles.iterator();
				while(listIterator.hasNext()) {
					SeparatedRectangle smallRectangle = listIterator.next();
					if(smallRectangle.getRectangle().intersects(rectangle.getRectangle())) {
						float[] coords = new float[] {smallRectangle.getRectangle().x, smallRectangle.getRectangle().y};
						float[] dimensions = new float[] {(float) smallRectangle.getRectangle().width, 
								(float) smallRectangle.getRectangle().height};
						smallerRectanglesRtree.delete(coords, dimensions, smallRectangle);
					} else {
						listIterator.remove();
					}
				}
				
				Iterator<SeparatedRectangle> smallIterator = smallerRectangles.iterator();
				while(smallIterator.hasNext()) {
					SeparatedRectangle smallRectangle = smallIterator.next();
					if(intersectingRectangles.contains(smallRectangle)) {
						CustomRectangle newCustomRectangle = new CustomRectangle(smallRectangle.getRectangle().x, 
								smallRectangle.getRectangle().y, smallRectangle.getRectangle().height, 
								smallRectangle.getRectangle().width, true);
						addToTempList(newCustomRectangle);
						smallIterator.remove();
					}
				}
			}
			computeCrestDynamicAlgorithm();
		}
		drawSmallerRectangles();
		br.close();
		fr.close();
	}
	
	/* Change the dimension of rectangles */
	public void createRectanglesChangeDimension () throws Exception {
		resetCanvasForChangeInDimension();
		ArrayList<CustomRectangle> tempRectangles = new ArrayList<CustomRectangle>();
		Integer height = Constants.RECTANGLE_HEIGHT;
		if(!(this.height.getText().equals("") || this.height.getText() == null)) {
			height = Integer.parseInt(this.height.getText());
			if(height%2 != 0) {
				height = height + 1;
			}
		}
		Integer width = Constants.RECTANGLE_WIDTH;
		if(!(this.width.getText() == null || this.width.getText().equals(""))) {
			width = Integer.parseInt(this.width.getText());
			if(width%2 != 0) {
				width = width + 1;
			}
		}
		if(!(height <= 0 || width <= 0)) {
			Constants.setHeight(height);
			Constants.setWidth(width);
			Iterator<CustomRectangle> iter = rectangles.iterator();
			while(iter.hasNext()) {
				CustomRectangle rectangle = iter.next();
				CustomRectangle changedRectangle = new CustomRectangle(rectangle.getxCordinateCenter(),
						rectangle.getyCordinateCenter());
				changedRectangle.changeDimensions();
				tempRectangles.add(changedRectangle);
				iter.remove();
			}
			iter = tempRectangles.iterator();
			while(iter.hasNext()) {
				CustomRectangle rectangle = iter.next();
				addToList(rectangle, rectangle.getxCordinateCenter(), rectangle.getyCordinateCenter());
			}
			computeCrestAlgorithm();
			drawSmallerRectangles();
		} else {
			Alert alert = new Alert(AlertType.ERROR, "Invalid value in Height and Width", ButtonType.YES);
			alert.showAndWait();
		}
	}
	
	/* checks which action should be performed on the canvas based on the mouse button click */
	public void interactWithCanvas (MouseEvent event) {
		MouseButton button = event.getButton();
		Double xOnCanvas = event.getX();
		Double yOnCanvas = event.getY();
		Integer xCordinate = xOnCanvas.intValue();
		Integer yCordinate = yOnCanvas.intValue();
		if(!this.addingDrone && !this.addingDynamicRectangle) {
			if(button.equals(MouseButton.PRIMARY)) {
				addRectangle(xCordinate, yCordinate);
			} else if(button.equals(MouseButton.SECONDARY)) {
				removeRectangle(xCordinate, yCordinate);
			}
		} else if (this.addingDrone) {
			if(this.addingCordinates) {
				if(button.equals(MouseButton.PRIMARY)) {
					addDroneCordinate(xCordinate, yCordinate);
				} else if(button.equals(MouseButton.SECONDARY)) {
					this.addingCordinates = false;
				}
			} else {
				if(button.equals(MouseButton.PRIMARY)) {
					Drone drone = new Drone();
					Coordinate coordinate = new Coordinate(xCordinate, yCordinate);
					drone.coordinates.add(coordinate);
					canvas.getGraphicsContext2D().drawImage(this.droneImage, coordinate.getxValue()-25, coordinate.getyValue()-25);
					drones.add(drone);
					this.addingCordinates = true;
				} else if(button.equals(MouseButton.SECONDARY)) {
					this.addingDrone = false;
				}
			}
		} else if (this.addingDynamicRectangle) {
			if(this.addingDynamicRectangleCordinates) {
				if(button.equals(MouseButton.PRIMARY)) {
					addDynamicRectangleCordinate(xCordinate, yCordinate);
				} else if(button.equals(MouseButton.SECONDARY)) {
					this.addingDynamicRectangleCordinates = false;
				}
			} else {
				if(button.equals(MouseButton.PRIMARY)) {
					DynamicRectangle dynamicRectangle = new DynamicRectangle();
					Coordinate coordinate = new Coordinate(xCordinate, yCordinate);
					dynamicRectangle.coordinates.add(coordinate);
					Rectangle rectangle = new Rectangle((coordinate.getxValue()-(Constants.RECTANGLE_WIDTH/2)), 
							(coordinate.getyValue()-(Constants.RECTANGLE_HEIGHT/2)), 
							Constants.RECTANGLE_WIDTH, Constants.RECTANGLE_HEIGHT);
					canvas.getGraphicsContext2D().setStroke(Color.gray(0));
					canvas.getGraphicsContext2D().strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
					canvas.getGraphicsContext2D().setFill(Color.rgb(255, 0, 0, 0.5));
					canvas.getGraphicsContext2D().fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
					canvas.getGraphicsContext2D().setFill(Color.rgb(255, 255, 255, 1));
					canvas.getGraphicsContext2D().fillRect(rectangle.getCenterX()-2.5, rectangle.getCenterY()-2.5, 5, 5);
					dynamicRectangles.add(dynamicRectangle);
					this.addingDynamicRectangleCordinates = true;
				} else if(button.equals(MouseButton.SECONDARY)) {
					this.addingDynamicRectangle = false;
				}
			}
		}
	}
	
	/* creates a new rectangle based on the data point */
	public void addNewRectangle() {
		tempSweepCoordinates.clear();
		try {
			Integer xCordinate = Integer.parseInt(this.xCordinate.getText());
			Integer yCordinate = Integer.parseInt(this.yCordinate.getText());
			addRectangle(xCordinate, yCordinate);
		} catch (Exception ex) {
			Alert alert = new Alert(AlertType.ERROR, "Please provide an integer value for both the coordinates", ButtonType.YES);
			alert.showAndWait();
		}
	}
	
	public void addRectangle(Integer xCordinate, Integer yCordinate) {
		tempSweepCoordinates.clear();
		if(rectangles == null) {
			rectangles = new ArrayList<CustomRectangle>();
		}
		Boolean existingRectangle = checkIfAlreadyExist(xCordinate, yCordinate);
		if(!existingRectangle) {
			initialiseSweep();
			CustomRectangle rectangle;
			rectangle = new CustomRectangle(xCordinate, yCordinate);
			addToList(rectangle, xCordinate, yCordinate);
			addToTempList(rectangle);

			float[] topLeftCordinates = new float[] {rectangle.getxLeft(), rectangle.getyTop()};
			float[] dimension = new float[] {(float) rectangle.getRectangle().width, 
					(float) rectangle.getRectangle().height};
			List<SeparatedRectangle> intersectingRectangles = 
					smallerRectanglesRtree.search(topLeftCordinates, dimension);

			Iterator<SeparatedRectangle> listIterator = intersectingRectangles.iterator();
			while(listIterator.hasNext()) {
				SeparatedRectangle smallRectangle = listIterator.next();
				if(smallRectangle.getRectangle().intersects(rectangle.getRectangle())) {
					float[] coords = new float[] {smallRectangle.getRectangle().x, smallRectangle.getRectangle().y};
					float[] dimensions = new float[] {(float) smallRectangle.getRectangle().width, 
							(float) smallRectangle.getRectangle().height};
					smallerRectanglesRtree.delete(coords, dimensions, smallRectangle);
				} else {
					listIterator.remove();
				}
			}

			Iterator<SeparatedRectangle> smallIterator = smallerRectangles.iterator();
			while(smallIterator.hasNext()) {
				SeparatedRectangle smallRectangle = smallIterator.next();
				if(intersectingRectangles.contains(smallRectangle)) {
					CustomRectangle newCustomRectangle = new CustomRectangle(smallRectangle.getRectangle().x, 
							smallRectangle.getRectangle().y, smallRectangle.getRectangle().height, 
							smallRectangle.getRectangle().width, true);
					addToTempList(newCustomRectangle);
					smallIterator.remove();
				}
			}
			computeCrestDynamicAlgorithm();
			drawSmallerRectangles();
		} else {
			Alert alert = new Alert(AlertType.INFORMATION, "Another rectangle with the same coordinates already exist. "
					+ "Please use a different set of coordinates", ButtonType.YES);
			alert.showAndWait();
		}
	}
	
	/* removes rectangle based on the point of interaction */
	public void removeRectangle(Integer xCordinate, Integer yCordinate) {
		tempSweepCoordinates.clear();
		Boolean existingRectangle = checkIfAlreadyExistForDeletion(xCordinate, yCordinate);
		if(existingRectangle) {
			Iterator<CustomRectangle> iterator = rectangles.iterator();
			int index = 0;
			Boolean pointFound = false;
			ArrayList<Integer> deleteIndexs = new ArrayList<Integer>();
			while(iterator.hasNext()) {
				CustomRectangle rectangle = iterator.next();
				Integer xCord = (int) rectangle.getxCordinateCenter();
				Integer yCord = (int) rectangle.getyCordinateCenter();
				if(clickedOnPoint(xCord, yCord, xCordinate,yCordinate)) {
					xCordinate = xCord;
					yCordinate = yCord;
					pointFound = true;
					Iterator<SeparatedRectangle> recIterator = smallerRectangles.iterator();
					while(recIterator.hasNext()) {
						SeparatedRectangle separatedRectangle = recIterator.next();
						Rectangle rec = separatedRectangle.getRectangle();
						if(rectangle.getRectangle().contains(rec)) {
							separatedRectangle.setIntersectionCount(separatedRectangle.getIntersectionCount() - 1);
							if(separatedRectangle.getIntersectionCount() == 0) {
								float[] coords = new float[] {separatedRectangle.getRectangle().x, separatedRectangle.getRectangle().y};
								float[] dimensions = new float[] {(float) separatedRectangle.getRectangle().width, 
										(float) separatedRectangle.getRectangle().height};
								smallerRectanglesRtree.delete(coords, dimensions, separatedRectangle);
								recIterator.remove();
							}
						}
					}
					
					Iterator<SweepCoordinate> sweepIterator = sweepCoordinates.iterator();		
					int count = 0;
					while(sweepIterator.hasNext()) {
						SweepCoordinate sweep = sweepIterator.next();
						if((sweep.getRectangle() != null) && (sweep.getRectangle().equals(rectangle))) {
							deleteIndexs.add(count);
						}
						count = count + 1;
					}
					Collections.reverse(deleteIndexs);
					break;
				}
				index = index + 1;
			}
			if(pointFound) {
				ObservableList<String> points = FXCollections.observableArrayList();
				points = pointsList.getItems();
				rectangles.remove(index);
				String removeCoordinate = xCordinate + ", " + yCordinate;
				Iterator<String> pointListIterator = points.iterator();
				while(pointListIterator.hasNext()) {
					String coordinate = pointListIterator.next();
					if(coordinate.equals(removeCoordinate)) {
						pointListIterator.remove();
					}
				}
			}
			maxIntersection();
			drawSmallerRectangles();
		}
	}
	
	/* add the next coordinate to the current active drone */
	public void addDroneCordinate(Integer xCordinate, Integer yCordinate) {
		Coordinate coordinate = new Coordinate(xCordinate, yCordinate);
		Drone lastDrone = drones.get(drones.size()-1);
		int coordinateLength = lastDrone.coordinates.size();
		if(coordinateLength > 0) {
			Coordinate lastCoordinate = lastDrone.coordinates.get(coordinateLength - 1);
			if(!(lastCoordinate.getxValue().equals(xCordinate)) 
					&& !(lastCoordinate.getyValue().equals(yCordinate))) {
				float m = (float) (yCordinate - lastCoordinate.getyValue()) / (float) (xCordinate - lastCoordinate.getxValue());
				int xWidth = xCordinate - lastCoordinate.getxValue();
				if(xWidth > 0) {
					for(int i = 0; i < xWidth; i++) {
					    Integer x = lastCoordinate.getxValue() + i;
					    Integer y = (int) (lastCoordinate.getyValue() + (m * i));
					    Coordinate intermediateCoordinate = new Coordinate(x, y);
					    lastDrone.coordinates.add(intermediateCoordinate);
					}
				} else {
					for(int i = 0; i > xWidth; i--) {
					    Integer x = lastCoordinate.getxValue() + i;
					    Integer y = (int) (lastCoordinate.getyValue() + (m * i));
					    Coordinate intermediateCoordinate = new Coordinate(x, y);
					    lastDrone.coordinates.add(intermediateCoordinate);
					}
				}
				lastDrone.coordinates.add(coordinate);
				canvas.getGraphicsContext2D().setStroke(Color.gray(0));
				canvas.getGraphicsContext2D().drawImage(this.droneImage, coordinate.getxValue()-25, coordinate.getyValue()-25);
				canvas.getGraphicsContext2D().strokeLine(lastCoordinate.getxValue(),
						lastCoordinate.getyValue(), coordinate.getxValue(), coordinate.getyValue());
			} else if((lastCoordinate.getxValue().equals(xCordinate))
					&& (lastCoordinate.getyValue().equals(yCordinate))) {
			} else if(lastCoordinate.getxValue().equals(xCordinate)) {
				if(lastCoordinate.getyValue() > yCordinate) {
					Integer nextYValue = lastCoordinate.getyValue() - 1;
					while(!nextYValue.equals(yCordinate)) {
						Coordinate intermediateCoordinate = new Coordinate(xCordinate, nextYValue);
						lastDrone.coordinates.add(intermediateCoordinate);
						nextYValue = nextYValue - 1;
					}
				} else if(lastCoordinate.getyValue() < yCordinate) {
					Integer nextYValue = lastCoordinate.getyValue() + 1;
					while(!nextYValue.equals(yCordinate)) {
						Coordinate intermediateCoordinate = new Coordinate(xCordinate, nextYValue);
						lastDrone.coordinates.add(intermediateCoordinate);
						nextYValue = nextYValue + 1;
					}
				}
				lastDrone.coordinates.add(coordinate);
				canvas.getGraphicsContext2D().setStroke(Color.gray(0));
				canvas.getGraphicsContext2D().drawImage(this.droneImage, coordinate.getxValue()-25, coordinate.getyValue()-25);
				canvas.getGraphicsContext2D().strokeLine(lastCoordinate.getxValue(),
						lastCoordinate.getyValue(), coordinate.getxValue(), coordinate.getyValue());
			} else if(lastCoordinate.getyValue().equals(yCordinate)) {
				if(lastCoordinate.getxValue() > xCordinate) {
					Integer nextXValue = lastCoordinate.getxValue() - 1;
					while(!nextXValue.equals(xCordinate)) {
						Coordinate intermediateCoordinate = new Coordinate(nextXValue, yCordinate);
						lastDrone.coordinates.add(intermediateCoordinate);
						nextXValue = nextXValue - 1;
					}
				} else if(lastCoordinate.getxValue() < xCordinate) {
					Integer nextXValue = lastCoordinate.getxValue() + 1;
					while(!nextXValue.equals(xCordinate)) {
						Coordinate intermediateCoordinate = new Coordinate(nextXValue, yCordinate);
						lastDrone.coordinates.add(intermediateCoordinate);
						nextXValue = nextXValue + 1;
					}
				}
				lastDrone.coordinates.add(coordinate);
				canvas.getGraphicsContext2D().setStroke(Color.gray(0));
				canvas.getGraphicsContext2D().drawImage(this.droneImage, coordinate.getxValue()-25, coordinate.getyValue()-25);
				canvas.getGraphicsContext2D().strokeLine(lastCoordinate.getxValue(),
						lastCoordinate.getyValue(), coordinate.getxValue(), coordinate.getyValue());
			}
		} else {
			lastDrone.coordinates.add(coordinate);
			canvas.getGraphicsContext2D().setStroke(Color.gray(0));
			canvas.getGraphicsContext2D().drawImage(this.droneImage, coordinate.getxValue()-25, coordinate.getyValue()-25);
		}
	}
	
	/* adds the next coordinate to the current active dynamic rectangle */
	public void addDynamicRectangleCordinate(Integer xCordinate, Integer yCordinate) {
		Coordinate coordinate = new Coordinate(xCordinate, yCordinate);
		DynamicRectangle lastDynamicRectangle = dynamicRectangles.get(dynamicRectangles.size()-1);
		int coordinateLength = lastDynamicRectangle.coordinates.size();
		if(coordinateLength > 0) {
			Coordinate lastCoordinate = lastDynamicRectangle.coordinates.get(coordinateLength - 1);
			if(!(lastCoordinate.getxValue().equals(xCordinate)) 
					&& !(lastCoordinate.getyValue().equals(yCordinate))) {
				float m = (float) (yCordinate - lastCoordinate.getyValue()) / (float) (xCordinate - lastCoordinate.getxValue());
				int xWidth = xCordinate - lastCoordinate.getxValue();
				if(xWidth > 0) {
					for(int i = 0; i < xWidth; i++) {
					    Integer x = lastCoordinate.getxValue() + i;
					    Integer y = (int) (lastCoordinate.getyValue() + (m * i));
					    Coordinate intermediateCoordinate = new Coordinate(x, y);
					    lastDynamicRectangle.coordinates.add(intermediateCoordinate);
					}
				} else {
					for(int i = 0; i > xWidth; i--) {
					    Integer x = lastCoordinate.getxValue() + i;
					    Integer y = (int) (lastCoordinate.getyValue() + (m * i));
					    Coordinate intermediateCoordinate = new Coordinate(x, y);
					    lastDynamicRectangle.coordinates.add(intermediateCoordinate);
					}
				}
				lastDynamicRectangle.coordinates.add(coordinate);
				Rectangle rectangle = new Rectangle((coordinate.getxValue()-(Constants.RECTANGLE_WIDTH/2)), 
						(coordinate.getyValue()-(Constants.RECTANGLE_HEIGHT/2)), 
						Constants.RECTANGLE_WIDTH, Constants.RECTANGLE_HEIGHT);
				canvas.getGraphicsContext2D().setStroke(Color.gray(0));
				canvas.getGraphicsContext2D().strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
				canvas.getGraphicsContext2D().setFill(Color.rgb(255, 0, 0, 0.5));
				canvas.getGraphicsContext2D().fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
				canvas.getGraphicsContext2D().setFill(Color.rgb(255, 255, 255, 1));
				canvas.getGraphicsContext2D().fillRect(rectangle.getCenterX()-2.5, rectangle.getCenterY()-2.5, 5, 5);
				canvas.getGraphicsContext2D().strokeLine(lastCoordinate.getxValue(),
						lastCoordinate.getyValue(), coordinate.getxValue(), coordinate.getyValue());
			} else if((lastCoordinate.getxValue().equals(xCordinate))
					&& (lastCoordinate.getyValue().equals(yCordinate))) {
			} else if(lastCoordinate.getxValue().equals(xCordinate)) {
				if(lastCoordinate.getyValue() > yCordinate) {
					Integer nextYValue = lastCoordinate.getyValue() - 1;
					while(!nextYValue.equals(yCordinate)) {
						Coordinate intermediateCoordinate = new Coordinate(xCordinate, nextYValue);
						lastDynamicRectangle.coordinates.add(intermediateCoordinate);
						nextYValue = nextYValue - 1;
					}
				} else if(lastCoordinate.getyValue() < yCordinate) {
					Integer nextYValue = lastCoordinate.getyValue() + 1;
					while(!nextYValue.equals(yCordinate)) {
						Coordinate intermediateCoordinate = new Coordinate(xCordinate, nextYValue);
						lastDynamicRectangle.coordinates.add(intermediateCoordinate);
						nextYValue = nextYValue + 1;
					}
				}
				lastDynamicRectangle.coordinates.add(coordinate);
				Rectangle rectangle = new Rectangle((coordinate.getxValue()-(Constants.RECTANGLE_WIDTH/2)), 
						(coordinate.getyValue()-(Constants.RECTANGLE_HEIGHT/2)), 
						Constants.RECTANGLE_WIDTH, Constants.RECTANGLE_HEIGHT);
				canvas.getGraphicsContext2D().setStroke(Color.gray(0));
				canvas.getGraphicsContext2D().strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
				canvas.getGraphicsContext2D().setFill(Color.rgb(255, 0, 0, 0.5));
				canvas.getGraphicsContext2D().fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
				canvas.getGraphicsContext2D().setFill(Color.rgb(255, 255, 255, 1));
				canvas.getGraphicsContext2D().fillRect(rectangle.getCenterX()-2.5, rectangle.getCenterY()-2.5, 5, 5);
				canvas.getGraphicsContext2D().strokeLine(lastCoordinate.getxValue(),
						lastCoordinate.getyValue(), coordinate.getxValue(), coordinate.getyValue());
			} else if(lastCoordinate.getyValue().equals(yCordinate)) {
				if(lastCoordinate.getxValue() > xCordinate) {
					Integer nextXValue = lastCoordinate.getxValue() - 1;
					while(!nextXValue.equals(xCordinate)) {
						Coordinate intermediateCoordinate = new Coordinate(nextXValue, yCordinate);
						lastDynamicRectangle.coordinates.add(intermediateCoordinate);
						nextXValue = nextXValue - 1;
					}
				} else if(lastCoordinate.getxValue() < xCordinate) {
					Integer nextXValue = lastCoordinate.getxValue() + 1;
					while(!nextXValue.equals(xCordinate)) {
						Coordinate intermediateCoordinate = new Coordinate(nextXValue, yCordinate);
						lastDynamicRectangle.coordinates.add(intermediateCoordinate);
						nextXValue = nextXValue + 1;
					}
				}
				lastDynamicRectangle.coordinates.add(coordinate);
				Rectangle rectangle = new Rectangle((coordinate.getxValue()-(Constants.RECTANGLE_WIDTH/2)), 
						(coordinate.getyValue()-(Constants.RECTANGLE_HEIGHT/2)), 
						Constants.RECTANGLE_WIDTH, Constants.RECTANGLE_HEIGHT);
				canvas.getGraphicsContext2D().setStroke(Color.gray(0));
				canvas.getGraphicsContext2D().strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
				canvas.getGraphicsContext2D().setFill(Color.rgb(255, 0, 0, 0.5));
				canvas.getGraphicsContext2D().fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
				canvas.getGraphicsContext2D().setFill(Color.rgb(255, 255, 255, 1));
				canvas.getGraphicsContext2D().fillRect(rectangle.getCenterX()-2.5, rectangle.getCenterY()-2.5, 5, 5);
				canvas.getGraphicsContext2D().strokeLine(lastCoordinate.getxValue(),
						lastCoordinate.getyValue(), coordinate.getxValue(), coordinate.getyValue());
			}
		} else {
			lastDynamicRectangle.coordinates.add(coordinate);
			Rectangle rectangle = new Rectangle((coordinate.getxValue()-(Constants.RECTANGLE_WIDTH/2)), 
					(coordinate.getyValue()-(Constants.RECTANGLE_HEIGHT/2)), 
					Constants.RECTANGLE_WIDTH, Constants.RECTANGLE_HEIGHT);
			canvas.getGraphicsContext2D().setStroke(Color.gray(0));
			canvas.getGraphicsContext2D().strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
			canvas.getGraphicsContext2D().setFill(Color.rgb(255, 0, 0, 0.5));
			canvas.getGraphicsContext2D().fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
			canvas.getGraphicsContext2D().setFill(Color.rgb(255, 255, 255, 1));
			canvas.getGraphicsContext2D().fillRect(rectangle.getCenterX()-2.5, rectangle.getCenterY()-2.5, 5, 5);
		}
	}
	
	/* check if a point being added already exists in the canvas */
	public Boolean checkIfAlreadyExist(Integer xCordinate, Integer yCordinate) {
		Boolean pointFound = false;
		if(rectangles != null) {
			Iterator<CustomRectangle> iterator = rectangles.iterator();
			while(iterator.hasNext()) {
				CustomRectangle rectangle = iterator.next();
				Integer xCord = (int) rectangle.getxCordinateCenter();
				Integer yCord = (int) rectangle.getyCordinateCenter();
				if((xCord.equals(xCordinate)) && (yCord.equals(yCordinate))) {
					pointFound = true;
					return pointFound;
				}
			}
		}
		return pointFound;
	}
	
	/* to check if the point of the canvas being clicked has an existing point
	 * within its 3X3 area for easier deletion*/
	public Boolean clickedOnPoint(int xCord, int yCord, int xCoordinate, int yCoordinate) {
		if((xCord + 3 > xCoordinate) && (xCord - 3 < xCoordinate)
				&& (yCord + 3 > yCoordinate) && (yCord - 3 < yCoordinate)) {
			return true;
		}
		return false;
	}
	
	/* check if the point exists for deletion */
	public Boolean checkIfAlreadyExistForDeletion(Integer xCordinate, Integer yCordinate) {
		Boolean pointFound = false;
		if(rectangles != null) {
			Iterator<CustomRectangle> iterator = rectangles.iterator();
			while(iterator.hasNext()) {
				CustomRectangle rectangle = iterator.next();
				Integer xCord = (int) rectangle.getxCordinateCenter();
				Integer yCord = (int) rectangle.getyCordinateCenter();
				if(clickedOnPoint(xCord, yCord, xCordinate, yCordinate)) {
					pointFound = true;
					return pointFound;
				}
			}
		}
		return pointFound;
	}
	
	/* select and highlight a rectangle in the canvas using the list of points */
	public void selectRectangle () {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		int selectedIndex = pointsList.getSelectionModel().getSelectedIndex();
		drawSmallerRectangles();
		if(selectedIndex != -1) {
			String selection = pointsList.getSelectionModel().getSelectedItem();
			String[] coordinates = selection.split(", ");
			Integer xCordinate = Integer.parseInt(coordinates[0]);
			Integer yCordinate = Integer.parseInt(coordinates[1]);
			Iterator<CustomRectangle> iterator = rectangles.iterator();
			int index = 0;
			Boolean pointFound = false;
			while(iterator.hasNext()) {
				CustomRectangle rectangle = iterator.next();
				Integer xCord = (int) rectangle.getxCordinateCenter();
				Integer yCord = (int) rectangle.getyCordinateCenter();
				if((xCord.equals(xCordinate)) && (yCord.equals(yCordinate))) {
					pointFound = true;
					Rectangle rec = rectangle.getRectangle();
					gc.setFill(Color.rgb(0, 0, 0, 1));
					gc.fillRect(rec.getCenterX()-2.5, rec.getCenterY()-2.5, 5, 5);
				}
				if(pointFound) {
					break;
				}
				index = index + 1;
			}
		}
	}
	
	/* removes the selected rectangle from the list of points */
	public void removeSelectedRectangles () {
		int selectedIndex = pointsList.getSelectionModel().getSelectedIndex();
		if(selectedIndex != -1) {
			String selection = pointsList.getSelectionModel().getSelectedItem();
			String[] coordinates = selection.split(", ");
			Integer xCordinate = Integer.parseInt(coordinates[0]);
			Integer yCordinate = Integer.parseInt(coordinates[1]);
			removeRectangle(xCordinate, yCordinate);
		} else {
			Alert alert = new Alert(AlertType.ERROR, "Select a rectangle to remove it from the canvas.", ButtonType.YES);
			alert.showAndWait();
		}
	}
	
	/* removes the rectangle selected from the list or by right clicking the center point
	 * of the rectangle */
	public void removeSelectedRectangle (Integer xCordinate, Integer yCordinate) {
		Iterator<CustomRectangle> iterator = rectangles.iterator();
		int index = 0;
		Boolean pointFound = false;
		ArrayList<Integer> deleteIndexs = new ArrayList<Integer>();
		while(iterator.hasNext()) {
			CustomRectangle rectangle = iterator.next();
			Integer xCord = (int) rectangle.getxCordinateCenter();
			Integer yCord = (int) rectangle.getyCordinateCenter();
			if((xCord.equals(xCordinate)) && (yCord.equals(yCordinate))) {
				pointFound = true;

				Iterator<SeparatedRectangle> recIterator = smallerRectangles.iterator();
				while(recIterator.hasNext()) {
					SeparatedRectangle separatedRectangle = recIterator.next();
					Rectangle rec = separatedRectangle.getRectangle();
					if(rectangle.getRectangle().contains(rec)) {
						separatedRectangle.setIntersectionCount(separatedRectangle.getIntersectionCount() - 1);
						if(separatedRectangle.getIntersectionCount() == 0) {
							float[] coords = new float[] {separatedRectangle.getRectangle().x, separatedRectangle.getRectangle().y};
							float[] dimensions = new float[] {(float) separatedRectangle.getRectangle().width, 
									(float) separatedRectangle.getRectangle().height};
							smallerRectanglesRtree.delete(coords, dimensions, separatedRectangle);
							recIterator.remove();
						}
					}
				}

				Iterator<SweepCoordinate> sweepIterator = sweepCoordinates.iterator();		
				int count = 0;
				while(sweepIterator.hasNext()) {
					SweepCoordinate sweep = sweepIterator.next();
					if((sweep.getRectangle() != null) && (sweep.getRectangle().equals(rectangle))) {
						deleteIndexs.add(count);
					}
					count = count + 1;
				}
				Collections.reverse(deleteIndexs);
				break;
			}
			index = index + 1;
		}
		if(pointFound) {
			rectangles.remove(index);
			Iterator<Integer> deleteIterator = deleteIndexs.iterator();
			while(deleteIterator.hasNext()) {
				int currentIndex = deleteIterator.next();
				sweepCoordinates.remove(currentIndex);
			}
			ObservableList<String> points = FXCollections.observableArrayList();
			points = pointsList.getItems();
			String removeCoordinate = xCordinate + ", " + yCordinate;
			Iterator<String> pointListIterator = points.iterator();
			while(pointListIterator.hasNext()) {
				String coordinate = pointListIterator.next();
				if(coordinate.equals(removeCoordinate)) {
					pointListIterator.remove();
				}
			}
		}
		maxIntersection();
		drawSmallerRectangles();
	}
	
	/* compute static crest algorithm */
	public void computeCrestAlgorithm() {
		smallerRectangles = new ArrayList<SeparatedRectangle>();
		Integer[] xValuePair = new Integer[2];
		ArrayList<Integer> yValues = new ArrayList<Integer>();
		ListIterator<SweepCoordinate> sweep = sweepCoordinates.listIterator();
		while (sweep.hasNext()) {
			SweepCoordinate coordinate = sweep.next();
			xValuePair[0] = xValuePair[1];
			xValuePair[1] = coordinate.getxValue();
			if (xValuePair[0] == null) {
				xValuePair[0] = 0;
			}
			ListIterator<Integer> iter = yValues.listIterator();
			int index = 0;
			while (iter.hasNext()) {
				Integer yCordinate = iter.next();
				if (index != yValues.size() - 1) {
					Rectangle rectangle = new Rectangle();
					rectangle.x = xValuePair[0];
					rectangle.width = xValuePair[1] - xValuePair[0];
					Integer heightEndPoint = iter.next();
					iter.previous();
					rectangle.y = yCordinate;
					rectangle.height = heightEndPoint - yCordinate;

					SeparatedRectangle smallRectangle = new SeparatedRectangle(rectangle);
					Iterator<CustomRectangle> input = rectangles.iterator();
					int count = 0;
					while (input.hasNext()) {
						Rectangle inputRec = input.next().getRectangle();
						if (inputRec.intersects(rectangle)) {
							count = count + 1;
						}
					}
					smallRectangle.setIntersectionCount(count);
					if (count > this.maximumIntersection) {
						this.maximumIntersection = count;
					}
					if (count > 0) {
						smallerRectangles.add(smallRectangle);
						float[] coords = new float[] {rectangle.x, rectangle.y};
						float[] dimensions = new float[] {rectangle.height, rectangle.width};
						smallerRectanglesRtree.insert(coords, dimensions, smallRectangle);
					}
					index = index + 1;
				}
			}
			if (coordinate.getRectangle() != null) {
				if (!coordinate.getEndPoint()) {
					yValues.add(coordinate.getRectangle().getyTop());
					yValues.add(coordinate.getRectangle().getyBottom());
					Collections.sort(yValues);
				} else {
					if (coordinate.getRectangle() != null) {
						yValues.remove(coordinate.getRectangle().getyTop());
						yValues.remove(coordinate.getRectangle().getyBottom());
					}
				}
			}
		}
	}
	
	/* compute dynamic crest algorithm */
	public void computeCrestDynamicAlgorithm () {
		Integer[] xValuePair = new Integer[2];
		ArrayList<Integer> yValues = new ArrayList<Integer>();
		ListIterator<SweepCoordinate> sweep = tempSweepCoordinates.listIterator();
		while (sweep.hasNext()) {
			SweepCoordinate coordinate = sweep.next();
			xValuePair[0] = xValuePair[1];
			xValuePair[1] = coordinate.getxValue();
			if (xValuePair[0] == null) {
				xValuePair[0] = 0;
			}
			ListIterator<Integer> iter = yValues.listIterator();
			int index = 0;
			while (iter.hasNext()) {
				Integer yCordinate = iter.next();
				if (index != yValues.size() - 1) {
					Rectangle rectangle = new Rectangle();
					rectangle.x = xValuePair[0];
					rectangle.width = xValuePair[1] - xValuePair[0];
					Integer heightEndPoint = iter.next();
					iter.previous();
					rectangle.y = yCordinate;
					rectangle.height = heightEndPoint - yCordinate;

					SeparatedRectangle smallRectangle = new SeparatedRectangle(rectangle);
					Iterator<CustomRectangle> input = rectangles.iterator();
					int count = 0;
					while (input.hasNext()) {
						Rectangle inputRec = input.next().getRectangle();
						if (inputRec.intersects(rectangle)) {
							count = count + 1;
						}
					}
					smallRectangle.setIntersectionCount(count);
					if (count > this.maximumIntersection) {
						this.maximumIntersection = count;
					}
					if (count > 0) {
						smallerRectangles.add(smallRectangle);
						float[] coords = new float[] {rectangle.x, rectangle.y};
						float[] dimensions = new float[] {rectangle.width, rectangle.height};
						smallerRectanglesRtree.insert(coords, dimensions, smallRectangle);
					}
					index = index + 1;
				}
			}
			if (coordinate.getRectangle() != null) {
				if (!coordinate.getEndPoint()) {
					yValues.add(coordinate.getRectangle().getyTop());
					yValues.add(coordinate.getRectangle().getyBottom());
					Collections.sort(yValues);
				} else {
					if (coordinate.getRectangle() != null) {
						yValues.remove(coordinate.getRectangle().getyTop());
						yValues.remove(coordinate.getRectangle().getyBottom());
					}
				}
			}
		}
	}
	
	/* check the maximum intersection between rectangles */
	public void maxIntersection() {
		int intersection = 0;
		Iterator<SeparatedRectangle> iter = smallerRectangles.iterator();
		while(iter.hasNext()) {
			SeparatedRectangle rec = iter.next();
			if(rec.getIntersectionCount() > intersection) {
				intersection = rec.getIntersectionCount();
			}
		}
		maximumIntersection = intersection;
	}
	
	/* draw the rectangles on the canvas */
	public void drawSmallerRectangles() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		setImage();
		Iterator<SeparatedRectangle> newRectangles = smallerRectangles.iterator();
		
		// color of the smaller rectangles is varied based on the maximum number of intersections
		double colorMultiplier = 1/this.maximumIntersection;
		
		newRectangles = smallerRectangles.iterator();
		while(newRectangles.hasNext()) {
			SeparatedRectangle separatedRectangle = newRectangles.next();
			if(separatedRectangle.getIntersectionCount() > 0) {
				Rectangle rectangle = separatedRectangle.getRectangle();
				gc.setFill(Color.gray(1-(colorMultiplier*separatedRectangle.getIntersectionCount()), this.opacity));
				gc.fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
				gc.setStroke(Color.gray(1-(colorMultiplier*separatedRectangle.getIntersectionCount()), this.opacity));
				gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
			}
		}
		
		Iterator<CustomRectangle> input = rectangles.iterator();
		while(input.hasNext()) {
			Rectangle rectangle = input.next().getRectangle();
			gc.setFill(Color.rgb(255, 255, 255, 1));
			gc.fillRect(rectangle.getCenterX()-2.5, rectangle.getCenterY()-2.5, 5, 5);
		}
	}
	
	// add a background image to the canvas (typically a map)
	public void changeBackground(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extentionFilter = new FileChooser.ExtensionFilter("Image files", "*.jpg", "*.png");
		fileChooser.getExtensionFilters().add(extentionFilter);
		
		String userDirectoryString = System.getProperty("user.home") + "/Desktop";
		File userDirectory = new File(userDirectoryString);
		fileChooser.setInitialDirectory(userDirectory);
		
		File selectedFile = fileChooser.showOpenDialog(null);
		
		if(selectedFile != null) {
			this.imagePath = selectedFile.getAbsolutePath();
			File file = new File(selectedFile.getAbsolutePath());
		    Image image = new Image(file.toURI().toString(), 550, 550, true, true);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0);;
		}
		drawSmallerRectangles();
	}
	
	public void setImage() {
		try {
			File file = new File(this.imagePath);
		    Image image = new Image(file.toURI().toString(), 550, 550, true, true);
	        canvas.getGraphicsContext2D().drawImage(image, 0, 0);
		} catch (Exception ex) {
			canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		}
	}
	
	/* increase the opacity of the data points */
	public void increaseOpacity() {
		if(this.opacity < 1) {
			this.opacity = this.opacity + 0.05;
			if(this.opacity > 1.0) {
				this.opacity = 1;
			}
			drawSmallerRectangles();
		}
	}
	
	/* decrease the opacity of the data points */
	public void decreaseOpacity() {
		if(this.opacity > 0) {
			this.opacity = this.opacity - 0.05;
			if(this.opacity < 0) {
				this.opacity = 0;
			}
			drawSmallerRectangles();
		}
	}
	
	/* upload data points and the map image from the Open load map */
	public void uploadMapData(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extentionFilter = new FileChooser.ExtensionFilter("Open Street Map Data", "*.osm");
		fileChooser.getExtensionFilters().add(extentionFilter);
		
		String userDirectoryString = System.getProperty("user.home") + "/Desktop";
		File userDirectory = new File(userDirectoryString);
		fileChooser.setInitialDirectory(userDirectory);
		
		File selectedFile = fileChooser.showOpenDialog(null);
		
		if(selectedFile != null) {
			this.imagePath = selectedFile.getAbsolutePath();
			this.imagePath = this.imagePath.substring(0, this.imagePath.length()-4) + ".png";
			File file = new File(this.imagePath);
		    Image image = new Image(file.toURI().toString(), 550, 550, true, true);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0);
    		drawSmallerRectangles();
    		
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    File dataFile = new File(selectedFile.getAbsolutePath());
			    Document doc = builder.parse(dataFile.toURI().toString());
			    
			    XPathFactory xPathfactory = XPathFactory.newInstance();
			    XPath xpath = xPathfactory.newXPath();
			    
			    Integer maxLat = 0;
			    Integer minLat = 0;
			    Integer maxLon = 0;
			    Integer minLon = 0;
			    
			    XPathExpression boundExpression = xpath.compile("//bounds");
			    NodeList boundList = (NodeList) boundExpression.evaluate(doc, XPathConstants.NODESET);
			    for(int boundCount=0; boundCount<boundList.getLength(); boundCount++) {
			    	NamedNodeMap boundAttributes = boundList.item(boundCount).getAttributes();
		        	for(int attributeCount = 0 ; attributeCount<boundAttributes.getLength() ; attributeCount++) {
	    		        Attr attribute = (Attr)boundAttributes.item(attributeCount);
	    		        if(attribute.getName().equals("maxlat")) {
	    		        	Float value = Float.valueOf(attribute.getValue()) * 10000000;
	    		        	maxLat = value.intValue();
	    		        } else if (attribute.getName().equals("minlat")) {
	    		        	Float value = Float.valueOf(attribute.getValue()) * 10000000;
	    		        	minLat = value.intValue();
	    		        } else if (attribute.getName().equals("maxlon")) {
	    		        	Float value = Float.valueOf(attribute.getValue()) * 10000000;
	    		        	maxLon = value.intValue();
	    		        } else if (attribute.getName().equals("minlon")) {
	    		        	Float value = Float.valueOf(attribute.getValue()) * 10000000;
	    		        	minLon = value.intValue();
	    		        } 
		        	}
			    }
			    Integer latFactor = (maxLat - minLat)/550;
			    Integer lonFactor = (maxLon - minLon)/550;
			    
			    XPathExpression expression = xpath.compile("//node");
			    
			    NodeList nl = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
			    for(int l=0; l<nl.getLength(); l++) {
			    	NodeList nl2 = nl.item(l).getChildNodes();
			    	for(int i=0; i<nl2.getLength(); i++) {
			    		if(nl2.item(i).getNodeName().equals("tag")) {
			    			NamedNodeMap attrs = nl2.item(i).getAttributes();
			    			for(int k = 0 ; k<attrs.getLength() ; k++) {
			    		        Attr attribute = (Attr)attrs.item(k);     
			    		        if(attribute.getName().equals("v") && attribute.getValue().equals("traffic_signals")) {
			    		        	NamedNodeMap attrs2 = nl.item(l).getAttributes();
			    		        	Integer yValue = 0;
			    		        	Integer xValue = 0;
			    		        	for(int j = 0 ; j<attrs2.getLength() ; j++) {
					    		        Attr attribute2 = (Attr)attrs2.item(j);
					    		        if(attribute2.getName().equals("lat")) {
					    		        	Float value = Float.valueOf(attribute2.getValue()) * 10000000;
					    		        	Integer latitude = value.intValue();
					    		        	yValue = ((maxLat - latitude)/latFactor);
					    		        } else if (attribute2.getName().equals("lon")) {
					    		        	Float value = Float.valueOf(attribute2.getValue()) * 10000000;
					    		        	Integer longitude = value.intValue();
					    		        	xValue = ((longitude - minLon)/lonFactor);
					    		        } 
			    		        	}
			    		        	if((yValue > 0) && (xValue > 0)) {
			    		        		Boolean existingRectangle = checkIfAlreadyExist(xValue, yValue);
			    		        		if(!existingRectangle) {
			    		        			CustomRectangle customRectangle;
			    		        			customRectangle = new CustomRectangle(xValue, yValue);
			    		        			newRectangles.add(customRectangle);
			    		        		}
			    		        	}
			    		        }
			    		    }
			    		}
			    	}
			    }
        		uploadData();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void uploadData() {
		Iterator<CustomRectangle> rectangleIterator = newRectangles.iterator();
		while(rectangleIterator.hasNext()) {
			CustomRectangle customRectangle = rectangleIterator.next();
			addToList(customRectangle, customRectangle.getxCordinateCenter(), 
					customRectangle.getyCordinateCenter());
		}
		computeCrestAlgorithm();
		drawSmallerRectangles();
	}
	
	// add a new drone
	public void addDrones() {
		if(!(this.addingDynamicRectangle || this.dynamicRectangleSimulation)) {
			this.addingDrone = true;
			this.addingCordinates = true;
			Drone drone = new Drone();
			drones.add(drone);
		}
	}
	
	// start simulation of the drones
	public void simulateDrones() {
		if(!(this.addingDynamicRectangle || this.dynamicRectangleSimulation)) {
			this.droneSimulation = true;
		}
	}
	
	// add a dynamic data point
	public void addDynamicRectangle() {
		if(!(this.addingDrone || this.droneSimulation)) {
			this.addingDynamicRectangle = true;
			this.addingDynamicRectangleCordinates = true;
			DynamicRectangle dynamicRectangle = new DynamicRectangle();
			dynamicRectangles.add(dynamicRectangle);
		}
	}
	
	// start simulation of the dynamic data point
	public void simulateDynamicRectangles() {
		if(!(this.addingDrone || this.droneSimulation)) {
			this.dynamicRectangleSimulation = true;
		}
	}
	
	/* check whether the canvas interaction is for the drone or the dynamic point */
	public void moveDroneOrRectangle() {
		if(this.droneSimulation) {
			moveDrone();
		} else if (this.dynamicRectangleSimulation) {
			moveDynamicRectangle();
		}
	}
	
	/* move the drone during simulation */
	public void moveDrone() {
		drawSmallerRectangles();
		int noOfDrones = drones.size();
		for(int i=0; i < noOfDrones; i++) {
			Drone drone = drones.get(i);
			if(drone.coordinates.size() > 0) {
				Coordinate coordinate = drone.coordinates.get(0);
				canvas.getGraphicsContext2D().drawImage(this.droneImage, coordinate.getxValue()-25, coordinate.getyValue()-25);
				
				Rectangle rectangle = new Rectangle((coordinate.getxValue()-(Constants.RECTANGLE_WIDTH/2)), 
						(coordinate.getyValue()-(Constants.RECTANGLE_HEIGHT/2)), 
						Constants.RECTANGLE_WIDTH, Constants.RECTANGLE_HEIGHT);
				
				Iterator<CustomRectangle> input = rectangles.iterator();
				while(input.hasNext()) {
					Rectangle inputRectangle = input.next().getRectangle();
					int x = (int) inputRectangle.getCenterX();
					int y = (int) inputRectangle.getCenterY();
					Point point = new Point(x, y);
					if(rectangle.contains(point)) {
						canvas.getGraphicsContext2D().setFill(Color.rgb(255, 0, 0, 1));
						canvas.getGraphicsContext2D().fillRect(inputRectangle.getCenterX()-2.5, inputRectangle.getCenterY()-2.5, 5, 5);
					}
				}
				drone.coordinates.remove(0);
			}
		}
		Boolean moreCoordinatesRemanining = false;
		for(int i=0; i < noOfDrones; i++) {
			Drone drone = drones.get(i);
			if(drone.getCoordinates().size() > 0) {
				moreCoordinatesRemanining = true;
			}
		}
		if(!moreCoordinatesRemanining) {
			drawSmallerRectangles();
			drones = new ArrayList<Drone>();
			this.droneSimulation = false;
		}
	}
	
	/* calculate crest algorithm for the dynamic point while simulation */
	public void calculateCrestForDynamicRectangle(Integer xCordinate, Integer yCordinate) {
		tempSweepCoordinates.clear();
		if(rectangles == null) {
			rectangles = new ArrayList<CustomRectangle>();
		}
		initialiseSweep();
		CustomRectangle rectangle;
		rectangle = new CustomRectangle(xCordinate, yCordinate);
		addToList(rectangle, xCordinate, yCordinate);
		addToTempList(rectangle);

		float[] topLeftCordinates = new float[] {rectangle.getxLeft(), rectangle.getyTop()};
		float[] dimension = new float[] {(float) rectangle.getRectangle().width, 
				(float) rectangle.getRectangle().height};
		List<SeparatedRectangle> intersectingRectangles = 
				smallerRectanglesRtree.search(topLeftCordinates, dimension);

		Iterator<SeparatedRectangle> listIterator = intersectingRectangles.iterator();
		while(listIterator.hasNext()) {
			SeparatedRectangle smallRectangle = listIterator.next();
			if(smallRectangle.getRectangle().intersects(rectangle.getRectangle())) {
				float[] coords = new float[] {smallRectangle.getRectangle().x, smallRectangle.getRectangle().y};
				float[] dimensions = new float[] {(float) smallRectangle.getRectangle().width, 
						(float) smallRectangle.getRectangle().height};
				smallerRectanglesRtree.delete(coords, dimensions, smallRectangle);
			} else {
				listIterator.remove();
			}
		}

		Iterator<SeparatedRectangle> smallIterator = smallerRectangles.iterator();
		while(smallIterator.hasNext()) {
			SeparatedRectangle smallRectangle = smallIterator.next();
			if(intersectingRectangles.contains(smallRectangle)) {
				CustomRectangle newCustomRectangle = new CustomRectangle(smallRectangle.getRectangle().x, 
						smallRectangle.getRectangle().y, smallRectangle.getRectangle().height, 
						smallRectangle.getRectangle().width, true);
				addToTempList(newCustomRectangle);
				smallIterator.remove();
			}
		}
		computeCrestDynamicAlgorithm();
		drawSmallerRectangles();
	}
	
	/* move the dynamic data point during simulation */
	public void moveDynamicRectangle() {
		Iterator<Coordinate> iter = previousPoints.iterator();
		while(iter.hasNext()) {
			Coordinate previousCoordinate = iter.next();
			removeSelectedRectangle(previousCoordinate.getxValue(), previousCoordinate.getyValue());
		}
		int noOfDynamicRectangles = dynamicRectangles.size();
		for(int i=0; i < noOfDynamicRectangles; i++) {
			DynamicRectangle dynamicRectangle = dynamicRectangles.get(i);
			if(dynamicRectangle.coordinates.size() > 0) {
				Coordinate coordinate = dynamicRectangle.coordinates.get(0);
				previousPoints.add(coordinate);
				calculateCrestForDynamicRectangle(coordinate.getxValue(), coordinate.getyValue());
				dynamicRectangle.coordinates.remove(0);
				drawSmallerRectangles();
			}
		}
		Boolean moreCoordinatesRemanining = false;
		for(int i=0; i < noOfDynamicRectangles; i++) {
			DynamicRectangle dynamicRectangle = dynamicRectangles.get(i);
			if(dynamicRectangle.getCoordinates().size() > 0) {
				moreCoordinatesRemanining = true;
			}
		}
		if(!moreCoordinatesRemanining) {
			iter = previousPoints.iterator();
			while(iter.hasNext()) {
				Coordinate previousCoordinate = iter.next();
				removeSelectedRectangle(previousCoordinate.getxValue(), previousCoordinate.getyValue());
			}
			drawSmallerRectangles();
			dynamicRectangles = new ArrayList<DynamicRectangle>();
			this.dynamicRectangleSimulation = false;
		}
	}
	
}
