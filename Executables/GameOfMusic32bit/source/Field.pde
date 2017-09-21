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
          scale(1.1);
        }
    }

    //calculates a global score for the color calculation
    float scoreGlobal = 0.86*scoreLow + 0.8*scoreMid + 1*scoreHi;
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
          int currentBoxSize = (int)(boxSize - (boxSize * 1.5 - (((int)pWidth / boxSize) - i)/100f + j/100f + i*j/50000f));
          
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
  private color getColor(int i, int j, int scoreGlobal) {
    //if the cell is dead the color is black
    if (field[j][i] == 0) {
      return color(0,0,0);     
      //else the red color is indicated the score low of the music and the generation of the cell, the green part is the score mid and the blue part is the score hi (brightness and *Mulitplicator are tweak values)
    } else return color((field[i][j] * 4.5 + scoreLow/4 + 10) * redMultiplicator + brightness * blendAddColorDecreaser, (scoreMid/4 +scoreGlobal/15 -20) * blueMultiplicator + brightness * blendAddColorDecreaser, (scoreHi/2 + field[j][i] + scoreGlobal/10 - 20 + 3)* blendAddColorDecreaser * greenMultiplicator + brightness * blendAddColorDecreaser);
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
  void print() {
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