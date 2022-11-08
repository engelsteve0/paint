//Steven Engel
//PaintApplication.java
//The "brains" of the operation. This file launches a JavaFX application and instantiates various objects associated with said application.
//This file also controls various functions which relate to the program as a whole (as opposed to particular files/canvases)
package com.example.paint;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.*;

import javax.imageio.ImageIO;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;

/**@author Steven Engel
The "brains" of the operation. This class launches a JavaFX application and instantiates various objects associated with said application.
This class also controls various functions which relate to the program as a whole (as opposed to particular files/canvases)
 *
 */
public class PaintApplication extends Application {
    private static Stage stage;                 //ensures that the stage can be referenced in functions
    private static Scene scene;                 //allows scene's style to be modified
    private static MyCanvas currentCanvas;      //references the canvas currently shown on screen
    private static StackPane currentRoot;       //references the stackpane of canvas and overlay shown on screen
    private static MyToolbar toolbar;           //holds the tools- just below the file menu
    private static ScrollPane sp;               //holds the canvas- allows it to be scrollable
    private static TabPane tabpane;             //holds the tabs, each of which have a canvas associated with them
    private static int tabsClosed;              //keeps track of number of tabs closed- for smart save on close
    private static UndoRedoButton undoButton;   //defines the undo and redo buttons
    private static UndoRedoButton redoButton;
    private static boolean closing = false;     //checks if the user is trying to close the program
    private static boolean nightMode = false;   //tracks if the user has toggled night mode
    private static boolean enableAutoSave = false;     //enable autosave- off by default
    private static Label autoSaveTimer;
    private static boolean showAutoSaveTimer = true;   //boolean for whether autosave timer should be displayed or not
    private static BufferedImage renderedImage;
    private static String extension;
    private static int autoSaveDuration = 300;         //stores autosave duration in seconds
    private static MyMenu menu;
    @Override
    //Starts the application, effectively the "constructor"
    public void start(Stage stage) throws IOException {

        //sets up the log handler
        String[] dummyArgs = {""};
        LogHandler.main(dummyArgs);
        LogHandler.getLogHandler().writeToLog(false, "Application started successfully.");

        //This section sets up the GUI and menu.
        PaintApplication.stage = stage;
        stage.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png")));
        BorderPane layout = new BorderPane();                               //uses a grid to align gui elements neatly- considering multiple grids for different parts of gui
        tabpane = new TabPane();                                       //for storing different tabs (canvases)
        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        sp = new ScrollPane();                                         //creates a new scrollpane, containing the canvas. This allows image to be scrolled through
        sp.setVisible(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        sp.setPannable(true);                                               //allows user to pan through pane
        layout.setCenter(sp);
        BorderPane.setAlignment(sp, Pos.CENTER);
        newImage();                                                         //starts with a blank slate
        tabpane.getSelectionModel().selectedItemProperty().addListener(     //if user has switched tab,
                (ov, t, t1) -> {
                    currentCanvas = ((MyTab) t1).getCurrentCanvas();        //change canvas to that tab's canvas
                    currentRoot = ((MyTab) t1).getCurrentRoot();
                    sp.setContent(currentRoot);
                    if(currentCanvas.getLastSaved()!=null){                 //Set window title to reflect newly selected tab's contents
                        stage.setTitle("Paint: " + currentCanvas.getLastSaved());}
                    else{
                        stage.setTitle("Paint: New Image");}
                    if(tabpane.getTabs().size()<2)                          //removes close option for last tab in pane to avoid errors
                        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
                    else
                        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
                    toolbar.setupTools();                                   //updates toolbar to respond to current canvas
                    ((MyTab) t).setAutoSaveTimer(false);                    //pause autosave timer for previous tab, resume for new tab
                    ((MyTab) t1).setAutoSaveTimer(true);
                }
        );

        menu = new MyMenu(); //see MyMenu.java (extends the MenuBar class)
        undoButton = new UndoRedoButton(true, new Image(PaintApplication.class.getResourceAsStream("/tools/undo.png")), 16, 16);
        undoButton.setTooltip(new Tooltip("Undo (CTRL + Z)"));
        redoButton = new UndoRedoButton(false, new Image(PaintApplication.class.getResourceAsStream("/tools/redo.png")), 16, 16);
        redoButton.setTooltip(new Tooltip("Redo (CTRL + Y)"));
        Button saveButton = new Button();
        saveButton.setTooltip(new Tooltip("Save (CTRL + S)"));
        ImageView image = new ImageView(new Image(PaintApplication.class.getResourceAsStream("/tools/save.png"))); //set the given image to be this button's icon
        image.setFitHeight(16);
        image.setFitHeight(16);
        image.setPreserveRatio(true);                           //gets buttons to display properly
        saveButton.setGraphic(image);
        saveButton.setOnAction(e->{                             //Saves when save button is pressed
            save(currentCanvas.getLastSaved());
        });
        autoSaveTimer = new Label("");
        HBox menuBox = new HBox(menu, saveButton, undoButton, redoButton, autoSaveTimer); //creates undo and redo buttons, adds to an hbox with rest of menu
        toolbar = new MyToolbar(); //see MyToolbar.java (extends the Toolbar class)
        VBox top = new VBox();
        top.getChildren().addAll(menuBox, toolbar, tabpane); //creates a vertical box for the menu, toolbar, and tabpane, allowing them all to rest at the top
        layout.setTop(top);
        BorderPane.setAlignment(top, Pos.TOP_LEFT);

        scene = new Scene(layout, 600, 480); //makes a scene (pun intended)
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show(); //Displays stage which hosts the scene which hosts the grid which hosts the canvas which hosts the gc

        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::exitProgramWarning);
        //This section houses the keyboard shortcuts.
        scene.setOnKeyReleased(event -> {
            String kc = event.getCode().toString();
            if(event.isControlDown()){
                switch (kc){
                    case "S": save(currentCanvas.getLastSaved()); break;                //implements save with control s
                    case "O": chooseFile("Open Image file", false); break;   //implements open with control o
                    case "N": newImage(); break;                                        //implements new file with control n
                    case "Z": undo(); break;                                            //implements undo with control z
                    case "Y": redo(); break;                                            //implements redo file with control y
                    case "X": toolbar.cutImage(); break;                                //implements cut with control X
                    case "C": toolbar.copyImage(); break;                               //implements copy with control c
                    case "V": try{                                                      //implements paste with control v
                        toolbar.pasteImage();
                    }
                    catch(Exception f){}; break;
                }
            }
            else if ((event.getCode() == KeyCode.F11)) { //implements fullscreen toggle
                getStage().setFullScreen(!PaintApplication.getStage().isFullScreen());
            }
        });

    }

