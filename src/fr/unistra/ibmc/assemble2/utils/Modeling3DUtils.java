package fr.unistra.ibmc.assemble2.utils;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.io.Modeling3DException;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.io.FileParser;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

public class Modeling3DUtils {

    public static String DNA = "DNA", RNA = "RNA";

    private static String[][] referenceLWBasePairs = new String[][]{
            {"1JJ2","A1912-A1927","CWW"},
            {"402D","A105-C112","CWW"},
            {"402D", "C112-A105","CWW"},
            {"1MME","C170-C30","CWW"},
            {"1GID","G215-A105","CWW"},
            {"280D","G4-C21","CWW"},
            {"420D","U14-A19","CWW"},
            {"413D","U7-C8","CWW"},
            {"1GID","A105-G215","CWW"},
            {"420D","A19-U14","CWW"},
            {"280D","C21-G4","CWW"},
            {"413D","C8-U7","CWW"},
            {"433D","G9-U20","CWW"},
            {"433D","U20-G9","CWW"},
            {"280D","U19-U6","CWW"},
            {"1GID","A151-A248","TWW"},
            {"1JJ2","A1742-C2037","TWW"},
            {"1JJ2","C2037-A1742","TWW"},
            {"1B23","C16-C59","TWW"},
            {"4TRA","G15-C48","TWW"},
            {"1JJ2","U205-A437","TWW"},
            {"1JJ2","U1432-C1394","TWW"},
            {"1JJ2","A437-U205","TWW"},
            {"4TRA","C48-G15","TWW"},
            {"1JJ2","C1394-U1432","TWW"},
            {"402D","G126-G126","TWW"}, //Pb to parse the 402D PDB => listed in the annoying PDBs
            {"1JJ2","G1970-U1966","TWW"},
            {"1JJ2","U1966-G1970","TWW"},
            {"1JJ2","U2621-U1838","TWW"},
            {"1QA6","C122-C142","CWH"},
            {"1FJG","G724-A665","CWH"}, //error in the publication for this bp
            {"1GID","U258-A105","CWH"},
            {"420D","A27-G6","CWH"},
            //{"","A-U","CWH"},		// based on "420D","A27-G6","CWH"
            {"1GID","C260-G108","CWH"},
            //{"","C-U","CWH"},		// based on "420D","A27-G6","CWH"
            {"1JJ2","G702-G744","CWH"},
            {"1JJ2","U2581-G2602","CWH"},
            {"1GID","U259-U107","CWH"},
            {"283D","A7-A6","TWH"},
            {"1DUL","C163-A148","TWH"},
            {"1JJ2","C1834-C1841","TWH"},
            {"354D","U103-A73","TWH"},
            {"2TRA","A46-G22","TWH"},
            {"437D","C8-G12","TWH"},
            {"1JJ2","G2082-G535","TWH"},
            {"1GID","G188-U168","TWH"},
            {"1JJ2","U2419-G2404","TWH"},
            {"1OSU","U1-U2","TWH"},
            {"1JJ2","A857-A1845","CWS"},
            {"437D","A23-C15","CWS"},
            {"1JJ2","C1920-A419","CWS"},
            {"1JJ2","C1428-C1439","CWS"},
            //{"","G-A","CWS"},		// based on "1JJ2","U2063-A2083"
            {"1JJ2","G1302-C1353","CWS"},
            {"1JJ2","U2063-A2083","CWS"},
            //{"","U-C","CWS"},		// based on "1JJ2","G1302-C1353"
            {"1JJ2","A923-G2480","CWS"},
            {"1JJ2","A1081-U626","CWS"},
            {"1JJ2","C2720-G2763","CWS"},
            {"1J5E","C519-U516","CWS"},	// new ndb id: RR0052
            {"1JJ2","G921-G2279","CWS"},
            {"1JJ2","G1002-U966","CWS"},
            {"1JJ2","U1435-G1389","CWS"},
            //{"","U-U","CWS"},		// based on "1JJ2","G1302-C1353"
            {"1JJ2","A2018-A1829","TWS"},
            //{"","A-C","TWS"},		// based on "1JJ2","C963-C959"
            {"1JJ2","C1983-A1981","TWS"},
            {"1JJ2","C963-C959","TWS"},
            {"1EC6","G10-C7","TWS"},
            {"1JJ2","U779-A776","TWS"},
            {"1J5E","U1205-C1200","TWS"},	// new ndb id: RR0052
            {"437D","A24-G7","TWS"},
            //{"","A-U","TWS"},		// based on "1JJ2","C963-C959"
            {"1JJ2","C26-G22","TWS"},
            //{"","C-U","TWS"},		// based on "1JJ2","C963-C959"
            {"1JJ2","G1773-U1770","TWS"},
            {"1JJ2","U121-G51","TWS"},
            //{"","U-U","TWS"},		// based on "IJ5E","U1205-C1200"
            {"1JJ2","G2033-A1742","CHH"},
            {"1JJ2","G2494-C2493","CHH"},
            {"1JJ2","A1742-G2033","CHH"},
            {"1JJ2","C2493-G2494","CHH"},
            {"1JJ2","G2616-G2617","CHH"},
            {"430D","A9-A21","THH"},
            {"1JJ2","A1875-C1856","THH"},
            {"1JJ2","C1856-A1875","THH"},
            {"1FJG","G428-A415","THH"},
            {"1JJ2","G2397-C2391","THH"},
            {"1JJ2","U2853-A2902","THH"},
            {"1JJ2","U2889-C2868","THH"},
            {"1FJG","A415-G428","THH"},
            {"1JJ2","A2902-U2853","THH"},
            {"1JJ2","C2391-G2397","THH"},
            {"1JJ2","C2868-U2889","THH"},
            {"1JJ2","G2428-G2462","THH"},
            {"1GID","A225-A226","CHS"},
            {"1JJ2","A1106-C1105","CHS"},
            {"1JJ2","C2533-A2532","CHS"},
            {"1FJG","C748-C749","CHS"},
            //{"","G-A","CHS"},		// based on "1JJ2","G2093-G2092"
            {"1GRZ","U412-A410","CHS"},
            //{"","U-C","CHS"},		// based on "1JJ2","U831-U832"
            {"1JJ2","A1236-G1235","CHS"},
            {"1J5E","A1499-U1498","CHS"},	// new ndb id: RR0052
            {"1J5E","C596-G595","CHS"},		// new ndb id: RR0052
            {"1DRZ","C156-U155","CHS"},
            {"1JJ2","G2093-G2092","CHS"},
            {"430D","U11-G10","CHS"},
            {"1JJ2","U831-U832","CHS"},
            {"1GID","A113-A207","THS"},
            {"1JJ2","A498-C494","THS"},
            {"1JJ2","C2769-A2805","THS"},
            {"1JJ2","C162-C173","THS"},
            {"1JJ2","U1831-A1845","THS"},
            {"354D","A78-G98","THS"},
            {"1JJ2","A2398-U2390","THS"},
            {"1JJ2","C2704-U2690","THS"},
            {"1JJ2","G722-G706","THS"},
            {"1JJ2","U12-G531","THS"},
            {"1JJ2","A1458-A784","CSS"},
            {"1JJ2","A1459-C783","CSS"},
            {"1JJ2","C906-A1329","CSS"}, //error in the publication for this bp
            //{"","C-C","CSS"},		// based on "1QA6","A135-C154"
            {"1JJ2","G190-A204","CSS"},
            //{"","G-C","CSS"},		// based on "1QA6","A135-C154"
            {"1QA6","U155-A134","CSS"},
            {"1JJ2","U2595-C1993","CSS"},
            {"1F27","A23-G19","CSS"},
            {"1JJ2","A1007-U2297","CSS"},
            {"1GID","C197-G200","CSS"},
            {"1JJ2","C879-U779","CSS"},
            {"1JJ2","G2113-G885","CSS"},
            //{"","G-U","CSS"},		// based on "1JJ2","A1007-U2297"
            {"1JJ2","U2115-G2632","CSS"},
            //{"","U-U","CSS"},		// based on "1JJ2","U121-C110"
            {"1JJ2","A306-A340","TSS"},
            {"1JJ2","A867-C880","TSS"},
            //{"","G-A","TSS"},		// based on "1JJ2","G32-G456"
            {"1JJ2","G2588-C2542","TSS"},
            {"437D","A20-G4","TSS"},
            {"1QRS","A21-U8","TSS"},
            {"1JJ2","G32-G456","TSS"},
            {"1JJ2","G1855-U1874","TSS"}
    };

    /**
     * return the references of the base-pairs used for the publication Leontis, Stombaugh and Westhof 2002
     * @return  a map whose key if the pdbID and the value a list pair of String describing the basepair ("A1912-A1927","CWCWC") for example
     */
    public static Map<String,List<Pair<String,String>>> getReferenceLWBasePairs() {
        Map<String,List<Pair<String,String>>> basepairs =  new HashMap<String,List<Pair<String,String>>>();
        for (String[] reference:referenceLWBasePairs)
            if (basepairs.containsKey(reference[0]))
                basepairs.get(reference[0]).add(new Pair<String,String>(reference[1],reference[2]));
            else {
                List<Pair<String,String>> pairs = new ArrayList<Pair<String,String>>();
                pairs.add(new Pair<String,String>(reference[1],reference[2]));
                basepairs.put(reference[0],pairs);
            }
        return basepairs;
    }

