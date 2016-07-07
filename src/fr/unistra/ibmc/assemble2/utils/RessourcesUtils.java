package fr.unistra.ibmc.assemble2.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public final class RessourcesUtils {

    public static Icon getIcon(String file) {
        Icon ret = null;
        try {
            ret = new ImageIcon(getImage(file));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static Image getImage(final String imageName) {
        if (imageName == null) {
            return null;
        }
        Image image = null;
        try {
            image = ImageIO.read(RessourcesUtils.class.getResource("images/"+imageName));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

}

