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