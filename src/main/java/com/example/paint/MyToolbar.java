package com.example.paint;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToolBar;
import javafx.scene.paint.Color;

public class MyToolbar extends ToolBar {
    private ColorPicker cp;
    public MyToolbar(){ //calls Toolbar's constructor with no args
        super();
        this.cp = new ColorPicker(Color.BLACK);   //creates a new color picker. To be used with
        this.getItems().add(cp);                 //adds items to toolbar
    }
}
