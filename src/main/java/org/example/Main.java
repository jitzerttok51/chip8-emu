package org.example;

import org.example.gui.controllers.LauncherController;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LauncherController::new);
    }
}
