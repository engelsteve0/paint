//Steven Engel
//MyMenu.java
//This file houses much of the tedious GUI code needed to make the menubar across the top, as well as allowing for dialog popups for some of these options to be created
package com.example.paint;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Steven Engel
 * @MyMenu.java: This file houses much of the tedious GUI code needed to make the menubar across the top, as well as allowing for dialog popups for some of these options to be created.
 */
public class MyMenu extends MenuBar{ //hierarchy: this is a MenuBar, which contains Menus, which dropdown and contain MenuItems
    /**
     * Sets up the GUI for the menu bar, after calling the MenuBar constructor.
     */
    public MyMenu(){
        super();
        Menu fileMenu = new Menu("File"); //creates the menu bar across the top. Menus are subject to change.
        MenuItem newDD = new MenuItem("New (Ctrl + N)");    //Creates new, blank image. Keyboard shortcuts are implemented in PaintApplication.java
        newDD.setOnAction(e -> PaintApplication.newImage());
        MenuItem openDD = new MenuItem("Open (Ctrl + O)");    //opens existing image from filesystem
        openDD.setOnAction(e -> PaintApplication.chooseFile("Open Image file", false));
        SeparatorMenuItem s1 = new SeparatorMenuItem();
        MenuItem saveDD = new MenuItem("Save (Ctrl + S)");  //saves/overwrites to old location
        saveDD.setOnAction(e -> PaintApplication.save(PaintApplication.getCanvas().getLastSaved()));
        MenuItem saveAsDD = new MenuItem("Save As");        //saves to a user-specified location
        saveAsDD.setOnAction(e -> PaintApplication.saveAs());
        MenuItem saveAllDD = new MenuItem("Save All");        //asks user about saving all files
        saveAllDD.setOnAction(e -> PaintApplication.saveAll());
        MenuItem autoSaveDD = new MenuItem("Autosave Settings"); //allows user to toggle autosave settings
        autoSaveDD.setOnAction(e -> {
            createAutoSavePopup();
        });
        SeparatorMenuItem s2 = new SeparatorMenuItem();
        MenuItem exitDD = new MenuItem("Exit");             //exits the application
        exitDD.setOnAction(e -> {
            Window window = PaintApplication.getStage().getScene().getWindow(); //calls special close request to ensure event in main program fires
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        fileMenu.getItems().addAll(newDD, openDD, s1, saveDD, saveAsDD, saveAllDD, autoSaveDD, s2, exitDD);    //adds controls to fileMenu

        Menu viewMenu = new Menu("View");
        MenuItem fullScreenDD = new MenuItem("Toggle Full Screen (F11)");        //toggle fullscreen
        fullScreenDD.setOnAction(e -> PaintApplication.getStage().setFullScreen(!PaintApplication.getStage().isFullScreen()));
        MenuItem nightModeDD = new MenuItem("Toggle Night Mode");                //toggles night mode
        nightModeDD.setOnAction(e-> {
            if(PaintApplication.getNightMode()){
                PaintApplication.getScene().getRoot().setStyle("");                 //normal mode
                PaintApplication.setNightMode(false);
            }
            else{
                PaintApplication.getScene().getRoot().setStyle("-fx-accent: #1e74c6;" +
                        "    -fx-focus-color: -fx-accent;" +
                        "    -fx-base: #373e43;" +
                        "    -fx-control-inner-background: derive(-fx-base, 35%);" +
                        "    -fx-control-inner-background-alt: -fx-control-inner-background;"); //dark mode
                PaintApplication.setNightMode(true);
            }
        });
        viewMenu.getItems().addAll(fullScreenDD, nightModeDD);

        Menu editMenu = new Menu("Edit");
        MenuItem undoDD = new MenuItem("Undo (Ctrl + Z)");    //undoes latest canvas action
        undoDD.setOnAction(e -> {
            PaintApplication.undo();
        });
        MenuItem redoDD = new MenuItem("Redo (Ctrl + Y)");    //redoes latest canvas action
        redoDD.setOnAction(e -> {
            PaintApplication.redo();
        });
        SeparatorMenuItem s3 = new SeparatorMenuItem();
        MenuItem cutDD = new MenuItem("Cut (Ctrl + X)");     //cuts an image if selected
        cutDD.setOnAction(e -> {
            if(((MyTab)(PaintApplication.getTabPane().getSelectionModel().getSelectedItem())).getSelection()>=1) //if an area is currently selected
                PaintApplication.getToolbar().cutImage();
        });
        MenuItem copyDD = new MenuItem("Copy (Ctrl + C)");    //copies an image if selected
        copyDD.setOnAction(e -> {
            if(((MyTab)(PaintApplication.getTabPane().getSelectionModel().getSelectedItem())).getSelection()>=1) //if an area is currently selected
                PaintApplication.getToolbar().copyImage();
        });
        MenuItem pasteDD = new MenuItem("Paste (Ctrl + V)");    //pastes image into tab if possible
        pasteDD.setOnAction(e -> {
            try{
                PaintApplication.getToolbar().pasteImage();
            }
            catch(Exception f){}
        });
        SeparatorMenuItem s4 = new SeparatorMenuItem();
        MenuItem resizeDD = new MenuItem("Resize Canvas");    //opens window letting user resize canvas
        resizeDD.setOnAction(e -> {
            createResizePopup();
        });

        MenuItem clearCanvasDD = new MenuItem("Clear Canvas");    //opens window letting user clear
        clearCanvasDD.setOnAction(e -> {
            createClearCanvasPopup();
        });
        SeparatorMenuItem s5 = new SeparatorMenuItem();
        MenuItem rotateCanvasDD = new MenuItem("Rotate Canvas/Selection");    //opens window letting user clear
        rotateCanvasDD.setOnAction(e -> {
            createRotateCanvasPopup();
        });
        MenuItem flipCanvasDD = new MenuItem("Flip/Mirror Canvas");    //opens window letting user clear
        flipCanvasDD.setOnAction(e -> {
            createRotateCanvasPopup();
        });
        editMenu.getItems().addAll(undoDD, redoDD, s3, cutDD, copyDD, pasteDD, s4, resizeDD, clearCanvasDD, s5, rotateCanvasDD, flipCanvasDD);
        Menu helpMenu = new Menu("Help");
        MenuItem helpDD = new MenuItem("Help");                     //saves to a user-specified location
        helpDD.setOnAction(e -> createPopup("Help", "Hello and welcome to Paint!\nTo begin, open an existing image, or create a new 128x128 pixel canvas through the file menu.\nThen, select a tool in the toolbar and click on the canvas area to draw.\nBe sure to save your work frequently in the file menu!"));
        MenuItem aboutDD = new MenuItem("About");                     //saves to a user-specified location
        aboutDD.setOnAction(e -> createPopup("About", "Paint 0.0.8 is an all-purpose art program for professional (pixel) artists, as well as complete amateurs.\nIt was written by Steven Engel, who is a Computer Engineer and Comic Sans enthusiast\n(but thankfully for the user, this is in Times New Roman)."));
        helpMenu.getItems().addAll(helpDD, aboutDD);

        this.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu); //adds all menus to menubar

    }

    /**
     * Creates an informational popup, with just a title, text, and a close button.
     * @param titleText The text to be displayed at the top of the window.
     * @param bodyText The text to be displayed in the body of the window.
     */
    public void createPopup(String titleText, String bodyText) {        //creates a 600x200 popup window
        final Stage dialog = new Stage();                               //creates a new window
        dialog.setTitle(titleText);
        dialog.initModality(Modality.APPLICATION_MODAL);                //only allows user to open one of these, pushes to front
        dialog.initOwner(PaintApplication.getStage());
        dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
        VBox dialogVbox = new VBox(20);
        Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
        Text t =  new Text(bodyText);
        t.setFont(CS);
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e->dialog.close());
        dialogVbox.getChildren().addAll(t, closeButton);                //actually adds text to window
        Scene dialogScene = new Scene(dialogVbox, 600, 200);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
    }

