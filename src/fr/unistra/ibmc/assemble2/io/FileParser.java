package fr.unistra.ibmc.assemble2.io;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import fr.unistra.ibmc.assemble2.gui.components.ColorMenu;
import fr.unistra.ibmc.assemble2.gui.components.MessagingSystemAction;
import fr.unistra.ibmc.assemble2.gui.components.ReportDialog;
import fr.unistra.ibmc.assemble2.io.computations.*;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.Modeling2DUtils;
import fr.unistra.ibmc.assemble2.utils.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.bson.types.ObjectId;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class FileParser {

    public static int LEFT_ALIGN = 0, RIGHT_ALIGN = 1;

    public static void writeBPSEQFile(Mediator mediator, SecondaryStructure ss, Writer writer) throws Exception {
        PrintWriter pw = new java.io.PrintWriter(writer);
        for (int i=1 ; i <= ss.getMolecule().size() ; i++) {
            Residue pairedResidue = ss.getPairedResidue(new Residue(mediator, ss.getMolecule(),i));
            pw.write(i+" "+ss.getMolecule().getResidueAt(i)+" "+(pairedResidue == null ? 0 : pairedResidue.getAbsolutePosition())+"\n");
        }
        pw.close();
    }

    public static void writeClustalFile(StructuralAlignment alignment, Writer writer, int firstIndex, int lastIndex) throws Exception {
        PrintWriter pw = new java.io.PrintWriter(writer);
        pw.write("CLUSTALW\n");
        int c = 0;
        while (c < lastIndex-firstIndex+1) {
            int d = Math.min(lastIndex-firstIndex+1, c + 60);
            for (AlignedMolecule alignedMolecule:alignment.getAlignedMolecules())
                pw.write(alignedMolecule.getMolecule().getOrganism() != null? alignedMolecule.getMolecule().getOrganism() : alignedMolecule.getMolecule().getName()+"\t"+alignedMolecule.getSequence(firstIndex, lastIndex).substring(c,d)+'\n');
            pw.write('\n');
            c += 60;
        }
        pw.close();
    }

    public static void writeFastaFile(String name, String sequence, Writer writer) throws Exception {
        PrintWriter pw = new java.io.PrintWriter(writer);
        pw.write(">"+name+"\n");
        pw.write(sequence+"\n");
        pw.close();
    }

    public static void writeFastaFile(StructuralAlignment alignment, Writer writer) throws Exception {
        PrintWriter pw = new java.io.PrintWriter(writer);
        String str = alignment.getConsensusStructure().getSequence();
        pw.write(">Consensus 2D\n");
        int c = 0;
        while (c < str.length()) {
            int d = Math.min(str.length(), c + 79);
            pw.write(str.substring(c,d) + "\n");
            c += 79;
        }
        str = alignment.getReferenceStructure().getSequence();
        pw.write(">Reference 2D\n");
        c = 0;
        while (c < str.length()) {
            int d = Math.min(str.length(), c + 79);
            pw.write(str.substring(c,d) + "\n");
            c += 79;
        }
        for (AlignedMolecule a:alignment.getAlignedMolecules()) {
            pw.write(">"+a.getMolecule().getName()+"\n");
            c = 0;
            String seq = a.getSequence();
            while (c < seq.length()) {
                int d = Math.min(seq.length(), c + 79);
                pw.write(seq.substring(c,d) + "\n");
                c += 79;
            }
        }
        pw.close();
    }

    public static void writeStokholmFile(StructuralAlignment alignment, Writer writer, int firstIndex, int lastIndex) throws Exception {
        PrintWriter pw = new java.io.PrintWriter(writer);
        pw.write("# STOCKHOLM 1.0\n");
        int c = 0;
        while (c < lastIndex-firstIndex+1) {
            int d = Math.min(lastIndex-firstIndex+1, c + 80);
            for (AlignedMolecule alignedMolecule:alignment.getAlignedMolecules())
                pw.write((alignedMolecule.getMolecule().getOrganism() != null? alignedMolecule.getMolecule().getOrganism() : alignedMolecule.getMolecule().getName())+"\t"+alignedMolecule.getSequence(firstIndex, lastIndex).substring(c,d)+'\n');
            pw.write("#=GC SS_cons\t"+alignment.getConsensusStructure().getSequence(firstIndex, lastIndex).substring(c,d)+'\n');
            pw.write('\n');
            c += 80;
        }
        pw.write("//");
        pw.close();
    }

    public static void writePDBFile(List<Residue3D> residues, boolean exportNumberingSystem, Writer writer) throws Exception {
        PrintWriter pw = new java.io.PrintWriter(writer);
        int atomID = 0;
        NumberFormat coordFormat = NumberFormat.getInstance(Locale.ENGLISH);
        coordFormat.setMinimumFractionDigits(3);
        coordFormat.setMaximumFractionDigits(3);
        for (Residue3D residue : residues) {
            for (Residue3D.Atom a : residue.getAtoms()) {
                if (a.hasCoordinatesFilled()) {
                    pw.print(formatPDBField(6, "ATOM", LEFT_ALIGN));
                    pw.print(formatPDBField(11 - 7 + 1, "" + (++atomID), RIGHT_ALIGN));
                    pw.print("  ");
                    pw.print(formatPDBField(16 - 13 + 1, a.getName().replace('\'', '*'), LEFT_ALIGN));
                    pw.print(formatPDBField(20 - 18 + 1, residue.getName(), RIGHT_ALIGN));
                    pw.print(formatPDBField(1, " " + residue.getMolecule().getName().charAt(0), LEFT_ALIGN));
                    if (exportNumberingSystem)
                        pw.print(formatPDBField(26 - 23 + 1, residue.getLabel(), RIGHT_ALIGN));
                    else
                        pw.print(formatPDBField(26 - 23 + 1, "" + residue.getAbsolutePosition(), RIGHT_ALIGN));
                    pw.print(formatPDBField(1, "", LEFT_ALIGN));
                    pw.print("   ");
                    pw.print(formatPDBField(38 - 31 + 1, "" + coordFormat.format(a.getX()), RIGHT_ALIGN));
                    pw.print(formatPDBField(46 - 39 + 1, "" + coordFormat.format(a.getY()), RIGHT_ALIGN));
                    pw.print(formatPDBField(54 - 47 + 1, "" + coordFormat.format(a.getZ()), RIGHT_ALIGN));
                    pw.print(formatPDBField(60 - 55 + 1, "1.00", RIGHT_ALIGN));
                    pw.print(formatPDBField(66 - 61 + 1, "100.00", RIGHT_ALIGN));
                    pw.print(formatPDBField(10, "", LEFT_ALIGN));
                    pw.print(formatPDBField(78 - 77 + 1, ""+a.getName().charAt(0), RIGHT_ALIGN));
                    pw.println(formatPDBField(2, "", LEFT_ALIGN));
                }
            }
        }
        pw.println("END   ");
        pw.close();
    }

    private static String formatPDBField(int finalSize, String word, int align) {
        StringBuffer field = new StringBuffer();
        if (align == LEFT_ALIGN) {
            field.append(word);
            for (int i = 0; i < finalSize - word.length(); i++) {
                field.append(" ");
            }
        } else {
            for (int i = 0; i < finalSize - word.length(); i++) {
                field.append(" ");
            }
            field.append(word);
        }
        return field.toString();
    }

    static List<String> residuesIgnored = new ArrayList<String>();
    static {
        residuesIgnored.add("MG");
        residuesIgnored.add("K");
        residuesIgnored.add("NA");
        residuesIgnored.add("CL");
        residuesIgnored.add("SR");
        residuesIgnored.add("CD");
        residuesIgnored.add("ACA");

    }

    static List<String> chainsIgnored = new ArrayList<String>();
    static {
        chainsIgnored.add("FMN");
        chainsIgnored.add("PRF");
        chainsIgnored.add("HOH");
        chainsIgnored.add("MG");
        chainsIgnored.add("OHX");
        chainsIgnored.add("MN");
        chainsIgnored.add("ZN");
        chainsIgnored.add("SO4");
        chainsIgnored.add("CA");
        chainsIgnored.add("UNK"); //this is a residue named UNK
        chainsIgnored.add("N"); //this is a residue named N
    }

    public static void parseGenbank(File f, Mediator mediator) throws Exception  {

    }

    public static void parsePDB(Mediator mediator, File f)  {
        try {
            mediator.getAssemble().loadTertiaryStructures(parsePDB(mediator, new FileReader(f)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<TertiaryStructure> parsePDB(Mediator mediator, Reader reader) {
        List<TertiaryStructure> tertiaryStructures = new ArrayList<TertiaryStructure>();
        StringBuffer fullContent = new StringBuffer();
        try {
            TertiaryStructure ts = new TertiaryStructure("Tertiary Structure");
            String tag = "fake";
            int nucleic_chain_id = 1, protein_chain_id = 1;
            String molecule_label = "fake";
            Molecule m = null;
            int nt_id = 0, aa_id = 0;
            String resId = "fake";
            Residue3D r = null;
            //necessary to store the atoms parameters until the parser knowns if it parses a nucleotide or an aminoacid
            Map<String, float[]> atoms = new HashMap<String, float[]>();
            boolean isInsideNucleicAcid = false, isInsideProtein = false, ter_tag = false;
            String line = null;
            BufferedReader input = new BufferedReader(reader);
            LINES:
            while ((line = input.readLine()) != null) {
                fullContent.append(line);
                try {
                    tag = line.substring(0, 6).trim();
                } catch (IndexOutOfBoundsException e) {
                    tag = "fake";
                }
                if ((tag.equalsIgnoreCase("ATOM") || tag.equalsIgnoreCase("HETATM")) && !chainsIgnored.contains(line.substring(17, 21).trim()) && !residuesIgnored.contains(line.substring(12, 16).trim()) && line.substring(21, 22).trim().length() != 0 /*only if the ATOM or HETATM line precises a molecule name*/) {
                    //with the following statement we're testing if we have a new molecule
                    //we have a new molecule if the molecule name has changed (even if no TER tag has been meet the line before
                    //we have a new molecule if the TER tag has been meet AND if the molecule name has changed (some PDB or PDB exported by PyMOL can have a TER tag in a middle of a molecular chain
                    if (!molecule_label.equalsIgnoreCase(line.substring(21, 22).trim()) || (ter_tag && !molecule_label.equalsIgnoreCase(line.substring(21, 22).trim()))) {
                        //name.length==0 for H2O, Magnesium ions, .... For residues not inside a macromolecule
                        if ((isInsideNucleicAcid || isInsideProtein) && molecule_label.length() > 0) {
                            if (isInsideNucleicAcid) {
                                nucleic_chain_id++;
                                isInsideNucleicAcid = false;
                            } else {
                                protein_chain_id++;
                                isInsideProtein = false;
                            }
                        }
                        molecule_label = line.substring(21, 22).trim();
                        m = null;
                        ts = null;
                        nt_id = 0;
                        aa_id = 0;
                        resId = line.substring(22, 27).trim();
                        r = null;
                        ter_tag = false;
                    }
                    //only new residue
                    else if (!resId.equalsIgnoreCase(line.substring(22, 27).trim()) && isInsideNucleicAcid)
                        r = null;
                    //residue is a nucleotide if the 04' atom is detected
                    if ((line.substring(12, 16).trim().equals("O4*") || line.substring(12, 16).trim().equals("O4'"))) {
                        nt_id++;
                        resId = line.substring(22, 27).trim();
                        if (!isInsideNucleicAcid) {
                            isInsideNucleicAcid = true;
                            isInsideProtein = false;
                        }
                        if (m == null && ts == null) {
                            m = new Molecule(molecule_label);
                            ts = new TertiaryStructure(m);
                            tertiaryStructures.add(ts);
                        }
                        m.addResidue(line.substring(17, 21).trim().toUpperCase());
                        r = ts.addResidue3D(nt_id);
                        if (r == null)
                            throw new Exception("Unknown residue "+line.substring(17, 21).trim().toUpperCase());
                        r.setLabel(resId);
                        for (Map.Entry<String, float[]> e : atoms.entrySet())
                            r.setAtomCoordinates(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]);
                        atoms.clear();
                    }
                    //residue is an amino-acid if the CA atom is detected
                    else if (line.substring(12, 16).trim().equals("CA")) {
                        aa_id++;
                        isInsideProtein = true;
                        isInsideNucleicAcid = false;
                    }
                    float[] coord = new float[]{Float.parseFloat(line.substring(30, 38).trim()),
                            Float.parseFloat(line.substring(38, 46).trim()),
                            Float.parseFloat(line.substring(46, 54).trim())};
                    if (r != null) {
                        r.setAtomCoordinates(line.substring(12, 16).trim(), coord[0], coord[1], coord[2]);
                    }
                    //if the parser doesn't know the type of Residue currently parsed, we store the atoms in a temporary Map
                    else
                        atoms.put(line.substring(12, 16).trim(), coord);
                } else if (tag.equalsIgnoreCase("TER"))
                    ter_tag = true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            java.util.List<String> texts = new ArrayList<String>();
            java.util.List<MessagingSystemAction> closeActions = new ArrayList<MessagingSystemAction>(),
                    nextActions = new ArrayList<MessagingSystemAction>();
            texts.add("Assemble2 had problems to load data.");
            closeActions.add(null);
            nextActions.add(null);
            texts.add("Click next to report a bug.");
            closeActions.add(null);
            nextActions.add(new MessagingSystemAction() {
                @Override
                public void run() {
                    ReportDialog d = new ReportDialog(mediator);
                    d.setTitle("Bug Report");
                    d.getReportContent().setContentType("text/html");
                    d.getReportContent().setText("Dear Assemble2 team,<br/><br/>" +
                                    "My Assemble2 had problems to load the following data:<br/><br/>" +
                                    "<pre>"+fullContent.toString()+"</pre>" +
                                    "<br/><br/><b>My details:</b><br/>" +
                                    "Assemble2 version: " + IoUtils.getAssemble2Release() + "<br/>"+
                                    "Operating system: "+ System.getProperty("os.name") +"<br/>"+
                                    "Java version: " + System.getProperty("java.version") + "<br/><br/>" +
                                    "Cheers."
                    );
                    final java.awt.Dimension win =  Toolkit.getDefaultToolkit().getScreenSize().getSize();
                    d.setSize(win.width / 2, win.height / 2);
                    d.setResizable(false);
                    IoUtils.centerOnScreen(d);
                    d.setVisible(true);
                }
            });
            mediator.getSecondaryCanvas().getMessagingSystem().addThread(texts, closeActions, nextActions);
            mediator.getSecondaryCanvas().repaint();
            return null;
        }
        return tertiaryStructures;
    }

    public static SecondaryStructure parseRnaml(File f, Mediator mediator) throws Exception {
        SAXBuilder builder = new SAXBuilder(false);
        builder.setValidation(false);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document document = builder.build(f);
        Element root = document.getRootElement();
        Element child = null;
        String name = null;
        Molecule m = null;
        SecondaryStructure ss = null;
        for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
            child = (Element) i.next();
            name = child.getName();
            if (name.equals("molecule")) {
                String moleculeSequence = "", moleculeName = "RNA";
                Element sequence = child.getChild("sequence");
                if (sequence != null) {
                    Element seqdata = sequence.getChild("seq-data");
                    if (seqdata != null)
                        moleculeSequence = seqdata.getValue().trim().replaceAll("\\s+", "");
                }
                m = new Molecule(moleculeName, moleculeSequence);
                Element structure = child.getChild("structure");
                if (structure != null) {
                    Location allSingleStrandsLocation = new Location(1, m.size());
                    Element str_annotation = structure.getChild("model").getChild("str-annotation");
                    ss = new SecondaryStructure(mediator, m, "2D");
                    for (Object e : str_annotation.getChildren("helix")) {
                        Element helixElement = (Element) e;
                        int _5end = Integer.parseInt(helixElement.getChild("base-id-5p").getChild("base-id").getChild("position").getText()),
                                _3end = Integer.parseInt(helixElement.getChild("base-id-3p").getChild("base-id").getChild("position").getText()),
                                length = Integer.parseInt(helixElement.getChild("length").getText());
                        Location l = new Location(new Location(_5end, _5end + length - 1), new Location(_3end - length + 1, _3end));
                        Helix h = ss.addHelix(l, "H"+helixElement.getAttributeValue("id"));
                        if (h != null) //not pseudoknot
                            allSingleStrandsLocation.remove(l);
                    }
                    for (Object e : str_annotation.getChildren("base-pair")) {
                        Element bp = (Element) e;
                        char edge1 = '(', edge2 = ')';
                        switch (bp.getChild("edge-5p").getText().charAt(0)) {
                            case 'S' : case 's' : edge1 = '{' ; break;
                            case 'H' : edge1 = '[' ; break;
                            case 'W' : case '+': case '-': edge1 = '(' ; break; //++ is for GC and -- if for AU base-pairs
                            case '!' : case '?' : edge1 = '!' ; break;
                            default: edge1 = '?';
                        }
                        switch (bp.getChild("edge-3p").getText().charAt(0)) {
                            case 'S' : case 's' : edge2 = '}' ; break;
                            case 'H' : edge2 = ']' ; break;
                            case 'W' : case '+': case '-': edge2 = ')' ; break;   //++ is for GC and -- if for AU base-pairs
                            case '!' : case '?' : edge2 = '!' ; break;
                            default: edge2 = '?';
                        }
                        Location l = new Location(new Location(Integer.parseInt(bp.getChild("base-id-5p").getChild("base-id").getChild("position").getText())), new Location(Integer.parseInt(bp.getChild("base-id-3p").getChild("base-id").getChild("position").getText())));
                        boolean isSecondary = false;
                        for (Helix h:ss.getHelices())
                            if (h.getLocation().hasPosition(l.getStart()) && h.getLocation().hasPosition(l.getEnd())) {
                                h.addSecondaryInteraction(l, bp.getChild("bond-orientation").getText().toUpperCase().toCharArray()[0],edge1,edge2);
                                isSecondary = true;
                                break;
                            }
                        if (!isSecondary)
                            ss.addTertiaryInteraction(l, bp.getChild("bond-orientation").getText().toUpperCase().toCharArray()[0], edge1, edge2);
                    }
                    int[] boundaries = allSingleStrandsLocation.getEnds();
                    for (int j = 0; j < boundaries.length; j += 2)
                        ss.addSingleStrand(new Location(boundaries[j], boundaries[j + 1]), "SS" + (j + 1));
                }
            }
        }
        return ss;
    }

    public static List<Double[]> parseSVG(Reader reader) throws Exception {
        SAXBuilder builder = new SAXBuilder(false);
        Document document = builder.build(reader);
        Element root = document.getRootElement();
        List<Double[]> coords = new ArrayList<Double[]>();
        for (Object child:root.getChildren()) {
            if (((Element)child).getName().equals("g")) {
                for (Object _child:((Element)child).getChildren()) {
                    if (((Element)_child).getName().equals("g") && "seq".equals(((Element)_child).getAttributeValue("id"))) {
                        for (Object textNode:((Element)_child).getChildren()) {
                            Double[] coord = new Double[2];
                            coord[0] = Double.parseDouble(((Element)textNode).getAttributeValue("x"));
                            coord[1] = Double.parseDouble(((Element)textNode).getAttributeValue("y"));
                            coords.add(coord);
                        }
                        return coords;
                    }
                }
            }
        }
        return coords;
    }

    public static List<SecondaryStructure> parseFasta(Reader reader, Mediator mediator) {
        List<SecondaryStructure> secondaryStructures = new ArrayList<SecondaryStructure>();
        StringBuffer fullContent = new StringBuffer();
        try {
            List<Molecule> molecules = new ArrayList<Molecule>();
            BufferedReader in = new BufferedReader(reader);
            StringBuffer seq = new StringBuffer();
            String name = null;
            String line = null;
            while ((line = in.readLine()) != null) {
                fullContent.append(line);
                if (line.startsWith(">")) {
                    if (seq.length() != 0 && name != null) {
                        String sequence = seq.toString().toUpperCase();
                        molecules.add(new Molecule(name, sequence));
                    }
                    name = line.substring(1);
                    seq = new StringBuffer();
                } else
                    seq.append(line.replace('.', '-').replace('_', '-').replace(" ", ""));
            }
            //the last
            if (seq.length() != 0 && name != null) {
                String sequence = seq.toString().toUpperCase();
                molecules.add(new Molecule(name, sequence));
            }
            in.close();

            if (molecules.size() > 1) {
                Molecule m = (Molecule) JOptionPane.showInputDialog(null, "Choose the molecule to load", "Choose the molecule to load", JOptionPane.PLAIN_MESSAGE, null, molecules.toArray(), molecules.get(0));
                //secondaryStructures.add(new Contrafold(mediator).fold(m));
                secondaryStructures.add(new Rnafold(mediator).fold(m));
                secondaryStructures.addAll(new Rnasubopt(mediator).fold(m));

                StringBuffer fastaData = new StringBuffer(); //if several molecules, we compute the 2D with mlocarna
                for (Molecule _m : molecules) {
                    fastaData.append(">" + _m.getName().replaceAll("\\s", "_") + "\n");
                    fastaData.append(_m.printSequence() + "\n");
                }
                if (Mlocarna.useForFoldingLandscape) {
                    try {
                        Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> result = new Mlocarna(mediator).align(fastaData.toString(), null);
                        for (SecondaryStructure ss : result.getFirst().getSecond())
                            if (ss.getMolecule().getName().equals(m.getName().replaceAll("\\s", "_"))) {
                                secondaryStructures.add(ss);
                                break;
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            } else {
                Molecule m = molecules.get(0);
                //secondaryStructures.add(new Contrafold(mediator).fold(m));
                secondaryStructures.add(new Rnafold(mediator).fold(m));
                secondaryStructures.addAll(new Rnasubopt(mediator).fold(m));
            }
        } catch (Exception e) {
            java.util.List<String> texts = new ArrayList<String>();
            java.util.List<MessagingSystemAction> closeActions = new ArrayList<MessagingSystemAction>(),
                    nextActions = new ArrayList<MessagingSystemAction>();
            texts.add("Assemble2 had problems to load data.");
            closeActions.add(null);
            nextActions.add(null);
            texts.add("Click next to report a bug.");
            closeActions.add(null);
            nextActions.add(new MessagingSystemAction() { //if the user close at this step, this means that he doesn't want the basic introduction anymore
                @Override
                public void run() {
                    ReportDialog d = new ReportDialog(mediator);
                    d.setTitle("Bug Report");
                    d.getReportContent().setContentType("text/html");
                    d.getReportContent().setText("Dear Assemble2 team,<br/><br/>" +
                                    "My Assemble2 had problems to load the following data:<br/><br/>" +
                                    "<pre>"+fullContent.toString()+"</pre>" +
                                    "<br/><br/><b>My details:</b><br/>" +
                                    "Assemble2 version: " + IoUtils.getAssemble2Release() + "<br/>" +
                                    "Operating system: " + System.getProperty("os.name") + "<br/>" +
                                    "Java version: " + System.getProperty("java.version") + "<br/><br/>" +
                                    "Cheers."
                    );
                    final java.awt.Dimension win =  Toolkit.getDefaultToolkit().getScreenSize().getSize();
                    d.setSize(win.width/2, win.height/2);
                    d.setResizable(false);
                    IoUtils.centerOnScreen(d);
                    d.setVisible(true);
                }
            });
            mediator.getSecondaryCanvas().getMessagingSystem().addThread(texts, closeActions, nextActions);
            mediator.getSecondaryCanvas().repaint();
        }
        return secondaryStructures;
    }

    public static SecondaryStructure parseCT(Reader reader, Mediator mediator) throws Exception {
        StringBuffer sequence = new StringBuffer();
        BufferedReader in = new BufferedReader(reader);
        List<MutablePair<Location,String>> allpairs = new ArrayList<MutablePair<Location,String>>();
        String line = null;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            String[] tokens = line.split("\\s+");
            if (tokens.length != 6 || !tokens[0].matches("-?\\d+(.\\d+)?"))
                continue;
            sequence.append(tokens[1]);
            int base5, base3;
            base5 = Integer.parseInt(tokens[0]);
            base3 = Integer.parseInt(tokens[4]);
            String type = BaseBaseInteraction.ORIENTATION_CIS+"()";
            if (base3 != 0) {
                Location l =new Location(new Location(base5), new Location(base3));
                if (base5 <= base3)
                    allpairs.add(new MutablePair<Location, String>(l,type));
            }
        }
        MutablePair<List<Location>,List<MutablePair<Location,String>>> result = constructSecondaryStructure(allpairs);
        in.close();
        Molecule m = new Molecule("A");
        for (int i=0 ; i < sequence.length() ; i++)
            m.addResidue(""+sequence.charAt(i));

        SecondaryStructure ss = new SecondaryStructure(mediator, m, result.left, new ArrayList<MutablePair<Location,String>>(), result.right);
        return ss;
    }

    public static SecondaryStructure parseVienna(File f, Mediator mediator) throws Exception {
        SecondaryStructure ss = parseVienna(new FileReader(f),mediator);
        if (ss != null)
            ss.setName("Parsed from "+f.getName());
        return ss;
    }

    public static SecondaryStructure parseVienna(Reader reader, Mediator mediator) throws Exception {
        Map<Molecule,MutablePair<List<Location>,List<MutablePair<Location,String>>>> secondaryStructures = new HashMap<Molecule,MutablePair<List<Location>,List<MutablePair<Location,String>>>>();
        BufferedReader in = new BufferedReader(reader);
        StringBuffer seq = new StringBuffer();
        StringBuffer bn = new StringBuffer();
        String name = null;
        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.startsWith(">")) {
                if (seq.length() != 0 && name != null) {
                    StringBuffer sequence =new StringBuffer(seq.toString());
                    bn = new StringBuffer(bn.toString().split(" ")[0]); //to remove the thermodynamic value at the end of the line (if any)

                    int index = sequence.lastIndexOf("-");
                    while(index >= 0) {
                        sequence = sequence.deleteCharAt(index);
                        bn = bn.deleteCharAt(index);
                        index = sequence.lastIndexOf("-");
                    }

                    List<MutablePair<Location,String>> allpairs = new ArrayList<MutablePair<Location,String>>();
                    List<Integer> leftPositions =  new ArrayList<Integer>();
                    for (int i=0; i < bn.length();i++) {
                        if (bn.charAt(i) == '(')
                            leftPositions.add(i+1);
                        else if (bn.charAt(i) == ')') {
                            allpairs.add(new MutablePair<Location, String>(new Location(new Location(bn.charAt(bn.length()-1)), new Location(i+1)),BaseBaseInteraction.ORIENTATION_CIS+"()"));
                            leftPositions.remove(leftPositions.size()-1);
                        }
                    }
                    MutablePair<List<Location>,List<MutablePair<Location,String>>> result = constructSecondaryStructure(allpairs);
                    Molecule m = new Molecule(name);
                    for (int i=0 ; i < sequence.length() ; i++)
                        m.addResidue((""+sequence.charAt(i)).toUpperCase());
                    secondaryStructures.put(m,result);
                }
                name = line.substring(1);
                seq = new StringBuffer();
                bn = new StringBuffer();
            }
            else if (line.startsWith(".") || line.startsWith("(") || line.startsWith(")"))
                bn.append(line);
            else
                seq.append(line);
        }
        //the last
        if (seq.length() != 0 && name != null) {

            StringBuffer sequence =new StringBuffer(seq.toString());
            bn = new StringBuffer(bn.toString().split("\\s+")[0]); //to remove the thermodynamic value at the end of the line (if any)
            int index = sequence.lastIndexOf("-");
            while(index >= 0) {
                sequence = sequence.deleteCharAt(index);
                bn = bn.deleteCharAt(index);
                index = sequence.lastIndexOf("-");
            }

            List<MutablePair<Location,String>> allpairs = new ArrayList<MutablePair<Location,String>>();
            List<Integer> leftPositions =  new ArrayList<Integer>();
            for (int i=0; i < bn.length();i++) {
                if (bn.charAt(i) == '(')
                    leftPositions.add(i+1);
                else if (bn.charAt(i) == ')') {
                    allpairs.add(new MutablePair<Location, String>(new Location(new Location(leftPositions.get(leftPositions.size()-1)), new Location(i+1)),BaseBaseInteraction.ORIENTATION_CIS+"()"));
                    leftPositions.remove(leftPositions.size()-1);
                }
            }
            MutablePair<List<Location>,List<MutablePair<Location,String>>> result = constructSecondaryStructure(allpairs);
            Molecule m = new Molecule(name);
            for (int i=0 ; i < sequence.length() ; i++)
                m.addResidue((""+sequence.charAt(i)).toUpperCase());
            secondaryStructures.put(m,result);
        }
        in.close();
        Molecule m = null;
        if (secondaryStructures.size() > 1)
            m = (Molecule) JOptionPane.showInputDialog(null, "Choose a molecule", "Choose a molecule", JOptionPane.PLAIN_MESSAGE, null, secondaryStructures.keySet().toArray(), secondaryStructures.keySet().iterator().next());
        else if (!secondaryStructures.isEmpty())
            m = secondaryStructures.keySet().iterator().next();
        if (m != null) {
            SecondaryStructure ss = new SecondaryStructure(mediator, m, secondaryStructures.get(m).left, new ArrayList<MutablePair<Location,String>>(), secondaryStructures.get(m).right);
            return ss;
        }

        return null;
    }

    public static SecondaryStructure parseBPSeq(File f, Mediator mediator) throws Exception  {
        return parseBPSeq(new FileReader(f), mediator);
    }

    public static SecondaryStructure parseBPSeq(Reader reader, Mediator mediator) throws Exception {
        StringBuffer sequence = new StringBuffer();
        BufferedReader in = new BufferedReader(reader);
        List<MutablePair<Location,String>> allpairs = new ArrayList<MutablePair<Location,String>>();
        String line = null;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            String[] tokens = line.split(" ");
            if (tokens.length != 3 || !tokens[0].matches("-?\\d+(.\\d+)?"))
                continue;
            sequence.append(tokens[1]);
            int base5, base3;
            base5 = Integer.parseInt(tokens[0]);
            base3 = Integer.parseInt(tokens[2]);
            String type = BaseBaseInteraction.ORIENTATION_CIS+"()";
            if (base3 != 0) {
                Location l =new Location(new Location(base5), new Location(base3));
                if (base5 <= base3)
                    allpairs.add(new MutablePair<Location, String>(l,type));
            }
        }
        MutablePair<List<Location>,List<MutablePair<Location,String>>> result = constructSecondaryStructure(allpairs);
        in.close();
        Molecule m = new Molecule("A");
        for (int i=0 ; i < sequence.length() ; i++)
            m.addResidue(""+sequence.charAt(i));
        return new SecondaryStructure(mediator, m, result.left, new ArrayList<MutablePair<Location,String>>(), result.right);
    }

    private static MutablePair<List<Location>,List<MutablePair<Location,String>>> constructSecondaryStructure(List<MutablePair<Location,String>> allpairs) {
        List<Location> helices = new ArrayList<Location>();
        List<MutablePair<Location,String>> tertiaryInteractions = new ArrayList<MutablePair<Location,String>>();
        if (!allpairs.isEmpty()) {
            Collections.sort(allpairs, new Comparator<MutablePair<Location,String>>() {
                public int compare(MutablePair<Location,String> o, MutablePair<Location,String> o1) {
                    return o.left.getStart() - o1.left.getStart();
                }
            });

            Location newHelix = null;
            Location l1 = null, l2 = null;
            String type1 = null, type2 = null;
            for (int i = 0; i < allpairs.size()-1 ; i++) {
                l1 = allpairs.get(i).left;
                type1 = allpairs.get(i).right;
                l2 = allpairs.get(i+1).left;
                type2 = allpairs.get(i+1).right;
                //System.out.println("l1: "+l1);
                //System.out.println("l2: "+l2);
                if (l1.getStart()+1 == l2.getStart() && type1.equals(BaseBaseInteraction.ORIENTATION_CIS+"()") && l1.getEnd()-1 == l2.getEnd() && type2.equals(BaseBaseInteraction.ORIENTATION_CIS+"()") ) { //if the basepairs are contiguous
                    if (newHelix == null) {
                        newHelix = new Location(l1, l2);
                        //System.out.println("new Helix "+newHelix);
                    }
                    else {
                        newHelix.add(l2);
                        //System.out.println("extension of Helix "+newHelix);
                    }
                }
                else {
                    if (newHelix == null) {
                        tertiaryInteractions.add(new MutablePair<Location,String>(new Location(l1),type1));
                        //System.out.println("tertiary interaction "+l1);
                    }
                    else {
                        helices.add(newHelix);
                        //System.out.println("end of helix "+newHelix);

                    }
                    newHelix = null;
                }
            }
            //last helix
            if (newHelix != null) {
                helices.add(newHelix);
                //System.out.println("end of helix "+newHelix);
            }
            else  {
                tertiaryInteractions.add(new MutablePair<Location,String>(new Location(l2),type2));
                //System.out.println("tertiary interaction "+l2);
            }
        }
        return new MutablePair<List<Location>, List<MutablePair<Location, String>>>(helices,tertiaryInteractions);
    }


    public static void parseQuantitativeValues(File f, final Mediator mediator) throws Exception {
        BufferedReader buff = new BufferedReader(new FileReader(f));
        String line = null;
        float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
        while ((line = buff.readLine())!= null) {
            String[] tokens = null;
            if (line.indexOf(';') != -1)
                tokens = line.trim().split(";");
            else if (line.split("\\s+").length >= 2)
                tokens = line.trim().split("\\s+");
            else
                tokens = line.trim().split("\\t+");
            Location location = new Location(tokens[0].trim());

            float value = Float.parseFloat(tokens[1].trim());
            if (value > max)
                max = value;
            if (value < min && value >= 0)
                min = value;

            for (int absolutePos: location.getSinglePositions())
                if (absolutePos <= mediator.getSecondaryStructure().getMolecule().size())  {
                    mediator.getSecondaryStructure().getResidue(absolutePos).setQuantitativeValue(""+value);
                    for (SecondaryStructure ss:mediator.getFoldingLandscape().getAllSecondaryStructures())
                        ss.getResidue(absolutePos).setQuantitativeValue("" + value);
                }
        }

        mediator.getSecondaryCanvas().getGraphicContext().setMaxQuantitativeValue(max);
        mediator.getSecondaryCanvas().getGraphicContext().setMinQuantitativeValue(min);
        mediator.getSecondaryCanvas().getGraphicContext().displayQuantitativeValues(true);
        mediator.getSecondaryCanvas().repaint();

        mediator.getFoldingLandscape().getGraphicContext().setMaxQuantitativeValue(max);
        mediator.getFoldingLandscape().getGraphicContext().setMinQuantitativeValue(min);
        mediator.getFoldingLandscape().getGraphicContext().displayQuantitativeValues(true);
        mediator.getFoldingLandscape().repaint();

    }

    public static void parseQualitativeValues(File f, Mediator mediator) throws Exception {
        mediator.getSecondaryCanvas().getGraphicContext().clearQualitativeNames();
        mediator.getFoldingLandscape().getGraphicContext().clearQualitativeNames();

        BufferedReader buff = new BufferedReader(new FileReader(f));
        String line = null;
        while ((line = buff.readLine())!= null) {
            String[] tokens = null;
            if (line.indexOf(';') != -1)
                tokens = line.trim().split(";");
            else if (line.split("\\s+").length >= 2)
                tokens = line.trim().split("\\s+");
            else
                tokens = line.trim().split("\\t+");
            Location location = new Location(tokens[0].trim());
            String value = StringUtils.join(Arrays.copyOfRange(tokens, 1, tokens.length), " ");
            mediator.getSecondaryCanvas().getGraphicContext().addQualitative(value);
            mediator.getFoldingLandscape().getGraphicContext().addQualitative(value);

            for (int absolutePos: location.getSinglePositions())
                if (absolutePos <= mediator.getSecondaryStructure().getMolecule().size())  {
                    mediator.getSecondaryStructure().getResidue(absolutePos).setQualitativeValue(value);
                    for (SecondaryStructure ss:mediator.getFoldingLandscape().getAllSecondaryStructures())
                        ss.getResidue(absolutePos).setQualitativeValue(value);
                }
        }

        mediator.getSecondaryCanvas().getGraphicContext().displayQualitativeValues(true);
        mediator.getSecondaryCanvas().repaint();

        mediator.getFoldingLandscape().getGraphicContext().displayQualitativeValues(true);
        mediator.getFoldingLandscape().repaint();

        JMenu menu = mediator.getAssemble().getQualitativeColorMenu();

        menu.removeAll();

        menu.add(new ColorMenu(mediator, "No Value", ColorMenu.QUALITATIVE_VALUES_NO_VALUE));

        List<String> categoryNames = new ArrayList<String>(mediator.getSecondaryCanvas().getGraphicContext().getQualitativeNames().keySet());
        Collections.sort(categoryNames);

        for (String categoryName:categoryNames)
            menu.add(new ColorMenu(mediator, categoryName, ColorMenu.QUALITATIVE_VALUES_CATEGORY));

    }

    public static void parseStockholm(Reader content, Mediator mediator, String referenceMoleculeId) throws Exception {
        Map<String,StringBuffer> alignedMolecules = new HashMap<String,StringBuffer>();
        List<Molecule> molecules = new ArrayList<Molecule>();
        List<SecondaryStructure> secondaryStructures = new ArrayList<SecondaryStructure>();
        BufferedReader buff = new BufferedReader(content);
        StringBuffer bn = new StringBuffer();
        String line = null, moleculeName = null;
        while ((line = buff.readLine())!= null) {
            String[] tokens = line.trim().split("\\s+");
            if (line.trim().length() != 0 && !line.startsWith("# ") && tokens.length == 2) {
                if (alignedMolecules.containsKey(tokens[0]))
                    alignedMolecules.put(tokens[0], alignedMolecules.get(tokens[0]).append(tokens[1].replace(".", "-")));
                else
                    alignedMolecules.put(tokens[0],new StringBuffer(tokens[1].replace(".", "-")));
            }
            else if (line.trim().length() != 0 && line.startsWith("#=GC SS_cons"))
                bn.append(tokens[2].replace("<", "(").replace(">", ")").replace(":", "."));
            else if (line.trim().startsWith("#=GF DE"))
                moleculeName = line.split("#=GF DE")[1].trim();
        }
        buff.close();
        List<AlignedMolecule> alignedSequences = new ArrayList<AlignedMolecule>();
        AlignedMolecule referenceMolecule = null;
        SecondaryStructure reference2D = null;
        for (Map.Entry<String,StringBuffer> alignedMolecule: alignedMolecules.entrySet()) {
            Molecule m = new Molecule(alignedMolecule.getKey(), alignedMolecule.getValue().toString().replace("-",""));
            if (ObjectId.isValid(alignedMolecule.getKey())) {//if the name of the molecule in the clustalw content is a valid ObjectId string, it becomes the id of the Molecule object
                m.setId(alignedMolecule.getKey());
            }
            AlignedMolecule bs = new AlignedMolecule(mediator, m, alignedMolecule.getValue().toString());
            if (alignedMolecule.getKey().equals(referenceMoleculeId))
                referenceMolecule = bs;
            molecules.add(m);
            alignedSequences.add(bs);
        }
        if (referenceMoleculeId == null) {
            Molecule m = (Molecule) JOptionPane.showInputDialog(null, "Choose the molecule to display", "Choose the molecule to display", JOptionPane.PLAIN_MESSAGE, null, molecules.toArray(), molecules.get(0));
            referenceMoleculeId = m.getName();
            for (AlignedMolecule am:alignedSequences) {
                if (am.getMolecule().equals(m)) {
                    referenceMolecule = am;
                    break;
                }
            }
        }
        for (Molecule m:molecules) {
            StringBuffer alignedSeq = alignedMolecules.get(m.getName());
            if (bn.length() > 0) {
                secondaryStructures.add(Modeling2DUtils.getSecondaryStructure(mediator, alignedSeq.toString(), m, bn.toString()));
                if (m.getName().equals(referenceMoleculeId))
                    reference2D = secondaryStructures.get(secondaryStructures.size()-1);
            }
        }

        ReferenceStructure referenceStructure = new ReferenceStructure(mediator, referenceMolecule, reference2D);
        alignedSequences.remove(referenceMolecule);
        StructuralAlignment alignment = new StructuralAlignment(mediator, bn.toString(), referenceMolecule, referenceStructure, alignedSequences);
        mediator.getAlignmentCanvas().setMainAlignment(alignment);
        mediator.loadRNASecondaryStructure(referenceStructure.getSecondaryStructure(), false, true);
        mediator.getAssemble().getFrame().setTitle(moleculeName);
    }

    public static Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> parseClustal(Reader content, Mediator mediator, String referenceMoleculeId) throws Exception {
        Map<String,StringBuffer> alignedMolecules = new HashMap<String,StringBuffer>();
        List<Molecule> molecules = new ArrayList<Molecule>();
        List<SecondaryStructure> secondaryStructures = new ArrayList<SecondaryStructure>();
        BufferedReader buff = new BufferedReader(content);
        StringBuffer bn = new StringBuffer();
        String line = null;
        StringBuffer clustalWContent = new StringBuffer();
        while ((line = buff.readLine())!= null) {
            clustalWContent.append(line+"\n");
            if (line.matches(".+CLUSTAL.+") || line.trim().length() == 0)
                continue;
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length == 2 && !tokens[0].equals("2D") && Pattern.compile("^[-A-Z]+$", Pattern.CASE_INSENSITIVE).matcher(tokens[1]).matches()) {
                if (alignedMolecules.containsKey(tokens[0]))
                    alignedMolecules.put(tokens[0], alignedMolecules.get(tokens[0]).append(tokens[1]));
                else
                    alignedMolecules.put(tokens[0],new StringBuffer(tokens[1]));
            }
            else if (tokens.length >= 2 && tokens[1].matches("^[\\.()\\{\\}\\[\\]]+$"))
                bn.append(tokens[1]);
        }
        buff.close();
        List<AlignedMolecule> alignedSequences = new ArrayList<AlignedMolecule>();
        for (Map.Entry<String,StringBuffer> alignedMolecule: alignedMolecules.entrySet()) {
            Molecule m = new Molecule(alignedMolecule.getKey(), alignedMolecule.getValue().toString().replace("-",""));
            AlignedMolecule bs = new AlignedMolecule(mediator, m, alignedMolecule.getValue().toString());
            molecules.add(m);
            alignedSequences.add(bs);
        }
        for (Molecule m:molecules) {
            StringBuffer alignedSeq = alignedMolecules.get(m.getName());
            if (bn.length() > 0)
                secondaryStructures.add(Modeling2DUtils.getSecondaryStructure(mediator, alignedSeq.toString(), m, bn.toString()));
        }
        Pair<Pair<String,List<SecondaryStructure>>, List<AlignedMolecule>> result = null;
        if (secondaryStructures.isEmpty()) { //it was a clustalw alignment without any structural information
            result = new Rnalifold(mediator).fold(clustalWContent.toString());
        } else {
            result = new Pair<Pair<String,List<SecondaryStructure>>, List<AlignedMolecule>>(new Pair<String,List<SecondaryStructure>>(bn.toString(), secondaryStructures), alignedSequences);
        }

        return result;
    }

}
