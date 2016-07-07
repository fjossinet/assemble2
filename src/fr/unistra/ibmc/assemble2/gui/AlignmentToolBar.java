package fr.unistra.ibmc.assemble2.gui;


import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.components.ToolTip;
import fr.unistra.ibmc.assemble2.io.AssembleProject;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.io.computations.Mlocarna;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class AlignmentToolBar implements ToolBar {

    private Shape lock,
            importNewMolecules,
            inferNew2D,
            extractSubAlignment;
    private boolean overLock, overImportNewMolecules, overInfertNew2D, overExtractSubAlignment;
    private Mediator mediator;
    private ToolTip tooltip;

    public AlignmentToolBar(Mediator mediator) {
        this.mediator = mediator;
        try {
            this.lock = new SvgPath("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v4.876H5.042v15.083h21.916V15.334H24.875zM10.625,10.458c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876h-10.75V10.458zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z").getShape();
            this.importNewMolecules = new SvgPath("M15.067,2.25c-5.979,0-11.035,3.91-12.778,9.309h3.213c1.602-3.705,5.271-6.301,9.565-6.309c5.764,0.01,10.428,4.674,10.437,10.437c-0.009,5.764-4.673,10.428-10.437,10.438c-4.294-0.007-7.964-2.605-9.566-6.311H2.289c1.744,5.399,6.799,9.31,12.779,9.312c7.419-0.002,13.437-6.016,13.438-13.438C28.504,8.265,22.486,2.252,15.067,2.25zM10.918,19.813l7.15-4.126l-7.15-4.129v2.297H-0.057v3.661h10.975V19.813z").getShape();
            this.inferNew2D = new SvgPath("M17.41,20.395l-0.778-2.723c0.228-0.2,0.442-0.414,0.644-0.643l2.721,0.778c0.287-0.418,0.534-0.862,0.755-1.323l-2.025-1.96c0.097-0.288,0.181-0.581,0.241-0.883l2.729-0.684c0.02-0.252,0.039-0.505,0.039-0.763s-0.02-0.51-0.039-0.762l-2.729-0.684c-0.061-0.302-0.145-0.595-0.241-0.883l2.026-1.96c-0.222-0.46-0.469-0.905-0.756-1.323l-2.721,0.777c-0.201-0.228-0.416-0.442-0.644-0.643l0.778-2.722c-0.418-0.286-0.863-0.534-1.324-0.755l-1.96,2.026c-0.287-0.097-0.581-0.18-0.883-0.241l-0.683-2.73c-0.253-0.019-0.505-0.039-0.763-0.039s-0.51,0.02-0.762,0.039l-0.684,2.73c-0.302,0.061-0.595,0.144-0.883,0.241l-1.96-2.026C7.048,3.463,6.604,3.71,6.186,3.997l0.778,2.722C6.736,6.919,6.521,7.134,6.321,7.361L3.599,6.583C3.312,7.001,3.065,7.446,2.844,7.907l2.026,1.96c-0.096,0.288-0.18,0.581-0.241,0.883l-2.73,0.684c-0.019,0.252-0.039,0.505-0.039,0.762s0.02,0.51,0.039,0.763l2.73,0.684c0.061,0.302,0.145,0.595,0.241,0.883l-2.026,1.96c0.221,0.46,0.468,0.905,0.755,1.323l2.722-0.778c0.2,0.229,0.415,0.442,0.643,0.643l-0.778,2.723c0.418,0.286,0.863,0.533,1.323,0.755l1.96-2.026c0.288,0.097,0.581,0.181,0.883,0.241l0.684,2.729c0.252,0.02,0.505,0.039,0.763,0.039s0.51-0.02,0.763-0.039l0.683-2.729c0.302-0.061,0.596-0.145,0.883-0.241l1.96,2.026C16.547,20.928,16.992,20.681,17.41,20.395zM11.798,15.594c-1.877,0-3.399-1.522-3.399-3.399s1.522-3.398,3.399-3.398s3.398,1.521,3.398,3.398S13.675,15.594,11.798,15.594zM27.29,22.699c0.019-0.547-0.06-1.104-0.23-1.654l1.244-1.773c-0.188-0.35-0.4-0.682-0.641-0.984l-2.122,0.445c-0.428-0.364-0.915-0.648-1.436-0.851l-0.611-2.079c-0.386-0.068-0.777-0.105-1.173-0.106l-0.974,1.936c-0.279,0.054-0.558,0.128-0.832,0.233c-0.257,0.098-0.497,0.22-0.727,0.353L17.782,17.4c-0.297,0.262-0.568,0.545-0.813,0.852l0.907,1.968c-0.259,0.495-0.437,1.028-0.519,1.585l-1.891,1.06c0.019,0.388,0.076,0.776,0.164,1.165l2.104,0.519c0.231,0.524,0.541,0.993,0.916,1.393l-0.352,2.138c0.32,0.23,0.66,0.428,1.013,0.6l1.715-1.32c0.536,0.141,1.097,0.195,1.662,0.15l1.452,1.607c0.2-0.057,0.399-0.118,0.596-0.193c0.175-0.066,0.34-0.144,0.505-0.223l0.037-2.165c0.455-0.339,0.843-0.747,1.152-1.206l2.161-0.134c0.152-0.359,0.279-0.732,0.368-1.115L27.29,22.699zM23.127,24.706c-1.201,0.458-2.545-0.144-3.004-1.345s0.143-2.546,1.344-3.005c1.201-0.458,2.547,0.144,3.006,1.345C24.931,22.902,24.328,24.247,23.127,24.706z").getShape();
            this.extractSubAlignment = new SvgPath("M11.108,10.271c1.083-1.876,0.159-4.443-2.059-5.725C8.231,4.074,7.326,3.825,6.433,3.825c-1.461,0-2.721,0.673-3.373,1.801C2.515,6.57,2.452,7.703,2.884,8.814C3.287,9.85,4.081,10.751,5.12,11.35c0.817,0.473,1.722,0.723,2.616,0.723c0.673,0,1.301-0.149,1.849-0.414c0.669,0.387,1.566,0.904,2.4,1.386c1.583,0.914,0.561,3.861,5.919,6.955c5.357,3.094,11.496,1.535,11.496,1.535L10.75,10.767C10.882,10.611,11.005,10.449,11.108,10.271zM9.375,9.271c-0.506,0.878-2.033,1.055-3.255,0.347C5.474,9.245,4.986,8.702,4.749,8.09C4.541,7.555,4.556,7.035,4.792,6.626c0.293-0.509,0.892-0.801,1.64-0.801c0.543,0,1.102,0.157,1.616,0.454C9.291,6.996,9.898,8.366,9.375,9.271zM17.246,15.792c0,0.483-0.392,0.875-0.875,0.875c-0.037,0-0.068-0.017-0.104-0.021l0.667-1.511C17.121,15.296,17.246,15.526,17.246,15.792zM16.371,14.917c0.037,0,0.068,0.017,0.104,0.021l-0.666,1.51c-0.188-0.16-0.312-0.39-0.312-0.656C15.496,15.309,15.887,14.917,16.371,14.917zM29.4,10.467c0,0-6.139-1.559-11.496,1.535c-0.537,0.311-0.995,0.618-1.415,0.924l4.326,2.497L29.4,10.467zM13.171,17.097c-0.352,0.851-0.575,1.508-1.187,1.859c-0.833,0.481-1.73,0.999-2.399,1.386c-0.549-0.265-1.176-0.414-1.85-0.414c-0.894,0-1.798,0.249-2.616,0.721c-2.218,1.282-3.143,3.851-2.06,5.726c0.651,1.127,1.912,1.801,3.373,1.801c0.894,0,1.799-0.25,2.616-0.722c1.04-0.601,1.833-1.501,2.236-2.536c0.432-1.112,0.368-2.245-0.178-3.189c-0.103-0.178-0.226-0.34-0.356-0.494l3.982-2.3C14.044,18.295,13.546,17.676,13.171,17.097zM9.42,24.192c-0.238,0.612-0.725,1.155-1.371,1.528c-1.221,0.706-2.75,0.532-3.257-0.347C4.27,24.47,4.878,23.099,6.12,22.381c0.514-0.297,1.072-0.453,1.615-0.453c0.749,0,1.346,0.291,1.64,0.8C9.612,23.138,9.628,23.657,9.42,24.192z").getShape();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2, int startX, int startY) {
        if (this.tooltip != null)
            this.tooltip.draw(g2);
        if (this.lock != null) {
            Rectangle2D buttonShape = this.lock.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getAlignmentCanvas().getMainAlignment() == null)
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overLock) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.lock);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.lock);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }

        if (this.importNewMolecules != null) {
            Rectangle2D buttonShape = this.importNewMolecules.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getAlignmentCanvas().getMainAlignment() == null)
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overImportNewMolecules) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.importNewMolecules);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.importNewMolecules);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+10;
        }

        if (this.inferNew2D != null) {
            Rectangle2D buttonShape = this.inferNew2D.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getAlignmentCanvas().getMainAlignment() == null)
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overInfertNew2D) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.inferNew2D);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.inferNew2D);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }

        if (this.extractSubAlignment != null) {
            Rectangle2D buttonShape = this.extractSubAlignment.getBounds2D();
            g2.translate(startX, startY);
            if (mediator.getAlignmentCanvas().getMainAlignment() == null || mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules().size() < 2 || mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSelection().isEmpty())
                g2.setColor(Color.LIGHT_GRAY);
            else {
                if (overExtractSubAlignment) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.extractSubAlignment);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.extractSubAlignment);
            g2.translate(-startX, -startY);
            startY += buttonShape.getHeight()+5;
        }

    }

    public void mouseClicked(MouseEvent e, int startX, int startY) {

        if (this.lock != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.lock.getBounds2D().getMinX()+startX, this.lock.getBounds2D().getBounds2D().getMinY()+startY, this.lock.getBounds2D().getWidth(), this.lock.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getAlignmentCanvas().getMainAlignment() == null)
                    return;
                if (mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().isGenomicAnnotation()) {
                    JOptionPane.showMessageDialog(null, "A genomic sequence cannot be unlocked!!");
                    return;
                }
                try {
                    if (mediator.getAlignmentCanvas().isEditSequences()) {
                        this.lock = new SvgPath("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v4.876H5.042v15.083h21.916V15.334H24.875zM10.625,10.458c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876h-10.75V10.458zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z").getShape();
                        mediator.getAlignmentCanvas().setEditSequences(false);
                    }
                    else {
                        this.lock = new SvgPath("M24.875,15.334v-4.876c0-4.894-3.981-8.875-8.875-8.875s-8.875,3.981-8.875,8.875v0.375h3.5v-0.375c0-2.964,2.411-5.375,5.375-5.375s5.375,2.411,5.375,5.375v4.876H5.042v15.083h21.916V15.334H24.875zM18.272,26.956h-4.545l1.222-3.667c-0.782-0.389-1.324-1.188-1.324-2.119c0-1.312,1.063-2.375,2.375-2.375s2.375,1.062,2.375,2.375c0,0.932-0.542,1.73-1.324,2.119L18.272,26.956z").getShape();
                        mediator.getAlignmentCanvas().setEditSequences(true);
                    }
                    mediator.getAlignmentCanvas().repaint();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.importNewMolecules != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.importNewMolecules.getBounds2D().getMinX()+startX, this.importNewMolecules.getBounds2D().getBounds2D().getMinY()+startY, this.importNewMolecules.getBounds2D().getWidth(), this.importNewMolecules.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getAlignmentCanvas().getMainAlignment() == null)
                    return;
                final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(Assemble.getLastWorkingDirectory());
                fileChooser.setFileHidingEnabled(true);
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().endsWith(".fasta") || file.getName().endsWith(".fna")|| file.getName().endsWith(".fas") || file.getName().endsWith(".fa");
                    }

                    @Override
                    public String getDescription() {
                        return "FASTA Files (.fasta, .fna, .fas, .fa)";
                    }
                });

                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().endsWith(".aln");
                    }

                    @Override
                    public String getDescription() {
                        return "Clustal (.aln)";
                    }
                });

                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().endsWith(".pdb");
                    }

                    @Override
                    public String getDescription() {
                        return "PDB (.pdb)";
                    }
                });

                if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    final File f = fileChooser.getSelectedFile();
                    Assemble.setLastWorkingDirectory(f);
                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {

                            BufferedReader in = new BufferedReader(new FileReader(f));
                            StringBuffer seq = new StringBuffer();
                            String name = null;
                            String line = null;
                            if (fileChooser.getFileFilter().getDescription().startsWith("Clustal")) {
                                Map<String,StringBuffer> alignedMolecules = new HashMap<String,StringBuffer>();
                                while ((line = in.readLine())!= null) {
                                    String[] tokens = line.trim().split("\\s+");
                                    if (tokens.length == 2 && !tokens[0].equals("2D") && Pattern.compile("^[-A-Z]+$", Pattern.CASE_INSENSITIVE).matcher(tokens[1]).matches()) {
                                        if (alignedMolecules.containsKey(tokens[0]))
                                            alignedMolecules.put(tokens[0], alignedMolecules.get(tokens[0]).append(tokens[1]));
                                        else
                                            alignedMolecules.put(tokens[0],new StringBuffer(tokens[1]));
                                    }
                                }
                                for (Map.Entry<String, StringBuffer> e : alignedMolecules.entrySet()) {
                                    AlignedMolecule alignedM = new AlignedMolecule(mediator, new Molecule(e.getKey(), e.getValue().toString().replaceAll("-", "")), e.getValue().toString());
                                    mediator.getAlignmentCanvas().getMainAlignment().addBiologicalSequence(alignedM);
                                    mediator.getMoleculesList().addRow(alignedM.getMolecule());
                                }
                            } else if (fileChooser.getFileFilter().getDescription().startsWith("PDB")) {
                                List<TertiaryStructure> structures = FileParser.parsePDB(mediator, in);
                                for (TertiaryStructure ts:structures) {
                                    AlignedMolecule alignedM = new AlignedMolecule(mediator, ts.getMolecule(), ts.getMolecule().printSequence());
                                    mediator.getAlignmentCanvas().getMainAlignment().addBiologicalSequence(alignedM);
                                    mediator.getMoleculesList().addRow(alignedM.getMolecule());
                                }
                            } else {
                                while ((line = in.readLine()) != null) {
                                    if (line.startsWith(">")) {
                                        if (seq.length() != 0 && name != null) {
                                            String sequence = seq.toString();
                                            AlignedMolecule alignedM = new AlignedMolecule(mediator, new Molecule(name, sequence.replaceAll("-", "")), seq.toString());
                                            mediator.getAlignmentCanvas().getMainAlignment().addBiologicalSequence(alignedM);
                                            mediator.getMoleculesList().addRow(alignedM.getMolecule());
                                        }
                                        name = line.substring(1);
                                        seq = new StringBuffer();
                                    }
                                    else
                                        seq.append(line.replace('.', '-').replace('_','-').replace(" ",""));
                                }
                                //the last
                                if (seq.length() != 0 && name != null) {
                                    String sequence = seq.toString();
                                    AlignedMolecule alignedM = new AlignedMolecule(mediator, new Molecule(name, sequence.replaceAll("-", "")), seq.toString());
                                    mediator.getAlignmentCanvas().getMainAlignment().addBiologicalSequence(alignedM);
                                    mediator.getMoleculesList().addRow(alignedM.getMolecule());
                                }
                            }
                            in.close();

                            mediator.getAlignmentCanvas().repaint();

                            return null;
                        }
                    }.execute();
                }
                return;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.inferNew2D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.inferNew2D.getBounds2D().getMinX()+startX, this.inferNew2D.getBounds2D().getBounds2D().getMinY()+startY, this.inferNew2D.getBounds2D().getWidth(), this.inferNew2D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getAlignmentCanvas().getMainAlignment() == null)
                    return;
                final AssembleProject parser = new AssembleProject(mediator, mediator.getAssemble().getCurrentAssembleProject().getLocation());
                if (mediator.getAssemble().getCurrentAssembleProject() != null) { //if the current project has been saved, then we ask the user to save the current 2D/3D AND we can propose him/her to load precomputed 2Ds (previously saved)
                    if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Do you want to save the current 2D/3D?", "Save 2D/3D", JOptionPane.YES_NO_OPTION)) {
                        new javax.swing.SwingWorker() {
                            @Override
                            protected Object doInBackground() {
                                try {
                                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                    SecondaryStructure ss = mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure().getSecondaryStructure();
                                    parser.saveMolecule(ss.getMolecule());
                                    parser.saveSecondaryStructure(ss);
                                    if (mediator.getTertiaryStructure() != null)
                                        parser.saveTertiaryStructure(mediator.getTertiaryStructure());
                                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        }.execute();
                    }
                }

                java.util.List<JComponent> inputs = new ArrayList<JComponent>();
                java.util.List<String> molecules = new ArrayList<String>();
                int i = 2;
                for (Molecule m: mediator.getAlignmentCanvas().getMainAlignment().getMolecules())
                    if (m != mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule()) //the reference molecule cannot be the target to infer a new 2D/3D
                        molecules.add(m.getName()+" (S"+(i++)+")");
                Collections.sort(molecules);
                final JComboBox moleculeChoices = new JComboBox<String>(molecules.toArray(new String[]{}));
                inputs.add(new JLabel("Choose the target molecule"));
                inputs.add(moleculeChoices);
                inputs.add(new JLabel("Choose your structural mask"));
                final JComboBox structuralChoices = new JComboBox<String>(new String[]{"Consensus structure", "Reference structure"});
                inputs.add(structuralChoices);
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, inputs.toArray(new JComponent[]{}), "Derive a new RNA structure", JOptionPane.DEFAULT_OPTION))  {
                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() {
                            String choice  = (String)structuralChoices.getSelectedItem();
                            AlignedMolecule seq = null;

                            if (moleculeChoices.getItemCount() == 1)
                                seq = mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules().get(1);
                            else {
                                int j = 1;
                                for (AlignedMolecule alignedMolecule: mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules())
                                    if ((alignedMolecule.getMolecule().getName()+" (S"+(j++)+")").equals(moleculeChoices.getSelectedItem())) {
                                        seq = alignedMolecule;
                                        break;
                                    }
                            }

                            if ("Reference structure".equals(choice)) {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                try {
                                    //first we derive the secondary structure
                                    List<Location> secondaryInteractions = new ArrayList<Location>();
                                    Map<Location, String> secondaryInteractionsTypes = new HashMap<Location, String>();
                                    Map<Location, String> tertiaryInteractionsTypes = new HashMap<Location, String>();
                                    ReferenceStructure referenceStructure = mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure();
                                    for (int i=0 ; i <referenceStructure.size() ; i++) {
                                        ReferenceStructureSymbol referenceStructureSymbol = (ReferenceStructureSymbol)referenceStructure.getSymbol(i);
                                        int positionInTargetSequence = seq.getSymbol(i).getPositionInSequence();
                                        for (BaseBaseInteraction bbi: referenceStructureSymbol.getReferenceBaseBaseInteractions()) {
                                            if (bbi.isSecondaryInteraction()) {
                                                ReferenceStructureSymbol pairedReferenceStructureSymbol = referenceStructureSymbol.getPairedSymbol(bbi);
                                                int pairedPositionInTargetSequence = seq.getSymbol(referenceStructure.getIndex(pairedReferenceStructureSymbol)).getPositionInSequence();

                                                if (positionInTargetSequence < pairedPositionInTargetSequence /*to avoid to do twice the same stuff*/&& !seq.getSymbol(i).isGap() && !seq.getSymbol(referenceStructure.getIndex(pairedReferenceStructureSymbol)).isGap()) {
                                                    Location location = new Location(new Location(positionInTargetSequence), new Location(pairedPositionInTargetSequence));
                                                    secondaryInteractions.add(location);
                                                    secondaryInteractionsTypes.put(location, bbi.getOrientation() + "" + bbi.getEdge(bbi.getResidue()) + "" + bbi.getEdge(bbi.getPartnerResidue()));
                                                }
                                            } else{
                                                ReferenceStructureSymbol pairedReferenceStructureSymbol = referenceStructureSymbol.getPairedSymbol(bbi);
                                                int pairedPositionInTargetSequence = seq.getSymbol(referenceStructure.getIndex(pairedReferenceStructureSymbol)).getPositionInSequence();

                                                if (positionInTargetSequence < pairedPositionInTargetSequence /*to avoid to do twice the same stuff*/&& !seq.getSymbol(i).isGap() && !seq.getSymbol(referenceStructure.getIndex(pairedReferenceStructureSymbol)).isGap()) {
                                                    Location location = new Location(new Location(positionInTargetSequence), new Location(pairedPositionInTargetSequence));
                                                    tertiaryInteractionsTypes.put(location, bbi.getOrientation() + "" + bbi.getEdge(bbi.getResidue()) + "" + bbi.getEdge(bbi.getPartnerResidue()));
                                                }
                                            }
                                        }
                                    }

                                    //first we infer the 2D structure. If some secondary interactions are lonely, they become tertiary interactions
                                    SecondaryStructure ss  = Modeling2DUtils.getSecondaryStructure(mediator, "2D", seq.getMolecule(), secondaryInteractions, secondaryInteractionsTypes, new ArrayList<Location>(), new HashMap<Location, String>());

                                    //now we add the tertiary interactions to the infered 2D
                                    for (Map.Entry<Location,String> e: tertiaryInteractionsTypes.entrySet()) {
                                        char[] chars = e.getValue().toCharArray();
                                        ss.addTertiaryInteraction(e.getKey(), chars[0], chars[1], chars[2]);
                                    }
                                    //now we derive the tertiary structure (if any)
                                    if (ss != null && mediator.getTertiaryStructure() != null) {
                                        TertiaryStructure newTertiaryStructure = new TertiaryStructure(seq.getMolecule());
                                        //System.out.println(seq.getMolecule().printSequence());
                                        Location targetSequence = new Location();
                                        int pos = 0;
                                        List<Residue3D> referenceResidues3D = new ArrayList<Residue3D>();
                                        for (int i = 0 ; i < mediator.getAlignmentCanvas().getMainAlignment().getLength() ; i++) {

                                            Symbol targetSymbol = seq.getSymbol(i),
                                                    referenceSymbol = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(i);
                                            if (!targetSymbol.isGap())
                                                pos++;

                                            if (!targetSymbol.isGap() && !referenceSymbol.isGap()) {
                                                Residue3D referenceResidue3D = mediator.getTertiaryStructure().getResidue3DAt(referenceSymbol.getPositionInSequence());
                                                if (referenceResidue3D != null) {
                                                    referenceResidues3D.add(referenceResidue3D);
                                                    targetSequence.add(pos);
                                                }
                                            } else if (targetSymbol.isGap() || referenceSymbol.isGap()) {
                                                if (!referenceResidues3D.isEmpty()) {
                                                    Modeling3DUtils.thread(mediator, newTertiaryStructure, targetSequence.getStart(), targetSequence.getLength(), Modeling3DUtils.RNA, referenceResidues3D);
                                                }
                                                targetSequence = new Location();
                                                referenceResidues3D = new ArrayList<Residue3D>();
                                            }
                                        }

                                        if (!referenceResidues3D.isEmpty()) { //the last set of residues
                                            //System.out.println(targetSequence.toString());
                                            Modeling3DUtils.thread(mediator, newTertiaryStructure, targetSequence.getStart(), targetSequence.getLength(), Modeling3DUtils.RNA, referenceResidues3D);
                                        }
                                        ss.setLinkedTs(newTertiaryStructure);
                                    }

                                    mediator.getSecondaryCanvas().closeSession();
                                    if (mediator.getChimeraDriver() != null)
                                        mediator.getChimeraDriver().closeSession();

                                    mediator.loadRNASecondaryStructure(ss, false, true);
                                    mediator.getMoleculesList().setMoleculeAsNewReference(seq, ss);

                                    if (ss.getLinkedTs() != null) {
                                        mediator.setTertiaryStructure(ss.getLinkedTs());
                                        if (mediator.getChimeraDriver() != null) {
                                            File tmpF = IoUtils.createTemporaryFile("ts.pdb");
                                            FileParser.writePDBFile(ss.getLinkedTs().getResidues3D(), true, new FileWriter(tmpF));
                                            mediator.getChimeraDriver().loadTertiaryStructure(tmpF);
                                        }
                                    }

                                } catch (Exception e) {
                                    mediator.getAssemble().getMessageBar().printException(e);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();

                            } else if ("Consensus structure".equals(choice)) {
                                if (mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().isUnbalanced()) {
                                    JOptionPane.showMessageDialog(null, "Your consensus 2D is unbalanced. Check the red characters in the consensus bracket notation!!");
                                } else {
                                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                    try {

                                        StringBuffer consensus = new StringBuffer(),
                                                alignedSequence = new StringBuffer();

                                        int firstIndex = 0,
                                                lastIndex =  mediator.getAlignmentCanvas().getMainAlignment().getLength()-1;

                                        for (int i=firstIndex ; i <= lastIndex ; i++) {
                                            consensus.append(mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().getSymbol(i).getSymbol());
                                            alignedSequence.append(seq.getSymbol(i).getSymbol());
                                        }

                                        SecondaryStructure ss  = Modeling2DUtils.getSecondaryStructure(mediator, alignedSequence.toString(), seq.getMolecule(), consensus.toString());
                                        mediator.getSecondaryCanvas().closeSession();
                                        mediator.getChimeraDriver().closeSession();

                                        mediator.loadRNASecondaryStructure(ss, false, true);
                                        mediator.getMoleculesList().setMoleculeAsNewReference(seq, ss);

                                    } catch (Exception e) {
                                        mediator.getAssemble().getMessageBar().printException(e);
                                    }

                                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();

                                }
                            }

                            return null;
                        }
                    }.execute();
                }
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.extractSubAlignment != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.extractSubAlignment.getBounds2D().getMinX()+startX, this.extractSubAlignment.getBounds2D().getBounds2D().getMinY()+startY, this.extractSubAlignment.getBounds2D().getWidth(), this.extractSubAlignment.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (mediator.getAlignmentCanvas().getMainAlignment() == null || mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules().size() < 2 || mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSelection().isEmpty())
                    return;
                new SwingWorker() {
                    protected Object doInBackground()  {
                        try {
                            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null,"Are you sure to quit the current Project?"))
                                return null;
                            else {
                                String referenceName = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().getName();
                                List<Symbol> selection = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSelection();
                                int firstIndex = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getIndex(selection.get(0)),
                                        lastIndex =  mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getIndex(selection.get(selection.size()-1));
                                StringBuffer fastaData = new StringBuffer();
                                for (AlignedMolecule alignedMolecule: mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules()) {
                                    fastaData.append(">"+alignedMolecule.getMolecule().getName().replaceAll("\\s","_")+"\n");
                                    fastaData.append(alignedMolecule.getSequence(firstIndex, lastIndex) + "\n");
                                }
                                if (mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules().size() > 1 && JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,"Would you like to recompute the alignment?")) {
                                    mediator.clearSession();
                                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                    Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> result = new Mlocarna(mediator).align(fastaData.toString(), referenceName.replaceAll("\\s","_"));
                                    SecondaryStructure reference2D = null;
                                    AlignedMolecule referenceMolecule = null;
                                    for (SecondaryStructure ss: result.getFirst().getSecond())
                                        if (ss.getMolecule().getName().equals(referenceName)) {
                                            reference2D = ss;
                                            break;
                                        }
                                    for (AlignedMolecule m: result.getSecond())
                                        if (m.getMolecule().getName().equals(referenceName)) {
                                            referenceMolecule = m;
                                            break;
                                        }
                                    ReferenceStructure referenceStructure = new ReferenceStructure(mediator, referenceMolecule, reference2D);
                                    result.getSecond().remove(referenceMolecule);
                                    StructuralAlignment alignment = new StructuralAlignment(mediator,  result.getFirst().getFirst(), referenceMolecule, referenceStructure,  result.getSecond());
                                    mediator.getAlignmentCanvas().setMainAlignment(alignment);
                                    mediator.loadRNASecondaryStructure(referenceStructure.getSecondaryStructure(), false, true);
                                }

                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            mediator.getAssemble().getMessageBar().printException(e);
                        }
                        mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                        return null;
                    }
                }.execute();

                return;
            }
            startY += buttonShape.getHeight()+5;
        }

    }

    public void mouseMoved(MouseEvent e, int startX, int startY) {
        if (this.lock != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.lock.getBounds2D().getMinX()+startX, this.lock.getBounds2D().getBounds2D().getMinY()+startY, this.lock.getBounds2D().getWidth(), this.lock.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overLock) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overLock) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Lock/unlock the alignment. Once unlocked, the sequences can be modified by clicking residues.");
                                mediator.getAlignmentCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getAlignmentCanvas().repaint();
                }
                overLock = true;
            } else {
                if (overLock)
                    mediator.getAlignmentCanvas().repaint();
                overLock = false;
            }
            startY += buttonShape.getHeight()+5;
        }


        if (this.importNewMolecules != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.importNewMolecules.getBounds2D().getMinX()+startX, this.importNewMolecules.getBounds2D().getBounds2D().getMinY()+startY, this.importNewMolecules.getBounds2D().getWidth(), this.importNewMolecules.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overImportNewMolecules) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overImportNewMolecules) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Import new sequences in the alignment.");
                                mediator.getAlignmentCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getAlignmentCanvas().repaint();
                }
                overImportNewMolecules = true;
            } else {
                if (overImportNewMolecules)
                    mediator.getAlignmentCanvas().repaint();
                overImportNewMolecules = false;
            }
            startY += buttonShape.getHeight()+10;
        }

        if (this.inferNew2D != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.inferNew2D.getBounds2D().getMinX()+startX, this.inferNew2D.getBounds2D().getBounds2D().getMinY()+startY, this.inferNew2D.getBounds2D().getWidth(), this.inferNew2D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overInfertNew2D) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overInfertNew2D) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Infer a new 2D/3D structure for a sequence in the alignment.");
                                mediator.getAlignmentCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getAlignmentCanvas().repaint();
                }
                overInfertNew2D = true;
            } else {
                if (overInfertNew2D)
                    mediator.getAlignmentCanvas().repaint();
                overInfertNew2D = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (this.extractSubAlignment != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.extractSubAlignment.getBounds2D().getMinX()+startX, this.extractSubAlignment.getBounds2D().getBounds2D().getMinY()+startY, this.extractSubAlignment.getBounds2D().getWidth(), this.inferNew2D.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (!overExtractSubAlignment) {
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (overExtractSubAlignment) {
                                tooltip = new ToolTip(mediator);
                                tooltip.setX(40);
                                tooltip.setHeight(40);
                                tooltip.setY((int) (buttonShape.getCenterY()));
                                tooltip.setText("Extract a subalignment from selection.");
                                mediator.getAlignmentCanvas().repaint();
                            }
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    mediator.getAlignmentCanvas().repaint();
                }
                overExtractSubAlignment = true;
            } else {
                if (overExtractSubAlignment)
                    mediator.getAlignmentCanvas().repaint();
                overExtractSubAlignment = false;
            }
            startY += buttonShape.getHeight()+5;
        }

        if (!overLock && !overImportNewMolecules && !overInfertNew2D && !overExtractSubAlignment) {
            this.tooltip = null;
            this.mediator.getAlignmentCanvas().repaint();
        }

    }

    public double getWidth() {
        return this.lock.getBounds2D().getWidth();
    }

}
