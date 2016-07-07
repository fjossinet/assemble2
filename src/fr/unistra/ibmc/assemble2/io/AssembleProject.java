package fr.unistra.ibmc.assemble2.io;


import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.*;

public class AssembleProject {

    private static File moleculesdirectory, secondarydirectory, tertiarydirectory, alignmentDirectory;
    private Map<String, Molecule> moleculesAlreadyLoaded;
    private Map<String,TertiaryStructure> tertiaryStructureAlreadyLoaded;
    private Map<String,SecondaryStructure> secondaryStructureAlreadyLoaded;
    private Map<String,StructuralAlignment> structuralAlignmentAlreadyLoaded;
    private File location;
    private File chimeraSession;
    private Mediator mediator;

    public AssembleProject(Mediator mediator, File location) {
        this.location = location;
        this.mediator = mediator;
    }

    public File getLocation() {
        return location;
    }

    public File getChimeraSession() {
        return this.chimeraSession;
    }

    public void save() throws Exception {
        if (!location.exists())
            location.mkdir();
        moleculesdirectory = new File(location,Molecule.class.getSimpleName()+"s");
        if (!moleculesdirectory.exists())
            moleculesdirectory.mkdir();
        tertiarydirectory = new File(location, TertiaryStructure.class.getSimpleName()+"s");
        if (!tertiarydirectory.exists())
            tertiarydirectory.mkdir();
        secondarydirectory = new File(location,SecondaryStructure.class.getSimpleName()+"s");
        if (!secondarydirectory.exists())
            secondarydirectory.mkdir();
        alignmentDirectory = new File(location,StructuralAlignment.class.getSimpleName()+"s");
        if (!alignmentDirectory.exists())
            alignmentDirectory.mkdir();
        File sessionFile = new File(location,".session.xml");
        if (!sessionFile.exists())
            sessionFile.createNewFile();
        Element session = new Element("session");
        Element selections = new Element("user-selections");
        session.addContent(selections);

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        Document doc = new Document(session);
        FileWriter writer = new FileWriter(sessionFile);
        outputter.output(doc, writer);
        writer.close();

        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().saveSession(new File(location, "chimera_session.py"));
        saveMolecule(mediator.getSecondaryStructure().getMolecule());
        saveSecondaryStructure(mediator.getSecondaryStructure());
        if (mediator.getTertiaryStructure() != null)
            saveTertiaryStructure(mediator.getTertiaryStructure());
        saveStructuralAlignment(mediator.getAlignmentCanvas().getMainAlignment());
    }

    public void save(SecondaryStructure ss, StructuralAlignment alignment) throws Exception {
        if (!location.exists())
            location.mkdir();
        moleculesdirectory = new File(location,Molecule.class.getSimpleName()+"s");
        if (!moleculesdirectory.exists())
            moleculesdirectory.mkdir();
        tertiarydirectory = new File(location, TertiaryStructure.class.getSimpleName()+"s");
        if (!tertiarydirectory.exists())
            tertiarydirectory.mkdir();
        secondarydirectory = new File(location,SecondaryStructure.class.getSimpleName()+"s");
        if (!secondarydirectory.exists())
            secondarydirectory.mkdir();
        alignmentDirectory = new File(location,StructuralAlignment.class.getSimpleName()+"s");
        if (!alignmentDirectory.exists())
            alignmentDirectory.mkdir();
        File sessionFile = new File(location,".session.xml");
        if (!sessionFile.exists())
            sessionFile.createNewFile();
        Element session = new Element("session");
        Element selections = new Element("user-selections");
        session.addContent(selections);

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        Document doc = new Document(session);
        FileWriter writer = new FileWriter(sessionFile);
        outputter.output(doc, writer);
        writer.close();

        if (mediator.getChimeraDriver() != null)
            mediator.getChimeraDriver().saveSession(new File(location, "chimera_session.py"));
        saveMolecule(ss.getMolecule());
        saveSecondaryStructure(ss);
        if (ss.getLinkedTs() != null)
            saveTertiaryStructure(ss.getLinkedTs());
        saveStructuralAlignment(alignment);
    }

