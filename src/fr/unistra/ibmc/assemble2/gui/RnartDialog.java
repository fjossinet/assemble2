package fr.unistra.ibmc.assemble2.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.io.computations.Rnart;
import fr.unistra.ibmc.assemble2.model.BaseBaseInteraction;
import fr.unistra.ibmc.assemble2.model.Helix;
import fr.unistra.ibmc.assemble2.model.Residue;
import fr.unistra.ibmc.assemble2.model.Residue3D;
import fr.unistra.ibmc.assemble2.utils.IoUtils;

public class RnartDialog extends JFrame {

    private JTextField thres;
    private JLabel mod ;

    public RnartDialog(final Mediator mediator) {
        this.setTitle("3D Model Refinement");
        this.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(5,5,5,5));

        this.add(mainPanel);

        //this.mod = new JComboBox(new Object[]{"Number of passes (ex : 10)", "Average RMSD treshold (ex : 0.2)", "Similarity between 2 successive RMSD (in %, ex : 95)"});
        this.mod = new JLabel("Number of Iterations");
        this.thres = new JTextField("10");
        this.thres.setPreferredSize(new Dimension(50,0));
        JPanel buttonsPanel = new JPanel();
        JButton button = new JButton("Cancel");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                RnartDialog.this.dispose();
            }
        });
        buttonsPanel.add(button);

        button = new JButton("Refine");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    //Parameters parameters = new Parameters();
                    String mode = "n";
                    /*
                    switch (mod.getSelectedIndex()) {
                        case 0:
                            mode = "n";
                            Integer.parseInt(thres.getText());
                            break;
                        case 1:
                            mode = "v";
                            Float.parseFloat(thres.getText());
                            break;
                        case 2:
                            mode = "p";
                            Integer.parseInt(thres.getText());
                            break;
                    }*/
                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            try {
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
                                java.util.List<Residue3D> refinedResidues = new Rnart(mediator).refine(originalResidues, interactions , thres.getText().trim());;
                                int i=0;
                                for (Residue3D refinedResidue : refinedResidues) {
                                    Residue3D residue = originalResidues.get(i++);
                                    for (Residue3D.Atom refinedAtom : refinedResidue.getAtoms()) {
                                        if (refinedAtom.hasCoordinatesFilled()) {
                                            //System.out.println("refinedResidue.getName() "+refinedResidue.getName()+""+refinedResidue.getAbsolutePosition());
                                            //System.out.println("residue.getName() "+residue.getName()+""+residue.getAbsolutePosition());
                                            Residue3D.Atom atom = residue.getAtom(refinedAtom.getName());
                                            //System.out.println("refinedAtom.getName() "+refinedAtom.getName());
                                            //System.out.println("atom.getName() "+atom.getName());
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
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                    RnartDialog.this.dispose();
                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null,"The value you entered is not a number");
                }
            }
        });
        buttonsPanel.add(button);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        JLabel warning = new JLabel("Warning ! This operation can take a LONG time !");
        warning.setForeground(Color.RED);
        mainPanel.add(warning, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());

        JPanel parametersPanel = new JPanel();
        parametersPanel.setBorder(new EmptyBorder(5,0,5,0));
        parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.X_AXIS));
        parametersPanel.add(mod);
        parametersPanel.add(Box.createRigidArea(new Dimension(5,0)));
        parametersPanel.add(thres);

        centerPanel.add(parametersPanel,BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.NORTH);

        this.pack();
        IoUtils.centerOnScreen(this);
        this.setVisible(true);
    }
}
