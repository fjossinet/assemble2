package fr.unistra.ibmc.assemble2.gui;


import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.model.AlignedMolecule;
import fr.unistra.ibmc.assemble2.model.ReferenceStructureSymbol;
import fr.unistra.ibmc.assemble2.model.Symbol;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalGenomicDataHandler implements GenomicDataHandler {

    private File rootDir;
    private Mediator mediator;

    public LocalGenomicDataHandler(Mediator mediator, File rootDir) {
        this.mediator = mediator;
        this.rootDir = rootDir;
    }

    @Override
    public BasicDBObject getGenome(String _id) {
        for (File f: new File(rootDir, "genomes").listFiles()) {
            if (f.getName().startsWith(_id)) {
                BasicDBObject o = null;
                try {
                    return (BasicDBObject) JSON.parse(FileUtils.readFileToString(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public BasicDBObject getAlignment(String _id) {
        for (File f: new File(rootDir, "alignments").listFiles()) {
            if (f.getName().startsWith(_id)) {
                BasicDBObject o = null;
                try {
                    return (BasicDBObject) JSON.parse(FileUtils.readFileToString(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public BasicDBObject getncRNA(String _id) {
        for (File f: new File(rootDir, "ncRNAs").listFiles()) {
            if (f.getName().startsWith(_id)) {
                BasicDBObject o = null;
                try {
                    o = (BasicDBObject) JSON.parse(FileUtils.readFileToString(f));
                    return o;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public List<BasicDBObject> getAnnotationsPerGenome(String _id) {
        List<BasicDBObject> annotations = new ArrayList<BasicDBObject>();
        for (File f: new File(rootDir, "annotations").listFiles()) {
            if (f.getName().startsWith(_id)) {
                try {
                    BasicDBList l = (BasicDBList) JSON.parse(FileUtils.readFileToString(f));
                    for (Object o: l)
                        annotations.add((BasicDBObject)o);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return annotations;
    }

    @Override
    public List<BasicDBObject> getncRNAsPerGenome(String _id) {
        List<BasicDBObject> annotations = new ArrayList<BasicDBObject>();
        for (File f: new File(rootDir, "ncRNAs").listFiles()) {
            if (f.getName().endsWith(".json")) {
                BasicDBObject o = null;
                try {
                    o = (BasicDBObject) JSON.parse(FileUtils.readFileToString(f));
                    if (o.get("genome").equals(_id+"@genomes"))
                        annotations.add(o);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return annotations;
    }

    @Override
    public void iWantMoreResiduesOnTheLeft() {

        for (int i = 1 ; i <= Assemble.MORE ; i++) {
            mediator.getAlignmentCanvas().getMainAlignment().getSequenceMeter().insertGap(0);
            mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().insertGap(0);
            mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure().addSymbolAt(0,new ReferenceStructureSymbol(mediator, mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure(), '.'));
        }

        for (final AlignedMolecule alignedMolecule:mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules()) {

            java.util.List<Symbol> symbols = new ArrayList<Symbol>();

            final DBObject currentAnnotation = alignedMolecule.getMolecule().getBasicDBObject();

            if (currentAnnotation != null) {
                DBObject genome = this.getGenome(((String)currentAnnotation.get("genome")).split("@")[0]);

                if (genome != null) {

                    char[] sequence = null;
                    Symbol s = null;
                    if (currentAnnotation.get("genomicStrand").equals("+")) {
                        int genomic_start = alignedMolecule.getSymbol(0).getPositionInSequence();
                        sequence = ((String)genome.get("sequence")).substring(genomic_start-1- Assemble.MORE, genomic_start-1).toCharArray();

                        int i=0;
                        for (char res: sequence) {
                            s = new Symbol(mediator, res, alignedMolecule);
                            s.setPositionInSequence(genomic_start-Assemble.MORE+i);
                            symbols.add(s);
                            i++;
                        }
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

    @Override
    public void iWantMoreResiduesOnTheRight() {

        for (int i = 1 ; i <= Assemble.MORE ; i++) {
            mediator.getAlignmentCanvas().getMainAlignment().getSequenceMeter().insertGap(mediator.getAlignmentCanvas().getMainAlignment().getSequenceMeter().size());
            mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().insertGap(mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().size());
            mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure().addSymbolAt(mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure().size(), new ReferenceStructureSymbol(mediator, mediator.getAlignmentCanvas().getMainAlignment().getReferenceStructure(), '.'));
        }

        for (final AlignedMolecule alignedMolecule: mediator.getAlignmentCanvas().getMainAlignment().getAlignedMolecules()) {

            java.util.List<Symbol> symbols = new ArrayList<Symbol>();

            final DBObject currentAnnotation = alignedMolecule.getMolecule().getBasicDBObject();
            if (currentAnnotation != null) {
                DBObject genome = this.getGenome(((String)currentAnnotation.get("genome")).split("@")[0]);

                if (genome != null) {
                    char[] sequence = null;
                    Symbol s = null;
                    if (currentAnnotation.get("genomicStrand").equals("+")) {
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
}
