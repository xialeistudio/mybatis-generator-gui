package com.ddhigh.mybatis.util;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

/**
 * swing1
 * 2016/6/22 0022 13:08
 *
 * @author xialeistudio
 */
public class GUIUtil {

    public final static String LOOK_AND_FEEL_WINDOWS = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    public static void setCenter(Component component) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        component.setLocation(screenSize.width / 2 - component.getWidth() / 2, screenSize.height / 2 - component.getHeight() / 2);
    }

    public static void setCursor(Component component, String image) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image1 = toolkit.getImage(image);
        Cursor cursor = toolkit.createCustomCursor(image1, new Point(0, 0), "stick");
        component.setCursor(cursor);
    }

    public static void setIcon(Window window, String image) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image1 = toolkit.getImage(image);
        window.setIconImage(image1);
    }

    public static void setFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys();
             keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }
}
