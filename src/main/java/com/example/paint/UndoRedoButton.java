//Steven Engel
//UndoRedoButton.java
//A special class for the undo and redo buttons and their associated behavior. Extends a normal button
package com.example.paint;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Stack;

public class UndoRedoButton extends Button {
    private boolean isUndo; //if true, this is an undo button; if false, a redo
    public UndoRedoButton(boolean isUndo, Image originalImage, int h, int w){
        super();
        this.isUndo = isUndo;
        ImageView image = new ImageView(originalImage);         //set the given image to be this button's icon
        image.setFitHeight(h);
        image.setFitHeight(w);
        image.setPreserveRatio(true);                           //gets buttons to display properly
        setGraphic(image);

        if(isUndo){ //undo button
            setOnAction(e->{        //behavior for when this is pressed
               undo();
            });
        }
        else{ //redo button
            setOnAction(e->{        //behavior for when this is pressed
                redo();
            });
        }
    }
    public void undo(){     //source function for what happens when user undoes
        Stack<Image> uS = ((MyTab)PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas().getUndoStack();    //gets undo and redo stacks for this tab
        Stack<Image> rS = ((MyTab)PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas().getRedoStack();
        try{
            if(!uS.isEmpty()) {
                Image image = uS.pop();           //pops top item from undo stack, places in redo stack
                rS.push(image);
                image = uS.peek();
                MyCanvas currentCanvas = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas();
                currentCanvas.setHeight(image.getHeight());        //adjust canvas according to image size
                currentCanvas.setWidth(image.getWidth());
                currentCanvas.getGraphicsContext2D().clearRect(0, 0, image.getWidth(), image.getHeight());
                currentCanvas.getGraphicsContext2D().drawImage(image, 0, 0, image.getWidth(), image.getHeight()); //actually draws image
                currentCanvas.setDirty(true);
            }
        }
        catch(Exception e){}

    }
    public void redo(){     //source function for what happens when user redoes
        Stack<Image> uS = ((MyTab)PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas().getUndoStack();
        Stack<Image> rS = ((MyTab)PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas().getRedoStack();
        try{
            if(!rS.isEmpty()){
                Image image = rS.pop();           //pops top item from redo stack, places in undo stack
                uS.push(image);
                //image = rS.peek();
                MyCanvas currentCanvas = ((MyTab)PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas();
                currentCanvas.setHeight(image.getHeight());        //adjust canvas according to image size
                currentCanvas.setWidth(image.getWidth());
                currentCanvas.getGraphicsContext2D().clearRect(0, 0, image.getWidth(), image.getHeight());
                currentCanvas.getGraphicsContext2D().drawImage(image, 0, 0, image.getWidth(), image.getHeight()); //actually draws image
                currentCanvas.setDirty(true);
            }
        }
        catch(Exception e){}
    }
}