    /**
     * Creates a 600x200 popup window specifically for resizing the canvas, with button functions for saving changes, cancelling.
     */
    public void createResizePopup() {        //creates a 600x200 popup window specifically for resizing with button functions for saving changes, cancelling
        final Stage dialog = new Stage();                               //creates a new window
        dialog.setTitle("Resize Canvas");
        dialog.initModality(Modality.APPLICATION_MODAL);                //only allows user to open one of these, pushes to front
        dialog.initOwner(PaintApplication.getStage());
        dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
        VBox dialogVbox = new VBox(20);
        Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
        Text t =  new Text("Specify the new size of the canvas in x and y pixels: ");
        t.setFont(CS);
        double oldX = PaintApplication.getCanvas().getWidth();
        double oldY = PaintApplication.getCanvas().getHeight();
        AtomicInteger xValue = new AtomicInteger((int) PaintApplication.getCanvas().getWidth());
        AtomicInteger yValue = new AtomicInteger((int) PaintApplication.getCanvas().getHeight());
        Label xLabel = new Label("x: ");
        Label yLabel = new Label("y: ");
        TextField xInput = new TextField(String.valueOf(xValue.get()));
        TextField yInput = new TextField(String.valueOf(yValue.get()));
        HBox xHBox = new HBox(xLabel, xInput);
        HBox yHBox = new HBox(yLabel, yInput);
        xInput.textProperty().addListener((ov, old_val, new_val) -> {
            try{                                        //if user tries to input a non-number, don't let them and instead set it to 1
                xValue.set(Integer.parseInt(new_val));
            }
            catch(Exception e){
                xValue.set(1);
            }
            if(xValue.get()<=0){                           //Limit size to >0 range
                xValue.set(1);
            }
                                                        //updates text input with new value
            xInput.setText(String.valueOf(xValue.get()));
        });
        yInput.textProperty().addListener((ov, old_val, new_val) -> {
            try{                                        //if user tries to input a non-number, don't let them and instead set it to 1
                yValue.set(Integer.parseInt(new_val));
            }
            catch(Exception e){
                yValue.set(1);
            }
            if(yValue.get()<=0){                           //Limit size to >0 range
                yValue.set(1);
            }
            //updates text input with new value
            yInput.setText(String.valueOf(yValue.get()));
        });
        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e->{
            MyCanvas currentCanvas = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas();
            double ogx = currentCanvas.getScaleX();//stores original x and y scales to reset after save
            double ogy = currentCanvas.getScaleY();
            currentCanvas.setScaleX(1);            //briefly sets canvas scale to default to avoid errors
            currentCanvas.setScaleY(1);
            WritableImage writableImage = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
            currentCanvas.snapshot(null, writableImage);
            currentCanvas.setHeight(yValue.doubleValue());   //resizes canvas, sets dirty status
            currentCanvas.setWidth(xValue.doubleValue());
            currentCanvas.setDirty(true);
            currentCanvas.getGraphicsContext2D().drawImage(writableImage, 0, 0, xValue.doubleValue(), yValue.doubleValue());
            currentCanvas.setScaleX(ogx);            //resets canvas scale
            currentCanvas.setScaleY(ogy);
            currentCanvas.updateUndoStack();       //updates undo stack to retain initial copy of image
            LogHandler.getLogHandler().writeToLog(true, "Canvas resized to " + xValue + "x" + yValue);
            dialog.close();
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e->dialog.close());
        HBox buttonsBox = new HBox(applyButton, cancelButton);
        dialogVbox.getChildren().addAll(t, xHBox, yHBox, buttonsBox);                //actually adds text to window
        Scene dialogScene = new Scene(dialogVbox, 600, 200);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
    }