    public void saveMolecule(Molecule m) throws Exception  {
        moleculesdirectory = new File(location,Molecule.class.getSimpleName()+"s");
        if (!moleculesdirectory.exists())
            moleculesdirectory.mkdir();
        File f = new File(moleculesdirectory, m.getId()+".rnaml");

        Element rnamlElement = null;
        Document doc = null;
        rnamlElement = new Element("rnaml");
        doc = new Document(rnamlElement);
        Element moleculeElement = new Element("molecule");
        rnamlElement.addContent(moleculeElement);
        moleculeElement.setAttribute("type", "rna");
        Element identityElement = new Element("identity");
        Element nameElement = new Element("name");
        nameElement.addContent(m.getName());
        identityElement.addContent(nameElement);
        moleculeElement.addContent(identityElement);
        Element sequenceElement = new Element("sequence");
        sequenceElement.setAttribute("length", "" + m.size());
        Element seqdataElement = new Element("seq-data");
        seqdataElement.addContent(m.printSequence());
        sequenceElement.addContent(seqdataElement);
        moleculeElement.addContent(sequenceElement);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileWriter writer = new FileWriter(f);
        outputter.output(doc, writer);
        writer.close();
    }

    public void saveTertiaryStructure(TertiaryStructure ts) throws Exception {
        tertiarydirectory = new File(location, TertiaryStructure.class.getSimpleName()+"s");
        if (!tertiarydirectory.exists())
            tertiarydirectory.mkdir();
        File f = new File(tertiarydirectory, ts.getId()+".rnaml");

        Element rnamlElement = null;
        Document doc = null;
        rnamlElement = new Element("rnaml");
        doc = new Document(rnamlElement);
        Element tertiaryStructureElement = new Element("tertiary-structure");
        rnamlElement.addContent(tertiaryStructureElement);
        tertiaryStructureElement.setAttribute("name", ts.getName());
        tertiaryStructureElement.setAttribute("molecule-ids", ts.getMolecule().getId()+".rnaml");

        for (Residue3D residue3D:ts.getResidues3D()) {
            Element baseElement = new Element("base");
            baseElement.setAttribute("position", ""+residue3D.getAbsolutePosition());
            baseElement.setAttribute("base-id", residue3D.getLabel());
            boolean hasExportedAtom = false;
            for (Residue3D.Atom atom : residue3D.getAtoms()) {
                if (atom.hasCoordinatesFilled()) {
                    hasExportedAtom = true;
                    Element atomElement = new Element("atom");
                    baseElement.addContent(atomElement);
                    atomElement.setAttribute("type", atom.getName());
                    atomElement.setAttribute("x", "" + atom.getX());
                    atomElement.setAttribute("y", "" + atom.getY());
                    atomElement.setAttribute("z", "" + atom.getZ());
                }
            }
            //we don't export the base element if no atom element inside
            if (hasExportedAtom)
                tertiaryStructureElement.addContent(baseElement);
        }
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileWriter writer = new FileWriter(f);
        outputter.output(doc, writer);
        writer.close();
    }

