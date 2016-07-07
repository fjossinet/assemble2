package fr.unistra.ibmc.assemble2.model;


import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import org.bson.types.ObjectId;
import org.jdesktop.swingworker.SwingWorker;

public class StructuralAlignment {

    private SequenceMeter sequenceMeter;
    private ConsensusStructure consensusStructure;
    private ReferenceStructure referenceStructure;
    private List<AlignedMolecule> alignedMolecules;
    private Mediator mediator;
    private String id;
    private String name = "Structural Alignment";
    private Map<String, List<Molecule>> clusters;

    public StructuralAlignment(final Mediator mediator, final String consensusStructure, final AlignedMolecule referenceMolecule, final ReferenceStructure referenceStructure, final List<AlignedMolecule> sortedMolecules) {
        this.id = new ObjectId().toString();
        this.mediator = mediator;
        this.clusters = new HashMap<String, List<Molecule>>();
        this.mediator.getMoleculesList().clearList();
        this.sequenceMeter = new SequenceMeter(mediator, consensusStructure.length());
        this.alignedMolecules = new ArrayList<AlignedMolecule>(sortedMolecules.size());
        this.alignedMolecules.add(referenceMolecule);
        mediator.getMoleculesList().addRow(referenceMolecule.getMolecule());
        mediator.getMoleculesList().revalidate();
        this.consensusStructure = new ConsensusStructure(mediator, consensusStructure);
        this.referenceStructure = referenceStructure;
        /*Collections.sort(sortedMolecules, new Comparator<AlignedMolecule>() {
            @Override
            public int compare(AlignedMolecule alignedMolecule, AlignedMolecule alignedMolecule2) {
                if (alignedMolecule.getMolecule().getOrganism() != null && alignedMolecule2.getMolecule().getOrganism() != null)
                    return alignedMolecule.getMolecule().getOrganism().compareTo(alignedMolecule2.getMolecule().getOrganism());
                else
                    return  -alignedMolecule.getMolecule().getName().compareTo(alignedMolecule2.getMolecule().getName());
            }
        });*/
        new SwingWorker() {
            protected Object doInBackground() {
                try {
                    for (AlignedMolecule m: sortedMolecules) {
                        StructuralAlignment.this.alignedMolecules.add(m);
                        mediator.getMoleculesList().addRow(m.getMolecule());
                        mediator.getAlignmentCanvas().repaint();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public StructuralAlignment(final Mediator mediator, final AlignedMolecule referenceMolecule, final ReferenceStructure referenceStructure, final List<AlignedMolecule> sortedMolecules) {
        this(mediator, referenceStructure.getSecondaryStructure().printAsBracketNotation(), referenceMolecule, referenceStructure, sortedMolecules);
    }

    public boolean hasAnyGap(int column) {
        for (AlignedMolecule m:this.alignedMolecules)
            if (m.getSymbol(column).isGap())
                return true;
        return false;
    }

    public String getCluster(Molecule m) {
        for (Map.Entry<String, List<Molecule>> e:clusters.entrySet())
            if (e.getValue().contains(m))
                return e.getKey();
        return null;
    }

    public void addToCluster(Molecule m, String cluster) {
        if (!this.clusters.containsKey(cluster))
            this.clusters.put(cluster,new ArrayList<Molecule>());
        this.clusters.get(cluster).add(m);
    }

    public void removeFromCluster(Molecule m) {
        for (Map.Entry<String, List<Molecule>> e:clusters.entrySet())
            if (e.getValue().contains(m)) {
                e.getValue().remove(m);
            }
    }

    public boolean clusterEmpty(String cluster) {
        return this.clusters.get(cluster).isEmpty();
    }

    public List<Molecule> getMolecules() {
        List<Molecule> molecules = new ArrayList<Molecule>();
        for (AlignedMolecule seq:this.alignedMolecules)
            molecules.add(seq.getMolecule());
        return molecules;
    }

    /**
     * Modify the reference molecule and structure in the current alignment.
     * The molecule should be annotated by the structure given as argument (not checked by this method)
     * @param referenceMolecule
     * @param referenceSecondaryStructure
     */
    public void setReferenceMolecule(AlignedMolecule referenceMolecule, SecondaryStructure referenceSecondaryStructure) {
        this.alignedMolecules.remove(referenceMolecule);
        this.alignedMolecules.add(0,referenceMolecule);
        this.referenceStructure = new ReferenceStructure(mediator, referenceMolecule, referenceSecondaryStructure);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SequenceMeter getSequenceMeter() {
        return this.sequenceMeter;
    }

    public ReferenceStructure getReferenceStructure() {
        return this.referenceStructure;
    }

    public AlignedMolecule getBiologicalSequenceAt(final int index) {
        return this.alignedMolecules.get(index);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Create and return a new SecondaryStructure object for the biological sequence given as argument.
     * @param targetSequence
     */
    public SecondaryStructure deriveReferenceStructure(AlignedMolecule targetSequence) {
        SecondaryStructure ss = new SecondaryStructure(mediator, targetSequence.getMolecule(),"Secondary Structure");
        Helix newHelix = null;
        int positionInTargetSequence = -1, previousPositionInTargetSequence = -1, pairedPositionInTargetSequence = -1, previousPairedPositionInTargetSequence = -1;
        char previousEdge1 = '?', previousEdge2= '?', previousOrientation= '?';
        for (int i=0 ; i < this.referenceStructure.size() ; i++) {
            ReferenceStructureSymbol referenceStructureSymbol = (ReferenceStructureSymbol)this.referenceStructure.getSymbol(i);
            positionInTargetSequence = targetSequence.getSymbol(i).getPositionInSequence();
            for (BaseBaseInteraction bbi: referenceStructureSymbol.getReferenceBaseBaseInteractions()) {
                ReferenceStructureSymbol pairedReferenceStructureSymbol = referenceStructureSymbol.getPairedSymbol(bbi);
                pairedPositionInTargetSequence = targetSequence.getSymbol(this.referenceStructure.getIndex(pairedReferenceStructureSymbol)).getPositionInSequence();
                if (positionInTargetSequence < pairedPositionInTargetSequence /*to avoid to do twice the same stuff*/) {
                    if (bbi.isSecondaryInteraction()) {
                        //System.out.println(positionInTargetSequence+" "+pairedPositionInTargetSequence);
                        if (previousPositionInTargetSequence == positionInTargetSequence-1 && previousPairedPositionInTargetSequence == pairedPositionInTargetSequence+1) {
                            if (newHelix != null) {//we add the new interaction to the currentHelix
                                //System.out.println("Extend helix");
                                newHelix.addSecondaryInteraction(new Location(new Location(positionInTargetSequence), new Location(pairedPositionInTargetSequence)), bbi.getOrientation(), bbi.getEdge(bbi.getResidue()), bbi.getEdge(bbi.getPartnerResidue()));
                            }
                            else {//we create a new one with the same name than the original one
                                newHelix = ss.addHelix(new Location(new Location(previousPositionInTargetSequence,positionInTargetSequence), new Location(pairedPositionInTargetSequence,previousPairedPositionInTargetSequence)), bbi.getResidue().getStructuralDomain().getName());
                                newHelix.addSecondaryInteraction(new Location(new Location(previousPositionInTargetSequence), new Location(previousPairedPositionInTargetSequence)),previousOrientation, previousEdge1, previousEdge2 );
                                newHelix.addSecondaryInteraction(new Location(new Location(positionInTargetSequence), new Location(pairedPositionInTargetSequence)), bbi.getOrientation(), bbi.getEdge(bbi.getResidue()), bbi.getEdge(bbi.getPartnerResidue()));
                                //System.out.println(newHelix);
                            }
                        }
                        else {
                            if (newHelix == null && previousPositionInTargetSequence != -1 && previousPairedPositionInTargetSequence != -1)
                                // this means that there was a previous secondary interaction that was not contiguous with the previous one (since newHelix == null)
                                //and since we're here, the current secondary interaction is also not contiguous, so we have a secondary interaction that become a tertiary one (if they're no gaps at the corresponding positions in the target sequence)
                                ss.addTertiaryInteraction(new Location(new Location(previousPositionInTargetSequence), new Location(previousPairedPositionInTargetSequence)), previousOrientation, previousEdge1, previousEdge2);
                            newHelix = null;
                        }
                        previousEdge1 = bbi.getEdge(bbi.getResidue());
                        previousEdge2= bbi.getEdge(bbi.getPartnerResidue());
                        previousOrientation = bbi.getOrientation();
                        previousPositionInTargetSequence = positionInTargetSequence;
                        previousPairedPositionInTargetSequence = pairedPositionInTargetSequence;
                    }
                    else if (positionInTargetSequence != -1 && pairedPositionInTargetSequence != -1)//it is a tertiary interaction as in the reference sequence (if they're no gaps at the corresponding positions in the target sequence)
                        ss.addTertiaryInteraction(new Location(new Location(positionInTargetSequence), new Location(pairedPositionInTargetSequence)), bbi.getOrientation(), bbi.getEdge(bbi.getResidue()), bbi.getEdge(bbi.getPartnerResidue()));
                }
            }
        }
        //now with the helices just constructed, we deduce the locations of the single-strands
        Location singleStrandsLocation = new Location(1,targetSequence.getMolecule().size());
        for (Helix h:ss.getHelices())
            singleStrandsLocation = singleStrandsLocation.differenceOf(h.getLocation());
        int i=0;
        int[] boundaries = singleStrandsLocation.getEnds();
        for (int j=0;j<boundaries.length-1;j+=2)
            ss.addSingleStrand(new Location(boundaries[j], boundaries[j + 1]), "SS" + (i++));
        return ss;
    }

    /**
     * Return the number of LetterSequence objects in this RnalignAlignment object
     *
     * @return
     */
    public int getSymbolSequenceCount() {
        return 3 + this.getBiologicalSequenceCount();
    }

    /**
     * Return the number of aligned symbols
     *
     * @return
     */
    public int getBiologicalSequenceCount() {
        return this.alignedMolecules.size();
    }

    /**
     * Return the alignment's length
     *
     * @return
     */
    public int getLength() {
        return this.getSequenceMeter().size();
    }

    public void addBiologicalSequence(final AlignedMolecule seq) {
        int more = 0;
        SymbolSequence sequence = null;
        for (int j = 0; j < this.getSymbolSequenceCount(); j++) {
            sequence = this.getSymbolSequenceAt(j);
            more = seq.size() - sequence.size();
            while (more != 0) {
                if (more > 0) {
                    sequence.increaseSize();
                    more--;
                } else {
                    seq.increaseSize();
                    more++;
                }
            }
        }
        this.alignedMolecules.add(this.alignedMolecules.size(),seq);
    }

    public SymbolSequence getSymbolSequenceAt(final int index) {
        switch (index) {
            case 0:
                return this.sequenceMeter;
            case 1:
                return this.consensusStructure;
            case 2:
                return this.referenceStructure;
            default :
                return this.alignedMolecules.get(index - 3);
        }
    }

    public List<SymbolSequence> getAllSymbolSequences() {
        List<SymbolSequence> symbolSequences = new ArrayList<SymbolSequence>();
        symbolSequences.add(this.sequenceMeter);
        symbolSequences.add(this.consensusStructure);
        symbolSequences.add(this.referenceStructure);
        for (AlignedMolecule m:this.alignedMolecules)
            symbolSequences.add(m);
        return symbolSequences;
    }

    public int getMaxLabelSize() {
        return 1+Integer.toString(this.alignedMolecules.size()).length()+1; /* the first 1 constant is for the S starting each sequence's label, the second 1 is to have a space between the label and the sequence itself*/
    }

    public ConsensusStructure getConsensusStructure() {
        return this.consensusStructure;
    }

    public AlignedMolecule getBiologicalReferenceSequence() {
        return this.alignedMolecules.get(0);
    }

    private char[] getAlignedSequence(String alignedSequence) {
        alignedSequence.trim();
        alignedSequence = alignedSequence.replace('.', '-');
        alignedSequence = alignedSequence.toUpperCase();
        final StringBuffer alSequence = new StringBuffer();
        for (int i = 0; i < alignedSequence.length(); i++) {
            char base = alignedSequence.charAt(i);
            if (Character.isWhitespace(base)) base = '-';
            if (base >= 'A' && base <= 'z' || base == '-')
                alSequence.append(base);
        }
        return alSequence.toString().toCharArray();
    }

    public double calculateMutualInformation(int pos1InAlignment, int pos2InAlignment) {
        AlignedMolecule seq = null;
        double A1 = 1, A2 = 1, U1 = 1, U2 = 1, G1 = 1, G2 = 1, C1 = 1, C2 = 1, gap1 = 1, gap2 = 1,
                AA = 1, AU = 1, AG = 1, AC = 1,
                UA = 1, UU = 1, UG = 1, UC = 1,
                GA = 1, GU = 1, GG = 1, GC = 1,
                CA = 1, CU = 1, CG = 1, CC = 1,
                gA = 1, gU = 1, gG = 1, gC = 1,
                Ag = 1, Ug = 1, Gg = 1, Cg = 1,
                gg = 1;
        for (int k = 0; k < this.getBiologicalSequenceCount(); k++) {
            seq = this.getBiologicalSequenceAt(k);
            switch (seq.getSymbol(pos1InAlignment).getSymbol()) {
                case 'A' :
                    switch (seq.getSymbol(pos2InAlignment).getSymbol()) {
                        case 'A' :
                            A1++;
                            A2++;
                            AA++;
                            break;
                        case 'U' :
                            A1++;
                            U2++;
                            AU++;
                            break;
                        case 'G' :
                            A1++;
                            G2++;
                            AG++;
                            break;
                        case 'C' :
                            A1++;
                            C2++;
                            AC++;
                            break;
                        case '-' :
                            A1++;
                            gap2++;
                            Ag++;
                            break;
                    }
                    ;
                    break;
                case 'U' :
                    switch (seq.getSymbol(pos2InAlignment).getSymbol()) {
                        case 'A' :
                            U1++;
                            A2++;
                            UA++;
                            break;
                        case 'U' :
                            U1++;
                            U2++;
                            UU++;
                            break;
                        case 'G' :
                            U1++;
                            G2++;
                            UG++;
                            break;
                        case 'C' :
                            U1++;
                            C2++;
                            UC++;
                            break;
                        case '-' :
                            U1++;
                            gap2++;
                            Ug++;
                            break;
                    }
                    ;
                    break;
                case 'G' :
                    switch (seq.getSymbol(pos2InAlignment).getSymbol()) {
                        case 'A' :
                            G1++;
                            A2++;
                            GA++;
                            break;
                        case 'U' :
                            G1++;
                            U2++;
                            GU++;
                            break;
                        case 'G' :
                            G1++;
                            G2++;
                            GG++;
                            break;
                        case 'C' :
                            G1++;
                            C2++;
                            GC++;
                            break;
                        case '-' :
                            G1++;
                            gap2++;
                            Gg++;
                            break;
                    }
                    ;
                    break;
                case 'C' :
                    switch (seq.getSymbol(pos2InAlignment).getSymbol()) {
                        case 'A' :
                            C1++;
                            A2++;
                            CA++;
                            ;
                            break;
                        case 'U' :
                            C1++;
                            U2++;
                            CU++;
                            break;
                        case 'G' :
                            C1++;
                            G2++;
                            CG++;
                            break;
                        case 'C' :
                            C1++;
                            C2++;
                            CC++;
                            break;
                        case '-' :
                            C1++;
                            gap2++;
                            Cg++;
                            break;
                    }
                    ;
                    break;
                case '-' :
                    switch (seq.getSymbol(pos2InAlignment).getSymbol()) {
                        case 'A' :
                            gap1++;
                            A2++;
                            gA++;
                            ;
                            break;
                        case 'U' :
                            gap1++;
                            U2++;
                            gU++;
                            break;
                        case 'G' :
                            gap1++;
                            G2++;
                            gG++;
                            break;
                        case 'C' :
                            gap1++;
                            C2++;
                            gC++;
                            break;
                        case '-' :
                            gap1++;
                            gap2++;
                            gg++;
                            break;
                    }
                    ;
                    break;
            }
        }
        int t = this.getBiologicalSequenceCount();
        double result = Math.log10((AA/t)/((A1/t)*(A2/t)))
                +Math.log10((AU/t)/((A1/t)*(U2/t)))
                +Math.log10((AG/t)/((A1/t)*(G2/t)))
                +Math.log10((AC/t)/((A1/t)*(C2/t)))
                +Math.log10((GG/t)/((G1/t)*(G2/t)))
                +Math.log10((GU/t)/((G1/t)*(U2/t)))
                +Math.log10((GA/t)/((G1/t)*(A2/t)))
                +Math.log10((GC/t)/((G1/t)*(C2/t)))
                +Math.log10((UU/t)/((U1/t)*(U2/t)))
                +Math.log10((UA/t)/((U1/t)*(A2/t)))
                +Math.log10((UG/t)/((U1/t)*(G2/t)))
                +Math.log10((UC/t)/((U1/t)*(C2/t)))
                +Math.log10((CC/t)/((C1/t)*(C2/t)))
                +Math.log10((CU/t)/((C1/t)*(U2/t)))
                +Math.log10((CG/t)/((C1/t)*(G2/t)))
                +Math.log10((CA/t)/((C1/t)*(A2/t)))
                ;
        return result;

    }

    /**
     * Calculate and print the covariations between two positions within the alignment
     *
     * @param pos1
     * @param pos2
     * @return
     */
    public int[][] calculateCovariation(final int pos1, final int pos2) {
        int[][] m = null;
        if (pos1 != -1 && pos2 != -1) {
            AlignedMolecule seq = null;
            m = new int[6][6];
            for (int k = 0; k < this.getBiologicalSequenceCount(); k++) {
                seq = this.getBiologicalSequenceAt(k);
                //System.out.println(seq.getSymbol(this.pos1).getSymbol()+" "+seq.getSymbol(this.pos2).getSymbol());
                final char c2 = seq.getSymbol(pos2).getSymbol();
                switch (seq.getSymbol(pos1).getSymbol()) {
                    case 'A' :
                        switch (c2) {
                            case 'A' :
                                m[0][0]++;
                                break;
                            case 'U' :
                                m[0][1]++;
                                break;
                            case 'G' :
                                m[0][2]++;
                                break;
                            case 'C' :
                                m[0][3]++;
                                break;
                            case '-' :
                                m[0][4]++;
                                break;
                        }
                        ;
                        break;
                    case 'U' :
                        switch (c2) {
                            case 'A' :
                                m[1][0]++;
                                break;
                            case 'U' :
                                m[1][1]++;
                                break;
                            case 'G' :
                                m[1][2]++;
                                break;
                            case 'C' :
                                m[1][3]++;
                                break;
                            case '-' :
                                m[1][4]++;
                                break;
                        }
                        ;
                        break;
                    case 'G' :
                        switch (c2) {
                            case 'A' :
                                m[2][0]++;
                                break;
                            case 'U' :
                                m[2][1]++;
                                break;
                            case 'G' :
                                m[2][2]++;
                                break;
                            case 'C' :
                                m[2][3]++;
                                break;
                            case '-' :
                                m[2][4]++;
                                break;
                        }
                        ;
                        break;
                    case 'C' :
                        switch (c2) {
                            case 'A' :
                                m[3][0]++;
                                break;
                            case 'U' :
                                m[3][1]++;
                                break;
                            case 'G' :
                                m[3][2]++;
                                break;
                            case 'C' :
                                m[3][3]++;
                                break;
                            case '-' :
                                m[3][4]++;
                                break;
                        }
                        ;
                        break;
                    case '-' :
                        switch (c2) {
                            case 'A' :
                                m[4][0]++;
                                break;
                            case 'U' :
                                m[4][1]++;
                                break;
                            case 'G' :
                                m[4][2]++;
                                break;
                            case 'C' :
                                m[4][3]++;
                                break;
                            case '-' :
                                m[4][4]++;
                                break;
                        }
                        ;
                        break;
                }
            }
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++)
                    m[i][5] += m[i][j];
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++)
                    m[5][i] += m[j][i];
            for (int i = 0; i < 5; i++)
                m[5][5] += m[i][5];
            //we print it
            final DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            System.out.println("Statistics between positions " + (pos1 + 1) + "-" + (pos2 + 1) + ":\n");
            System.out.println("\t\tPosition " + (pos2 + 1) + "\n");
            System.out.println("\tA\tU\tG\tC\t-\n");
            for (int i = 0; i < 6; i++) {
                switch (i) {
                    case 0 :
                        System.out.print("A\t");
                        break;
                    case 1 :
                        System.out.print("U\t");
                        break;
                    case 2 :
                        System.out.print("G\t");
                        break;
                    case 3 :
                        System.out.print("C\t");
                        break;
                    case 4 :
                        System.out.print("-\t");
                        break;
                    case 5 :
                        System.out.print(" \t");
                        break;
                }
                for (int j = 0; j < 6; j++)
                    System.out.print(m[i][j] + "\t");
                if (i == 2)
                    System.out.print("Position " + (pos1 + 1));
                if (i != 5) {
                    System.out.println();
                    System.out.print("\t");
                    for (int j = 0; j < 5; j++)
                        System.out.print("(" + df.format((float) m[5][j] * (float) m[i][5] / (float) m[5][5]) + ")\t");
                }
                System.out.println();
                System.out.println();
            }

        }
        return m;
    }

    /**
     * Change a sequence position in the multiple alignment
     */
    public void moveSequence(int newPos, AlignedMolecule seq) {
        this.alignedMolecules.remove(seq);
        this.alignedMolecules.add(newPos, seq);
    }

    public void removeBiologicalSequenceAt(int index) {
        this.alignedMolecules.remove(index);
    }

    public List<AlignedMolecule> getAlignedMolecules() {
        return new ArrayList<AlignedMolecule>(this.alignedMolecules);
    }

    public int indexOf(Molecule m) {
        return this.alignedMolecules.indexOf(m);
    }

    public class SequenceMeter extends SymbolSequence {

        private MeterSize size;

        private SequenceMeter(final Mediator mediator, int length) {
            super(mediator);
            this.color = Color.DARK_GRAY;
            this.size = new MeterSize(length);
        }

        public String getSequence() {
            return null;
        }

        public Symbol getSymbol(final int index) {
            return null;
        }

        public void drawSymbol(final StructuralAlignment alignment, Graphics2D g, int currentX, int viewX, int currentY, int viewY, GraphicContext gc, int positionInReferenceSequence) {
            g.setColor(Color.BLACK);
            if (positionInReferenceSequence == 1 || positionInReferenceSequence % gc.getRatio() == 0) {
                String position = new StringBuffer().append(positionInReferenceSequence).toString();
                g.drawString(position, (float)currentX - (float)viewX-(float)(gc.getLetterWidth()*position.trim().length())/4f, (float)(currentY - gc.getLetterHeight()));
                g.drawString(new StringBuffer().append('|').toString(), currentX - viewX, currentY);
            } else
                g.drawString(new StringBuffer().append('.').toString(), (float) currentX - (float) viewX, (float) currentY);
            g.setColor(Color.BLACK);
        }


        public int size() {
            return this.size.getSize();
        }

        public void insertGap(final int index) {
            this.size.increaseSize();
        }

        public boolean removeGap(final int index) {
            if (this.isGap(index)) {
                this.size.decreaseSize();
                return true;
            }
            return false;
        }

        public void increaseSize() {
            this.size.increaseSize();
        }

        public boolean isGap(final int index) {
            return true;
        }

        public void setSelected(final boolean selected) {
            this.selected = false;
        }

        public void setSymbolAt(Symbol s, int i) {
            //does nothing
        }

        private class MeterSize {

            private int size;

            private MeterSize(final int size) {
                this.size = size;
            }

            private void setSize(final int size) {
                this.size = size;
            }

            private void increaseSize() {
                this.size++;
            }

            private void decreaseSize() {
                this.size--;
            }

            private int getSize() {
                return size;
            }
        }

    }
}

