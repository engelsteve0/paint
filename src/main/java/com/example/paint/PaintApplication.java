package com.example.paint;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class PaintApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GridPane grid = new GridPane(); //uses a grid to align gui elements neatly- considering multiple grids for different parts of gui
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10); //sets distance between elements in grid
        grid.setVgap(10);
        //grid.setPadding(new Insets(25, 25, 25, 25));


        final Menu fileMenu = new Menu("File"); //creates the menu bar across the top. Menus are subject to change.
        final Menu optionsMenu = new Menu("View");
        final Menu helpMenu = new Menu("About");
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, optionsMenu, helpMenu);
        grid.add(menuBar, 0, 0); //adds to top left of window

        final Canvas canvas = new Canvas(250,250);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.BLUE);
        gc.fillRect(75,75,100,100);
        grid.add(canvas, 0, 1);

        Scene scene = new Scene(grid, 300, 275); //makes a scene (pun intended)
        stage.setScene(scene);
        stage.setTitle("Paint");

        StackPane root = new StackPane(); //Adds grid to stackpane
        root.getChildren().add(grid);
        stage.setScene(new Scene(root, 300, 250));
        stage.show(); //Displays stage which hosts the scene which hosts the grid
    }

    public static void main(String[] args) {
        launch();
    }
}