package org.example.gui.components;

import org.example.core.Controls;
import org.example.core.Display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SwingDisplay extends Canvas implements Display, KeyListener, Controls {

    private final BufferedImage image;
    private final int[] pixels;
    private final boolean[] tiles;

    private static final int SIZE = 8;

    private final boolean grid = false;

    private final JFrame frame;

    private Timer timer;

    public SwingDisplay() {this(null);}

    public SwingDisplay(Component c) {
        setSize(Display.WIDTH * SIZE, Display.HEIGHT * SIZE);

        image = new BufferedImage(Display.WIDTH * SIZE, Display.HEIGHT * SIZE, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

        tiles = new boolean[Display.WIDTH * Display.HEIGHT];
        frame = new JFrame();
        this.addKeyListener(this);
        timer = new Timer(50, e->this.refresh());
        SwingUtilities.invokeLater(()->{

            frame.add(this);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(c);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setVisible(true);
            timer.start();
        });
    }

    private void draw() {
        Arrays.fill(pixels, 0x000000);

        for(int y=0; y<getHeight(); y++) {
            for(int x=0; x<getWidth(); x++) {
                int tx = x / SIZE;
                int ty = y / SIZE;
                boolean tile = tiles[tx + ty * Display.WIDTH];

                if (grid) {
                    if(x % SIZE == 0 || y % SIZE == 0) {
                        pixels[x + y * getWidth()] = 0x555555;
                    } else {
                        pixels[x + y * getWidth()] = tile ? 0xFFFFFF : 0;
                    }
                } else {
                    pixels[x + y * getWidth()] = tile ? 0xFFFFFF : 0;
                }
            }
        }
    }

    public void refresh() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null){
            createBufferStrategy(3);
            return;
        }

        Graphics2D g = null;
        do {
            try {
                draw();
                g = (Graphics2D) bs.getDrawGraphics();
                g.drawImage(image, 0, 0, null);
                bs.show();
            } finally {
                if(g!=null) {
                    g.dispose();
                }
            }
        } while (bs.contentsLost());
    }

    @Override
    public int getPixel(int x, int y) {
        return tiles[x + y * Display.WIDTH] ? 1 : 0;
    }

    @Override
    public void setPixel(int x, int y, int p) {
        tiles[x + y * Display.WIDTH] = p!=0;
    }

    public JFrame getFrame() {
        return frame;
    }

    private static final Map<Integer, Byte> keyToCode = new HashMap<>() {{
        put(KeyEvent.VK_X, (byte) 0);
        put(KeyEvent.VK_1, (byte) 1);
        put(KeyEvent.VK_2, (byte) 2);
        put(KeyEvent.VK_3, (byte) 3);
        put(KeyEvent.VK_Q, (byte) 4);
        put(KeyEvent.VK_W, (byte) 5);
        put(KeyEvent.VK_E, (byte) 6);
        put(KeyEvent.VK_A, (byte) 7);
        put(KeyEvent.VK_S, (byte) 8);
        put(KeyEvent.VK_D, (byte) 9);
        put(KeyEvent.VK_Z, (byte) 0xA);
        put(KeyEvent.VK_C, (byte) 0xB);
        put(KeyEvent.VK_4, (byte) 0xC);
        put(KeyEvent.VK_R, (byte) 0xD);
        put(KeyEvent.VK_F, (byte) 0xE);
        put(KeyEvent.VK_V, (byte) 0xF);
    }};

    private final Map<Byte, Boolean> pressed = new HashMap<>();

    private byte keyPressed = -1;

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        var code = setKey(e.getKeyCode(), true);
        if(code > -1 && keyPressed==-2) {
            keyPressed = code;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        setKey(e.getKeyCode(), false);
    }

    private byte setKey(int keyCode, boolean isPressed) {
        if(keyToCode.containsKey(keyCode)) {
            var code = keyToCode.get(keyCode);
            pressed.put(code, isPressed);
            return code;
        }

        return -1;
    }

    @Override
    public boolean isKeyPressed(byte v) {
        var res = pressed.getOrDefault(v, false);
        pressed.put(v, false);
        return res;
    }

    @Override
    public boolean isKeyNotPressed(byte v) {
        return !isKeyPressed(v);
    }

    @Override
    public byte waitForKeyPress() {
        byte ret = -1;
        if(keyPressed > -1) {
            ret = keyPressed;
            keyPressed = -1;
        } else if(keyPressed!=-2) {
            keyPressed = -2;
        }
        return ret;
    }

    public void exitOnClose(Runnable cleanup) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                    timer.stop();
                    cleanup.run();
            }
        });
    }
}
