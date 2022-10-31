//Steven Engel
//LogHandler.java
//This class handles all of the logging/threading for said logging. The setup function serves as a constructor and is called in main,
//while writeToLog can be called from anywhere.
package com.example.paint;

import javafx.concurrent.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Steven Engel
 * @LogHandler.java: This class handles all of the logging/threading for said logging. The setup function serves as a constructor and is called in main
 * while writeToLog can be called from anywhere.
 */
public class LogHandler extends Thread{
    private static File logFile;
    //starts the thread
    private static Queue<Runnable> runnableQueue = new LinkedBlockingQueue<Runnable>();
    private static LogHandler lh;
    public static LogHandler getLogHandler(){
        return lh;
    }
    public void run() { //what the thread actually runs
            try{
                if(runnableQueue.size()>0)  //checks to make sure that there is actually something to run
                    runnableQueue.remove().run(); //removes runnable from queue, runs it
            }
            catch(Exception e){}
    }

    public static void main(String[] args){
        try {
            String userHome =  System.getProperty("user.home"); //gets user's home directory
            String dateTime = (LocalDateTime.now()).toString(); //creates a filename based on the current date/time
            String fileName = dateTime.substring(0, 4) + dateTime.substring(5, 7) + dateTime.substring(8, 13) + dateTime.substring(14, 16) + dateTime.substring(17, 19);
            logFile = new File(userHome + "/paint/logs/" + fileName + ".txt"); //creates folder for logs
            logFile.getParentFile().mkdirs();   //makes sure the directory gets created
            logFile.createNewFile();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        lh = new LogHandler();
        lh.start();
        lh.writeToLog(false,"Log file created: " + logFile.getName()); //This log file has been created. Does not need filename because log is global.
    }

    /**
     * Attempts to clean out the log directory, deleting all logs except for the current log
     */
    public static void cleanLogDir(){
        String userHome =  System.getProperty("user.home"); //gets user's home directory
        File dir = new File(userHome + "/paint/logs/"); //gets log directory
        for (File file: dir.listFiles()) {
            if(file.equals(logFile)) {      //if this is the log file currently being worked on...
                //do nothing
            } else {                        //otherwise...
                //delete file
                file.delete();
            }

        }
    }
    /**
     * Stops any threads currently running
     */
    public static void stopThread(){
        lh.interrupt();
    }
    class logWriter implements Runnable{
        private boolean addFileName;
        private String text;
        public logWriter(boolean aFN, String txt){  //gets parameters from writer method
            addFileName = aFN;
            text = txt;
        }
        @Override
        public void run() {
            try{
                FileWriter myWriter = new FileWriter(logFile, true);              //writes text to file
                String dateTime = (LocalDateTime.now()).toString();         //gets current date/time for log events
                myWriter.append(dateTime.substring(5,7)+"/"+dateTime.substring(8,10)+"/"+dateTime.substring(0,4)+" "); //MM/DD/YYYY
                if(addFileName) { //if we want to add the name of the current file, get it from the current tab's name
                    try {
                        if(!(((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas().getLastSaved().equals(null)))
                            myWriter.append(((MyTab) PaintApplication.getTabPane().getSelectionModel().getSelectedItem()).getCurrentCanvas().getLastSaved() + " ");
                        else
                            myWriter.append("(Unsaved Image) ");
                    } catch (Exception e) {myWriter.append("(Unsaved Image) ");}
                }
                myWriter.append(dateTime.substring(11, 19) + " ");
                myWriter.append(text + "\n");   //writes the above plus the text passed in as an arg
                myWriter.close();       //closes the file writer to be safe
            }
            catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
    /**
     * Writes to a log with the current date/time under userhome/paint
     * Log has the format of each line: MM/DD/YYYY [filename, if included] hh:mm:ss [text]
     * @param addFileName Whether to add a file name to the log line or not.
     * @param text The status message that should be written to the log
     */
    public void writeToLog(boolean addFileName, String text){
        logWriter lw = new logWriter(addFileName, text);
        runnableQueue.add(lw);
        lh.run();       //runs thread on-demand
    }

}
