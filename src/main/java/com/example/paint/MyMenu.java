//Steven Engel
//MyMenu.java
//This file houses much of the tedious GUI code needed to make the menubar across the top, as well as allowing for dialog popups for some of these options to be created
package com.example.paint;

import javafx.embed.swing.SwingFXUtils;
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

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

public class MyMenu extends MenuBar{ //hierarchy: this is a MenuBar, which contains Menus, which dropdown and contain MenuItems
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
        SeparatorMenuItem s2 = new SeparatorMenuItem();
        MenuItem resizeDD = new MenuItem("Resize Canvas");    //opens window letting user resize canvas
        resizeDD.setOnAction(e -> {
            createResizePopup();
        });
        SeparatorMenuItem s3 = new SeparatorMenuItem();
        MenuItem exitDD = new MenuItem("Exit");             //exits the application
        exitDD.setOnAction(e -> {
            Window window = PaintApplication.getStage().getScene().getWindow(); //calls special close request to ensure event in main program fires
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        fileMenu.getItems().addAll(newDD, openDD, s1, saveDD, saveAsDD, saveAllDD, s2, resizeDD, s3, exitDD);    //adds controls to fileMenu

        Menu viewMenu = new Menu("View");
        MenuItem fullScreenDD = new MenuItem("Toggle Full Screen (F11)");        //saves to a user-specified location
        fullScreenDD.setOnAction(e -> PaintApplication.getStage().setFullScreen(!PaintApplication.getStage().isFullScreen()));
        viewMenu.getItems().addAll(fullScreenDD);

        Menu helpMenu = new Menu("Help");
        MenuItem helpDD = new MenuItem("Help");                     //saves to a user-specified location
        helpDD.setOnAction(e -> createPopup("Help", "Hello and welcome to Paint!\nTo begin, open an existing image, or create a new 128x128 pixel canvas through the file menu.\nThen, select a tool in the toolbar and click on the canvas area to draw.\nBe sure to save your work frequently in the file menu!"));
        MenuItem aboutDD = new MenuItem("About");                     //saves to a user-specified location
        aboutDD.setOnAction(e -> createPopup("About", "Paint 0.0.2 is an all-purpose art program for professional (pixel) artists, as well as complete amateurs.\nIt was written by Steven Engel, who is a Computer Engineer and Comic Sans enthusiast\n(but thankfully for the user, this is in Times New Roman)."));
        helpMenu.getItems().addAll(helpDD, aboutDD);

        this.getMenus().addAll(fileMenu, viewMenu, helpMenu); //adds all menus to menubar

    }

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
    public void createResizePopup() {        //creates a 600x200 popup window specifically with button functions for saving changes, cancelling
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
            MyCanvas currentCanvas = PaintApplication.getCanvas();
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
}
