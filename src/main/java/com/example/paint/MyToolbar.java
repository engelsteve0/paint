package com.example.paint;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
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
import javafx.util.Pair;

import java.awt.image.RenderedImage;

/*tools array:
index name
0     line
*/

public class MyToolbar extends ToolBar {
    private Pair<Double, Double> initialTouch;
    private MyCanvas layer;
    private static final int numTools = 1;      //number of tools in tools array. Needs to be incremented to add tools
    private static int selectedTool = -1;
    private int sizeValue;
    private ColorPicker cp;
    private Color selectedColor;
    private StackPane root;
    public MyToolbar(){ //calls Toolbar's constructor with no args
        super();
        //starting with the bottom of the hierarchy up (tool Buttons -> toolBox as a GridPane -> VBox containing the tools and title -> overall toolbar
        ImageButton[] tools = new ImageButton[numTools];      //uses an array to store tool buttons
        tools[0] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/line.png")), 16, 16, 0 );
        GridPane toolBox = new GridPane();
        toolBox.add(tools[0], 0, 0);                    //uses a gridpane to add tools. Can allow for multiple rows etc.
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
        this.getItems().addAll(toolVBox, s1, sizeSelector, s2, cp);         //adds items to toolbar


        cp.setOnAction((EventHandler) t -> selectedColor = cp.getValue());


        this.root = new StackPane(); //is eventually used as an overlay for previewing changes
    }
    public int getSelectedTool(){
        return selectedTool;
    }
    public Color getSelectedColor(){
        return selectedColor;
    }
    public void setSelectedTool(int toolNumber){
        selectedTool = toolNumber;
        //this section handles the functionality of the tools
        switch (selectedTool){
            //0 - drawing a straight line
            case 0: MyCanvas canvas = PaintApplication.getCanvas();
                    canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                            event -> {
                                if(selectedTool==0) {
                                    layer = new MyCanvas((int) canvas.getWidth(), (int) canvas.getHeight());
                                    layer.setScaleX(canvas.getScaleX());
                                    layer.setScaleY(canvas.getScaleY());                        //NOTE - need to fix Stackpane bug
                                    root.getChildren().addAll(canvas, layer);                   //adds canvas and overlay to a temporary stackpane to display both
                                    root.setAlignment(Pos.TOP_LEFT);
                                    PaintApplication.getScrollPane().setContent(root);
                                    //PaintApplication.getScrollPane().setContent(layer);
                                    GraphicsContext context = layer.getGraphicsContext2D();
                                    initDraw(context);
                                    initialTouch = new Pair<>(event.getX(), event.getY());//getSceneX(), event.getSceneY());
                                }
                            });

                canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                        event -> {
                            if(selectedTool==0) {
                                GraphicsContext context = layer.getGraphicsContext2D();
                                initDraw(context);
                                context.clearRect(0, 0, layer.getWidth(), layer.getHeight());
                                context.strokeLine(initialTouch.getKey(), initialTouch.getValue(), event.getX(), event.getY());

                            }
                        });
                canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                        event -> {
                            if(selectedTool==0) {
                                //PaintApplication.getScrollPane().setContent(canvas);

                                GraphicsContext context = canvas.getGraphicsContext2D();
                                initDraw(context);
                                //context.clearRect(0, 0, PaintApplication.getCanvas().getWidth(), PaintApplication.getCanvas().getHeight());
                                context.strokeLine(initialTouch.getKey(), initialTouch.getValue(), event.getX(), event.getY());
                                PaintApplication.getScrollPane().setContent(canvas);    //sets scrollpane's content back to just canvas
                                root.getChildren().removeAll(canvas, layer);            //removes children from stackpane
                                layer = null;                                           //effectively "deletes" layer
                            }
                        });
                break;
        }

    }
    private void initDraw(GraphicsContext gc){  //sets properties of the current graphicscontext so that it doesn't have to be done every single time we want to draw something
        gc.setFill(selectedColor);
        gc.setStroke(selectedColor);
        gc.setLineWidth(sizeValue);

    }
}
