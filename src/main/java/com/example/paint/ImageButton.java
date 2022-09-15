package com.example.paint;

import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageButton extends ToggleButton {

    private int buttonId;
    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 2, 2, 2, 2;";
    private final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 3 1 1 3;";
    private final String STYLE_SELECTED = "-fx-background-color: transparent; -fx-padding: 2, 2, 2, 2; -fx-border-color: black; -fx-border-width: 1 1 1 1;";

    public ImageButton(Image originalImage, double h, double w, int buttonId) {
        super();
        ImageView image = new ImageView(originalImage);
        image.setFitHeight(h);
        image.setFitHeight(w);
        image.setPreserveRatio(true);
        setGraphic(image);
        setStyle(STYLE_NORMAL);

        setOnAction(e -> {
            if(PaintApplication.getToolbar().getSelectedTool()!=buttonId) {     //if the previous selection was different than this, set this as the new buttonId selected
                PaintApplication.getToolbar().setSelectedTool(buttonId);
            }
            else
                PaintApplication.getToolbar().setSelectedTool(-1);              //otherwise, deselect
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
