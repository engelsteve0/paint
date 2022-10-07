package com.example.paint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageSelectionTest {

    @Test //test getter methods to make sure they are actually returning the correct values
    /**
     * test getter methods to make sure they are actually returning the correct values
     */
    void setSelectionCoordinates() {
        ImageSelection iS = new ImageSelection();
        iS.setSelectionCoordinates(0, 1, 2, 3);
        assertEquals(0, iS.getx1());
        assertEquals(1, iS.gety1());
        assertEquals(2, iS.getw());
        assertEquals(3, iS.geth());
    }
}