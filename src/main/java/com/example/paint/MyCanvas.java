//Steven Engel
//MyCanvas.java
//This file provides a way for each canvas to store data associated with it, such as the file being worked on.
package com.example.paint;

import javafx.scene.canvas.Canvas;
import java.io.File;

public class MyCanvas extends Canvas implements Cloneable{
    private File lastSaved; //stores file that was previously saved for this canvas
    private boolean dirty;
    public MyCanvas(int w, int h){  //calls Canvas's constructor with width and height
        super(w, h);
        dirty = false;              //fresh file, no changes have been made
    }
    public File getLastSaved() { return lastSaved;} //accessor/mutator methods for getting and setting the file that was last saved
    public void setLastSaved(File file) {lastSaved = file;}
    public boolean getDirty() {return dirty;}
    public void setDirty(boolean isDirty) {
        dirty = isDirty;
        try{
            PaintApplication.updateTab((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()); //updates tab with * or not depending on dirty status
        }
        catch(Exception e){}
    }
}
