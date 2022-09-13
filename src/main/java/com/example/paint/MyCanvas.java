package com.example.paint;

import javafx.scene.canvas.Canvas;

import java.io.File;

public class MyCanvas extends Canvas implements Cloneable{
    private File lastSaved; //stores file that was previously saved for this canvas
    public MyCanvas(int w, int h){  //calls Canvas's constructor with width and height
        super(w, h);
    }
    public File getLastSaved() { return lastSaved;} //accessor/mutator methods for getting and setting the file that was last saved
    public void setLastSaved(File file) {lastSaved = file;}

}
