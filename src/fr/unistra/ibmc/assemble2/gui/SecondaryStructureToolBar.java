package fr.unistra.ibmc.assemble2.gui;


import fr.unistra.ibmc.assemble2.gui.components.ToolTip;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.io.computations.Rnaplot;
import fr.unistra.ibmc.assemble2.model.Residue;
import fr.unistra.ibmc.assemble2.utils.Pair;
import fr.unistra.ibmc.assemble2.utils.SvgPath;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SecondaryStructureToolBar implements ToolBar {

    public static final byte REFERENCE_STRUCTURE = 0, CONSENSUS_STRUCTURE = 1, QUALITATIVE_DATA = 2, QUANTITATIVE_DATA = 3, BP_PROBABILITIES = 4;

    private Shape
            showInteractions,
            centerView,
            fitToPage,
            locked2D,
            unlocked2D,
            flip,
            reorganizeHelices,
            rendering,
            linkQualitativeValues,
            linkQuantitativeValues,
            screenshot;
    private boolean overLocked2D, overCenterView, overFitToPage, overShowInteractions, overFlip, overRendering, overReorganizeHelices, overUnLocked2D, overLinkQualitativeValues, overLinkQuantitativeValues, overScreenshot;
    private byte renderingMode = REFERENCE_STRUCTURE;
    private Mediator mediator;
    private ToolTip tooltip;

    public SecondaryStructureToolBar(Mediator mediator) {
        this.mediator = mediator;
        try {
            this.showInteractions = new SvgPath("M16,8.286C8.454,8.286,2.5,16,2.5,16s5.954,7.715,13.5,7.715c5.771,0,13.5-7.715,13.5-7.715S21.771,8.286,16,8.286zM16,20.807c-2.649,0-4.807-2.157-4.807-4.807s2.158-4.807,4.807-4.807s4.807,2.158,4.807,4.807S18.649,20.807,16,20.807zM16,13.194c-1.549,0-2.806,1.256-2.806,2.806c0,1.55,1.256,2.806,2.806,2.806c1.55,0,2.806-1.256,2.806-2.806C18.806,14.451,17.55,13.194,16,13.194z").getShape();
            this.centerView = new SvgPath("M25.083,18.895l-8.428-2.259l2.258,8.428l1.838-1.837l7.053,7.053l2.476-2.476l-7.053-7.053L25.083,18.895zM5.542,11.731l8.428,2.258l-2.258-8.428L9.874,7.398L3.196,0.72L0.72,3.196l6.678,6.678L5.542,11.731zM7.589,20.935l-6.87,6.869l2.476,2.476l6.869-6.869l1.858,1.857l2.258-8.428l-8.428,2.258L7.589,20.935zM23.412,10.064l6.867-6.87l-2.476-2.476l-6.868,6.869l-1.856-1.856l-2.258,8.428l8.428-2.259L23.412,10.064z").getShape();
            this.fitToPage =  new SvgPath("M1.999,2.332v26.499H28.5V2.332H1.999zM26.499,26.832H4V12.5h8.167V4.332h14.332V26.832zM15.631,17.649l5.468,5.469l-1.208,1.206l5.482,1.469l-1.47-5.481l-1.195,1.195l-5.467-5.466l1.209-1.208l-5.482-1.469l1.468,5.48L15.631,17.649z").getShape();
            this.locked2D = new SvgPath("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v4.876H5.042v15.083h21.916V15.334H24.875zM10.625,10.458c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876h-10.75V10.458zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z").getShape();
            this.flip = new SvgPath("M19.275,3.849l1.695,8.56l1.875-1.642c2.311,3.59,1.72,8.415-1.584,11.317c-2.24,1.96-5.186,2.57-7.875,1.908l-0.84,3.396c3.75,0.931,7.891,0.066,11.02-2.672c4.768-4.173,5.521-11.219,1.94-16.279l2.028-1.775L19.275,3.849zM8.154,20.232c-2.312-3.589-1.721-8.416,1.582-11.317c2.239-1.959,5.186-2.572,7.875-1.909l0.842-3.398c-3.752-0.93-7.893-0.067-11.022,2.672c-4.765,4.174-5.519,11.223-1.939,16.283l-2.026,1.772l8.26,2.812l-1.693-8.559L8.154,20.232z").getShape();
            this.reorganizeHelices = new SvgPath("M23.043,4.649l-0.404-2.312l-1.59,1.727l-2.323-0.33l1.151,2.045l-1.032,2.108l2.302-0.463l1.686,1.633l0.271-2.332l2.074-1.099L23.043,4.649zM26.217,18.198l-0.182-1.25l-0.882,0.905l-1.245-0.214l0.588,1.118l-0.588,1.118l1.245-0.214l0.882,0.905l0.182-1.25l1.133-0.56L26.217,18.198zM4.92,7.672L5.868,7.3l0.844,0.569L6.65,6.853l0.802-0.627L6.467,5.97L6.118,5.013L5.571,5.872L4.553,5.908l0.647,0.786L4.92,7.672zM10.439,10.505l1.021-1.096l1.481,0.219l-0.727-1.31l0.667-1.341l-1.47,0.287l-1.069-1.048L10.16,7.703L8.832,8.396l1.358,0.632L10.439,10.505zM17.234,12.721c-0.588-0.368-1.172-0.618-1.692-0.729c-0.492-0.089-1.039-0.149-1.425,0.374L2.562,30.788h6.68l9.669-15.416c0.303-0.576,0.012-1.041-0.283-1.447C18.303,13.508,17.822,13.09,17.234,12.721zM13.613,21.936c-0.254-0.396-0.74-0.857-1.373-1.254c-0.632-0.396-1.258-0.634-1.726-0.69l4.421-7.052c0.064-0.013,0.262-0.021,0.543,0.066c0.346,0.092,0.785,0.285,1.225,0.562c0.504,0.313,0.908,0.677,1.133,0.97c0.113,0.145,0.178,0.271,0.195,0.335c0.002,0.006,0.004,0.011,0.004,0.015L13.613,21.936z").getShape();
            this.rendering = new SvgPath("M8.125,29.178l1.311-1.5l1.315,1.5l1.311-1.5l1.311,1.5l1.315-1.5l1.311,1.5l1.312-1.5l1.314,1.5l1.312-1.5l1.312,1.5l1.314-1.5l1.312,1.5v-8.521H8.125V29.178zM23.375,17.156c-0.354,0-5.833-0.166-5.833-2.917s0.75-8.834,0.75-8.834S18.542,2.822,16,2.822s-2.292,2.583-2.292,2.583s0.75,6.083,0.75,8.834s-5.479,2.917-5.833,2.917c-0.5,0-0.5,1.166-0.5,1.166v1.271h15.75v-1.271C23.875,18.322,23.875,17.156,23.375,17.156zM16,8.031c-0.621,0-1.125-2.191-1.125-2.812S15.379,4.094,16,4.094s1.125,0.504,1.125,1.125S16.621,8.031,16,8.031z").getShape();
            this.linkQualitativeValues = new SvgPath("M14.263,2.826H7.904L2.702,8.028v6.359L18.405,30.09l11.561-11.562L14.263,2.826zM6.495,8.859c-0.619-0.619-0.619-1.622,0-2.24C7.114,6,8.117,6,8.736,6.619c0.62,0.62,0.619,1.621,0,2.241C8.117,9.479,7.114,9.479,6.495,8.859z").getShape();
            this.linkQuantitativeValues = new SvgPath("M21.25,8.375V28h6.5V8.375H21.25zM12.25,28h6.5V4.125h-6.5V28zM3.25,28h6.5V12.625h-6.5V28z").getShape();
            this.screenshot = new SvgPath("M24.25,10.25H20.5v-1.5h-9.375v1.5h-3.75c-1.104,0-2,0.896-2,2v10.375c0,1.104,0.896,2,2,2H24.25c1.104,0,2-0.896,2-2V12.25C26.25,11.146,25.354,10.25,24.25,10.25zM15.812,23.499c-3.342,0-6.06-2.719-6.06-6.061c0-3.342,2.718-6.062,6.06-6.062s6.062,2.72,6.062,6.062C21.874,20.78,19.153,23.499,15.812,23.499zM15.812,13.375c-2.244,0-4.062,1.819-4.062,4.062c0,2.244,1.819,4.062,4.062,4.062c2.244,0,4.062-1.818,4.062-4.062C19.875,15.194,18.057,13.375,15.812,13.375z").getShape();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2, int startX, int startY) {
        if (tooltip != null)
            tooltip.draw(g2);
        if (mediator.getSecondaryCanvas().getGraphicContext()!= null && mediator.getSecondaryCanvas().getGraphicContext().isEditStructure()) {
            this.locked2D = null;
            try {
                this.unlocked2D = new SvgPath("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v0.375h3.5v-0.375c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876H5.042v15.083h21.916V15.334H24.875zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z").getShape();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            this.unlocked2D = null;
            try {
                this.locked2D = new SvgPath("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v4.876H5.042v15.083h21.916V15.334H24.875zM10.625,10.458c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876h-10.75V10.458zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z").getShape();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.locked2D != null) {
            Rectangle2D buttonShape = this.locked2D.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overLocked2D) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.locked2D);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.locked2D);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+10;
        }
        if (this.unlocked2D != null) {
            Rectangle2D buttonShape = this.unlocked2D.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overUnLocked2D) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.unlocked2D);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.unlocked2D);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+10;
        }
        if (this.centerView != null) {
            Rectangle2D buttonShape = this.centerView.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overCenterView) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.centerView);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.centerView);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;

        }
        if (this.fitToPage != null) {
            Rectangle2D buttonShape = this.fitToPage.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overFitToPage) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.fitToPage);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.fitToPage);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }
        if (this.flip != null) {
            Rectangle2D buttonShape = this.flip.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryCanvas().getSelectedHelix() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overFlip) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.flip);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.flip);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }
        if (this.showInteractions != null) {
            Rectangle2D buttonShape = this.showInteractions.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null){
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overShowInteractions) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.showInteractions);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.showInteractions);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+10;
        }
        if (this.reorganizeHelices != null) {
            Rectangle2D buttonShape = this.reorganizeHelices.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overReorganizeHelices) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.reorganizeHelices);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.reorganizeHelices);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }
        if (this.rendering != null) {
            Rectangle2D buttonShape = this.rendering.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overRendering) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.rendering);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.rendering);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }
        if (this.linkQualitativeValues != null) {
            Rectangle2D buttonShape = this.linkQualitativeValues.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overLinkQualitativeValues) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.linkQualitativeValues);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.linkQualitativeValues);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }
        if (this.linkQuantitativeValues != null) {
            Rectangle2D buttonShape = this.linkQuantitativeValues.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overLinkQuantitativeValues) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.linkQuantitativeValues);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.linkQuantitativeValues);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+10;
        }
        if (this.screenshot != null) {
            Rectangle2D buttonShape = this.screenshot.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getSecondaryStructure() == null) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            else {
                if (overScreenshot) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.screenshot);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.screenshot);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }
    }

    public void setRenderingMode(byte renderingMode) {
        this.renderingMode = renderingMode;
    }

    public byte getRenderingMode() {
        return this.renderingMode;
    }

    public void mouseClicked(MouseEvent e, int startX, int startY) {
        if (this.locked2D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.locked2D.getBounds2D().getMinX()+startX, this.locked2D.getBounds2D().getBounds2D().getMinY()+startY, this.locked2D.getBounds2D().getWidth(), this.locked2D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                mediator.getSecondaryCanvas().getGraphicContext().setEditStructure(true);
                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("2D unlocked.", null, null);
                try {
                    this.locked2D = null;
                    this.unlocked2D = new SvgPath("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v0.375h3.5v-0.375c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876H5.042v15.083h21.916V15.334H24.875zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z").getShape();
                    this.overLocked2D = false;
                    this.overUnLocked2D = true;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                mediator.getSecondaryCanvas().repaint();
                return;
            }
            startY += buttonShape.getHeight()+10;
        }
        if (this.unlocked2D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.unlocked2D.getBounds2D().getMinX()+startX, this.unlocked2D.getBounds2D().getBounds2D().getMinY()+startY, this.unlocked2D.getBounds2D().getWidth(), this.unlocked2D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                mediator.getSecondaryCanvas().getGraphicContext().setEditStructure(false);
                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("2D locked.", null, null);
                try {
                    this.unlocked2D = null;
                    this.locked2D = new SvgPath("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v4.876H5.042v15.083h21.916V15.334H24.875zM10.625,10.458c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876h-10.75V10.458zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z").getShape();
                    this.overLocked2D = true;
                    this.overUnLocked2D = false;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                mediator.getSecondaryCanvas().repaint();
                return;
            }
            startY += buttonShape.getHeight()+10;
        }
        if (this.centerView != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.centerView.getBounds2D().getMinX()+startX, this.centerView.getBounds2D().getBounds2D().getMinY()+startY, this.centerView.getBounds2D().getWidth(), this.centerView.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                if (mediator.getSecondaryStructure() != null) {
                    mediator.getSecondaryCanvas().centerSecondaryStructure();
                }
                return;

            }
            startY += buttonShape.getHeight()+7;
        }
        if (this.fitToPage != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.fitToPage.getBounds2D().getMinX()+startX, this.fitToPage.getBounds2D().getBounds2D().getMinY()+startY, this.fitToPage.getBounds2D().getWidth(), this.fitToPage.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                if (mediator.getSecondaryStructure() != null) {
                    mediator.getSecondaryCanvas().fitToPage();
                }
                return;

            }
            startY += buttonShape.getHeight()+7;
        }
        if (this.flip != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.flip.getBounds2D().getMinX()+startX, this.flip.getBounds2D().getBounds2D().getMinY()+startY, this.flip.getBounds2D().getWidth(), this.flip.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure()== null || mediator.getSecondaryCanvas().getSelectedHelix() == null)
                    return;
                mediator.getSecondaryCanvas().flipSelection();
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.showInteractions != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.showInteractions.getBounds2D().getMinX()+startX, this.showInteractions.getBounds2D().getBounds2D().getMinY()+startY, this.showInteractions.getBounds2D().getWidth(), this.showInteractions.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {  //we iterate over the display of single hbonds and tertiary interactions
                if (mediator.getSecondaryStructure() == null)
                    return;
                if (mediator.getSecondaryStructure().hasSingleHBonds() && mediator.getSecondaryCanvas().getGraphicContext().displaySingleHBonds) {
                    mediator.getSecondaryCanvas().getGraphicContext().setDisplaySingleHBonds(false);
                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Single hydrogen bonds hidden.", null, null);
                } else if (mediator.getSecondaryStructure().hasTertiaryInteractions() && mediator.getSecondaryCanvas().getGraphicContext().displayTertiaryInteractions) {
                    mediator.getSecondaryCanvas().getGraphicContext().setDisplayTertiaryInteractions(false);
                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Tertiary interactions hidden.", null, null);
                } else if (mediator.getSecondaryCanvas().getGraphicContext().isDisplayPositions()) {
                    mediator.getSecondaryCanvas().getGraphicContext().setDisplayPositions(false);
                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Labels hidden.", null, null);
                } else if (mediator.getSecondaryCanvas().getGraphicContext().isDisplayResidues()) {
                    mediator.getSecondaryCanvas().getGraphicContext().setDisplayResidues(false);
                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Residues hidden.", null, null);
                }
                else {
                    mediator.getSecondaryCanvas().getGraphicContext().setDisplaySingleHBonds(true);
                    mediator.getSecondaryCanvas().getGraphicContext().setDisplayTertiaryInteractions(true);
                    mediator.getSecondaryCanvas().getGraphicContext().setDisplayPositions(true);
                    mediator.getSecondaryCanvas().getGraphicContext().setDisplayResidues(true);
                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Full display restored.", null, null);
                }
                mediator.getSecondaryCanvas().repaint();
                return;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.reorganizeHelices != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.reorganizeHelices.getBounds2D().getMinX()+startX, this.reorganizeHelices.getBounds2D().getBounds2D().getMinY()+startY, this.reorganizeHelices.getBounds2D().getWidth(), this.reorganizeHelices.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                new javax.swing.SwingWorker() {
                    @Override
                    protected Object doInBackground()  {
                        mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                        try {
                            Pair<Double, Double> sizes =  new Rnaplot(mediator).plot(mediator.getSecondaryStructure());
                            if (sizes.getFirst() > mediator.getSecondaryCanvas().getWidth() && sizes.getSecond() > mediator.getSecondaryCanvas().getHeight()) {
                                Residue first = mediator.getSecondaryStructure().getResidue(1);
                                double transX = first.getX()-mediator.getSecondaryCanvas().getWidth()/2,
                                        transY = first.getY()-mediator.getSecondaryCanvas().getHeight()/2;
                                for (Residue r:mediator.getSecondaryStructure().getResidues())
                                    r.setRealCoordinates(r.getX()-transX,r.getY()-transY);
                            }
                            mediator.getSecondaryCanvas().getGraphicContext().initialize(mediator.getSecondaryStructure());
                            mediator.getSecondaryCanvas().repaint();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                        return null;
                    }
                }.execute();
                return;
            }
            startY += buttonShape.getHeight()+5;
        }
        if (this.rendering != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.rendering.getBounds2D().getMinX()+startX, this.rendering.getBounds2D().getBounds2D().getMinY()+startY, this.rendering.getBounds2D().getWidth(), this.rendering.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                //We iterate over the different rendering modes
                //1. reference structure mask
                //2. consensus structure mask
                //3. qualitative data (if any)
                //4. quantitative data (if any)
                //5. bp conservation (if any)
                switch (renderingMode) {
                    case REFERENCE_STRUCTURE:
                        renderingMode = CONSENSUS_STRUCTURE;
                        mediator.getSecondaryCanvas().repaint();
                        //mediator.getFoldingLandscape().repaint();
                        mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Consensus structure color scheme.", null, null);
                        break;
                    case CONSENSUS_STRUCTURE:
                        if (!mediator.getSecondaryCanvas().getGraphicContext().getQualitativeNames().isEmpty()) {
                            renderingMode = QUALITATIVE_DATA;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Qualitative data color scheme.", null, null);
                        }
                        else if (mediator.getSecondaryCanvas().getGraphicContext().getMinQuantitativeValue() != Float.MAX_VALUE && mediator.getSecondaryCanvas().getGraphicContext().getMaxQuantitativeValue() != Float.MIN_VALUE) {
                            renderingMode = QUANTITATIVE_DATA;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Quantitative data color scheme.", null, null);
                        }
                        else if (!mediator.getFoldingLandscape().getAllSecondaryStructures().isEmpty()) {
                            renderingMode = BP_PROBABILITIES;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Base-pair probabilities color scheme.", null, null);
                        }
                        else {
                            renderingMode = REFERENCE_STRUCTURE;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Reference structure color scheme.", null, null);
                        }
                        break;
                    case QUALITATIVE_DATA:
                        if (mediator.getSecondaryCanvas().getGraphicContext().getMinQuantitativeValue() != Float.MAX_VALUE && mediator.getSecondaryCanvas().getGraphicContext().getMaxQuantitativeValue() != Float.MIN_VALUE) {
                            renderingMode = QUANTITATIVE_DATA;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Quantitative data color scheme.", null, null);
                        }
                        else if (!mediator.getFoldingLandscape().getAllSecondaryStructures().isEmpty()) {
                            renderingMode = BP_PROBABILITIES;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Base-pair probabilities color scheme.", null, null);
                        }
                        else {
                            renderingMode = REFERENCE_STRUCTURE;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Reference structure color scheme.", null, null);
                        }
                        break;
                    case QUANTITATIVE_DATA:
                        if (!mediator.getFoldingLandscape().getAllSecondaryStructures().isEmpty()) {
                            renderingMode = BP_PROBABILITIES;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Base-pair probabilities color scheme.", null, null);
                        }
                        else {
                            renderingMode = REFERENCE_STRUCTURE;
                            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Reference structure color scheme.", null, null);
                        }
                        break;
                    case BP_PROBABILITIES:
                        renderingMode = REFERENCE_STRUCTURE;
                        mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Reference structure color scheme.", null, null);

                }
                mediator.getSecondaryCanvas().repaint();
                mediator.getFoldingLandscape().repaint();
                mediator.getAlignmentCanvas().repaint();
                return;
            }
            startY += buttonShape.getHeight()+5;
        }
        if (this.linkQualitativeValues != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.linkQualitativeValues.getBounds2D().getMinX()+startX, this.linkQualitativeValues.getBounds2D().getBounds2D().getMinY()+startY, this.linkQualitativeValues.getBounds2D().getWidth(), this.linkQualitativeValues.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(mediator.getAssemble().getLastWorkingDirectory());
                fileChooser.setFileHidingEnabled(true);
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().endsWith(".txt");
                    }

                    @Override
                    public String getDescription() {
                        return "Text Files (.txt)";
                    }
                });

                if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    final File f = fileChooser.getSelectedFile();
                    mediator.getAssemble().setLastWorkingDirectory(f);
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            if (f.getName().endsWith(".txt")) {
                                try {
                                    FileParser.parseQualitativeValues(f, mediator);
                                    renderingMode = QUALITATIVE_DATA;
                                    mediator.getSecondaryCanvas().repaint();
                                    mediator.getFoldingLandscape().repaint();
                                    mediator.getAlignmentCanvas().repaint();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    }.execute();
                }
                return;
            }
            startY += buttonShape.getHeight()+5;
        }
        if (this.linkQuantitativeValues != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.linkQuantitativeValues.getBounds2D().getMinX()+startX, this.linkQuantitativeValues.getBounds2D().getBounds2D().getMinY()+startY, this.linkQuantitativeValues.getBounds2D().getWidth(), this.linkQuantitativeValues.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(mediator.getAssemble().getLastWorkingDirectory());
                fileChooser.setFileHidingEnabled(true);
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().endsWith(".txt");
                    }

                    @Override
                    public String getDescription() {
                        return "Text Files (.txt)";
                    }
                });

                if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    final File f = fileChooser.getSelectedFile();
                    mediator.getAssemble().setLastWorkingDirectory(f);
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            if (f.getName().endsWith(".txt")) {
                                try {
                                    FileParser.parseQuantitativeValues(f, mediator);
                                    renderingMode = QUANTITATIVE_DATA;
                                    mediator.getSecondaryCanvas().repaint();
                                    mediator.getFoldingLandscape().repaint();
                                    mediator.getAlignmentCanvas().repaint();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    }.execute();
                }
                return;
            }
            startY += buttonShape.getHeight()+8;
        }

        if (this.screenshot != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.screenshot.getBounds2D().getMinX()+startX, this.screenshot.getBounds2D().getBounds2D().getMinY()+startY, this.screenshot.getBounds2D().getWidth(), this.screenshot.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryStructure() == null)
                    return;
                final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(mediator.getAssemble().getLastWorkingDirectory());
                fileChooser.setFileHidingEnabled(true);
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().endsWith(".png");
                    }

                    @Override
                    public String getDescription() {
                        return "PNG Files (.png)";
                    }
                });
                if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(null)) {
                    new javax.swing.SwingWorker() {
                        protected Object doInBackground()  {
                            mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                            try {
                                int maxX = (int)(mediator.getSecondaryCanvas().getSecondaryStructure().getCurrentMaxX(mediator.getSecondaryCanvas().getGraphicContext(), mediator.getSecondaryStructure().getResidues())+2*mediator.getSecondaryCanvas().getGraphicContext().getCurrentWidth()),
                                        maxY =  (int)(mediator.getSecondaryCanvas().getSecondaryStructure().getCurrentMaxY(mediator.getSecondaryCanvas().getGraphicContext(), mediator.getSecondaryStructure().getResidues())+mediator.getSecondaryCanvas().getGraphicContext().getCurrentHeight());
                                if (maxY < mediator.getSecondaryCanvas().getMaxY())
                                    maxY = (int)(mediator.getSecondaryCanvas().getMaxY()+mediator.getSecondaryCanvas().getGraphicContext().getCurrentHeight());
                                BufferedImage bi = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_ARGB);
                                Graphics2D g2 = bi.createGraphics();
                                g2.setBackground(java.awt.Color.white);
                                g2.fillRect(0,0, maxX, maxY);
                                mediator.getSecondaryCanvas().getGraphicContext().setDrawInPNG(true); //to inactivate the drawing limitation to the drawingArea
                                mediator.getSecondaryCanvas().setDisplaySecondaryStructureToolBar(false);
                                mediator.getSecondaryCanvas().setDisplayTertiaryStructureToolBar(false);
                                mediator.getSecondaryCanvas().setDisplayActivityToolBar(false);
                                mediator.getSecondaryCanvas().paintComponent(g2);
                                mediator.getSecondaryCanvas().getGraphicContext().setDrawInPNG(false); //to reactivate the drawing limitation to the drawingArea
                                mediator.getSecondaryCanvas().setDisplaySecondaryStructureToolBar(true);
                                mediator.getSecondaryCanvas().setDisplayTertiaryStructureToolBar(true);
                                mediator.getSecondaryCanvas().setDisplayActivityToolBar(true);
                                ImageIO.write(bi, "PNG", fileChooser.getSelectedFile());
                                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Screenshot saved.", null, null);
                                mediator.getSecondaryCanvas().repaint();
                            }
                            catch (Exception e) {
                                mediator.getAssemble().getMessageBar().printException(e);
                            }
                            mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                            return null;
                        }
                    }.execute();
                }
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

    }

    public void mouseMoved(MouseEvent e, int startX, int startY) {
        if (this.locked2D != null) {
            final Rectangle2D buttonShape = new Rectangle2D.Double(this.locked2D.getBounds2D().getMinX()+startX, this.locked2D.getBounds2D().getBounds2D().getMinY()+startY, this.locked2D.getBounds2D().getWidth(), this.locked2D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overLocked2D) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overLocked2D) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Lock/unlock the 2D. If unlocked, the 2D can be edited.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overLocked2D = true;
            } else {
                if (overLocked2D)
                    mediator.getSecondaryCanvas().repaint();
                overLocked2D = false;
            }
            startY += buttonShape.getHeight()+10;
        }
        if (this.unlocked2D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.unlocked2D.getBounds2D().getMinX()+startX, this.unlocked2D.getBounds2D().getBounds2D().getMinY()+startY, this.unlocked2D.getBounds2D().getWidth(), this.unlocked2D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overUnLocked2D) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overUnLocked2D) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Lock/unlock the 2D. If unlocked, the 2D can be edited.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overUnLocked2D = true;
            } else {
                if (overUnLocked2D)
                    mediator.getSecondaryCanvas().repaint();
                overUnLocked2D = false;
            }
            startY += buttonShape.getHeight()+10;
        }
        if (this.centerView != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.centerView.getBounds2D().getMinX()+startX, this.centerView.getBounds2D().getBounds2D().getMinY()+startY, this.centerView.getBounds2D().getWidth(), this.centerView.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overCenterView) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overCenterView) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Center the full 2D or the current selection on the panel.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overCenterView = true;
            } else {
                if (overCenterView)
                    mediator.getSecondaryCanvas().repaint();
                overCenterView = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.fitToPage != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.fitToPage.getBounds2D().getMinX()+startX, this.fitToPage.getBounds2D().getBounds2D().getMinY()+startY, this.fitToPage.getBounds2D().getWidth(), this.fitToPage.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overFitToPage) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overFitToPage) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Align the 2D on the upper-left corner (useful to take a screenshot).");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overFitToPage = true;
            } else {
                if (overFitToPage)
                    mediator.getSecondaryCanvas().repaint();
                overFitToPage = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.flip != null) {
            final Rectangle2D buttonShape = new Rectangle2D.Double(this.flip.getBounds2D().getMinX()+startX, this.flip.getBounds2D().getBounds2D().getMinY()+startY, this.flip.getBounds2D().getWidth(), this.flip.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overFlip) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overFlip) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Flip the selected helix.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overFlip = true;
            } else {
                if (overFlip)
                    mediator.getSecondaryCanvas().repaint();
                overFlip = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.showInteractions != null) {
            final Rectangle2D buttonShape = new Rectangle2D.Double(this.showInteractions.getBounds2D().getMinX()+startX, this.showInteractions.getBounds2D().getBounds2D().getMinY()+startY, this.showInteractions.getBounds2D().getWidth(), this.showInteractions.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overShowInteractions) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overShowInteractions) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Reduce iteratively the amount of information displayed.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overShowInteractions = true;
            } else {
                if (overShowInteractions)
                    mediator.getSecondaryCanvas().repaint();
                overShowInteractions = false;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.reorganizeHelices != null) {
            final Rectangle2D buttonShape = new Rectangle2D.Double(this.reorganizeHelices.getBounds2D().getMinX()+startX, this.reorganizeHelices.getBounds2D().getBounds2D().getMinY()+startY, this.reorganizeHelices.getBounds2D().getWidth(), this.reorganizeHelices.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overReorganizeHelices) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overReorganizeHelices) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Plot automatically a non-overlapping 2D.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overReorganizeHelices = true;
            } else {
                if (overReorganizeHelices)
                    mediator.getSecondaryCanvas().repaint();
                overReorganizeHelices = false;
            }
            startY += buttonShape.getHeight()+5;
        }
        if (this.rendering != null) {
            final Rectangle2D buttonShape = new Rectangle2D.Double(this.rendering.getBounds2D().getMinX()+startX, this.rendering.getBounds2D().getBounds2D().getMinY()+startY, this.rendering.getBounds2D().getWidth(), this.rendering.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overRendering) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overRendering) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Alternate between several coloring schemes.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overRendering = true;
            } else {
                if (overRendering)
                    mediator.getSecondaryCanvas().repaint();
                overRendering = false;
            }
            startY += buttonShape.getHeight()+5;
        }
        if (this.linkQualitativeValues != null) {
            final Rectangle2D buttonShape = new Rectangle2D.Double(this.linkQualitativeValues.getBounds2D().getMinX()+startX, this.linkQualitativeValues.getBounds2D().getBounds2D().getMinY()+startY, this.linkQualitativeValues.getBounds2D().getWidth(), this.linkQualitativeValues.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overLinkQualitativeValues) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overLinkQualitativeValues) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Link qualitative values to the 2D.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overLinkQualitativeValues = true;
            } else {
                if (overLinkQualitativeValues)
                    mediator.getSecondaryCanvas().repaint();
                overLinkQualitativeValues = false;
            }
            startY += buttonShape.getHeight()+5;
        }
        if (this.linkQuantitativeValues != null) {
            final Rectangle2D buttonShape = new Rectangle2D.Double(this.linkQuantitativeValues.getBounds2D().getMinX()+startX, this.linkQuantitativeValues.getBounds2D().getBounds2D().getMinY()+startY, this.linkQuantitativeValues.getBounds2D().getWidth(), this.linkQuantitativeValues.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overLinkQuantitativeValues) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overLinkQuantitativeValues) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Link quantitative values to the 2D.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overLinkQuantitativeValues = true;
            } else {
                if (overLinkQuantitativeValues)
                    mediator.getSecondaryCanvas().repaint();
                overLinkQuantitativeValues = false;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.screenshot != null) {
            final Rectangle2D buttonShape = new Rectangle2D.Double(this.screenshot.getBounds2D().getMinX()+startX, this.screenshot.getBounds2D().getBounds2D().getMinY()+startY, this.screenshot.getBounds2D().getWidth(), this.screenshot.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overScreenshot) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overScreenshot) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Take a screenshot.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overScreenshot = true;
            } else {
                if (overScreenshot)
                    mediator.getSecondaryCanvas().repaint();
                overScreenshot = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (! overLocked2D && ! overUnLocked2D && ! overCenterView && !overFitToPage && ! overFlip && ! overShowInteractions && ! overReorganizeHelices && ! overRendering && ! overLinkQualitativeValues && ! overLinkQuantitativeValues && ! overScreenshot) {
            tooltip = null;
            this.mediator.getSecondaryCanvas().repaint();
        }


    }


}
