package com.example.paint;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyToolbarTest {

    @Test
    /**
     * Tests if the polygon side finder function actually does its job right, at least for a basic triangle
     */
    void getPolygonSides() {
        JFXPanel fxPanel = new JFXPanel();      //need this to work
        MyToolbar tb = new MyToolbar(true);
        double[] tA = {0, 15, 15};
        double testArray[] = tb.getPolygonSides(10, 10, 10, 3, false);
        for(int i=0; i<3; i++){
            testArray[i] = Math.round(testArray[i]);
        }
        assertArrayEquals(tA, testArray);
    }
}