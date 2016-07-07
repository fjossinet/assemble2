package fr.unistra.ibmc.assemble2.gui;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.components.ColorMenu;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.io.computations.DataHandler;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import org.bson.types.ObjectId;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.*;
import java.util.List;

public class GenomicAnnotationsPanel extends JXTable implements MouseListener {

    private Mediator mediator;
    private MyModel model;
    private JPopupMenu popupMenu;
    private int startIndex = -1,  overIndex = -1;
    private GenomicDataHandler genomicDataHandler;
    private ColorRenderer colorRenderer;

    private class MyModel extends AbstractTableModel {

        private java.util.List<BasicDBObject> genomicAnnotations = new ArrayList<BasicDBObject>();

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < genomicAnnotations.size()) {
                BasicDBObject genomicAnnotation = genomicAnnotations.get(rowIndex);
                BasicDBList genomicPositions = ((BasicDBList)genomicAnnotation.get("genomicPositions"));
                if (columnIndex == 0)
                    return (String)genomicAnnotation.get("organism");
                else if (columnIndex == 1)
                    return (String)genomicAnnotation.get("genomeName");
                else if (columnIndex == 2)
                    return (String)genomicAnnotation.get("genomicStrand");
                else if (columnIndex == 3)
                    return (Integer)genomicPositions.get(0);
                else if (columnIndex == 4)
                    return (Integer)genomicPositions.get(1);
                else if (columnIndex == 5)
                    return (Integer)genomicPositions.get(1)-(Integer)genomicPositions.get(0)+1;
                /*else if (columnIndex == 6)
                    return (String)genomicAnnotation.get("name");*/
                else if (columnIndex == 6)
                    return (String)genomicAnnotation.get("class");
                else if (columnIndex == 7)
                    return (String)genomicAnnotation.get("source");
                else if (columnIndex == 8) {
                    if (genomicAnnotation.get("score") != null)
                        return (Double)genomicAnnotation.get("score");
                    else
                        return 0.0;
                }
                /*else if (columnIndex == 10)
                    try {
                        if (genomicAnnotation.get("last_update") != null)
                            return new Date((Long) genomicAnnotation.get("last_update"));
                        else
                            return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                else if (columnIndex == 11)
                    try {
                        if (genomicAnnotation.get("validated") != null)
                            return (Boolean)genomicAnnotation.get("validated");
                        else
                            return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }*/

                else
                    return Assemble.getColorForGenomicFeature((String)genomicAnnotation.get("class"));


            } else
                return null;

        }

        public int getColumnCount() {
            return 10;
        }

        public int getRowCount() {
            return genomicAnnotations.size();
        }

        @Override
        public String getColumnName(int i) {
            if (i == 0)
                return "Organism";
            else if (i == 1)
                return "Scaffold/Chromosome";
            else if (i == 2)
                return "Strand";
            else if (i == 3)
                return "Start";
            else if (i == 4)
                return "End";
            else if (i == 5)
                return "Length";
            /*else if (i == 6)
                return "Name";*/
            else if (i == 6)
                return "Class";
            else if (i == 7)
                return "Source";
            else if (i == 8)
                return "Score";
            /*else if (i == 10)
                return "Last Update";
            else if (i == 11)
                return "Validated";*/
            else
                return "Color";
        }
    }

    public class ColorRenderer extends JLabel implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true);
        }

        public Component getTableCellRendererComponent( JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
            Color newColor = (Color)color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }
            return this;
        }
    }

    public void iWantMoreResiduesOnTheLeft() {
        this.genomicDataHandler.iWantMoreResiduesOnTheLeft();
    }

    public void iWantMoreResiduesOnTheRight() {
        this.genomicDataHandler.iWantMoreResiduesOnTheRight();
    }


    public void setGenomicDataHandler(GenomicDataHandler handler) {
        this.genomicDataHandler = handler;
    }

    public java.util.List<Symbol> iWantNextAnnotationOnTheLeft(Annotation most_downstream_annotation, Annotation most_upstream_annotation) {
        java.util.List<Symbol> symbols = new ArrayList<Symbol>();

        /*BasicDBObject query = new BasicDBObject("_id", currentAnnotation.genome_id);
        DBObject genome = mediator.getGenomicMongo().getCollection("genomes").find(query).iterator().next();

        char[] sequence = null;
        Symbol s = null;
        if (currentAnnotation.strand.equals("+")) {
            int genomic_start = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(0).getPositionInSequence();
            Annotation downstreamAnnotation = most_downstream_annotation.getDownstreamAnnotation();
            if (downstreamAnnotation != null) {
                sequence = ((String)genome.get("sequence")).substring(downstreamAnnotation.getStart()-1, genomic_start-1).toCharArray();

                int i=0;
                for (char res: sequence) {
                    s = new Symbol(res, mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence());
                    s.setPositionInSequence(downstreamAnnotation.getStart()+i);
                    symbols.add(s);
                    i++;
                }
            }
        }
        else {
            int genomic_start = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().size()-1).getPositionInSequence(),
                    genomic_end = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(0).getPositionInSequence();
            Annotation upstreamAnnotation = most_upstream_annotation.getUpstreamAnnotation();
            if (upstreamAnnotation != null) {
                sequence = new StringBuffer(((String)genome.get("sequence")).substring(genomic_end, upstreamAnnotation.getEnd())).reverse().toString().toCharArray();

                int i=0;
                for (char res: sequence) {
                    switch (res) {
                        case 'A': s = new Symbol('T', mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()); break;
                        case 'T':
                        case 'U': s = new Symbol('A', mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()); break;
                        case 'G': s = new Symbol('C', mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()); break;
                        case 'C': s = new Symbol('G', mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()); break;
                        default : s = new Symbol(res, mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence());
                    }
                    s.setPositionInSequence(upstreamAnnotation.getEnd()-i);
                    symbols.add(s);
                    i++;
                }
            }
        }*/
        return symbols;
    }

    public java.util.List<Symbol> iWantNextAnnotationOnTheRight(Annotation most_downstream_annotation, Annotation most_upstream_annotation) {
        java.util.List<Symbol> symbols = new ArrayList<Symbol>();

        /*BasicDBObject query = new BasicDBObject("_id", currentAnnotation.genome_id);
        DBObject genome = mediator.getGenomicMongo().getCollection("genomes").find(query).iterator().next();

        char[] sequence = null;
        Symbol s = null;
        if (currentAnnotation.strand.equals("+")) {
            int genomic_end = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().size()-1).getPositionInSequence();
            Annotation upstreamAnnotation = most_upstream_annotation.getUpstreamAnnotation();

            if (upstreamAnnotation != null) {
                sequence = ((String)genome.get("sequence")).substring(genomic_end, upstreamAnnotation.getEnd()).toCharArray();

                int i=0;
                for (char res: sequence) {
                    s = new Symbol(res, mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence());
                    s.setPositionInSequence(genomic_end+1+i);
                    symbols.add(s);
                    i++;
                }
            }
        }
        else {
            int genomic_start = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().size()-1).getPositionInSequence(),
                    genomic_end = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSymbol(0).getPositionInSequence();

            Annotation downstreamAnnotation = most_downstream_annotation.getDownstreamAnnotation();

            if (downstreamAnnotation != null) {
                sequence = new StringBuffer(((String)genome.get("sequence")).substring(downstreamAnnotation.getStart()-1, genomic_start-1)).reverse().toString().toCharArray();

                int i=0;
                for (char res: sequence) {
                    switch (res) {
                        case 'A': s = new Symbol('T', mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()); break;
                        case 'T':
                        case 'U': s = new Symbol('A', mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()); break;
                        case 'G': s = new Symbol('C', mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()); break;
                        case 'C': s = new Symbol('G', mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence()); break;
                        default : s = new Symbol(res, mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence());
                    }
                    s.setPositionInSequence(genomic_start-1-i);
                    symbols.add(s);
                    i++;
                }
            }
        }*/
        return symbols;
    }

    public void loadAnnotation(final DBObject selected_annotation) {
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                if (selected_annotation == null)
                    return null;
                mediator.clearSession();
                try {

                    if (selected_annotation.get("alignment") != null)  {
                        BasicDBObject alignment = genomicDataHandler.getAlignment(((String)selected_annotation.get("alignment")).split("@")[0]);
                        //FileParser.parseClustal(new StringReader((String) alignment.get("alignment")), mediator, (String) selected_annotation.get("_id"));
                        //we extract and add all the annotations linked to the the molecules loaded and localized between the genomic positions loaded

                        for (Molecule m: mediator.getAlignmentCanvas().getMainAlignment().getMolecules()) {

                            DBObject _selected_annotation =  m.getBasicDBObject();

                            if (_selected_annotation != null) { //some molecules loaded in Assemble2 are not related to any ncRNA stored in the database (think about all the orthologous ncRNAs during the annotation process of a genome using the RFAM data)

                                if (_selected_annotation.get("genome") != null) {//a ncRNA can have genomic details (positions, strand,...), but not linked to any genome

                                    m.isGenomicAnnotation(true);

                                    for (DBObject annotation : genomicDataHandler.getAnnotationsPerGenome(((String)_selected_annotation.get("genome")).split("@")[0])) {
                                        //IT SEEMS THAT LOADING ALL THE ANNOTATIONS DIRECTLY IS LESS CONSUMING THAN STEP BY STEP (SEE methods iWantMoreResiduesOnTheLeft/Right)
                                        //BUT THEN, FOR SOME MONGODB, I WILL NEED TO LAUNCH ASSEMBLE2 WITH MORE MEMORY
                                        //BasicDBList _genomicPositions = (BasicDBList)annotation.get("genomicPositions");
                                        //if ((Integer)_genomicPositions.get(0) >= (Integer)genomicPositions.get(0) && (Integer)_genomicPositions.get(0) <= (Integer)genomicPositions.get(1) || (Integer)_genomicPositions.get(1) >= (Integer)genomicPositions.get(0) && (Integer)_genomicPositions.get(1) <= (Integer)genomicPositions.get(1)) {
                                        m.addAnnotation(new Annotation(annotation));
                                        //}
                                    }

                                    for (DBObject annotation : genomicDataHandler.getncRNAsPerGenome(((String)_selected_annotation.get("genome")).split("@")[0])) {
                                        //IT SEEMS THAT LOADING ALL THE ANNOTATIONS DIRECTLY IS LESS CONSUMING THAN STEP BY STEP (SEE methods iWantMoreResiduesOnTheLeft/Right)
                                        //BUT THEN, FOR SOME MONGODB, I WILL NEED TO LAUNCH ASSEMBLE2 WITH MORE MEMORY
                                        //BasicDBList _genomicPositions = (BasicDBList)annotation.get("genomicPositions");
                                        //if ((Integer)_genomicPositions.get(0) >= (Integer)genomicPositions.get(0) && (Integer)_genomicPositions.get(0) <= (Integer)genomicPositions.get(1) || (Integer)_genomicPositions.get(1) >= (Integer)genomicPositions.get(0) && (Integer)_genomicPositions.get(1) <= (Integer)genomicPositions.get(1)) {
                                        m.addAnnotation(new Annotation(annotation));
                                        //}
                                    }
                                }
                            }
                        }
                    }
                    else {
                        mediator.getFoldingLandscape().clear();
                        if (AssembleConfig.popupLateralPanels())
                            mediator.getFoldingLandscape().getToolWindow().setVisible(true);

                        StringBuffer fastaData = new StringBuffer();
                        int start = (Integer)((BasicDBList)selected_annotation.get("genomicPositions")).get(0),
                                end = (Integer)((BasicDBList)selected_annotation.get("genomicPositions")).get(1);

                        fastaData.append(">"+selected_annotation.get("class")+" "+selected_annotation.get("organism")+" "+selected_annotation.get("genomicStrand")+" "+start+"-"+end+"\n");

                        BasicDBObject genome = genomicDataHandler.getGenome(((String)selected_annotation.get("genome")).split("@genomes")[0]);
                        String sequence = ((String)genome.get("sequence")).substring(start-1, end);
                        if ("+".equals(selected_annotation.get("genomicStrand")))
                            sequence =  Molecule.reverseComplement(sequence);
                        fastaData.append(sequence);
                        List<SecondaryStructure> secondaryStructures = FileParser.parseFasta(new StringReader(fastaData.toString()), mediator); //at the end of this step, we will have the 2D
                        if (!secondaryStructures.isEmpty()) {
                            List<Annotation> annotations = new ArrayList<Annotation>();
                            for (DBObject annotation : genomicDataHandler.getAnnotationsPerGenome(((String) selected_annotation.get("genome")).split("@")[0]))
                                annotations.add(new Annotation(annotation));
                            for (DBObject annotation : genomicDataHandler.getncRNAsPerGenome(((String) selected_annotation.get("genome")).split("@")[0]))
                                annotations.add(new Annotation(annotation));
                            //we extract and add all the annotations linked to the the molecule loaded
                            secondaryStructures.get(0).getMolecule().setBasicDBObject(selected_annotation);
                            secondaryStructures.get(0).getMolecule().isGenomicAnnotation(true);
                            secondaryStructures.get(0).getMolecule().setFivePrimeEndGenomicPosition((Integer) ((BasicDBList) selected_annotation.get("genomicPositions")).get(0));
                            secondaryStructures.get(0).getMolecule().setPlusOrientation(selected_annotation.get("genomicStrand").equals("+"));
                            secondaryStructures.get(0).getMolecule().addAnnotations(annotations);
                            for (SecondaryStructure ss : secondaryStructures)
                                mediator.loadRNASecondaryStructure(ss, true, false);
                        }
                    }
                    if (selected_annotation.get("tertiaryStructure") != null && mediator.getChimeraDriver() != null) {
                        List<Residue3D> tertiaryStructure = new DataHandler(mediator).getTertiaryStructure(((String)selected_annotation.get("tertiaryStructure")).split("@")[0], mediator.getGenomicMongo().getName());
                        File tmpF = IoUtils.createTemporaryFile("ts.pdb");
                        FileParser.writePDBFile(tertiaryStructure, true, new FileWriter(tmpF));
                        mediator.getChimeraDriver().loadTertiaryStructure(tmpF);
                    }
                    mediator.getAlignmentCanvas().repaint();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public GenomicAnnotationsPanel(final Mediator mediator) {
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setEditable(false);
        this.colorRenderer = new ColorRenderer(true);
        this.mediator = mediator;
        this.mediator.setGenomicAnnotationsPanel(this);
        this.model = new MyModel();
        this.addMouseListener(this);
        this.setModel(this.model);
        this.popupMenu = new JPopupMenu();
        JMenuItem menuItem;

        menuItem = new JMenuItem("Load") {
            public void paintComponent(Graphics graphics) {
                this.setEnabled(GenomicAnnotationsPanel.this.getSelectedRow()!= -1);
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            loadAnnotation(model.genomicAnnotations.get(GenomicAnnotationsPanel.this.convertRowIndexToModel(GenomicAnnotationsPanel.this.getSelectedRow())));
            }
        });
        this.popupMenu.add(menuItem);

        /*menuItem = new JMenuItem("Invalidate/Validate ncRNA") {
            public void paintComponent(Graphics graphics) {
                this.setEnabled(mediator.getGenomicMongo() != null);
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        BasicDBObject selectedNcRNA = model.genomicAnnotations.get(NcRNAsPanel.this.convertRowIndexToModel(NcRNAsPanel.this.getSelectedRow()));
                        Boolean validated = (Boolean)selectedNcRNA.get("validated");
                        if (validated == null || !validated)
                            selectedNcRNA.put("validated", true);
                        else
                            selectedNcRNA.put("validated", false);
                        mediator.getGenomicMongo().getCollection("ncRNAs").save(selectedNcRNA);
                        model.fireTableDataChanged();
                        return null;
                    }
                }.execute();
            }
        });
        this.popupMenu.add(menuItem);*/

        this.popupMenu.add(new ColorMenu(mediator, "Change Color", ColorMenu.GENOMIC_ANNOTATION));
    }

    public String getSelectedClass() {
        return (String)this.model.genomicAnnotations.get(this.convertRowIndexToModel(GenomicAnnotationsPanel.this.getSelectedRow())).get("class");
    }

    public void duplicateEntry() {
        Date now = new Date();
        String newAlignmentId = new ObjectId().toString();
        BasicDBObject alignment = null;
        for (AlignedMolecule alignedMolecule: this.mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules()) {
            INNER: for (BasicDBObject _ncRNA: model.genomicAnnotations) {
                if (_ncRNA.get("_id").equals(alignedMolecule.getMolecule().getId())) {
                    String newNcRNAId = new ObjectId().toString();
                    BasicDBObject new_ncRNA = (BasicDBObject)_ncRNA.clone();
                    if (alignment == null) {
                        BasicDBObject query = new BasicDBObject();
                        query.put("_id", ((String)_ncRNA.get("alignment")).split("@")[0]);
                        alignment = (BasicDBObject)mediator.getGenomicMongo().getCollection("alignments").findOne(query);
                    }
                    new_ncRNA.put("_id", newNcRNAId);
                    new_ncRNA.put("alignment", newAlignmentId+"@alignments");
                    new_ncRNA.put("last_update", now.getTime());
                    mediator.getGenomicMongo().getCollection("ncRNAs").insert(new_ncRNA);
                    addRow(new_ncRNA);
                    alignedMolecule.getMolecule().setId(newNcRNAId);
                    //we create the Annotation object
                    alignedMolecule.getMolecule().addAnnotation(new Annotation(new_ncRNA));
                    break INNER;
                }
            }
        }

        BasicDBObject new_alignment = (BasicDBObject)alignment.clone();
        new_alignment.put("_id", newAlignmentId);
        new_alignment.put("last_update", now.getTime());
        mediator.getGenomicMongo().getCollection("alignments").insert(new_alignment);
        model.fireTableDataChanged();
        mediator.getAlignmentCanvas().repaint();

        JOptionPane.showMessageDialog(null, "Entry duplicated!!");
    }

    public void saveEntry() throws Exception {

        java.util.List<Symbol> selection = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSelection();
        DBObject ncRNA = mediator.getGenomicMongo().getCollection("ncRNAs").findOne(new BasicDBObject("_id", mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getMolecule().getId())),
                alignment = mediator.getGenomicMongo().getCollection("alignments").findOne(new BasicDBObject("_id", ((String)ncRNA.get("alignment")).split("@")[0]));
        BasicDBList genomicPositions = (BasicDBList)ncRNA.get("genomicPositions");

        if (!selection.isEmpty() && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null, "Are you sure to crop your ncRNA to the current selection?"))
            return;

        int firstIndex = -1,lastIndex = -1;
        if (!selection.isEmpty()) {
            firstIndex = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getIndex(selection.get(0));
            lastIndex =  mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getIndex(selection.get(selection.size()-1));
        } else {
            firstIndex = 0;
            lastIndex = mediator.getAlignmentCanvas().getMainAlignment().getLength()-1;
        }
        StringBuffer clustalwOutput = new StringBuffer(),
                consensus = new StringBuffer(),
                sequence = null;

        for (int i=firstIndex ; i <= lastIndex ; i++)
            consensus.append(mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().getSymbol(i).getSymbol());

        //we test if the 2D is unbalanced before to save
        if (mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().isUnbalanced())
            JOptionPane.showMessageDialog(null, "Your consensus 2D is unbalanced. Check the red characters!!");
        else {
            //if we reach this point, the 2D is not unbalanced. We can save the entry!!

            Date now = new Date();

            List<String> ncRNAs_to_remove = new ArrayList<String>();

            for (AlignedMolecule alignedMolecule: mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules()) {

                ncRNA = mediator.getGenomicMongo().getCollection("ncRNAs").findOne(new BasicDBObject("_id", alignedMolecule.getMolecule().getId()));

                if (ncRNA != null) { //some molecules can not be linked to any ncRNA in MongoDB

                    sequence = new StringBuffer();

                    for (int i=firstIndex ; i <= lastIndex ; i++)
                        sequence.append(alignedMolecule.getSymbol(i).getSymbol());

                    if (sequence.toString().replace("-","").trim().length() == 0) {//for the current interval, this ncRNA is only made with gaps => this is not a ncRNA hit anymore
                        ncRNAs_to_remove.add((String)ncRNA.get("_id"));
                        continue;
                    }

                    genomicPositions = new BasicDBList();

                    if (alignedMolecule.getSymbol(firstIndex).getPositionInSequence() < alignedMolecule.getSymbol(lastIndex).getPositionInSequence()) {
                        genomicPositions.add(alignedMolecule.getSymbol(firstIndex).getPositionInSequence());
                        genomicPositions.add(alignedMolecule.getSymbol(lastIndex).getPositionInSequence());
                    }
                    else {
                        genomicPositions.add(alignedMolecule.getSymbol(lastIndex).getPositionInSequence());
                        genomicPositions.add(alignedMolecule.getSymbol(firstIndex).getPositionInSequence());
                    }

                    ncRNA.put("genomicPositions", genomicPositions);
                    ncRNA.put("last_update", now.getTime());

                    //we're searching for the BasicDBObject stored as model in the ncRNAsPanel and we update it also
                    boolean found = false;
                    for (BasicDBObject _ncRNA: model.genomicAnnotations) {
                        if (_ncRNA.get("_id").equals(ncRNA.get("_id"))) {
                            _ncRNA.put("genomicPositions", genomicPositions);
                            _ncRNA.put("last_update", now.getTime());
                            found = true;
                            break;
                        }
                    }

                    if (!found) //it is possible that a ncRNA has been removed from a previous save and needs now to be restored
                        addRow((BasicDBObject)ncRNA);

                    //we update the Annotation object
                    for (Annotation a: alignedMolecule.getMolecule().getAnnotations()) {
                        if (a.get_id().equals(ncRNA.get("_id"))) {
                            a.setStart((Integer)genomicPositions.get(0));
                            a.setEnd((Integer)genomicPositions.get(1));
                            break;
                        }
                    }

                    mediator.getGenomicMongo().getCollection("ncRNAs").save(ncRNA);
                }

            }

            //we remove from the ncRNAsPanel the ncRNAs to be removed
            for (BasicDBObject _ncRNA: model.genomicAnnotations) {
                if (ncRNAs_to_remove.contains(_ncRNA.get("_id"))) {
                    model.genomicAnnotations.remove(_ncRNA);
                    break;
                }
            }

            int c = 0;
            while (c < lastIndex-firstIndex+1) {
                int d = Math.min(lastIndex-firstIndex+1, c + 60);
                for (AlignedMolecule alignedMolecule: mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules())
                    if (mediator.getGenomicMongo().getCollection("ncRNAs").findOne(new BasicDBObject("_id", alignedMolecule.getMolecule().getId())) == null)
                        clustalwOutput.append(alignedMolecule.getMolecule().getName()+"\t"+alignedMolecule.getSequence(firstIndex, lastIndex).substring(c,d)+'\n');
                    else
                        clustalwOutput.append(alignedMolecule.getMolecule().getId()+"\t"+alignedMolecule.getSequence(firstIndex, lastIndex).substring(c,d)+'\n');
                clustalwOutput.append('\n');
                c += 60;
            }

            clustalwOutput.append("2D\t"+mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().getSequence(firstIndex, lastIndex));

            alignment.put("alignment", clustalwOutput.toString());
            alignment.put("last_update", now.getTime());

            mediator.getGenomicMongo().getCollection("alignments").save(alignment);

            model.fireTableDataChanged();

            mediator.getAlignmentCanvas().repaint();

            JOptionPane.showMessageDialog(null, "Entry saved!!");
        }

    }

    public boolean isSelected(Molecule m) {
        int[] indices = this.getSelectedRows();
        for (int i = 0 ; i < indices.length ; i++ )
            if (mediator.getAlignmentCanvas().getMainAlignment().getBiologicalSequenceAt(indices[i]).getMolecule().equals(m))
                return true;
        return false;
    }

    public void addRow(BasicDBObject object) {
        /*Boolean validated = (Boolean)object.get("validated");
        if (validated == null) {//if the entry in the DB has no validated feature
            object.put("validated", false);
            mediator.getGenomicMongo().getCollection("ncRNAs").save(object);
        }*/
        this.model.genomicAnnotations.add(object);
        this.model.fireTableDataChanged();
    }

    public void clearList() {
        this.model.genomicAnnotations.clear();
        this.model.fireTableDataChanged();
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        if (row == overIndex && column == 1) {
            return new ReorderTableRenderer();
        } else if (column == 9)
            return this.colorRenderer;
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

}
