//Steven Engel
//ImageButton.java
//This file provides an easy shortcut for making ToggleButtons with images as their icons. This is useful for the tools in MyToolbar.java
//It also communicates to MyToolbar which tool has been selected to determine mouse functionality on the canvas.
package com.example.paint;

import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
public class ImageButton extends ToggleButton {

    private int buttonId;
    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 2, 2, 2, 2;"; //defines various styles
    private final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 3 1 1 3;";   //pressed is not currently used but may be added for visual polish in the future
    private final String STYLE_SELECTED = "-fx-background-color: transparent; -fx-padding: 2, 2, 2, 2; -fx-border-color: black; -fx-border-width: 1 1 1 1;";

    public ImageButton(Image originalImage, double h, double w, int buttonId) {
        super();                                                //start with ToggleButton constructor
        ImageView image = new ImageView(originalImage);         //set the given image to be this button's icon
        image.setFitHeight(h);
        image.setFitHeight(w);
        image.setPreserveRatio(true);
        setGraphic(image);
        setStyle(STYLE_NORMAL);                                 //default is not selected

        setOnAction(e -> { //if this ImageButton is clicked on, toggle it
            if(PaintApplication.getToolbar().getSelectedTool()!=buttonId) {     //if the previous selection was different than this, set this as the new buttonId selected
                PaintApplication.getToolbar().setSelectedTool(buttonId);
                PaintApplication.getScrollPane().setPannable(false);             //stop panning
                if(buttonId==7){                                                 //if this is the polygon button, prompt user for number of sides
                    PaintApplication.getToolbar().promptNumSides();
                }
            }
            else {
                PaintApplication.getToolbar().setSelectedTool(-1);              //otherwise, deselect
                PaintApplication.getScrollPane().setPannable(true);             //pan tool by default
            }
        });

    }
    public String getStyleType(String type){   //returns specific style selected so that it can be set here once
        if (type.equals("selected")){
            return STYLE_SELECTED;
        }
        else
            return STYLE_NORMAL;
    }

}
