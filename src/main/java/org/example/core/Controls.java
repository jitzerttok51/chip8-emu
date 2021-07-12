package org.example.core;

public interface Controls {

    boolean isKeyPressed(byte v);
    boolean isKeyNotPressed(byte v);
    byte waitForKeyPress();
}
