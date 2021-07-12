package org.example.gui.panels;

import org.example.gui.controllers.LauncherController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class LauncherPanel extends JFrame {
    private final LauncherController controller;
    public LauncherPanel(LauncherController controller) {
        this.controller = controller;
        init();
    }

    private void init() {
        setTitle("Test");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        var gb = new GridBagConstraints();
        gb.insets = new Insets(20, 20, 5, 20);
        panel.add(createButton("Start Editor", controller::startEditor), gb);

        gb.gridy = 1;
        gb.insets = new Insets(5, 20, 5, 20);
        panel.add(createButton("Start Debugger", controller::startDebugger), gb);

        gb.gridy = 2;
        gb.insets = new Insets(5, 20, 20, 20);
        panel.add(createButton("Run Program", controller::runProgram), gb);

//        gb.gridy = 3;
//        gb.insets = new Insets(5, 20, 20, 20);
//        panel.add(createButton("Beep", e->Toolkit.getDefaultToolkit().beep()), gb);

        add(panel);
        pack();
        setVisible(true);
    }

    public JButton createButton(String text, ActionListener listener) {
        var button = new JButton(text);
        button.addActionListener(listener);
        //button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    public Optional<Path> chooseFile() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Load program");

        int selection = fileDialog.showSaveDialog(this);

        if (selection == JFileChooser.APPROVE_OPTION) {
            return Optional.of(Paths.get(fileDialog.getSelectedFile().toURI()));
        }
        return Optional.empty();
    }

    public void showException(Exception e) {
        JOptionPane.showMessageDialog(this, e.getLocalizedMessage());
    }

}
