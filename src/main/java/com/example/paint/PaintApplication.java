//Steven Engel
//PaintApplication.java
//The "brains" of the operation. This file launches a JavaFX application and instantiates various objects associated with said application.
//This file also controls various functions which relate to the program as a whole (as opposed to particular files/canvases)
package com.example.paint;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
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

/**@author Steven Engel
@PaintApplication.java:
The "brains" of the operation. This class launches a JavaFX application and instantiates various objects associated with said application.
This class also controls various functions which relate to the program as a whole (as opposed to particular files/canvases)
 *
 */
public class PaintApplication extends Application {
    private static Stage stage; //ensures that the stage can be referenced in functions
    private static Scene scene; //allows scene's style to be modified
    private static MyCanvas currentCanvas; //references the canvas currently shown on screen
    private static StackPane currentRoot;  //references the stackpane of canvas and overlay shown on screen
    private static MyToolbar toolbar;      //holds the tools- just below the file menu
    private static ScrollPane sp;          //holds the canvas- allows it to be scrollable
    private static TabPane tabpane;        //holds the tabs, each of which have a canvas associated with them
    private static int tabsClosed;         //keeps track of number of tabs closed- for smart save on close
    private static UndoRedoButton undoButton;   //defines the undo and redo buttons
    private static UndoRedoButton redoButton;
    private static boolean closing = false;     //checks if the user is trying to close the program
    private static boolean nightMode = false;   //tracks if the user has toggled night mode
    private static boolean enableAutoSave = false;     //enable autosave- off by default
    private static Label autoSaveTimer;
    private static boolean showAutoSaveTimer = true;           //boolean for whether autosave timer should be displayed or not
    @Override
    public void start(Stage stage) throws IOException {
        //This section sets up the GUI and menu.
        this.stage = stage;
        stage.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png")));
        BorderPane layout = new BorderPane(); //uses a grid to align gui elements neatly- considering multiple grids for different parts of gui

        this.tabpane = new TabPane(); //for storing different tabs (canvases)
        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        this.sp = new ScrollPane();   //creates a new scrollpane, containing the canvas. This allows image to be scrolled through
        sp.setVisible(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setFitToHeight(true); //center
        sp.setFitToWidth(true);  //center
        sp.setPannable(true);  //allows user to pan through pane
        layout.setCenter(sp);
        layout.setAlignment(sp, Pos.CENTER); //starts with a blank slate
        newImage();
        tabpane.getSelectionModel().selectedItemProperty().addListener(         //if user has switched tab,
                (ov, t, t1) -> {
                    currentCanvas = ((MyTab) t1).getCurrentCanvas();            //change canvas to that tab's canvas
                    this.currentRoot = ((MyTab) t1).getCurrentRoot();
                    //if(toolbar.getSelectedTool()==8&&currentRoot.getChildren().contains(currentCanvas)||((MyTab) t1).getSelection()>=1) //if using select tool, maintain selection and add full stackpane instead of just canvas
                        sp.setContent(currentRoot);
                    //else    //otherwise, just load canvas like normal.
                    //    sp.setContent(currentCanvas);
                    if(currentCanvas.getLastSaved()!=null){                     //Set window title to reflect newly selected tab's contents
                        stage.setTitle("Paint: " + currentCanvas.getLastSaved());}
                    else{
                        stage.setTitle("Paint: New Image");}
                    if(tabpane.getTabs().size()<2)                              //removes close option for last tab to avoid errors
                        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
                    else
                        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
                    toolbar.setupTools(); //updates toolbar to respond to current canvas
                    ((MyTab) t).setAutoSaveTimer(false);        //pause autosave timer for previous tab, resume for new tab
                    ((MyTab) t1).setAutoSaveTimer(true);
                }
        );

        MyMenu menu = new MyMenu(); //see MyMenu.java (extends the MenuBar class)
        this.undoButton = new UndoRedoButton(true, new Image(PaintApplication.class.getResourceAsStream("/tools/undo.png")), 16, 16);
        this.redoButton = new UndoRedoButton(false, new Image(PaintApplication.class.getResourceAsStream("/tools/redo.png")), 16, 16);
        Button saveButton = new Button();
        ImageView image = new ImageView(new Image(PaintApplication.class.getResourceAsStream("/tools/save.png")));         //set the given image to be this button's icon
        image.setFitHeight(16);
        image.setFitHeight(16);
        image.setPreserveRatio(true);                           //gets buttons to display properly
        saveButton.setGraphic(image);
        saveButton.setOnAction(e->{        //behavior for when this is pressed
            save(currentCanvas.getLastSaved());
        });
        this.autoSaveTimer = new Label("");
        HBox menuBox = new HBox(menu, saveButton, undoButton, redoButton, autoSaveTimer); //creates undo and redo buttons, adds to an hbox with rest of menu
        this.toolbar = new MyToolbar(); //see MyToolbar.java (extends the Toolbar class)
        VBox top = new VBox();
        top.getChildren().addAll(menuBox, toolbar, tabpane); //creates a vertical box for the menu, toolbar, and tabpane, allowing them all to rest at the top
        layout.setTop(top);
        layout.setAlignment(top, Pos.TOP_LEFT);

        this.scene = new Scene(layout, 600, 480); //makes a scene (pun intended)
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show(); //Displays stage which hosts the scene which hosts the grid which hosts the canvas which hosts the gc

        //this.zoom();  //sets up zoom properties
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
                else if (event.isControlDown() && (event.getCode() == KeyCode.Z)) { //implements undo with control z
                    undo();
                }
                else if (event.isControlDown() && (event.getCode() == KeyCode.Y)) { //implements redo file with control y
                    redo();
                }
                else if (event.isControlDown() && (event.getCode() == KeyCode.X)) { //implements cut with control X
                    toolbar.cutImage();
                }
                else if (event.isControlDown() && (event.getCode() == KeyCode.C)) { //implements copy with control c
                    toolbar.copyImage();
                }
                else if (event.isControlDown() && (event.getCode() == KeyCode.V)) { //implements paste with control v
                    try{
                        toolbar.pasteImage();
                    }
                    catch(Exception f){}
                }
                else if ((event.getCode() == KeyCode.F11)) { //implements fullscreen toggle
                    getStage().setFullScreen(!PaintApplication.getStage().isFullScreen());
                }
            }});
    }

    /**
     * Simply launches the application. That should be all that this method does.
     * @param args
     */
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
    public static boolean getNightMode(){return nightMode;}
    public static void setNightMode(boolean nM){nightMode=nM;}
    public static boolean getEnableAutoSave(){return enableAutoSave;}
    public static void setEnableAutoSave(boolean eAS){enableAutoSave = eAS;}
    public static Label getAutoSaveTimer(){return autoSaveTimer;}
    public static void setDisplayAutoSaveTimer(boolean set){showAutoSaveTimer = set;}
    public static boolean getDisplayAutoSaveTimer(){return showAutoSaveTimer;}

    public static Scene getScene() {return scene;}
    /**
     * This method increments tabsClosed to keep track of tabs closed for smart save on close
     */
    public static void incrementTabsClosed() {++tabsClosed;     //increment tabsClosed to keep track of tabs closed for smart save on close
        if(((tabsClosed)>=tabpane.getTabs().size())&&closing){
            System.exit(0);                                    //exit if all tabs have been dealt with
        }
    }
    public static void undo(){      //used to undo/redo (for functions outside this class)
        undoButton.undo();
    }
    public static void redo(){
        redoButton.redo();
    }

    /**
     * Opens a file chooser dialogue in stage. Used for save as and open.
     * @param title
     * Sets title of file chooser popup
     * @param saving
     * Determines next course of action based on if user is saving (true) or opening (false) a file
     * @return File (to be saved/opened)
     */
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
    /**
     * Prepares canvas for opening an image file, opens image file, displays on canvas, and creates and switches to a new tab.
     * @param file the file to be opened
     */
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
            currentCanvas.updateUndoStack();       //updates undo stack to retain initial copy of image
        } catch (Exception e) {
            System.out.println("Could not open file.");
        }
    }

    /**
     * Saves the current canvas to an image file of a user-selected type. If user has not saved this canvas before, user is prompted to save as.
     * @param saveFile the file to be saved.
     */
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
                    BufferedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    String extension = "";
                    int i = saveFile.getName().lastIndexOf('.');
                    if (i > 0) {
                        extension = saveFile.getName().substring(i+1);
                    }
                    switch (extension){
                        case "jpg": case "bmp": //for a few formats, transparency cannot be saved. This removes the alpha (TYPE_INT_RGB, not ARGB) to fix that
                            BufferedImage newBufferedImage = new BufferedImage(renderedImage.getWidth(), renderedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                            newBufferedImage.createGraphics().drawImage(renderedImage, 0, 0, java.awt.Color.WHITE, null);
                            renderedImage = newBufferedImage;
                            break;
                    }
                    ImageIO.write(renderedImage, extension, saveFile); //writes file with png writer regardless of format for now... jpg was not working
                    currentCanvas.setScaleX(ogx);
                    currentCanvas.setScaleY(ogy);  //sets canvas scale to its original size
                    currentCanvas.setDirty(false); //canvas is now considered clean (saved)
                    updateTab((MyTab) tabpane.getSelectionModel().getSelectedItem()); //updates this tab's name, getting rid of dirty status
                    ((MyTab) tabpane.getSelectionModel().getSelectedItem()).resetAutoSaveTimer();
                    ((MyTab) tabpane.getSelectionModel().getSelectedItem()).setAutoSaveTimer(true); //resets autosave timer
                } catch (Exception ex) {
                    System.out.println("Error!");
                }}}}

    /**
     * Allows user to choose where a file is saved, utilizing the chooseFile function.
     */
    public static void saveAs() {       //allows user to choose where a file is saved
        MyTab selTab = (MyTab) tabpane.getSelectionModel().getSelectedItem();
        String name = selTab.getTabName();
        File file = chooseFile("Save " + name + " as...", true);  //opens file explorer
        if(file!=null) {    //if the chosen file is not null, stored reference to it in lastSaved, save to that file
            save(file);
            currentCanvas.setLastSaved(file);
            stage.setTitle("Paint: " + file);
            updateTab((MyTab) tabpane.getSelectionModel().getSelectedItem()); //updates this tab's name with new name
        }
        ((MyTab) tabpane.getSelectionModel().getSelectedItem()).setAutoSaveTimer(true); //resets autosave timer
    }

    /**
     * Saves to the files of all currently opened tabs, iterating through each tab and asking the user to save as for files that have not yet been saved.
     */
    public static void saveAll(){
        tabsClosed = 0;
        for (Tab tabs:                  //iterates through all tabs in the tabpane
                tabpane.getTabs()) {
            tabpane.getSelectionModel().select(tabs);           //selects each as it goes along in order
            currentCanvas = ((MyTab)tabs).getCurrentCanvas();   //gets canvas assigned to each to perform save operation correctly
            if (currentCanvas.getDirty()==true){
                ((MyTab)tabs).savePrompt(false); //asks user about each file
            }
            else {
                tabsClosed++;           //increments tabsClosed even for autosaves to avoid inaccurate count
            }
        }
    }

    /**
     * Sets up a blank canvas in a new tab of size 128x128 by default.
     */
    public static void newImage() { //sets up a blank canvas
        currentCanvas = new MyCanvas(128, 128);    //creates a canvas for loading images, drawing on
        currentCanvas.setDirty(true);      //this is considered "dirty" for saving purposes
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
        currentCanvas.updateUndoStack();       //updates undo stack to retain initial copy of image
    }

    /**
     * Completes the setup which allows user to zoom/scale the screen with the mouse.
     */
    public static void zoom() { //handles zooming/scaling with mouse
        currentRoot.setOnScroll(
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent event) {
                        if(event.isControlDown()) { //must have control down
                            double zoomFactor = 1.05;   //factor to zoom by
                            double deltaY = event.getDeltaY();
                            if (deltaY < 0){            //checks if you scroll up, zooms in
                                zoomFactor = 0.95;
                            }
                            currentRoot.setScaleX(currentRoot.getScaleX() * zoomFactor);  //resizes canvas appropriately
                            currentRoot.setScaleY(currentRoot.getScaleY() * zoomFactor);
                            sp.setHmax(sp.getHmax()*zoomFactor);                //sizes scrollpane appropriately
                            sp.setHmin(sp.getHmin()*zoomFactor);
                            sp.setVmax(sp.getVmax()*zoomFactor);                //sizes scrollpane appropriately
                            sp.setVmin(sp.getVmin()*zoomFactor);
                        }
                        event.consume();
                    }});}

    /**
     * Creates a new tab and sets up its zoom controls. Shows its canvas, as well.
     */
    public static void createTab(){
        MyTab tab = new MyTab(currentCanvas);       //creates a new tab
        tabpane.getTabs().add(tab);
        tabpane.getSelectionModel().select(tab);
        currentRoot = tab.getCurrentRoot();
        //sp.setContent(currentCanvas);
        zoom();                                     //gets zoom controls working again
        updateTab(tab);
    }

    /**
     * Updates a tab's name in the visible GUI.
     * @param tab the tab to be updated
     */
    public static void updateTab(MyTab tab){        //updates tab's name with new tab name
        tab.setText(tab.getTabName());
    }

    /**
     * If user hasn't saved some tabs, iterate through tabs and ask user whether they want to save or not.
     * @param event reference to the exiting event
     */
    public void exitProgramWarning(WindowEvent event){
        boolean allClean = true;
        for (Tab tabs:
             tabpane.getTabs()) {
            if(((MyTab)tabs).getCurrentCanvas().getDirty()){            //if any canvases are dirty, set boolean false
                allClean = false;
                break;}}
        if(!allClean){                                                  //if all canvases are clean, skip all of the following
            event.consume();                                                //prevents program from closing immediately
            Stage dialog = new Stage();                                     //creates a new window
            dialog.setTitle("Unsaved Work");
            dialog.initModality(Modality.APPLICATION_MODAL);                //only allows user to open one of these, pushes to front
            dialog.initOwner(PaintApplication.getStage());
            dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
            VBox dialogVbox = new VBox(20);
            Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
            Text t =  new Text("You have one or more unsaved canvases. Would you like to save your work?");
            t.setFont(CS);
            Button saveAllButton = new Button("Save");     //Gives user options for saving, not saving, or cancelling the operation
            saveAllButton.setOnAction(e->{
                dialog.close();
                this.closing = true;
                saveAll();
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
}

