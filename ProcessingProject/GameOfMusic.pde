import g4p_controls.*; //<>//
import java.io.File;
import ddf.minim.*;
import ddf.minim.analysis.*;

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
  
  //old score values to calculate the reduction (dämpfung)
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
  
  void setup() {
    //sets playing and cooldown at the beginning to false
    playing=false;
    cooldown = false;
    
    //set up the window
    //size(1920,1080,P3D);      //uncomment to switch to windows mode
    fullScreen(P3D);             //comment to turn of fullscreen
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
  
  void setupGameOfLife(){
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
  void draw() {
    
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
      int offsetX = (int)(width/2 - oneFieldSize - boxSize);1
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
  String[] readMp3s(String dir){
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
  void drawCube() {
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
  void drawWholeField(){
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
  for(int i = 0; i < fft.specSize() * 0.04; i++)
  {
    scoreLow += fft.getBand(i);
  }
  //gets the mid bands (sums up the mid bands)
  for(int i = (int)(fft.specSize() * 0.04); i < fft.specSize() * 0.125; i++)
  {
    scoreMid += fft.getBand(i);
  }
  //sums up the hi bands (irgnors the bands that are too high and not really recognized in the mix)
  for(int i = (int)(fft.specSize() * 0.125); i < fft.specSize() * 0.20; i++)
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
void keyPressed() {
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
      blendAddColorDecreaser = 0.5;
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
  
  
  
  