/*
 * This class contains all logic to read and update the high score.
 */
package mygame;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author dwalters
 */
public class HighScore extends ArrayList<HighScoreKeeper> {
    
    private String filePath = "HighScore.txt";
    private BufferedWriter writer;
    private String builtString;
    
    public HighScore () {
        
        populateHighScoreList();
    }
    
    /*
     * This populates an array list of high scores based on HighScore.txt
     * The placement determines the players level -1.
     * Example: Place 0 in highScoreList is equal to the highest score.
     *  Place 1 in highScoreList is equal to the 2nd highest score.
     */
    
    private void populateHighScoreList () {  
        
        try {
            Scanner reader = new Scanner(new FileReader(filePath));

            while (reader.hasNext()){
                if (reader.next().matches("1.")){
                    String name = (reader.nextLine());
                    int score = (reader.nextInt());
                    add(0,new HighScoreKeeper(name,score));
                } //end if
                if (reader.hasNext() && reader.next().matches("2.")){
                    String name = (reader.nextLine());
                    int score = (reader.nextInt());
                    add(1,new HighScoreKeeper(name,score));
                } //end if
                if (reader.hasNext() && reader.next().matches("3.")){
                    String name = (reader.nextLine());
                    int score = (reader.nextInt());
                    add(2,new HighScoreKeeper(name,score));
                } //end if
                if (reader.hasNext() && reader.next().matches("4.")){
                    String name = (reader.nextLine());
                    int score = (reader.nextInt());
                    add(3,new HighScoreKeeper(name,score));
                } //end if
                if (reader.hasNext() && reader.next().matches("5.")){
                    String name = (reader.nextLine());
                    int score = (reader.nextInt());
                    add(4,new HighScoreKeeper(name,score));
                } //end if
            } //end while loop
            System.out.println(this.size());
        } //end try
        catch (FileNotFoundException ex) {
            builtString = "Unable to load high scores."
                    + "\n Reason: " + ex.getLocalizedMessage();
            System.out.println (builtString);
        }         
        catch (IOException e){
            builtString = "Unable to load high scores."
                    + "\n Reason: " + e.getLocalizedMessage();
            System.out.println (builtString);
        }

    } //end populateHighScoreList()
    
    public void recordToFile () {
        try {
            FileWriter file = new FileWriter(filePath, false);
            writer = new BufferedWriter(file);
            int i = 0;
           for (HighScoreKeeper hsObject : this) {
               writer.write((i+1)+ ". ");
               writer.write(hsObject.getName());
               writer.flush();
               writer.newLine();
               writer.write(Integer.toString(hsObject.getScore()));
               writer.newLine();
               writer.flush();
               i++;
           }
            writer.flush();
        } //end try
        
        catch (IOException e) {
            builtString = "Unable to write high scores."
                    + "\n Reason: " + e.getLocalizedMessage();
            System.out.println (builtString);
        } //end catch
    }
    

}
