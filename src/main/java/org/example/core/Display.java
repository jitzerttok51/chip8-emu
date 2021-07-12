package org.example.core;

public interface Display {

     int WIDTH = 64;
     int HEIGHT = 32;

     int getPixel(int x, int y);
     void setPixel(int x, int y, int p);

     default void clear() {
          for (int y=0; y<HEIGHT; y++) {
               for (int x=0; x<WIDTH; x++) {
                    setPixel(x,y, 0);
               }
          }
     }
}
