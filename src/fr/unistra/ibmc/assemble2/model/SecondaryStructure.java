package fr.unistra.ibmc.assemble2.model;

import fr.unistra.ibmc.assemble2.gui.GraphicContext;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import org.apache.commons.lang3.tuple.MutablePair;
import org.bson.types.ObjectId;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class SecondaryStructure {

    private List<Helix> helices;
    private List<SingleStrand> singleStrands;
    private List<Junction> junctions;
    private List<BaseBaseInteraction> tertiaryInteractions, phosphodiesterBonds;
    private List<Residue> residues;
    private Molecule m;
    private String id;
    private String name;
    private TertiaryStructure linkedTs;
    private boolean plotted;
    private Mediator mediator;
    private String source;

    public SecondaryStructure(SecondaryStructure _ss) { //this method is only used to create a deep copy of a 2D for the working session. FOR NOW, I DON'T CARE TO COPY THE JUNCTIONS !!!
        this.mediator = _ss.mediator;
        /*this.m = new Molecule(_ss.getMolecule().getName(), _ss.getMolecule().printSequence());
        this.m.setBasicDBObject(_ss.getMolecule().getBasicDBObject());
        this.m.isGenomicAnnotation(_ss.getMolecule().isGenomicAnnotation());
        this.m.addAnnotations(_ss.getMolecule().getAnnotations());
        this.m.setPlusOrientation(_ss.getMolecule().isPlusOrientation());
        this.m.setFivePrimeEndGenomicPosition(_ss.getMolecule().getFivePrimeEndGenomicPosition());
        this.m.setSecondaryStructure(this);*/
        this.m = _ss.getMolecule();
        this.source = _ss.source;
        this.residues = new ArrayList<Residue>();
        for (int i=0; i< m.size();i++) {
            Residue r = new Residue(mediator, this,i+1);
            r.setRealCoordinates(_ss.getResidue(i+1).getX(), _ss.getResidue(i+1).getY());
            this.residues.add(r);
        }
        this.phosphodiesterBonds = new ArrayList<BaseBaseInteraction>();
        for (int i=1; i<= this.residues.size()-1;i++)
            this.phosphodiesterBonds.add(new PhosphodiesterBond(mediator, this,new Location(i,i+1)));
        this.helices= new ArrayList<Helix>();
        for (Helix _h:_ss.getHelices()) {
            Helix h = new Helix(mediator, this, new Location(_h.getLocation()), _h.getName());
            for (BaseBaseInteraction _bbi:_h.getSecondaryInteractions())
                h.addSecondaryInteraction(new Location(_bbi.getLocation()), _bbi.getOrientation(), _bbi.getEdge(_bbi.getResidue()), _bbi.getEdge(_bbi.getPartnerResidue()));
            this.helices.add(h);
        }
        this.tertiaryInteractions = new ArrayList<BaseBaseInteraction>();
        for (BaseBaseInteraction _bbi:_ss.getTertiaryInteractions())
            this.addTertiaryInteraction(new Location(_bbi.getLocation()), _bbi.getOrientation(), _bbi.getEdge(_bbi.getResidue()), _bbi.getEdge(_bbi.getPartnerResidue()));
        this.singleStrands = new ArrayList<SingleStrand>();
        for (SingleStrand _singlestrand:_ss.getSingleStrands())
            this.singleStrands.add(new SingleStrand(this, new Location(_singlestrand.getLocation()),_singlestrand.getName()));
        this.junctions = new ArrayList<Junction>(); //for now I don't care
        this.name = _ss.getName();
        this.id = new ObjectId().toString();
    }

    public Point2D getCenter(GraphicContext gc, List<Residue> residues) {
        double  maxX =  this.getCurrentMaxX(gc, residues),
                minX =  this.getCurrentMinX(gc, residues),
                maxY =  this.getCurrentMaxY(gc, residues),
                minY =  this.getCurrentMinY(gc, residues);
        return new Point2D.Double((maxX-minX)/2+minX, (maxY-minY)/2+minY);
    }

    public boolean hasSingleHBonds() {
        for (BaseBaseInteraction bbi: this.tertiaryInteractions)
            if (SingleHBond.class.isInstance(bbi))
                return true;
        return false;
    }

    public boolean hasTertiaryInteractions() {
        return !this.tertiaryInteractions.isEmpty();
    }

    public SecondaryStructure(Mediator mediator, Molecule m, String name) {
        this.mediator = mediator;
        this.m = m;
        this.m.setSecondaryStructure(this);
        this.residues = new ArrayList<Residue>();
        for (int i=0; i< m.size();i++)
            this.residues.add(new Residue(mediator, this,i+1));
        this.phosphodiesterBonds = new ArrayList<BaseBaseInteraction>();
        for (int i=1; i<= this.residues.size()-1;i++)
            this.phosphodiesterBonds.add(new PhosphodiesterBond(mediator, this,new Location(i,i+1)));
        this.helices= new ArrayList<Helix>();
        this.tertiaryInteractions = new ArrayList<BaseBaseInteraction>();
        this.singleStrands = new ArrayList<SingleStrand>();
        this.junctions = new ArrayList<Junction>();
        this.name = name;
        this.id = new ObjectId().toString();
    }

    public SecondaryStructure(Mediator mediator, Molecule m, List<Location> helices, List<MutablePair<Location,String>> non_canonical_secondary_interactions, List<MutablePair<Location,String>> tertiary_interactions) {
        this(mediator, m,helices, non_canonical_secondary_interactions, tertiary_interactions, "Secondary Structure");
    }

    private SecondaryStructure(Mediator mediator, Molecule m, List<Location> helices, List<MutablePair<Location,String>> non_canonical_secondary_interactions, List<MutablePair<Location,String>> tertiary_interactions, String name) {
        this(mediator, m,name);
        Location single_strands_location = new Location(1,m.size());
        int index=0;
        for (Location helixLocation:helices) {
            Helix h = this.addHelix(helixLocation, "H" + (index++));
            if (h != null) {//could be null if pseudoknot
                single_strands_location.remove(helixLocation);
                for (MutablePair<Location,String> pair:non_canonical_secondary_interactions) {
                    Location location = pair.left;
                    if (h.getLocation().hasPosition(location.getStart()) && h.getLocation().hasPosition(location.getEnd())) {
                        String type = pair.right;
                        h.addSecondaryInteraction(location, type.charAt(0), type.charAt(1), type.charAt(2));
                    }
                }
            } else {//we need to add the secondary interactions of this helix as tertiary ones
                for (int i=0; i<helixLocation.getLength()/2;i++) {
                    BaseBaseInteraction interaction = null;
                    for (MutablePair<Location,String> pair: non_canonical_secondary_interactions) { //is it a non-canonical interaction?
                        Location location = pair.left;
                        if (helixLocation.hasPosition(location.getStart()) && helixLocation.hasPosition(location.getEnd())) {
                            String type = pair.right;
                            interaction = this.addTertiaryInteraction(location, type.charAt(0), type.charAt(1), type.charAt(2));
                        }
                    }
                    if (interaction == null) //so it is a canonical tertiary interaction
                        interaction = this.addTertiaryInteraction(new Location(new Location(helixLocation.getStart()+i),new Location(helixLocation.getEnd()-i)), BaseBaseInteraction.ORIENTATION_CIS, '(', ')');
                    interaction.getResidue().addTertiaryInteraction(interaction);
                    interaction.getPartnerResidue().addTertiaryInteraction(interaction);
                }
            }
        }
        for (MutablePair<Location,String> pair:tertiary_interactions) {
            String type = pair.right;
            BaseBaseInteraction tertiaryInteraction  = null;
            if (type.charAt(1)== '!' && type.charAt(2) == '!')
                tertiaryInteraction = new SingleHBond(mediator, this,pair.left);
            else
                tertiaryInteraction = new BaseBaseInteraction(mediator, this,pair.left, type.charAt(0), type.charAt(1), type.charAt(2));
            this.tertiaryInteractions.add(tertiaryInteraction);
            tertiaryInteraction.getResidue().addTertiaryInteraction(tertiaryInteraction);
            tertiaryInteraction.getPartnerResidue().addTertiaryInteraction(tertiaryInteraction);
        }
        int[] positions = single_strands_location.getEnds();
        index=0;
        for (int i=0; i<positions.length-1;i+=2)
            this.singleStrands.add(new SingleStrand(this,new Location(positions[i],positions[i+1]),"SS"+(index++)));
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double getMinX() {
        double minx = Integer.MAX_VALUE;
        for (Residue r: this.residues)
            minx = r.getRealCoordinates().getX() < minx ? r.getRealCoordinates().getX() : minx;
        return minx;
    }

    public double getMinY() {
        double miny = Integer.MAX_VALUE;
        for (Residue r: this.residues)
            miny = r.getRealCoordinates().getY() < miny ? r.getRealCoordinates().getY() : miny;
        return miny;
    }

    public double getMaxX() {
        double max = 0;
        for (Residue r: this.residues)
            max = r.getRealCoordinates().getX() > max ? r.getRealCoordinates().getX() : max;
        return max;
    }

    public double getMaxY() {
        double max = 0;
        for (Residue r: this.residues)
            max = r.getRealCoordinates().getY() > max ? r.getRealCoordinates().getY() : max;
        return max;
    }

    public double getCurrentMinX(GraphicContext gc, List<Residue> residues) {
        double minx = Integer.MAX_VALUE;
        for (Residue r: residues)
            minx = r.getCurrentX(gc) < minx ? r.getCurrentX(gc) : minx;
        return minx;
    }

    public double getCurrentMinY(GraphicContext gc, List<Residue> residues) {
        double miny = Integer.MAX_VALUE;
        for (Residue r: residues)
            miny = r.getCurrentY(gc)-gc.getCurrentHeight() < miny ? r.getCurrentY(gc)-gc.getCurrentHeight() : miny;
        return miny;
    }

    public double getCurrentMaxX(GraphicContext gc, List<Residue> residues) {
        double max = 0;
        for (Residue r: residues)
            max = r.getCurrentX(gc) > max ? r.getCurrentX(gc) : max;
        return max;
    }

    public double getCurrentMaxY(GraphicContext gc, List<Residue> residues) {
        double max = 0;
        for (Residue r: residues)
            max = r.getCurrentY(gc) > max ? r.getCurrentY(gc) : max;
        return max;
    }

    public void setMolecule(Molecule m) {
        this.m = m;
        this.m.setSecondaryStructure(this);
    }


    public TertiaryStructure getLinkedTs() {
        return linkedTs;
    }

    public void setLinkedTs(TertiaryStructure linkedTs) {
        this.linkedTs = linkedTs;
    }

    public boolean isPlotted() {
        return plotted;
    }

    public void setPlotted(boolean plotted) {
        this.plotted = plotted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Helix addHelix(Location location, String name) {
        for (int pos:location.getSinglePositions())
            if (Helix.class.isInstance(this.getResidue(pos).getStructuralDomain()))
                this.removeHelix((Helix)this.getResidue(pos).getStructuralDomain());
        //no pseudoknot allowed
        for (Helix h:new ArrayList<Helix>(this.getHelices())) {
            int[]   _ends = location.getEnds(),
                    ends = h.getLocation().getEnds();

            //fix to handle helices with contiguous strands (1FFK has one)
            if (_ends.length == 2)
                _ends = new int[]{location.getStart(), location.getStart()+location.getLength()/2-1, location.getEnd()-location.getLength()/2+1, location.getEnd()};
            if (ends.length == 2)
                ends = new int[]{h.getLocation().getStart(), h.getLocation().getStart()+h.getLocation().getLength()/2-1, h.getLocation().getEnd()-h.getLocation().getLength()/2+1, h.getLocation().getEnd()};
            //end of fix

            if ((_ends[0] >= ends[1] && _ends[0] <= ends[2] && _ends[3] >= ends[3] ||
                    _ends[0] <= ends[0] && _ends[3] >= ends[1] && _ends[3] <= ends[2])) {
                return null;
            }
        }
        Helix h = new Helix(mediator, this,location,name);
        this.helices.add(h);
        if (this.mediator != null)
            this.mediator.getSecondaryStructureNavigator().insertNode(m, h);
        for (SingleStrand ss:new ArrayList<SingleStrand>(this.getSingleStrands())) {
            Location l = ss.getLocation();
            int previousLength = l.getLength();
            for (int pos:h.getLocation().getSinglePositions())
                l.remove(pos);
            if (previousLength != l.getLength()) { //the single strand has been altered
                this.removeSingleStrand(ss);  //we remove it
                int[] ends = l.getEnds(); //we create new single-strands
                for (int i = 0; i < ends.length-1;i+=2)
                    this.addSingleStrand(new Location(ends[i], ends[i + 1]), ss.getName());
            }
        }
        return h;
    }

    public Helix addPseudoknot(Location location, String name) {
        for (int pos:location.getSinglePositions())
            if (Helix.class.isInstance(this.getResidue(pos).getStructuralDomain()))
                this.removeHelix((Helix)this.getResidue(pos).getStructuralDomain());
        Pseudoknot h = new Pseudoknot(mediator, this,location,name);
        this.helices.add(h);
        this.mediator.getSecondaryStructureNavigator().insertNode(m, h);
        for (SingleStrand ss:new ArrayList<SingleStrand>(this.getSingleStrands())) {
            Location l = ss.getLocation();
            int previousLength = l.getLength();
            for (int pos:h.getLocation().getSinglePositions())
                l.remove(pos);
            if (previousLength != l.getLength()) { //the single strand has been altered
                this.removeSingleStrand(ss);  //we remove it
                int[] ends = l.getEnds(); //we create new single-strands
                for (int i = 0; i < ends.length-1;i+=2)
                    this.addSingleStrand(new Location(ends[i], ends[i + 1]), ss.getName());
            }
        }
        return h;
    }

    public SingleStrand addSingleStrand(Location location, String name) {
        SingleStrand ss = new SingleStrand(this,location,name);
        this.singleStrands.add(ss);
        this.mediator.getSecondaryStructureNavigator().insertNode(m, ss);
        return ss;
    }

    public BaseBaseInteraction addTertiaryInteraction(Location location, char orientation, char edge, char partnerEdge) {
        BaseBaseInteraction bbi = null;
        if (edge == '!' && partnerEdge == '!')
            bbi = new SingleHBond(mediator, this,location);
        else
            bbi= new BaseBaseInteraction(mediator, this,location,orientation,edge,partnerEdge);

        bbi.getResidue().addTertiaryInteraction(bbi);
        bbi.getPartnerResidue().addTertiaryInteraction(bbi);
        this.tertiaryInteractions.add(bbi);
        this.mediator.getSecondaryStructureNavigator().insertNode(m, bbi);
        return bbi;
    }

    public BaseBaseInteraction addSecondaryInteraction(Location location, char orientation, char edge, char partnerEdge) {
        for (Helix h:this.helices)
            if (h.getLocation().hasPosition(location.getStart()) && h.getLocation().hasPosition(location.getEnd()))
                return h.addSecondaryInteraction(location, orientation, edge, partnerEdge);
        return null;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<Junction> getJunctions() {
        return junctions;
    }

    public void findJunctions() {
        this.junctions.clear();
        Junction junction = null;
        List<StructuralDomain> structuralDomains = null;
        List<MutablePair<Molecule,Location>> fragments = null;
        for (SingleStrand singleStrand:this.getSingleStrands()) {
            if (singleStrand.isAtFivePrimeEnd() || singleStrand.isAtThreePrimeEnd())
                continue;
            if (structuralDomains == null) {
                structuralDomains = new ArrayList<StructuralDomain>();
                structuralDomains.add(singleStrand);
                //System.out.println(singleStrand.getFullLocation(singleStrand.getMolecules().iterator().next())+" added to junction");
            }
            if (fragments == null) {
                fragments = new ArrayList<MutablePair<Molecule, Location>>();
                Location l = singleStrand.getLocation();
                fragments.add(new MutablePair<Molecule, Location>(m,new Location(l.getStart()-1,l.getEnd()+1)));
            }
            //now we walk along the new junction, next residue per next residue
            StructuralDomain current_structural_domain = null;
            Residue currentResidue = this.getResidue(singleStrand.getLocation().getEnd()).getNextResidue();
            while (currentResidue != null) {
                current_structural_domain = this.getEnclosingStructuralDomain(currentResidue);
                if (current_structural_domain.equals(singleStrand) || structuralDomains.contains(current_structural_domain)) {//the walk is finished
                    junction = new Junction(this);
                    this.junctions.add(junction);
                    for (StructuralDomain _sd:structuralDomains)
                        junction.addStructuralDomain(_sd);
                    for (MutablePair<Molecule, Location> fragment:fragments)
                        junction.addFragment(fragment);
                    junctions.add(junction);
                    break;
                }
                //System.out.println(sd.getFullLocation(singleStrand.getMolecules().iterator().next())+" added to junction");
                structuralDomains.add(current_structural_domain);
                if (Helix.class.isInstance(current_structural_domain)) { //an helix has two sides, we have to stay on the right one
                    if (Helix.class.isInstance(this.getEnclosingStructuralDomain(currentResidue.getPreviousResidue()))) { //two helices directly linked
                        fragments.add(new MutablePair<Molecule, Location>(currentResidue.getMolecule(),new Location(currentResidue.getAbsolutePosition()-1,currentResidue.getAbsolutePosition())));
                    }
                    if (Pseudoknot.class.isInstance(current_structural_domain)) {
                        currentResidue = new Residue(mediator, this, currentResidue.getAbsolutePosition()+current_structural_domain.getLength());
                    }
                    else {
                        Residue pairedResidue = this.getPairedResidueInSecondaryInteraction(currentResidue);
                        currentResidue = pairedResidue.getNextResidue();
                    }
                }
                else  {
                    Location l = current_structural_domain.getLocation();
                    fragments.add(new MutablePair<Molecule, Location>(m,new Location(l.getStart()-1,l.getEnd()+1)));
                    currentResidue = new Residue(mediator, this, current_structural_domain.getLocation().getEnd()).getNextResidue();
                }
            }
            structuralDomains = null;
            fragments = null;
        }
    }

    public String printAsBracketNotation() {
        char[] ret = new char[m.size()];
        //initialisation, unpaired residues
        for (int i = 0; i < m.size(); i++)
            ret[i] = '.';
        //now the paired residues
        for (Helix helix : this.getHelices()) {
            for (BaseBaseInteraction bb : helix.getSecondaryInteractions()) {
                ret[bb.getLocation().getStart() - 1] = '(';
                ret[bb.getLocation().getEnd() - 1] = ')';
            }
        }
        return new String(ret);
    }

    public Molecule getMolecule() {
        return m;
    }

    public Residue getResidue(int pos) {
        if (pos < 1 || pos > this.m.size())
            return null;
        return this.residues.get(pos-1);
    }

    public void draw(final java.awt.Graphics2D g,GraphicContext gc) {
        for (Helix h: this.helices)
            h.draw(g,gc);
        for (SingleStrand ss:this.singleStrands)
            ss.draw(g,gc);
        for (BaseBaseInteraction bbi: this.tertiaryInteractions)
            if (SingleHBond.class.isInstance(bbi) && gc.displaySingleHBonds() || !SingleHBond.class.isInstance(bbi))
                bbi.draw(g,gc);
        for (BaseBaseInteraction bbi:this.phosphodiesterBonds)
            bbi.draw(g,gc);
        for (Residue r:this.residues)
            r.isUpdated(false);
    }

    public List<Helix> getHelices() {
        return helices;
    }

    public List<SingleStrand> getSingleStrands() {
        return singleStrands;
    }

    public List<BaseBaseInteraction> getAllBaseBaseInteractions() {
        List <BaseBaseInteraction> all = new ArrayList<BaseBaseInteraction>();
        all.addAll(this.getTertiaryInteractions());
        for (Helix h:this.getHelices())
            all.addAll(h.getSecondaryInteractions());
        return all;
    }

    public List<BaseBaseInteraction> getTertiaryInteractions() {
        return tertiaryInteractions;
    }

    public List<BaseBaseInteraction> getPhosphodiesterBonds() {
        return phosphodiesterBonds;
    }

    public List<Residue> getResidues() {
        return residues;
    }

    public Residue getPairedResidueInSecondaryInteraction(Residue residue) {
        Residue pairedResidue = null;
        for (Helix h: this.helices) {
            pairedResidue = h.getPairedResidue(residue);
            if (pairedResidue != null)
                return pairedResidue;
        }
        return null;
    }

    public Residue getPairedResidue(Residue residue) {
        Residue pairedResidue = null;
        for (Helix h: this.helices) {
            pairedResidue = h.getPairedResidue(residue);
            if (pairedResidue != null)
                return pairedResidue;
        }
        for (BaseBaseInteraction bbi:this.tertiaryInteractions) {
            if (bbi.getResidue().equals(residue))
                return bbi.getPartnerResidue();
            else if (bbi.getPartnerResidue().equals(residue))
                return bbi.getResidue();
        }
        return null;
    }

    public StructuralDomain getEnclosingStructuralDomain(Residue residue) {
        for (Helix h: this.helices)
            if (h.getLocation().hasPosition(residue.getAbsolutePosition()))
                return h;
        for (SingleStrand ss:this.singleStrands)
            if (ss.getLocation().hasPosition(residue.getAbsolutePosition()))
                return ss;
        return null;
    }

    public Junction getEnclosingJunction(Residue residue) {
        for (Junction j: this.junctions)
            if (j.hasPosition(residue.getAbsolutePosition()))
                return j;
        return null;
    }

    public void setCoordinates(int position, float x, float y) {
        Residue r = this.getResidue(position);
        if (r != null) {
            r.setRealCoordinates(x,y);
        }
    }

    public void removeTertiaryInteraction(BaseBaseInteraction interaction) {
        interaction.getResidue().removeTertiaryInteraction(interaction);
        interaction.getPartnerResidue().removeTertiaryInteraction(interaction);
        this.tertiaryInteractions.remove(interaction);
    }

    public void removeHelix(Helix helix) {
        for (BaseBaseInteraction bbi:helix.getSecondaryInteractions()) {
            bbi.getResidue().removeSecondaryInteraction();
            bbi.getPartnerResidue().removeSecondaryInteraction();
        }
        this.mediator.getSecondaryStructureNavigator().removeNode(this.m,helix);
        for (Residue r: helix.get5PrimeEnds()) {
            SingleStrand ss1 = null, ss2 = null;
            if (r.getPreviousResidue() != null && SingleStrand.class.isInstance(r.getPreviousResidue().getStructuralDomain())) {
                ss1 = (SingleStrand)r.getPreviousResidue().getStructuralDomain();
                this.removeSingleStrand(ss1);
            }
            Residue _r = helix.get3PrimeEnd(r).getNextResidue();
            if (_r != null && SingleStrand.class.isInstance(_r.getStructuralDomain())) {
                ss2 =  (SingleStrand)_r.getStructuralDomain();
                this.removeSingleStrand(ss2);
            }
            Location newSingleStrand =  new Location(r.getAbsolutePosition(),r.getAbsolutePosition()+helix.getLength()-1);
            String name = "SS"+this.getSingleStrands().size();
            if (ss1 != null) {
                newSingleStrand.add(ss1.getLocation());
                name = ss1.getName();
            }
            if (ss2 != null) {
                newSingleStrand.add(ss2.getLocation());
                name = ss2.getName();
            }
            this.addSingleStrand(newSingleStrand,name);
        }
        this.helices.remove(helix);
    }

    public void removeSingleStrand(SingleStrand ss) {
        this.singleStrands.remove(ss);
        this.mediator.getSecondaryStructureNavigator().removeNode(this.m,ss);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
