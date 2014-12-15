
/*
 * Author: Dave Walters
 * CMSC 325
 * RandomPhysics.java
 * This class generates some random variables for use with JME3. Can
 * be expanded upon later to contribute to the open source community.
 */
package mygame;

import com.jme3.math.Vector3f;
import java.util.Random;


public class RandomPhysics {
    
    Random randomFloatGenerator = new Random();
    
    public RandomPhysics(){
        
    } //end constructor
    
    /*
     * Returns a randomly generated Vector3f with random X Y and Z coordinates,
     * takes max values for all 3 coordinates to ensure random Vector3f
     * is within bounds of the scene and physics space.
     */
    public Vector3f getRandomVector3f (float maxX, float maxY, float maxZ) {
        
        float X = getRandomFloat(maxX);
        float Y = getRandomFloat(maxY);
        float Z = getRandomFloat(maxZ);   
        
        Vector3f random = new Vector3f(X,Y,Z);
        
        return random;
    } //end getRandomVector3f()
    
    /*
     * Returns a random Vector3f with a positive Y value for setting
     * positive physics locations
     */
    public Vector3f getRandomYPosVector3f (float maxX, float maxY, float maxZ) {
        
        float X = getRandomFloat(maxX);
        float Y = getRandomPosFloat(maxY);
        float Z = getRandomFloat(maxZ);
        
        Vector3f random = new Vector3f(X,Y,Z);
        
        return random;
    }
    
    
   /*
    * Generates a random float with a max value, and then randomly decides on
    * if number should be positive or negative.
    */   
    private float getRandomFloat (float max) {
        
     //Instantiate Random util   

        
      //generates a number between 0 and 1, multiplied by maximum value  
        float randomFloat = (randomFloatGenerator.nextFloat()) * max;
        
      //generates a random number between 1 and 10, and checks %2 = 0
      //if %2 = 0, make randomFloat negative, to simulate generating a random number
        int negativeTest = randomFloatGenerator.nextInt(10);
        
        if (negativeTest %2 ==0){
            randomFloat = (-1)*randomFloat;
        }
        
        return randomFloat;              
    } //end getRandomFloat
       /*
    * Generates a random float with a max value with a positive value
    */   
    private float getRandomPosFloat (float max) {
        
        
      //generates a number between 0 and 1, multiplied by maximum value  
        float randomFloat = (randomFloatGenerator.nextFloat()) * max + 7;
        
        return randomFloat;              
    } //end getRandomFloat
    
    
}
