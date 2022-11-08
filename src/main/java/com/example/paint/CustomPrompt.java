//Steven Engel
//CustomPrompt.java
//The CustomPrompt class assists with the setup of popup windows; it is an extension of stage designed to avoid some of the duplicate code associated with creating these windows.
//However, some code will inevitably look similar among all popup windows (especially in MyMenu.java) due to the custom needs of each popup window.
package com.example.paint;

import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Steven Engel
 * The CustomPrompt class assists with the setup of popup windows; it is an extension of stage designed to avoid some of the duplicate code associated with creating these windows. However, some code will inevitably look similar among all popup windows (especially in MyMenu.java) due to the custom needs of each popup window.
 */
public class CustomPrompt extends Stage {
    /**
     * Constructor for a CustomPrompt
     * @param decorated Whether this prompt gives the user the ability to minimize/close etc. (true) or not (false)
     * @param title The title of this popup window
     * @param resizable Whether the user can resize this window (true) or not (false)
     */
    public CustomPrompt(boolean decorated, String title, boolean resizable) {
        if(!decorated)
            this.initStyle(StageStyle.UNDECORATED); //Gets rid of user ability to close this without buttons
        if(!resizable)
            this.setResizable(false);               //does not allow user to resize the new window
        this.setTitle(title);
        this.initModality(Modality.APPLICATION_MODAL);
        this.initOwner(PaintApplication.getStage());
        this.getIcons().add(new Image(PaintApplication.class.getResourceAsStream("/icon.png"))); //adds the official icon to window
    }
}
