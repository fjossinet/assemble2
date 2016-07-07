package fr.unistra.ibmc.assemble2.model;

import com.mongodb.DBObject;
import fr.unistra.ibmc.assemble2.Assemble;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Molecule {

    protected boolean isGenomicAnnotation;
    protected String name, organism;
    protected StringBuffer sequence;
    protected String id;
    protected int fivePrimeEndGenomicPosition = -1; //the 5'end position on the plus strand of the genome (so the position of the first residue of the molecule if '+' orientation, of the last residue if '-' orientation
    protected boolean plusOrientation = true; //orientation according to a genome (if any, so plus by default)
    protected List<Annotation> annotations;
    protected SecondaryStructure ss;
    private DBObject basicDBObject;

    public Molecule(String name) {
        this.name = name;
        this.sequence = new StringBuffer();
        this.annotations = new ArrayList<Annotation>();
        this.setId(new ObjectId().toString());
    }

    public Molecule(String name, String sequence) {
        this(name);
        this.setSequence(sequence);
    }


    public void setSecondaryStructure(SecondaryStructure ss) {
        this.ss = ss;
    }

    public SecondaryStructure getSecondaryStructure() {
        return this.ss;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getOrganism() {
        return organism;
    }

    /**
     * Return all the annotations whose location overlap the location defined by the start and end parameters
     * @param start
     * @param end
     * @return
     */
    public List<Annotation> getAnnotations(int start, int end) {
        List<Annotation> hits = new ArrayList<Annotation>();
        for (Annotation a:this.annotations) {
            if (start >= a.getStart() && start <= a.getEnd() && end >= a.getStart() && end <= a.getEnd() ||  a.getStart() >= start &&  a.getStart() <= end || a.getEnd() >= start &&  a.getEnd() <= end)
                hits.add(a);
        }
        return hits;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(Annotation annotation) {
        for (Annotation a: this.annotations)
            if (a.getBasicDBObject().get("_id").equals(annotation.getBasicDBObject().get("_id")))
                return;
        this.annotations.add(annotation);
    }

    public void addAnnotations(List<Annotation> annotations) {
        this.annotations.addAll(annotations);
    }

    public boolean isGenomicAnnotation() {
        return this.isGenomicAnnotation;
    }

    public void isGenomicAnnotation(boolean genomicAnnotation) {
        this.isGenomicAnnotation = genomicAnnotation;
    }

    public int getFivePrimeEndGenomicPosition() {
        return fivePrimeEndGenomicPosition;
    }

    public void setFivePrimeEndGenomicPosition(int fivePrimeEndGenomicPosition) {
        this.fivePrimeEndGenomicPosition = fivePrimeEndGenomicPosition;
    }

    public boolean isPlusOrientation() {
        return plusOrientation;
    }

    public void setPlusOrientation(boolean plusOrientation) {
        this.plusOrientation = plusOrientation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setSequence(String sequence) {
        this.sequence = new StringBuffer();
        for (char c:sequence.toCharArray())
            this.addResidue(""+c);
    }

    public String getName() {
        return name;
    }

    public void addResidue(String residue) {
        String unModifiedNucleotide = Assemble.modifiedNucleotides.get(residue);
        if (unModifiedNucleotide != null)
            this.sequence.append(unModifiedNucleotide);
        else {
            if ("ADE".equals(residue) || "A".equals(residue))
                this.sequence.append("A");
            else if ("URA".equals(residue) || "URI".equals(residue) || "U".equals(residue))
                this.sequence.append("U");
            else if ("GUA".equals(residue) || "G".equals(residue))
                this.sequence.append("G");
            else if ("CYT".equals(residue) || "C".equals(residue))
                this.sequence.append("C");
            else if ("a".equals(residue) || "u".equals(residue) || "g".equals(residue) || "c".equals(residue) || "t".equals(residue))
                this.sequence.append(residue);
                //other kind of IUPAC symbols
            else if ("M".equals(residue))
                this.sequence.append("M");
            else if ("R".equals(residue))
                this.sequence.append("R");
            else if ("W".equals(residue))
                this.sequence.append("W");
            else if ("S".equals(residue))
                this.sequence.append("S");
            else if ("Y".equals(residue))
                this.sequence.append("Y");
            else if ("K".equals(residue))
                this.sequence.append("K");
            else if ("V".equals(residue))
                this.sequence.append("V");
            else if ("H".equals(residue))
                this.sequence.append("H");
            else if ("D".equals(residue))
                this.sequence.append("D");
            else if ("B".equals(residue))
                this.sequence.append("B");
            else if ("X".equals(residue))
                this.sequence.append("X");
            else if ("N".equals(residue))
                this.sequence.append("N");
            else
                this.sequence.append(residue);
        }
    }

    public int size() {
        return this.sequence.length();
    }

    public String printSequence() {
        return this.sequence.toString();
    }

    public String printSequence(Location l) {
        return this.sequence.substring(l.getStart() - 1, l.getEnd());
    }

    public String getResidueAt(int position) {
        if (position <= 0 || position > this.size())
            throw new RuntimeException("The position asked for is outside the molecule's boundaries");
        else
            return Character.toString(this.sequence.charAt(position - 1));
    }

    public static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    public static String reverseComplement(String s) {
        StringBuffer rc = new StringBuffer();
        String reverse = reverse(s);
        for (char r: reverse.toCharArray())
            switch (r) {
                case 'A': rc.append('U'); break;
                case 'U':
                case 'T': rc.append('A'); break;
                case 'G': rc.append('C'); break;
                case 'C': rc.append('G'); break;
                default: rc.append('?');
            }

        return rc.toString();
    }

    @Override
    public String toString() {
        return this.name;
    }

    void addToSequence(int position, String sequence) {
        this.sequence.insert(position,sequence);
    }

    public void removeAnnotation(Annotation annotation) {
        this.annotations.remove(annotation);
    }

    public void setBasicDBObject(DBObject basicDBObject) {
        this.basicDBObject = basicDBObject;
    }

    public DBObject getBasicDBObject() {
        return basicDBObject;
    }
}
