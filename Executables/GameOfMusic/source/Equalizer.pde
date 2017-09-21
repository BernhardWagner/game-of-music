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
        0.05,
        0.1,
        0.5,
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
    stroke((4.5 + scoreLow/4 + 10) * redMultiplicator + brightness, (scoreMid/4) * blueMultiplicator + brightness, (scoreHi/2 ) * greenMultiplicator + brightness);
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