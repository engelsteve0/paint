//Steven Engel
//MyCanvas.java
//This file provides a way for each canvas to store data associated with it, such as the file being worked on.
package com.example.paint;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.Stack;

public class MyCanvas extends Canvas{
    private File lastSaved; //stores file that was previously saved for this canvas
    private boolean dirty;
    private Stack<Image> undoStack;                //stores operations on associated canvas. Used for undo/redo operations
    private Stack<Image> redoStack;
    public MyCanvas(int w, int h){  //calls Canvas's constructor with width and height
        super(w, h);
        this.setStyle("-fx-background-color: white"); //set background manually
        dirty = false;              //fresh file, no changes have been made
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }
    public File getLastSaved() { return lastSaved;} //accessor/mutator methods for getting and setting the file that was last saved
    public void setLastSaved(File file) {lastSaved = file;}
    public boolean getDirty() {return dirty;}
    public Stack<Image> getUndoStack(){
        return undoStack;
    }
    public Stack<Image> getRedoStack(){
        return redoStack;
    }
    public void setDirty(boolean isDirty) {
        dirty = isDirty;
        try{
            PaintApplication.updateTab((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()); //updates tab with * or not depending on dirty status
        }
        catch(Exception e){}
    }
    public void clearCanvas(){
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.WHITE);                                        //fills in canvas with white (slightly distinct from gray background)
        gc.fillRect(0, 0, getWidth(), getHeight());               //sets gc to canvas size
        updateUndoStack();       //updates undo stack to retain initial copy of image
    }
    public void updateUndoStack(){
        double ogx = getScaleX();//stores original x and y scales to reset after save
        double ogy = getScaleY();
        setScaleX(1);            //briefly sets canvas scale to default to avoid saving errors
        setScaleY(1);
        WritableImage writableImage = new WritableImage((int) getWidth(), (int) getHeight());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image image = snapshot(params, writableImage);
        setScaleX(ogx);            //set back to original scale
        setScaleY(ogy);

        undoStack.push(image);

    }
}
