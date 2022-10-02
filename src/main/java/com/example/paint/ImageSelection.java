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
    public void setSelectionImage(Image selectionImage) {this.selectionImage = selectionImage;}
    public void setState(int selection){this.state = selection;}
    public int getState(){return state;}
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
    public double getx1(){return selectionCoordinates[0];}
    public double gety1(){return selectionCoordinates[1];}
    public double getw(){return selectionCoordinates[2];}
    public double geth(){return selectionCoordinates[3];}
}