    public void saveSecondaryStructure(SecondaryStructure ss) throws Exception {
        secondarydirectory = new File(location,SecondaryStructure.class.getSimpleName()+"s");
        if (!secondarydirectory.exists())
            secondarydirectory.mkdir();
        File f = new File(secondarydirectory, ss.getId()+".rnaml");

        Element rnamlElement = null;
        Document doc = null;
        rnamlElement = new Element("rnaml");
        doc = new Document(rnamlElement);
        Element strAnnotationElement = new Element("structure-annotation");
        rnamlElement.addContent(strAnnotationElement);
        strAnnotationElement.setAttribute("name", ss.getName());
        strAnnotationElement.setAttribute("molecule-ids", ""+ss.getMolecule().getId()+".rnaml");
        if (ss.getLinkedTs() != null)
            strAnnotationElement.setAttribute("tertiary-structure-id", ""+ss.getLinkedTs().getId()+".rnaml");

        for (BaseBaseInteraction baseBaseInteraction : ss.getTertiaryInteractions()) {
            Element basepairElement = new Element("base-pair");
            strAnnotationElement.addContent(basepairElement);
            basepairElement.setAttribute("base1-id", "" + baseBaseInteraction.getResidue().getAbsolutePosition());
            basepairElement.setAttribute("edge1", "" + baseBaseInteraction.getEdge(baseBaseInteraction.getResidue()));
            basepairElement.setAttribute("base2-id", "" + baseBaseInteraction.getPartnerResidue().getAbsolutePosition());
            basepairElement.setAttribute("edge2", "" + baseBaseInteraction.getEdge(baseBaseInteraction.getPartnerResidue()));
            basepairElement.setAttribute("orientation", "" + baseBaseInteraction.getOrientation());
        }
        for (Helix h : ss.getHelices()) {
            Residue[] _5PrimeEnds = h.get5PrimeEnds();
            Element helixElement = new Element("helix");
            if (Pseudoknot.class.isInstance(h))
                helixElement.setAttribute("type", "pseudoknot");
            strAnnotationElement.addContent(helixElement);
            helixElement.setAttribute("name", h.getName() == null ? "?" : h.getName());
            helixElement.setAttribute("base5-id", "" + _5PrimeEnds[0].getAbsolutePosition());
            helixElement.setAttribute("base3-id", "" + h.get3PrimeEnd(_5PrimeEnds[1]).getAbsolutePosition());
            helixElement.setAttribute("length", "" + h.getLength());
            for (BaseBaseInteraction baseBaseInteraction : h.getSecondaryInteractions()) {
                if (!baseBaseInteraction.isCanonical()) {
                    Element basepairElement = new Element("base-pair");
                    helixElement.addContent(basepairElement);
                    basepairElement.setAttribute("base1-id", "" + baseBaseInteraction.getResidue().getAbsolutePosition());
                    basepairElement.setAttribute("edge1", "" + baseBaseInteraction.getEdge(baseBaseInteraction.getResidue()));
                    basepairElement.setAttribute("base2-id", "" + baseBaseInteraction.getPartnerResidue().getAbsolutePosition());
                    basepairElement.setAttribute("edge2", "" + baseBaseInteraction.getEdge(baseBaseInteraction.getPartnerResidue()));
                    basepairElement.setAttribute("orientation", "" + baseBaseInteraction.getOrientation());
                }
            }
        }
        for (SingleStrand sstrand : ss.getSingleStrands()) {
            Element sstrandElement = new Element("single-strand");
            strAnnotationElement.addContent(sstrandElement);
            sstrandElement.setAttribute("name", sstrand.getName() == null ? "?" : sstrand.getName());
            sstrandElement.setAttribute("base5-id", "" + sstrand.getLocation().getStart());
            sstrandElement.setAttribute("base3-id", "" + sstrand.getLocation().getEnd());
        }
        Element displayElement = new Element("secondary-structure-display");
        displayElement.setAttribute("id", "1");
        strAnnotationElement.addContent(displayElement);
        for (Residue r:ss.getResidues()) {
            Element residue2DElement = new Element("ss-base-coord");
            displayElement.addContent(residue2DElement);
            residue2DElement.setAttribute("base-id", "" + r.getAbsolutePosition());
            residue2DElement.setAttribute("x", "" + r.getX());
            residue2DElement.setAttribute("y", "" + r.getY());
        }

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileWriter writer = new FileWriter(f);
        outputter.output(doc, writer);
        writer.close();
    }

