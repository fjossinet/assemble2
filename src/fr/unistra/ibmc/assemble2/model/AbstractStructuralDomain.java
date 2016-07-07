package fr.unistra.ibmc.assemble2.model;


import java.awt.*;

public abstract class AbstractStructuralDomain implements StructuralDomain {

    protected Color customColor;

    @Override
    public void setCustomColor(Color c) {
        this.customColor = c;
    }

    @Override
    public Color getCustomColor() {
        return this.customColor;
    }
}