    public static String[] getReferencePDBIDs() {
        return new String[]{"157D","1A34","1A9N","1C04","1C0A","1CSL",
                "1D4R","1DDY","1DFU","1DI2","1DPL","1DQF","1DRZ",
                "1E7K","1E8O","1EC6","1EFW","1EHZ","1ET4",
                "1EVP","1F1T","1F27","1F7U","1FIR","1G1X",
                "1GAX","1GID","1GRZ","1H3E","1H4S","1HQ1","1HR2","1HVU",
                "1I2X","1I2Y","1I6U","1I9V","1I9X","1ID9","1IL2","1J1U",
                "1J2B","1J5E","1J9H","1JBR","1JBS","1JBT","1JID", "1JJ2",
                "1JZV","1K8W","1K9W","1KD3","1KH6","1KOG","1KUQ","1KXK",
                "1L2X","1L9A","1LNG","1LNT","1MFQ",
                "1MHK","1MJI","1MMS","1MZP","1N38","1N78",
                "1NBS","1NLC","1NTA","1OOA",
                "1P6V","1PWF","1Q96","1Q9A","1QA6","1QBP","1QC0",
                "1QF6","1QTQ","1QU2","1QZW","1R3E","1R9F","1RC7",
                "1RLG","1RNA","1S03","1S72","1SER","1SZ1",
                "1T0E","1T0K","1TTT","1U0B","1U1Y",
                "1U63","1U6B","1U9S","1UN6","1URN","1VFG","1WSU",
                "1WZ2","1X8W","1XJR","1XOK","1XPE","1Y0Q","1Y26","1Y69",
                "1Y77","1YRJ","1YTU","1YYK","1YYW",
                "1YZ9","1YZD","1Z7F","1ZBH","1ZCI","1ZDJ","1ZDK",
                "1ZEV","1ZFT","1ZH5","1ZHO","1ZSE","205D","246D",
                "259D","280D","2A0P","2A2E","2A43","2A64","2AB4",
                "2ANN","2ANR","2AO5","2AVY","2AW4","2AZ0","2AZX",
                "2B2D","2B3J","2B63","2BH2","2BQ5","2BS0",
                "2BTE","2BU1","2C4Y","2C4Z","2C50","2C51","2CKY","2CT8",
                "2CZJ","2D6F","2DER","2DET","2DLC","2DR2","2DR8",
                "2DU3","2E9R","2E9Z","2EZ6","2F8K","2F8S",
                "2FCX","2FCZ","2FK6","2FMT","2FQN","2G3S","2G5K",
                "2G91","2G9C","2GDI","2GIS",
                "2H1M","2HOJ","2HOP","2HVY","2HW8","2I82","2IL9",
                "2IPY","2IY5","2IZ9","2IZN","2J01","2NPY",
                "2NR0","2NRE","2NUE","2OE5","2OEU","2OIU",
                "2OIY","2OUE","2OZB","2P7E","2PJP","2PWT","2PXL",
                "2QBZ","2QUX","2R20","2R22","2R8S","2R92","2R93","2RFK",
                "2TRA","2UWM","2V3C","2V6W","2Z75","315D","332D",
                "353D","354D","361D","364D","377D","387D","397D",
                "3B31","3BSO","3TRA","402D","409D","418D","419D","420D",
                "422D","430D","433D","438D","439D",
                "472D","480D", "485D","488D","5MSF","6MSF","7MSF"};
    }

    public static String[] getPDBIDsContainingReferenceLWBasePairs() {
        Set<String> pdbIDs = new HashSet<String>();
        for (String[] referenceBp:referenceLWBasePairs)
            pdbIDs.add(referenceBp[0]);
        String[] _pdbIs = new String[pdbIDs.size()];
        Iterator<String> it = pdbIDs.iterator();
        for (int i=0; i< pdbIDs.size();i++)
            _pdbIs[i] = it.next();
        return _pdbIs;
    }

    public static String[] getAnnoyingPDBIDs() {
        return new String[]{"1B23","1B7F","1BY4","1CVJ","1DUQ","1EGK","1FEU","1FUF","1HMH","1J8G","1L3Z","1M5O","1M8V","1MME",
                "1N35","1N7A","1NUJ","1NYI","1OB2","1Q2R","1R9S","1RPU","1SDR","1TFW","1TFY","1T0D","1UTD","1YKV","1YLS","1YVP","1ZE2",
                "1ZX7","2A04","2AWE","2BGG","2BJ6","2DEU","2E9T","2EC0","2G32","2G92","2GJW","2GRB", "2GXB",
                "2I91","2NOK","2NQP","2NUG","2Q1O","2NZ4","2VAL","398D","429D","434D","435D","466D"};
    }

    public static Residue3D compute3DResidue(Mediator mediator, TertiaryStructure ts, Residue residue) throws Exception{
        int step = 0;
        File tmpPdb = IoUtils.createTemporaryFile("nahelix");
        String seq1;
        String seq2;
        fr.unistra.ibmc.assemble2.model.Molecule m = residue.getMolecule();
        seq1 = m.printSequence(new Location(residue.getAbsolutePosition(),residue.getAbsolutePosition()));
        seq2 = "";
        for (int i = 0; i < seq1.length(); i++)
            seq2 += 'X';
        NahelixAlgorithm nahelixAlgorithm = new NahelixAlgorithm();
        nahelixAlgorithm.getRNAasPDBFile(residue.getAbsolutePosition(), residue.getAbsolutePosition(), seq1, seq2, tmpPdb, step);
        //the PDB file generated by the NaHelix algorithm is parsed
        TertiaryStructure structuralDomain3D = FileParser.parsePDB(mediator, new FileReader(tmpPdb)).get(0);
        Residue3D computedResidue3D = structuralDomain3D.getResidues3D().get(0);
        Residue3D newResidue3D = ts.addResidue3D(residue.getAbsolutePosition());
        for (Residue3D.Atom a:computedResidue3D.getAtoms())
            if (a.hasCoordinatesFilled()) {
                newResidue3D.getAtom(a.getName()).setCoordinates(a.getX(),a.getY(),a.getZ());
            }
        return newResidue3D;
    }

    public static List<Residue3D> compute3DSingleStrand(Mediator mediator, TertiaryStructure ts, Location l) throws Exception {
        List<Residue3D> residues = new ArrayList<Residue3D>();
        int step = 0;
        String seq1 = ts.getMolecule().printSequence(l);
        String seq2 = "";
        for (int i = 0; i < seq1.length(); i++)
            seq2 += 'X';
        NahelixAlgorithm nahelixAlgorithm = new NahelixAlgorithm();
        File nahelixOutput = IoUtils.createTemporaryFile("nahelix");
        nahelixAlgorithm.getRNAasPDBFile(l.getStart(), l.getEnd(), seq1, seq2, nahelixOutput, step);
        List<TertiaryStructure> tertiaryStructures = FileParser.parsePDB(mediator, new FileReader(nahelixOutput));
        int residueParsed = 0;
        TertiaryStructure strand3D = tertiaryStructures.get(0);
        for (int i = l.getStart(); i <= l.getEnd(); i++) {
            Residue3D computedResidue3D = strand3D.getResidues3D().get(residueParsed++);
            Residue3D newResidue3D = ts.addResidue3D(i);
            for (Residue3D.Atom a:computedResidue3D.getAtoms())
                if (a.hasCoordinatesFilled()) {
                    newResidue3D.getAtom(a.getName()).setCoordinates(a.getX(),a.getY(),a.getZ());
                }
            residues.add(newResidue3D);
        }
        return residues;
    }

    /**
     * compute the 3D fold of an helix. The 3D residues are added to the tertiary structure given as argument. These residues are also returned by the method.
     * @param ts the tertiary structure to which the new 3D residues will be added to.
     * @return the 3D residues making the helix
     * @throws Exception
     */
    public static List<Residue3D> compute3DHelix(Mediator mediator, TertiaryStructure ts, Location l) throws Exception {
        List<Residue3D> residues = new ArrayList<Residue3D>();
        int step = 0;
        File nahelixOutput = IoUtils.createTemporaryFile("nahelix");
        String seq1,seq2;
        seq1 = ts.getMolecule().printSequence(new Location(l.getStart(), l.getStart()+l.getLength()/2-1));
        seq2 = fr.unistra.ibmc.assemble2.model.Molecule.reverse(ts.getMolecule().printSequence(new Location(l.getEnd() - l.getLength() / 2 + 1, l.getEnd())));
        NahelixAlgorithm nahelixAlgorithm = new NahelixAlgorithm();
        nahelixAlgorithm.getRNAasPDBFile(l.getStart(), l.getEnd()-l.getLength()/2+1, seq1, seq2, nahelixOutput, step);
        List<TertiaryStructure> tertiaryStructures = FileParser.parsePDB(mediator, new FileReader(nahelixOutput));
        int residueParsed = 0;
        int start = l.getStart(), end = l.getStart()+l.getLength()/2-1;
        TertiaryStructure strand3D = tertiaryStructures.get(0);
        for (int i = start; i <= end; i++) {
            Residue3D computedResidue3D = strand3D.getResidues3D().get(residueParsed++);
            Residue3D newResidue3D = ts.addResidue3D(i);
            for (Residue3D.Atom a:computedResidue3D.getAtoms())
                if (a.hasCoordinatesFilled()) {
                    newResidue3D.getAtom(a.getName()).setCoordinates(a.getX(),a.getY(),a.getZ());
                }
            residues.add(newResidue3D);
        }
        residueParsed = 0;
        start = l.getEnd()-l.getLength()/2+1;
        end = l.getEnd();
        strand3D = tertiaryStructures.get(1);
        for (int i = start; i <= end; i++) {
            Residue3D computedResidue3D = strand3D.getResidues3D().get(residueParsed++);
            Residue3D newResidue3D = ts.addResidue3D(i);
            for (Residue3D.Atom a:computedResidue3D.getAtoms())
                if (a.hasCoordinatesFilled()) {
                    newResidue3D.getAtom(a.getName()).setCoordinates(a.getX(),a.getY(),a.getZ());
                }
            residues.add(newResidue3D);
        }
        return residues;
    }

