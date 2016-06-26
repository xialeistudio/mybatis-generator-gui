package com.ddhigh.mybatis.window;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

public class AboutDialog extends JDialog {
    private JPanel contentPane;
    private static Logger logger = Logger.getLogger(AboutDialog.class);


    public AboutDialog() {
        setContentPane(contentPane);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("关于");
        setLocationRelativeTo(null);
        setResizable(false);
        pack();
        setVisible(true);
    }
}
