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
*/
package com.example.paint;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MyToolbar extends ToolBar {
    private double[] initialTouch; //tracks initial mouse x and y when drawing
    private static final int numTools = 9;      //number of tools in tools array. Needs to be incremented to add tools
    private static int selectedTool = -1;       //keeps track of which tool is selected. -1 for none selected
    private int sizeValue = 1;                  //keeps track of line width, shape outline size
    private ColorPicker cp;                     //allows user to choose colors for their shapes/lines
    private Color selectedColor;                //keeps track of which color was picked
    private ImageButton[] tools;                //stores the tools themselves
    private int polygonSides;                   //stores number of sides for regular polygon
    public MyToolbar(){ //calls Toolbar's constructor with no args
        super();
        this.initialTouch = new double[2];
        polygonSides = 3;
        //starting with the bottom of the hierarchy up (tool Buttons -> toolBox as a GridPane -> VBox containing the tools and title -> overall toolbar
        this.tools = new ImageButton[numTools];      //uses an array to store tool buttons
        tools[0] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/pencil.png")), 16, 16, 0 );
        tools[1] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/eraser.png")), 16, 16, 1 );
        tools[2] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/line.png")), 16, 16, 2 );
        tools[3] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/picker.png")), 16, 16, 3 );
        tools[4] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/dashline.png")), 16, 16, 4 );
        tools[5] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/square.png")), 16, 16, 5 );
        tools[6] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/circle.png")), 16, 16, 6 );
        tools[7] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/polygon.png")), 16, 16, 7 );
        tools[8] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/selectrect.png")), 16, 16, 8 );
        GridPane toolBox = new GridPane();
        for(int i=0; i<numTools; i++)
        {
            int j=0;
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
        Separator s3 = new Separator(Orientation.VERTICAL);
        this.getItems().addAll(toolVBox, s1, sizeSelector, s2, cp, s3);         //adds items to toolbar
        cp.setOnAction((EventHandler) t -> selectedColor = cp.getValue());


        setupTools();

    }
    public int getSelectedTool(){
        return selectedTool;
    }
    public Color getSelectedColor(){
        return selectedColor;
    }
    public void setSelectedTool(int toolNumber){
        selectedTool = toolNumber;
        for(int i=0; i<numTools; i++){
            tools[i].setStyle(tools[i].getStyleType("normal"));                         //unselects all buttons
        }
        if (toolNumber>-1)  //if a button is selected
            tools[selectedTool].setStyle(tools[selectedTool].getStyleType("selected")); //shows that only this button is selected
    }
    public void setupTools() {
        //this section handles the functionality of the tools
        MyTab thisTab = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem());
        MyCanvas canvas = thisTab.getCurrentCanvas();
        Canvas layer = thisTab.getCurrentLayer();
        StackPane root = thisTab.getCurrentRoot();
        AtomicBoolean rightClick = new AtomicBoolean(false);                           //adds event filter preventing right clicks
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, e ->                                    //prevents bug where canvas would freeze up on right mouse click. Disables right click
        {
            if( e.isSecondaryButtonDown()) {
                rightClick.set(true);
                e.consume();
            }});
        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, e ->                                    //carries above to next two events with a boolean
        {
            if( rightClick.get()==true) {
                e.consume();
            }});
        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e ->
        {
            if( rightClick.get()==true) {
                rightClick.set(false);
                e.consume();
            }});
        layer.addEventHandler(MouseEvent.MOUSE_PRESSED,     //special cases for where we need to save what is in the layer, such as selecting
                event-> {
                    GraphicsContext context = layer.getGraphicsContext2D();
                    if(selectedTool==8) {           //rectselect
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
                    else{           //if using another tool, end layer/selection
                        endDraw(canvas);
                        thisTab.setSelection(false);
                    }


                });
        layer.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    GraphicsContext context = layer.getGraphicsContext2D();
                    if(selectedTool==8) {
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

                        context.setLineDashes(2);
                        context.setLineWidth(1);
                        if(event.isShiftDown()) //if shift is down, always draw a square
                                context.strokeRect(startx, starty, endx-startx, endx-startx);
                        else                    //otherwise, freeform rectangle
                                context.strokeRect(startx, starty, endx-startx, endy-starty);

                    }
                });
        layer.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    GraphicsContext context = layer.getGraphicsContext2D();
                    if(selectedTool==8) {
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
                            WritableImage writableImage = new WritableImage((int) width, (int) height);
                            SnapshotParameters parameter = new SnapshotParameters();
                            parameter.setTransform(new Translate(startx, starty));
                            canvas.snapshot(parameter, writableImage);
                            canvas.setScaleX(ogx);            //briefly sets canvas scale to default to avoid saving errors
                            canvas.setScaleY(ogy);
                            thisTab.setSelection(true, writableImage);
                        }
                        catch(Exception e){}
                    }

                });
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {

                    GraphicsContext context = layer.getGraphicsContext2D();
                    if(selectedTool!=3&&selectedTool!=-1){
                        canvas.setDirty(true);    //set canvas dirty unless just color picking or panning
                        //canvas.updateUndoRedoStack();       //updates undo stack when feature drawing is initiated
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
                        case 2: case 4: case 5: case 6: case 7:{     //Line-drawing/shape tools (need preview) //Try to delete previous overlay if possible
                            try{
                                layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
                                root.getChildren().removeAll(canvas, layer);            //removes children from stackpane
                            }
                            catch(Exception e){}
                            layer.setWidth(canvas.getWidth());                          //resizes overlay to fit canvas as necessary
                            layer.setHeight(canvas.getHeight());
                            layer.setScaleX(canvas.getScaleX());
                            layer.setScaleY(canvas.getScaleY());
                            root.getChildren().addAll(canvas, layer);                   //adds canvas and overlay to a temporary stackpane to display both
                            root.setAlignment(Pos.TOP_LEFT);
                            PaintApplication.getScrollPane().setContent(root);

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
                        case 8: {                                       //if selecting, switch to drawing on layer
                            if(!root.getChildren().contains(layer)) {
                            root.getChildren().removeAll(canvas, layer);            //removes children from stackpane
                                root.getChildren().addAll(canvas, layer);                   //adds canvas and overlay to a temporary stackpane to display both
                                root.setAlignment(Pos.TOP_LEFT);
                                PaintApplication.getScrollPane().setContent(root);
                            }
                            layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
                            layer.setWidth(canvas.getWidth());                          //resizes overlay to fit canvas as necessary
                            layer.setHeight(canvas.getHeight());
                            layer.setScaleX(canvas.getScaleX());
                            layer.setScaleY(canvas.getScaleY());
                            initDraw(context);
                            context.setLineDashes(2);
                            initialTouch[0] = event.getX();
                            initialTouch[1] = event.getY();}

                    }});
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    GraphicsContext context = layer.getGraphicsContext2D();
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
                        case 5: case 6: case 7:{                   //draws a square/rectangle/polygon
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
                            if(selectedTool==5/*||selectedTool==8*/){ //square/rect or selectrect
                                //if(selectedTool==8) {
                                //    context.setLineDashes(2);
                                //    context.setLineWidth(1);
                               // }
                                if(event.isShiftDown()) //if shift is down, always draw a square
                                    context.strokeRect(startx, starty, endx-startx, endx-startx);
                                else                    //otherwise, freeform rectangle
                                    context.strokeRect(startx, starty, endx-startx, endy-starty);
                            }
                            else if(selectedTool==6){ //circle/oval
                                if(event.isShiftDown()) //if shift is down, always draw a perfect circle
                                    context.strokeOval(startx, starty, endx-startx, endx-startx);
                                else                    //otherwise, freeform oval
                                    context.strokeOval(startx, starty, endx-startx, endy-starty);
                            }
                            else if(selectedTool==7){ //polygon
                                double[] xSides = getPolygonSides(initialTouch[0], initialTouch[1], endx-startx, polygonSides, true);    //specialized method for getting points
                                double[] ySides = getPolygonSides(initialTouch[0], initialTouch[1], endx-startx, polygonSides, false);
                                context.strokePolygon(xSides, ySides, polygonSides);
                            }
                        } break;

                    }});
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    GraphicsContext context = canvas.getGraphicsContext2D();
                    switch (selectedTool) {
                        case 0: case 1: ; break;
                        case 2: case 4: {
                            initDraw(context);
                            context.strokeLine(initialTouch[0], initialTouch[1], event.getX(), event.getY());
                            endDraw(canvas);
                            } break;
                        case 5: case 6: case 7: {                   //square/circle/polygon/select
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
                            if(selectedTool==5){ //square/rect
                                if(event.isShiftDown()) //if shift is down, always draw a square
                                    context.strokeRect(startx, starty, endx-startx, endx-startx);
                                else                    //otherwise, freeform rectangle
                                    context.strokeRect(startx, starty, endx-startx, endy-starty);
                            }
                            else if(selectedTool==6){ //circle/oval
                                if(event.isShiftDown()) //if shift is down, always draw a perfect circle
                                    context.strokeOval(startx, starty, endx-startx, endx-startx);
                                else                    //otherwise, freeform oval
                                    context.strokeOval(startx, starty, endx-startx, endy-starty);
                            }
                            else if(selectedTool==7){ //polygon
                                double[] xSides = getPolygonSides(initialTouch[0], initialTouch[1], endx-startx, polygonSides, true);    //specialized method for getting points
                                double[] ySides = getPolygonSides(initialTouch[0], initialTouch[1], endx-startx, polygonSides, false);
                                context.strokePolygon(xSides, ySides, polygonSides);
                            }
                                endDraw(canvas);
                        } break;
                    }
                    if(selectedTool!=3&&selectedTool!=-1){
                        canvas.updateUndoStack();       //updates undo stack when feature drawing is initiated
                    }
        });
    }
    public void initDraw(GraphicsContext gc){  //sets properties of the current graphicscontext so that it doesn't have to be done every single time we want to draw something
        selectedColor = cp.getValue();  //updates color based on colorpicker
        gc.setFill(selectedColor);      //primes tool with proper color/size
        gc.setStroke(selectedColor);
        gc.setLineWidth(sizeValue);
    }
    public void endDraw(MyCanvas canvas){      //handles end of preview-draw sequence
        PaintApplication.getScrollPane().setContent(canvas);    //sets scrollpane's content back to just canvas
        try {
            Canvas layer = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentLayer();
            StackPane root = ((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentRoot();
            layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
            root.getChildren().removeAll(canvas, layer);        //removes children from stackpane
        }
        catch(Exception e){}
    }
    private static double[] getPolygonSides(double centerX, double centerY, double radius, int sides, boolean x) {
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
    public void promptNumSides(){                                       //creates a dialog prompting user for number of sides
        final Stage dialog = new Stage();                               //creates a new window
        dialog.setTitle("Draw Regular Polygon");
        dialog.initModality(Modality.APPLICATION_MODAL);                //only allows user to open one of these, pushes to front
        dialog.initOwner(PaintApplication.getStage());
        dialog.initStyle(StageStyle.UNDECORATED);
        VBox dialogVbox = new VBox(20);
        Font CS = new Font("Times New Roman", 12);  //Changed to Times New Roman because Comic Sans was too fun
        Text t =  new Text("Specify the number of sides on the regular polygon: ");
        t.setFont(CS);
        AtomicInteger sidesValue = new AtomicInteger(polygonSides);
        Label sidesLabel = new Label("Number of sides: ");
        TextField sidesInput = new TextField(String.valueOf(sidesValue.get()));
        HBox sidesBox = new HBox(sidesLabel, sidesInput);
        sidesInput.textProperty().addListener((ov, old_val, new_val) -> {
            try{                                        //if user tries to input a non-number, don't let them and instead set it to 3
                sidesValue.set(Integer.parseInt(new_val));
            }
            catch(Exception e){
                sidesValue.set(3);
            }
            if(sidesValue.get()<3){                           //Limit size to >2 range
                sidesValue.set(3);
            }
            //updates text input with new value
            sidesInput.setText(String.valueOf(sidesValue.get()));
        });
        Button applyButton = new Button("Enter");
        applyButton.setOnAction(e->{
            dialog.close();
            polygonSides = sidesValue.get();
        });

        dialogVbox.getChildren().addAll(t, sidesBox, applyButton);                //actually adds text to window
        Scene dialogScene = new Scene(dialogVbox, 600, 120);
        dialog.setScene(dialogScene);                   //displays window to user
        dialog.show();
    }
}