    /**
     * Generate a set of 3D residues with the sub-sequence start:length from Molecule m threaded into the 3D fold of the reference 3D residues. These residues are added to the tertiary structure given as argument and returned by the method
     * @param ts
     * @param start
     * @param length
     * @param type
     * @param referenceResidues3D
     * @return
     * @throws Exception
     */
    public static List<Residue3D> thread(Mediator mediator, TertiaryStructure ts, int start, int length, String type, List<Residue3D> referenceResidues3D) throws Exception {
        String subSequence = ts.getMolecule().printSequence(new Location(start,start+length-1));
        //System.out.println("subsequence threaded: "+subSequence+"("+start+":"+length+")");
        List<Residue3D> residues = new ArrayList<Residue3D>();
        //It cannot work efficiently if the residue at the 5'-end doesn't have a phosphate group
        Residue3D firstResidue3D = referenceResidues3D.get(0);
        boolean hasPhosphate = false;
        for (String phosphate:RiboNucleotide3D.P)
            if (firstResidue3D.getAtom(phosphate).hasCoordinatesFilled()) {
                hasPhosphate = true;
                break;
            }
        if (!hasPhosphate) {
            //if no phosphate atom found
            firstResidue3D.setAtomCoordinates("P", 0f, 0f, 0f);
            firstResidue3D.setAtomCoordinates(RiboNucleotide3D.O1P, 0f, 0f, 0f); //difficult to have O1P or O2P if no P detected
            firstResidue3D.setAtomCoordinates(RiboNucleotide3D.O2P, 0f, 0f, 0f);
            if (!firstResidue3D.getAtom(RiboNucleotide3D.O5).hasCoordinatesFilled()) //the O5 atom could be present even if no phosphate atom
                firstResidue3D.setAtomCoordinates(RiboNucleotide3D.O5, 0f, 0f, 0f);
        }
        File tmpPDB = IoUtils.createTemporaryFile("fragment");
        FileParser.writePDBFile(referenceResidues3D, false, new PrintWriter(tmpPDB));
        String pdbData = FileUtils.readFileToString(tmpPDB);
        //first we convert the pdb data to the fgm format
        //System.out.println(pdbData);
        String fgmData = pdb2fgm(mediator, pdbData);
        //System.out.println(fgmData);
        int natom = 100000;  //HORRIBLE HERE !!!!!!! All the array have to have a size proportional to the data processed, and not fixed ones
        double xxr[][] = new double[30][3];
        double xxf[][] = new double[natom][3];
        double xxrot[][] = new double[50][3];
        double x[][][] = new double[2][9][3];
        double xi[][][] = new double[2][9][3];
        double b[][] = new double[3][3];
        double xbid[] = new double[3];
        double xfin[][] = new double[natom][3];
        int nres[] = new int[natom];
        double[][] xbar = new double[2][3];
        double chiseq[] = new double[length];
        String atomf[] = new String[natom];
        String atfin[] = new String[natom];
        char ares[] = new char[natom];
        String atomr[] = new String[30];
        String atnamr[] = {"C5*", "C4*", "C3*", "O4*", "O2*", "C2*", "C1*", "O3*", "N"};
        String atnamd[] = {"C5*", "C4*", "C3*", "O4*", "C2*", "C1*", "O3*", "N"};
        int lat = 9;

        //the fgm data are parsed
        String line = null;
        BufferedReader inputBuff = new BufferedReader(new StringReader(fgmData));
        int nr1 = 1;
        int nwrite = nr1;
        int ki = 0;
        int nr1c =  nr1 - 1;
        try {
            while ((line = inputBuff.readLine()) != null) {
                if (line.length() < 5)
                    throw new Exception("Strange line for FGMFormat "+line);
                FGMFormat fgm = new FGMFormat(line);
                atomf[ki] = fgm.getAtomName();
                xxf[ki][0] = fgm.getX();
                xxf[ki][1] = fgm.getY();
                xxf[ki][2] = fgm.getZ();
                ki++;
                if (fgm.getAtomName().equals("N")) { //N is the last atom of a residue in an FGM format
                    String l = inputBuff.readLine();
                    FGMFormat fgmChi = new FGMFormat(l);
                    chiseq[nr1c] = fgmChi.getChi();
                    nr1c++;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Residue3D>();
        }
        int kfend = ki;
        int iend = length;
        int ipass = 0;
        int l1 = 1;
        int l13 = 13;
        if (DNA.equals(type))
            l13--;
        int lfin = 0;

        for (int i = 0; i < iend; i++) {
            //System.out.println(sequence.charAt(i)+""+i);
            int nwr = nwrite + i;
            if (subSequence.charAt(i) == '0') {
                if (ipass == 0) {
                    l1 += 13;
                    l13 += 13; /* added */
                    if (DNA.equals(type)) {
                        l1--;
                        l13--; /* added */
                    }
                    continue;
                } else {
                    break;
                }
            }
            ipass++;
            if (RNA.equals(type)) {
                for (int l = l1; l <= l13; l++) {
                    for (int ij = 0; ij < atnamr.length; ij++) {
                        if (atomf[l - 1] != null && atomf[l - 1].equals(atnamr[ij])) {
                            for (int k = 0; k < 3; k++) {
                                x[0][ij][k] = xxf[l - 1][k];
                            }
                        }
                    }
                }
            } else {
                for (int l = l1; l <= l13; l++) {
                    for (int ij = 0; ij < atnamd.length; ij++) {

                        if (atomf[l - 1] != null && atomf[l - 1].equals(atnamd[ij])) {
                            for (int k = 0; k < 3; k++) {
                                x[0][ij][k] = xxf[l - 1][k];
                            }
                        }
                    }
                }

            }
            int ipseu = pseudo(x, RNA.equals(type) ? 0:1);

            //System.out.println("PSEUDO=" + ipseu);
            String fileName = null;
            if (RNA.equals(type)) {
                switch (subSequence.charAt(i)) {
                    case 'A':
                        if (ipseu == 0) {
                            fileName = "ADO.RNA";
                        } else {
                            fileName = "ADOS.RNA";
                        }
                        break;
                    case 'C':
                        if (ipseu == 0) {
                            fileName = "CYD.RNA";
                        } else {
                            fileName = "CYDS.RNA";
                        }
                        break;
                    case 'G':
                        if (ipseu == 0) {
                            fileName = "GUO.RNA";
                        } else {
                            fileName = "GUOS.RNA";
                        }
                        break;
                    case 'T':
                        if (ipseu == 0) {
                            fileName = "THD.RNA";
                        } else {
                            fileName = "THDS.RNA";
                        }
                        break;
                    case 'U':
                        if (ipseu == 0) {
                            fileName = "URD.RNA";
                        } else {
                            fileName = "URDS.RNA";
                        }
                        break;
                    case 'P':
                        fileName = "PSEU.RNA";
                        break;
                    case 'D':
                        fileName = "DIH.RNA";
                        break;
                }
            } else {
                switch (subSequence.charAt(i)) {
                    case 'A':
                        if (ipseu == 0) {
                            fileName = "ADON.DNA";
                        } else {
                            fileName = "ADO.DNA";
                        }
                        break;
                    case 'C':
                        if (ipseu == 0) {
                            fileName = "CYDN.DNA";
                        } else {
                            fileName = "CYD.DNA";
                        }
                        break;
                    case 'G':
                        if (ipseu == 0) {
                            fileName = "GUON.DNA";
                        } else {
                            fileName = "GUO.DNA";
                        }
                        break;
                    case 'T':
                        if (ipseu == 0) {
                            fileName = "THDN.DNA";
                        } else {
                            fileName = "THD.DNA";
                        }
                        break;
                    case 'U':
                        if (ipseu == 0) {
                            fileName = "URDN.DNA";
                        } else {
                            fileName = "URD.DNA";
                        }
                        break;
                    default:
                        System.out.println("This letter is not allowed in DNA : " + subSequence.charAt(i));
                }
            }
            BufferedReader in4 = new BufferedReader(new FileReader(new File(new File(Assemble.getTertiaryDataDirectory(),"dict"), fileName)));
            int k = 0;
            while ((line = in4.readLine()) != null) {
                FGMFormat fgm = new FGMFormat(line);
                atomr[k] = fgm.getAtomName();
                xxr[k][0] = fgm.getX();
                xxr[k][1] = fgm.getY();
                xxr[k][2] = fgm.getZ();
                k++;
            }
            in4.close();
            int krend = k;
            for (int l = 0; l < lat; l++) {
                for (k = 0; k < 3; k++) {
                    x[1][l][k] = xxr[l][k];
                }
            }
            compar(x, xi, b, lat,xbar);

            for (int il = lat - 1; il < krend; il++) {
                for (k = 0; k < 3; k++) {
                    xxr[il][k] = xxr[il][k] - xbar[1][k];
                }
                for (k = 0; k < 3; k++) {
                    xbid[k] = xxr[il][k];
                }
                for (k = 0; k < 3; k++) {
                    xxr[il][k] = xbid[0] * b[0][k] + xbid[1] * b[1][k] + xbid[2] * b[2][k];
                }
                for (k = 0; k < 3; k++) {
                    xxr[il][k] += xbar[0][k];
                }
            }
            int lf = l13 - 2;
            for (int l = l1; l <= lf; l++) {
                if (atomf[l - 1] != null) {
                    lfin++;
                    atfin[lfin - 1] = atomf[l - 1];
                    if (atomf[l - 1].equals("O4*")) {
                        for (k = 0; k < 3; k++) {
                            xxrot[0][k] = xxf[l - 1][k];
                        }
                    }
                    if (atomf[l - 1].equals("C1*")) {
                        for (k = 0; k < 3; k++) {
                            xxrot[1][k] = xxf[l - 1][k];
                        }
                    }

                    ares[lfin - 1] = subSequence.charAt(i);
                    nres[lfin - 1] = nr1;
                    for (k = 0; k < 3; k++) {
                        xfin[lfin - 1][k] = xxf[l - 1][k];
                    }
                }
            }
            int nrot = 2;
            int lbg = lfin;
            int ir3 = 0, ir4 = 0;
            for (int l = lat; l <= krend; l++) {
                nrot++;
                lfin++;
                atfin[lfin - 1] = atomr[l - 1];

                if (subSequence.charAt(i) == 'G' || subSequence.charAt(i) == 'A') {
                    if (atomr[l - 1].equals("N9")) {
                        ir3 = nrot;
                    }
                    if (atomr[l - 1].equals("C8")) {
                        ir4 = nrot;
                    }
                } else {
                    if (subSequence.charAt(i) != 'P') {
                        if (atomr[l - 1].equals("N1")) {
                            ir3 = nrot;
                        }
                        if (atomr[l - 1].equals("C6")) {
                            ir4 = nrot;
                        }
                    }
                    if (subSequence.charAt(i) == 'D') {
                        if (atomr[l - 1].equals("C6H2")) {
                            ir4 = nrot;
                        }
                    }
                }

                if (subSequence.charAt(i) == 'P') {
                    if (atomr[l - 1].equals("C5")) {
                        ir3 = nrot;
                    }
                    if (atomr[l - 1].equals("C6")) {
                        ir4 = nrot;
                    }
                }

                ares[lfin - 1] =subSequence.charAt(i);
                nres[lfin - 1] = nr1;
                for (k = 0; k < 3; k++) {
                    xxrot[nrot - 1][k] = xxr[l - 1][k];
                }
            }
            //System.out.println("ir3=" + ir3 + " ir4=" + ir4);
            chirot(xxrot, ir3 - 1, ir4 - 1, chiseq[nr1 - 1], nrot - 1);
            lfin = lbg;
            for (int l = 3; l <= nrot; l++) {
                lfin++;
                for (k = 0; k < 3; k++) {
                    xfin[lfin - 1][k] = xxrot[l - 1][k];
                }
            }
            lfin++;
            int lo3 = l13 - 1;
            atfin[lfin - 1] = atomf[lo3 - 1];
            ares[lfin - 1] = subSequence.charAt(i);
            nres[lfin - 1] = nr1;
            for (k = 0; k < 3; k++) {
                xfin[lfin - 1][k] = xxf[lo3 - 1][k];
            }
            l1 += 13;
            l13 += 13;
            if (DNA.equals(type)) {
                l1--;
                l13--;
            }
            nr1++;
        }

        Residue3D residue3D = null;
        int currentRes = -1;
        for (int i = 0; i < lfin; i++) {
            if (currentRes != nres[i]) {
                residue3D = ts.addResidue3D(nres[i]+start-1);
                //System.out.println("Residue3D created "+residue3D.getName());
                //System.out.println("At "+(nres[i]+start-1));
                residues.add(residue3D);
                currentRes = nres[i];
            }
            //System.out.println("atfin[i] "+atfin[i]);
            if (atfin[i] != null)
                residue3D.setAtomCoordinates(atfin[i],(float)xfin[i][0],(float)xfin[i][1],(float)xfin[i][2]);
        }
        return residues;
    }

    private static class NahelixAlgorithm {

        private static final int P = 1;
        private static final int O5 = 3;


        private int algoStr1;
        private int algoStr2;
        private int algoStr3;
        private String algoSeq1;
        private String algoSeq2;
        private String algoSeq3;
        private int algoLength;
        private File algoOutputFile;

        private static double con = Math.PI / 180;

        /**
         * Prints the result of the construction in a .hd file format
         *
         * @param x    float[] - list of x
         * @param y    float[] - list of y
         * @param z    float[] - list of z
         * @param name String[] - list of atom names
         * @param seq  char[] - list of base names
         * @param rnum int[] - list of residue number
         */
        private void save(float[] x, float[] y, float[] z, String[] name, char seq[], int rnum[], float offsetX, float offsetY, float offsetZ) throws Exception {
            try {
                PrintWriter out = new PrintWriter(new FileWriter(algoOutputFile));
                char chain = 'A';
                char oldChain = chain;
                int i = 0;
                int offset = 0;

                while (name[i] != null) {
                    if (rnum[i] > algoSeq1.length() + algoStr1 - 1) {
                        //             Printing.debug("rnum > algoLength "+rnum[i]+" > "+Nahelix.algoSeq1.length());
                        chain = 'B';
                    }
                    if (oldChain != chain) {
                        String ligne = PDB.getTerString(i + 1 + offset, seq[i - 1] + "", oldChain, rnum[i - 1]);
                        out.println(ligne);
                        offset++;
                    }
                    /*(int nb, String name, String type, char chId, int num, float x, float y, float z)*/
                    if (seq[i] != 'X') {
                        String ligne = PDB.getAtomString(i + 1 + offset, name[i], seq[i] + "", chain, rnum[i], x[i] + offsetX, y[i] + offsetY, z[i] + offsetZ);
                        out.println(ligne);
                        //     System.out.println(ligne);

                    }
                    i++;
                    oldChain = chain;
                }
                String ligne = PDB.getTerString(i + 1 + offset, seq[i - 1] + "", oldChain, rnum[i - 1]);
                out.println(ligne);
                out.close();
            }
            catch (IOException e) {
                System.err.println("In Nahelix.print() : can't write to file : " + algoOutputFile);
                e.printStackTrace();
            }
        }

        private void getRNAasPDBFile(int str1, int str2, String seq1, String seq2, File outputFile, float offsetX) throws Exception {
            algoOutputFile = outputFile;
            HELIXaBr(str1, str2, seq1, seq2, P, offsetX, 0, 0);
        }

        /**
         * Builds a double helix of TYPE_ADNA, TYPE_BDNA or TYPE_RNA
         *
         * @param str1      int - index of the first residue of the first strand
         * @param str2      int - index of the first residue of the second strand
         * @param seq1      String - sequence of the first strand
         * @param seq2      String - sequence of the second strand
         * @param endType   int - end type (P, O3P or O5')
         */
        private void HELIXaBr(int str1, int str2, String seq1, String seq2, int endType, float offsetX, float offsetY, float offsetZ) throws Exception {
            algoSeq1 = seq1;
            algoSeq2 = seq2;
            algoStr1 = str1;
            algoStr2 = str2;
            int size = (seq1.length()+seq2.length())*24;
            float x[] = new float[size];
            float y[] = new float[size];
            float z[] = new float[size];
            String l3f[] = new String[size];
            char seq[] = new char[size];
            int rnum[] = new int[size];

            int jc = 0, jd, jci, jdi;
            jci = 0;
            jdi = 2;

            if (endType == NahelixAlgorithm.P) {
                jci = 1;
            }
            if (endType == NahelixAlgorithm.O5) {
                jci = 4;
                jdi = 5;
            }

            float omega = 0;

            float omeg = 32.7f, rh = 2.81f;
            double[] theta = new double[]{72.8, 70.5, 71.2, 76.6, 60.1, 55.8, 46.9, 41.4, 45.3, 30.2, 33.2, 36.9};
            double[] r = new double[]{9.54, 8.71, 9.59, 7.50, 8.49, 9.75, 9.68, 8.86, 9.15, 10.15, 8.82, 8.55};
            double[] zi = new double[]{-2.44, -3.75, -4.94, -3.80, -3.48, -3.35, -3.10, -4.02, -1.77, -3.14, -3.17, -1.77};
            String[] l3r = new String[]{"O3' ", "P   ", "O1P ", "O2P ", "O5' ", "C5' ", "C4' ", "C3' ", "O4' ", "O2' ", "C2' ", "C1' "};

            seq1 = seq1.toUpperCase();
            seq2 = seq2.toUpperCase();

            int coe = -1;
            int nfi1 = 0;

            for (int k = 0; k < seq1.length(); k++) {
                if (seq1.charAt(k) != 'X') {
                    break;
                }
                nfi1++;
            }

            double arg = coe * (theta[0] - omega - omeg * nfi1) * con;
            x[0] = (float) (r[0] * Math.cos(arg));
            y[0] = (float) (r[0] * Math.sin(arg));
            z[0] = (float) (coe * (zi[0] - rh * nfi1));
            l3f[0] = "O3P ";
            seq[0] = seq1.charAt(nfi1);
            rnum[0] = str1 + nfi1;

            char base;
            for (int i = 1; i <= seq1.length(); i++) {
                base = seq1.charAt(i - 1);
                if (i == 1) {
                    jd = jdi;
                    jc = jc - jci;
                } else {
                    jd = 2;
                }

                if (base != 'X') {
                    for (int j1 = jd - 1; j1 < r.length; j1++) {
                        arg = coe * (theta[j1] - omega) * con;
                        x[jc + j1] = (float) (r[j1] * Math.cos(arg));
                        y[jc + j1] = (float) (r[j1] * Math.sin(arg));
                        z[jc + j1] = (float) (coe * (zi[j1] - rh * (i - 1)));
                        l3f[jc + j1] = l3r[j1];
                        seq[jc + j1] = base;
                        rnum[jc + j1] = str1 + i - 1;
                    }
                    jc += r.length;

                    Atoms at = new Atoms();

                    switch (base) {
                        case 'A':
                            at = AA(coe, i - 1, omega, rh);
                            break;
                        case 'C':
                            at = CA(coe, i - 1, omega, rh);
                            break;
                        case 'G':
                            at = GA(coe, i - 1, omega, rh);
                            break;
                        case 'U':
                            at = UA(coe, i - 1, omega, rh);
                            break;
                        case 'T':
                            at = TA(coe, i - 1, omega, rh);
                            break;
                        default:
                            System.out.println("Nucleotide unknown : " + base);
                    }

                    for (int j = 0; j < at.nb; j++) {
                        x[jc + j] = at.x[j];
                        y[jc + j] = at.y[j];
                        z[jc + j] = at.z[j];
                        l3f[jc + j] = at.name[j];
                        seq[jc + j] = base;
                        rnum[jc + j] = str1 + i - 1;
                    }

                    jc += at.nb;
                }

                omega += omeg;

                if (base != 'X') {
                    arg = coe * (theta[0] - omega) * con;
                    x[jc] = (float) (r[0] * Math.cos(arg));
                    y[jc] = (float) (r[0] * Math.sin(arg));
                    z[jc] = (float) (coe * (zi[0] - rh * i));
                    l3f[jc] = l3r[0];
                    seq[jc] = base;
                    rnum[jc] = str1 + i - 1;
                }

            }
            coe = 1;
            int nfi2 = 0;
            omega = omega - omeg;
            //int ij = 0;

            for (int k = 0; k < seq2.length(); k++) {
                if (seq2.charAt(k) != 'X') {
                    break;
                }
                nfi2++;
            }

            arg = coe * (theta[0] + omega - omeg * nfi2) * con;
            x[jc + 1] = (float) (r[0] * Math.cos(arg));
            y[jc + 1] = (float) (r[0] * Math.sin(arg));
            z[jc + 1] = (float) (coe * (zi[0] + rh * (seq2.length() - 1 - nfi2)));
            l3f[jc + 1] = "O3P ";
            seq[jc + 1] = seq2.charAt(seq2.length() - 1);
            rnum[jc + 1] = str2;
            jc++;

            for (int i = seq2.length(); i >= 1; i--) {
                base = seq2.charAt(i - 1);
                if (i == seq2.length()) {
                    jd = jdi;
                    jc = jc - jci;
                } else {
                    jd = 2;
                }
                if (base != 'X') {
                    for (int j1 = jd - 1; j1 < r.length; j1++) {
                        arg = coe * (theta[j1] + omega) * con;
                        x[jc + j1] = (float) (r[j1] * Math.cos(arg));
                        y[jc + j1] = (float) (r[j1] * Math.sin(arg));
                        z[jc + j1] = (float) (coe * (zi[j1] + rh * (i - 1)));
                        l3f[jc + j1] = l3r[j1];
                        seq[jc + j1] = base;
                        rnum[jc + j1] = str2 + seq2.length() - i;
                    }
                    jc += r.length;

                    Atoms at = new Atoms();

                    switch (base) {
                        case 'A':
                            at = AA(coe, i - 1, omega, rh);
                            break;
                        case 'C':
                            at = CA(coe, i - 1, omega, rh);
                            break;
                        case 'G':
                            at = GA(coe, i - 1, omega, rh);
                            break;
                        case 'U':
                            at = UA(coe, i - 1, omega, rh);
                            break;
                        case 'T':
                            at = TA(coe, i - 1, omega, rh);
                            break;
                    }

                    for (int j = 0; j < at.nb; j++) {
                        x[jc + j] = at.x[j];
                        y[jc + j] = at.y[j];
                        z[jc + j] = at.z[j];
                        l3f[jc + j] = at.name[j];
                        seq[jc + j] = base;
                        rnum[jc + j] = str2 + seq2.length() - i;
                    }
                    jc += at.nb;

                }

                omega = omega - omeg;

                if (base != 'X') {
                    arg = coe * (theta[0] + omega) * con;
                    x[jc] = (float) (r[0] * Math.cos(arg));
                    y[jc] = (float) (r[0] * Math.sin(arg));
                    z[jc] = (float) (coe * (zi[0] + rh * (i - 2)));
                    l3f[jc] = l3r[0];
                    seq[jc] = base;
                    rnum[jc] = str2 + seq2.length() - i;
                }
            }
            save(x, y, z, l3f, seq, rnum, offsetX, offsetY, offsetZ);

        }

        private Atoms AA(int coe, int i, float omega, float rh) throws Exception {
            double[] theta = {37.2, 47.0, 46.2, 30.9, 15.6, 17.4, 5.5, 8.7, 18.0, 27.6};
            double[] r = {7.12, 6.39, 5.15, 5.14, 3.08, 4.39, 5.28, 6.54, 7.15, 6.44};
            double[] zi = {-1.39, -1.33, -.95, -.75, -.05, -.34, -.24, -.54, -.93, -1.01};
            String[] name = {"N9  ", "C8  ", "N7  ", "C5  ", "N6  ", "C6  ", "N1  ", "C2  ", "N3  ", "C4  "};
            return new Atoms(theta, r, zi, name, coe, i, omega, rh);
        }

        private Atoms CA(int coe, int i, float omega, float rh) throws Exception {
            double[] theta = {37.2, 47.6, 50.6, 36.1, 36.8, 24.4, 19.9, 26.8};
            double[] r = {7.12, 6.53, 5.26, 3.23, 4.50, 5.32, 7.51, 6.61};
            double[] zi = {-1.39, -1.38, -1.03, -.31, -.67, -.68, -1.06, -1.04};
            String[] name = {"N1  ", "C6  ", "C5  ", "N4  ", "C4  ", "N3  ", "O2  ", "C2  "};
            return new Atoms(theta, r, zi, name, coe, i, omega, rh);
        }

        private Atoms GA(int coe, int i, float omega, float rh) throws Exception {
            double[] theta = {37.2, 47., 46., 30.5, 13.9, 17.1, 5.5, .7, 8.5, 18., 27.5};
            double[] r = {7.12, 6.36, 5.11, 5.13, 3.16, 4.34, 5.35, 7.58, 6.67, 7.20, 6.44};
            double[] zi = {-1.39, -1.32, -.94, -.74, -.04, -.33, -.26, -.42, -.55, -.94, -1.01};
            String[] name = {"N9  ", "C8  ", "N7  ", "C5  ", "O6  ", "C6  ", "N1  ", "N2  ", "C2  ", "N3  ", "C4  "};
            return new Atoms(theta, r, zi, name, coe, i, omega, rh);
        }

        private Atoms TA(int coe, int i, float omega, float rh) throws Exception {
            double[] theta = {37.2, 47.7, 50.8, 37.1, 37.8, 25.2, 19.6, 26.9, 67.7};
            double[] r = {7.12, 6.54, 5.28, 3.24, 4.42, 5.31, 7.46, 6.63, 5.19};
            double[] zi = {-1.39, -1.38, -1.04, -.32, -.66, -.69, -1.04, -1.04, -1.08};
            String[] name = {"N1  ", "C6  ", "C5  ", "O4  ", "C4  ", "N3  ", "O2  ", "C2  ", "M5  "};
            return new Atoms(theta, r, zi, name, coe, i, omega, rh);
        }

        private Atoms UA(int coe, int i, float omega, float rh) throws Exception {
            double[] theta = {37.2, 47.7, 50.8, 37.1, 37.8, 25.2, 19.6, 26.9};
            double[] r = {7.12, 6.54, 5.28, 3.24, 4.42, 5.31, 7.46, 6.63};
            double[] zi = {-1.39, -1.38, -1.04, -.32, -.66, -.69, -1.04, -1.04};
            String[] name = {"N1  ", "C6  ", "C5  ", "O4  ", "C4  ", "N3  ", "O2  ", "C2  "};
            return new Atoms(theta, r, zi, name, coe, i, omega, rh);
        }

        private class Atoms {
            private float[] x;
            private float[] y;
            private float[] z;
            private String[] name;
            private int nb = 0;

            private Atoms() {
            }

            /**
             * Builds an atom set, with cardinal coordinates
             *
             * @param theta double[] - theta angle of each atom
             * @param r     double[] - radius of each atom
             * @param zi    double[] - cylindric offset of each atom
             * @param name2 String[] - name of each atom
             * @param coe   int - a coefficient
             * @param i     int - position of the base in the sequence
             * @param omega float - rotation step
             * @param rh    float - vertical offset
             */
            private Atoms(double[] theta, double[] r, double[] zi, String[] name2, int coe, int i, float omega, float rh) throws Exception {
                if ((theta.length == r.length) && (zi.length == r.length) && (name2.length == r.length)) {
                    x = new float[theta.length];
                    y = new float[theta.length];
                    z = new float[theta.length];
                    name = new String[theta.length];
                    for (int w = 0; w < theta.length; w++) {
                        double arg = coe * (theta[w] + coe * omega) * NahelixAlgorithm.con;
                        x[w] = (float) (r[w] * Math.cos(arg));
                        y[w] = (float) (r[w] * Math.sin(arg));
                        z[w] = (float) (coe * (zi[w] + coe * rh * i));
                        name[w] = name2[w];
                        nb = x.length;
                    }
                } else {
                    throw new Exception("Wrong number of elements in one of the arrays");
                }
            }
        }

    }

    public static Matrix computeTransformationMatrixToMoveAt3PrimeEnd(Residue3D magnet, Residue3D residueToTranslate) {
        float ap = -0.51939f;
        float bp = 1.70925f;
        float cp = -1.08467f;

        float ao1 = -0.60246f;
        float bo1 = 0.530618f;
        float co1 = -0.883508f;

        float ao2 = -1.52441f;
        float bo2 = 4.57711f;
        float co2 = -1.77632f;

        float ao5 = 0.33345f;
        float bo5 = 1.29635f;
        float co5 = -1.54795f;

        float ax = magnet.getAtom(RiboNucleotide3D.C2).getX() - magnet.getAtom(RiboNucleotide3D.O3).getX();
        float bx = magnet.getAtom(RiboNucleotide3D.C3).getX() - magnet.getAtom(RiboNucleotide3D.O3).getX();
        float cx = magnet.getAtom(RiboNucleotide3D.C4).getX() - magnet.getAtom(RiboNucleotide3D.O3).getX();
        float dx = magnet.getAtom(RiboNucleotide3D.O3).getX();

        float ay = magnet.getAtom(RiboNucleotide3D.C2).getY() - magnet.getAtom(RiboNucleotide3D.O3).getY();
        float by = magnet.getAtom(RiboNucleotide3D.C3).getY() - magnet.getAtom(RiboNucleotide3D.O3).getY();
        float cy = magnet.getAtom(RiboNucleotide3D.C4).getY() - magnet.getAtom(RiboNucleotide3D.O3).getY();
        float dy = magnet.getAtom(RiboNucleotide3D.O3).getY();

        float az = magnet.getAtom(RiboNucleotide3D.C2).getZ() - magnet.getAtom(RiboNucleotide3D.O3).getZ();
        float bz = magnet.getAtom(RiboNucleotide3D.C3).getZ() - magnet.getAtom(RiboNucleotide3D.O3).getZ();
        float cz = magnet.getAtom(RiboNucleotide3D.C4).getZ() - magnet.getAtom(RiboNucleotide3D.O3).getZ();
        float dz = magnet.getAtom(RiboNucleotide3D.O3).getZ();

        float px = ap * ax + bp * bx + cp * cx + dx;
        float py = ap * ay + bp * by + cp * cy + dy;
        float pz = ap * az + bp * bz + cp * cz + dz;

        float o1x = ao1 * ax + bo1 * bx + co1 * cx + dx;
        float o1y = ao1 * ay + bo1 * by + co1 * cy + dy;
        float o1z = ao1 * az + bo1 * bz + co1 * cz + dz;

        float o2x = ao2 * ax + bo2 * bx + co2 * cx + dx;
        float o2y = ao2 * ay + bo2 * by + co2 * cy + dy;
        float o2z = ao2 * az + bo2 * bz + co2 * cz + dz;

        float o5x = ao5 * ax + bo5 * bx + co5 * cx + dx;
        float o5y = ao5 * ay + bo5 * by + co5 * cy + dy;
        float o5z = ao5 * az + bo5 * bz + co5 * cz + dz;

        float ra[] = new float[]{px, py, pz};
        float rb[] = new float[]{o1x, o1y, o1z};
        float rc[] = new float[]{o2x, o2y, o2z};
        float rd[] = new float[]{o5x, o5y, o5z};

        float oa[] = null;
        for (String p :  RiboNucleotide3D.P)
            if (residueToTranslate.getAtom(p).hasCoordinatesFilled()) {
                oa = residueToTranslate.getAtom(p).getCoordinates();
                break;
            }
        float ob[] = residueToTranslate.getAtom(RiboNucleotide3D.O1P).getCoordinates();
        float oc[] = residueToTranslate.getAtom(RiboNucleotide3D.O2P).getCoordinates();
        float od[] = residueToTranslate.getAtom(RiboNucleotide3D.O5).getCoordinates();
        double[][] mr = {{ra[0], rb[0], rc[0], rd[0]}, {ra[1], rb[1], rc[1], rd[1]}, {ra[2], rb[2], rc[2], rd[2]}, {1, 1, 1, 1}};

        double[][] mo = {{oa[0], ob[0], oc[0], od[0]}, {oa[1], ob[1], oc[1], od[1]}, {oa[2], ob[2], oc[2], od[2]}, {1, 1, 1, 1}};

        Matrix r = new Matrix(mr);
        Matrix o = new Matrix(mo);
        return r.times(o.inverse());
    }

    public static String cleanPDB(Mediator mediator, String data) {
        StringWriter result = new StringWriter();
        try {
            TertiaryStructure ts = FileParser.parsePDB(mediator, new StringReader(data)).get(0);
            FileParser.writePDBFile(ts.getResidues3D(),true,result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String pdb2fgm(Mediator mediator, String pdbData) throws Exception {
        StringBuffer fgmData = new StringBuffer();
        //first step, the cleaning of the PDB, to be sure that no modified residues will not interfer with the conversion process
        pdbData = cleanPDB(mediator, pdbData);
        BufferedReader in = new BufferedReader( new StringReader(pdbData) );
        double chiAtomsCoords[][] = new double[4][3];
        String ch = "chi ";
        String[] atnam = {"P", "O1P", "O2P", "O5*", "C5*", "C4*", "O4*", "C3*", "C2*", "O2*", "C1*", "O3*", "N"};
        String[] outLine = new String[13];
        int atomSerialNumber = 0;
        String pdbLine = null;
        int currentRes = -1;
        while ((pdbLine = in.readLine()) != null ) {
            if (pdbLine.startsWith("TER") || pdbLine.startsWith("END"))
                break;
            if (currentRes != -1 && Integer.parseInt(pdbLine.substring(22, 27).trim()) != currentRes)  {
                for (int i = 0; i < 13; i++)
                    if (outLine[i] == null)
                        throw new Modeling3DException(Modeling3DException.MISSING_ATOMS_IN_MOTIFS);
                    else
                        fgmData.append(outLine[i]+"\n");
                double ator = TBMath.torsx(chiAtomsCoords[0], chiAtomsCoords[1], chiAtomsCoords[2], chiAtomsCoords[3]);
                fgmData.append(FGMFormat.getChiString(currentRes, ch, ator) + "\n");
                outLine = new String[13];
            }
            currentRes = Integer.parseInt(pdbLine.substring(22, 27).trim());

            char residueName = pdbLine.substring(17, 21).trim().charAt(0);
            String atomf = pdbLine.substring(12, 16).trim();

            double xxf[] = new double[3];
            xxf[0] = Double.parseDouble(pdbLine.substring(30, 38).trim());
            xxf[1] = Double.parseDouble(pdbLine.substring(38, 46).trim());
            xxf[2] = Double.parseDouble(pdbLine.substring(46, 54).trim());

            for (int k = 0; k < 12; k++) {
                if (atomf.equals(atnam[k])) {
                    atomSerialNumber++;
                    if (k == 6) {
                        for (int l = 0; l < 3; l++) {
                            chiAtomsCoords[0][l] = xxf[l];
                        }
                    }
                    if (k == 10) {
                        for (int l = 0; l < 3; l++) {
                            chiAtomsCoords[1][l] = xxf[l];
                        }
                    }
                    outLine[k] =  FGMFormat.getAtomString(atomSerialNumber, currentRes, atomf, xxf[0], xxf[1], xxf[2]);
                    break;
                }
            }

            if (residueName == 'P') {
                if (atomf.equals("C5")) {
                    atomSerialNumber++;
                    for (int i = 0; i < 3; i++)
                        chiAtomsCoords[2][i] = xxf[i];
                    outLine[12] = FGMFormat.getAtomString(atomSerialNumber, currentRes, "N", xxf[0], xxf[1], xxf[2]);
                }
                else if (atomf.equals("C6")) {
                    for (int i = 0; i < 3; i++)
                        chiAtomsCoords[3][i] = xxf[i];
                }
            }

            else if (residueName == 'G' || residueName == 'A' || residueName == 'I' || residueName == 'Y') {
                if (atomf.equals("N9")) {
                    atomSerialNumber++;
                    for (int i = 0; i < 3; i++) {
                        chiAtomsCoords[2][i] = xxf[i];
                    }
                    outLine[12] = FGMFormat.getAtomString(atomSerialNumber, currentRes, "N", xxf[0], xxf[1], xxf[2]);
                }
                else if (atomf.equals("C8")) {
                    for (int i = 0; i < 3; i++) {
                        chiAtomsCoords[3][i] = xxf[i];
                    }
                }
            }
            else if (residueName == 'C' || residueName == 'U' || residueName == 'T' || residueName == 'D') {
                if (atomf.equals("N1")) {
                    atomSerialNumber++;
                    for (int i = 0; i < 3; i++) {
                        chiAtomsCoords[2][i] = xxf[i];
                    }
                    outLine[12] = FGMFormat.getAtomString(atomSerialNumber, currentRes, "N", xxf[0], xxf[1], xxf[2]);
                }
                else if (atomf.equals("C6")) {
                    for (int i = 0; i < 3; i++) {
                        chiAtomsCoords[3][i] = xxf[i];
                    }
                }
            }


        }
        //the last
        for (int i = 0; i < 13; i++)
            if (outLine[i] == null)
                throw new Modeling3DException(Modeling3DException.MISSING_ATOMS_IN_MOTIFS);
            else
                fgmData.append(outLine[i]+"\n");
        double ator = TBMath.torsx(chiAtomsCoords[0], chiAtomsCoords[1], chiAtomsCoords[2], chiAtomsCoords[3]);
        fgmData.append(FGMFormat.getChiString(currentRes, ch, ator) + "\n");
        return fgmData.toString();
    }

    private static int psrot(double[] the) {
        double p144 = 144 * TBMath.DegreeToRadian;
        double a = 0;
        double b = 0;
        for (int i = 0; i < 5; i++) {
            a += the[i] * Math.cos(p144 * i);
            b = b - the[i] * Math.sin(p144 * i);
        }
        a *= 0.4;
        b *= 0.4;

        double val = Math.atan2(b, a) * TBMath.RadianToDegree;
        if (val < 0) {
            val += 360;

        }
        if (val > 90 && val < 270) {
            return 1;
        } else {
            return 0;
        }
    }

    private static void compar(double[][][] x, double[][][] xi, double[][] b, int natc, double[][] xbar) {
        double[][] sig2 = new double[2][9];
        int natom[] = new int[2];
        int iat[][] = new int[2][9];
        double[] w = new double[9];
        double[][][] xx = new double[2][9][3];
        double sx[][] = new double[2][3];
        double[][][] ainer = new double[2][3][3];
        double[][] r = new double[3][3];
        double[] chi2 = new double[11];
        double[] rb = new double[6];
        double[][] at = new double[3][3];
        double[] x0 = new double[3];
        double[][] tp = new double[4][3];
        double[] xabs = new double[3];
        double[] xpart = new double[3];
        double[][] bt = new double[3][3];
        double[][] rot = new double[3][3];
        double[][][] trans = new double[2][3][3];
        double[][][] ortho = new double[2][3][3];
        int nmol = 2;

        for (int imol = 0; imol < nmol; imol++) {
            natom[imol] = natc;
            for (int k = 0; k < 3; k++) {
                for (int l = 0; l < 3; l++) {
                    ortho[imol][l][k] = 0;
                }
                ortho[imol][k][k] = 1;
            }
            for (int i = 0; i < natc; i++) {
                sig2[imol][i] = 1;
            }
        }
        for (int k = 0; k < 3; k++) {
            for (int l = 0; l < 3; l++) {
                trans[0][k][l] = ortho[0][k][l];
                trans[1][k][l] = ortho[1][k][l];
            }
        }
        int ist1 = 0;
        int ist2 = 0;
        for (int j = 0; j < natc; j++) {
            iat[0][j] = ist1 + j;
            iat[1][j] = ist2 + j;
        }
        for (int j = 0; j < natc; j++) {
            int n1 = iat[0][j];
            int n2 = iat[1][j];
            for (int k = 0; k < 3; k++) {
                xx[1][j][k] = x[1][n2][k];
                xx[0][j][k] = x[0][n1][k];
            }

            w[j] = 1.0 / (sig2[0][n1] + sig2[1][n2]);
        }

        double sw = 0;
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 3; k++) {
                sx[i][k] = 0;
            }
        }
        for (int j = 0; j < natc; j++) {
            sw += w[j];
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 3; k++) {
                    sx[i][k] += w[j] * xx[i][j][k];
                }
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 3; k++) {
                xbar[i][k] = sx[i][k] / sw;
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < natc; j++) {
                for (int k = 0; k < 3; k++) {
                    xx[i][j][k] = xx[i][j][k] - xbar[i][k];
                }
                trcoor(xx, trans, j, i);
            }
        }

        for (int im = 0; im < 2; im++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    ainer[im][j][i] = 0;
                }
            }

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    for (int jat = 0; jat < natc; jat++) {
                        ainer[im][j][i] = ainer[im][j][i] - w[jat] * xx[im][jat][i] * xx[im][jat][j];
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                ainer[im][i][i] = 0;
            }
            for (int i = 0; i < 3; i++) {
                for (int jat = 0; jat < natc; jat++) {
                    ainer[im][i][i] += w[jat] * (xx[im][jat][0] * xx[im][jat][0] + xx[im][jat][1] * xx[im][jat][1] + xx[im][jat][2] * xx[im][jat][2] - xx[im][jat][i] * xx[im][jat][i]);
                }
            }
            int k = 0;

            for (int j = 0; j < 3; j++) {
                for (int i = 0; i <= j; i++) {
                    rb[k] = ainer[im][j][i];
                    k++;
                }
            }
            /*
            for (int i = 0; i < rb.length; i++) {
                System.out.println("rb[" + i + "]=" + rb[i]);
            }*/

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    at[j][i] = 0;
                }
            }
            eigen(rb, at, 3, 0);


            double d = at[0][0] * (at[1][1] * at[2][2] - at[2][1] * at[1][2]) + at[1][0] * (at[2][1] * at[0][2] - at[0][1] * at[2][2]) + at[2][0] * (at[0][1] * at[1][2] - at[1][1] * at[0][2]);

            if (d < 0) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        at[i][j] = -at[i][j];
                    }
                }
            }
            for (k = 0; k < 3; k++) {
                for (int l = 0; l < 3; l++) {
                    rot[k][l] = at[l][k];
                }
            }
            matmul(rot, trans, im);

            for (int jat = 0; jat < natc; jat++) {
                for (k = 0; k < 3; k++) {
                    xi[im][jat][k] = 0;
                    for (int j = 0; j < 3; j++) {
                        if (im == 0) {
                            bt[k][j] = at[k][j];
                        }
                        xi[im][jat][k] += at[k][j] * xx[im][jat][j];
                    }
                }
            }
        }

        chi2[0] = 0;
        for (int im = 0; im < 2; im++) {
            for (int jat = 0; jat < natc; jat++) {
                for (int j = 0; j < 3; j++) {
                    xx[im][jat][j] = xi[im][jat][j];
                }
            }
        }

        for (int k = 0; k < 3; k++) {
            for (int l = 0; l < 3; l++) {
                rot[l][k] = 0;
            }
        }

        for (int i = 1; i < 5; i++) {
            for (int iperm = 0; iperm < 3; iperm++) {
                for (int im = 0; im < 2; im++) {
                    for (int jat = 0; jat < natc; jat++) {
                        for (int j = 0; j < 3; j++) {
                            int jp = j + 1;
                            if (jp > 2) {
                                jp = jp - 3;
                            }
                            xi[im][jat][jp] = xx[im][jat][j];
                            rot[j][jp] = 1;
                        }
                    }
                }

                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        b[k][j] = 0;
                        for (int jat = 0; jat < natc; jat++) {
                            b[k][j] += w[jat] * xi[0][jat][j] * xi[1][jat][k];
                        }
                    }
                }

                for (int l = 1; l < 3; l++) {
                    if (b[l][l] < 0) {
                        for (int jat = 0; jat < natc; jat++) {
                            xi[1][jat][l] = -xi[1][jat][l];
                            xi[1][jat][0] = -xi[1][jat][0];
                        }
                        for (int j = 0; j < 3; j++) {
                            rot[j][0] = -rot[j][0];
                            rot[j][l] = -rot[j][l];
                            b[0][j] = -b[0][j];
                            b[l][j] = -b[l][j];
                        }
                    }
                }
                chi2[i] = 0;
                for (int jat = 0; jat < natc; jat++) {
                    for (int j = 0; j < 3; j++) {
                        chi2[i] += w[jat] * (xi[1][jat][j] - xi[0][jat][j]) * (xi[1][jat][j] - xi[0][jat][j]);
                    }
                }
                //System.out.println("chi2[" + i + "]=" + chi2[i]);
                double cu = 1, cv = 1, cw = 1, su = 0, sv = 0;
                sw = 0;
                double u = Math.atan((-cv * sw * b[0][0] + cw * b[1][0] - cv * cw * b[0][1] - sw * b[1][1] + sv * b[0][2]) / (cw * b[0][0] + cv * sw * b[1][0] - sw * b[0][1] + cv * cw * b[1][1] - sv * b[1][2]));
                su = Math.sin(u);
                cu = Math.cos(u);
                double v = Math.atan((sw * b[2][0] + cw * b[2][1] + su * b[0][2] - cu * b[1][2]) / (-su * sw * b[0][0] + cu * sw * b[1][0] - su * cw * b[0][1] + cu * cw * b[1][1] + b[2][2]));
                sv = Math.sin(v);
                cv = Math.cos(v);
                double ww = Math.atan((-su * cv * b[0][0] + cu * cv * b[1][0] + sv * b[2][0] - cu * b[0][1] - su * b[1][1]) / (cu * b[0][0] + su * b[1][0] - su * cv * b[0][1] + cu * cv * b[1][1] + sv * b[2][1]));
                sw = Math.sin(ww);
                cw = Math.cos(ww);

                r[0][0] = cu * cw - su * cv * sw;
                r[1][0] = su * cw + cu * cv * sw;
                r[2][0] = sv * sw;
                r[0][1] = -cu * sw - su * cv * cw;
                r[1][1] = -su * sw + cu * cv * cw;
                r[2][1] = sv * cw;
                r[0][2] = su * sv;
                r[1][2] = -cu * sv;
                r[2][2] = cv;

                for (int jat = 0; jat < natc; jat++) {
                    for (int j = 0; j < 3; j++) {
                        xx[1][jat][j] = 0;
                        for (int k = 0; k < 3; k++) {
                            xx[1][jat][j] += r[k][j] * xi[1][jat][k];
                        }
                    }
                    for (int j = 0; j < 3; j++) {
                        xx[0][jat][j] = xi[0][jat][j];
                    }
                }
                matmul(rot, trans, 1);
                matmul(r, trans, 1);
            }
            if (Math.abs(chi2[i] - chi2[i - 1]) < 0.01 * chi2[i]) {
                break;
            }
        }
        double d;
        int nu = 3 * natc - 6;
        for (int jat = 0; jat < natc; jat++) {
            double d2 = 0;
            for (int j = 0; j < 3; j++) {
                d2 += (xx[1][jat][j] - xx[0][jat][j]) * (xx[1][jat][j] - xx[0][jat][j]);
            }
            d = Math.sqrt(d2);
            //System.out.println("d=" + d);
        }

        for (int imol = 0; imol < 2; imol++) {
            for (int jat = 0; jat < natc; jat++) {
                for (int i = 0; i < 3; i++) {
                    xabs[i] = bt[0][i] * xx[imol][jat][0] + bt[1][i] * xx[imol][jat][1] + bt[2][i] * xx[imol][jat][2] + xbar[0][i];
                }
                for (int i = 0; i < 3; i++) {
                    xpart[i] = tp[0][i] * xabs[0] + tp[1][i] * xabs[1] + tp[2][i] * xabs[2] + tp[3][i];
                }
            }
        }


        for (int k = 0; k < 3; k++) {
            for (int l = 0; l < 3; l++) {
                rot[l][k] = trans[0][l][k];
            }
        }
        d = minv(rot, 3);
        matmul(rot, trans, 1);
        for (int k = 0; k < 3; k++) {
            b[0][k] = trans[1][0][k];
            b[1][k] = trans[1][1][k];
            b[2][k] = trans[1][2][k];
        }
        int in = 0;
        int i = 0;
        int ik = 0;
        boolean go = false;

        ik++;
        i = iat[1][ik];
        if (ik <= natc) {
            go = true;
        }

        if (go) {
            in += 1;
            for (int k = 0; k < 3; k++) {
                xi[1][i][k] = x[1][i][k] - xbar[1][k];
            }
            for (int k = 0; k < 3; k++) {
                x0[k] = xi[1][i][k];
            }
            for (int k = 0; k < 3; k++) {
                xi[1][i][k] = trans[1][0][k] + trans[1][1][k] + trans[1][2][k];
            }
            for (int k = 0; k < 3; k++) {
                xi[1][i][k] += xbar[0][k];
            }
        }
    }

    private static void eigen(double[] a, double[][] rout, int n, int mv) {
        //System.out.println("n = " + n + " mv = " + mv);
        double r[] = new double[9];

        double range = 1.0E-6;
        if (mv != 1) {
            int iq = -n;
            for (int j = 1; j <= n; j++) {
                iq += n;
                for (int i = 1; i <= n; i++) {
                    int ij = iq + i;
                    if (i == j) {
                        r[ij - 1] = 1;
                    } else {
                        r[ij - 1] = 0;
                    }
                }
            }
        }
        double anorm = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = i; j <= n; j++) {
                if (i != j) {
                    int ia = i + (j * (j - 1)) / 2;
                    anorm += a[ia - 1] * a[ia - 1];
                }
            }
        }
        if (anorm != 0) {
            anorm = 1.414 * Math.sqrt(anorm);
            double anrmx = anorm * range / n;
            int ind = 0;
            double thr = anorm;
            int mygoto = 45;
            int l = 0, m = 0, mq, lq, lm;

            //System.out.println("thr = " + thr + " anrmx=" + anrmx);

            while (mygoto > 0) {
                //      Printing.debug("goto "+mygoto + " n = " + n);
                if (mygoto < 46) {
                    thr = thr / n;
                }
                if (mygoto < 51) {
                    l = 1;
                }
                if (mygoto < 56) {

                    m = l + 1;
                }
                mq = (m * (m - 1)) / 2;
                lq = (l * (l - 1)) / 2;
                lm = l + mq;
                //    Printing.debug("l "+l+" m "+m+" lq "+lq+" mq "+mq+" lm"+lm);
                if (Math.abs(a[lm - 1]) >= thr) {
                    //        Printing.debug("a[lm-1] : a["+(lm-1)+"] : "+a[lm-1]+" >= thr : "+thr);
                    ind = 1;
                    int ll = l + lq;
                    int mm = m + mq;
                    double x = 0.5 * (a[ll - 1] - a[mm - 1]);
                    double y = -a[lm - 1] / Math.sqrt(a[lm - 1] * a[lm - 1] + x * x);
                    if (x < 0) {
                        y = -y;
                    }
                    double sinx = y / Math.sqrt(2 * (1 + Math.sqrt(1 - y * y)));
                    double sinx2 = sinx * sinx;
                    double cosx = Math.sqrt(1 - sinx2);
                    double cosx2 = cosx * cosx;
                    double sincs = sinx * cosx;
                    int ilq = n * (l - 1);
                    int imq = n * (m - 1);
                    for (int i = 1; i <= n; i++) {
                        int iq = (i * (i - 1)) / 2;
                        if (i != l) {
                            if (i != m) {
                                int im, il;
                                if (i < m) {
                                    im = i + mq;
                                } else {
                                    im = m + iq;
                                }
                                if (i < l) {
                                    il = i + lq;
                                } else {
                                    il = l + iq;
                                }
                                x = a[il - 1] * cosx - a[im - 1] * sinx;
                                a[im - 1] = a[il - 1] * sinx + a[im - 1] * cosx;
                                a[il - 1] = x;
                            }
                        }
                        if (mv != 1) {
                            int ilr = ilq + i;
                            int imr = imq + i;
                            x = r[ilr - 1] * cosx - r[imr - 1] * sinx;
                            r[imr - 1] = r[ilr - 1] * sinx + r[imr - 1] * cosx;
                            r[ilr - 1] = x;
                        }
                    }

                    x = 2 * a[lm - 1] * sincs;
                    y = a[ll - 1] * cosx2 + a[mm - 1] * sinx2 - x;
                    x = a[ll - 1] * sinx2 + a[mm - 1] * cosx2 + x;
                    a[lm - 1] = (a[ll - 1] - a[mm - 1]) * sincs + a[lm - 1] * (cosx2 - sinx2);
                    a[ll - 1] = y;
                    a[mm - 1] = x;
                }

                mygoto = -1;

                if (m != n) {
                    m++;
                    mygoto = 60;
                    continue;
                }
                if (l != n - 1) {
                    l++;
                    mygoto = 55;
                    continue;
                }
                if (ind == 1) {
                    ind = 0;
                    mygoto = 50;
                    continue;
                }
                if (thr > anrmx) {
                    //          Printing.debug("thr("+thr+") > anrmx("+anrmx+")");
                    mygoto = 45;
                    continue;
                }
            }
        }
        int iq = -n;
        for (int i = 1; i <= n; i++) {
            iq += n;
            int ll = i + (i * (i - 1)) / 2;
            int jq = n * (i - 2);
            for (int j = i; j <= n; j++) {
                jq += n;
                int mm = j + (j * (j - 1)) / 2;
                if (a[ll - 1] < a[mm - 1]) {
                    double x = a[ll - 1];
                    a[ll - 1] = a[mm - 1];
                    a[mm - 1] = x;
                    if (mv != 1) {
                        for (int k = 1; k <= n; k++) {
                            int ilr = iq + k;
                            int imr = jq + k;
                            x = r[ilr - 1];
                            r[ilr - 1] = r[imr - 1];
                            r[imr - 1] = x;
                        }
                    }
                }
            }
        }
        TBMath.singleToDouble(r, rout);
    }

    private static void matmul(double[][] a, double[][][] trans, int imol) {
        double[][] transo = new double[3][3];
        for (int k = 0; k < 3; k++) {
            for (int l = 0; l < 3; l++) {
                transo[l][k] = trans[imol][l][k];
            }
        }
        for (int k = 0; k < 3; k++) {
            for (int l = 0; l < 3; l++) {
                trans[imol][l][k] = a[0][k] * transo[l][0] + a[1][k] * transo[l][1] + a[2][k] * transo[l][2];
            }
        }
    }

    private static void chirot(double[][] xx, int ir3, int ir4, double chiang, int nf) throws Exception {
        /*System.out.println("chi = " + chiang);
        for (int i = 0; i < xx.length; i++){
            System.out.println(i + " : (\t" + xx[i][0] + "\t" + xx[i][1] + "\t" + xx[i][2] + "\t)");
        }*/
        double[][] b = new double[3][3];
        double[] u = new double[3];
        double basis[][] = new double[3][3];
        double[] u1 = new double[3];
        double[] u2 = new double[3];
        double[] u3 = new double[3];
        double[] v = new double[3];

        int ir1 = 0;
        int ir2 = 1;

        double ator = TBMath.torsx(xx[ir1], xx[ir2], xx[ir3], xx[ir4]);
        double akappa = chiang - ator;
        /*
        System.out.println("ator = "+ator);
        System.out.println("Chiang = "+chiang);
        System.out.println(ir1 + " " + ir2 + " " + ir3 + " " + ir4 + " " + nf);
        System.out.println("kappa = " + akappa);*/

        for (int k = 0; k < 3; k++) {
            basis[0][k] = xx[ir3][k] - xx[ir2][k];
            basis[1][k] = xx[ir1][k] - xx[ir2][k];
            u1[k] = xx[ir1][k];
            u2[k] = xx[ir2][k];
            u3[k] = xx[ir3][k];
        }

        try {
            if (TBMath.norm(basis[0]) == 0) {
                throw new Exception("(basis[0]) Two of the given atoms have identical coordinates !");
            }
            TBMath.normalize(basis[0]);
            basis[2] = TBMath.crossProduct(basis[0], basis[1]);

            if (TBMath.norm(basis[2]) == 0) {
                throw new Exception("(basis[2]) Two of the given atoms have identical coordinates !");
            }
        }
        catch (Exception e) {
            System.exit(1);
        }
        TBMath.normalize(basis[2]);
        basis[1] = TBMath.crossProduct(basis[2], basis[0]);

        for (int k = ir1; k <= nf; k++) {
            for (int j = 0; j < 3; j++) {
                xx[k][j] = xx[k][j] - u3[j];
            }
            for (int i = 0; i < 3; i++) {
                u[i] = 0;
                for (int j = 0; j < 3; j++) {
                    u[i] += basis[i][j] * xx[k][j];
                }
            }
            for (int i = 0; i < 3; i++) {
                xx[k][i] = u[i];
            }
        }
        akappa = akappa * TBMath.DegreeToRadian;
        double cosk = Math.cos(akappa);
        double sink = Math.sin(akappa);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                b[i][j] = 0;
            }
        }
        b[0][0] = 1;
        b[1][1] = cosk;
        b[1][2] = -sink;
        b[2][1] = sink;
        b[2][2] = cosk;
        for (int k = ir4; k <= nf; k++) {
            for (int i = 0; i < 3; i++) {
                u[i] = 0;
                for (int j = 0; j < 3; j++) {
                    u[i] += b[i][j] * xx[k][j];
                }
            }
            for (int i = 0; i < 3; i++) {
                xx[k][i] = u[i];
            }
        }

        for (int k = ir1; k <= nf; k++) {
            for (int i = 0; i < 3; i++) {
                u[i] = 0;
                for (int j = 0; j < 3; j++) {
                    u[i] += basis[j][i] * xx[k][j];
                }
            }
            for (int i = 0; i < 3; i++) {
                xx[k][i] = u[i] + u3[i];
            }
        }
    }

    private static double minv(double[][] ain, int n) {
        double[] a = new double[ain.length * ain.length];
        int[] l = new int[9];
        int[] m = new int[9];
        double d = 1;
        int nk = -n;

        TBMath.DoubleToSingle(ain, a);

        /*
        for (int i = 0; i < a.length; i++) {
            System.out.println("a[" + i + "]=" + a[i]);
        }*/

        for (int k = 1; k <= n; k++) {
            nk += n;
            l[k - 1] = k;
            m[k - 1] = k;
            int kk = nk + k;
            double biga = a[kk - 1];
            //System.out.println("biga " + biga);
            for (int j = k; j <= n; j++) {
                int iz = n * (j - 1);
                for (int i = k; i <= n; i++) {
                    int ij = iz + i;
                    if (Math.abs(biga) < Math.abs(a[ij - 1])) {
                        biga = a[ij - 1];
                        l[k - 1] = i;
                        m[k - 1] = j;
                    }
                }
            }
            //System.out.println("biga2 " + biga);
            int j = l[k - 1];
            if (j > k) {
                int ki = k - n;
                for (int i = 1; i <= n; i++) {
                    ki += n;
                    double hold = -a[ki - 1];
                    int ji = ki - k + j;
                    a[ki - 1] = a[ji - 1];
                    a[ji - 1] = hold;
                }
            }
            int i = m[k - 1];
            if (i > k) {
                int jp = n * (i - 1);
                for (j = 1; j <= n; j++) {
                    int jk = nk + j;
                    int ji = jp + j;
                    double hold = -a[jk - 1];
                    a[jk - 1] = a[ji - 1];
                    a[ji - 1] = hold;
                }
            }
            if (biga == 0) {
                TBMath.singleToDouble(a, ain);
                return 0;
            }
            for (i = 1; i <= n; i++) {
                if (i != k) {
                    int ik = nk + i;
                    a[ik - 1] = a[ik - 1] / -biga;
                }
            }
            for (i = 1; i <= n; i++) {
                int ik = nk + i;
                double hold = a[ik - 1];
                int ij = i - n;
                for (j = 1; j <= n; j++) {
                    ij += n;
                    if (i != k) {
                        if (j != k) {
                            int kj = ij - i + k;
                            a[ij - 1] += hold * a[kj - 1];
                        }
                    }
                }
            }
            int kj = k - n;
            for (j = 1; j <= n; j++) {
                kj += n;
                if (j != k) {
                    a[kj - 1] = a[kj - 1] / biga;
                }
            }
            d *= biga;
            a[kk - 1] = 1 / biga;
        }
        for (int k = n; k > 0; k--) {
            int i = l[k - 1];
            if (i > k) {
                int jq = n * (k - 1);
                int jr = n * (i - 1);
                for (int j = 1; j <= n; j++) {
                    int jk = jq + j;
                    double hold = a[jk - 1];
                    int ji = jr + j;
                    a[jk - 1] = -a[ji - 1];
                    a[ji - 1] = hold;
                }
            }
            int j = m[k - 1];
            if (j < k) {
                continue;
            }
            int ki = k - n;
            for (i = 1; i <= n; i++) {
                ki += n;
                double hold = a[ki - 1];
                int ji = ki - k + j;
                a[ki - 1] = -a[ji - 1];
                a[ji - 1] = hold;
            }
        }
        TBMath.singleToDouble(a, ain);
        return d;
    }

    private static void trcoor(double[][][] x, double[][][] trans, int iat, int imol) {
        double x0[] = new double[3];
        for (int k = 0; k < 3; k++) {
            x0[k] = x[imol][iat][k];
        }

        for (int k = 0; k < 3; k++) {
            x[imol][iat][k] = trans[imol][0][k] * x0[0] + trans[imol][1][k] * x0[1] + trans[imol][2][k] * x0[2];
        }
    }

    private static int pseudo(double[][][] x, int inc) {
        double[] x1 = new double[3];
        double[] x2 = new double[3];
        double[] x3 = new double[3];
        double[] x4 = new double[3];
        double[] x5 = new double[3];

        for (int i = 0; i < 3; i++) {
            x1[i] = x[0][1][i];
            x2[i] = x[0][3][i];
            x3[i] = x[0][6 - inc][i];
            x4[i] = x[0][5 - inc][i];
            x5[i] = x[0][2][i];
        }

        /*
        for (int i = 0; i < 3; i++) {
            System.out.println("x1 " + x1[i] + " x2 " + x2[i] + " x3 " + x3[i] + " x4 " + x4[i] + " x5 " + x5[i]);
        }*/

        double the[] = new double[5];
        the[3] = TBMath.torsx(x1, x2, x3, x4);
        the[4] = TBMath.torsx(x2, x3, x4, x5);
        the[0] = TBMath.torsx(x3, x4, x5, x1);
        the[1] = TBMath.torsx(x4, x5, x1, x2);
        the[2] = TBMath.torsx(x5, x1, x2, x3);

        return psrot(the);
    }

}