    public void saveStructuralAlignment(StructuralAlignment structuralAlignment) throws Exception {
        alignmentDirectory = new File(location,StructuralAlignment.class.getSimpleName()+"s");
        if (!alignmentDirectory.exists())
            alignmentDirectory.mkdir();

        File f = new File(alignmentDirectory, structuralAlignment.getId()+".rnaml");

        Element rnamlElement = null;
        Document doc = null;
        rnamlElement = new Element("rnaml");
        doc = new Document(rnamlElement);
        Element alignmentElement = new Element("alignment");
        //store the current source as an xml element
        //alignmentElement.setAttribute("source", structuralAlignment.getSource().toString());
        alignmentElement.setAttribute("name", structuralAlignment.getName());
        alignmentElement.setAttribute("structure-annotation-id", structuralAlignment.getReferenceStructure().getSecondaryStructure().getId()+".rnaml");
        rnamlElement.addContent(alignmentElement);
        Element consensusElement = new Element("consensus2D");
        consensusElement.setText(structuralAlignment.getConsensusStructure().getSequence());
        alignmentElement.addContent(consensusElement);
        int i=0;
        for (AlignedMolecule alignedMolecule:structuralAlignment.getAlignedMolecules()) { //the molecules are saved sorted
            saveMolecule(alignedMolecule.getMolecule());
            Element aliSequenceElement = new Element("ali-sequence");
            alignmentElement.addContent(aliSequenceElement);
            aliSequenceElement.setAttribute("molecule-id", alignedMolecule.getMolecule().getId() + ".rnaml");
            aliSequenceElement.setAttribute("position", ""+(i++));
            Element  structuralIdentityElement = new Element("structural-identity");
            aliSequenceElement.addContent(structuralIdentityElement);
            structuralIdentityElement.setAttribute("start", "1");
            structuralIdentityElement.setAttribute("end", ""+alignedMolecule.getMolecule().size());
            if (!alignedMolecule.getGapsLocation().isEmpty()) {
                structuralIdentityElement.addContent(alignedMolecule.getGapsLocation().toString());
            }
        }
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileWriter writer = new FileWriter(f);
        outputter.output(doc, writer);
        writer.close();
    }

    public List<StructuralAlignment> getStructuralAlignments() {
        return new ArrayList<StructuralAlignment>(structuralAlignmentAlreadyLoaded.values());
    }

    public List<SecondaryStructure> getSecondaryStructures() {
        return new ArrayList<SecondaryStructure>(this.secondaryStructureAlreadyLoaded.values());
    }

    public List<TertiaryStructure> getTertiaryStructures() {
        return new ArrayList<TertiaryStructure>(this.tertiaryStructureAlreadyLoaded.values());
    }

    public List<Molecule> getMolecules() {
        return new ArrayList<Molecule>(this.moleculesAlreadyLoaded.values());
    }

