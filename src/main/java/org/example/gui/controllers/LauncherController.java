package org.example.gui.controllers;

import org.example.core.CPU;
import org.example.cpuv2.CPUBuilder;
import org.example.gui.components.SwingDisplay;
import org.example.gui.panels.LauncherPanel;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;

public class LauncherController {

    private final LauncherPanel panel = new LauncherPanel(this);
    private EditorController editorController = null;
    private DebugController debugController = null;
    private Thread thread;

    public void startEditor(ActionEvent e) {
        if(editorController == null) {
            editorController = new EditorController();
        }
        this.panel.setVisible(false);
    }

    public void startDebugger(ActionEvent e) {
        if(debugController == null) {
            debugController = new DebugController();
        }
        this.panel.setVisible(false);
    }

    public void runProgram(ActionEvent e2) {
        var opt = panel.chooseFile();
        opt.ifPresent(path -> {
            byte[] bytes = new byte[0];
            try {
                bytes = Files.readAllBytes(path);
                run(bytes);
            } catch (IOException e) {
                panel.showException(e);
                e.printStackTrace();
            }

        });
    }

    private void run(byte[] rom) {

        thread = new Thread(()->{
            var display = new SwingDisplay(panel);
            var cpu = CPUBuilder.build(rom, display, display);
            display.exitOnClose(cpu::halt);
            cpu.run();
        });

        thread.setDaemon(true);
        thread.start();
    }
}
