//Steven Engel
//MyToolbar.java
//This file houses the functionality and GUI for the toolbar and various tools. The tools[] array contains ImageButtons which represent each of the tools
//The ids and names for these tools are listed below:
/*tools array:
index name
0     pencil
1     eraser
2     line
3     picker
4     dashed line
5     square
6     circle
7     polygon
8     select rectangle
9     Cross/T/+
*/
package com.example.paint;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Steven Engel
 * This file houses the functionality and GUI for the toolbar and various tools. The tools[] array contains ImageButtons which represent each of the tools in the tools array:
 * index, name
 * 0     pencil
 * 1     eraser
 * 2     line
 * 3     picker
 * 4     dashed line
 * 5     square
 * 6     circle
 * 7     polygon
 * 8     select rectangle
 * 9     Cross/T/+
 * 10    Text
 */
public class MyToolbar extends ToolBar {
    private double[] initialTouch;              //tracks initial mouse x and y when drawing
    private static final int numTools = 11;     //number of tools in tools array. Needs to be incremented to add tools
    private static int selectedTool = -1;       //keeps track of which tool is selected. -1 for none selected
    private int sizeValue = 1;                  //keeps track of line width, shape outline size
    private ColorPicker cp;                     //allows user to choose colors for their shapes/lines
    private Color selectedColor;                //keeps track of which color was picked
    private ImageButton[] tools;                //stores the tools themselves
    private int polygonSides;                   //stores number of sides for regular polygon
    private Clipboard clipboard;
    private ClipboardContent clipboardContent = new ClipboardContent();  //stores copied/cut images for pasting

    /**
     * Default constructor. Calls the default toolbar constructor, then adds individual tool ImageButtons, size/color controls.
     */
    public MyToolbar(){ //calls Toolbar's constructor with no args
        super();
        clipboard = Clipboard.getSystemClipboard();
        this.initialTouch = new double[2];
        polygonSides = 3;
        //starting with the bottom of the hierarchy up (tool Buttons -> toolBox as a GridPane -> VBox containing the tools and title -> overall toolbar
        this.tools = new ImageButton[numTools];      //uses an array to store tool buttons
        tools[0] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/pencil.png")), 16, 16, 0 );
        tools[0].setTooltip(new Tooltip("Pencil Tool"));
        tools[1] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/eraser.png")), 16, 16, 1 );
        tools[1].setTooltip(new Tooltip("Eraser Tool"));
        tools[2] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/line.png")), 16, 16, 2 );
        tools[2].setTooltip(new Tooltip("Straight Line Tool"));
        tools[3] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/picker.png")), 16, 16, 3 );
        tools[3].setTooltip(new Tooltip("Color Picker Tool"));
        tools[4] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/dashline.png")), 16, 16, 4 );
        tools[4].setTooltip(new Tooltip("Dashed Line Tool"));
        tools[5] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/square.png")), 16, 16, 5 );
        tools[5].setTooltip(new Tooltip("Rectangle/Square (SHIFT) Tool"));
        tools[6] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/circle.png")), 16, 16, 6 );
        tools[6].setTooltip(new Tooltip("Ellipse/Circle (SHIFT) Tool"));
        tools[7] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/polygon.png")), 16, 16, 7 );
        tools[7].setTooltip(new Tooltip("Polygon Tool"));
        tools[8] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/selectrect.png")), 16, 16, 8 );
        tools[8].setTooltip(new Tooltip("Rectangular/Square Selection Tool"));
        tools[9] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/axes.png")), 16, 16, 9 );
        tools[9].setTooltip(new Tooltip("Axes Tool"));
        tools[10] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/text.png")), 16, 16, 10 );
        tools[10].setTooltip(new Tooltip("Text Tool"));
        GridPane toolBox = new GridPane();
        int j=0;
        for(int i=0; i<numTools; i++)
        {
            if(i>0 && i%10==0)                              //every 10th item, make a new row
                j++;
            toolBox.add(tools[i], i%10, j);              //uses a gridpane to add tools. Can allow for multiple rows etc.
        }

        Label toolLabel = new Label("Tools");              //labels tool section
        VBox toolVBox = new VBox(toolLabel,toolBox);          //adds tools section to toolbar

        Separator s1 = new Separator(Orientation.VERTICAL);                       //adds a separator between tools and size section

        Slider sizeSlider = new Slider(1, 50, 1);                       //creates a slider to control size/width of lines/shapes
        Label sizeLabel = new Label("Size (px): ");                            //adds a label calling this size in pixels
        TextField sizeInput = new TextField("1");                              //adds a text field containing the size in pixels
        sizeSlider.valueProperty().addListener((ov, old_val, new_val) -> {        //updates size based on either slider or text field
            sizeValue = (int) Math.round(new_val.doubleValue());
            sizeSlider.setValue(sizeValue);
            sizeInput.setText(String.valueOf(sizeValue));
        });
        sizeInput.textProperty().addListener((ov, old_val, new_val) -> {
            try{                                        //if user tries to input a non-number, don't let them and instead set it to 1
                sizeValue = Integer.parseInt(new_val);
            }
            catch(Exception e){
                sizeValue=1;
            }

            if(sizeValue<=0||sizeValue>50){             //Limit size to 1-50 range
                sizeValue = 1;
            }
            sizeSlider.setValue(sizeValue);             //updates both slider and text input with new value
            sizeInput.setText(String.valueOf(sizeValue));
        });
        HBox sizeSelectorText = new HBox(sizeLabel, sizeInput);                   //groups label and text field together in an hbox, then adds this to a vbox above the slider
        VBox sizeSelector = new VBox(sizeSelectorText, sizeSlider);
        Separator s2 = new Separator(Orientation.VERTICAL);                       //adds a separator between size and color picker section

        this.cp = new ColorPicker(Color.BLACK);               //creates a new color picker. To be used with tools
        cp.setOnAction((EventHandler) t -> selectedColor = cp.getValue());
        cp.setTooltip(new Tooltip("Color Picker"));
        Separator s3 = new Separator(Orientation.VERTICAL);

        Label transformationLabel = new Label("Transformations");
        Button rotateButton = new Button();             //adds buttons for transformations
        ImageView rotateImage = new ImageView(new Image(PaintApplication.class.getResourceAsStream("/tools/rotate.png")));
        rotateImage.setFitHeight(16);
        rotateImage.setFitWidth(16);
        rotateImage.setPreserveRatio(true);
        rotateButton.setGraphic(rotateImage);
        rotateButton.setTooltip(new Tooltip("Rotate Canvas/Selection"));
        rotateButton.setOnAction(e->{ PaintApplication.getMenu().createRotateCanvasPopup();});        //creates the rotate canvas popup

        Button flipButton = new Button();
        ImageView flipImage = new ImageView(new Image(PaintApplication.class.getResourceAsStream("/tools/flip.png")));
        flipImage.setFitHeight(16);
        flipImage.setFitWidth(16);
        flipImage.setPreserveRatio(true);
        flipButton.setGraphic(flipImage);
        flipButton.setTooltip(new Tooltip("Flip/Mirror Canvas"));
        flipButton.setOnAction(e->{ PaintApplication.getMenu().createFlipCanvasPopup();});        //creates the flip/mirror popup

        Button resizeButton = new Button();
        ImageView resizeImage = new ImageView(new Image(PaintApplication.class.getResourceAsStream("/tools/resize.png")));
        resizeImage.setFitHeight(16);
        resizeImage.setFitWidth(16);
        resizeImage.setPreserveRatio(true);
        resizeButton.setGraphic(resizeImage);
        resizeButton.setTooltip(new Tooltip("Resize Canvas"));
        resizeButton.setOnAction(e->{ PaintApplication.getMenu().createResizePopup();});        //creates the resize popup

        HBox transformationHBox = new HBox(rotateButton, flipButton, resizeButton);
        VBox transformationVBox = new VBox(transformationLabel, transformationHBox);
        Separator s4 = new Separator(Orientation.VERTICAL);

        this.getItems().addAll(toolVBox, s1, sizeSelector, s2, cp, s3, transformationVBox, s4);         //adds items to toolbar
        setupTools();

    }

