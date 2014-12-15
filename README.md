Laser Spheres is an arcade style first person shooter created in JMonkey using Java. The game takes place in a large, 
floating metallic island in space. The premise of the game is simple: shoot the spheres to earn points. 
Each sphere is color coded blue, red, yellow, or cyan, and there is a special sphere type called a health sphere. 
When the spheres are shot, they produce an exploding effect. Blue spheres are worth 20 points, as they travel the fastest 
and are the smallest. Red spheres are worth 15 points, yellow spheres are worth 5 points, and cyan spheres are worth 5 points. 
If the player shoots a health sphere, they are deducted 10 points.

The game has a fully implemented health system with a health status bar. If the player collides with any of the spheres, with 
the exception of the health sphere, they are deducted health. If they have no health left, the game ends. When the player 
reaches 20 health or less, a heart-beat sound plays to warn the player of their health status. To regain health, the player can 
collect the health spheres. Each sphere automatically respawns when they are shot or collided with, at random locations and 
random velocities. 

The game also has a fully functioning timer system, where the game ends after 5 minutes. There is also a fully implemented 
high score system, which saves high scores into an ASCII text file. The game automatically runs checks based on an extended 
ArrayList class called HighScore and adds, subtracts, and notifies the player when they have received a high score.
