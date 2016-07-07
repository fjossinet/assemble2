package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public interface StructuralDomain {

    public int getLength();

    public List<Residue> getResidues();

    public void flipEnds(Point2D center,GraphicContext gc);

    public void rotateEnds(java.awt.geom.Point2D centerPoint, double angle,GraphicContext gc);

    public void translateEnds(double dragX, double dragY,GraphicContext gc);

    public void setCoordinates(GraphicContext gc);

    public boolean isSelected();

    public Location getLocation();

    public String getName();

    public void setName(String name);

    public void setCustomColor(Color c);

    public Color getCustomColor();

}