    /**
     * Launches the JavaFX application.
     * @param args
     */
    public static void main(String[] args) {
        launch();}       //Launches the application

    /**
     * Accessor for the main stage
     * @return Stage
     */
    public static Stage getStage() { //accessor method for stage in case anything external needs it. Will expand these for others as more classes branch out and need them
        return stage;
    }

    /**
     * Accessor for the canvas of the currently selected tab
     * @return MyCanvas
     */
    public static MyCanvas getCanvas() { return currentCanvas;}

    /**
     * Accessor for the menu
     * @return MyMenu
     */
    public static MyMenu getMenu() { return menu;}

    /**
     * Accessor for the toolbar
     * @return MyToolbar
     */
    public static MyToolbar getToolbar() { return toolbar;}

    /**
     * Accessor for the ScrollPane (contains canvas, overlay)
     * @return ScrollPane
     */
    public static ScrollPane getScrollPane() {return sp;}

    /**
     * Accessor for the tabpane
     * @return TabPane
     */
    public static TabPane getTabPane() {return tabpane;}

    /**
     * Returns whether nightMode is enabled or not
     * @return boolean
     */
    public static boolean getNightMode(){return nightMode;}
    public static void setNightMode(boolean nM){nightMode=nM;}

    /**
     * Returns whether autosave is enabled or not
     * @return boolean
     */
    public static boolean getEnableAutoSave(){return enableAutoSave;}

    /**
     * Sets whether autosave itself is enabled or not
     * @param eAS boolean controlling whether autosave should be enabled (true) or not (false)
     */
    public static void setEnableAutoSave(boolean eAS){enableAutoSave = eAS;}

    /**
     * Returns the label representing the graphical display of the autosave timer
     * @return Label
     */
    public static Label getAutoSaveTimer(){return autoSaveTimer;}

    /**
     * Sets whether the autosave timer is set or not
     * @param set if yes, show autosave timer
     */
    public static void setDisplayAutoSaveTimer(boolean set){showAutoSaveTimer = set;}

    /**
     * Returns the boolean controlling whether the autosave timer should be shown or not
     * @return boolean
     */
    public static boolean getDisplayAutoSaveTimer(){return showAutoSaveTimer;}

    /**
     * Returns the overall scene for the application
     * @return Scene
     */
    public static Scene getScene() {return scene;}

    /**
     * Gets the length between autosaves, in seconds
     * @return int
     */
    public static int getAutoSaveDuration(){return autoSaveDuration;}

