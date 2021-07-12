package org.example.gui.panels;

import org.example.gui.components.DebugViewComponent;
import org.example.gui.components.DisplayRegistersComponent;
import org.example.gui.components.MemoryComponent;
import org.example.gui.controllers.DebugController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class DebugPanel extends JFrame {

    private MemoryComponent memoryComponent;
    private DebugViewComponent debugView;
    private DisplayRegistersComponent registers;
    private JLabel lblLed;
    private final DebugController controller;

    public DebugPanel(DebugController controller) {
        this.controller = controller;
        init();
    }

    private void init() {
        setTitle("Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        setResizable(false);
        initMenu();

        var gb = new GridBagConstraints();
        gb.fill = GridBagConstraints.VERTICAL;

        gb.gridwidth = 2;
        add(buttons(), gb);
        gb.gridwidth = 1;

        gb.gridy = 1;
        add(debugView = new DebugViewComponent(new short[200]), gb);

        gb.gridx = 1;
        add(registers = new DisplayRegistersComponent(), gb);

        gb.gridx = 0;
        gb.gridy = 2;
        gb.gridwidth = 2;
        add(memoryComponent = new MemoryComponent(), gb);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private Component buttons() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(createButton("Start", controller::start));
        panel.add(createButton("Stop", controller::stop));
        panel.add(createButton("Step", controller::step));
        lblLed = new JLabel("RUNNING");
        lblLed.setForeground(Color.RED);
        panel.add(lblLed);
        return panel;
    }

    public JButton createButton(String text, ActionListener listener) {
        var button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    public void setMemory(byte[] bytes) {
        this.memoryComponent.setMemory(bytes);
    }

    public void setGeneralRegisters(byte[] registers, short iRegister, short pcRegister) {
        var len = Math.min(registers.length, 16);
        for(int i=0;i<len;i++) {
            this.registers.setRegisterValue(i, registers[i]);
        }
        this.registers.setIValue(iRegister);
        this.registers.setPCValue(pcRegister);
    }

    public void resetPanel() {
        setJumpToLine(-1);
        this.registers.resetRegisters();
        this.memoryComponent.resetMemory();
    }

    public void setDebugLines(short[] lines) {
        this.debugView.setRom(lines);
    }

    public void setJumpToLine(int i) {
        this.debugView.setSelectedOpcode(i);
    }

    private void initMenu() {
        var bar = new JMenuBar();

        var menu = new JMenu("File");
        var menuItem = new JMenuItem("Load Program");
        menuItem.addActionListener(controller::loadProgram);
        menu.add(menuItem);
        bar.add(menu);
        setJMenuBar(bar);
    }

    public Optional<Path> chooseFile() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Load program");
        fileDialog.setFileSelectionMode(FileDialog.LOAD);

        int selection = fileDialog.showSaveDialog(this);

        if (selection == JFileChooser.APPROVE_OPTION) {
            return Optional.of(Paths.get(fileDialog.getSelectedFile().toURI()));
        }
        return Optional.empty();
    }

    public void showException(Exception e) {
        showPopup(e.getLocalizedMessage());
    }

    public void showPopup(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void greenLight() {
        lblLed.setForeground(Color.GREEN);
    }

    public void redLight() {
        lblLed.setForeground(Color.RED);
    }
}