    /**
     * Creates a 600x70 popup window specifically built for clearing canvas confirmation.
     */
    public void createClearCanvasPopup() {        //creates a 600x70 popup window specifically built for clearing canvas confirmation
        final Stage dialog = new Stage();                               //creates a new window
        dialog.setTitle("Clear canvas: are you sure?");
        dialog.initModality(Modality.APPLICATION_MODAL);                //only allows user to open one of these, pushes to front
        dialog.initOwner(PaintApplication.getStage());
        dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
        VBox dialogVbox = new VBox(20);
        Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
        Text t =  new Text("This will cause the current canvas to be cleared and you will lose anything on said canvas. Are you sure you want to do this?");
        t.setFont(CS);
        Button clearButton = new Button("Clear Canvas");
        clearButton.setOnAction(e->{                                    //closes this dialog and clear canvas
            dialog.close();
            ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas().clearCanvas();
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e->{                                   //closes this dialog
            dialog.close();
        });
        HBox buttonsBox = new HBox(clearButton, cancelButton);
        dialogVbox.getChildren().addAll(t, buttonsBox);                //actually adds text to window
        Scene dialogScene = new Scene(dialogVbox, 600, 70);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
    }
    /**
     * Creates a 600x70 popup window specifically built for autosave settings.
     */
    public void createAutoSavePopup() {        //creates a 600x70 popup window specifically built for autosave settings
        final Stage dialog = new Stage();                               //creates a new window
        dialog.setTitle("Autosave Settings");
        dialog.initModality(Modality.APPLICATION_MODAL);                //only allows user to open one of these, pushes to front
        dialog.initOwner(PaintApplication.getStage());
        dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
        Button toggleButton = new Button();
        Label autoSaveDesc = new Label("The autosave feature automatically saves each canvas after the time shown.");   //informs user about autosave
        if(PaintApplication.getEnableAutoSave()){
            toggleButton.setText("Toggle Autosave (currently enabled)");
        }
        else
            toggleButton.setText("Toggle Autosave (currently disabled)");
        toggleButton.setOnAction(e->{                                    //master enable/disable for autosave
            if(PaintApplication.getEnableAutoSave()){
                PaintApplication.setEnableAutoSave(false);
                toggleButton.setText("Toggle Autosave (currently disabled)");            //changes text to let user know whether it is currently enabled or disabled
            }

            else{
                PaintApplication.setEnableAutoSave(true);
                toggleButton.setText("Toggle Autosave (currently enabled)");
            }

        });
        Button displayButton = new Button();
        if(PaintApplication.getDisplayAutoSaveTimer()){
            displayButton.setText("Toggle Autosave Display (currently displayed)");
        }
        else
            displayButton.setText("Toggle Autosave Display (currently not displayed)"); //toggles whether autosave timer is displayed or not
        displayButton.setOnAction(e->{
            if(PaintApplication.getDisplayAutoSaveTimer()==false){
                PaintApplication.setDisplayAutoSaveTimer(true);
                displayButton.setText("Toggle Autosave Display (currently displayed)"); //changes text to let user know whether it is currently displayed or not
            }
            else{
                PaintApplication.setDisplayAutoSaveTimer(false);
                PaintApplication.getAutoSaveTimer().setText("");        //clears text label
                displayButton.setText("Toggle Autosave Display (currently not displayed)");
            }
        });
        AtomicInteger timeValue = new AtomicInteger(PaintApplication.getAutoSaveDuration());
        TextField timeInput = new TextField(String.valueOf(timeValue.get()));
        Label timeLabel = new Label("Enter time between autosaves (seconds): ");
        HBox timeHBox = new HBox(timeLabel, timeInput);
        timeInput.textProperty().addListener((ov, old_val, new_val) -> {
            try{                                        //if user tries to input a non-number, don't let them and instead set it to 1
                timeValue.set(Integer.parseInt(new_val));
            }
            catch(Exception e){
                timeValue.set(300);
            }
            if(timeValue.get()<1||timeValue.get()>36000){                           //Limit size to >0, <6 hours range
                timeValue.set(1);
            }
            //updates text input with new value
            timeInput.setText(String.valueOf(timeValue.get()));
        });
        Button saveButton = new Button("Apply");
        saveButton.setOnAction(e->{
            PaintApplication.setAutoSaveDuration(timeValue.get());  //changes autosave duration to typed value
            for (Tab tabs:                  //iterates through all tabs in the tabpane, changing time left on them
                    PaintApplication.getTabPane().getTabs()) {
                ((MyTab)tabs).setTimeLeft(timeValue.get());
            }
        });
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e->{                                   //closes this dialog
            dialog.close();
        });
        HBox buttonsBox = new HBox(saveButton, closeButton);
        VBox bigBox = new VBox(autoSaveDesc, toggleButton, displayButton, timeHBox, buttonsBox);
        bigBox.setSpacing(10);      //ensures buttons aren't right on top of each other
        Scene dialogScene = new Scene(bigBox, 600, 200);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
    }
    /**
     * Creates a 600x70 popup window specifically built for rotation settings.
     */
    public void createRotateCanvasPopup() {        //creates a 600x70 popup window specifically built for rotation settings
        final Stage dialog = new Stage();                               //creates a new window
        dialog.setTitle("Rotation Settings");
        dialog.initModality(Modality.APPLICATION_MODAL);                //only allows user to open one of these, pushes to front
        dialog.initOwner(PaintApplication.getStage());
        dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
        Button toggleButton = new Button();
        Label autoSaveDesc = new Label("Enter the number of degrees to rotate by and choose whether this should apply to the whole canvas or selection:");   //informs user about autosave
        AtomicBoolean wholeCanvas = new AtomicBoolean(true);
        if(wholeCanvas.get()){
            toggleButton.setText("Whole Canvas");
        }
        else
            toggleButton.setText("Selection");
        toggleButton.setOnAction(e->{                                    //master enable/disable for autosave
            if(wholeCanvas.get()){
                wholeCanvas.set(false);
                toggleButton.setText("Selection");            //changes text to let user know whether it is currently enabled or disabled
            }

            else{
                wholeCanvas.set(true);
                toggleButton.setText("Whole Canvas");            //changes text to let user know whether it is currently enabled or disabled
            }

        });

        AtomicInteger rotationValue = new AtomicInteger(90);
        TextField rotationInput = new TextField(String.valueOf(rotationValue.get()));
        Label rotationLabel = new Label("Enter degrees to rotate by (CW): ");
        HBox rotationHBox = new HBox(rotationLabel, rotationInput);
        rotationInput.textProperty().addListener((ov, old_val, new_val) -> {
            try{                                        //if user tries to input a non-number, don't let them and instead set it to 1
                rotationValue.set(Integer.parseInt(new_val));
            }
            catch(Exception e){
                rotationValue.set(90);
            }
            if(rotationValue.get()<1||rotationValue.get()>359){                           //Limit size to >0, <6 hours range
                rotationValue.set(90);
            }
            //updates text input with new value
            rotationInput.setText(String.valueOf(rotationValue.get()));
        });
        Button saveButton = new Button("Apply");
        saveButton.setOnAction(e->{
            PaintApplication.getToolbar().rotateImage(rotationValue.get(), wholeCanvas.get());  //sets rotation value in degrees, whether to rotate whole canvas or not
            dialog.close();
        });
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e->{                                   //closes this dialog
            dialog.close();
        });
        HBox buttonsBox = new HBox(saveButton, closeButton);
        VBox bigBox = new VBox(autoSaveDesc, toggleButton, rotationHBox, buttonsBox);
        bigBox.setSpacing(10);      //ensures buttons aren't right on top of each other
        Scene dialogScene = new Scene(bigBox, 600, 200);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
    }
}
