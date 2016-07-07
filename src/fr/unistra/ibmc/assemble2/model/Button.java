package fr.unistra.ibmc.assemble2.model;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface Button  {

    public boolean mouseClicked(MouseEvent e);

    public void draw(final Graphics2D g2);

}