    /**
     * Sets the length between autosaves in seconds
     * @param asD length between autosaves, in seconds
     */
    public static void setAutoSaveDuration(int asD){autoSaveDuration = asD;}
    /**
     * This method increments tabsClosed to keep track of tabs closed for smart save on close
     */
    public static void incrementTabsClosed() {++tabsClosed;     //increment tabsClosed to keep track of tabs closed for smart save on close
        if(((tabsClosed)>=tabpane.getTabs().size())&&closing){
            System.exit(0);                                    //exit if all tabs have been dealt with
        }
    }

    /**
     * Calls the undo function through the undoButton
     */
    public static void undo(){      //used to undo/redo (for functions outside this class)
        undoButton.undo();
    }

    /**
     * Calls the redo function through the redoButton
     */
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
            String logFile = file.getName();
            LogHandler.getLogHandler().writeToLog(true, "Opened " + logFile + " in new tab.");
        } catch (Exception e) {
            LogHandler.getLogHandler().writeToLog(false, "Could not open file.");
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
            if (saveFile != null) {
                boolean savePrompted = false;   //checks if user has been prompted to save or not
                try {
                    double ogx = currentCanvas.getScaleX();//stores original x and y scales to reset after save
                    double ogy = currentCanvas.getScaleY();
                    currentCanvas.setScaleX(1);            //briefly sets canvas scale to default to avoid saving errors
                    currentCanvas.setScaleY(1);
                    WritableImage writableImage = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
                    currentCanvas.snapshot(null, writableImage);
                    renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    extension = "";
                    int i = saveFile.getName().lastIndexOf('.');
                    if (i > 0) {
                        extension = saveFile.getName().substring(i+1);
                    }
                    switch (extension){
                        case "jpg": case "bmp": //for a few formats, transparency cannot be saved. This removes the alpha (TYPE_INT_RGB, not ARGB) to fix that
                            File previousFile;
                            String previousExtension = "";
                            try{
                                previousFile = currentCanvas.getLastSaved();
                                int j = previousFile.getName().lastIndexOf('.');
                                if (j > 0) {
                                    previousExtension = previousFile.getName().substring(j+1);
                                }
                                System.out.println(previousExtension);
                            }
                            catch(Exception e){previousFile = null;}    //for first file being saved, don't lead to the warning
                                if(previousExtension.equals("png")||previousExtension.equals("png*")) {     //if saving from a png to something else, warn about transparency loss
                                    //create warning popup
                                    savePrompted = true;
                                    CustomPrompt dialog = new CustomPrompt(false, "WARNING!", false); //creates a new popup window asking if user still wants to save
                                    VBox dialogVbox = new VBox(20);
                                    Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
                                    Text t = new Text("Saving this image in that format will cause transparency to be lost. Are you sure that you want to save?");
                                    t.setFont(CS);
                                    Button saveButton = new Button("Yes, Save"); //Gives user options for saving, not saving, or cancelling the operation
                                    saveButton.setOnAction(e-> {
                                        try {
                                            writeImage(saveFile);
                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                        ((MyTab) tabpane.getSelectionModel().getSelectedItem()).resetAutoSaveTimer();
                                            ((MyTab) tabpane.getSelectionModel().getSelectedItem()).setAutoSaveTimer(true); //resets autosave timer
                                            currentCanvas.setDirty(false); //canvas is now considered clean (saved)
                                            updateTab((MyTab) tabpane.getSelectionModel().getSelectedItem()); //updates this tab's name, getting rid of dirty status
                                            currentCanvas.setLastSaved(saveFile);
                                            stage.setTitle("Paint: " + saveFile);
                                            updateTab((MyTab) tabpane.getSelectionModel().getSelectedItem()); //updates this tab's name with new name
                                            dialog.close();
                                            LogHandler.getLogHandler().writeToLog(true, "Saved to " + saveFile.getName());
                                    });
                                    Button cancelButton = new Button("Cancel");     //just get rid of the window
                                    cancelButton.setOnAction(e -> {
                                        dialog.close();
                                    });
                                    HBox options = new HBox();
                                    options.getChildren().addAll(saveButton, cancelButton);
                                    dialogVbox.getChildren().addAll(t, options);                //actually adds text, button to window
                                    Scene dialogScene = new Scene(dialogVbox, 600, 60);
                                    dialog.setScene(dialogScene);                   //displays window to user
                                    dialog.show();
                                }
                            BufferedImage newBufferedImage = new BufferedImage(renderedImage.getWidth(), renderedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                            newBufferedImage.createGraphics().drawImage(renderedImage, 0, 0, java.awt.Color.WHITE, null);
                            renderedImage = newBufferedImage;
                            break;
                    }
                    if(!savePrompted){
                        ImageIO.write(renderedImage, extension, saveFile); //writes file
                        ((MyTab) tabpane.getSelectionModel().getSelectedItem()).resetAutoSaveTimer();
                        ((MyTab) tabpane.getSelectionModel().getSelectedItem()).setAutoSaveTimer(true); //resets autosave timer
                        currentCanvas.setDirty(false); //canvas is now considered clean (saved)
                        updateTab((MyTab) tabpane.getSelectionModel().getSelectedItem()); //updates this tab's name, getting rid of dirty status
                        currentCanvas.setLastSaved(saveFile);
                        stage.setTitle("Paint: " + saveFile);
                        updateTab((MyTab) tabpane.getSelectionModel().getSelectedItem()); //updates this tab's name with new name
                        LogHandler.getLogHandler().writeToLog(true, "Saved to " + saveFile.getName());
                    }
                    currentCanvas.setScaleX(ogx);
                    currentCanvas.setScaleY(ogy);  //sets canvas scale to its original size
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
        }
        ((MyTab) tabpane.getSelectionModel().getSelectedItem()).setAutoSaveTimer(true); //resets autosave timer
        LogHandler.getLogHandler().writeToLog(true, "Saved as.");
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
            if (currentCanvas.getDirty()){
                ((MyTab)tabs).savePrompt(false); //asks user about each file
            }
            else {
                tabsClosed++;           //increments tabsClosed even for autosaves to avoid inaccurate count
            }
        }
    }
    public static void writeImage(File saveFile) throws IOException {
        ImageIO.write(renderedImage, extension, saveFile); //writes file
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
        LogHandler.getLogHandler().writeToLog(true, "New canvas created.");
    }
    /**
     * Completes the setup which allows user to zoom/scale the screen with the mouse.
     */
    public static void zoom() { //handles zooming/scaling with mouse
        currentRoot.setOnScroll(
                event -> {
                    if (event.isControlDown()) { //must have control down
                        double zoomFactor = 1.05;   //factor to zoom by
                        double deltaY = event.getDeltaY();
                        if (deltaY < 0) {            //checks if you scroll up, zooms in
                            zoomFactor = 0.95;
                        }
                        currentRoot.setScaleX(currentRoot.getScaleX() * zoomFactor);  //resizes canvas appropriately
                        currentRoot.setScaleY(currentRoot.getScaleY() * zoomFactor);
                        sp.setHmax(sp.getHmax() * zoomFactor);                //sizes scrollpane appropriately
                        sp.setHmin(sp.getHmin() * zoomFactor);
                        sp.setVmax(sp.getVmax() * zoomFactor);                //sizes scrollpane appropriately
                        sp.setVmin(sp.getVmin() * zoomFactor);
                    }
                    event.consume();
                });}
    /**
     * Creates a new tab and sets up its zoom controls. Shows its canvas, as well.
     */
    public static void createTab(){
        MyTab tab = new MyTab(currentCanvas);       //creates a new tab
        tabpane.getTabs().add(tab);
        tabpane.getSelectionModel().select(tab);
        currentRoot = tab.getCurrentRoot();
        zoom();                                     //gets zoom controls working again
        updateTab(tab);
        LogHandler.getLogHandler().writeToLog(true, "New tab created.");
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
        LogHandler.getLogHandler().writeToLog(false, "User attempted to exit program.");
        boolean allClean = true;
        for (Tab tabs:
             tabpane.getTabs()) {
            if(((MyTab)tabs).getCurrentCanvas().getDirty()){            //if any canvases are dirty, set boolean false
                allClean = false;
                break;}}
        if(!allClean){                                                  //if all canvases are clean, skip all of the following
            event.consume();                                                //prevents program from closing immediately
            CustomPrompt dialog = new CustomPrompt(true, "Unsaved Work", false);      //creates a new window
            VBox dialogVbox = new VBox(20);
            Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
            Text t =  new Text("You have one or more unsaved canvases. Would you like to save your work?");
            t.setFont(CS);
            Button saveAllButton = new Button("Save");     //Gives user options for saving, not saving, or cancelling the operation
            saveAllButton.setOnAction(e->{
                dialog.close();
                for (Tab tabs:                                //stops all autosave timer threads to ensure that the entire application closes
                        tabpane.getTabs()) {
                    ((MyTab) tabs).stopAutoSaveTimer();
                }
                closing = true;
                saveAll();
            });
            Button closeButton = new Button("Don't Save");  //close program without saving
            closeButton.setOnAction(e->{
                dialog.close();
                for (Tab tabs:                                  //stops all autosave timer threads to ensure that the entire application closes
                        tabpane.getTabs()) {
                    ((MyTab) tabs).stopAutoSaveTimer();
                }
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
        }
        else {
            for (Tab tabs:
                    tabpane.getTabs()) {
                ((MyTab) tabs).stopAutoSaveTimer();         //stops all autosave timer threads to ensure that the entire application closes
            }
            LogHandler.stopThread();
        }
    }
}

