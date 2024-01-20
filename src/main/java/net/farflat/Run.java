package net.farflat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Run {
    private static final int FPS = 75;

    public static void main(String[] args) {
        boolean debug;
        System.out.println("!");
        try {
            Thread.sleep(100);
            PointerInfo pinfo = MouseInfo.getPointerInfo();
            Point mloc = pinfo.getLocation();
            debug = mloc.distance(0, 0) <= 10;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (debug) {
            System.out.println("debug");
            SimplexNoise.main(args);
        }
        // Create an instance of the FarFlat class
        FarFlat farFlat = new FarFlat();

        // Create a JFrame to hold the JPanel
        JFrame frame = new JFrame("FarFlat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add the FarFlat panel to the frame
        frame.getContentPane().add(farFlat);

        // Set frame properties and make it visible
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);

        new Timer((int) (1000. / FPS), new ActionListener() {
            private long oTime = System.nanoTime();
            private double delta = 1. / FPS;

            @Override
            public void actionPerformed(ActionEvent e) {
                farFlat.run(2./FPS);
                frame.repaint();
            }
        }).start();
    }
}
