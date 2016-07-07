package fr.unistra.ibmc.assemble2.gui;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.Pair;
import org.bson.types.ObjectId;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;
import java.util.*;
import java.util.List;

public class MongoDBAlignments extends JXTable implements MouseListener {

    private Mediator mediator;
    private MyModel model;
    private JPopupMenu popupMenu;
    private int startIndex = -1,  overIndex = -1;

    public MongoDBAlignments(final Mediator mediator) {
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setEditable(false);
        this.mediator = mediator;
        this.mediator.setMongoDBAlignments(this);
        this.model = new MyModel();
        this.addMouseListener(this);
        this.setModel(this.model);
        this.popupMenu = new JPopupMenu();
        JMenuItem menuItem;

        menuItem = new JMenuItem("Load") {
            public void paintComponent(Graphics graphics) {
                this.setEnabled(MongoDBAlignments.this.getSelectedRow()!= -1);
                super.paintComponent(graphics);
            }
        };
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadAlignment(model.alignments.get(MongoDBAlignments.this.convertRowIndexToModel(MongoDBAlignments.this.getSelectedRow())));
            }
        });
        this.popupMenu.add(menuItem);

    }

    private class MyModel extends AbstractTableModel {

        private java.util.List<BasicDBObject> alignments = new ArrayList<BasicDBObject>();

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < alignments.size()) {
                BasicDBObject alignment = alignments.get(rowIndex);
                if (columnIndex == 0)
                    return (String)alignment.get("name");
                else if (columnIndex == 1)
                    return (String)alignment.get("class");
                else if (columnIndex == 2)
                    return (String)alignment.get("source");
                else {
                    return ((String)alignment.get("alignment")).indexOf("[New]") != -1;
                }

            } else
                return null;

        }

        public int getColumnCount() {
            return 4;
        }

        public int getRowCount() {
            return alignments.size();
        }

        @Override
        public String getColumnName(int i) {
            if (i == 0)
                return "Name";
            else if (i == 1)
                return "Class";
            else if (i == 2)
                return "Source";
            else
                return "Has new hits";
        }
    }

    public void loadAlignment(final DBObject alignment) {
        new SwingWorker() {
            @Override
            protected Object doInBackground() {
                try {
                    Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> result = FileParser.parseClustal(new StringReader((String) alignment.get("alignment")), mediator, null);
                    List<Molecule> molecules = new ArrayList<Molecule>();

                    for (AlignedMolecule am:result.getSecond()) {
                        String name = am.getMolecule().getName(),
                                species = null;
                        String[] tokens = name.split("\\[New\\]");
                        if (tokens.length == 2)
                            name = tokens[1];
                        if (name.indexOf("@") != -1) {
                            tokens =  name.split("@");
                            name = tokens[0];
                            species = tokens[1];
                        }
                        if(ObjectId.isValid(name)) {//if the name of the molecule in the clustalw content is a valid ObjectId string, it becomes the id of the Molecule object
                            am.getMolecule().setId(name);
                            am.getMolecule().isGenomicAnnotation(true);
                            BasicDBObject ncRNA = getncRNA(name, species);
                            am.getMolecule().setBasicDBObject(ncRNA);
                            String genomeId = ((String)ncRNA.get("genome")).split("@")[0];
                            int start =  (Integer)((BasicDBList)ncRNA.get("genomicPositions")).get(0),
                                    end = (Integer)((BasicDBList)ncRNA.get("genomicPositions")).get(1);
                            am.getMolecule().setFivePrimeEndGenomicPosition(start);
                            am.getMolecule().setPlusOrientation(ncRNA.get("genomicStrand").equals("+"));
                            am.renumber();

                            for (DBObject annotation : getAnnotationsPerGenome(genomeId, species, start, end))
                                am.getMolecule().addAnnotation(new Annotation(annotation));

                            for (DBObject annotation : getncRNAsPerGenome(genomeId, species, start, end))
                                am.getMolecule().addAnnotation(new Annotation(annotation));
                        }
                        molecules.add(am.getMolecule());
                    }

                    Molecule m = (Molecule) JOptionPane.showInputDialog(null, "Choose the molecule to display", "Choose the molecule to display", JOptionPane.PLAIN_MESSAGE, null, molecules.toArray(), molecules.get(0));

                    if (m != null) {
                        int index = molecules.indexOf(m);
                        //since no referenceId precised
                        SecondaryStructure reference2D = result.getFirst().getSecond().get(index);
                        AlignedMolecule referenceMolecule = result.getSecond().get(index);
                        ReferenceStructure referenceStructure = new ReferenceStructure(mediator, referenceMolecule, reference2D);
                        result.getSecond().remove(referenceMolecule);
                        StructuralAlignment alignment = new StructuralAlignment(mediator,  result.getFirst().getFirst(), referenceMolecule, referenceStructure,  result.getSecond());
                        mediator.getAlignmentCanvas().setMainAlignment(alignment);
                        mediator.loadRNASecondaryStructure(referenceStructure.getSecondaryStructure(), false, true);
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public BasicDBObject getGenome(String _id, String species) {
        BasicDBObject query = new BasicDBObject("_id", _id);
        return (BasicDBObject)mediator.getMongo().getDB(species).getCollection("genomes").findOne(query);
    }

    public BasicDBObject getncRNA(String _id, String species) {
        BasicDBObject query = new BasicDBObject("_id", _id);
        return (BasicDBObject)mediator.getMongo().getDB(species).getCollection("ncRNAs").findOne(query);
    }

    public List<BasicDBObject> getAnnotationsPerGenome(String _id, String species, int start, int end) {
        List<BasicDBObject> annotations = new ArrayList<BasicDBObject>();
        BasicDBObject query = new BasicDBObject("genome", _id+"@genomes");
        query.put("$where", "function(){return this.genomicPositions[0] >= " + start + " && this.genomicPositions[0] <= " + end + " || this.genomicPositions[1] >= " + start + " && this.genomicPositions[1] <= " + end+"}");
        for (DBObject annotation : mediator.getMongo().getDB(species).getCollection("annotations").find(query))
            annotations.add((BasicDBObject)annotation);
        return annotations;
    }

    public List<BasicDBObject> getncRNAsPerGenome(String _id, String species, int start, int end) {
        List<BasicDBObject> annotations = new ArrayList<BasicDBObject>();
        BasicDBObject query = new BasicDBObject("genome", _id+"@genomes");
        query.put("$where", "function(){return this.genomicPositions[0] >= " + start + " && this.genomicPositions[0] <= " + end + " || this.genomicPositions[1] >= " + start + " && this.genomicPositions[1] <= " + end+"}");
        for (DBObject annotation : mediator.getMongo().getDB(species).getCollection("ncRNAs").find(query))
            annotations.add((BasicDBObject)annotation);
        return annotations;
    }

    public void iWantMoreResiduesOnTheLeft() {

        for (int i = 1 ; i <= Assemble.MORE ; i++) {
            mediator.getAlignmentCanvas().getMainAlignment().getSequenceMeter().insertGap(0);
            mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().insertGap(0);
            mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure().addSymbolAt(0,new ReferenceStructureSymbol(mediator, mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure(), '.'));
        }

        for (final AlignedMolecule alignedMolecule:mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules()) {

            java.util.List<Symbol> symbols = new ArrayList<Symbol>();

            final DBObject ncRNA = alignedMolecule.getMolecule().getBasicDBObject();
            if (ncRNA != null) {
                String name = alignedMolecule.getMolecule().getName(),
                        species = null;
                String[] tokens = name.split("\\[New\\]");
                if (tokens.length == 2)
                    name = tokens[1];
                if (name.indexOf("@") != -1) {
                    tokens =  name.split("@");
                    name = tokens[0];
                    species = tokens[1];
                }
                String genomeId = ((String)ncRNA.get("genome")).split("@")[0];
                DBObject genome = this.getGenome(genomeId, species);

                if (genome != null) {
                    char[] sequence = null;
                    Symbol s = null;
                    if (ncRNA.get("genomicStrand").equals("+")) {
                        int genomic_start = alignedMolecule.getSymbol(0).getPositionInSequence();
                        sequence = ((String)genome.get("sequence")).substring(genomic_start-1- Assemble.MORE, genomic_start-1).toCharArray();

                        int i=0;
                        for (char res: sequence) {
                            s = new Symbol(mediator, res, alignedMolecule);
                            s.setPositionInSequence(genomic_start-Assemble.MORE+i);
                            symbols.add(s);
                            i++;
                        }
                        for (DBObject annotation : getAnnotationsPerGenome(genomeId, species, genomic_start- Assemble.MORE, genomic_start-1))
                            alignedMolecule.getMolecule().addAnnotation(new Annotation(annotation));

                        for (DBObject annotation : getncRNAsPerGenome(genomeId, species, genomic_start- Assemble.MORE, genomic_start-1))
                            alignedMolecule.getMolecule().addAnnotation(new Annotation(annotation));
                    }
                    else {
                        int genomic_start = alignedMolecule.getSymbol(alignedMolecule.size()-1).getPositionInSequence(),
                                genomic_end = alignedMolecule.getSymbol(0).getPositionInSequence();
                        sequence = new StringBuffer(((String)genome.get("sequence")).substring(genomic_end, genomic_end+Assemble.MORE)).reverse().toString().toCharArray();

                        int i=0;
                        for (char res: sequence) {
                            switch (res) {
                                case 'A': s = new Symbol(mediator, 'T', alignedMolecule); break;
                                case 'T':
                                case 'U': s = new Symbol(mediator, 'A', alignedMolecule); break;
                                case 'G': s = new Symbol(mediator, 'C', alignedMolecule); break;
                                case 'C': s = new Symbol(mediator, 'G', alignedMolecule); break;
                                default : s = new Symbol(mediator, res, alignedMolecule);
                            }
                            s.setPositionInSequence(genomic_end+Assemble.MORE-i);
                            symbols.add(s);
                            i++;
                        }
                        for (DBObject annotation : getAnnotationsPerGenome(genomeId, species, genomic_end+1, genomic_end+Assemble.MORE))
                            alignedMolecule.getMolecule().addAnnotation(new Annotation(annotation));

                        for (DBObject annotation : getncRNAsPerGenome(genomeId, species, genomic_end+1, genomic_end+Assemble.MORE))
                            alignedMolecule.getMolecule().addAnnotation(new Annotation(annotation));
                    }

                    alignedMolecule.addSymbolsAt(0,symbols);
                } else { //we don't have the genome stored
                    for (int i = 1 ; i <= Assemble.MORE ; i++)
                        alignedMolecule.insertGap(0);
                }


            } else { //we don't have the ncRNA stored
                for (int i = 1 ; i <= Assemble.MORE ; i++)
                    alignedMolecule.insertGap(0);
            }
        }

    }

    public void iWantMoreResiduesOnTheRight() {

        for (int i = 1 ; i <= Assemble.MORE ; i++) {
            mediator.getAlignmentCanvas().getMainAlignment().getSequenceMeter().insertGap(mediator.getAlignmentCanvas().getMainAlignment().getSequenceMeter().size());
            mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().insertGap(mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().size());
            mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure().addSymbolAt(mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure().size(), new ReferenceStructureSymbol(mediator, mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure(), '.'));
        }

        for (final AlignedMolecule alignedMolecule: mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules()) {

            java.util.List<Symbol> symbols = new ArrayList<Symbol>();

            final DBObject ncRNA = alignedMolecule.getMolecule().getBasicDBObject();
            if (ncRNA != null) {
                String name = alignedMolecule.getMolecule().getName(),
                        species = null;
                String[] tokens = name.split("\\[New\\]");
                if (tokens.length == 2)
                    name = tokens[1];
                if (name.indexOf("@") != -1) {
                    tokens =  name.split("@");
                    name = tokens[0];
                    species = tokens[1];
                }
                DBObject genome = this.getGenome(((String)ncRNA.get("genome")).split("@")[0], species);
                if (genome != null) {
                    char[] sequence = null;
                    Symbol s = null;
                    if (ncRNA.get("genomicStrand").equals("+")) {
                        int genomic_end = alignedMolecule.getSymbol(alignedMolecule.size()-1).getPositionInSequence();
                        sequence = ((String)genome.get("sequence")).substring(genomic_end, genomic_end+Assemble.MORE).toCharArray();

                        int i=0;
                        for (char res: sequence) {
                            s = new Symbol(mediator, res, alignedMolecule);
                            s.setPositionInSequence(genomic_end+1+i);
                            symbols.add(s);
                            i++;
                        }
                    }
                    else {
                        int genomic_start = alignedMolecule.getSymbol(alignedMolecule.size()-1).getPositionInSequence(),
                                genomic_end = alignedMolecule.getSymbol(0).getPositionInSequence();

                        sequence = new StringBuffer(((String)genome.get("sequence")).substring(genomic_start-1-Assemble.MORE, genomic_start-1)).reverse().toString().toCharArray();

                        int i=0;
                        for (char res: sequence) {
                            switch (res) {
                                case 'A': s = new Symbol(mediator, 'T', alignedMolecule); break;
                                case 'T':
                                case 'U': s = new Symbol(mediator, 'A', alignedMolecule); break;
                                case 'G': s = new Symbol(mediator, 'C', alignedMolecule); break;
                                case 'C': s = new Symbol(mediator, 'G', alignedMolecule); break;
                                default : s = new Symbol(mediator, res, alignedMolecule);
                            }
                            s.setPositionInSequence(genomic_start-1-i);
                            symbols.add(s);
                            i++;
                        }
                    }

                    alignedMolecule.addSymbolsAt(alignedMolecule.size(),symbols);
                } else { //we don't have the genome stored
                    for (int i = 1 ; i <= Assemble.MORE ; i++)
                        alignedMolecule.insertGap(alignedMolecule.size());
                }


            } else { //we don't have the ncRNA stored
                for (int i = 1 ; i <= Assemble.MORE ; i++)
                    alignedMolecule.insertGap(alignedMolecule.size());
            }
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
        this.model.alignments.add(object);
        this.model.fireTableDataChanged();
    }

    public void clearList() {
        this.model.alignments.clear();
        this.model.fireTableDataChanged();
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        if (row == overIndex && column == 1) {
            return new ReorderTableRenderer();
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
