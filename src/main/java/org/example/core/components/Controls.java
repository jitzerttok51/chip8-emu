package org.example.core.components;

public interface Controls {

    boolean isKeyPressed(byte v);
    boolean isKeyNotPressed(byte v);
    byte waitForKeyPress();
}
