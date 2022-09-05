package com.example.paint;

import java.awt.image.RenderedImage;
import java.io.File;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;

public class PaintApplication extends Application {
    private static Stage stage; //ensures that the stage can be referenced in functions
    //public static boolean hasSaved = false; //checks if a file has been saved previously
    private static File lastSaved = null;  //stores file saved previously
    private static Canvas canvas;
    private static DoubleProperty myScale = new SimpleDoubleProperty(1.0);
    @Override
    public void start(Stage stage) throws IOException {
        //This section sets up the GUI and menu.
        this.stage = stage;
        BorderPane layout = new BorderPane(); //uses a grid to align gui elements neatly- considering multiple grids for different parts of gui

        canvas = new Canvas(128, 128);    //creates a canvas for loading images, drawing on
        canvas.scaleXProperty().bind(myScale);
        canvas.scaleYProperty().bind(myScale);
        newImage();                             //starts with a blank slate
        //VBox canvasBox = new VBox();
        //canvasBox.getChildren().add(canvas);    //Not really used now but may be used to contain other tools in the future
        layout.setCenter(canvas);
        Slider slider = new Slider(0.1,4,1);
        canvas.scaleXProperty().bind(slider.valueProperty());
        canvas.scaleYProperty().bind(slider.valueProperty());
        layout.setBottom(slider);
        layout.setAlignment(slider, Pos.BOTTOM_CENTER);

        theMenu menu = new theMenu(); //see theMenu.java (extends the MenuBar class)
        //grid.add(menu, 0, 0); //adds to top left of window
        layout.setTop(menu);
        layout.setAlignment(menu, Pos.TOP_LEFT);

        Scene scene = new Scene(layout, 600, 480); //makes a scene (pun intended)
        stage.setScene(scene);
        stage.setTitle("Paint");

        stage.setResizable(true);
        stage.show(); //Displays stage which hosts the scene which hosts the grid which hosts the canvas which hosts the gc

        //This section houses the keyboard shortcuts.
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {       //implements save with ctrl + s
            @Override
            public void handle(KeyEvent event) {
                if (event.isControlDown() && (event.getCode() == KeyCode.S)) {
                    save(lastSaved);
                }
                else if (event.isControlDown() && (event.getCode() == KeyCode.O)) { //implements open with control o
                    chooseFile("Open Image file", false);
                }
                else if (event.isControlDown() && (event.getCode() == KeyCode.N)) { //implements new file with control n
                    newImage();
                }
                else if ((event.getCode() == KeyCode.F11)) { //implements fullscreen toggle
                    getStage().setFullScreen(!PaintApplication.getStage().isFullScreen());
                }
            }});


    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getStage() { //accessor method for stage in case anything external needs it
        return stage;
    }
    public static File getLastSaved() { //accessor method returning file last saved
        return lastSaved;
    }

    public static File chooseFile(String title, Boolean saving) {    //opens a file chooser dialogue in stage. Used for save as and open
        File file;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(       //adds various extensions for image files: jpg, png, etc.
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );

        if (saving) { //if user is doing a save as operation
            file = fileChooser.showSaveDialog(stage);
        } else {      //if user is doing an open operation
            file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                openFile(file); //open the chosen file
            }
        }
        return file;
    }

    private static void openFile(File file) {   //actually opens an image file
        try {
            System.out.println("Opening file");
            Image image = new Image(String.valueOf(file));
            canvas.setHeight(image.getHeight());
            canvas.setWidth(image.getWidth());
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, image.getWidth(), image.getHeight());
            //desktop.open(file);
        } catch (Exception e) {
            System.out.println("Could not open file.");
        }
    }

    public static void save(File saveFile) {
        if(saveFile==null){      //if a file hasn't been saved before, prompt user to save as.
            saveAs();
        }
        else{                   //otherwise, just save to previous
            System.out.println("Saving...");
            if (saveFile != null) {
                try {
                    WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                    canvas.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    String extension = "";

                    /*int i = saveFile.getName().lastIndexOf('.');
                    if (i > 0) {
                        extension = saveFile.getName().substring(i+1);
                    }*/
                    ImageIO.write(renderedImage, "png", saveFile); //writes file with png writer regardless of format for now... jpg was not working
                } catch (Exception ex) {
                    System.out.println("Error!");
                }}}}
    public static void saveAs() {
        File file = PaintApplication.chooseFile("Save as...", true);  //opens file explorer
        if(file!=null) {    //if the opened file is not null, stored reference to it in lastSaved, save to that file
            lastSaved = file;
            save(file);}}
    public static void newImage() { //sets up a blank canvas
        canvas.setWidth(128);       //default 128x128
        canvas.setHeight(128);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);                                        //fills in canvas with white (slightly distinct from gray background)
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); //sets gc to canvas size
    }
}