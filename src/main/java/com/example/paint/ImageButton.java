//Steven Engel
//ImageButton.java
//This file provides an easy shortcut for making ToggleButtons with images as their icons. This is useful for the tools in MyToolbar.java
//It also communicates to MyToolbar which tool has been selected to determine mouse functionality on the canvas.
package com.example.paint;

import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Steven Engel
 * This file provides an easy shortcut for making ToggleButtons with images as their icons. This is useful for the tools in MyToolbar.java.
 * It also communicates to MyToolbar which tool has been selected to determine mouse functionality on the canvas.
 */
public class ImageButton extends ToggleButton {
    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 2, 2, 2, 2;"; //defines various styles
    private final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 3 1 1 3;";   //pressed is not currently used but may be added for visual polish in the future
    private final String STYLE_SELECTED = "-fx-background-color: transparent; -fx-padding: 2, 2, 2, 2; -fx-border-color: orange; -fx-border-width: 1 1 1 1;";

    /**
     * Creates the ImageButton of a certain icon, size, and associated id (for use with MyToolbar.java).
     * @param originalImage the icon
     * @param h the icon's height in pixels
     * @param w the icon's width in pixels
     * @param buttonId the stored button ID for use with MyToolbar.java
     */
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
                switch (buttonId){
                    case 0: LogHandler.getLogHandler().writeToLog(true, "Pencil tool selected."); break;
                    case 1: LogHandler.getLogHandler().writeToLog(true, "Eraser tool selected."); break;
                    case 2: LogHandler.getLogHandler().writeToLog(true, "Straight line tool selected."); break;
                    case 3: LogHandler.getLogHandler().writeToLog(true, "Color picker tool selected."); break;
                    case 4: LogHandler.getLogHandler().writeToLog(true, "Dashed line tool selected."); break;
                    case 5: LogHandler.getLogHandler().writeToLog(true, "Rectangle/square tool selected."); break;
                    case 6: LogHandler.getLogHandler().writeToLog(true, "Ellipse/circle tool selected."); break;
                    case 7: LogHandler.getLogHandler().writeToLog(true, "Polygon tool selected."); break;
                    case 8: LogHandler.getLogHandler().writeToLog(true, "Rectangular selection tool selected."); break;
                    case 9: LogHandler.getLogHandler().writeToLog(true, "Axes tool selected."); break;
                    case 10: LogHandler.getLogHandler().writeToLog(true, "Text tool selected."); break;
                }
            }
            else {
                PaintApplication.getToolbar().setSelectedTool(-1);              //otherwise, deselect
                PaintApplication.getScrollPane().setPannable(true);             //pan tool by default
                LogHandler.getLogHandler().writeToLog(true, "Pan tool selected.");
            }
        });

    }

    /**
     * Returns specific style selected so that it can be set here once
     * @param type a string representing the style type, such as "selected"
     * @return a String storing the associated formatting information
     */
    public String getStyleType(String type){   //returns specific style selected so that it can be set here once
        if (type.equals("selected")){
            return STYLE_SELECTED;
        }
        else
            return STYLE_NORMAL;
    }

}
