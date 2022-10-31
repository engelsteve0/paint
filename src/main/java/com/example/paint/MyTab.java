//Steven Engel
//MyTab.java
//The MyTab class is a tab which stores extra information, such as the canvas assigned to it, as well as overriding the exit method to warn user to save their work
package com.example.paint;


import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Steven Engel
 * @MyTab.java: The MyTab class is a tab which stores extra information, such as the canvas assigned to it, as well as overriding the exit method to warn user to save their work
 */
public class MyTab extends Tab{
    private MyCanvas currentCanvas;         //stores reference to canvas associated with this tab
    private Canvas layer;                   //stores preview/selection information related to this tab
    private StackPane root;                 //stackpane for preview/selection layer overlay
    private ImageSelection imageSelection;
    private Timer autoSaveTimer;
    private TimerTask autoSaveTask;
    private boolean runAutoSaveTimer;       //determines whether autosave timer should be running at this point or not for this tab specifically
    private int timeLeft;                   //stores time left on autosave timer


    /**
     * Calls default tab constructor, then sets up this tab and its associated autosave timer.
     * @param canvas the canvas associated with this tab.
     */
    public MyTab(MyCanvas canvas){
        super();
        setCurrentCanvas(canvas);           //associates canvas given in constructor with this tab
        this.layer = new Canvas((int) PaintApplication.getCanvas().getWidth(), (int) PaintApplication.getCanvas().getHeight());
        this.root = new StackPane(); //is eventually used as an overlay for previewing changes
        this.imageSelection = new ImageSelection(); //stores data related to the current selection
        this.setSelection(0);
        root.getChildren().addAll(canvas, layer);                   //adds canvas and overlay to a temporary stackpane to display both
        root.setAlignment(Pos.TOP_LEFT);
        PaintApplication.getScrollPane().setContent(root);
        setOnCloseRequest(e->{
            if (currentCanvas.getDirty()){      //only calls smart save if canvas has changes since last save
                e.consume();                    //sets up smart/aware save
                savePrompt(true);
            }
        });
        runAutoSaveTimer = true;
        this.autoSaveTimer = new Timer();              //handles autosave timer
        this.timeLeft = PaintApplication.getAutoSaveDuration();                             //10 seconds for test, 300 (5 min) for actual autosave
        autoSaveTask = new TimerTask() {                //Special type of thread for autosave timer
            String time = "";
            public void run(){
                if (timeLeft > 0 && runAutoSaveTimer && PaintApplication.getEnableAutoSave()) { //if this is the selected tab, count down
                    timeLeft--;
                    time = String.format("%02d:%02d:%02d", timeLeft/3600, (timeLeft%3600) / 60, timeLeft % 60);
                    Platform.runLater(()->{
                        if(PaintApplication.getDisplayAutoSaveTimer())      //only display if user has opted in to display
                            PaintApplication.getAutoSaveTimer().setText(" Autosave in: " + time);    //let user know how much time is left
                    });
                }
                else if(timeLeft<=0 && PaintApplication.getEnableAutoSave()){
                    Platform.runLater(()-> {
                        timeLeft = PaintApplication.getAutoSaveDuration();
                        runAutoSaveTimer=false;
                        PaintApplication.save(currentCanvas.getLastSaved());        //autosaves and resets timer
                        LogHandler.getLogHandler().writeToLog(true, "Autosaved to " + currentCanvas.getLastSaved().getName());
                    });
                }
                else if(PaintApplication.getEnableAutoSave()==false&&PaintApplication.getAutoSaveTimer()!=null){
                    Platform.runLater(()->{
                        if(PaintApplication.getDisplayAutoSaveTimer())      //only display if user has opted in to display
                            PaintApplication.getAutoSaveTimer().setText(" Autosave disabled");                       //if autosave is disabled, let user know
                    });
                    }
            }
        };
        autoSaveTimer.scheduleAtFixedRate(autoSaveTask, 0, 1000);   //run timer at 1s/s
    }

    /**
     * Sets the current canvas of this tab to be the parameter
     * @param canvas the MyCanvas to assign to this tab
     */
    public void setCurrentCanvas(MyCanvas canvas){  //setter/getter methods for canvas associated with this tab
        this.currentCanvas = canvas;
    }

    /**
     * Accessor for the current tab's canvas
      * @return MyCanvas
     */
    public MyCanvas getCurrentCanvas(){
        return currentCanvas;
    }

