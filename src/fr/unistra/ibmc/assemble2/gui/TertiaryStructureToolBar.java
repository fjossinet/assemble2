package fr.unistra.ibmc.assemble2.gui;


import fr.unistra.ibmc.assemble2.gui.components.ToolTip;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.io.Modeling3DException;
import fr.unistra.ibmc.assemble2.io.computations.Rnart;
import fr.unistra.ibmc.assemble2.io.drivers.ChimeraDriver;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.*;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;

public class TertiaryStructureToolBar implements ToolBar {
    private Shape newChimera,
            trash,
            create,
            refine,
            erase3DModel,
            confirm3DModel,
            clipResidues3D,
            color3D,
            setFocus,
            setPivot;
    private boolean overTrash, overNewChimera, overCreate, overErase3DModel, overConfirm3DModel, overRefine, overClipResidues3D, overColor3D, overSetFocus, overSetPivot;
    private Mediator mediator;
    private ToolTip tooltip;

    public TertiaryStructureToolBar(Mediator mediator) {
        this.mediator = mediator;
        try {
            this.newChimera = new SvgPath("M5.896,5.333V21.25h23.417V5.333H5.896zM26.312,18.25H8.896V8.334h17.417V18.25L26.312,18.25zM4.896,9.542H1.687v15.917h23.417V22.25H4.896V9.542z").getShape();
            this.create = new SvgPath("M7.831,29.354c0.685,0.353,1.62,1.178,2.344,0.876c0.475-0.195,0.753-1.301,1.048-1.883c2.221-4.376,4.635-9.353,6.392-13.611c0-0.19,0.101-0.337-0.049-0.595c0.983-1.6,1.65-3.358,2.724-5.138c0.34-0.566,0.686-1.351,1.163-1.577l0.881-0.368c1.12-0.288,1.938-0.278,2.719,0.473c0.396,0.383,0.578,1.015,0.961,1.395c0.259,0.26,1.246,0.899,1.613,0.8c0.285-0.077,0.52-0.364,0.72-0.728l0.696-1.286c0.195-0.366,0.306-0.718,0.215-0.999c-0.117-0.362-1.192-0.84-1.552-0.915c-0.528-0.113-1.154,0.081-1.692-0.041c-1.057-0.243-1.513-0.922-1.883-2.02c-2.608-1.533-6.119-2.53-10.207-1.244c-1.109,0.349-2.172,0.614-2.901,1.323c-0.146,0.412,0.143,0.494,0.446,0.489c-0.237,0.216-0.62,0.341-0.399,0.848c2.495-1.146,7.34-1.542,7.669,0.804c0.072,0.522-0.395,1.241-0.682,1.835c-0.905,1.874-2.011,3.394-2.813,5.091c-0.298,0.017-0.366,0.18-0.525,0.287c-2.604,3.8-5.451,8.541-7.9,12.794c-0.326,0.566-1.098,1.402-1.002,1.906C5.961,28.641,7.146,29,7.831,29.354z").getShape();
            this.trash = new SvgPath("M16,3.667C9.189,3.667,3.667,9.188,3.667,16S9.189,28.333,16,28.333c6.812,0,12.333-5.521,12.333-12.333S22.812,3.667,16,3.667zM16,6.667c1.851,0,3.572,0.548,5.024,1.48L8.147,21.024c-0.933-1.452-1.48-3.174-1.48-5.024C6.667,10.854,10.854,6.667,16,6.667zM16,25.333c-1.85,0-3.572-0.548-5.024-1.48l12.876-12.877c0.933,1.452,1.48,3.174,1.48,5.024C25.333,21.146,21.146,25.333,16,25.333z").getShape();
            this.confirm3DModel = new SvgPath("M16,1.466C7.973,1.466,1.466,7.973,1.466,16c0,8.027,6.507,14.534,14.534,14.534c8.027,0,14.534-6.507,14.534-14.534C30.534,7.973,24.027,1.466,16,1.466zM20.729,7.375c0.934,0,1.688,1.483,1.688,3.312S21.661,14,20.729,14c-0.932,0-1.688-1.483-1.688-3.312S19.798,7.375,20.729,7.375zM11.104,7.375c0.932,0,1.688,1.483,1.688,3.312S12.037,14,11.104,14s-1.688-1.483-1.688-3.312S10.172,7.375,11.104,7.375zM16.021,26c-2.873,0-5.563-1.757-7.879-4.811c2.397,1.564,5.021,2.436,7.774,2.436c2.923,0,5.701-0.98,8.215-2.734C21.766,24.132,18.99,26,16.021,26z").getShape();
            this.refine = new SvgPath("M17.41,20.395l-0.778-2.723c0.228-0.2,0.442-0.414,0.644-0.643l2.721,0.778c0.287-0.418,0.534-0.862,0.755-1.323l-2.025-1.96c0.097-0.288,0.181-0.581,0.241-0.883l2.729-0.684c0.02-0.252,0.039-0.505,0.039-0.763s-0.02-0.51-0.039-0.762l-2.729-0.684c-0.061-0.302-0.145-0.595-0.241-0.883l2.026-1.96c-0.222-0.46-0.469-0.905-0.756-1.323l-2.721,0.777c-0.201-0.228-0.416-0.442-0.644-0.643l0.778-2.722c-0.418-0.286-0.863-0.534-1.324-0.755l-1.96,2.026c-0.287-0.097-0.581-0.18-0.883-0.241l-0.683-2.73c-0.253-0.019-0.505-0.039-0.763-0.039s-0.51,0.02-0.762,0.039l-0.684,2.73c-0.302,0.061-0.595,0.144-0.883,0.241l-1.96-2.026C7.048,3.463,6.604,3.71,6.186,3.997l0.778,2.722C6.736,6.919,6.521,7.134,6.321,7.361L3.599,6.583C3.312,7.001,3.065,7.446,2.844,7.907l2.026,1.96c-0.096,0.288-0.18,0.581-0.241,0.883l-2.73,0.684c-0.019,0.252-0.039,0.505-0.039,0.762s0.02,0.51,0.039,0.763l2.73,0.684c0.061,0.302,0.145,0.595,0.241,0.883l-2.026,1.96c0.221,0.46,0.468,0.905,0.755,1.323l2.722-0.778c0.2,0.229,0.415,0.442,0.643,0.643l-0.778,2.723c0.418,0.286,0.863,0.533,1.323,0.755l1.96-2.026c0.288,0.097,0.581,0.181,0.883,0.241l0.684,2.729c0.252,0.02,0.505,0.039,0.763,0.039s0.51-0.02,0.763-0.039l0.683-2.729c0.302-0.061,0.596-0.145,0.883-0.241l1.96,2.026C16.547,20.928,16.992,20.681,17.41,20.395zM11.798,15.594c-1.877,0-3.399-1.522-3.399-3.399s1.522-3.398,3.399-3.398s3.398,1.521,3.398,3.398S13.675,15.594,11.798,15.594zM27.29,22.699c0.019-0.547-0.06-1.104-0.23-1.654l1.244-1.773c-0.188-0.35-0.4-0.682-0.641-0.984l-2.122,0.445c-0.428-0.364-0.915-0.648-1.436-0.851l-0.611-2.079c-0.386-0.068-0.777-0.105-1.173-0.106l-0.974,1.936c-0.279,0.054-0.558,0.128-0.832,0.233c-0.257,0.098-0.497,0.22-0.727,0.353L17.782,17.4c-0.297,0.262-0.568,0.545-0.813,0.852l0.907,1.968c-0.259,0.495-0.437,1.028-0.519,1.585l-1.891,1.06c0.019,0.388,0.076,0.776,0.164,1.165l2.104,0.519c0.231,0.524,0.541,0.993,0.916,1.393l-0.352,2.138c0.32,0.23,0.66,0.428,1.013,0.6l1.715-1.32c0.536,0.141,1.097,0.195,1.662,0.15l1.452,1.607c0.2-0.057,0.399-0.118,0.596-0.193c0.175-0.066,0.34-0.144,0.505-0.223l0.037-2.165c0.455-0.339,0.843-0.747,1.152-1.206l2.161-0.134c0.152-0.359,0.279-0.732,0.368-1.115L27.29,22.699zM23.127,24.706c-1.201,0.458-2.545-0.144-3.004-1.345s0.143-2.546,1.344-3.005c1.201-0.458,2.547,0.144,3.006,1.345C24.931,22.902,24.328,24.247,23.127,24.706z").getShape();
            this.erase3DModel = new SvgPath("M25.947,11.14c0-5.174-3.979-9.406-10.613-9.406c-6.633,0-10.282,4.232-10.282,9.406c0,5.174,1.459,4.511,1.459,7.43c0,1.095-1.061,0.564-1.061,2.919c0,2.587,3.615,2.223,4.677,3.283c1.061,1.062,0.961,3.019,0.961,3.019s0.199,0.796,0.564,0.563c0,0,0.232,0.564,0.498,0.232c0,0,0.265,0.563,0.531,0.1c0,0,0.265,0.631,0.696,0.166c0,0,0.431,0.63,0.929,0.133c0,0,0.564,0.53,1.194,0.133c0.63,0.397,1.194-0.133,1.194-0.133c0.497,0.497,0.929-0.133,0.929-0.133c0.432,0.465,0.695-0.166,0.695-0.166c0.268,0.464,0.531-0.1,0.531-0.1c0.266,0.332,0.498-0.232,0.498-0.232c0.365,0.232,0.564-0.563,0.564-0.563s-0.1-1.957,0.961-3.019c1.062-1.061,4.676-0.696,4.676-3.283c0-2.354-1.061-1.824-1.061-2.919C24.488,15.651,25.947,16.314,25.947,11.14zM10.333,20.992c-1.783,0.285-2.59-0.215-2.785-1.492c-0.508-3.328,2.555-3.866,4.079-3.683c0.731,0.088,1.99,0.862,1.99,1.825C13.617,20.229,11.992,20.727,10.333,20.992zM16.461,25.303c-0.331,0-0.862-0.431-0.895-1.227c-0.033,0.796-0.63,1.227-0.961,1.227c-0.332,0-0.83-0.331-0.863-1.127c-0.033-0.796,1.028-4.013,1.792-4.013c0.762,0,1.824,3.217,1.791,4.013S16.794,25.303,16.461,25.303zM23.361,19.5c-0.195,1.277-1.004,1.777-2.787,1.492c-1.658-0.266-3.283-0.763-3.283-3.35c0-0.963,1.258-1.737,1.99-1.825C20.805,15.634,23.869,16.172,23.361,19.5z").getShape();
            this.clipResidues3D = new SvgPath("M20.812,19.5h5.002v-6.867c-0.028-1.706-0.61-3.807-2.172-5.841c-1.539-2.014-4.315-3.72-7.939-3.687C12.076,3.073,9.3,4.779,7.762,6.792C6.2,8.826,5.617,10.928,5.588,12.634V19.5h5v-6.866c-0.027-0.377,0.303-1.789,1.099-2.748c0.819-0.979,1.848-1.747,4.014-1.778c2.165,0.032,3.195,0.799,4.013,1.778c0.798,0.959,1.126,2.372,1.099,2.748V19.5L20.812,19.5zM25.814,25.579c0,0,0-2.354,0-5.079h-5.002c0,2.727,0,5.08,0,5.08l5.004-0.001H25.814zM5.588,25.58h5c0,0,0-2.354,0-5.08h-5C5.588,23.227,5.588,25.58,5.588,25.58z").getShape();
            this.color3D = new SvgPath("M8.125,29.178l1.311-1.5l1.315,1.5l1.311-1.5l1.311,1.5l1.315-1.5l1.311,1.5l1.312-1.5l1.314,1.5l1.312-1.5l1.312,1.5l1.314-1.5l1.312,1.5v-8.521H8.125V29.178zM23.375,17.156c-0.354,0-5.833-0.166-5.833-2.917s0.75-8.834,0.75-8.834S18.542,2.822,16,2.822s-2.292,2.583-2.292,2.583s0.75,6.083,0.75,8.834s-5.479,2.917-5.833,2.917c-0.5,0-0.5,1.166-0.5,1.166v1.271h15.75v-1.271C23.875,18.322,23.875,17.156,23.375,17.156zM16,8.031c-0.621,0-1.125-2.191-1.125-2.812S15.379,4.094,16,4.094s1.125,0.504,1.125,1.125S16.621,8.031,16,8.031z").getShape();
            this.setFocus = new SvgPath("M25.083,18.895l-8.428-2.259l2.258,8.428l1.838-1.837l7.053,7.053l2.476-2.476l-7.053-7.053L25.083,18.895zM5.542,11.731l8.428,2.258l-2.258-8.428L9.874,7.398L3.196,0.72L0.72,3.196l6.678,6.678L5.542,11.731zM7.589,20.935l-6.87,6.869l2.476,2.476l6.869-6.869l1.858,1.857l2.258-8.428l-8.428,2.258L7.589,20.935zM23.412,10.064l6.867-6.87l-2.476-2.476l-6.868,6.869l-1.856-1.856l-2.258,8.428l8.428-2.259L23.412,10.064z").getShape();
            this.setPivot = new SvgPath("M24.083,15.5c-0.009,4.739-3.844,8.574-8.583,8.583c-4.741-0.009-8.577-3.844-8.585-8.583c0.008-4.741,3.844-8.577,8.585-8.585c1.913,0,3.665,0.629,5.09,1.686l-1.782,1.783l8.429,2.256l-2.26-8.427l-1.89,1.89c-2.072-1.677-4.717-2.688-7.587-2.688C8.826,3.418,3.418,8.826,3.416,15.5C3.418,22.175,8.826,27.583,15.5,27.583S27.583,22.175,27.583,15.5H24.083z").getShape();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2, int startX, int startY) {
        if (tooltip != null)
            tooltip.draw(g2);
        if (this.newChimera != null) {
            Rectangle2D buttonShape = this.newChimera.getBounds2D();
            g2.translate(startX, startY);
            if (overNewChimera) {
                Stroke s = g2.getStroke();
                g2.setStroke(new BasicStroke(5));
                g2.setColor(iconHighlight);
                g2.draw(this.newChimera);
                g2.setStroke(s);
            }
            g2.setColor(Color.BLACK);
            g2.fill(this.newChimera);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+10;
        }

        if (this.create != null) {
            Rectangle2D buttonShape = this.create.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overCreate) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.create);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.create);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }

        if (this.trash != null) {
            Rectangle2D buttonShape = this.trash.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getTertiaryStructure() == null || mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overTrash) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.trash);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.trash);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+10;
        }

        if (this.confirm3DModel != null) {
            Rectangle2D buttonShape = this.confirm3DModel.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getTertiaryStructure() == null)
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overConfirm3DModel) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.confirm3DModel);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.confirm3DModel);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }

        if (this.erase3DModel != null) {
            Rectangle2D buttonShape = this.erase3DModel.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getTertiaryStructure() == null)
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overErase3DModel) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.erase3DModel);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.erase3DModel);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }

        if (this.clipResidues3D != null) {
            Rectangle2D buttonShape = this.clipResidues3D.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getTertiaryStructure() == null || mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overClipResidues3D) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.clipResidues3D);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.clipResidues3D);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }

        if (this.refine != null) {
            Rectangle2D buttonShape = this.refine.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getTertiaryStructure() == null || mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overRefine) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.refine);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.refine);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }
        if (this.color3D != null) {
            Rectangle2D buttonShape = this.color3D.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getTertiaryStructure() == null)
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overColor3D) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.color3D);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.color3D);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+10;
        }
        if (this.setFocus != null) {
            Rectangle2D buttonShape = this.setFocus.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getTertiaryStructure() == null)
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overSetFocus) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.setFocus);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.setFocus);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }
        if (this.setPivot != null) {
            Rectangle2D buttonShape = this.setPivot.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getChimeraDriver() == null || mediator.getTertiaryStructure() == null || mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overSetPivot) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.setPivot);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.setPivot);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }

    }

    public void mouseClicked(MouseEvent e, int startX, int startY) {

        if (this.newChimera != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.newChimera.getBounds2D().getMinX()+startX, this.newChimera.getBounds2D().getBounds2D().getMinY()+startY, this.newChimera.getBounds2D().getWidth(), this.newChimera.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Do you really want to open a new Chimera as the default 3D viewer?")) {
                    if (mediator.getChimeraDriver() != null)
                        mediator.getChimeraDriver().close();
                    new ChimeraDriver(mediator);
                    mediator.getSecondaryCanvas().repaint(); //to activate some icons if there was no Chimera driver so far...
                    try {
                        if (mediator.getAssemble().getCurrentAssembleProject() != null && mediator.getChimeraDriver() != null) {
                            mediator.getChimeraDriver().restoreSession(mediator.getAssemble().getCurrentAssembleProject().getChimeraSession());
                            mediator.getChimeraDriver().synchronizeFrom();
                        }
                        else if (mediator.getTertiaryStructure() != null && mediator.getChimeraDriver() != null)
                            mediator.getChimeraDriver().synchronizeTo();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                //mediator.getSecondaryCanvas().demoMode();
                return;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.create != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.create.getBounds2D().getMinX()+startX, this.create.getBounds2D().getBounds2D().getMinY()+startY, this.create.getBounds2D().getWidth(), this.create.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                    return;
                try {
                    List<Residue3D> computedResidues = null;
                    boolean firstFragment = false;
                    if (mediator.getTertiaryStructure() == null) {
                        TertiaryStructure ts = new TertiaryStructure(mediator.getSecondaryStructure().getMolecule());
                        mediator.setTertiaryStructure(ts);
                        mediator.getSecondaryStructure().setLinkedTs(ts);
                        firstFragment = true;
                    } else if (mediator.getTertiaryStructure().getResidues3D().isEmpty()) {
                        firstFragment = true;
                    }
                    List<Residue3D> previousResidues = mediator.getTertiaryStructure().getResidues3D();
                    TertiaryStructure ts = mediator.getTertiaryStructure();
                    if (mediator.getSecondaryCanvas().getSelectedHelix() != null) {  // the fold is generated de novo
                        computedResidues = fr.unistra.ibmc.assemble2.utils.Modeling3DUtils.compute3DHelix(mediator, ts,mediator.getSecondaryCanvas().getSelectedHelix().getLocation());
                        if (computedResidues.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Problem to generate the new 3D residues!!");
                            return;
                        }
                        File tmpPDB = IoUtils.createTemporaryFile("model.pdb");
                        try {
                            Collections.sort(computedResidues, new Comparator<Residue3D>() {
                                public int compare(Residue3D residue, Residue3D residue1) {
                                    return residue.getAbsolutePosition() - residue1.getAbsolutePosition();
                                }
                            });
                            FileParser.writePDBFile(computedResidues, false, new FileWriter(tmpPDB));
                            int anchorResidue1 = -1,anchorResidue2 = -1;
                            Residue[] _5PrimeEnds = mediator.getSecondaryCanvas().getSelectedHelix().get5PrimeEnds();
                            int previous = _5PrimeEnds[0].getAbsolutePosition();
                            int pairedPrevious = mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(previous)) != null ? mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(previous)).getAbsolutePosition() : -1;
                            for (Residue3D previousRes: previousResidues) {
                                if (previousRes.getAbsolutePosition() == previous)
                                    anchorResidue1 = previous;
                                else if  (previousRes.getAbsolutePosition() == pairedPrevious)
                                    anchorResidue2 = pairedPrevious;
                            }

                            if (anchorResidue1 == -1 && anchorResidue2 == -1) {
                                previous = _5PrimeEnds[1].getAbsolutePosition();
                                pairedPrevious = mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(previous)) != null ? mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(previous)).getAbsolutePosition() : -1;
                                for (Residue3D previousRes:previousResidues) {
                                    if (previousRes.getAbsolutePosition() == previous)
                                        anchorResidue1 = previous;
                                    else if  (previousRes.getAbsolutePosition() == pairedPrevious)
                                        anchorResidue2 = pairedPrevious;
                                }
                            }

                            if (mediator.getChimeraDriver() != null && (anchorResidue1 == -1 && anchorResidue2 == -1 && firstFragment || anchorResidue1 != -1 || anchorResidue2 != -1))
                                mediator.getChimeraDriver().addFragment(tmpPDB, computedResidues, anchorResidue1, anchorResidue2, firstFragment); //even if no anchor residues found, we generate the helix.
                            else if (anchorResidue1 == -1 && anchorResidue2 == -1) {
                                StringBuffer residuesNeeded = new StringBuffer();
                                if (_5PrimeEnds[0].getAbsolutePosition()-1 > 0)
                                    residuesNeeded.append(_5PrimeEnds[0].getAbsolutePosition() - 1 + ", ");
                                if (previous > 0)
                                    residuesNeeded.append(previous + ", ");
                                int pos = mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(_5PrimeEnds[0].getAbsolutePosition())).getAbsolutePosition()+1;
                                if (pos <= mediator.getSecondaryStructure().getMolecule().size())
                                    residuesNeeded.append(pos + ", ");
                                if (pairedPrevious <= mediator.getSecondaryStructure().getMolecule().size())
                                    residuesNeeded.append(pairedPrevious);
                                if (residuesNeeded.charAt(residuesNeeded.length()-1) == ',')
                                    residuesNeeded.deleteCharAt(residuesNeeded.length()-1);
                                JOptionPane.showMessageDialog(null, "You need at least residues n"+residuesNeeded.toString()+" in the 3D scene before to create your helix.");
                                //we remove the residues computed
                                for (Residue3D r:computedResidues)
                                    mediator.getTertiaryStructure().removeResidue3D(r.getAbsolutePosition());
                                return;
                            }
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    else if (mediator.getSecondaryCanvas().getSelectedJunction() != null) {  // the fold selected is cloned
                        if (mediator.getTertiaryFragmentsPanel().getSelectedRows().length != 1) {
                            JOptionPane.showMessageDialog(null,"You have to make a choice in the lateral panel named \"3D Folds\"");
                            return;
                        }
                        computedResidues = mediator.getRna2DViewer().loadJunctionHit((TertiaryFragmentHit)mediator.getTertiaryFragmentsPanel().getValueAt(mediator.getTertiaryFragmentsPanel().getSelectedRows()[0],1),mediator.getTertiaryStructure());
                        int anchorResidue1 = -1,anchorResidue2 = -1;
                        List<Integer> positionsNeeded = new ArrayList<Integer>();
                        List<MutablePair<Molecule,Location>> fragments = mediator.getSecondaryCanvas().getSelectedJunction().getFragments();
                        for (int i = 0 ; i< fragments.size() ; i++)  {
                            MutablePair<Molecule,Location> fragment = fragments.get(i);
                            positionsNeeded.add(fragment.getRight().getStart());
                            positionsNeeded.add(fragment.getRight().getEnd());
                        }
                        for (int positionNeeded: positionsNeeded) {
                            Residue pairedResidue = mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(positionNeeded));
                            if (pairedResidue == null)
                                continue;
                            int pairedPrevious = pairedResidue.getAbsolutePosition();
                            for (Residue3D previousRes:previousResidues) { //we search which residues in the new ones are already in the 3D scene
                                if (previousRes.getAbsolutePosition() == positionNeeded) {
                                    anchorResidue1 = positionNeeded;
                                }
                                else if  (previousRes.getAbsolutePosition() == pairedPrevious) {
                                    anchorResidue2 = pairedPrevious;
                                }
                            }
                            if (anchorResidue1 != -1 && anchorResidue2 != -1) { //if we found paired anchorResidues, we stop. This is what we needed.
                                break;
                            }
                        }

                        if (anchorResidue1 == -1 && anchorResidue2 == -1) {
                            StringBuffer residuesNeeded = new StringBuffer();
                            fragments = mediator.getSecondaryCanvas().getSelectedJunction().getFragments();
                            for (int i = 0 ; i< fragments.size()-1 ; i++)  {
                                MutablePair<Molecule,Location> fragment = fragments.get(i);
                                residuesNeeded.append(fragment.getRight().getStart()+", "+fragment.getRight().getEnd()+", ");
                            }
                            residuesNeeded.append(fragments.get(fragments.size()-1).getRight().getStart()+" or "+fragments.get(fragments.size()-1).getRight().getEnd());
                            JOptionPane.showMessageDialog(null, "You need at least residue n"+residuesNeeded.toString()+" in the 3D scene before to create your junction.");
                            //we remove the residues computed
                            for (Residue3D r:computedResidues)
                                mediator.getTertiaryStructure().removeResidue3D(r.getAbsolutePosition());
                            return;
                        }

                        try {
                            if (mediator.getChimeraDriver() != null)  {
                                File tmpPDB = IoUtils.createTemporaryFile("model.pdb");
                                Collections.sort(computedResidues,new Comparator<Residue3D>() {
                                    public int compare(Residue3D residue, Residue3D residue1) {
                                        return residue.getAbsolutePosition()-residue1.getAbsolutePosition();
                                    }
                                });
                                FileParser.writePDBFile(computedResidues, false, new FileWriter(tmpPDB));
                                mediator.getChimeraDriver().addFragment(tmpPDB, computedResidues, anchorResidue1, anchorResidue2, firstFragment);
                            }
                        }
                        catch (Modeling3DException ex) {
                            if (ex.getStatus() == Modeling3DException.MISSING_ATOMS_IN_MOTIFS)
                                JOptionPane.showMessageDialog(null,"Cannot apply the 3D fold. Some atoms are missing.");
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    else if (mediator.getSecondaryCanvas().getSelectedSingleStrand() != null && !mediator.getSecondaryCanvas().getSelectedSingleStrand().isAtFivePrimeEnd() && !mediator.getSecondaryCanvas().getSelectedSingleStrand().isAtThreePrimeEnd()  && mediator.getTertiaryFragmentsPanel().getSelectedRows().length == 1) { //the fold selected is cloned
                        try {
                            computedResidues = mediator.getRna2DViewer().loadJunctionHit((TertiaryFragmentHit)mediator.getTertiaryFragmentsPanel().getValueAt(mediator.getTertiaryFragmentsPanel().getSelectedRows()[0],1),mediator.getTertiaryStructure());
                            int anchorResidue1 = -1,anchorResidue2 = -1;
                            int previous = mediator.getSecondaryCanvas().getSelectedSingleStrand().getBase5().getAbsolutePosition()-1;
                            Residue pairedResidue = mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(previous));
                            if (pairedResidue != null) {
                                int pairedPrevious = pairedResidue.getAbsolutePosition();
                                for (Residue3D previousRes:previousResidues) { //we search which residues in the new ones are already in the 3D scene
                                    if (previousRes.getAbsolutePosition() == previous) {
                                        anchorResidue1 = previous;
                                    }
                                    else if  (previousRes.getAbsolutePosition() == pairedPrevious) {
                                        anchorResidue2 = pairedPrevious;
                                    }
                                }
                            }

                            if (anchorResidue1 == -1) {
                                JOptionPane.showMessageDialog(null, "You need residue n"+previous+" in the 3D scene before to create your single-strand.");
                                //we remove the residues computed
                                for (Residue3D r:computedResidues)
                                    mediator.getTertiaryStructure().removeResidue3D(r.getAbsolutePosition());
                                return;
                            }
                            if (mediator.getChimeraDriver() != null) {
                                File tmpPDB = IoUtils.createTemporaryFile("model.pdb");
                                Collections.sort(computedResidues,new Comparator<Residue3D>() {
                                    public int compare(Residue3D residue, Residue3D residue1) {
                                        return residue.getAbsolutePosition()-residue1.getAbsolutePosition();
                                    }
                                });
                                FileParser.writePDBFile(computedResidues, false, new FileWriter(tmpPDB));
                                mediator.getChimeraDriver().addFragment(tmpPDB, computedResidues, anchorResidue1, -1, firstFragment);
                            }
                        }
                        catch (Modeling3DException ex) {
                            if (ex.getStatus() == Modeling3DException.MISSING_ATOMS_IN_MOTIFS)
                                JOptionPane.showMessageDialog(null,"Cannot apply the 3D fold. Some atoms are missing.");
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    else if (mediator.getSecondaryCanvas().getSelectedSingleStrand() != null && mediator.getTertiaryFragmentsPanel().getSelectedRows().length == 0) { //the single-strand is generated de novo
                        if (mediator.getSecondaryCanvas().getSelectedSingleStrand().isAtFivePrimeEnd())
                            computedResidues = Modeling3DUtils.compute3DSingleStrand(mediator, ts,new Location(mediator.getSecondaryCanvas().getSelectedSingleStrand().getLocation().getStart(),mediator.getSecondaryCanvas().getSelectedSingleStrand().getLocation().getEnd()+1));
                        else if (mediator.getSecondaryCanvas().getSelectedSingleStrand().isAtThreePrimeEnd())
                            computedResidues = Modeling3DUtils.compute3DSingleStrand(mediator, ts,new Location(mediator.getSecondaryCanvas().getSelectedSingleStrand().getLocation().getStart()-1,mediator.getSecondaryCanvas().getSelectedSingleStrand().getLocation().getEnd()));
                        else
                            computedResidues = Modeling3DUtils.compute3DSingleStrand(mediator, ts,new Location(mediator.getSecondaryCanvas().getSelectedSingleStrand().getLocation().getStart()-1,mediator.getSecondaryCanvas().getSelectedSingleStrand().getLocation().getEnd()+1));

                        int anchorResidue1 = -1,anchorResidue2 = -1;
                        int previous = mediator.getSecondaryCanvas().getSelectedSingleStrand().getBase5().getAbsolutePosition()-1,
                                after = mediator.getSecondaryCanvas().getSelectedSingleStrand().getBase3().getAbsolutePosition()+1;
                        for (Residue3D previousRes:previousResidues) { //we search which residues in the previous ones are already in the 3D scene
                            if (previousRes.getAbsolutePosition() == previous) {
                                anchorResidue1 = previous;
                            }
                            else if  (previousRes.getAbsolutePosition() == after) {
                                anchorResidue2 = after;
                            }
                        }

                        if (anchorResidue1 == -1) {
                            JOptionPane.showMessageDialog(null, "You need at least residue n"+previous+" in the 3D scene before to create your single-strand.");
                            //we remove the residues computed
                            for (Residue3D r:computedResidues)
                                mediator.getTertiaryStructure().removeResidue3D(r.getAbsolutePosition());
                            return;
                        }

                        if (computedResidues.isEmpty()) {
                            JOptionPane.showMessageDialog(null,"Problem to generate the new 3D residues!!");
                            return;
                        }
                        if (mediator.getChimeraDriver() != null) {
                            File tmpPDB = IoUtils.createTemporaryFile("model.pdb");
                            try {
                                Collections.sort(computedResidues, new Comparator<Residue3D>() {
                                    public int compare(Residue3D residue, Residue3D residue1) {
                                        return residue.getAbsolutePosition() - residue1.getAbsolutePosition();
                                    }
                                });
                                FileParser.writePDBFile(computedResidues, false, new FileWriter(tmpPDB));
                                mediator.getChimeraDriver().addFragment(tmpPDB,computedResidues,anchorResidue1, -1 ,firstFragment);
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    else if (mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction() != null) {
                        int current = mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getLocation().getStart(), currentPaired = mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getLocation().getEnd();
                        Residue3D current3D = null, currentPaired3D = null;
                        computedResidues = new ArrayList<Residue3D>();
                        for (Residue3D previousRes:previousResidues) { //we search which residues in the new ones are already in the 3D scene
                            if (previousRes.getAbsolutePosition() == current) {
                                current3D = previousRes;
                            }
                            if (previousRes.getAbsolutePosition() == currentPaired) {
                                currentPaired3D = previousRes;
                            }
                        }

                        if (current3D == null || currentPaired3D == null ) {
                            JOptionPane.showMessageDialog(null, "You need to have the two residues of the interaction in the 3D scene before to change it.");
                            return;
                        }
                        char symbol = '?', pairedSymbol = '?';
                        switch (mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getEdge(mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getResidue())) {
                            case '(': symbol= 'W'; break;
                            case '[': symbol= 'H'; break;
                            case '{': symbol= 'S'; break;
                        }
                        switch (mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getEdge(mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getPartnerResidue())) {
                            case ')': pairedSymbol= 'W'; break;
                            case ']': pairedSymbol= 'H'; break;
                            case '}': pairedSymbol= 'S'; break;
                        }
                        String typeForSearch =   mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getResidue().getSymbol()+""+
                                mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getOrientation()+""+
                                mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getEdge(mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getResidue())+""+
                                mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getEdge(mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getPartnerResidue())+""+
                                mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getPartnerResidue().getSymbol();
                        String type =
                                mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getResidue().getSymbol()+"-"+
                                        Character.toLowerCase(mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getOrientation())+""+
                                        symbol+""+pairedSymbol+"-"+mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getPartnerResidue().getSymbol();

                        Residue3D[] residues3D = Modeling3DUtils2.getAtomsForLWReferenceBasePair(mediator.getAssemble(), type);
                        if (residues3D[0] == null && residues3D[1] == null)
                            residues3D = Modeling3DUtils3.getAtomsForLWReferenceBasePair(mediator.getAssemble(), typeForSearch);
                        if (residues3D[0] == null && residues3D[1] == null)
                            residues3D = Modeling3DUtils4.getAtomsForLWReferenceBasePair(mediator.getAssemble(), typeForSearch);
                        if (residues3D[0] == null && residues3D[1] == null)
                            residues3D = Modeling3DUtils5.getAtomsForLWReferenceBasePair(mediator.getAssemble(), typeForSearch);
                        if (residues3D[0] != null && residues3D[1] != null) {
                            List<Residue3D> referenceResidues = new ArrayList<Residue3D>();
                            referenceResidues.add(residues3D[0]);
                            computedResidues.add(fr.unistra.ibmc.assemble2.utils.Modeling3DUtils.thread(mediator, ts,mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getResidue().getAbsolutePosition(),1,Modeling3DUtils.RNA,referenceResidues).get(0));
                            referenceResidues.clear();
                            referenceResidues.add(residues3D[1]);
                            computedResidues.add(fr.unistra.ibmc.assemble2.utils.Modeling3DUtils.thread(mediator, ts,mediator.getSecondaryCanvas().getSelectedBaseBaseInteraction().getPartnerResidue().getAbsolutePosition(),1,Modeling3DUtils.RNA,referenceResidues).get(0));

                            if (mediator.getChimeraDriver() != null) {
                                File tmpPDB = IoUtils.createTemporaryFile("model.pdb");
                                FileParser.writePDBFile(computedResidues, false, new FileWriter(tmpPDB));
                                mediator.getChimeraDriver().substituteBaseBaseInteraction(tmpPDB, computedResidues);
                            }
                        }
                        else {
                            JOptionPane.showMessageDialog(null,"Interaction "+type+" not available in the database");
                            return;
                        }
                    }
                    else if (mediator.getSecondaryCanvas().getSelectedResidues().size() == 1) {
                        Residue r = mediator.getSecondaryCanvas().getSelectedResidues().get(0);

                        Residue3D magnetResidue3D = mediator.getTertiaryStructure().getResidue3DAt(r.getAbsolutePosition()-1);
                        if (magnetResidue3D == null) {
                            JOptionPane.showMessageDialog(null, "You need residue n"+(r.getAbsolutePosition()-1)+" in the 3D scene before to create residue n"+r.getAbsolutePosition()+".");
                            return;
                        }
                        computedResidues = new ArrayList<Residue3D>();
                        computedResidues.add(fr.unistra.ibmc.assemble2.utils.Modeling3DUtils.compute3DResidue(mediator, ts, r));
                        if (computedResidues.isEmpty()) {
                            JOptionPane.showMessageDialog(null,"Problem to generate the 3D residue!!");
                            return;
                        }

                        List<Residue3D> selectedResidues = new ArrayList<Residue3D>();
                        selectedResidues.add(computedResidues.get(0));

                        Matrix q = Modeling3DUtils.computeTransformationMatrixToMoveAt3PrimeEnd(magnetResidue3D, computedResidues.get(0));
                        for (Residue3D.Atom atom : computedResidues.get(0).getAtoms()) {
                            if (atom.hasCoordinatesFilled()) {
                                float x = atom.getCoordinates()[0];
                                float y = atom.getCoordinates()[1];
                                float z = atom.getCoordinates()[2];

                                double x2 = x * q.get(0, 0) + y * q.get(0, 1) + z * q.get(0, 2) + q.get(0, 3);
                                double y2 = x * q.get(1, 0) + y * q.get(1, 1) + z * q.get(1, 2) + q.get(1, 3);
                                double z2 = x * q.get(2, 0) + y * q.get(2, 1) + z * q.get(2, 2) + q.get(2, 3);
                                atom.setCoordinates((float) x2, (float) y2, (float) z2);
                            }
                        }
                        selectedResidues.add(0, magnetResidue3D);

                        if (mediator.getChimeraDriver() != null) {
                            File tmpPDB = IoUtils.createTemporaryFile("model.pdb");
                            FileParser.writePDBFile(selectedResidues, false, new FileWriter(tmpPDB));
                            mediator.getChimeraDriver().addFragment(tmpPDB, selectedResidues, magnetResidue3D.getAbsolutePosition(), -1, firstFragment);
                        }
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.trash != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.trash.getBounds2D().getMinX()+startX, this.trash.getBounds2D().getBounds2D().getMinY()+startY, this.trash.getBounds2D().getWidth(), this.trash.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                    return;
                java.util.List<Integer> selectedPositions = new ArrayList<Integer>();
                for (String position:mediator.getChimeraDriver().getLastSelectedResidues()) {
                    int pos = Integer.parseInt(position);
                    selectedPositions.add(pos);
                    if ( mediator.getTertiaryStructure() != null)
                        mediator.getTertiaryStructure().removeResidue3D(pos);
                }
                if (mediator.getChimeraDriver() != null)
                    mediator.getChimeraDriver().removeSelection(selectedPositions);
                return;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.confirm3DModel != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.confirm3DModel.getBounds2D().getMinX()+startX, this.confirm3DModel.getBounds2D().getBounds2D().getMinY()+startY, this.confirm3DModel.getBounds2D().getWidth(), this.confirm3DModel.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getTertiaryStructure() == null)
                    return;
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() {
                        try {
                            if (mediator.getChimeraDriver() != null) {
                                mediator.getChimeraDriver().synchronizeFrom();
                                mediator.getChimeraDriver().synchronizeTo();
                            }
                        } catch (Exception e) {
                            mediator.getAssemble().getMessageBar().printException(e);
                        }
                        return null;
                    }
                }.execute();
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.erase3DModel != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.erase3DModel.getBounds2D().getMinX()+startX, this.erase3DModel.getBounds2D().getBounds2D().getMinY()+startY, this.erase3DModel.getBounds2D().getWidth(), this.erase3DModel.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getTertiaryStructure() == null)
                    return;
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Are you sure to erase your 3D model?")) {
                    mediator.setTertiaryStructure(null);
                    if (mediator.getChimeraDriver() != null)
                        mediator.getChimeraDriver().eraseModel();
                }
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.clipResidues3D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.clipResidues3D.getBounds2D().getMinX()+startX, this.clipResidues3D.getBounds2D().getBounds2D().getMinY()+startY, this.clipResidues3D.getBounds2D().getWidth(), this.clipResidues3D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                    return;
                File tmpPDB = null;
                try {
                    java.util.List<Residue3D> selectedResidues = new ArrayList<Residue3D>();
                    for (Residue r:mediator.getSecondaryCanvas().getSelectedResidues()) {
                        Residue3D residue3D = mediator.getTertiaryStructure().getResidue3DAt(r.getAbsolutePosition());
                        if (residue3D != null)
                            selectedResidues.add(residue3D);
                    }
                    Collections.sort(selectedResidues,new Comparator<Residue3D>() {
                        public int compare(Residue3D residue, Residue3D residue1) {
                            return residue.getAbsolutePosition()-residue1.getAbsolutePosition();
                        }
                    });
                    if (selectedResidues.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "You need to select at least one residue in the 3D.");
                        return;
                    }
                    Residue3D magnetResidue3D = mediator.getTertiaryStructure().getResidue3DAt(selectedResidues.get(0).getAbsolutePosition()-1);
                    if (magnetResidue3D == null) {
                        JOptionPane.showMessageDialog(null, "You need residue "+(selectedResidues.get(0).getAbsolutePosition()-1)+" in the 3D scene to clip your selection.");
                        return;
                    }
                    if (magnetResidue3D != null ) {
                        Matrix q = Modeling3DUtils.computeTransformationMatrixToMoveAt3PrimeEnd(magnetResidue3D, selectedResidues.get(0));
                        for (Residue3D r:selectedResidues)
                            for (Residue3D.Atom atom : r.getAtoms()) {
                                if (atom.hasCoordinatesFilled()) {
                                    float x = atom.getCoordinates()[0];
                                    float y = atom.getCoordinates()[1];
                                    float z = atom.getCoordinates()[2];

                                    double x2 = x * q.get(0, 0) + y * q.get(0, 1) + z * q.get(0, 2) + q.get(0, 3);
                                    double y2 = x * q.get(1, 0) + y * q.get(1, 1) + z * q.get(1, 2) + q.get(1, 3);
                                    double z2 = x * q.get(2, 0) + y * q.get(2, 1) + z * q.get(2, 2) + q.get(2, 3);
                                    atom.setCoordinates((float) x2, (float) y2, (float) z2);
                                }
                            }
                        selectedResidues.add(0, magnetResidue3D);
                        if (mediator.getChimeraDriver() != null) {
                            tmpPDB = IoUtils.createTemporaryFile("model.pdb");
                            FileParser.writePDBFile(selectedResidues, false, new FileWriter(tmpPDB));
                            mediator.getChimeraDriver().addFragment(tmpPDB, selectedResidues, magnetResidue3D.getAbsolutePosition(), -1, false);
                        }

                    } else
                        JOptionPane.showMessageDialog(null, "You need residue n"+(selectedResidues.get(0).getAbsolutePosition()-1)+" in Chimera to clip your selection.");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.refine != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.refine.getBounds2D().getMinX()+startX, this.refine.getBounds2D().getBounds2D().getMinY()+startY, this.refine.getBounds2D().getWidth(), this.refine.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                    return;
                if (mediator.getTertiaryStructure() != null) {
                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() {
                            mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                            try {
                                //first we need to be sure that the 3D coordinates in memory are synchronized with what is displayed in Chimera
                                if (mediator.getChimeraDriver() != null)
                                    mediator.getChimeraDriver().synchronizeFrom();
                                //now we can refine
                                java.util.List<JComponent> inputs = new ArrayList<JComponent>();
                                inputs.add(new JLabel("Number of Iterations"));
                                final JTextField iterations = new JTextField("10");
                                inputs.add(iterations);
                                if (JOptionPane.OK_OPTION ==  JOptionPane.showConfirmDialog(null, inputs.toArray(new JComponent[]{}), "3D Refinement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE)) {

                                    java.util.List<Residue3D> originalResidues = new ArrayList<Residue3D>();
                                    for (Residue r:mediator.getSecondaryCanvas().getSelectedResidues()) {
                                        Residue3D residue3D = mediator.getTertiaryStructure().getResidue3DAt(r.getAbsolutePosition());
                                        if (residue3D != null)
                                            originalResidues.add(residue3D);
                                    }
                                    java.util.List<BaseBaseInteraction> interactions = new ArrayList<BaseBaseInteraction>();
                                    for (BaseBaseInteraction bbi:mediator.getSecondaryStructure().getTertiaryInteractions())
                                        if (bbi.isSelected())
                                            interactions.add(bbi);
                                    for (Helix h:mediator.getSecondaryStructure().getHelices())
                                        for (BaseBaseInteraction bbi:h.getSecondaryInteractions())
                                            if (bbi.isSelected())
                                                interactions.add(bbi);
                                    java.util.List<Residue3D> refinedResidues = new Rnart(mediator).refine(originalResidues, interactions , iterations.getText().trim());
                                    int i=0;
                                    for (Residue3D refinedResidue : refinedResidues) {
                                        Residue3D residue = originalResidues.get(i++);
                                        for (Residue3D.Atom refinedAtom : refinedResidue.getAtoms()) {
                                            if (refinedAtom.hasCoordinatesFilled()) {
                                                Residue3D.Atom atom = residue.getAtom(refinedAtom.getName());
                                                atom.setCoordinates(refinedAtom.getX(), refinedAtom.getY(), refinedAtom.getZ());
                                            }
                                        }
                                    }
                                    if (mediator.getChimeraDriver() != null) {
                                        File f = IoUtils.createTemporaryFile("refined.pdb");
                                        FileParser.writePDBFile(originalResidues, false, new FileWriter(f));
                                        mediator.getChimeraDriver().loadRefinedModel(f);
                                    }
                                }
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
        if (this.color3D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.color3D.getBounds2D().getMinX()+startX, this.color3D.getBounds2D().getBounds2D().getMinY()+startY, this.color3D.getBounds2D().getWidth(), this.color3D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getTertiaryStructure() == null)
                    return;
                new javax.swing.SwingWorker() {
                    @Override
                    protected Object doInBackground() {
                        try {
                            if (mediator.getSecondaryCanvas().getSelectedResidues().isEmpty() && mediator.getChimeraDriver() != null)
                                mediator.getChimeraDriver().color3D(mediator.getSecondaryStructure().getResidues());
                            else if (mediator.getChimeraDriver() != null)
                                mediator.getChimeraDriver().color3D(mediator.getSecondaryCanvas().getSelectedResidues());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
                return;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.setFocus != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.setFocus.getBounds2D().getMinX()+startX, this.setFocus.getBounds2D().getBounds2D().getMinY()+startY, this.setFocus.getBounds2D().getWidth(), this.setFocus.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                List<String> positions = new ArrayList<String>(1);
                if (mediator.getSecondaryCanvas().getSelectedResidues().isEmpty()) {
                    for (Residue _r:this.mediator.getSecondaryStructure().getResidues())
                        positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()) != null ? mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()).getLabel(): ""+_r.getAbsolutePosition());
                }
                else
                    for (Residue _r:this.mediator.getSecondaryCanvas().getSelectedResidues())
                        positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()) != null ? mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()).getLabel(): ""+_r.getAbsolutePosition());
                if (mediator.getChimeraDriver() != null)
                    mediator.getChimeraDriver().setFocus(positions);
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.setPivot != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.setPivot.getBounds2D().getMinX()+startX, this.setPivot.getBounds2D().getBounds2D().getMinY()+startY, this.setPivot.getBounds2D().getWidth(), this.setPivot.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getSecondaryCanvas().getSelectedResidues().isEmpty())
                    return;
                List<String> positions = new ArrayList<String>(1);
                for (Residue _r:this.mediator.getSecondaryCanvas().getSelectedResidues())
                    positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()) != null ? mediator.getTertiaryStructure().getResidue3DAt(_r.getAbsolutePosition()).getLabel(): ""+_r.getAbsolutePosition());
                if (mediator.getChimeraDriver() != null)
                    mediator.getChimeraDriver().setPivot(positions);
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

    }

    public void mouseMoved(MouseEvent e, int startX, int startY) {
        if (this.newChimera != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.newChimera.getBounds2D().getMinX()+startX, this.newChimera.getBounds2D().getBounds2D().getMinY()+startY, this.newChimera.getBounds2D().getWidth(), this.newChimera.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overNewChimera) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overNewChimera) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Launch a new Chimera window.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overNewChimera = true;
            } else {
                if (overNewChimera)
                    mediator.getSecondaryCanvas().repaint();
                overNewChimera = false;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.create != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.create.getBounds2D().getMinX()+startX, this.create.getBounds2D().getBounds2D().getMinY()+startY, this.create.getBounds2D().getWidth(), this.create.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overCreate) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overCreate) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Create 3D residues from current selection.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overCreate = true;
            } else {
                if (overCreate)
                    mediator.getSecondaryCanvas().repaint();
                overCreate = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.trash != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.trash.getBounds2D().getMinX()+startX, this.trash.getBounds2D().getBounds2D().getMinY()+startY, this.trash.getBounds2D().getWidth(), this.trash.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overTrash) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overTrash) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Delete selected 3D residues.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overTrash = true;
            } else {
                if (overTrash)
                    mediator.getSecondaryCanvas().repaint();
                overTrash = false;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.confirm3DModel != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.confirm3DModel.getBounds2D().getMinX()+startX, this.confirm3DModel.getBounds2D().getBounds2D().getMinY()+startY, this.confirm3DModel.getBounds2D().getWidth(), this.confirm3DModel.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overConfirm3DModel) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overConfirm3DModel) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Merge all 3D fragments into a single molecular chain.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overConfirm3DModel = true;
            } else {
                if (overConfirm3DModel)
                    mediator.getSecondaryCanvas().repaint();
                overConfirm3DModel = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.erase3DModel != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.erase3DModel.getBounds2D().getMinX()+startX, this.erase3DModel.getBounds2D().getBounds2D().getMinY()+startY, this.erase3DModel.getBounds2D().getWidth(), this.erase3DModel.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overErase3DModel) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overErase3DModel) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Delete the full 3D model.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overErase3DModel = true;
            } else {
                if (overErase3DModel)
                    mediator.getSecondaryCanvas().repaint();
                overErase3DModel = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.clipResidues3D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.clipResidues3D.getBounds2D().getMinX()+startX, this.clipResidues3D.getBounds2D().getBounds2D().getMinY()+startY, this.clipResidues3D.getBounds2D().getWidth(), this.clipResidues3D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overClipResidues3D) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overClipResidues3D) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Clip selected 3D residues.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overClipResidues3D = true;
            } else {
                if (overClipResidues3D)
                    mediator.getSecondaryCanvas().repaint();
                overClipResidues3D = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.refine != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.refine.getBounds2D().getMinX()+startX, this.refine.getBounds2D().getBounds2D().getMinY()+startY, this.refine.getBounds2D().getWidth(), this.refine.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overRefine) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overRefine) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Geometric refinement of the selected 3D residues.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overRefine = true;
            } else {
                if (overRefine)
                    mediator.getSecondaryCanvas().repaint();
                overRefine = false;
            }
            startY += buttonShape.getHeight()+5;
        }
        if (this.color3D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.color3D.getBounds2D().getMinX()+startX, this.color3D.getBounds2D().getBounds2D().getMinY()+startY, this.color3D.getBounds2D().getWidth(), this.color3D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overColor3D) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overColor3D) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Apply colors of the current selection to the 3D model.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overColor3D = true;
            } else {
                if (overColor3D)
                    mediator.getSecondaryCanvas().repaint();
                overColor3D = false;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.setFocus != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.setFocus.getBounds2D().getMinX()+startX, this.setFocus.getBounds2D().getBounds2D().getMinY()+startY, this.setFocus.getBounds2D().getWidth(), this.setFocus.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overSetFocus) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overSetFocus) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Focus the 3D model on the current selection.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overSetFocus = true;
            } else {
                if (overSetFocus)
                    mediator.getSecondaryCanvas().repaint();
                overSetFocus = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.setPivot != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.setPivot.getBounds2D().getMinX()+startX, this.setPivot.getBounds2D().getBounds2D().getMinY()+startY, this.setPivot.getBounds2D().getWidth(), this.setPivot.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overSetPivot) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overSetPivot) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(mediator.getSecondaryCanvas().getWidth()-40);
                                tooltip.setHeight(40);
                                tooltip.setOrientation(ToolTip.RIGHT);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Center the 3D model on the current selection.");
                                mediator.getSecondaryCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getSecondaryCanvas().repaint();
                }
                overSetPivot = true;
            } else {
                if (overSetPivot)
                    mediator.getSecondaryCanvas().repaint();
                overSetPivot = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (!overTrash && !overNewChimera && !overCreate && !overErase3DModel && !overConfirm3DModel && !overRefine && !overClipResidues3D && !overColor3D && !overSetFocus && !overSetPivot) {
            tooltip = null;
            this.mediator.getSecondaryCanvas().repaint();
        }

    }
}
