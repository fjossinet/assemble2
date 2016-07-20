package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.io.Modeling3DException;
import fr.unistra.ibmc.assemble2.io.computations.DataHandler;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jdesktop.swingx.JXTable;
import org.noos.xing.mydoggy.ToolWindow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;

public class TertiaryFragmentsPanel extends JXTable implements MouseListener {

    private Mediator mediator;
    private DefaultTableModel model;
    private JPopupMenu popupMenu;
    private ToolWindow toolWindow;

    public TertiaryFragmentsPanel(final Mediator mediator) {
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setSortable(false);
        this.setEditable(false);
        this.mediator = mediator;
        this.model = new DefaultTableModel();
        this.model.addColumn("3D");
        this.model.addColumn("Description");
        this.addMouseListener(this);
        this.setModel(this.model);
        this.popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Apply Hit"){
            public void paintComponent(Graphics graphics) {
                this.setEnabled(TertiaryFragmentsPanel.this.getSelectedRows().length == 1);
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        boolean firstFragment = false;
                        if (mediator.getTertiaryStructure() == null) {
                            TertiaryStructure ts =  new TertiaryStructure(mediator.getSecondaryStructure().getMolecule());
                            mediator.setTertiaryStructure(ts);
                            firstFragment = true;
                        }
                        List<Residue3D> previousResidues = mediator.getTertiaryStructure().getResidues3D();
                        try {
                            List<Residue3D> computedResidues = mediator.getRna2DViewer().loadJunctionHit((TertiaryFragmentHit)TertiaryFragmentsPanel.this.getValueAt(TertiaryFragmentsPanel.this.getSelectedRows()[0],1),mediator.getTertiaryStructure());
                            File tmpPDB = IoUtils.createTemporaryFile("model.pdb");
                            Collections.sort(computedResidues,new Comparator<Residue3D>() {
                                public int compare(Residue3D residue, Residue3D residue1) {
                                    return residue.getAbsolutePosition()-residue.getAbsolutePosition();
                                }
                            });
                            FileParser.writePDBFile(computedResidues, false, new FileWriter(tmpPDB));
                            int anchorResidue1 = -1,anchorResidue2 = -1;
                            for (Residue3D r: computedResidues) {
                                int previous = r.getAbsolutePosition();
                                Residue pairedResidue = mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(previous));
                                if (pairedResidue == null)
                                    continue;
                                int pairedPrevious = pairedResidue.getAbsolutePosition();
                                for (Residue3D previousRes:previousResidues) { //we search which residues in the new ones are already in the 3D scene
                                    if (previousRes.getAbsolutePosition() == previous) {
                                        anchorResidue1 = previous;
                                    }
                                    else if  (previousRes.getAbsolutePosition() == pairedPrevious) {
                                        anchorResidue2 = pairedPrevious;
                                    }
                                }
                                if (anchorResidue1 != -1 && anchorResidue2 != -1) {
                                    break;
                                }
                            }
                            if (mediator.getChimeraDriver() != null)
                                mediator.getChimeraDriver().addFragment(tmpPDB, computedResidues, anchorResidue1, anchorResidue2, firstFragment);
                        }
                        catch (Modeling3DException ex) {
                            if (ex.getStatus() == Modeling3DException.MISSING_ATOMS_IN_MOTIFS)
                                JOptionPane.showMessageDialog(null,"Cannot apply the 3D fold. Some atoms are missing.");
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return null;
                    }
                }.execute();

            }

        });
        //this.popupMenu.add(menuItem);

       /* menuItem = new JMenuItem("Display in Initial Structure") {
            public void paintComponent(Graphics graphics) {
                this.setEnabled(TertiaryFragmentsPanel.this.getSelectedRows().length == 1);
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        DataHandler dataHandler = new DataHandler(mediator);

                        TertiaryFragmentHit hit = (TertiaryFragmentHit)TertiaryFragmentsPanel.this.getValueAt(TertiaryFragmentsPanel.this.getSelectedRows()[0],1);

                        List<Residue3D> allResidues = dataHandler.getTertiaryStructure(hit.getTertiaryStructureId(), mediator.getPDBMongo().getName()),
                                junctionResidues = new ArrayList<Residue3D>() ;

                        for (MutablePair<String,Location> fragmentHit:hit.getFragments())
                            junctionResidues.addAll(dataHandler.getResidue3DByMolecularLocation(fragmentHit.left,fragmentHit.right, hit.getTertiaryStructureId()));

                        File tmpPDB = IoUtils.createTemporaryFile("initial_structure.pdb");
                        Collections.sort(allResidues,new Comparator<Residue3D>() {
                            public int compare(Residue3D residue, Residue3D residue1) {
                                return residue.getAbsolutePosition()-residue.getAbsolutePosition();
                            }
                        });
                        FileParser.writePDBFile(allResidues, false, new FileWriter(tmpPDB));
                        if (mediator.getChimeraDriver() != null) {
                            mediator.getChimeraDriver().loadTertiaryStructure(tmpPDB);

                            //We highlight the residues making the junction with the stick representation
                            List<String> positions = new ArrayList<String>();

                            for (Residue3D residue3D:junctionResidues)
                                positions.add(residue3D.getLabel());

                            mediator.getChimeraDriver().selectResidues(positions);
                            mediator.getChimeraDriver().showResidues(positions);
                        }

                        return null;
                    }
                }.execute();
            }
        });
        this.popupMenu.add(menuItem);*/

    }

    public void addRow(TertiaryFragmentHit hit) {
        this.model.addRow(new Object[]{hit.getPdbID(), hit});
    }

    public void clearList() {
        int size = this.model.getRowCount();
        for (int i = size-1; i >= 0; i--) {
            this.model.removeRow(i);
        }
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        return super.getCellRenderer(row, column);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private void maybeShowPopup(MouseEvent e) {
        /*if (e.isPopupTrigger()) {
            this.popupMenu.show(e.getComponent(),
                    e.getX(), e.getY());
        }*/
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }

    public void setToolWindow(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    public int length() {
        return this.model.getRowCount();
    }
}