    public void load(Mediator mediator) throws Exception {
        this.moleculesAlreadyLoaded = new HashMap<String, Molecule>();
        this.tertiaryStructureAlreadyLoaded = new HashMap<String, TertiaryStructure>();
        this.secondaryStructureAlreadyLoaded = new HashMap<String, SecondaryStructure>();
        this.structuralAlignmentAlreadyLoaded = new HashMap<String, StructuralAlignment>();
        //the molecule files are not loaded. only those necessary will be loaded through the molecule-id reference from the other files (alignments, 2D, 3D,...)
        //a Molecules directory is mandatory, otherwise it is not a ParadiseProject
        for (File rnamlFile:new File(location,"Molecules").listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith("rnaml");
            }
        })) {
            if (!this.moleculesAlreadyLoaded.containsKey(rnamlFile.getName()))
                this.parseRnamlFile(rnamlFile);
        }
        if (new File(location,SecondaryStructure.class.getSimpleName()+"s").exists())
            for (File rnamlFile:new File(location,SecondaryStructure.class.getSimpleName()+"s").listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().endsWith("rnaml");
                }
            })) {
                if (!this.secondaryStructureAlreadyLoaded.containsKey(rnamlFile.getName()))
                    this.parseRnamlFile(rnamlFile);
            }
        if (new File(location,TertiaryStructure.class.getSimpleName()+"s").exists())
            for (File rnamlFile:new File(location,TertiaryStructure.class.getSimpleName()+"s").listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().endsWith("rnaml");
                }
            })) {
                if (!this.tertiaryStructureAlreadyLoaded.containsKey(rnamlFile.getName()))
                    this.parseRnamlFile(rnamlFile);
            }
        if (new File(location,StructuralAlignment.class.getSimpleName()+"s").exists())
            for (File rnamlFile:new File(location,StructuralAlignment.class.getSimpleName()+"s").listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().endsWith("rnaml");
                }
            })) {
                if (!this.structuralAlignmentAlreadyLoaded.containsKey(rnamlFile.getName()))
                    this.parseRnamlFile(rnamlFile);
            }
        File sessionFile = new File(location,".session.xml");
        if (sessionFile.exists()) {
            SAXBuilder builder = new SAXBuilder(false);
            for (Object o:builder.build(sessionFile).getRootElement().getChild("user-selections").getChildren("user-selection")) {
                Element userSelection = (Element)o;
            }
        }
        File chimera_session = new File(location,"chimera_session.py");
        if (chimera_session.exists())
            chimeraSession = chimera_session;
    }

    public void parseRnamlFile(File rnamlFile) throws Exception {
        if (this.moleculesAlreadyLoaded == null)
            this.moleculesAlreadyLoaded = new HashMap<String, Molecule>();
        if (this.tertiaryStructureAlreadyLoaded == null)
            this.tertiaryStructureAlreadyLoaded = new HashMap<String, TertiaryStructure>();
        if (this.secondaryStructureAlreadyLoaded == null)
            this.secondaryStructureAlreadyLoaded = new HashMap<String, SecondaryStructure>();
        if (this.structuralAlignmentAlreadyLoaded == null)
            this.structuralAlignmentAlreadyLoaded = new HashMap<String, StructuralAlignment>();
        SAXBuilder builder = new SAXBuilder(false);
        builder.setValidation(false);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document document = builder.build(rnamlFile);
        Element root = document.getRootElement();
        this.traverseJDOMElement(rnamlFile, root);
    }

    private void traverseJDOMElement(File rnamlFile,final Element node) throws Exception {
        if (Thread.interrupted())
            throw new InterruptedException();
        Element child = null;
        String name = null;
        Element sourceEl = null;

        for (Iterator i = node.getChildren().iterator(); i.hasNext();) {
            child = (Element) i.next();
            name = child.getName();
            if (name.equals("molecule")) {
                String moleculeSequence = "", moleculeName = "";
                Element identity = child.getChild("identity");
                String type = "rna";
                Attribute _type = child.getAttribute("type");
                if (_type != null)
                    type = _type.getValue();
                if (identity != null) {
                    Element nameEl = identity.getChild("name");
                    moleculeName = nameEl.getValue();
                }
                Element sequence = child.getChild("sequence");
                if (sequence != null) {
                    Element seqdata = sequence.getChild("seq-data");
                    if (seqdata != null)
                        moleculeSequence = seqdata.getValue().trim();
                }
                Molecule m = null;
                if ("rna".equals(type)) {
                    m = new Molecule(moleculeName, moleculeSequence);
                    m.setId(rnamlFile.getName().split(".rnaml")[0]);
                    this.moleculesAlreadyLoaded.put(rnamlFile.getName(),m);
                }

            } else if (name.equals("tertiary-structure")) {
                for (String molecule_id: child.getAttributeValue("molecule-ids").split(" ")) {
                    Molecule _m = this.getMolecule(molecule_id);
                    TertiaryStructure ts = new TertiaryStructure(_m);
                    ts.setId(rnamlFile.getName().split(".rnaml")[0]);
                    this.tertiaryStructureAlreadyLoaded.put(molecule_id, ts);
                    int position = 0;
                    boolean allBasesPresentInThe3D = false;
                    for (Object o : child.getChildren("base")) {
                        Element base = (Element) o;
                        position++;
                        String id = base.getAttributeValue("base-id"),
                                _molecule_id = base.getAttributeValue("molecule-id");
                        if (base.getAttribute("molecule-id") == null || molecule_id.equals(_molecule_id)) {
                            Residue3D r = null;
                            if (base.getAttributeValue("position") != null) //we have a version after the bug of the 1.0 version
                                r = ts.addResidue3D(Integer.parseInt(base.getAttributeValue("position")));
                            else if (rnamlFile.getAbsolutePath().indexOf("mypdb") != -1) //its the file fom the mypdb library with the bug, then all the resiudes of the 2D are in the 3D. We can use the position counter
                                r = ts.addResidue3D(position);
                            else { //its a 3D model RNAML file with the numbering-system bug.
                                if (allBasesPresentInThe3D) //then no problem, the counter can be used
                                    r = ts.addResidue3D(position);
                                else //then the value of the base-id is considered as the absolute position (as it was the case before the 1.0 version).
                                    r = ts.addResidue3D(Integer.parseInt(id));
                            }
                            for (Object e : base.getChildren("atom")) {
                                Element atom = (Element) e;
                                r.setAtomCoordinates(atom.getAttributeValue("type"), Float.parseFloat(atom.getAttributeValue("x")), Float.parseFloat(atom.getAttributeValue("y")), Float.parseFloat(atom.getAttributeValue("z")));
                            }
                        }
                    }
                }
            }
            else if (name.equals("structure-annotation")) {
                for (String molecule_id: child.getAttributeValue("molecule-ids").split(" ")) {
                    Molecule m = this.getMolecule(molecule_id);
                    SecondaryStructure ss = new SecondaryStructure(this.mediator, m,child.getAttributeValue("name"));
                    if (child.getAttributeValue("tertiary-structure-id") != null) {
                        TertiaryStructure ts = this.getTertiaryStructure(molecule_id, child.getAttributeValue("tertiary-structure-id"));
                        ss.setLinkedTs(ts);
                    }
                    this.secondaryStructureAlreadyLoaded.put(molecule_id, ss);
                    try {
                        ss.setId(rnamlFile.getName().split(".rnaml")[0]);
                    } catch (NumberFormatException e) {
                        //if this exception is catched, this means that this was an old paradise project parsed for which the name of the feature was used to create the corresponding file.
                    }
                    for (Object e : child.getChildren("base-pair")) {
                        Element bp = (Element) e;
                        if (bp.getAttribute("molecule1-id") == null || bp.getAttributeValue("molecule1-id").equals(molecule_id) && bp.getAttributeValue("molecule2-id").equals(molecule_id)) {
                            char edge1 = '(', edge2 = ')';
                            switch (bp.getAttributeValue("edge1").toCharArray()[0]) {
                                case 'S' : case '{': edge1 = '{' ; break;
                                case 'H' : case '[': edge1 = '[' ; break;
                                case 'W' : case '(': edge1 = '(' ; break;
                                case '!' : case '?' : edge1 = '!' ; break;
                                default: edge1 = '?';
                            }
                            switch (bp.getAttributeValue("edge2").toCharArray()[0]) {
                                case 'S' : case '}': edge2 = '}' ; break;
                                case 'H' : case ']': edge2 = ']' ; break;
                                case 'W' : case ')': edge2 = ')' ; break;
                                case '!' : case '?' : edge2 = '!' ; break;
                                default: edge2 = '?';
                            }
                            ss.addTertiaryInteraction(new Location(new Location(Integer.parseInt(bp.getAttributeValue("base1-id"))), new Location(Integer.parseInt(bp.getAttributeValue("base2-id")))),bp.getAttributeValue("orientation").toCharArray()[0],edge1,edge2);
                        }
                    }
                    int index =  0;
                    for (Object e : child.getChildren("helix")) {
                        Element helixElement = (Element) e;
                        boolean isPseudoknot = helixElement.getAttribute("type") != null && helixElement.getAttributeValue("type").equals("pseudoknot");
                        if (helixElement.getAttribute("molecule1-id") == null || helixElement.getAttributeValue("molecule1-id").equals(molecule_id) && helixElement.getAttributeValue("molecule2-id").equals(molecule_id)) {
                            int _5end = Integer.parseInt(helixElement.getAttributeValue("base5-id")),
                                    _3end = Integer.parseInt(helixElement.getAttributeValue("base3-id")),
                                    length = Integer.parseInt(helixElement.getAttributeValue("length"));
                            Helix h =  null;
                            if (isPseudoknot)
                                h = ss.addPseudoknot(new Location(new Location(_5end, _5end + length - 1), new Location(_3end - length + 1, _3end)),helixElement.getAttributeValue("name") != null ? helixElement.getAttributeValue("name") : "H"+(index++));
                            else
                                h = ss.addHelix(new Location(new Location(_5end, _5end + length - 1), new Location(_3end - length + 1, _3end)),helixElement.getAttributeValue("name") != null ? helixElement.getAttributeValue("name") : "H"+(index++));
                            if (h != null)
                                for (Object o : helixElement.getChildren("base-pair")) {
                                    Element bp = (Element) o;
                                    char edge1 = '(', edge2 = ')';
                                    switch (bp.getAttributeValue("edge1").toCharArray()[0]) {
                                        case 'S' : case '{': edge1 = '{' ; break;
                                        case 'H' : case '[': edge1 = '[' ; break;
                                        case 'W' : case '(': edge1 = '(' ; break;
                                        case '!' : edge1 = '!' ; break;
                                        default: edge1 = '?';
                                    }
                                    switch (bp.getAttributeValue("edge2").toCharArray()[0]) {
                                        case 'S' : case '}': edge2 = '}' ; break;
                                        case 'H' : case ']': edge2 = ']' ; break;
                                        case 'W' : case ')': edge2 = ')' ; break;
                                        case '!' : edge2 = '!' ; break;
                                        default: edge2 = '?';
                                    }
                                    ss.addSecondaryInteraction(new Location(new Location(Integer.parseInt(bp.getAttributeValue("base1-id"))), new Location(Integer.parseInt(bp.getAttributeValue("base2-id")))),bp.getAttributeValue("orientation").toCharArray()[0],edge1,edge2);
                                }
                        }
                    }
                    for (Object e : child.getChildren("single-strand")) {
                        Element sstrandElement = (Element)e;
                        if (sstrandElement.getAttribute("molecule-id") == null || sstrandElement.getAttributeValue("molecule-id").equals(molecule_id)) {
                            int _5end = Integer.parseInt(sstrandElement.getAttributeValue("base5-id")),
                                    _3end = Integer.parseInt(sstrandElement.getAttributeValue("base3-id"));
                            ss.addSingleStrand(new Location(_5end, _3end),sstrandElement.getAttributeValue("name"));
                        }
                    }
                    Element displayElement = child.getChild("secondary-structure-display");
                    if (displayElement != null) {
                        for (Object o : displayElement.getChildren("ss-base-coord")) {
                            Element coord = (Element) o;
                            if (coord.getAttribute("molecule-id") == null || coord.getAttributeValue("molecule-id").equals(molecule_id))
                                ss.setCoordinates(Integer.parseInt(coord.getAttributeValue("base-id")),Float.parseFloat(coord.getAttributeValue("x")), Float.parseFloat(coord.getAttributeValue("y")));
                        }
                        ss.setPlotted(true);
                    }
                }
            }
            else if (child.getName().equals("alignment")) {
                String alignmentName = child.getAttributeValue("name");
                List<AlignedMolecule> sortedMolecules = new ArrayList<AlignedMolecule>();
                AlignedMolecule referenceAlignedSequence = null;
                Molecule referenceMolecule = null;
                String consensusStructure = child.getChild("consensus2D") == null ? null : child.getChild("consensus2D").getText().trim();
                for (Object o : child.getChildren("ali-sequence")) {
                    Element alisequence = (Element)o;
                    Molecule m = this.getMolecule(alisequence.getAttributeValue("molecule-id"));
                    StringBuffer alignedSequence = new StringBuffer(m.printSequence());
                    for (Object _o:alisequence.getChildren("structural-identity")) {
                        Element structuralIdentityElement = (Element)_o;
                        if (structuralIdentityElement.getText().trim().length() != 0) {
                            Location gapsLocation = new Location(structuralIdentityElement.getText());
                            for (int gapLocation:gapsLocation.getSinglePositions()) {
                                alignedSequence.insert(gapLocation-1,'-');
                            }
                        }
                    }
                    AlignedMolecule a = new AlignedMolecule(mediator, m, alignedSequence.toString());
                    if (alisequence.getAttribute("position") != null && alisequence.getAttributeValue("position").equals("0")) {
                        referenceAlignedSequence = a;
                        referenceMolecule = m;
                    } else
                        sortedMolecules.add(a);
                }
                if (referenceAlignedSequence == null) {
                    referenceAlignedSequence = sortedMolecules.get(0);
                    referenceMolecule = referenceAlignedSequence.getMolecule();
                    sortedMolecules.remove(referenceAlignedSequence);
                }
                SecondaryStructure ss = null;
                if (child.getAttribute("structure-annotation-id") != null)
                    ss = this.getSecondaryStructure(referenceMolecule.getId()+".rnaml", child.getAttributeValue("structure-annotation-id"));
                if (ss != null) {
                    if (consensusStructure == null) { //if no consensus was stored in the file, it is the reference structure as bracket notation with the same gaps than the reference aligned sequence
                        StringBuffer consensus = new StringBuffer(ss.printAsBracketNotation());
                        for (int j = 0 ; j < referenceAlignedSequence.size() ; j++)
                            if (referenceAlignedSequence.getSymbol(j).isGap() && j >= consensus.length())
                                consensus.append('-');
                            else if (referenceAlignedSequence.getSymbol(j).isGap())
                                consensus.insert(j,'-');
                        consensusStructure = consensus.toString();
                    }
                    StructuralAlignment alignment = new StructuralAlignment(mediator, consensusStructure, referenceAlignedSequence, new ReferenceStructure(mediator, referenceAlignedSequence, ss) , sortedMolecules);
                    alignment.setName(alignmentName);
                    alignment.setId(rnamlFile.getName().split(".rnaml")[0]);
                    this.structuralAlignmentAlreadyLoaded.put(rnamlFile.getName(),alignment);
                }
            }
            this.traverseJDOMElement(rnamlFile,child);
        }
    }

    private TertiaryStructure getTertiaryStructure(String moleculeName, String tertiaryStructureName) throws Exception {
        //is the ts already constructed and stored ?
        if (this.tertiaryStructureAlreadyLoaded.containsKey(moleculeName))
            return this.tertiaryStructureAlreadyLoaded.get(moleculeName);
        //if no, we inject it in the working session
        File f = new File(new File(location,"TertiaryStructures"), tertiaryStructureName);
        if (f.exists()) {
            this.parseRnamlFile(f);
            return this.tertiaryStructureAlreadyLoaded.get(moleculeName);
        }
        else
            return null;
    }

    private SecondaryStructure getSecondaryStructure(String moleculeName, String secondaryStructureName) throws Exception {
        //is the ss already constructed and stored ?
        if (this.secondaryStructureAlreadyLoaded.containsKey(moleculeName))
            return this.secondaryStructureAlreadyLoaded.get(moleculeName);
        //if no, we inject it in the working session
        File f = new File(new File(location,"SecondaryStructures"), secondaryStructureName);
        if (f.exists()) {
            this.parseRnamlFile(f);
            return this.secondaryStructureAlreadyLoaded.get(moleculeName);
        }
        else
            return null;
    }

    private Molecule getMolecule(String fileName) throws Exception {
        //is the molecule already constructed and stored ?
        if (this.moleculesAlreadyLoaded.containsKey(fileName))
            return this.moleculesAlreadyLoaded.get(fileName);
        //if no, we inject it in the working session
        this.parseRnamlFile(new File(new File(location,"Molecules"),fileName));
        return this.moleculesAlreadyLoaded.get(fileName);
    }

    public static javax.swing.filechooser.FileFilter getFileFilter() {
        return new javax.swing.filechooser.FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() || IoUtils.isAssembleProject(file);
            }

            public String getDescription() {
                return "Assemble Projects";
            }
        };
    }

    /**
     * Return a map (name of the secondary structure, RNAML file describing it)
     * @param molecule
     * @return
     */
    public Map<String,File> getAllSecondaryStructuresForMolecule(Molecule molecule) throws Exception {
        Map<String,File> secondaryStructures = new HashMap<String, File>();
        if (new File(location,SecondaryStructure.class.getSimpleName()+"s").exists())
            for (File rnamlFile:new File(location,SecondaryStructure.class.getSimpleName()+"s").listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().endsWith("rnaml");
                }
            })) {
                SAXBuilder sxb = new SAXBuilder();
                Document document = sxb.build(rnamlFile);
                for (String token:document.getRootElement().getChild("structure-annotation").getAttributeValue("molecule-ids").split(".rnaml")) {
                    if (token.trim().equals(molecule.getId()))
                        secondaryStructures.put(document.getRootElement().getChild("structure-annotation").getAttributeValue("name"),rnamlFile);
                }
            }
        return secondaryStructures;
    }

}
