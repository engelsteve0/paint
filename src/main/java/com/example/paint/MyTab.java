//Steven Engel
//MyTab.java
//The MyTab class is a tab which stores extra information, such as the canvas assigned to it, as well as overriding the exit method to warn user to save their work
package com.example.paint;


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



public class MyTab extends Tab{
    private MyCanvas currentCanvas;         //stores reference to canvas associated with this tab
    private Canvas layer;                   //stores preview/selection information related to this tab
    private StackPane root;                 //stackpane for preview/selection layer overlay
    boolean hasSelection;                   //used to keep track of if this tab's canvas/layer has a selection
    Image selectionImage;                   //stores snapshot of what was selected
    public MyTab(MyCanvas canvas){
        super();
        setCurrentCanvas(canvas);           //associates canvas given in constructor with this tab
        this.layer = new Canvas((int) PaintApplication.getCanvas().getWidth(), (int) PaintApplication.getCanvas().getHeight());
        this.root = new StackPane(); //is eventually used as an overlay for previewing changes
        this.hasSelection = false;
        setOnCloseRequest(e->{
            if (currentCanvas.getDirty()){      //only calls smart save if canvas has changes since last save
                e.consume();                    //sets up smart/aware save
                savePrompt(true);
            }
        });
    }
    public void setCurrentCanvas(MyCanvas canvas){  //setter/getter methods for canvas associated with this tab
        this.currentCanvas = canvas;
    }

    public MyCanvas getCurrentCanvas(){
        return currentCanvas;
    }
    public Canvas getCurrentLayer(){
        return layer;
    }
    public StackPane getCurrentRoot(){
        return root;
    }
    public void setSelection(boolean selection, Image selectionImage) {hasSelection = selection; this.selectionImage = selectionImage;}
    public void setSelection(boolean selection){hasSelection = selection;}
    public Image getSelectionImage(){return selectionImage;}
    public boolean getSelection() {return hasSelection;}
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
    public void savePrompt(boolean individualClose){              //boolean checks if this is being closed from tabpane or from application exit
        Stage dialog = new Stage();                               //creates a new window
        dialog.initStyle(StageStyle.UNDECORATED);                 //Looks ugly, but prevents user from messing up tab counts with x button for aware save
        dialog.setTitle("Unsaved Work");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(PaintApplication.getStage());
        dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
        VBox dialogVbox = new VBox(20);
        Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
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
}
