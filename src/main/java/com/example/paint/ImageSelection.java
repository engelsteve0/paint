//Steven Engel
//ImageSelection.java
//A class for storing information related to the selected portion of each tab
package com.example.paint;

import javafx.scene.image.Image;

/**
 * @author Steven Engel
 * @ImageSelection.java: A class for storing information related to the selected portion of each tab
 */
public class ImageSelection {
    private int state;  //used to keep track of if this tab's canvas/layer has a selection/progression through interactions.
                        //0 = no selection 1 = just selected/selection not clicked 2 = actively moving selection
    private Image selectionImage; //stores snapshot of what was selected
    private double[] selectionCoordinates = new double[4];
    public ImageSelection(){    //empty constructor

    }

    /**
     * Sets the Image that is stored in this SelectionImage
     * @param selectionImage Image
     */
    public void setSelectionImage(Image selectionImage) {this.selectionImage = selectionImage;}

    /**
     * Sets the state (0, 1, 2) that this selection is in
     * @param selection int
     */
    public void setState(int selection){this.state = selection;}

    /**
     * Returns the state (0, 1, 2) that this selection is in
     * @return int
     */
    public int getState(){return state;}

    /**
     * Accessor for the actual image stored in this ImageSelection
     * @return Image
     */
    public Image getSelectionImage(){return selectionImage;}

    /**
     * Stores initial position, width, and height in array for access later.
     * @param x1 the x coordinate of where the user originally clicked on the canvas
     * @param y1 the y coordinate of where the user originally clicked on the canvas
     * @param w the width between where the user originally clicked and the other side of the rectangle
     * @param h the height between where the user originally clicked and the other side of the rectangle
     */
    public void setSelectionCoordinates(double x1, double y1, double w, double h){
        selectionCoordinates[0]=x1; //stores initial position, width, and height in array for access later
        selectionCoordinates[1]=y1;
        selectionCoordinates[2]=w;
        selectionCoordinates[3]=h;
    }

    /**
     * Accessor for the x value of this selection's origin
     * @return double
     */
    public double getx1(){return selectionCoordinates[0];}

    /**
     * Accessor for the y value of this selection's origin
     * @return double
     */
    public double gety1(){return selectionCoordinates[1];}

    /**
     * Accessor for the width of this selection
     * @return double
     */
    public double getw(){return selectionCoordinates[2];}

    /**
     * Accessor for the height of this selection
     * @return double
     */
    public double geth(){return selectionCoordinates[3];}
}
