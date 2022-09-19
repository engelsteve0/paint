//Steven Engel
//MyTab.java
//The MyTab class is a tab which stores extra information, such as the canvas assigned to it, as well as overriding the exit method to warn user to save their work
package com.example.paint;

import javafx.scene.control.Tab;

import java.util.Iterator;
import java.util.function.Consumer;

public class MyTab extends Tab{
    private MyCanvas currentCanvas;
    public MyTab(MyCanvas canvas){
        super();
        setCurrentCanvas(canvas);
    }
    public void setCurrentCanvas(MyCanvas canvas){
        this.currentCanvas = canvas;
    }
    public MyCanvas getCurrentCanvas(){
        return currentCanvas;
    }
    public String getTabName(){
        String tabName = "";
        try{
            int i = currentCanvas.getLastSaved().toString().lastIndexOf("\\");    //gets name of current file, without path
            if (i > 0) {
                tabName = currentCanvas.getLastSaved().toString().substring(i+1);
            }
            if(currentCanvas.getDirty())    //add a star if this has been modified since last save, or is new
                tabName+="*";
        }
        catch(Exception e){             //if this file has not been saved yet, say New Image
            tabName = "New Image*";
        }
        return tabName;
    }
}
