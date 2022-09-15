package com.example.paint;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.concurrent.atomic.AtomicBoolean;

/*tools array:
index name
0     pencil
1     eraser
2     line
*/

public class MyToolbar extends ToolBar {
    private Pair<Double, Double> initialTouch; //tracks initial mouse x and y when drawing
    private MyCanvas layer;                     //serves as a temporary overlay for previewing
    private static final int numTools = 3;      //number of tools in tools array. Needs to be incremented to add tools
    private static int selectedTool = -1;       //keeps track of which tool is selected. -1 for none selected
    private int sizeValue;                      //keeps track of line width, shape outline size
    private ColorPicker cp;                     //allows user to choose colors for their shapes/lines
    private Color selectedColor;                //keeps track of which color was picked
    private StackPane root;                     //temporary stackpane for housing overlay, etc.
    private ToggleGroup tg;                     //stores toggle buttons (tools)
    private ImageButton[] tools;                //stores the tools themselves
    public MyToolbar(){ //calls Toolbar's constructor with no args
        super();
        //starting with the bottom of the hierarchy up (tool Buttons -> toolBox as a GridPane -> VBox containing the tools and title -> overall toolbar
        this.tools = new ImageButton[numTools];      //uses an array to store tool buttons
        tools[0] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/pencil.png")), 16, 16, 0 );
        tools[1] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/eraser.png")), 16, 16, 1 );
        tools[2] = new ImageButton(new Image(PaintApplication.class.getResourceAsStream("/tools/line.png")), 16, 16, 2 );
        //ToggleGroup tg =  new ToggleGroup();
        GridPane toolBox = new GridPane();
        for(int i=0; i<numTools; i++)
        {
            int j=0;
            if(i>0 && i%10==0)                              //every 10th item, make a new row
                j++;
            toolBox.add(tools[i], i%10, j);              //uses a gridpane to add tools. Can allow for multiple rows etc.
            //tools[i].setToggleGroup(tg);
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
        this.getItems().addAll(toolVBox, s1, sizeSelector, s2, cp);         //adds items to toolbar


        cp.setOnAction((EventHandler) t -> selectedColor = cp.getValue());


        this.root = new StackPane(); //is eventually used as an overlay for previewing changes
        this.layer = new MyCanvas((int) PaintApplication.getCanvas().getWidth(), (int) PaintApplication.getCanvas().getHeight());

        //this section handles the functionality of the tools
        MyCanvas canvas = PaintApplication.getCanvas();                                         //adds event filter preventing right clicks
        AtomicBoolean rightClick = new AtomicBoolean(false);
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
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {

                    switch(selectedTool){
                        case 0:  case 1:{     //pencil tool, eraser tool (just sets stroke color to white)
                            initDraw(canvas.getGraphicsContext2D());
                            if (selectedTool==1){
                                canvas.getGraphicsContext2D().setStroke(Color.WHITE);
                            }
                            canvas.getGraphicsContext2D().strokeLine(event.getX(), event.getY(), event.getX(), event.getY());
                            initialTouch = new Pair<>(event.getX(), event.getY());
                        }; break;
                        case 2: {     //Line-drawing tool                                      //Try to delete previous overlay if possible
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
                            GraphicsContext context = layer.getGraphicsContext2D();
                            initDraw(context);
                            initialTouch = new Pair<>(event.getX(), event.getY());//getSceneX(), event.getSceneY());
                        } break;
                    }});
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    switch(selectedTool) {
                        case 0: case 1:{     //pencil tool, eraser tool (just sets stroke color to white)
                            initDraw(canvas.getGraphicsContext2D());
                            if(selectedTool==1){
                                canvas.getGraphicsContext2D().setStroke(Color.WHITE);
                            }
                            canvas.getGraphicsContext2D().strokeLine(event.getX(), event.getY(), event.getX(), event.getY());
                        }; break;
                        case 2: {
                            GraphicsContext context = layer.getGraphicsContext2D();
                            initDraw(context);
                            context.clearRect(0, 0, layer.getWidth(), layer.getHeight());
                            context.strokeLine(initialTouch.getKey(), initialTouch.getValue(), event.getX(), event.getY());} break;
                    }});
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    switch (selectedTool) {
                        case 0: case 1: ; break;
                        case 2: {
                            //PaintApplication.getScrollPane().setContent(canvas);

                            GraphicsContext context = canvas.getGraphicsContext2D();
                            initDraw(context);
                            //context.clearRect(0, 0, PaintApplication.getCanvas().getWidth(), PaintApplication.getCanvas().getHeight());
                            context.strokeLine(initialTouch.getKey(), initialTouch.getValue(), event.getX(), event.getY());
                            PaintApplication.getScrollPane().setContent(canvas);    //sets scrollpane's content back to just canvas
                            try {
                                layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight()); //clears layer
                                root.getChildren().removeAll(canvas, layer);            //removes children from stackpane
                            }
                            catch(Exception e){}} break;
                    }});
    }
    public ToggleGroup getToggleGroup(){
        return tg;
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
            tools[i].setStyle(tools[i].getStyleType("normal"));                     //unselects all buttons
        }
        if (toolNumber>-1)  //if a button is selected
            tools[selectedTool].setStyle(tools[selectedTool].getStyleType("selected")); //shows that only this button is selected
    }
    private void initDraw(GraphicsContext gc){  //sets properties of the current graphicscontext so that it doesn't have to be done every single time we want to draw something
        selectedColor = cp.getValue();
        gc.setFill(selectedColor);
        gc.setStroke(selectedColor);
        gc.setLineWidth(sizeValue);

    }
}
