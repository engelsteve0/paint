//Steven Engel
//MyMenu.java
//This file houses much of the tedious GUI code needed to make the menubar across the top, as well as allowing for dialog popups for some of these options to be created
package com.example.paint;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

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
        MenuItem exitDD = new MenuItem("Exit");             //exits the application
        exitDD.setOnAction(e -> {
            Window window = PaintApplication.getStage().getScene().getWindow(); //calls special close request to ensure event in main program fires
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        fileMenu.getItems().addAll(newDD, openDD, s1, saveDD, saveAsDD, saveAllDD, s2, exitDD);    //adds controls to fileMenu

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
}
