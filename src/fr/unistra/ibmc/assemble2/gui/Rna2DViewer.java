package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.io.computations.DataHandler;
import fr.unistra.ibmc.assemble2.io.computations.Rnart;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.utils.*;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the widget displaying an extended secondary structure
 * @author fjossinet
 */
public class Rna2DViewer extends JPanel {

    private Mediator mediator;
    private SecondaryCanvas secondaryCanvas;

    public Rna2DViewer(Mediator mediator) {
        this.mediator = mediator;
        this.mediator.setRna2DViewer(this);
        this.setLayout(new BorderLayout());
        this.secondaryCanvas = new SecondaryCanvas(this.mediator);
        this.add(this.secondaryCanvas, java.awt.BorderLayout.CENTER);
    }

    /**
     * Return the current SecondaryCanvas
     *
     * @return the current SecondaryCanvas. Return null if no SecondaryCanvas available
     */
    public SecondaryCanvas getSecondaryCanvas() {
        return this.secondaryCanvas;
    }

    private class Rna2DViewerToolBar extends JToolBar {

        private ActionButton flip, center, reorganizeHelices;

        private Rna2DViewerToolBar() {

            ActionButton addA = new ActionButton(null,new ImageIcon(RessourcesUtils.getImage("A.png")), new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    try {
                        int pos  = mediator.getSecondaryCanvas().getSelectedResidues().get(0).getAbsolutePosition();
                        mediator.getAssemble().insertSubSequence(pos, "A");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            addA.setToolTipText("Add A");
            this.add(addA);

            ActionButton addU = new ActionButton(null,new ImageIcon(RessourcesUtils.getImage("U.png")), new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    try {
                        int pos  = mediator.getSecondaryCanvas().getSelectedResidues().get(0).getAbsolutePosition();
                        mediator.getAssemble().insertSubSequence(pos, "U");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            addU.setToolTipText("Add U");
            this.add(addU);

            ActionButton addG = new ActionButton(null,new ImageIcon(RessourcesUtils.getImage("G.png")), new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    try {
                        int pos  = mediator.getSecondaryCanvas().getSelectedResidues().get(0).getAbsolutePosition();
                        mediator.getAssemble().insertSubSequence(pos, "G");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            addG.setToolTipText("Add G");
            this.add(addG);

            ActionButton addC = new ActionButton(null,new ImageIcon(RessourcesUtils.getImage("C.png")), new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    try {
                        int pos  = mediator.getSecondaryCanvas().getSelectedResidues().get(0).getAbsolutePosition();
                        mediator.getAssemble().insertSubSequence(pos, "C");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            addC.setToolTipText("Add C");
            this.add(addC);

            ActionButton removeResidue = new ActionButton(null,new ImageIcon(RessourcesUtils.getImage("cut.png")), new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    try {
                        int pos  = mediator.getSecondaryCanvas().getSelectedResidues().get(0).getAbsolutePosition();
                        mediator.getAssemble().removeSubSequence(pos, 1);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            removeResidue.setToolTipText("Remove Residue");
            this.add(removeResidue);

        }

        private class FlatButton extends JButton {
            private FlatButton(String url, String name) {
                this(RessourcesUtils.getIcon(url), name);
            }

            private FlatButton(Icon image, String name) {
                super(image);
                this.setMargin(new Insets(0, 0, 0, 0));
                this.setBorderPainted(false);
                this.setBackground(Color.WHITE);
                this.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {

                        setBackground(Color.LIGHT_GRAY);
                        setBorderPainted(true);
                    }

                    public void mouseExited(MouseEvent e) {
                        setBackground(Color.WHITE);
                        setBorderPainted(false);
                    }
                });

                this.setToolTipText(name);

            }

            public Point getToolTipLocation(MouseEvent e) {
                return new Point(0, 0);
            }
        }

        private class ActionButton extends javax.swing.JButton implements MouseListener {

            private ActionButton(final String name, final javax.swing.Icon image, final String toolTipText, final ActionListener listener) {
                this(name, image, listener);
                this.setToolTipText(toolTipText);
            }

            private ActionButton(final String name, final javax.swing.Icon image, final ActionListener listener) {
                super(name, image);
                this.addActionListener(listener);
                this.addMouseListener(this);
                this.setMargin(new Insets(0, 0, 0, 0));
                this.setBorderPainted(false);
                this.setBackground(Color.white);
            }

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
                this.setBackground(Color.lightGray);
                this.setBorderPainted(true);
            }

            public void mouseExited(MouseEvent e) {
                this.setBackground(Color.white);
                this.setBorderPainted(false);
            }
        }

    }

    public List<Residue3D> loadJunctionHit(TertiaryFragmentHit hit, TertiaryStructure ts) throws Exception {
        List<Residue3D> computedResidues3D = new ArrayList<Residue3D>();
        int i=0;
        for (MutablePair<String,Location> fragmentHit:hit.getFragments()) {
            List<Residue3D> residuesToThreadIn = new DataHandler(mediator).getResidue3DByMolecularLocation(fragmentHit.left,fragmentHit.right, hit.getTertiaryStructureId());
            MutablePair<Molecule,Location> fragment = null;
            if (this.secondaryCanvas.getSelectedJunction() != null)
                fragment = this.secondaryCanvas.getSelectedJunction().getFragments().get(i);
            else
                fragment = new MutablePair<Molecule,Location>(this.secondaryCanvas.getSecondaryStructure().getMolecule(), new Location(this.secondaryCanvas.getSelectedSingleStrand().getLocation().getStart()-1, this.secondaryCanvas.getSelectedSingleStrand().getLocation().getEnd()+1));
            if (fragment != null) {
                List<Residue3D> threadedResidues = Modeling3DUtils.thread(mediator, ts, fragment.right.getStart(), fragment.right.getLength(), Modeling3DUtils.RNA, residuesToThreadIn);
                if (threadedResidues.isEmpty())
                    return new ArrayList<Residue3D>();
                computedResidues3D.addAll(threadedResidues);
            }
            i++;
        }
        return computedResidues3D;
    }

}
