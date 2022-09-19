//Steven Engel
//PaintApplication.java
//The "brains" of the operation. This file launches a JavaFX application and instantiates various objects associated with said application.
//This file also controls various functions which relate to the program as a whole (as opposed to particular files/canvases)
package com.example.paint;

import java.awt.image.RenderedImage;
import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.WindowEvent;

public class PaintApplication extends Application {
    private static Stage stage; //ensures that the stage can be referenced in functions
    private static MyCanvas currentCanvas;
    private static MyToolbar toolbar;
    private static ScrollPane sp;
    private static TabPane tabpane;
    @Override
    public void start(Stage stage) throws IOException {
        //This section sets up the GUI and menu.
        this.stage = stage;
        stage.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png")));
        BorderPane layout = new BorderPane(); //uses a grid to align gui elements neatly- considering multiple grids for different parts of gui

        //VBox canvasBox = new VBox();
        //canvasBox.getChildren().add(canvas);           //Not really used now but may be used to contain other tools in the future
        this.tabpane = new TabPane(); //for storing different tabs (canvases)
        this.sp = new ScrollPane();   //creates a new scrollpane, containing the canvas. This allows image to be scrolled through
        sp.setVisible(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setFitToHeight(true); //center
        sp.setFitToWidth(true);  //center
        sp.setPannable(true);  //allows user to pan through pane
        layout.setCenter(sp);
        layout.setAlignment(sp, Pos.CENTER);
        newImage();                                                             //starts with a blank slate
        tabpane.getSelectionModel().selectedItemProperty().addListener(         //if user has switched tab,
                (ov, t, t1) -> {
                    currentCanvas = ((MyTab) t1).getCurrentCanvas();            //change canvas to that tab's canvas
                    sp.setContent(currentCanvas);
                    if(currentCanvas.getLastSaved()!=null){                     //Set window title to reflect newly selected tab's contents
                        stage.setTitle("Paint: " + currentCanvas.getLastSaved());}
                    else{
                        stage.setTitle("Paint: New Image");}
                    if(tabpane.getTabs().size()<2)                              //removes close option for last tab to avoid errors
                        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
                    else
                        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
                    toolbar.setupTools(); //updates toolbar to respond to current canvas
                }
        );
        MyMenu menu = new MyMenu(); //see MyMenu.java (extends the MenuBar class)
        this.toolbar = new MyToolbar(); //see MyToolbar.java (extends the Toolbar class)
        VBox top = new VBox();
        top.getChildren().addAll(menu, toolbar, tabpane); //creates a vertical box for the menu, toolbar, and tabpane, allowing them all to rest at the top
        layout.setTop(top);
        layout.setAlignment(top, Pos.TOP_LEFT);

        Scene scene = new Scene(layout, 600, 480); //makes a scene (pun intended)
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show(); //Displays stage which hosts the scene which hosts the grid which hosts the canvas which hosts the gc

        this.zoom();  //sets up zoom properties
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::exitProgramWarning);
        //This section houses the keyboard shortcuts.
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {       //implements save with ctrl + s
            @Override
            public void handle(KeyEvent event) {
                if (event.isControlDown() && (event.getCode() == KeyCode.S)) {
                    save(currentCanvas.getLastSaved());
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
        launch();       //launches the application! :)
    }

    public static Stage getStage() { //accessor method for stage in case anything external needs it. Will expand these for others as more classes branch out and need them
        return stage;
    }
    public static MyCanvas getCanvas() { return currentCanvas;};
    public static MyToolbar getToolbar() { return toolbar;}
    public static ScrollPane getScrollPane() {return sp;}
    public static TabPane getTabPane() {return tabpane;}

    public static File chooseFile(String title, Boolean saving) {    //opens a file chooser dialogue in stage. Used for save as and open
        File file;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(       //adds various extensions for image files: jpg, png, etc.
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
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
            currentCanvas = new MyCanvas(128, 128);    //creates a canvas for loading images, drawing on
            currentCanvas.setLastSaved(file);                           //keeps track that this file should be saved to
            Image image = new Image(String.valueOf(file));
            currentCanvas.setHeight(image.getHeight());        //adjust canvas according to image size
            currentCanvas.setWidth(image.getWidth());
            currentCanvas.getGraphicsContext2D().drawImage(image, 0, 0, image.getWidth(), image.getHeight()); //actually draws image
            stage.setTitle("Paint: " + file);      //renames title to reflect name of file
            currentCanvas.setScaleX(1);            //sets canvas scale to default to avoid losing image
            currentCanvas.setScaleY(1);
            sp.setHmax(1);
            sp.setVmax(1);
            sp.setHmin(0);
            sp.setVmin(0);
            createTab();
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
                    double ogx = currentCanvas.getScaleX();//stores original x and y scales to reset after save
                    double ogy = currentCanvas.getScaleY();
                    currentCanvas.setScaleX(1);            //briefly sets canvas scale to default to avoid saving errors
                    currentCanvas.setScaleY(1);
                    WritableImage writableImage = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                    currentCanvas.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    String extension = "";
                    int i = saveFile.getName().lastIndexOf('.');
                    if (i > 0) {
                        extension = saveFile.getName().substring(i+1);
                    }
                    ImageIO.write(renderedImage, "png", saveFile); //writes file with png writer regardless of format for now... jpg was not working
                    currentCanvas.setScaleX(ogx);
                    currentCanvas.setScaleY(ogy);  //sets canvas scale to its original size
                    currentCanvas.setDirty(false); //canvas is now considered clean (saved)
                    updateTab((MyTab) tabpane.getSelectionModel().getSelectedItem()); //updates this tab's name, getting rid of dirty status
                } catch (Exception ex) {
                    System.out.println("Error!");
                }}}}
    public static void saveAs() {       //allows user to choose where a file is saved
        MyTab selTab = (MyTab) tabpane.getSelectionModel().getSelectedItem();
        String name = selTab.getTabName();
        File file = chooseFile("Save " + name + " as...", true);  //opens file explorer
        if(file!=null) {    //if the chosen file is not null, stored reference to it in lastSaved, save to that file
            save(file);
            currentCanvas.setLastSaved(file);
            stage.setTitle("Paint: " + file);
            updateTab((MyTab) tabpane.getSelectionModel().getSelectedItem()); //updates this tab's name with new name
        }}
    public static void saveAll(){
        for (Tab tabs:
                tabpane.getTabs()) {
            tabpane.getSelectionModel().select(tabs);
            currentCanvas = ((MyTab)tabs).getCurrentCanvas();
            /*if (currentCanvas.getDirty()==false){
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Save " + ((MyTab) tabs).getTabName() + "?", ButtonType.YES, ButtonType.CLOSE);
                a.show();
            }*/
            save(((MyTab)tabs).getCurrentCanvas().getLastSaved());
            //tabpane.getTabs().remove(tabs);
        }
    }
    public static void newImage() { //sets up a blank canvas
        currentCanvas = new MyCanvas(128, 128);    //creates a canvas for loading images, drawing on
        currentCanvas.setWidth(128);       //default 128x128, like in regular MS Paint
        currentCanvas.setHeight(128);
        GraphicsContext gc = currentCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);                                        //fills in canvas with white (slightly distinct from gray background)
        gc.fillRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight()); //sets gc to canvas size
        stage.setTitle("Paint: New Image");                             //makes sure user knows they're working on a new image
        currentCanvas.setLastSaved(null);                               //fixes a bug whereby user could work on a new image and then automatically save/overwrite an old image
        if(sp!=null){
            sp.setHmax(1);
            sp.setVmax(1);
            sp.setHmin(0);
            sp.setVmin(0);
        }
        currentCanvas.setScaleX(1);            //sets canvas scale to default to avoid losing image
        currentCanvas.setScaleY(1);
        createTab();                           //creates a new tab with this canvas
    }
    public static void zoom() { //handles zooming/scaling with mouse
        currentCanvas.setOnScroll(
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent event) {
                        if(event.isControlDown()) { //must have control down
                            double zoomFactor = 1.05;   //factor to zoom by
                            double deltaY = event.getDeltaY();
                            if (deltaY < 0){            //checks if you scroll up, zooms in
                                zoomFactor = 0.95;
                            }
                            currentCanvas.setScaleX(currentCanvas.getScaleX() * zoomFactor);  //resizes canvas appropriately
                            currentCanvas.setScaleY(currentCanvas.getScaleY() * zoomFactor);
                            sp.setHmax(sp.getHmax()*zoomFactor);                //sizes scrollpane appropriately
                            sp.setHmin(sp.getHmin()*zoomFactor);
                            sp.setVmax(sp.getVmax()*zoomFactor);                //sizes scrollpane appropriately
                            sp.setVmin(sp.getVmin()*zoomFactor);
                        }
                        event.consume();
                    }});}
    public static void createTab(){
        MyTab tab = new MyTab(currentCanvas);       //creates a new tab
        tabpane.getTabs().add(tab);
        tabpane.getSelectionModel().select(tab);
        sp.setContent(currentCanvas);
        zoom();                                     //gets zoom controls working again
        updateTab(tab);
    }
    public static void updateTab(MyTab tab){
        tab.setText(tab.getTabName());
    }
    public void exitProgramWarning(WindowEvent event){
        event.consume();                                                //prevents program from closing immediately
        final Stage dialog = new Stage();                               //creates a new window
        dialog.setTitle("Warning: Unsaved Work");
        dialog.initModality(Modality.APPLICATION_MODAL);                //only allows user to open one of these, pushes to front
        dialog.initOwner(PaintApplication.getStage());
        dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
        VBox dialogVbox = new VBox(20);
        Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
        Text t =  new Text("You have one or more unsaved canvases. Would you like to save your work before exiting?");
        t.setFont(CS);
        Button saveAllButton = new Button("Save"); //Gives user options for saving, not saving, or cancelling the operation
        saveAllButton.setOnAction(e->{
            dialog.close();
            saveAll();
            Platform.exit();
        });
        Button closeButton = new Button("Don't Save");  //close program without saving
        closeButton.setOnAction(e->{
            dialog.close();
            Platform.exit();
        });
        Button cancelButton = new Button("Cancel");     //just get rid of the window
        cancelButton.setOnAction(e->{
            dialog.close();
            event.consume();
        });
        HBox options = new HBox();
        options.getChildren().addAll(saveAllButton, closeButton, cancelButton);
        dialogVbox.getChildren().addAll(t, options);                //actually adds text, button to window
        Scene dialogScene = new Scene(dialogVbox, 450, 60);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
        dialog.setResizable(false);                     //don't let user resize; this is just an alert window
    }

}

