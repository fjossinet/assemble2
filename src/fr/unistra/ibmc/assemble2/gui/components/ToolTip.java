package fr.unistra.ibmc.assemble2.gui.components;

import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.model.Residue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.JToolTip;


public class ToolTip {

    //orientation of the arrow
    public static Byte LEFT = 0, RIGHT = 1, REMOVABLE = 3;

    private float width, height, x,y;
    private String text;
    private Byte orientation = ToolTip.LEFT;
    private Residue residue; //a tooltip can be linked to a residue. This means that the tooltip will follow the residue during zoom/translation
    private Mediator mediator;

    public ToolTip(Mediator mediator) {
        this.mediator = mediator;
    }

    public ToolTip(Mediator mediator, float x, float y, float width, float height, String text) {
        this(mediator);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    public void setResidue(Residue residue) {
        this.residue = residue;
    }

    public void setOrientation(Byte orientation) {
        this.orientation = orientation;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        Font previousFont = g2.getFont();
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(text, g2);
        this.width = (float)r.getWidth()+20;

        Color previousColor = g2.getColor();

        if (this.residue != null) { //we update the tootip to the last coordinates of the residue
            if (this.orientation == RIGHT)
                this.x = (float)this.residue.getCurrentX(mediator.getSecondaryCanvas().getGraphicContext());
            else
                this.x = (float)this.residue.getCurrentX(mediator.getSecondaryCanvas().getGraphicContext())+(float)mediator.getSecondaryCanvas().getGraphicContext().getCurrentWidth();
            this.y = (float)this.residue.getCurrentCenterY(mediator.getSecondaryCanvas().getGraphicContext());
        }

        if (this.orientation == LEFT) {
            GeneralPath triangle = new GeneralPath();
            triangle.moveTo(x, y);
            triangle.lineTo(x + 15, y - 6);
            triangle.lineTo(x + 15, y + 6);
            triangle.lineTo(x, y);
            triangle.closePath();

            Shape round = new RoundRectangle2D.Float(x + 15, y - (height - 1 - 8) / 2,
                    width - 1 - 8,
                    height - 1 - 8,
                    15, 15);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(50, 73, 126, 160));
            g2.fill(round);
            g2.fill(triangle);

            g2.setColor(new Color(39, 74, 116));
            g2.setStroke(new BasicStroke(2));
            g2.draw(round);
            g2.fill(triangle);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            double y = this.y - r.getHeight() / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(text, x + 20, (float) y);
        } else if (this.orientation == RIGHT) {
            GeneralPath triangle = new GeneralPath();
            triangle.moveTo(x, y);
            triangle.lineTo(x - 15, y - 6);
            triangle.lineTo(x - 15, y + 6);
            triangle.lineTo(x, y);
            triangle.closePath();
            Shape round = new RoundRectangle2D.Float(x - 15-(width - 1 - 8), y - (height - 1 - 8) / 2,
                    width - 1 - 8,
                    height - 1 - 8,
                    15, 15);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(50, 73, 126, 160));
            g2.fill(round);
            g2.fill(triangle);

            g2.setColor(new Color(39, 74, 116));
            g2.setStroke(new BasicStroke(2));
            g2.draw(round);
            g2.fill(triangle);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            double y = this.y - r.getHeight() / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(text, x - 15 - (width - 1 - 8) + 5, (float) y);

        }  else if (this.orientation == REMOVABLE) {
            Shape round = new RoundRectangle2D.Float(x - (width - 1 - 8)/2, y - (height - 1 - 8) / 2,
                    width - 1 - 8,
                    height - 1 - 8,
                    15, 15);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(50, 73, 126, 160));
            g2.fill(round);

            g2.setColor(new Color(39, 74, 116));
            g2.setStroke(new BasicStroke(2));
            g2.draw(round);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            double y = this.y - r.getHeight() / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(text, x - 15 - (width - 1 - 8) + 5, (float) y);

        }

        g2.setFont(previousFont);
        g2.setColor(previousColor);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    }


}
