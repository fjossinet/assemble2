package fr.unistra.ibmc.assemble2.gui.components;


import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.IoUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.ArrayList;

public class SecondaryCanvasMessagingSystem {

    private float width, height;
    private java.util.List<String> messages;
    private java.util.List<MessagingSystemAction> closeActions, nextActions;
    private int step;
    private Mediator mediator;
    private Shape messageWindow, close, next;
    //if the messaging system is unResponsive, no ability to add a new thread or a single step (which also the current thread of messages)
    private boolean unResponsive;
    private Timer currentTimer;

    public SecondaryCanvasMessagingSystem(Mediator mediator) {
        this.mediator = mediator;
    }

    public SecondaryCanvasMessagingSystem(Mediator mediator, float width, float height) {
        this(mediator);
        this.width = width;
        this.height = height;
        this.messages = new ArrayList<String>();
        this.closeActions = new ArrayList<MessagingSystemAction>();
        this.nextActions = new ArrayList<MessagingSystemAction>();
        this.currentTimer = new Timer(5000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clear();
                mediator.getSecondaryCanvas().repaint();
            }
        });
        currentTimer.setRepeats(false);
    }

    public void setUnResponsive(boolean unResponsive) {
        this.unResponsive = unResponsive;
    }

    public boolean isUnResponsive() {
        return this.unResponsive;
    }

    public void nextStep() {
        this.step++;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void addThread(java.util.List<String> messages, java.util.List<MessagingSystemAction> closeActions,  java.util.List<MessagingSystemAction> nextActions) {
        if (!this.unResponsive) {
            this.messages.clear();
            this.messages.addAll(messages);
            this.nextActions.clear();
            this.nextActions.addAll(nextActions);
            this.closeActions.clear();
            this.closeActions.addAll(closeActions);
            this.step = 0;
            this.currentTimer.stop();
        }
    }

    public void addSingleStep(String message, MessagingSystemAction closeAction, MessagingSystemAction nextAction) {
        if (!this.unResponsive) {
            this.messages.clear();
            this.messages.add(message);
            this.closeActions.clear();
            this.closeActions.add(closeAction);
            this.nextActions.clear();
            this.nextActions.add(nextAction);
            this.step = 0;
            this.currentTimer.restart();
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        Font previousFont = g2.getFont();
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(messages.get(step), g2);
        this.width = (float)r.getWidth()+20;

        Color previousColor = g2.getColor();

        float x = mediator.getSecondaryCanvas().getWidth()/2,
                y = mediator.getSecondaryCanvas().getHeight()-100;

        //the message window
        this.messageWindow = new RoundRectangle2D.Float(x - (width - 1 - 8)/2, y - (height - 1 - 8) / 2,
                width - 1 - 8,
                height - 1 - 8,
                15, 15);

        g2.setColor(new Color(50, 73, 126, 160));
        g2.fill(this.messageWindow);

        g2.setColor(new Color(39, 74, 116));
        g2.setStroke(new BasicStroke(2));
        g2.draw(this.messageWindow);


        this.close = new Ellipse2D.Float(x - (width - 1 - 8) / 2 - 10, y - (height - 1 - 8) / 2 - 10, 20, 20);

        g2.setColor(new Color(163, 9, 18));
        g2.fill(this.close);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double x1 = 10 * Math.cos(Math.toRadians(-45)) + x - (width - 1 - 8) / 2;
        double y1 = 10 * Math.sin(Math.toRadians(-45)) + y - (height - 1 - 8) / 2;
        double x2 = 10 * Math.cos(Math.toRadians(135)) + x - (width - 1 - 8) / 2;
        double y2 = 10 * Math.sin(Math.toRadians(135)) + y - (height - 1 - 8) / 2;
        g2.draw(new Line2D.Double(x1, y1, x2, y2));
        x1 = 10 * Math.cos(Math.toRadians(45)) + x - (width - 1 - 8) / 2;
        y1 = 10 * Math.sin(Math.toRadians(45)) + y - (height - 1 - 8) / 2;
        x2 = 10 * Math.cos(Math.toRadians(-135)) + x - (width - 1 - 8) / 2;
        y2 = 10 * Math.sin(Math.toRadians(-135)) + y - (height - 1 - 8) / 2;
        g2.draw(new Line2D.Double(x1, y1, x2, y2));

        g2.setColor(new Color(163, 9, 18));
        g2.setStroke(new BasicStroke(3));
        g2.draw(this.close);

        //next icon is displayed if we're not at the end of the arrays OR if we're at the end and if the last nextActions is not null (meaning that something will be triggered at the end. See for example the send of a bug report in Assemble@loadPDBID()
        if (this.step < this.messages.size()-1 || (this.step == this.messages.size()-1 && this.nextActions.get(this.messages.size()-1) != null)) {
            this.next = new Ellipse2D.Float(x + (width - 1 - 8) / 2 - 10, y + (height - 1 - 8) / 2 - 10, 20, 20);
            g2.setColor(new Color(74, 163, 6));
            g2.setStroke(new BasicStroke(3));
            g2.draw(this.next);
            g2.fill(this.next);

            g2.setColor(Color.WHITE);
            GeneralPath arrow = new GeneralPath();
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND ,BasicStroke.JOIN_ROUND));
            x1 = x+(width - 1 - 8)/2-1.5;
            y1 = y + (height - 1 - 8) / 2-5;
            x2 = x+(width - 1 - 8)/2+5-1.5;
            y2 = y + (height - 1 - 8) / 2;
            arrow.moveTo(x1, y1);
            arrow.lineTo(x2, y2);
            double x3 = x+(width - 1 - 8)/2+5-1.5;
            double y3 = y + (height - 1 - 8) / 2;
            double x4 = x+(width - 1 - 8)/2-1.5;
            double y4 = y + (height - 1 - 8) / 2+5;
            arrow.lineTo(x3, y3);
            arrow.lineTo(x4, y4);
            arrow.lineTo(x1, y1);
            arrow.closePath();
            g2.fill(arrow);
            g2.draw(arrow);
        } else {
            this.next = null;
        }

        //the message
        y = y - (float)r.getHeight() / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(messages.get(step), x - (width - 1 - 8) / 2 + 5, y);

        g2.setFont(previousFont);
        g2.setColor(previousColor);

    }

    public boolean hasSomethingToPrint() {
        return this.step < this.messages.size();
    }

    public void mouseClicked(MouseEvent e) {
        if ( this.next != null && this.next.contains(e.getX(), e.getY())) {
            this.nextStep();
            mediator.getSecondaryCanvas().repaint();
            if (this.step-1 >= 0 && this.step-1 < this.nextActions.size() && this.nextActions.get(this.step-1) != null)
                this.nextActions.get(this.step-1).run();
            if ((this.nextActions.get(this.messages.size()-1)) == null) { //nothing to do next, the message is disappearing after 5 seconds
                currentTimer.restart();
            }
        }
        if ( this.close != null && this.close.contains(e.getX(), e.getY())) {
            this.nextStep();
            if (this.step-1 >= 0 && this.step-1 < this.closeActions.size() && this.closeActions.get(this.step-1) != null)
                this.closeActions.get(this.step-1).run();
            this.clear();
            mediator.getSecondaryCanvas().repaint();
        }
    }


    public void clear() {
        this.step = 0;
        this.messages.clear();
        this.closeActions.clear();
        this.nextActions.clear();
    }
}
