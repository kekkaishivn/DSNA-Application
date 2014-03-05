package com.dsna.desktop.client.ui;

import java.awt.Dimension;

import javax.swing.JFrame;

public class ClientLauncher {

    public static void main(String[] args) {
        JFrame appFrame = new ClientFrame();
        appFrame.setSize(new Dimension(800, 600));
        appFrame.setVisible(true);
        appFrame.show();
    }

}
