package fr.unistra.ibmc.assemble2.gui;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.io.AssembleProject;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.Modeling2DUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class MoleculesList extends JXTable implements MouseListener, DragSourceListener, DropTargetListener, DragGestureListener {

    private Mediator mediator;
    private DefaultTableModel model;
    private JPopupMenu popupMenu;
    private DragSource dragSource;
    private DropTarget dropTarget;
    private int startIndex = -1,  overIndex = -1;
    private static DataFlavor localObjectFlavor;
    static {
        try {
            localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private DataFlavor[] supportedFlavors = {localObjectFlavor};
    private Map<String,Color> cluster2Colors;
    private String lastClusterName="";

    public MoleculesList(final Mediator mediator) {
        this.cluster2Colors = new HashMap<String,Color>();
        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.setSortable(false);
        this.setEditable(false);
        this.dragSource = new DragSource();
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        this.dropTarget = new DropTarget(this, this);
        this.mediator = mediator;
        this.mediator.setMoleculesList(this);
        this.model = new DefaultTableModel();
        this.model.addColumn("Label");
        this.model.addColumn("Sequence Name");
        this.addMouseListener(this);
        this.setModel(this.model);
        this.popupMenu = new JPopupMenu();
        JMenuItem menuItem;

        menuItem = new JMenuItem("Create new molecules from selection");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /*for (int i : MoleculesList.this.getSelectedRows()) {
                    java.util.List<Integer> selectedPositions = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getSelectedPositions();
                    StringBuffer newSequence = null;
                    int previousPos = -1;
                    for (int pos:selectedPositions) {
                        if (pos-1 != previousPos) {
                            if (newSequence != null)
                                Molecule rna  = new Molecule(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getMolecule().getName(), newSequence.toString());
                            newSequence = new StringBuffer();
                        }
                        newSequence.append(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getMolecule().getResidueAt(pos));
                        previousPos = pos;
                    }
                    if (newSequence != null)
                            Molecule  rna  = new Molecule(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getMolecule().getName(), newSequence.toString());
                }*/
            }
        });
        //this.popupMenu.add(menuItem);
        JMenu cluster = new JMenu("Cluster");
        //this.popupMenu.add(cluster);

        cluster.add(new ColorMenu("Create Cluster",true));

        cluster.add(new ColorMenu("Change Color",false));

        menuItem = new JMenuItem("Erase Cluster");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /*new ParadiseTask((Application) mediator.getS2sApplication(), mediator.getS2sApplication().getProgressMonitor()) {
                    protected Void doInBackground() throws InterruptedException {
                        for (int i : MoleculesList.this.getSelectedRows())
                            if (MoleculesList.this.isCellSelected(i, 1))
                                mediator.getAlignmentCanvas().getMainAlignment().getStructuralAlignment().removeFromCluster(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getMolecule());
                        MoleculesList.this.repaint();
                        this.finished();
                        return null;
                    }
                }.execute();*/
            }
        });
        cluster.add(menuItem);

        menuItem = new JMenuItem("New 2D from consensus"){
            public void paintComponent(Graphics graphics) {
                this.setEnabled(MoleculesList.this.getSelectedRows().length == 1 && MoleculesList.this.getSelectedRows()[0] == 0 );
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mediator.getAssemble().getCurrentAssembleProject() != null) {
                    final AssembleProject parser = new AssembleProject(mediator, mediator.getAssemble().getCurrentAssembleProject().getLocation());
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
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        try {
                            List<Symbol> selection = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSelection();
                            StringBuffer consensus = new StringBuffer(),
                                    sequence = new StringBuffer();

                            int firstIndex = -1,
                                    lastIndex =  -1;

                            if (!selection.isEmpty()) { //new 2D based on the current selection.
                                firstIndex = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getIndex(selection.get(0));
                                lastIndex =  mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getIndex(selection.get(selection.size()-1));

                            } else {
                                firstIndex = 0;
                                lastIndex =  mediator.getAlignmentCanvas().getMainAlignment().getLength()-1;
                            }
                            for (int i=firstIndex ; i <= lastIndex ; i++) {
                                consensus.append(mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().getSymbol(i).getSymbol());
                                sequence.append(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(i).getSymbol());
                            }

                            //we check if unbalanced
                            if (StringUtils.countMatches(consensus, "(") != StringUtils.countMatches(consensus, ")"))
                                JOptionPane.showMessageDialog(null, "Your consensus 2D is unbalanced!!");
                            else {
                                Molecule m = new Molecule(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().getName(), sequence.toString().replace("-",""));
                                m.setId(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().getId());
                                m.setBasicDBObject(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().getBasicDBObject());
                                m.setPlusOrientation(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().isPlusOrientation());
                                if (mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().isGenomicAnnotation()) {
                                    m.isGenomicAnnotation(true);
                                    if (m.isPlusOrientation())
                                        m.setFivePrimeEndGenomicPosition(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(firstIndex).getPositionInSequence());
                                    else
                                        m.setFivePrimeEndGenomicPosition(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(lastIndex).getPositionInSequence());
                                } else {
                                    m.setFivePrimeEndGenomicPosition(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(firstIndex).getPositionInSequence());
                                }

                                for (Annotation a: mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().getAnnotations())
                                    m.addAnnotation(new Annotation(a.getBasicDBObject()));

                                mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().setMolecule(m);

                                SecondaryStructure ss  = Modeling2DUtils.getSecondaryStructure(mediator, sequence.toString(), m, consensus.toString());

                                if (mediator.getTertiaryStructure() != null)
                                    ss.setLinkedTs(mediator.getTertiaryStructure());

                                mediator.loadRNASecondaryStructure(ss, false, true);
                            }
                        } catch (Exception e) {
                            mediator.getAssemble().getMessageBar().printException(e);
                        }
                        return null;

                    }
                }.execute();
            }
        });

        //this.popupMenu.add(menuItem);

        menuItem = new JMenuItem("Duplicate") {
            public void paintComponent(Graphics graphics) {
                boolean referenceMoleculeSelected = false;
                for (int row:MoleculesList.this.getSelectedRows()) {
                    if (row == 0) {
                        referenceMoleculeSelected = true;
                        break;
                    }
                }
                this.setEnabled(MoleculesList.this.getSelectedRows().length == 1 && referenceMoleculeSelected);
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (Assemble.HELP_MODE) {
                    IoUtils.openBrowser("http://bioinformatics.org/assemble/panels.html#aligned_rnas");
                    return;
                }
                AlignedMolecule ref = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence();

                Molecule copy = new Molecule("copy of "+ref.getMolecule().getName(), ref.getMolecule().printSequence());
                AlignedMolecule copyAligned = new AlignedMolecule(mediator, copy, ref.getSequence());
                mediator.getAlignmentCanvas().getMainAlignment().addBiologicalSequence(copyAligned);
                mediator.getMoleculesList().addRow(copyAligned.getMolecule());
                for (AlignmentView v:mediator.getAlignmentCanvas().getAlignmentViews()) {
                    v.setViewY(0);
                    AssembleConfig.setNumberOfSequencesToDisplay(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceCount());
                    mediator.getAlignmentCanvas().repaint();
                }
                mediator.getAlignmentCanvas().repaint();
            }
        });
        this.popupMenu.add(menuItem);

        /*menuItem = new JMenuItem("Create new Annotation..."){
            public void paintComponent(Graphics graphics) {
                List<Symbol> selection = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSelection();
                this.setEnabled(MoleculesList.this.getSelectedRows().length == 1 && MoleculesList.this.getSelectedRows()[0] == 0 && !selection.isEmpty() &&  mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().isGenomicAnnotation());
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        try {
                            List<Symbol> selection = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSelection();
                            final JComboBox strand = new JComboBox(new String[]{"Plus", "Minus"});
                            final JComboBox _class = new JComboBox(Assemble.genomic_features_classes);
                            final JComponent[] inputs = new JComponent[] {
                                    new JLabel("Strand:"),
                                    strand,
                                    new JLabel("Class:"),
                                    _class
                            };
                            int result = JOptionPane.showConfirmDialog(null, inputs, "Create Annotation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                            if(result == JOptionPane.OK_OPTION) {
                                String _id = new ObjectId().toString();
                                if (mediator.getGenomicMongo() != null) {
                                    BasicDBObject query = new BasicDBObject();
                                    query.put("_id", mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(MoleculesList.this.getSelectedRow()).getMolecule().getId());
                                    DBObject currentAnnotation = mediator.getGenomicMongo().getCollection("ncRNAs").findOne(query),
                                            newAnnotation =  new BasicDBObject();
                                    newAnnotation.put("_id", _id);
                                    newAnnotation.put("class", (String) _class.getSelectedItem());
                                    newAnnotation.put("genomeName", (String)currentAnnotation.get("genomeName"));
                                    newAnnotation.put("genomicStrand", (String)currentAnnotation.get("genomicStrand"));
                                    newAnnotation.put("genome", (String)currentAnnotation.get("genome"));
                                    newAnnotation.put("organism", (String)currentAnnotation.get("organism"));

                                    query = new BasicDBObject("_id", ((String)currentAnnotation.get("genome")).split("@")[0]);
                                    DBObject genome = mediator.getGenomicMongo().getCollection("genomes").find(query).iterator().next();

                                    String sequence = null;

                                    if (mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().isPlusOrientation()) {
                                        sequence = ((String)genome.get("sequence")).substring(selection.get(0).getPositionInSequence()-1, selection.get(selection.size()-1).getPositionInSequence());
                                        BasicDBList genomicPositions = new BasicDBList();
                                        genomicPositions.add(selection.get(0).getPositionInSequence());
                                        genomicPositions.add(selection.get(selection.size()-1).getPositionInSequence());
                                        newAnnotation.put("genomicPositions", genomicPositions);
                                    }
                                    else {
                                        sequence = ((String)genome.get("sequence")).substring(selection.get(selection.size()-1).getPositionInSequence()-1, selection.get(0).getPositionInSequence());
                                        BasicDBList genomicPositions = new BasicDBList();
                                        genomicPositions.add(selection.get(selection.size()-1).getPositionInSequence());
                                        genomicPositions.add(selection.get(0).getPositionInSequence());
                                        newAnnotation.put("genomicPositions", genomicPositions);
                                    }

                                    if (strand.getSelectedItem().equals("Minus")) {

                                        StringBuffer reverseComplement = new StringBuffer();

                                        for (char res: new StringBuffer(sequence).reverse().toString().toCharArray())
                                            switch (res) {
                                                case 'A': reverseComplement.append('T'); break;
                                                case 'T':
                                                case 'U': reverseComplement.append('A'); break;
                                                case 'G': reverseComplement.append('C'); break;
                                                case 'C': reverseComplement.append('G'); break;
                                                default : reverseComplement.append(res); break;
                                            }

                                        sequence = reverseComplement.toString();
                                    }

                                    newAnnotation.put("sequence", sequence);

                                    if (_class.getSelectedItem().equals("ncRNA")) {

                                        final JComboBox __class = new JComboBox(Assemble.ncRNA_classes),
                                                id = new JComboBox();

                                        __class.addItemListener(new ItemListener() {
                                            public void itemStateChanged(ItemEvent itemEvent) {
                                                id.removeAllItems();
                                                for (String ncRNA_id: Assemble.ncRNA_ids.get(itemEvent.getItem().toString()))
                                                    id.addItem(ncRNA_id);
                                            }
                                        });

                                        __class.setSelectedItem(Assemble.ncRNA_classes[0]);

                                        for (String ncRNA_id: Assemble.ncRNA_ids.get(Assemble.ncRNA_classes[0]))
                                            id.addItem(ncRNA_id);

                                        final JComponent[] _inputs = new JComponent[] {
                                                new JLabel("ncRNA class:"),
                                                __class,
                                                new JLabel("ncRNA id/name:"),
                                                id,
                                        };



                                        result = JOptionPane.showConfirmDialog(null, _inputs, "Create ncRNA", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                                        if(result == JOptionPane.OK_OPTION) {
                                            newAnnotation.put("name", (String) id.getSelectedItem());
                                            newAnnotation.put("class", (String) __class.getSelectedItem());
                                            newAnnotation.put("last_update", new Date().getTime());
                                            mediator.getGenomicMongo().getCollection("ncRNAs").insert(newAnnotation);
                                            mediator.getGenomicAnnotationsPanel().addRow((BasicDBObject)newAnnotation);
                                            mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().addAnnotation(new Annotation(newAnnotation));

                                        }

                                    } else {
                                        mediator.getGenomicMongo().getCollection("annotations").insert(newAnnotation);
                                        mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().addAnnotation(new Annotation(newAnnotation));

                                    }

                                }
                                mediator.getAlignmentCanvas().repaint();
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return null;

                    }
                }.execute();
            }
        });*/

        //this.popupMenu.add(menuItem);

        /*menuItem = new JMenuItem("Set Molecule as reference") {
            public void paintComponent(Graphics graphics) {
                this.setEnabled(MoleculesList.this.getSelectedRows().length == 1 && MoleculesList.this.getSelectedRows()[0] !=0 );
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Map<String, File> secondaryStructures = null;
                String[] choices = null;
                final AlignedMolecule seq = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(MoleculesList.this.getSelectedRow());
                final AssembleProjectParser parser = new AssembleProjectParser(mediator, mediator.getAssemble().getCurrentAssembleProject());
                if (mediator.getAssemble().getCurrentAssembleProject() != null) { //if the current project has been saved, then we ask the user to save the current 2D/3D AND we can propose him/her to load precomputed 2Ds (previously saved)
                    if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Do you want to save the current 2D/3D?", "Save 2D/3D", JOptionPane.YES_NO_OPTION)) {
                        new javax.swing.SwingWorker() {
                            @Override
                            protected Object doInBackground() {
                                try {
                                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity();
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

                    try {
                        secondaryStructures = parser.getAllSecondaryStructuresForMolecule(seq.getMolecule());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (secondaryStructures == null || secondaryStructures.isEmpty())
                    choices = new String[]{"Derived from consensus structure", "Derived from reference structure", "De novo prediction"};
                else
                    choices = new String[]{"Precomputed structure", "Derived from consensus structure", "Derived from reference structure", "De novo prediction"};

                String choice = (String) JOptionPane.showInputDialog(null, "You need a 2D", "You need a 2D", JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
                if (choice != null) {
                    if ("Precomputed structure".equals(choice)) {
                        SecondaryStructure ss = null;
                        if (secondaryStructures.size() > 1) {
                            String[] names = new String[secondaryStructures.size()];
                            int i = 0;
                            for (String name : secondaryStructures.keySet())
                                names[i++] = name;
                            String name = (String) JOptionPane.showInputDialog(null, "Choose a Secondary Structure", "Choose a Secondary Structure", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
                            if (name != null) {
                                try {
                                    parser.parseRnamlFile(secondaryStructures.get(name));
                                    ss = parser.getSecondaryStructures().get(0);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                parser.parseRnamlFile(secondaryStructures.entrySet().iterator().next().getValue());
                                ss = parser.getSecondaryStructures().get(0);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        if (ss != null) {
                            mediator.getSecondaryCanvas().getActivityToolBar().startActivity();
                            mediator.getSecondaryCanvas().closeSession();
                            mediator.getChimeraDriver().closeSession();
                            mediator.loadRNASecondaryStructure(ss, false, true);
                            MoleculesList.this.setMoleculeAsNewReference(seq, mediator.getSecondaryStructure());
                            if (ss.getLinkedTs() != null) {
                                mediator.setTertiaryStructure(ss.getLinkedTs());
                                if (parser.getChimeraSession() == null) {
                                    try {
                                        File tmpF = IoUtils.createTemporaryFile("ts.pdb");
                                        FileParser.writePDBFile(ss.getLinkedTs().getResidues3D(), true, new FileWriter(tmpF));
                                        mediator.getChimeraDriver().loadTertiaryStructure(tmpF);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                            MoleculesList.this.setMoleculeAsNewReference(seq, ss);
                        }
                        mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    } else if ("De novo prediction".equals(choice)) {
                        mediator.getSecondaryCanvas().getActivityToolBar().startActivity();
                        new SwingWorker() {
                            @Override
                            protected Object doInBackground() {
                                try {
                                    mediator.getSecondaryCanvas().closeSession();
                                    mediator.getChimeraDriver().closeSession();
                                    SecondaryStructure ss = new Contrafold(mediator).fold(seq.getMolecule());
                                    if (ss != null) {
                                        mediator.loadRNASecondaryStructure(ss, false, true);
                                        MoleculesList.this.setMoleculeAsNewReference(seq, mediator.getSecondaryStructure());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    } else if ("Derived from reference structure".equals(choice)) {
                        mediator.getSecondaryCanvas().getActivityToolBar().startActivity();
                        new SwingWorker() {
                            @Override
                            protected Object doInBackground() {
                                try {
                                    SecondaryStructure ss = mediator.inferNew2DAnd3D(seq, 0, mediator.getAlignmentCanvas().getMainAlignment().getLength() - 1);

                                    mediator.getSecondaryCanvas().closeSession();
                                    mediator.getChimeraDriver().closeSession();

                                    mediator.loadRNASecondaryStructure(ss, false, true);
                                    mediator.getMoleculesList().setMoleculeAsNewReference(seq, ss);

                                    try {
                                        if (ss.getLinkedTs() != null) {
                                            mediator.setTertiaryStructure(ss.getLinkedTs());
                                            File tmpF = IoUtils.createTemporaryFile("ts.pdb");
                                            FileParser.writePDBFile(ss.getLinkedTs().getResidues3D(), true, new FileWriter(tmpF));
                                            mediator.getChimeraDriver().loadTertiaryStructure(tmpF);
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    } else if ("Derived from consensus structure".equals(choice)) {
                        if (mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().isUnbalanced()) {
                            JOptionPane.showMessageDialog(null, "Your consensus 2D is unbalanced. Check the red characters in the bracket notation!!");
                        } else {
                            mediator.getSecondaryCanvas().getActivityToolBar().startActivity();
                            new SwingWorker() {
                                @Override
                                protected Object doInBackground() {
                                    try {
                                        mediator.getSecondaryCanvas().closeSession();
                                        mediator.getChimeraDriver().closeSession();
                                        StringBuffer consensus = new StringBuffer(),
                                                alignedSequence = new StringBuffer();

                                        int firstIndex = 0,
                                                lastIndex =  mediator.getAlignmentCanvas().getMainAlignment().getLength()-1;

                                        for (int i=firstIndex ; i <= lastIndex ; i++) {
                                            consensus.append(mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().getSymbol(i).getSymbol());
                                            alignedSequence.append(seq.getSymbol(i).getSymbol());
                                        }

                                        Molecule m = new Molecule(seq.getMolecule().getName(), alignedSequence.toString().replace("-",""));
                                        m.setPlusOrientation(seq.getMolecule().isPlusOrientation());
                                        m.setId(seq.getMolecule().getId());
                                        m.setBasicDBObject(seq.getMolecule().getBasicDBObject());

                                        if (seq.getMolecule().isGenomicAnnotation()) {
                                            m.isGenomicAnnotation(true);
                                            if (m.isPlusOrientation())
                                                m.setFivePrimeEndGenomicPosition(seq.getSymbol(firstIndex).getPositionInSequence());
                                            else
                                                m.setFivePrimeEndGenomicPosition(seq.getSymbol(lastIndex).getPositionInSequence());
                                        } else
                                            m.setFivePrimeEndGenomicPosition(seq.getSymbol(firstIndex).getPositionInSequence());

                                        for (Annotation a: mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().getAnnotations())
                                            m.addAnnotation(new Annotation(a.getBasicDBObject()));

                                        seq.setMolecule(m);

                                        SecondaryStructure ss  = Modeling2DUtils.getSecondaryStructure(mediator, alignedSequence.toString(), m, consensus.toString());

                                        mediator.loadRNASecondaryStructure(ss, false, true);
                                        mediator.getMoleculesList().setMoleculeAsNewReference(seq, ss);

                                    } catch (Exception e) {
                                        mediator.getAssemble().getMessageBar().printException(e);
                                    }

                                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                    return null;
                                }
                            }.execute();
                        }
                    }
                }

            }
        });
        this.popupMenu.add(menuItem);*/

        menuItem = new JMenuItem("Remove selected Molecules") {
            public void paintComponent(Graphics graphics) {
                boolean referenceMoleculeSelected = false;
                for (int row:MoleculesList.this.getSelectedRows()) {
                    if (row == 0) {
                        referenceMoleculeSelected = true;
                        break;
                    }
                }
                this.setEnabled(!referenceMoleculeSelected);
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (Assemble.HELP_MODE) {
                    IoUtils.openBrowser("http://bioinformatics.org/assemble/panels.html#aligned_rnas");
                    return;
                }
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Are you sure to remove the selected sequences?")) {
                    int numRows = MoleculesList.this.getRowCount();
                    //the user cannot remove the first sequence (the reference molecule)
                    for (int i = numRows - 1; i >= 1; i--) {
                        if (MoleculesList.this.isCellSelected(i, 1)) {
                            mediator.getAlignmentCanvas().removeBiologicalSequenceAt(i);
                            MoleculesList.this.model.removeRow(i);
                        }
                    }
                }
            }
        });
        this.popupMenu.add(menuItem);

        menuItem = new JMenuItem("Search motif");
        menuItem.addActionListener(new ActionListener() {

            private String lastMotif = "";

            public void actionPerformed(ActionEvent e) {
                if (Assemble.HELP_MODE) {
                    IoUtils.openBrowser("http://bioinformatics.org/assemble/panels.html#aligned_rnas");
                    return;
                }
                String motif = JOptionPane.showInputDialog("Enter your motif", this.lastMotif);
                if (motif != null && motif.length() != 0) {
                    for (int i : MoleculesList.this.getSelectedRows()) {
                        mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).clearSelectedPositions();
                        mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).searchFor(motif);
                        if (mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i) == mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()) {
                            mediator.getSecondaryCanvas().clearSelection();
                            List<Integer> selectedPositions = new ArrayList<Integer>();
                            for (Symbol s: mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getSelection())
                                if (!s.isGap())
                                    selectedPositions.add(s.getPositionInSequence());
                            mediator.getSecondaryCanvas().select(selectedPositions);
                        }

                    }
                    mediator.getAlignmentCanvas().repaint();
                    this.lastMotif = motif;
                }
            }
        });
        this.popupMenu.add(menuItem);

        menuItem = new JMenuItem("Rename Molecule") {
            public void paintComponent(Graphics graphics) {
                this.setEnabled(MoleculesList.this.getSelectedRows().length == 1);
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (Assemble.HELP_MODE) {
                    IoUtils.openBrowser("http://bioinformatics.org/assemble/panels.html#aligned_rnas");
                    return;
                }
                int i = MoleculesList.this.getSelectedRow();
                String name = JOptionPane.showInputDialog("Enter your new name",mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getMolecule().getName());
                if (name != null && name.length() != 0 && MoleculesList.this.isCellSelected(i, 1)) {
                    mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getMolecule().setName(name);
                    MoleculesList.this.model.setValueAt(name,i,1);
                    MoleculesList.this.model.fireTableRowsUpdated(i,i);
                }
            }
        });
        this.popupMenu.add(menuItem);

        menuItem = new JMenuItem("Display in Alignment") {
            public void paintComponent(Graphics graphics) {
                boolean referenceMoleculeSelected = false;
                for (int row:MoleculesList.this.getSelectedRows()) {
                    if (row == 0) {
                        referenceMoleculeSelected = true;
                        break;
                    }
                }
                this.setEnabled(!referenceMoleculeSelected);
                super.paintComponent(graphics);
            }
        };

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Assemble.HELP_MODE) {
                    IoUtils.openBrowser("http://bioinformatics.org/assemble/panels.html#aligned_rnas");
                    return;
                }
                for (AlignmentView v:mediator.getAlignmentCanvas().getAlignmentViews()) {
                    v.setViewY(0);
                    AssembleConfig.setNumberOfSequencesToDisplay(MoleculesList.this.getSelectedRow()+1);
                    mediator.getAlignmentCanvas().repaint();
                }
            }
        });
        this.popupMenu.add(menuItem);
    }

    public boolean isSelected(Molecule m) {
        int[] indices = this.getSelectedRows();
        for (int i = 0 ; i < indices.length ; i++ )
            if (mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(indices[i]).getMolecule().equals(m))
                return true;
        return false;
    }

    public void setMoleculeAsNewReference(AlignedMolecule seq, SecondaryStructure ss) {
        //we update the model of the SequencesList
        int rowToMove = -1;
        for (int i = 0 ; i < model.getRowCount() ; i++) {
            if (model.getValueAt(i, 1).equals(seq.getMolecule())) {
                rowToMove = i;
                break;
            }
        }
        model.moveRow(rowToMove, rowToMove, 0);
        MoleculesList.this.setRowSelectionInterval(0, 0);
        //we update the alignment
        mediator.getAlignmentCanvas().getMainAlignment().setReferenceMolecule(seq,ss);
        mediator.getAlignmentCanvas().repaint();

        //if the sequence moved is in a cluster, all the sequences are moved
        /*String cluster = mediator.getAlignmentCanvas().getMainAlignment().getCluster(seq.getMolecule());
        if (cluster != null) {
            int newPosition = 1;
            for (int i=1 ; i< MoleculesList.this.getRowCount();i++) {
                if (cluster.equals(mediator.getAlignmentCanvas().getMainAlignment().getCluster(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getMolecule()))) {
                    model.moveRow(i, i, newPosition);
                    mediator.getAlignmentCanvas().getMainAlignment().moveSequence(newPosition, mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i));
                    mediator.getAlignmentCanvas().repaint();
                    newPosition++;
                }
            }
        }*/
    }

    public Map<String, Color> getCluster2Colors() {
        return this.cluster2Colors;
    }

    public void addCluster2Color(String clusterName, Color color) {
        this.cluster2Colors.put(clusterName,color);
    }

    public void addRow(Molecule m) {
        this.model.addRow(new Object[]{"", m});
    }

    public void removeRow(Molecule m) {
        for (int i = 0 ; i < model.getRowCount() ; i++) {
            if (model.getValueAt(i, 1).equals(m)) {
                mediator.getAlignmentCanvas().removeBiologicalSequenceAt(i);
                MoleculesList.this.model.removeRow(i);
                break;
            }
        }
    }

    public void clearList() {
        int size = this.model.getRowCount();
        for (int i = 0; i < size; i++) {
            this.model.removeRow(0);
        }
        this.cluster2Colors.clear();
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        if (row == overIndex && column == 1) {
            return new ReorderTableRenderer();
        } else if (column == 0) {
            return new FirstColumnTableRenderer();
        }
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
        if (e.isPopupTrigger()) {
            this.popupMenu.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }

    public void dragEnter(DragSourceDragEvent dsde) {

    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.getSource() != dropTarget) {
            dtde.rejectDrag();
        } else {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        }
    }

    public void dragOver(DropTargetDragEvent dtde) {
        if (dtde.getSource() != dropTarget) {
            dtde.rejectDrag();
        } else {
            Point dragPoint = dtde.getLocation();
            int index = this.rowAtPoint(dragPoint);
            if (index == -1 || index == 0) {
                return;
            } else {
                overIndex = index;
                repaint();
            }
        }
    }

    public void drop(DropTargetDropEvent dtde) {
        if (dtde.getSource() != dropTarget) {
            dtde.rejectDrop();
        } else {
            if (overIndex == -1 || overIndex == 0 || overIndex == startIndex) {
                return;
            } else {
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                model.moveRow(startIndex, startIndex, overIndex);
                MoleculesList.this.setRowSelectionInterval(overIndex, overIndex);
                mediator.getAlignmentCanvas().getMainAlignment().moveSequence(overIndex, mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(startIndex));
                mediator.getAlignmentCanvas().repaint();
            }
        }
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        Point clickPoint = dge.getDragOrigin();
        int index = this.rowAtPoint(clickPoint);
        //the user cannot drag the first sequence (the reference molecule)
        if (index == -1 || index == 0) {
            return;
        } else {
            startIndex = index;
            dragSource.startDrag(dge, Cursor.getDefaultCursor(), new MyTransferable(null), this);
        }
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        startIndex = -1;
        overIndex = -1;
        repaint();
    }

    private class MyTransferable implements Transferable {

        private Object object;

        private MyTransferable(Object object) {
            this.object = object;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return supportedFlavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(localObjectFlavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return object;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }

    private class ReorderTableRenderer extends JLabel implements TableCellRenderer {

        public ReorderTableRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawLine(0, 0, getSize().width, 0);
        }
    }

    private class FirstColumnTableRenderer extends JLabel implements TableCellRenderer {

        public FirstColumnTableRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String cluster = null;
            if (mediator.getAlignmentCanvas().getMainAlignment() != null)  {
                cluster = mediator.getAlignmentCanvas().getMainAlignment().getCluster(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(row).getMolecule());
                this.setText("S" + (row + 1)+ (cluster != null ? " ("+cluster+")" :""));
                if (cluster != null && cluster2Colors.containsKey(cluster)) {
                    this.setBackground(cluster2Colors.get(cluster));
                    this.setForeground(Color.WHITE);
                }
                else {
                    this.setBackground(Color.WHITE);
                    this.setForeground(Color.BLACK);
                }
            }
            return this;
        }


    }

    private  class ColorMenu extends JMenu {
        protected Border unselectedBorder;

        protected Border selectedBorder;

        protected Border activeBorder;

        protected Hashtable paneTable;

        protected ColorPane colorPane;
        protected boolean createNewCluster;

        public ColorMenu(String name, boolean createNewCluster) {
            super(name);
            this.createNewCluster = createNewCluster;
            unselectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                    getBackground()), new BevelBorder(BevelBorder.LOWERED,
                    Color.white, Color.gray));
            selectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                    Color.red), new MatteBorder(1, 1, 1, 1, getBackground()));
            activeBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1,
                    Color.blue), new MatteBorder(1, 1, 1, 1, getBackground()));

            JPanel p = new JPanel();
            p.setBorder(new EmptyBorder(5, 5, 5, 5));
            p.setLayout(new GridLayout(8, 8));
            paneTable = new Hashtable();

            int[] values = new int[] { 0 ,128, 192, 255 };

            for (int r = 0; r < values.length; r++) {
                for (int g = 0; g < values.length; g++) {
                    for (int b = 0; b < values.length; b++) {
                        Color c = new Color(values[r], values[g], values[b]);
                        ColorPane pn = new ColorPane(c);
                        p.add(pn);
                        paneTable.put(c, pn);
                    }
                }
            }
            add(p);
        }

        public void setColor(Color c) {
            Object obj = paneTable.get(c);
            if (obj == null)
                return;
            if (colorPane != null)
                colorPane.setSelected(false);
            colorPane = (ColorPane) obj;
            colorPane.setSelected(true);
        }

        public Color getColor() {
            if (colorPane == null)
                return null;
            return colorPane.getColor();
        }

        public void doSelection() {
            fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                    getActionCommand()));
        }

        private class ColorPane extends JPanel implements MouseListener {
            protected Color color;

            protected boolean isSelected;

            public ColorPane(Color c) {
                color = c;
                setBackground(c);
                setBorder(unselectedBorder);
                String msg = "R " + c.getRed() + ", G " + c.getGreen() + ", B "
                        + c.getBlue();
                setToolTipText(msg);
                addMouseListener(this);
            }

            public Color getColor() {
                return color;
            }

            public Dimension getPreferredSize() {
                return new Dimension(15, 15);
            }

            public Dimension getMaximumSize() {
                return getPreferredSize();
            }

            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            public void setSelected(boolean selected) {
                isSelected = selected;
                if (isSelected)
                    setBorder(selectedBorder);
                else
                    setBorder(unselectedBorder);
            }

            public boolean isSelected() {
                return isSelected;
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {
                setColor(color);
                MenuSelectionManager.defaultManager().clearSelectedPath();
                doSelection();
                if (createNewCluster) {
                    String cluster = JOptionPane.showInputDialog("Enter your Cluster Name",lastClusterName);
                    if (cluster != null && cluster.length() != 0) {
                        lastClusterName = cluster;
                        int[] indices = MoleculesList.this.getSelectedRows();
                        for (int i = 0 ; i < indices.length ; i++ )  {
                            if (MoleculesList.this.isCellSelected(indices[i], 1)) {
                                mediator.getAlignmentCanvas().getMainAlignment().addToCluster(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(indices[i]).getMolecule(), cluster);
                            }
                            if (i > 0 && indices[i] != indices[0]+i) {
                                //the two rows are not contiguous
                                //they have to be contiguous in a cluster
                                MoleculesList.this.model.moveRow(indices[i],indices[i],indices[0]+i);
                                MoleculesList.this.selectionModel.addSelectionInterval(indices[i - 1] + 1, indices[i - 1] + 1);
                                mediator.getAlignmentCanvas().getMainAlignment().moveSequence(indices[0]+i, mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(indices[i]));
                                mediator.getAlignmentCanvas().repaint();
                            }
                        }
                        cluster2Colors.put(cluster,color);
                        MoleculesList.this.setRowSelectionInterval(indices[0], indices[0]+indices.length-1);
                        MoleculesList.this.repaint();
                    }
                }
                else {
                    for (int i : MoleculesList.this.getSelectedRows())
                        if (MoleculesList.this.isCellSelected(i, 1)) {
                            String cluster = mediator.getAlignmentCanvas().getMainAlignment().getCluster(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(i).getMolecule());
                            if (cluster != null)
                                cluster2Colors.put(cluster,color);
                        }
                    MoleculesList.this.repaint();
                }
            }

            public void mouseEntered(MouseEvent e) {
                setBorder(activeBorder);
            }

            public void mouseExited(MouseEvent e) {
                setBorder(isSelected ? selectedBorder : unselectedBorder);
            }
        }
    }

}
