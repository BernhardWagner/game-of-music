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
  soundReactionSpeedSlider = new GSlider(this, 160, 80, 100, 40, 10.0);
  soundReactionSpeedSlider.setLimits(28, 5, 50);
  soundReactionSpeedSlider.setNumberFormat(G4P.INTEGER, 0);
  soundReactionSpeedSlider.setLocalColorScheme(GCScheme.BLUE_SCHEME);
  soundReactionSpeedSlider.setOpaque(false);
  soundReactionSpeedSlider.addEventHandler(this, "soundRectionSpeedSlide_change1");
  SoundReactionSpeedLabel = new GLabel(this, 11, 92, 142, 20);
  SoundReactionSpeedLabel.setText("SoundReactionSpeed");
  SoundReactionSpeedLabel.setLocalColorScheme(GCScheme.PURPLE_SCHEME);
  SoundReactionSpeedLabel.setOpaque(false);
  glitchFactorSlider = new GSlider(this, 160, 120, 100, 40, 10.0);
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
  redComp = new GSlider(this, 160, 230, 100, 40, 10.0);
  redComp.setLimits(1.0, 0.5, 2.0);
  redComp.setNumberFormat(G4P.DECIMAL, 2);
  redComp.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  redComp.setOpaque(false);
  redComp.addEventHandler(this, "redComp_change2");
  blueComp = new GSlider(this, 160, 290, 100, 40, 10.0);
  blueComp.setLimits(1.0, 0.5, 2.0);
  blueComp.setNumberFormat(G4P.DECIMAL, 2);
  blueComp.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  blueComp.setOpaque(false);
  blueComp.addEventHandler(this, "blueComp_change2");
  greenComp = new GSlider(this, 160, 260, 100, 40, 10.0);
  greenComp.setLimits(1.0, 0.5, 2.0);
  greenComp.setNumberFormat(G4P.BLUE_SCHEME, 2);
  greenComp.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  greenComp.setOpaque(false);
  greenComp.addEventHandler(this, "greenComp_change2");
  bassTolerance = new GLabel(this, 10, 165, 94, 20);
  bassTolerance.setText("Bass Tolerance");
  bassTolerance.setLocalColorScheme(GCScheme.PURPLE_SCHEME);
  bassTolerance.setOpaque(false);
  bassTol = new GSlider(this, 160, 160, 100, 40, 10.0);
  bassTol.setLimits(800, 400, 1600);
  bassTol.setNumberFormat(G4P.INTEGER, 0);
  bassTol.setLocalColorScheme(GCScheme.BLUE_SCHEME);
  bassTol.setOpaque(false);
  bassTol.addEventHandler(this, "bassTol_change2");
  brightnessLable = new GLabel(this, 11, 326, 80, 20);
  brightnessLable.setText("brightness");
  brightnessLable.setLocalColorScheme(GCScheme.CYAN_SCHEME);
  brightnessLable.setOpaque(false);
  brightnessVal = new GSlider(this, 160, 320, 100, 40, 10.0);
  brightnessVal.setLimits(0.0, 0.0, 80.0);
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