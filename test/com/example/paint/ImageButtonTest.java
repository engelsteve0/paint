package com.example.paint;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageButtonTest {

    @Test
    /**
     * checks if selected style has changed (false) or not (true)
     */
    void getStyleType() { //checks if selected style has changed (false) or not (true)
        JFXPanel fxPanel = new JFXPanel();      //need this to work
        ImageButton iB = new ImageButton(null, 32, 32, 0);
        assertEquals("-fx-background-color: transparent; -fx-padding: 2, 2, 2, 2; -fx-border-color: orange; -fx-border-width: 1 1 1 1;", iB.getStyleType("selected"));
    }
}