    /**
     * Accessor for the current overlay layer
     * @return Canvas
     */
    public Canvas getCurrentLayer(){
        return layer;
    }

    /**
     * Accessor for the current tab's "root" (a stackpane containing the canvas and overlay layer)
     * @return StackPane
     */
    public StackPane getCurrentRoot(){
        return root;
    }

    /**
     * Sets selection state as well as selection image stored
     * @param selection int (selection state: 0, 1, 2)
     * @param selectionImage Image (the image stored)
     */
    public void setSelection(int selection, Image selectionImage) {imageSelection.setState(selection); imageSelection.setSelectionImage(selectionImage);}

    /**
     * Sets current state of image selection
     * @param selection int: 0, 1, or 2
     */
    public void setSelection(int selection){imageSelection.setState(selection);}
    public Image getSelectionImage(){return imageSelection.getSelectionImage();}

    /**
     * Accessor method for current state of image selection
     * @return int
     */
    public int getSelection() {return imageSelection.getState();}
    public ImageSelection getImageSelection(){return imageSelection;}

    /**
     * Sets the time left on the autosave timer
     * @param s time left in seconds
     */
    public void setTimeLeft(int s){timeLeft = s;}

    /**
     * Returns the name of this tab (based on the filename without the path).
     * @return String (the name of this tab)
     */
    public String getTabName(){                     //returns name of this tab (based on file without path)
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

    /**
     * Creates a new window prompting user if they want to save the canvas associated with this tab to a file or not.
     * @param individualClose Is this being closed from a tabpane itself (true) or application exit (false)?
     */
    public void savePrompt(boolean individualClose){              //boolean checks if this is being closed from tabpane or from application exit
        Stage dialog = new Stage();                               //creates a new window
        dialog.initStyle(StageStyle.UNDECORATED);                 //Looks ugly, but prevents user from messing up tab counts with x button for aware save
        dialog.setTitle("Unsaved Work");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(PaintApplication.getStage());
        dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
        VBox dialogVbox = new VBox(20);
        Font CS = new Font("Times New Roman", 12);
        Text t =  new Text(this.getTabName() + " has not been saved. Would you like to save your work?");
        t.setFont(CS);
        Button saveAllButton = new Button("Save"); //Gives user options for saving, not saving, or cancelling the operation
        saveAllButton.setOnAction(e->{
            PaintApplication.getTabPane().getSelectionModel().select(this);           //selects each as it goes along in order
            PaintApplication.save(this.getCurrentCanvas().getLastSaved());
            if(individualClose)                       //if this is being closed individually, close the tab. Otherwise just let the program close
                PaintApplication.getTabPane().getTabs().remove(this);
            else{
                PaintApplication.incrementTabsClosed();
            }
            dialog.close();

        });
        Button closeButton = new Button("Don't Save");  //close tab without saving
        closeButton.setOnAction(e->{
            if(individualClose)
                PaintApplication.getTabPane().getTabs().remove(this);
            else{
                PaintApplication.incrementTabsClosed();    //keeps track of tabs closed so window knows when to close
            }
            dialog.close();
        });
        Button cancelButton = new Button("Cancel");     //just get rid of the window
        cancelButton.setOnAction(e->{
            dialog.close();
        });
        HBox options = new HBox();
        if(individualClose)     //only add cancelButton if this is an individual dialog
            options.getChildren().addAll(saveAllButton, closeButton, cancelButton);
        else
            options.getChildren().addAll(saveAllButton, closeButton);
        dialogVbox.getChildren().addAll(t, options);                //actually adds text, button to window
        Scene dialogScene = new Scene(dialogVbox, 450, 60);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
        dialog.setResizable(false);                     //don't let user resize; this is just an alert window
    }

    /**
     * A method for resetting the timer to 5m. Could be used to reset on manual save.
     */
    public void resetAutoSaveTimer(){                   //a method for resetting the timer to 5m. Could be used to reset on manual save
        timeLeft = PaintApplication.getAutoSaveDuration();
    }

    /**
     *
     * @param set Should this autosave timer run (true) or not (false)?
     */
    public void setAutoSaveTimer(boolean set){          //determines whether this autosave timer should run or not
        runAutoSaveTimer = set;
    }

    /**
     * Stops all autosave timer threads. Useful for closing the program and stopping threads from PaintApplication.java.
     */
    public void stopAutoSaveTimer() {try{autoSaveTimer.cancel();} catch(Exception e){}};
}
