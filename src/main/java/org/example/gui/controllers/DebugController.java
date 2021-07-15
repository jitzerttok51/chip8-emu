package org.example.gui.controllers;

import org.example.core.CPU;
import org.example.core.CPUAgent;
import org.example.core.CPUAgentContext;
import org.example.cpuv2.CPUBuilder;
import org.example.gui.components.SwingDisplay;
import org.example.gui.panels.DebugPanel;
import org.example.utils.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class DebugController {

    public final DebugPanel panel = new DebugPanel(this);
    private short[] rom = null;

    private Thread cpuThread = null;

    private AtomicBoolean stop = new AtomicBoolean(true);
    private final Object stopper = new Object();
    private enum DebugMode {
        RUNNING, STEP
    }

    private DebugMode mode = DebugMode.STEP;

    public void loadProgram(ActionEvent ae) {
        Optional<Path> opt = panel.chooseFile();
        opt.ifPresent(path -> {
            if(Files.notExists(path)) {
                panel.showPopup("Provided path does not exists");
                return;
            }

            if(Files.isDirectory(path)) {
                panel.showPopup("Provided path points to a directory");
                return;
            }

            try {
                byte[] bytes = Files.readAllBytes(path);
                rom = Utils.byteArrToShort(bytes);
                panel.setDebugLines(rom);
            } catch (IOException e) {
                panel.showException(e);
                e.printStackTrace();
            }
        });
    }

    public void start(ActionEvent e) {
        if (rom == null) {
            panel.showPopup("You must load a program first");
            return;
        }

        if (this.cpuThread != null && this.cpuThread.isAlive()) {
            panel.showPopup("Program is already running");
            return;
        }

        this.cpuThread = new Thread(this::runnable);
        this.cpuThread.start();
    }

    private void runnable() {
        stop.set(false);
        panel.setGeneralRegisters(new byte[0xF], (short) 0,(short) 0);
        var agent = new CPUDebugAgent();
        var display = new SwingDisplay(panel);
        var cpu = CPUBuilder.build(Utils.shortArrToBytes(rom), display, display);
        cpu.addAgents(agent);
        update(()->panel.setJumpToLine(0));
        update(panel::greenLight);
        cpu.run();
        display.getFrame().dispose();
    }

    public synchronized void step(ActionEvent e) {
        synchronized (stopper) {
            stopper.notify();
        }
    }

    public void stop(ActionEvent e) {
        stop.set(true);
        step(e);
    }

    private void update(Runnable r) {
        SwingUtilities.invokeLater(r);
    }

    private class CPUDebugAgent implements CPUAgent {

        @Override
        public void postCycle(CPUAgentContext ctx) {
            if(mode == DebugMode.STEP) {
                update(()->panel.setMemory(ctx.getMemorySnapshot()));
                setRegisters(ctx);
                var lineNumber = (ctx.getPCRegisterValue() - 0x200) / 2;
                update(()->panel.setJumpToLine(lineNumber));

                synchronized (stopper) {
                    try {
                        stopper.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        @Override
        public boolean isHalt() {
            if(stop.get()) {
                update(panel::resetPanel);
                update(panel::redLight);
            }
            return stop.get();
        }
    }

    private void setRegisters(CPUAgentContext ctx) {
        var regs = new byte[0x10];
        for (int i=0; i<0x10; i++) {
            regs[i] = ctx.getRegisterValue(i);
        }
        update(()->panel.setGeneralRegisters(regs, ctx.getIRegisterValue(), ctx.getPCRegisterValue()));
    }
}
