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

/**
 * @author Steven Engel
 *
 * This class provides a way for each canvas to store data associated with it, such as the file being worked on.
 */
public class MyCanvas extends Canvas{
    private File lastSaved; //stores file that was previously saved for this canvas
    private boolean dirty;
    private Stack<Image> undoStack;                //stores operations on associated canvas. Used for undo/redo operations
    private Stack<Image> redoStack;

    /**
     * Calls Canvas's constructor with width and height, and creates associated stack.
     * @param w the width of this canvas
     * @param h the height of this canvas
     */
    public MyCanvas(int w, int h){  //calls Canvas's constructor with width and height
        super(w, h);
        this.setStyle("-fx-background-color: white"); //set background manually
        dirty = false;              //fresh file, no changes have been made
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Accessor for the file that was last saved to from this canvas
     * @return File
     */
    public File getLastSaved() { return lastSaved;} //accessor/mutator methods for getting and setting the file that was last saved

    /**
     * Sets what file has last been saved to from this canvas
     * @param file File
     */
    public void setLastSaved(File file) {lastSaved = file;}

    /**
     * Returns whether this canvas is "dirty" (has been modified/touched)
     * @return boolean
     */
    public boolean getDirty() {return dirty;}

    /**
     * Accessor for the undo stack
     * @return The undo stack
     */
    public Stack<Image> getUndoStack(){
        return undoStack;
    }

    /**
     * Accessor for the redo stack
     * @return The redo stack
     */
    public Stack<Image> getRedoStack(){
        return redoStack;
    }

    /**
     * If user has made an edit, store that information and update tab name with a * appended to reflect this.
     * @param isDirty Has the canvas been modified?
     */
    public void setDirty(boolean isDirty) {
        dirty = isDirty;
        try{
            PaintApplication.updateTab((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()); //updates tab with * or not depending on dirty status
        }
        catch(Exception e){}
    }

    /**
     * Sets canvas back to white.
     */
    public void clearCanvas(){
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.WHITE);                                        //fills in canvas with white (slightly distinct from gray background)
        gc.fillRect(0, 0, getWidth(), getHeight());               //sets gc to canvas size
        updateUndoStack();       //updates undo stack to retain initial copy of image
        LogHandler.getLogHandler().writeToLog(true, "Canvas cleared.");
    }

    /**
     * Gets a snapshot of the image, then pushes it onto the canvas's associated undo stack.
     */
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

        undoStack.push(image);      //push to undo stack

    }
}
