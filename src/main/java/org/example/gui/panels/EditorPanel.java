package org.example.gui.panels;

import org.example.gui.components.Editor;
import org.example.gui.controllers.EditorController;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class EditorPanel extends JFrame {

    private Editor editor;
    private final EditorController controller;
    public EditorPanel(EditorController controller) {
        this.controller = controller;
        init();
    }

    private void init() {
        setTitle("Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initMenu();

        editor = new Editor();
        add(editor);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initMenu() {
        var bar = new JMenuBar();

        var menu = new JMenu("File");
        var menuItem = new JMenuItem("Save");
        menuItem.addActionListener(controller::saveFile);
        menu.add(menuItem);
        bar.add(menu);
        setJMenuBar(bar);
    }

    public String getText() {
        return editor.getText();
    }

    public Optional<Path> chooseFile() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Save file");

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
