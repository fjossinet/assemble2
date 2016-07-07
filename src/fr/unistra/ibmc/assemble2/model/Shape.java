package fr.unistra.ibmc.assemble2.model;


public interface Shape {

    void draw(final java.awt.Graphics2D g2, final char orientation);

    java.awt.Shape getShape();

    String getSVG(BaseBaseInteraction bbi);
}

