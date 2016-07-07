package fr.unistra.ibmc.assemble2.utils;

import org.apache.batik.parser.AWTPathProducer;

import java.io.StringReader;
import java.io.IOException;
import java.awt.geom.GeneralPath;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.Shape;
import java.text.DecimalFormat;


public class SvgPath {
    private String pathString;
    private Shape shape;
    private int width = 100, height = 100;

    public SvgPath(String pathString) throws IOException {
        this.pathString = pathString;
        shape = AWTPathProducer.createShape(new StringReader(pathString), GeneralPath.WIND_EVEN_ODD);
        width = shape.getBounds().width;
        height = shape.getBounds().height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Shape getShape() {
        return shape;
    }

    public String toString() {
        return pathString;
    }
}
