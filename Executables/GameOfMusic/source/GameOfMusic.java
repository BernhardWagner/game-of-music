import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 
import java.io.File; 
import ddf.minim.*; 
import ddf.minim.analysis.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class GameOfMusic extends PApplet {

 //<>//




  //size in pixels of one game of life field
  int oneFieldSize = 460;
  
  //how big should one box be (median)
  double boxSize = 10;
  
  //creation of the game field for the game of Life
  Field field = new Field(oneFieldSize, oneFieldSize, (int)boxSize);
  
  
  //rotation of the whole field
  float wholeFieldRotation;
  
  //direction of the rotation of the big field
  float direction;
  
  //Booleans for the modes to switch throught with the numbers
  boolean threeD;
  boolean specialMode;
  boolean specialMode2;
  boolean specialMode3;
  boolean specialMode4;
  
  //maximum age a cell can live before it dies (poor cell)
  int maxGeneration;
  
  //cooldown count for the placing of an object in the center (so the cells have enough time to move)
  boolean cooldown;
  int coolDownCounter;
  
  //when in add color mode (s pressed), then the colors should dim a little bit, so not everything is white
  float blendAddColorDecreaser;
  
  //didnt work
  //Eqaualizer
 // Equalizer equalizer;
  
  
  //variables for music
  Minim minim;
  AudioPlayer song;
  FFT fft;
  //BeatDetect beat;
  
  //current song chosen and its position
  String currentSong = "";
  int songPosition;
  
  //variable for the whole gameOfLife-Logic
  GameOfLife game = new GameOfLife();
  
  
  // scores for the zones
  float scoreLow = 0;
  float scoreMid = 0;
  float scoreHi = 0;
  
  //old score values to calculate the reduction (d\u00e4mpfung)
  float oldScoreLow = scoreLow;
  float oldScoreMid = scoreMid;
  float oldScoreHi = scoreHi;
  
  //values to tweak
  // decrease value
  float scoreDecreaseRate;    //standard  = 25
  int glitchFactor;          //standart = 250;
  
  //when the play button is hit then playing will be set true
  boolean playing;
  
  
  //color tweaks
  float redMultiplicator = 1;
  float blueMultiplicator = 1;
  float greenMultiplicator = 1;
  float brightness = 0;
  
  //bass tweaks
  int bassTolerant;
  
  //for Random seed
  float maxBass = 1000;
  float maxHi = 500;

  //----------------------------------------------------------------------------------------------------------------
  
  public void setup() {
    //sets playing and cooldown at the beginning to false
    playing=false;
    cooldown = false;
    
    //set up the window
    //size(1920,1080,P3D);      //uncomment to switch to windows mode
                 //comment to turn of fullscreen
    background(0);
    
    //song should start at beginning
    songPosition = 0;
    
    //creates the gui with the help of the G4P GUI Builder @see http://www.lagers.org.uk/g4p/
    createGUI();
    
    //reads the files out from the scetch folder
    String[] files = readMp3s("songs");
    fileChooser.setItems(files,0);
    
    //sets all special modes off    
    threeD = false;
    specialMode = false;
    specialMode2 = false;
    specialMode3 = false;
    specialMode4 = false;
    blendAddColorDecreaser = 1;
    
  }
  
  public void setupGameOfLife(){
    if(songPosition == 0){
      //sets up the camera
      camera(width/2, height/2, 800, width/2, height/2, 0, 0, 1, 0);

      //sets up the max living time of a cell to 65 generations
      maxGeneration = 65;
      
      //rotation of the field should be 0 at the beginning and the direction should be right (1)
      wholeFieldRotation = 0;
      direction = 1;
      
      coolDownCounter = 0;
      
      field.setFirstObject(0,0);
      
      //setUp for the Music Analisation
      minim = new Minim(this);
      
      //load the song
      currentSong = "songs/" + fileChooser.getSelectedText();
      song = minim.loadFile(currentSong);
      
      //beat Detection (didnt work right)
      //beat = new BeatDetect(song.bufferSize(), song.sampleRate());
      //beat.setSensitivity(100);
      //analyse it with the fft
      
      //create the fft
      fft = new FFT(song.bufferSize(), song.sampleRate());
      //equalizer = new Equalizer(100);
    }

    //plays the song (at a position (beginning = 0))
    song.play(songPosition);
    
    //sets playing to true so we are now in the play mode
    playing = true;
    
  }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------
  public void draw() {
    
    //if we are in the play mode
    if(playing){
      
      //each frame calc the fftValues new
      calcFFTValues();
      
      //glichiness: only when the high bands of the spectrum reaches the glitchFactor it should reset the window, else it draws over the window
      if(scoreHi < glitchFactor){   //standart 250
            background(0);
      }
      
      
      //---draws the field each frame---
      //calculates the offsets to place the filed in the middle
      int offsetX = (int)(width/2 - oneFieldSize - boxSize);
      int offsetY = (int)(height/2 - oneFieldSize - boxSize);

     //if we are in the 3D or cube mode then the whole scene should move backwards to get everything in the view
     if(threeD || specialMode3){
        translate(0,0,-500);
     }
    
      //move to the middle of the view for the rotation and 3D
      pushMatrix();
      translate(width / 2, height/2); 
      
      //to get the 3D mode working
      if(threeD){
        rotateX((int)(90 * Math.PI/180f));
        translate(0,0,20);
      }
      
      //rotate the whole field that will be later drawn
      rotateZ(wholeFieldRotation);
      
      //go back to the corder of the field
      translate(-width / 2, -height/2);
      
      //to center the field
      translate(offsetX,offsetY,0);
      //translate(0,0,z - 100);
      
      //when we are in special mode 3 then a cube with the fields should be drawn
      if(specialMode3) {     
         drawCube();
      }
      
      //else just a cube should be drawn
      else {
        drawWholeField();
      }
      
      popMatrix();
      
      //---   ---
      
      //calculates the livings cells to determine if we need to create new cells
      int livingCells = field.getLivingCells();
      
     //if the new new score is higher than the old score of the low bands of the spectrum (kick or bass is "crawling") 
     //then the game of life field should jump one generation further
      if(scoreLow > oldScoreLow){
        game.nextGeneration();
      }
      
      //if there is a significant bass and the bass is increasing (kick) then the cells should get bigger
      if(scoreLow > bassTolerant && scoreLow > oldScoreLow){
        field.setBoxSize(field.boxSize + 1);
        //equalizer.setCircleSize(equalizer.circleSize + 0.2);
      }
      
      //if the base gets lesser again (end of kick) the cells should go to its normal size back again
      if(scoreLow < oldScoreLow && scoreHi > 20){
        field.setBoxSize(10);
        //equalizer.setCircleSize(1);
      }
      
      
      //if there is a mid score it should rotate slowly with the amount of the mid score (decreased by 100000 because else...)
      //remove the 100000f for funny effect
      if(scoreMid > 5){
        wholeFieldRotation += (((scoreMid)/100000f) * direction);
        /* z += scoreMid/100f;
        if(z > 500){
          z = -100;
        }
        
         z2 += scoreMid/150f;
        if(z2 > 500){
          z2 = -200;
        }
        */
      }
      
      //if there is a high mid (melody) every about 130 beats a center object (glider) will be spawned
      //will not work on specialMode (the first one)
      if(scoreMid > bassTolerant/2f && cooldown && !specialMode) {
        field.setCenterObject();
        cooldown = false;
        
      }
      
      //if cooldown is false the cooldown counter will be count up and if it reaches 11 (frames) then it will be reset and the
      //cooldown will be again set to true and then again a object can be spawend
      if(!cooldown) {
          coolDownCounter++;
          if(coolDownCounter > 11) {
            cooldown = true;
            coolDownCounter = 0;
          }
      }
      
      //if there is a significant score in the high frequency bands then
      //the rotation should go counterwise or gets slowed down
      if(scoreHi > 5){
        wholeFieldRotation -= ((scoreHi)/200000f);
      }
     
      //if there are to less living cells on the field then there will be set random
      //objects on the field
      if(livingCells < 20){
        field.setRandomObject(scoreLow,scoreMid,scoreHi);
      }
      
      //every thousand frames the rules of game of life will be reverted to fresh up the field (not always the same)
      if(frameCount % 1000 == 0){ 
        game.nextGenerationRev();        //one short reverse game of life to mix it throught
      }
      
      //if the score low is higher than the max bass, the max bass should be set to the score low
      //and on that special occastion (drop for example) also random objects will be spawnd
      if(scoreLow > maxBass){
        maxBass = scoreLow;
        field.setRandomObject(scoreLow,scoreMid,scoreHi);
        
      }
      
      //same for the hi tones but they will be reset when max Hi reaches 700
      if(scoreHi > maxHi){
        maxHi = scoreHi;
        field.setRandomObject(scoreLow,scoreMid,scoreHi);
        
        if(maxHi > 700){
          maxHi = 400;
        }
      }
      
      //if it is very silent (dark field) (silent music part) but the max bass got reached once and there are not to much cells
      //on the field then there should be set objects on the field
      //when the silent is over (dark over) then suddenly there are many cells on the field (good for drops)
      if((scoreLow < 20 && scoreMid < 20 && scoreHi < 20) && maxBass > 1005 && livingCells < 550){
        field.setCenterObject();
        field.setRandomObject(scoreLow,scoreMid,scoreHi);
      }
      
      //on the last special mode it sets the blend mode to add, when there is an increase on the mid scores and score mid and score hi are present
      //to get a blinking factor
      //else the blend mode will be reset to blend (standart)
      if(specialMode4) {
        if(scoreMid > oldScoreMid && scoreMid > 200 && scoreHi > 100) {
          blendMode(ADD);
        }
        else {blendMode(BLEND);}
      }
    }
  }
  
  //--------------------------------------------------------------------------------------------------------------------------------------
  
  //helping function to get the Mp3 files (strings) from the song folder
  public String[] readMp3s(String dir){
    //whole file names of directory
    String names[] = new String[0];
    //result array that will be sent back
    String[] result = null;
    int counter = 0;
    
      //selects the directory
      File directory = new File(sketchPath(dir));
      if (directory.isDirectory()) {
            //lists all the files of the directory and saves it in the names array
            names = directory.list();
      }
    
    //gets how much space is needed for hte result array
    //counts the file names that ends with .mp3 or .wav or .aiff or .aif .au
    for(int i = 0; i < names.length; i++){
      if(names[i].endsWith(".mp3") || names[i].endsWith(".wav") ||names[i].endsWith(".aif") || names[i].endsWith(".aiff") || names[i].endsWith(".au")){
        counter++;
      }
    }
    
    //creates the result array
    result = new String[counter];
    counter = 0;
    
    //saves the file names with the supported file extensiosn in the array
    for(int i = 0; i < names.length; i++){
        if(names[i].endsWith(".mp3") || names[i].endsWith(".wav") ||names[i].endsWith(".aif") || names[i].endsWith(".aiff") || names[i].endsWith(".au")){
          result[counter++] = names[i];
      }
    }
    
    //returns the result
     return result;
  }
  
  //draws a cube for the special mode 4 instead of one big field
  //with rotation and translation
  public void drawCube() {
    // beat.detect(song.mix);
        translate(oneFieldSize, oneFieldSize, -oneFieldSize);
        rotateY(wholeFieldRotation);
        translate(-oneFieldSize, -oneFieldSize, oneFieldSize);
        
        pushMatrix();
        rotateX(radians(-90));
        drawWholeField();
        popMatrix();

         pushMatrix();
         translate(0,oneFieldSize * 2,-oneFieldSize * 2);
        rotateX(radians(90));
        drawWholeField();
        popMatrix();
        

        pushMatrix();
        translate(oneFieldSize * 2 , oneFieldSize * 2, 0);
         scale(-1);
         drawWholeField();
        popMatrix();
        
         pushMatrix();
        translate(0,0, -oneFieldSize * 2 );
         drawWholeField();
        popMatrix();
        
        pushMatrix();
        rotateY(radians(90));
        drawWholeField();
        popMatrix();
        
        pushMatrix();
        translate(oneFieldSize * 2, 0 , -oneFieldSize * 2);
        rotateY(radians(-90));
        drawWholeField();
        popMatrix();
  }
  
  //draws 4 small game of life fields so that they look symmetrical
  public void drawWholeField(){
    int offset = oneFieldSize;
   pushMatrix();
    translate(0,0,-50);    
    drawFieldRotated(180);
     
     
     translate(offset,0,0);     
     drawFieldRotated(270);
    
    translate(0,offset,0);
    drawFieldRotated(0);
    
    /*
    if(threeD){
        equalizer.draw();
      }
*/
    translate(-offset,0,0);
    drawFieldRotated(90);
 
   popMatrix();
  
  }
  
  //helping function to draw the field rotated
  private void drawFieldRotated(int angle){
    pushMatrix();
    //sets the pivot to the middle of the field
     translate(oneFieldSize / 2, oneFieldSize / 2, 0);
     //rotates the field
      rotate(radians(angle));
      //resets the pivot to the beginning of the field
      translate(-oneFieldSize / 2, -oneFieldSize / 2, 0);
      //draws the field
      field.drawField();
      popMatrix();
  } //<>//
  
  //thanks to the code @see https://github.com/samuellapointe/ProcessingCubes that helped strongly for the music analysis
  private void calcFFTValues(){
  //calcs the forward fft for the song part
  fft.forward(song.mix);
  
  //sets the newscores to the old scores and reset the new ones
  oldScoreLow = scoreLow;
  oldScoreMid = scoreMid;
  oldScoreHi = scoreHi;
  
  //resets the values
  scoreLow = 0;
  scoreMid = 0;
  scoreHi = 0;
 

  //calc the new scores from the code
  //gets the low bands (sums up the low bands)
  for(int i = 0; i < fft.specSize() * 0.04f; i++)
  {
    scoreLow += fft.getBand(i);
  }
  //gets the mid bands (sums up the mid bands)
  for(int i = (int)(fft.specSize() * 0.04f); i < fft.specSize() * 0.125f; i++)
  {
    scoreMid += fft.getBand(i);
  }
  //sums up the hi bands (irgnors the bands that are too high and not really recognized in the mix)
  for(int i = (int)(fft.specSize() * 0.125f); i < fft.specSize() * 0.20f; i++)
  {
    scoreHi += fft.getBand(i);
  }
  
  //dampen the reduction when the old score is higher than the new score (with a decrease rate)
  if (oldScoreLow > scoreLow) {
    scoreLow = oldScoreLow - scoreDecreaseRate;
  }
  
  if (oldScoreMid > scoreMid) {
    scoreMid = oldScoreMid - scoreDecreaseRate;
  }
  
  if (oldScoreHi > scoreHi) {
    scoreHi = oldScoreHi - scoreDecreaseRate;
  }
  
}

//--------------------Interaction elements---------------------------
public void keyPressed() {
  if(keyCode == 80 && playing){
    //get the current position to start from there
    songPosition = song.position();
    //pauses the song
    song.pause();
    //sets playing to false
    playing = false;
    //resets the blend mode
    blendMode(BLEND);
    //earses the printed cubes
    background(0);
    //creates the gui again
    createGUI();
    
    //sets the values for the menu with the acual set values
    soundReactionSpeedSlider.setValue(scoreDecreaseRate);
    glitchFactorSlider.setValue(500 - glitchFactor);
    bassTol.setValue(bassTolerant);
    
    redComp.setValue(redMultiplicator);
    blueComp.setValue(blueMultiplicator);
    greenComp.setValue(greenMultiplicator);
    brightnessVal.setValue(brightness);
    
    //the current song should only be able to get chosen
    String [] currentItem = {currentSong};
    fileChooser.setItems(currentItem,0);
    //redraw to make everything visible
    redraw();
  }
  
  //FIELD MODES
  
  //pressed 1 for mode 1
  if(keyCode == 49) {
    boxSize = 10;
    specialMode3 = false;
    specialMode = false;
    specialMode2 = false;
    threeD = false;
  }
  
  //pressed 2 for mode 2
  else if(keyCode == 50){
    boxSize = 10;
    specialMode3 = false;
    specialMode = true;
    specialMode2 = false;
    threeD = false;
  }
  
  //pressed 3 for mode 3
  else if(keyCode == 51) {
    boxSize = 10;
    specialMode3 = false;
    specialMode2 = true;
    specialMode = false;
    threeD = false;
  }
  
  //pressed 4 for mode 4 (threeD mode)
  else if(keyCode == 52) {
    boxSize = 1;
    specialMode3 = false;
    threeD = true;
    specialMode2 = false;
    specialMode = false;
  }
  
  //pressed 5 for mode 5 (cube mode)
  else if(keyCode == 53) {
    specialMode3 = true;
    threeD = false;
    specialMode2 = false;
    specialMode = false;
  }
  
  
   //COLOR (BLEND) MODES
  
  //pressed a for normal blend mode
  else if(keyCode == 65) {
    specialMode4 = false;
    blendMode(BLEND);
    blendAddColorDecreaser = 1;
  }
  
  //pressed s for add blend mode (glowing)
  else if(keyCode == 83) {
    if(playing) {
      specialMode4 = false;
      blendMode(ADD);
      blendAddColorDecreaser = 0.5f;
    }
  }
  
  //pressed d for lightest blend mode
  else if(keyCode == 68) {
    if(playing) {
      specialMode4 = false;
      blendMode(LIGHTEST);
      blendAddColorDecreaser = 1;
    }
  }
  
  //pressed f for exclusion blend mode
  else if(keyCode == 70) {
    if(playing) {
      specialMode4 = false;
      blendMode(EXCLUSION);
      blendAddColorDecreaser = 1;
    }
  }
  
  
  //pressed g for blinking mode
  else if(keyCode == 71) {
    specialMode4 = true;
    blendAddColorDecreaser = 1;
  }
}
  
  
  
  
//just an regular old Equalizer (isnt used in the visualisation)
class Equalizer {
    int radius;
    float bands[];
    float bandsOld[];
    float envelope[];
    float circleSize;
  
   Equalizer(int radius) {
     this.radius = radius;
     bands = new float[((int)(fft.specSize() - 200)/2)];
     bandsOld = new float[((int)(fft.specSize() - 200)/2)];
     circleSize = 1;
   }
  
  
  public void draw() {
    int bandValue = 0;
    
    int numPoints = (int)((fft.specSize() - 200) / 4);
    
      envelope = new float[numPoints];
    for(int i = 0; i < envelope.length; i++) {
        envelope[i] = bezierPoint(
        0.05f,
        0.1f,
        0.5f,
        2,
        map(i,0,envelope.length - 1 , 0,1));
  }
    
    for(int i=0;i<numPoints;i++) {
        bandsOld[i] = bands[i]/4f;
     }
    
    int j = 0;
    
    for(int i=0;i < numPoints * 2;i += 2) {
        bands[j] = fft.getBand(i);
        j++;
     }
     
    
    float angle=TWO_PI/(float)numPoints;
    stroke((4.5f + scoreLow/4 + 10) * redMultiplicator + brightness, (scoreMid/4) * blueMultiplicator + brightness, (scoreHi/2 ) * greenMultiplicator + brightness);
    fill(0);
    pushMatrix();
    translate(0,0,300);
    
          
     scale(circleSize);
    for(int i=0;i<numPoints;i++) {
         //line(radius*sin(angle*i),radius*cos(angle*i),0,radius*sin(angle*i),radius*cos(angle*i), ((bands[i] - bandsOld[i]) * envelope[i]) * 3);
         pushMatrix();
            translate(radius*sin(angle*i),radius*cos(angle*i),0);
            //box(1);
             translate(0,0, -((bands[i] - bandsOld[i]) * envelope[i]) * 3);
             box(5);
          popMatrix();            
     }
     
     pushMatrix();
     for(int i=0;i<numPoints;i++) {
        // line(radius*sin(angle*i) / 2f,radius*cos(angle*i) / 2f,0,radius*sin(angle*i)/2f,radius*cos(angle*i)/2f, ((bands[i] - bandsOld[i]) * envelope[i]) * 5);
       pushMatrix();
            translate(radius*sin(angle*i) / 2f,radius*cos(angle*i) / 2f,0);
            //box(1);
             translate(0,0, -((bands[i] - bandsOld[i]) * envelope[i]) * 5);
             box(5);
          popMatrix();
     }
     popMatrix();
     popMatrix();
     stroke(0);
     
     /*
      for(int i=0;i<numPoints;i++) {
         bandValue += (bands[i] - bandsOld[i]) * envelope[i];
     }
     
     pushMatrix();
     println(bandValue / 40f);
     scale(1,1,bandValue / 40f);
     box(30);
     popMatrix();
     */
  }
  
  
  public void setCircleSize(float circleSize){
    this.circleSize = circleSize;
  }
  
}
public class Field extends Thread{
  int pHeight;     //pixel Height fo the field
  int pWidth;      //pixel Width of the field
  int fWidth;      //width of the field countet with cells
  int fHeight;    //height of the field counted with cells
  int[][] field;        //game Field array
  int gameOfLifeObjects[][][];    //objects that can be set on the field
  int boxSize;                  //size of one cell in px
  int towerDecVal;              //decrease the tower up on threeD mode so that there arent too much cells on the field

  //constructor of the field
  public Field(int pHeight, int pWidth, int boxSize) {
    //sets the pixel with height and box size
    this.pHeight = pHeight;
    this.pWidth = pWidth;
    this.boxSize = boxSize;

    //calculates the field with and height
    this.fWidth = (int)pWidth / boxSize;
    this.fHeight = (int)pHeight / boxSize;
    
    //sets the tower decrease value to 4 (every 4 generations of the cells it builds up one block)
    towerDecVal = 4;

    //sets up the field
    field = new int[fWidth][fHeight];

     //creates the start setup for the game of life (cells)
    createGameOfLifeObjects();
  }
  
  //draws the cells field
 public void drawField() {
  
     //when we are in specialMode 3 (cube mode) than the box size should be set to 17 instead of 15 for performance reasons (less cells to display)
     //if there is a kick (scoreLow > oldScoreLow && scoreMid > bassTolerant/3.5f + 50 && oldScoreLow > bassTolerant/3.5f + 100) then is should scale up for a short moment
     //(detection of kick works differnetly on the other bodes
    if(specialMode3) {
        boxSize = 17;
        if(scoreLow > oldScoreLow && scoreMid > bassTolerant/3.5f + 50 && oldScoreLow > bassTolerant/3.5f + 100){
          scale(1.1f);
        }
    }

    //calculates a global score for the color calculation
    float scoreGlobal = 0.86f*scoreLow + 0.8f*scoreMid + 1*scoreHi;
    //color selection and weight depending on the music    

    //when set threeD the blocks should grow upwards (growDirection)
    short growDirection;
    if (threeD) {
      growDirection = -1;
    } else {
      growDirection = 1;
    } 
    
    //the drawing process:
    //iterates over the whole field
    for (int j = 0; j < pHeight; j += boxSize) {
      for (int i = 0; i < pWidth; i += boxSize) {
        //sets the position (with offset) where to draw the cell
        pushMatrix();
        translate(i + 20, j + 20);
        //if on this place there is a cell alive (then draw it)
        if (field[i / boxSize][j / boxSize] > 0) {
          //calculates the current box size (gets to the mid smaller)
          int currentBoxSize = (int)(boxSize - (boxSize * 1.5f - (((int)pWidth / boxSize) - i)/100f + j/100f + i*j/50000f));
          
          //if we are in the first special mode (obelisk)
          //draws the cell more far away draws the cell sets the another cell even more away and make it bigger (only for cells more outside
          if(specialMode){
            if(i > 20 && j > 20 ) {
              translate(0,0,-500);
              box(currentBoxSize);
              translate(0,0,-4500);
              currentBoxSize = 15 - i;
            }
          }
          
          //on special mode 2 the box sizes also will be calculated differently
          if(specialMode2){
            if(i > 30 && j > 30) {
              currentBoxSize = 30 - i/10;
            }
          }
          
          //sets the color for the cell
          fill(getColor(j / boxSize, i / boxSize, (int)scoreGlobal));
          
          //draws the cell (as a box)
          box(currentBoxSize);

          //towers up the cell depending of the cells generation (decreased with the tower Dec value) and makes the drawn box every tower build smaller
          pushMatrix();
          for (int k= 0; k < (int)(field[i / boxSize][j / boxSize]/towerDecVal); k++) {
            translate(0, 0, growDirection * currentBoxSize);
            box(currentBoxSize + k);
          }
          popMatrix();
          
          //on the 3d (skyscraper) mode it should also tower the oder direction
          if (threeD) {
            for (int k= 0; k < (int)(field[i / boxSize][j / boxSize]/towerDecVal); k++) {
              translate(0, 0, -growDirection * currentBoxSize);
              box(currentBoxSize + k/2);
            }
          }
        }
        popMatrix();
      }
    }
  }


  //overrides the field array with another field array (used for the generations jumps)
  public void overrideFieldWith(int[][] bufferField) {
    for (int i = 0; i < fHeight; i++)
    {
      for (int j = 0; j < fWidth; j++)
      {
        field[j][i] = bufferField[j][i];
      }
    }
  }

  //copies the current field and returns it
  public int[][] copyField() {
    int[][] bufferField = new int[fWidth][fHeight];

    for (int i = 0; i < fHeight; i++)
    {
      for (int j = 0; j < fWidth; j++)
      {
        bufferField[j][i] = field[j][i];
      }
    }

    return bufferField;
  }

  //set the first object (the first cells when the visualisation is started
  public void setFirstObject(int x, int y) {                                //may not work (fehlerbehandlung)

    for (int i = 0; i < field.length - 6; i++)
    {
      field[field.length - 6 + x][i + y] = 1;
    }

    field[2 + x][0 + y] = 1;
    field[2 + x][4 + y] = 1;

    field[(int)field.length/2][(int)field.length/2] = 1;
    field[(int)field.length/2 + 1][(int)field.length/2] = 1;
    field[(int)field.length/2 + 1][(int)field.length/2 + 1] = 1;
    field[(int)field.length/2][(int)field.length/2 + 1] = 1;
    field[(int)field.length/2 + 2][(int)field.length/2 + 1] = 1;
    /*
    field[1 + x][0 + y] = 1;
     field[0 + x][1 + y] = 1;
     field[0 + x][2 + y] = 1;
     field[0 + x][3 + y] = 1;
     field[1 + x][3 + y] = 1;
     field[2 + x][3 + y] = 1;
     field[3 + x][3 + y] = 1;
     field[4 + x][0 + y] = 1;
     field[4 + x][2 + y] = 1;
     */

    for (int i = x; i < fWidth - 3; i++) {
      field[i + 1][y + 5] = 1;
    }
  }

  //sets random objects ont he field
  public void setRandomObject(float low, float mid, float hi) {
    int objectID = Math.abs((int)hi % 9);
    int x = Math.abs((int)low % 40);
    int y = Math.abs((int)mid % 40);

    //check if the length of the arrays are ok
    if (field.length > x + gameOfLifeObjects[objectID].length && field[0].length > y + gameOfLifeObjects[objectID][0].length && field.length + gameOfLifeObjects[objectID].length > x && field[0].length + gameOfLifeObjects[objectID][0].length > y) {   
      for (int i = 0; i < gameOfLifeObjects[objectID].length; i++) {
        for (int j = 0; j < gameOfLifeObjects[objectID][i].length; j++) {
          field[i + x][j + y] = gameOfLifeObjects[objectID][i][j];          //sets the objet
        }
      }
    }
    
    //sets another object
    objectID = Math.abs(objectID - 1);
    x = y;
    y = Math.abs((int)low % 40);

    if (field.length > x + gameOfLifeObjects[objectID].length && field[0].length > y + gameOfLifeObjects[objectID][0].length && field.length + gameOfLifeObjects[objectID].length > x && field[0].length + gameOfLifeObjects[objectID][0].length > y) {   
      for (int i = 0; i < gameOfLifeObjects[objectID].length; i++) {
        for (int j = 0; j < gameOfLifeObjects[objectID][i].length; j++) {
          field[i + x][j + y] = gameOfLifeObjects[objectID][i][j];
        }
      }
    }
  }
  
  //sets an object on the center with a higher start generation than one
  public void setCenterObject(){
    /*
    field[0][0] = 40;
    field[1][0] = 25;
    field[0][1] = 25;
    field[2][1] = 40;
    field[2][2] = 25;
    field[2][3] = 40;
    field[3][3] = 10;
    
    field[5][5] = 20;
    field[5][6] = 20;
    field[6][5] = 20;
    field[6][6] = 20;
    */
    /*
    field[0][2] = 40;
    field[1][3] = 40;
    field[1][4] = 40;
    field[2][1] = 40;
    field[2][2] = 40;
    field[3][0] = 40;
    field[3][1] = 40;
    field[3][2] = 40;
    field[4][3] = 40;
    field[4][4] = 40;
    field[5][1] = 40;
    field[5][2] = 40;
    field[6][4] = 40;
    */
    
    //this is a glider
    field[0][2] = 40;
    field[1][0] = 40;
    field[1][2] = 40;
    field[2][1] = 40;
    field[2][2] = 40;
    
    /*for(int i = 0; i < 2; i++) {
      for(int j = 0; j < 2; j++) {
        field[i][j] = i * 3;
      }
    }
    
    for(int i = 4; i < 7; i++) {
      for(int j = 4; j < 7; j++) {
        field[i][j] = j;
      }
    }*/
  }

  //creates some interesting cell objects for the game of life objects array (@see http://conwaylife.com/w/index.php?title=Main_Page)
  private void createGameOfLifeObjects() {
    gameOfLifeObjects = new int[10][][];        //10 objects

    //objects are rotated 90 degreess
    
    
    gameOfLifeObjects[0] = new int[][]{
      {1, 1}, 
      {0, 1}, 
      {1, 0}, 
      {1, 1}};

    gameOfLifeObjects[1] = new int[][]{
      {1, 1, 1, 1, 1}, 
      {0, 0, 0, 0, 0}, 
      {1, 0, 0, 0, 1}, 
      {0, 0, 0, 0, 0}, 
      {1, 1, 1, 1, 1}};

    gameOfLifeObjects[2] = new int[][]{
      {0, 1, 1, 1}, 
      {1, 0, 0, 1}, 
      {0, 0, 0, 1}, 
      {0, 0, 0, 1}, 
      {0, 0, 1, 0}};

    gameOfLifeObjects[3] = new int[][]{
      {0, 1, 0}, 
      {1, 1, 1}, 
      {1, 0, 0}};

    gameOfLifeObjects[4] = new int[][]{
      {0, 1, 0}, 
      {1, 1, 1}, 
      {1, 0, 1}, 
      {1, 0, 0}};

    gameOfLifeObjects[5] = new int[][]{
      {1, 1, 1, 0}, 
      {0, 1, 0, 0}, 
      {0, 1, 1, 1}};


    gameOfLifeObjects[6] = new int[][]{
      {0, 1, 0, 0, 0, 1}, 
      {1, 1, 0, 1, 1, 0}, 
      {1, 1, 0, 0, 1, 0}};

    gameOfLifeObjects[7] = new int[][]{
      {0, 1}, 
      {1, 1}, 
      {1, 1}, 
      {1, 0}};

    gameOfLifeObjects[8] = new int[][]{
      {1, 1, 0, 0}, 
      {1, 0, 0, 0}, 
      {0, 1, 1, 1}, 
      {0, 0, 0, 1}};

    gameOfLifeObjects[9] = new int[][]{
      {1, 1, 0}, 
      {0, 1, 1}, 
      {1, 1, 0}, 
      {1, 0, 0}};
  }

  //calculates the color for the cell
  private int getColor(int i, int j, int scoreGlobal) {
    //if the cell is dead the color is black
    if (field[j][i] == 0) {
      return color(0,0,0);     
      //else the red color is indicated the score low of the music and the generation of the cell, the green part is the score mid and the blue part is the score hi (brightness and *Mulitplicator are tweak values)
    } else return color((field[i][j] * 4.5f + scoreLow/4 + 10) * redMultiplicator + brightness * blendAddColorDecreaser, (scoreMid/4 +scoreGlobal/15 -20) * blueMultiplicator + brightness * blendAddColorDecreaser, (scoreHi/2 + field[j][i] + scoreGlobal/10 - 20 + 3)* blendAddColorDecreaser * greenMultiplicator + brightness * blendAddColorDecreaser);
  }

  //counts the living cells on the field and returns the number of them
  public int getLivingCells() {
    int livingCells = 0;

    for (int i = 0; i < fHeight; i++)
    {
      for (int j = 0; j < fWidth; j++)
      {
        if (field[j][i] > 0) {
          livingCells++;
        }
      }
    }

    return livingCells;
  }

  //sets the box size of the cells on the field
  public void setBoxSize(int boxSize) {
    this.boxSize = boxSize;
  }


  //for debugging reasons (prints the field array)
  public void print() {
    for (int i = 0; i < fHeight; i++)
    {
      for (int j = 0; j < fWidth; j++)
      {
        System.out.print(field[j][i]);
      }
      System.out.println();
    }

    System.out.println();

    System.out.println();
  }
}
public class GameOfLife extends Thread{
  
  //consturctor
  public GameOfLife(){}
  
  //goes to next generation of the field
  public void nextGeneration(){
    //creates a bufferField with the current Field
    int[][] bufferField = field.copyField();
    
    //iterates over the field
     for (int i = 0; i < field.fHeight; i++)
    {
      for (int j = 0; j < field.fWidth; j++)
      {  
        //for every cell calculates the neighbours
        int neighbors = calcNeighbours(j, i);
                
        
        //core rules (heart of game of life)
        //if the cell is dead (no cell here) and it has 3 naighbours it gets alive (generation is 1)
         if (neighbors == 3 && field.field[j][i] == 0) {
            bufferField[j][i] = 1;
         }
      
      //if the cell is alive but there are less than 2 neighbours it dies due to underpopulation (generation is 0)
        else if (neighbors < 2 && field.field[j][i] > 0) {
          bufferField[j][i] = 0;
        }
      
      //if there are more than 3 cells on the filed and the cell is alive it dies due to overpopulation (generation is 0)
        else if (neighbors > 3 && field.field[j][i] > 0) {
          bufferField[j][i] = 0;
        }
      
      //on every other case when the cell is alive it ages
        else if (field.field[j][i] > 0)
        {
          bufferField[j][i]++;
        } 
        
        //if the cell is too old (higher than max generation) it also dies
        if(bufferField[j][i] > maxGeneration){
          bufferField[j][i] = 0;
        }
        
      }
    }
    
    //sets the buffer field to the current field
    field.overrideFieldWith(bufferField);
    
  }
  
  
  //same as above but with reversed core rules for the cells
   public void nextGenerationRev(){
    int[][] bufferField = field.copyField();
    
     for (int i = 0; i < field.fHeight; i++)
    {
      for (int j = 0; j < field.fWidth; j++)
      {
        int neighbors = calcNeighbours(j, i);
                
        
        //core rules
         if (neighbors == 3 && field.field[j][i] > 0) {
            bufferField[j][i] = 1;
         }
      
        else if (neighbors < 2 && field.field[j][i] == 0) {
          bufferField[j][i] = 0;
        }
      
        else if (neighbors > 3 && field.field[j][i] == 0) {
          bufferField[j][i] = 0;
        }
      
        else if (field.field[j][i] == 0)
        {
          bufferField[j][i]++;
        } 
        
        
      }
    }
    
    field.overrideFieldWith(bufferField);
  }
  
  
  //calculates the naeighbours for the cells
  private int calcNeighbours(int j, int i){
    int counter = 0;
      //w = west
    //o = east
    //s = south
    //n = north
    int n = i - 1;
    int s = i + 1;
    int o = j + 1;
    int w = j - 1;
       
    //gets the widht and height
    int fieldWidth = field.field[0].length;
    int fieldHeight = field.field.length;
    
    
    //border behavior
    //when the neigbour cell is outside of the field it comes back in on the other side
    if (o == fieldWidth) {
      o = 0;
    }

    if (w == -1) {
      w = fieldWidth - 1;
    }

    if (n == -1) {
      n = fieldHeight - 1;
    }

    if (s == fieldHeight) {
      s = 0;
    }


    //checks the neighbour cells (all 8 neighbour cells)       
    if (field.field[w][i] > 0) {
      counter++;
    }

    if (field.field[o][i] > 0) {
      counter++;
    }
    
    if (field.field[j][s] > 0) {
      counter++;
    }

    if (field.field[j][n] > 0) {
      counter++;
    }

    if (field.field[w][n] > 0) {
      counter++;
    }

    if (field.field[o][n] > 0) {
      counter++;
    }

    if (field.field[o][s] > 0) {
      counter++;
    }

    if (field.field[w][s] > 0) {
      counter++;
    }
    
    //returns the number of the neighbours
    return counter;
  }
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

public void panel1_Click1(GPanel source, GEvent event) { //_CODE_:panel1:777488:
  background(0);
} //_CODE_:panel1:777488:

public void playButton_click1(GButton source, GEvent event) { //_CODE_:playButton:628660:
  setupGameOfLife();
  scoreDecreaseRate = soundReactionSpeedSlider.getValueI();
  glitchFactor = 500 - glitchFactorSlider.getValueI();
  bassTolerant = bassTol.getValueI();
  redMultiplicator = redComp.getValueF();
  blueMultiplicator = blueComp.getValueF();
  greenMultiplicator = greenComp.getValueF();
  brightness = brightnessVal.getValueF();
  fileChooser.dispose();
  glitchFactorLabel.dispose();
  glitchFactorSlider.dispose();
  SoundReactionSpeedLabel.dispose();
  soundReactionSpeedSlider.dispose();
  playButton.dispose();
  redComponent.dispose(); 
  blueComponent.dispose(); 
  greenComponent.dispose(); 
  redComp.dispose(); 
  blueComp.dispose(); 
  greenComp.dispose(); 
  bassTolerance.dispose(); 
  bassTol.dispose(); 
  brightnessLable.dispose(); 
  brightnessVal.dispose(); 
  panel1.dispose();
} //_CODE_:playButton:628660:

public void dropList1_click1(GDropList source, GEvent event) { //_CODE_:fileChooser:631830:
  background(0);
  redraw();
} //_CODE_:fileChooser:631830:

public void soundRectionSpeedSlide_change1(GSlider source, GEvent event) { //_CODE_:soundReactionSpeedSlider:279956:
} //_CODE_:soundReactionSpeedSlider:279956:

public void slider1_change1(GSlider source, GEvent event) { //_CODE_:glitchFactorSlider:607805:
  //println("slider1 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:glitchFactorSlider:607805:

public void redComp_change2(GSlider source, GEvent event) { //_CODE_:redComp:483627:
  //println("redComp - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:redComp:483627:

public void blueComp_change2(GSlider source, GEvent event) { //_CODE_:blueComp:323978:
  //println("blueComp - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:blueComp:323978:

public void greenComp_change2(GSlider source, GEvent event) { //_CODE_:greenComp:536966:
  //println("greenComp - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:greenComp:536966:

public void bassTol_change2(GSlider source, GEvent event) { //_CODE_:bassTol:852677:
  //println("bassTol - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:bassTol:852677:

public void brightness_change2(GSlider source, GEvent event) { //_CODE_:brightnessVal:917494:
  //println("slider1 - GSlider >> GEvent." + event + " @ " + millis());
} //_CODE_:brightnessVal:917494:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.GOLD_SCHEME);
  G4P.setCursor(ARROW);
  surface.setTitle("Sketch Window");
  panel1 = new GPanel(this, 750, 330, 330, 436, "Game of Life Music");
  panel1.setText("Game of Life Music");
  panel1.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  panel1.setOpaque(true);
  panel1.addEventHandler(this, "panel1_Click1");
  playButton = new GButton(this, 118, 379, 80, 30);
  playButton.setText("Play");
  playButton.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  playButton.addEventHandler(this, "playButton_click1");
  fileChooser = new GDropList(this, 18, 39, 285, 120, 5);
  fileChooser.setItems(loadStrings("list_631830"), 0);
  fileChooser.setLocalColorScheme(GCScheme.PURPLE_SCHEME);
  fileChooser.addEventHandler(this, "dropList1_click1");
  soundReactionSpeedSlider = new GSlider(this, 160, 80, 100, 40, 10.0f);
  soundReactionSpeedSlider.setLimits(28, 5, 50);
  soundReactionSpeedSlider.setNumberFormat(G4P.INTEGER, 0);
  soundReactionSpeedSlider.setLocalColorScheme(GCScheme.BLUE_SCHEME);
  soundReactionSpeedSlider.setOpaque(false);
  soundReactionSpeedSlider.addEventHandler(this, "soundRectionSpeedSlide_change1");
  SoundReactionSpeedLabel = new GLabel(this, 11, 92, 142, 20);
  SoundReactionSpeedLabel.setText("SoundReactionSpeed");
  SoundReactionSpeedLabel.setLocalColorScheme(GCScheme.PURPLE_SCHEME);
  SoundReactionSpeedLabel.setOpaque(false);
  glitchFactorSlider = new GSlider(this, 160, 120, 100, 40, 10.0f);
  glitchFactorSlider.setLimits(250, 0, 400);
  glitchFactorSlider.setNumberFormat(G4P.INTEGER, 0);
  glitchFactorSlider.setLocalColorScheme(GCScheme.BLUE_SCHEME);
  glitchFactorSlider.setOpaque(false);
  glitchFactorSlider.addEventHandler(this, "slider1_change1");
  glitchFactorLabel = new GLabel(this, 12, 130, 80, 20);
  glitchFactorLabel.setText("Glitch Factor");
  glitchFactorLabel.setLocalColorScheme(GCScheme.PURPLE_SCHEME);
  glitchFactorLabel.setOpaque(false);
  redComponent = new GLabel(this, 12, 244, 108, 20);
  redComponent.setText("red component");
  redComponent.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  redComponent.setOpaque(false);
  blueComponent = new GLabel(this, 11, 271, 108, 20);
  blueComponent.setText("blue component");
  blueComponent.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  blueComponent.setOpaque(false);
  greenComponent = new GLabel(this, 11, 299, 108, 20);
  greenComponent.setText("green component");
  greenComponent.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  greenComponent.setOpaque(false);
  redComp = new GSlider(this, 160, 230, 100, 40, 10.0f);
  redComp.setLimits(1.0f, 0.5f, 2.0f);
  redComp.setNumberFormat(G4P.DECIMAL, 2);
  redComp.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  redComp.setOpaque(false);
  redComp.addEventHandler(this, "redComp_change2");
  blueComp = new GSlider(this, 160, 290, 100, 40, 10.0f);
  blueComp.setLimits(1.0f, 0.5f, 2.0f);
  blueComp.setNumberFormat(G4P.DECIMAL, 2);
  blueComp.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  blueComp.setOpaque(false);
  blueComp.addEventHandler(this, "blueComp_change2");
  greenComp = new GSlider(this, 160, 260, 100, 40, 10.0f);
  greenComp.setLimits(1.0f, 0.5f, 2.0f);
  greenComp.setNumberFormat(G4P.BLUE_SCHEME, 2);
  greenComp.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  greenComp.setOpaque(false);
  greenComp.addEventHandler(this, "greenComp_change2");
  bassTolerance = new GLabel(this, 10, 165, 94, 20);
  bassTolerance.setText("Bass Tolerance");
  bassTolerance.setLocalColorScheme(GCScheme.PURPLE_SCHEME);
  bassTolerance.setOpaque(false);
  bassTol = new GSlider(this, 160, 160, 100, 40, 10.0f);
  bassTol.setLimits(800, 400, 1600);
  bassTol.setNumberFormat(G4P.INTEGER, 0);
  bassTol.setLocalColorScheme(GCScheme.BLUE_SCHEME);
  bassTol.setOpaque(false);
  bassTol.addEventHandler(this, "bassTol_change2");
  brightnessLable = new GLabel(this, 11, 326, 80, 20);
  brightnessLable.setText("brightness");
  brightnessLable.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  brightnessLable.setOpaque(false);
  brightnessVal = new GSlider(this, 160, 320, 100, 40, 10.0f);
  brightnessVal.setLimits(0.0f, 0.0f, 80.0f);
  brightnessVal.setNumberFormat(G4P.DECIMAL, 2);
  brightnessVal.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  brightnessVal.setOpaque(false);
  brightnessVal.addEventHandler(this, "brightness_change2");
  panel1.addControl(playButton);
  panel1.addControl(fileChooser);
  panel1.addControl(soundReactionSpeedSlider);
  panel1.addControl(SoundReactionSpeedLabel);
  panel1.addControl(glitchFactorSlider);
  panel1.addControl(glitchFactorLabel);
  panel1.addControl(redComponent);
  panel1.addControl(blueComponent);
  panel1.addControl(greenComponent);
  panel1.addControl(redComp);
  panel1.addControl(blueComp);
  panel1.addControl(greenComp);
  panel1.addControl(bassTolerance);
  panel1.addControl(bassTol);
  panel1.addControl(brightnessLable);
  panel1.addControl(brightnessVal);
}

// Variable declarations 
// autogenerated do not edit
GPanel panel1; 
GButton playButton; 
GDropList fileChooser; 
GSlider soundReactionSpeedSlider; 
GLabel SoundReactionSpeedLabel; 
GSlider glitchFactorSlider; 
GLabel glitchFactorLabel; 
GLabel redComponent; 
GLabel blueComponent; 
GLabel greenComponent; 
GSlider redComp; 
GSlider blueComp; 
GSlider greenComp; 
GLabel bassTolerance; 
GSlider bassTol; 
GLabel brightnessLable; 
GSlider brightnessVal; 
  public void settings() {  fullScreen(P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#000000", "--stop-color=#cccccc", "GameOfMusic" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
