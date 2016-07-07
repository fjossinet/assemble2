package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.utils.SvgPath;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class HelpDialog {
    private Shape dialog;
    private Timer activityTimer;
    private Color color;
    private Mediator mediator;
    private int flick = 0;

    public HelpDialog(final Mediator mediator) {
        this.mediator = mediator;
        try {
            this.dialog = new SvgPath("M 76.24207 59.592477 L 82.10348 77.461454 L 87.965064 59.592477 L 106.945264 59.592477 C 116.973344 59.592477 125.112076 47.139745 125.112076 31.796218 C 125.112076 16.452771 116.973344 4.0000312 106.945264 4.0000312 L 22.166811 4.0000312 C 12.1387315 4.0000312 4 16.452771 4 31.796218 C 4 47.139745 12.1387315 59.592477 22.166811 59.592477 L 76.24207 59.592477 Z").getShape();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.activityTimer = new Timer(500,new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                color = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
                mediator.getSecondaryCanvas().repaint();
                flick +=1;
                if (flick == 10) {
                    activityTimer.stop();
                }
            }
        });
    }

    public void startActivity() {
        this.activityTimer.start();
    }

    public void draw(Graphics2D g2, int startX, int startY) {
        g2.setColor(this.color);
        Rectangle2D buttonShape = this.dialog.getBounds2D();
        g2.translate(startX-buttonShape.getWidth(), startY-buttonShape.getHeight());
        g2.fill(this.dialog);
        g2.translate(-startX+buttonShape.getWidth(), -startY+buttonShape.getHeight());
        g2.setColor(Color.WHITE);
        FontRenderContext frc = g2.getFontRenderContext();
        String text = "Click me\nfor help...";
        TextLayout layout = new TextLayout(text, new Font("Helvetica", Font.PLAIN, 20), frc);
        String[] outputs = text.split("\n");
        Font f = g2.getFont();
        g2.setFont(new Font("Helvetica", Font.PLAIN, 20));
        for(int i=0; i<outputs.length; i++)
            g2.drawString(outputs[i], startX-(int)buttonShape.getWidth()+20,(int) (startY-(int)buttonShape.getHeight()+25+i*layout.getBounds().getHeight()+0.5));
        g2.setFont(f);
        if (flick == 0)
            this.startActivity();
    }


}