    /**
     * Dummy constructor designed for unit testing, does nothing.
     * @param yes useless parameter
     */
    public MyToolbar(boolean yes) {}; //dummy constructor for unit testing

    /**
     * accessor for the currently selected tool (id)
     * @return int
     */
    public int getSelectedTool(){
        return selectedTool;
    }

    /**
     * Sets the currently selected tool, -1 if unselecting
     * @param toolNumber the tool id to select (as listed in MyToolbar.java)
     */
    public void setSelectedTool(int toolNumber){
        selectedTool = toolNumber;
        for(int i=0; i<numTools; i++){
            tools[i].setStyle(tools[i].getStyleType("normal"));                         //unselects all buttons
        }
        if (toolNumber>-1)  //if a button is selected
            tools[selectedTool].setStyle(tools[selectedTool].getStyleType("selected")); //shows that only this button is selected
    }

    /**
     * Defines the functions on the canvas based on which tool is currently selected.
     */
    public void setupTools() {
        //this section handles the functionality of the tools
        MyTab thisTab = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem());
        MyCanvas canvas = thisTab.getCurrentCanvas();
        Canvas layer = thisTab.getCurrentLayer();
        StackPane root = thisTab.getCurrentRoot();
        //adds event filter preventing right clicks
        root.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    GraphicsContext context = layer.getGraphicsContext2D();
                    if(selectedTool!=3&&selectedTool!=-1){
                        canvas.setDirty(true);    //set canvas dirty unless just color picking or panning
                    }
                    switch(selectedTool){
                        case 0: case 1:{     //pencil tool, eraser tool (just sets stroke color to white)
                            initDraw(canvas.getGraphicsContext2D());
                            if (selectedTool==1)
                                canvas.getGraphicsContext2D().setStroke(Color.WHITE);
                            canvas.getGraphicsContext2D().strokeLine(event.getX(), event.getY(), event.getX(), event.getY());
                            initialTouch[0] = event.getX();
                            initialTouch[1] = event.getY();
                        }; break;
                        case 2: case 4: case 5: case 6: case 7: case 9:{     //Line-drawing/shape tools (need preview) //Try to delete previous overlay if possible
                            try{
                                layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
                            }
                            catch(Exception e){}
                            layer.setWidth(canvas.getWidth());                          //resizes overlay to fit canvas as necessary
                            layer.setHeight(canvas.getHeight());
                            layer.setScaleX(canvas.getScaleX());
                            layer.setScaleY(canvas.getScaleY());

                            initDraw(context);
                            if(selectedTool==4){                   //use dashed lines with dashed line tool or select, otherwise, don't
                                context.setLineDashes(2*sizeValue);
                                canvas.getGraphicsContext2D().setLineDashes(2*sizeValue);
                            }
                            else{
                                context.setLineDashes(1);
                                context.setLineWidth(sizeValue);
                                canvas.getGraphicsContext2D().setLineDashes(1);
                            }
                            initialTouch[0] = event.getX();
                            initialTouch[1] = event.getY();}
                         break;
                        case 3:{
                            //color picker (user may click to pick a color from canvas)
                            double ogx = canvas.getScaleX();//stores original x and y scales to reset after snapshot
                            double ogy = canvas.getScaleY();
                            canvas.setScaleX(1);            //briefly sets canvas scale to default to avoid snapshot errors
                            canvas.setScaleY(1);
                            cp.setValue(canvas.snapshot(null, null).getPixelReader().getColor((int)event.getX(), (int)event.getY()));
                            canvas.setScaleX(ogx);          //resets scale
                            canvas.setScaleY(ogy);
                        } break;
                        case 8: {                                       //rectselect
                            readyLayer();
                            initDraw(context);
                            context.setLineDashes(2);                   //for dashed effect
                            initialTouch[0] = event.getX();
                            initialTouch[1] = event.getY();
                            context = layer.getGraphicsContext2D();
                                if(thisTab.getSelection()==1&&(event.getX()>=thisTab.getImageSelection().getx1())&&(event.getX()<=(thisTab.getImageSelection().getx1()+thisTab.getImageSelection().getw()))&&(event.getY()>=thisTab.getImageSelection().gety1())&&(event.getX()<=thisTab.getImageSelection().gety1()+thisTab.getImageSelection().geth())){  //if we've selected something but haven't clicked on it yet, register state change to clicked on
                                    thisTab.setSelection(2);
                                    Paint prevCol = canvas.getGraphicsContext2D().getFill();
                                    canvas.getGraphicsContext2D().setFill(Color.WHITE);
                                    canvas.getGraphicsContext2D().fillRect(thisTab.getImageSelection().getx1(), thisTab.getImageSelection().gety1(), thisTab.getImageSelection().getw(), thisTab.getImageSelection().geth()); //clears original box that was selected
                                    canvas.getGraphicsContext2D().setFill(prevCol); //sets fill color back to original
                                }
                                else{
                                    layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
                                    layer.setWidth(canvas.getWidth());                          //resizes overlay to fit canvas as necessary
                                    layer.setHeight(canvas.getHeight());
                                    layer.setScaleX(canvas.getScaleX());
                                    layer.setScaleY(canvas.getScaleY());
                                    initDraw(context);
                                    context.setLineDashes(2);   //uses dashed lines for selection box
                                    initialTouch[0] = event.getX();
                                    initialTouch[1] = event.getY();
                                }
                            }; break;
                        case 10: {
                            try{
                                initialTouch[0] = event.getX(); //get coordinates, then create a prompt allowing user to enter text here
                                initialTouch[1] = event.getY();
                            }
                            catch (Exception e){};
                                Stage dialog = new Stage();                               //creates a new window
                                dialog.initStyle(StageStyle.UNDECORATED);                 //minimizes size of this window
                                dialog.setTitle("Text Tool");
                                dialog.initModality(Modality.APPLICATION_MODAL);
                                dialog.initOwner(PaintApplication.getStage());
                                dialog.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
                                VBox dialogVbox = new VBox(20);
                                String[] FontNames = Font.getFamilies().toArray(new String[0]);
                                Font[] Fonts = new Font[FontNames.length];
                                for(int i=0; i<FontNames.length; i++){
                                    Fonts[i] = new Font(FontNames[i], sizeValue);  //creates an array of fonts corresponding to names array
                                }
                                Text t =  new Text("Enter text to be placed to upper right of location selected. \nFont size and color are controlled by the toolbar.");
                                ComboBox<String> fontDropDown = new ComboBox<>(FXCollections.observableArrayList(FontNames));
                                fontDropDown.getSelectionModel().select(0);
                                fontDropDown.setCellFactory((ListView<String> listView) -> {
                                final ListCell<String> cell = new ListCell<String>() {
                                    @Override
                                    public void updateItem(String item, boolean empty) {    //sets the text of the listed fonts in the dropdown to be the actual fonts
                                        super.updateItem(item, empty);
                                        if (item != null) {
                                            setText(item);
                                            setFont(new Font(item, 12));
                                        }}};
                                    return cell;});
                                TextField tF = new TextField("Enter Text Here");
                                Button applyButton = new Button("Add text to canvas"); //Gives user options for applying text or cancelling the operation
                                applyButton.setOnAction(e->{
                                    GraphicsContext gc = canvas.getGraphicsContext2D();
                                    initDraw(gc);
                                    int selectedFont = fontDropDown.getSelectionModel().getSelectedIndex();
                                    gc.setFont(Fonts[selectedFont]); //sets canvas font
                                    gc.setLineWidth(1);
                                    gc.setFill(cp.getValue());
                                    gc.strokeText(tF.getText(), initialTouch[0], initialTouch[1], canvas.getWidth()-initialTouch[0]); //Draws text on canvas, with the max width being the dist between x and canvas width
                                    gc.fillText(tF.getText(), initialTouch[0], initialTouch[1], canvas.getWidth()-initialTouch[0]);
                                    dialog.close();
                                    canvas.updateUndoStack();       //updates undo stack when feature drawing is ended
                                });
                                Button cancelButton = new Button("Cancel");     //just get rid of the window
                                cancelButton.setOnAction(e->{
                                    dialog.close();
                                });
                                HBox options = new HBox();
                                options.getChildren().addAll(applyButton, cancelButton);
                                dialogVbox.getChildren().addAll(t, fontDropDown, tF, options);                //actually adds text, button to window
                                Scene dialogScene = new Scene(dialogVbox, 450, 180);
                                dialog.setScene(dialogScene);                   //displays window to user
                                dialog.show();
                                dialog.setResizable(false);                     //don't let user resize
                        }
                    } if(selectedTool!=8) { //if using another tool, end selection
                        endDraw();
                        thisTab.setSelection(0);
                    }
        });
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    GraphicsContext context = layer.getGraphicsContext2D(); //previewing on overlay
                    context.setLineDashes(1);
                    canvas.getGraphicsContext2D().setLineDashes(1);
                    switch(selectedTool) {
                        case 0:  case 1:{     //pencil tool, eraser tool (just sets stroke color to white)
                            initDraw(canvas.getGraphicsContext2D());
                            if(selectedTool==1)
                                canvas.getGraphicsContext2D().setStroke(Color.WHITE);
                            canvas.getGraphicsContext2D().strokeLine(initialTouch[0], initialTouch[1], event.getX(), event.getY());
                            initialTouch[0] = event.getX(); //draws a more continuous line by storing previous initialTouch info
                            initialTouch[1] = event.getY();
                        }; break;
                        case 4: case 2:{            //for lines, shapes, use preview tool
                            initDraw(context);
                            if(selectedTool==4){    //use dashed lines with dashed line tool, otherwise, don't
                                context.setLineDashes(2*sizeValue);
                                canvas.getGraphicsContext2D().setLineDashes(2*sizeValue);
                            }
                            context.clearRect(0, 0, layer.getWidth(), layer.getHeight());
                            context.strokeLine(initialTouch[0], initialTouch[1], event.getX(), event.getY());} break;
                        case 5: case 6: case 7: case 9:{                   //draws a square/rectangle/polygon
                            initDraw(context);

                            context.clearRect(0, 0, layer.getWidth(), layer.getHeight());
                            double startx = initialTouch[0];        //handles drawing to areas other than to bottom right
                            double starty = initialTouch[1];
                            double endx = event.getX();
                            double endy = event.getY();
                            if(endx-startx<0){                      //if in quadrants II or III, flip x
                                startx = event.getX();
                                endx = initialTouch[0];
                            }
                            if(endy-starty<0){                      //if in quadrants I or II, flip y
                                starty = event.getY();
                                endy = initialTouch[1];
                            }
                            switch (selectedTool){
                                case 5:
                                    if(event.isShiftDown()) //if shift is down, always draw a square
                                        context.strokeRect(startx, starty, endx-startx, endx-startx);
                                    else                    //otherwise, freeform rectangle
                                        context.strokeRect(startx, starty, endx-startx, endy-starty);; break;
                                case 6:
                                    if(event.isShiftDown()) //if shift is down, always draw a perfect circle
                                        context.strokeOval(startx, starty, endx-startx, endx-startx);
                                    else                    //otherwise, freeform oval
                                        context.strokeOval(startx, starty, endx-startx, endy-starty); break;
                                case 7:
                                    double[] xSides = getPolygonSides(initialTouch[0], initialTouch[1], endx-startx, polygonSides, true);    //specialized method for getting points
                                    double[] ySides = getPolygonSides(initialTouch[0], initialTouch[1], endx-startx, polygonSides, false);
                                    context.strokePolygon(xSides, ySides, polygonSides); break;
                                case 9:
                                    context.strokeLine(initialTouch[0], (initialTouch[1]+event.getY())/2, event.getX(), (initialTouch[1]+event.getY())/2);
                                    context.strokeLine((initialTouch[0]+event.getX())/2, initialTouch[1], (initialTouch[0]+event.getX())/2, event.getY()); break;
                            }
                        } break;
                        case 8: {
                            initDraw(context);
                            if(thisTab.getSelection()==2) {//drag mode
                                try{
                                    context.clearRect(0, 0, layer.getWidth(), layer.getHeight());   //clear layer, move selected portion around
                                    layer.getGraphicsContext2D().drawImage(thisTab.getSelectionImage(), event.getX(), event.getY(), thisTab.getSelectionImage().getWidth(), thisTab.getSelectionImage().getHeight()); //actually draws image
                                }
                                catch(Exception e){}
                            }
                            else{
                                context.clearRect(0, 0, layer.getWidth(), layer.getHeight());
                                double startx = initialTouch[0];        //handles drawing to areas other than to bottom right
                                double starty = initialTouch[1];
                                double endx = event.getX();
                                double endy = event.getY();
                                if(endx-startx<0){                      //if in quadrants II or III, flip x
                                    startx = event.getX();
                                    endx = initialTouch[0];
                                }
                                if(endy-starty<0){                      //if in quadrants I or II, flip y
                                    starty = event.getY();
                                    endy = initialTouch[1];
                                }

                                context.setLineDashes(2);
                                context.setLineWidth(1);
                                if(event.isShiftDown()) //if shift is down, always draw a square
                                    context.strokeRect(startx, starty, endx-startx, endx-startx);
                                else                    //otherwise, freeform rectangle
                                    context.strokeRect(startx, starty, endx-startx, endy-starty);
                            }
                        }
                    }});
        root.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    GraphicsContext context = canvas.getGraphicsContext2D();        //now drawing on the actual canvas
                    switch (selectedTool) {
                        case 0: case 1: ; break;
                        case 2: case 4: {
                            initDraw(context);
                            context.strokeLine(initialTouch[0], initialTouch[1], event.getX(), event.getY());
                            endDraw();
                            } break;
                        case 5: case 6: case 7: case 9: {           //square/circle/polygon/select
                            initDraw(context);
                            double startx = initialTouch[0];        //handles drawing to areas other than to bottom right
                            double starty = initialTouch[1];
                            double endx = event.getX();
                            double endy = event.getY();
                            if(endx-startx<0){                      //if in quadrants II or III, flip x
                                startx = event.getX();
                                endx = initialTouch[0];
                            }
                            if(endy-starty<0){                      //if in quadrants I or II, flip y
                                starty = event.getY();
                                endy = initialTouch[1];
                            }
                            switch (selectedTool){
                                case 5:
                                    if(event.isShiftDown()) //if shift is down, always draw a square
                                        context.strokeRect(startx, starty, endx-startx, endx-startx);
                                    else                    //otherwise, freeform rectangle
                                        context.strokeRect(startx, starty, endx-startx, endy-starty); break;
                                case 6:
                                    if(event.isShiftDown()) //if shift is down, always draw a perfect circle
                                        context.strokeOval(startx, starty, endx-startx, endx-startx);
                                    else                    //otherwise, freeform oval
                                        context.strokeOval(startx, starty, endx-startx, endy-starty); break;
                                case 7:
                                    double[] xSides = getPolygonSides(initialTouch[0], initialTouch[1], endx-startx, polygonSides, true);    //specialized method for getting points
                                    double[] ySides = getPolygonSides(initialTouch[0], initialTouch[1], endx-startx, polygonSides, false);
                                    context.strokePolygon(xSides, ySides, polygonSides); break;
                                case 9:
                                    context.strokeLine(initialTouch[0], (initialTouch[1]+event.getY())/2, event.getX(), (initialTouch[1]+event.getY())/2);
                                    context.strokeLine((initialTouch[0]+event.getX())/2, initialTouch[1], (initialTouch[0]+event.getX())/2, event.getY()); break;
                            }
                            endDraw();
                        } break;
                        case 8: {       //select rectangle
                            context = layer.getGraphicsContext2D();
                            initDraw(context);
                            if(thisTab.getSelection()==2){ //if in dragging mode, "plop" the image down onto the canvas
                                canvas.getGraphicsContext2D().drawImage(thisTab.getSelectionImage(), event.getX(), event.getY(), thisTab.getSelectionImage().getWidth(), thisTab.getSelectionImage().getHeight()); //actually draws image
                                thisTab.setSelection(0);    //go back to base unselected state
                                canvas.updateUndoStack();   //this is an edit, let undo know that
                            }
                            else{
                                context.clearRect(0, 0, layer.getWidth(), layer.getHeight());
                                double startx = initialTouch[0];        //handles drawing to areas other than to bottom right
                                double starty = initialTouch[1];
                                double endx = event.getX();
                                double endy = event.getY();
                                if(endx-startx<0){                      //if in quadrants II or III, flip x
                                    startx = event.getX();
                                    endx = initialTouch[0];
                                }
                                if(endy-starty<0){                      //if in quadrants I or II, flip y
                                    starty = event.getY();
                                    endy = initialTouch[1];
                                }
                                context.setLineDashes(2);
                                context.setLineWidth(1);
                                double width;
                                double height;
                                if(event.isShiftDown()){ //if shift is down, always draw a square
                                    context.strokeRect(startx, starty, endx-startx, endx-startx);
                                    width = endx-startx;
                                    height = endx-startx;
                                }
                                else{                   //otherwise, freeform rectangle
                                    context.strokeRect(startx, starty, endx-startx, endy-starty);
                                    width = endx-startx;
                                    height = endy-starty;
                                }
                                try{
                                    double ogx = canvas.getScaleX();//stores original x and y scales to reset after save
                                    double ogy = canvas.getScaleY();
                                    canvas.setScaleX(1);            //briefly sets canvas scale to default to avoid saving errors
                                    canvas.setScaleY(1);
                                    Rectangle2D bounds = new Rectangle2D(startx, starty, width, height);
                                    WritableImage writableImage = new WritableImage((int) width, (int) height);
                                    SnapshotParameters parameter = new SnapshotParameters();
                                    parameter.setViewport(bounds);
                                    canvas.snapshot(parameter, writableImage);
                                    canvas.setScaleX(ogx);            //briefly sets canvas scale to default to avoid saving errors
                                    canvas.setScaleY(ogy);
                                    thisTab.setSelection(1, writableImage); //stores this image in an image object associated with tab
                                    thisTab.getImageSelection().setSelectionCoordinates(startx, starty, width, height); //stores coordinates associated with selection
                                }
                                catch(Exception e){}
                            }
                            }; break;

                    }
                    if(selectedTool!=3&&selectedTool!=-1&&selectedTool!=8&&selectedTool!=10){
                        canvas.updateUndoStack();       //updates undo stack when feature drawing is ended
                    }
        });
        LogHandler.getLogHandler().writeToLog(false, "Toolbar initialized.");
    }

    /**
     * Sets properties of the current graphicscontext so that it doesn't have to be done every single time we want to draw something.
     * @param gc the current graphics context
     */
    public void initDraw(GraphicsContext gc){  //sets properties of the current graphicscontext so that it doesn't have to be done every single time we want to draw something
        selectedColor = cp.getValue();  //updates color based on colorpicker
        gc.setFill(selectedColor);      //primes tool with proper color/size
        gc.setStroke(selectedColor);
        gc.setLineWidth(sizeValue);
    }

    /**
     * Handles end of preview-draw sequence, clearing overlay layer.
     */
    public void endDraw(){      //handles end of preview-draw sequence
        try {
            Canvas layer = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentLayer();
            layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
        }
        catch(Exception e){}
    }

    /**
     * Returns either the x or y coordinates of each vertex of a sides-sided polygon. Should be called twice, once with x true and once with it false, to get arrays of both x and y coordinates.
     * @param centerX the x coordinate of the center of the polygon
     * @param centerY the y coordinate of the center of the polygon
     * @param radius the radius of the polygon
     * @param sides the number of sides on the polygon
     * @param x Should this return the set of x coordinates (true) or y coordinates (false)?
     * @return
     */
    public double[] getPolygonSides(double centerX, double centerY, double radius, int sides, boolean x) {
        double[] returnX = new double[sides];
        double[] returnY = new double[sides];
        final double angleStep = Math.PI * 2 / sides;
        double angle = 0; // assumes one point is located directly beneath the center point
        for (int i = 0; i < sides; i++, angle += angleStep) {
            //draws rightside-up; to change, change multiple of angle
            returnX[i] = -1 * Math.sin(angle) * radius + centerX; // x coordinate of the corner
            returnY[i] = -1 * Math.cos(angle) * radius + centerY; // y coordinate of the corner
        }
        if(x)
            return returnX;
        else
            return returnY;
    }

    /**
     * Creates a new dialog prompting the user to enter the number of sides (>=3).
     */
    public void promptNumSides(){                                       //creates a dialog prompting user for number of sides
        final CustomPrompt dialog = new CustomPrompt(false, "Draw Regular Polygon", false);                               //creates a new window
        VBox dialogVbox = new VBox(20);
        Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
        Text t =  new Text("Specify the number of sides on the regular polygon: ");
        t.setFont(CS);
        AtomicInteger sidesValue = new AtomicInteger(polygonSides);
        Label sidesLabel = new Label("Number of sides (minimum of 3): ");
        TextField sidesInput = new TextField(String.valueOf(sidesValue.get()));
        HBox sidesBox = new HBox(sidesLabel, sidesInput);
        sidesInput.textProperty().addListener((ov, old_val, new_val) -> {
            try{                                        //if user tries to input a non-number, don't let them and instead set it to 3
                sidesValue.set(Integer.parseInt(new_val));
            }
            catch(Exception e){
                sidesValue.set(3);
            }
            //updates text input with new value
            sidesInput.setText(String.valueOf(sidesValue.get()));
        });
        Button applyButton = new Button("Enter");
        applyButton.setOnAction(e->{
            dialog.close();
            if(sidesValue.get()>=3)                     //last check; if user entered a value less than 3, just make a triangle
                polygonSides = sidesValue.get();
            else
                polygonSides = 3;
        });

        dialogVbox.getChildren().addAll(t, sidesBox, applyButton);                //actually adds text to window
        Scene dialogScene = new Scene(dialogVbox, 600, 120);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
    }

    /**
     * Gets overlay layer ready for pasting into from another tab, initializing select operation.
     */
    public void readyLayer(){   //gets layer ready for pasting from another tab, initializing select operation
        MyTab thisTab = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem());
        MyCanvas canvas = thisTab.getCurrentCanvas();
        Canvas layer = thisTab.getCurrentLayer();
        layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
        layer.setWidth(canvas.getWidth());                          //resizes overlay to fit canvas as necessary
        layer.setHeight(canvas.getHeight());
        layer.setScaleX(canvas.getScaleX());
        layer.setScaleY(canvas.getScaleY());
    }

    /**
     * Copies image into system clipboard.
     */
    public void copyImage(){    //copies image into system clipboard
        this.clipboardContent.putImage(((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getSelectionImage());
        LogHandler.getLogHandler().writeToLog(true, "Selection copied.");
    }

    /**
     * Cuts the current selection from the current canvas, leaving a blank rectangle behind in its place.
     */
    public void cutImage(){
        MyTab thisTab = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem());
        Canvas layer = thisTab.getCurrentLayer();
        MyCanvas canvas = thisTab.getCurrentCanvas();
        layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);                                        //fills in canvas with white (slightly distinct from gray background)
        gc.fillRect(thisTab.getImageSelection().getx1(), thisTab.getImageSelection().gety1(), /*thisTab.getImageSelection().getx1()+*/thisTab.getImageSelection().getw(), /*thisTab.getImageSelection().gety1()+*/thisTab.getImageSelection().geth());               //sets gc to canvas size
        copyImage();            //first, copy the image. Then, leave blank rectangle behind on canvas and deselect
        thisTab.setSelection(0);    //set selection back to 0
        ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas().updateUndoStack();
        LogHandler.getLogHandler().writeToLog(true, "Selection cut.");
    }

    /**
     * Pastes image onto canvas. If user has select tool on, they can click or hold the mouse and drag it into place. Otherwise, it is placed ot the top-left of the image.
     */
    public void pasteImage(){   //pastes image onto canvas near user's current x, y
        MyTab thisTab = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem());
        Image image = this.clipboardContent.getImage(); //actually obtains image from clipboard
        if(selectedTool==8){
            readyLayer();
            thisTab.setSelection(2, image);                      //sets state to let user move this around canvas (click to move)
            thisTab.getCurrentLayer().getGraphicsContext2D().drawImage(image, 0, 0, image.getWidth(), image.getHeight()); //actually draws image
        }
        else{
            thisTab.getCurrentCanvas().getGraphicsContext2D().drawImage(image, 0, 0, image.getWidth(), image.getHeight()); //actually draws image right on canvas
            thisTab.getCurrentCanvas().updateUndoStack(); //lets undo know a change has been made
        }
        LogHandler.getLogHandler().writeToLog(true, "Selection pasted.");
    }

    /**
     * Resizes the canvas to fit the given width/height
     * @param width int representing the new width of the canvas
     * @param height int representing the new height of the canvas
     * @param stretchContent whether to stretch the content of the canvas to fit the new size or not
     */
    public void resizeCanvas(int width, int height, boolean stretchContent){
        MyCanvas currentCanvas = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas();
        MyTab thisTab = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem());
        Canvas layer = thisTab.getCurrentLayer();
        double ogx = currentCanvas.getScaleX();//stores original x and y scales to reset after save
        double ogy = currentCanvas.getScaleY();
        currentCanvas.setScaleX(1);            //briefly sets canvas scale to default to avoid errors
        currentCanvas.setScaleY(1);
        WritableImage writableImage = new WritableImage((int) currentCanvas.getWidth(), (int) currentCanvas.getHeight());
        currentCanvas.snapshot(null, writableImage);
        currentCanvas.setHeight(height);   //resizes canvas, sets dirty status
        currentCanvas.setWidth(width);
        layer.setHeight(height);
        layer.setWidth(width);
        currentCanvas.setDirty(true);
        if(stretchContent)                  //if we want to stretch the content, actually redraw the content, otherwise don't
            currentCanvas.getGraphicsContext2D().drawImage(writableImage, 0, 0, width, height);
        currentCanvas.setScaleX(ogx);            //resets canvas scale
        currentCanvas.setScaleY(ogy);
    }

    /**
     * Rotates the contents of the current canvas or a selection
     * @param degreesCW int, the number of degrees to rotate the canvas by (CW)
     * @param wholeCanvas boolean representing whether the whole canvas or just a selection should be rotated
     */
    public void rotateImage(int degreesCW, boolean wholeCanvas){
        MyTab thisTab = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem());
        MyCanvas canvas = thisTab.getCurrentCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Canvas layer = thisTab.getCurrentLayer();
        GraphicsContext lgc = layer.getGraphicsContext2D();
        boolean select = (!wholeCanvas&&selectedTool==8&&thisTab.getSelection()==1);  //boolean for quick reference to whether selection rotation is being done or not
        double imw;
        double imh;
        double imx1;
        double imy1;
        if(select) {       //if this applies to only selection and a selection has been made, rotate only selection
            imw = thisTab.getImageSelection().getw();    //stores width of selection
            imh = thisTab.getImageSelection().geth();    //stores height of selection
            imx1 = thisTab.getImageSelection().getx1();
            imy1 = thisTab.getImageSelection().gety1();
        }
        else{
            imw = canvas.getWidth();    //stores width of selection
            imh = canvas.getHeight();    //stores height of selection
            imx1 = 0;
            imy1 = 0;
        }
            double centerx = imx1+imw/2;
            double centery = imy1+imh/2;    //stores center point of selection
            double degreesCWR = Math.toRadians(degreesCW);  //converts to radians for trig methods
            Point im1 = new Point((int)imx1, (int)imy1);
            Point im2 = new Point((int)(imx1+imw), (int)imy1);  //4 points, starting with top left of selection and moving CW
            Point im3 = new Point((int)(imx1+imw), (int)(imy1+imh));
            Point im4 = new Point((int)(imx1), (int)(imy1+imh));
            Point2D rotatedim1 = new Point2D.Double();
            Point2D rotatedim2 = new Point2D.Double();
            Point2D rotatedim3 = new Point2D.Double();
            Point2D rotatedim4 = new Point2D.Double();
            AffineTransform rotation = new AffineTransform();
            rotation.rotate(-1*degreesCWR, centerx, centery);   //rotates ccw, so negative to compensate
            rotation.transform(im1, rotatedim1);
            rotation.transform(im2, rotatedim2);
            rotation.transform(im3, rotatedim3);
            rotation.transform(im4, rotatedim4);
            double newimx1 = Math.min(Math.min(rotatedim1.getX(), rotatedim2.getX()),Math.min(rotatedim3.getX(), rotatedim4.getX()));
            double newimy1 = Math.min(Math.min(rotatedim1.getY(), rotatedim2.getY()),Math.min(rotatedim3.getY(), rotatedim4.getY()));
            double neww = Math.max(Math.max(rotatedim1.getX(), rotatedim2.getX()),Math.max(rotatedim3.getX(), rotatedim4.getX()))-newimx1;  //sets new widths and heights
            double newh = Math.max(Math.max(rotatedim1.getY(), rotatedim2.getY()),Math.max(rotatedim3.getY(), rotatedim4.getY()))-newimy1;
            SnapshotParameters sp = new SnapshotParameters();               //takes snapshot of selection on canvas
            Rectangle2D boundsRect = new Rectangle2D(imx1, imy1, imw, imh);
            sp.setViewport(boundsRect);
            Image snapshot = canvas.snapshot(sp, null);
            if(select){
                lgc.clearRect(0, 0, layer.getWidth(), layer.getHeight());  //clears layer, copies selection to layer
                lgc.drawImage(snapshot, imx1, imy1);
                lgc.save(); // Save default transform
                Affine rotate = new Affine();       //creates a new rotation transform about the center of the selection
                rotate.appendRotation(degreesCW, centerx, centery);
                lgc.setTransform(rotate);    //rotates about center of canvas
                lgc.drawImage(snapshot, imx1, imy1);
                lgc.restore(); // Restore default transform
                Rectangle2D newBoundsRect = new Rectangle2D(newimx1, newimy1, neww, newh);
                sp.setViewport(newBoundsRect);  //changes bounds to match bounds needed by rotation
                Image snapshot2 = layer.snapshot(sp, null); //takes snapshot of changes to layer
                gc.setFill(Color.WHITE);
                gc.clearRect(imx1, imy1, imw, imh);   //clears old part of canvas this was rotated from
                gc.fillRect(imx1, imy1, imw, imh);   //clears old part of canvas this was rotated from
                gc.drawImage(snapshot2, newimx1, newimy1, neww, newh);  //draws rotated image on canvas
                lgc.clearRect(0, 0, layer.getWidth(), layer.getHeight());  //clears layer after operation has completed
                thisTab.setSelection(0);    //can no longer move selection
            }

            else{
                int maxDim = (int)Math.max(neww, newh);
                Image snapshot2 = canvas.snapshot(null, null);
                resizeCanvas(maxDim*2, maxDim*2, false);  //resize canvas to max of both canvas's size to be safe, then resize again later if necessary
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, neww, newh);   //clears old canvas
                gc.save(); // Save default transform
                Affine rotate = new Affine();       //creates a new rotation transform about the center of the selection
                rotate.appendTranslation(-(newimx1-imx1), -(newimy1-imy1));
                rotate.appendRotation(degreesCW, centerx, centery);
                gc.setTransform(rotate);    //rotates about center of canvas
                gc.drawImage(snapshot2, 0, 0);
                gc.restore(); // Restore default transform
                resizeCanvas((int)neww, (int)newh, false);
            }
        canvas.updateUndoStack();
        LogHandler.getLogHandler().writeToLog(true, "Rotated " + degreesCW + " degrees clockwise.");
    }

    /**
     * Flips the canvas.
     * @param verticalAxis whether this should be about the vertical axis (horizontal) or not
     */
    public void flipImage(boolean verticalAxis){
        MyTab thisTab = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem());
        MyCanvas canvas = thisTab.getCurrentCanvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Image snapshot = canvas.snapshot(null, null);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());   //temporarily clears canvas to complete flip
        if(verticalAxis)    //do a flip over y axis
            gc.drawImage(snapshot, canvas.getWidth(), 0, -1*canvas.getWidth(), canvas.getHeight()); //flips x
        else
            gc.drawImage(snapshot, 0, canvas.getHeight(), canvas.getWidth(), -1*canvas.getHeight()); //flips y
        canvas.updateUndoStack();
        LogHandler.getLogHandler().writeToLog(true, "Flipped canvas.");
    }
}
