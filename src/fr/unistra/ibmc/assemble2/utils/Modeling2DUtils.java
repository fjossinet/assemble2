package fr.unistra.ibmc.assemble2.utils;

import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.model.*;

import java.util.*;

public class Modeling2DUtils {

    /**
     *
     * @param mediator
     * @param alignedSequence
     * @param m
     * @param bracketNotation
     * @return
     * @throws Exception
     */
    public static SecondaryStructure getSecondaryStructure(Mediator mediator, String alignedSequence, Molecule m, String bracketNotation) throws Exception {
        bracketNotation.replace('_','-');
        if (alignedSequence.length() != bracketNotation.length())
            return null;
        List<Pair<Integer,Boolean>> roundedPositions = new ArrayList<Pair<Integer,Boolean>>(),
                                    squarredPositions = new ArrayList<Pair<Integer,Boolean>>(),
                                    curlyPositions = new ArrayList<Pair<Integer,Boolean>>(); //to store the positions of the left brackets and if the corresponding symbol in the sequence was a gep
        List<Location> allpairs = new ArrayList<Location>();
        int moleculePosition = 0;
        for (int i = 0; i < bracketNotation.length() ; i++) {
            if (alignedSequence.charAt(i) != '-')
                moleculePosition++;
            char c = bracketNotation.charAt(i);
            switch (c) {
                case '(' :
                    roundedPositions.add(new Pair<Integer, Boolean>(moleculePosition, alignedSequence.charAt(i) != '-'));
                    break;
                case '[' :
                    squarredPositions.add(new Pair<Integer, Boolean>(moleculePosition, alignedSequence.charAt(i) != '-'));
                    break;
                case '{' :
                    curlyPositions.add(new Pair<Integer, Boolean>(moleculePosition, alignedSequence.charAt(i) != '-'));
                    break;
                case ')' :
                    if (roundedPositions.isEmpty()) //unbalanced 2D => too many ')'
                        throw new Exception("Your bracket notation is unbalanced!!");
                    Pair<Integer, Boolean> lastLeftBracket = roundedPositions.get(roundedPositions.size()-1);
                    if (alignedSequence.charAt(i) != '-' && lastLeftBracket.getSecond()) {//if the residue under the left bracket was not a gap in the sequence
                        allpairs.add(new Location(new Location(lastLeftBracket.getFirst()), new Location(moleculePosition)));
                    }
                    roundedPositions.remove(lastLeftBracket);
                    break;
                case ']' :
                    if (squarredPositions.isEmpty()) //unbalanced 2D => too many ')'
                        throw new Exception("Your bracket notation is unbalanced!!");
                    lastLeftBracket = squarredPositions.get(squarredPositions.size()-1);
                    if (alignedSequence.charAt(i) != '-' && lastLeftBracket.getSecond()) {//if the residue under the left bracket was not a gap in the sequence
                        allpairs.add(new Location(new Location(lastLeftBracket.getFirst()), new Location(moleculePosition)));
                    }
                    squarredPositions.remove(lastLeftBracket);
                    break;
                case '}' :
                    if (curlyPositions.isEmpty()) //unbalanced 2D => too many ')'
                        throw new Exception("Your bracket notation is unbalanced!!");
                    lastLeftBracket = curlyPositions.get(curlyPositions.size()-1);
                    if (alignedSequence.charAt(i) != '-' && lastLeftBracket.getSecond()) {//if the residue under the left bracket was not a gap in the sequence
                        allpairs.add(new Location(new Location(lastLeftBracket.getFirst()), new Location(moleculePosition)));
                    }
                    curlyPositions.remove(lastLeftBracket);
                    break;
            }
        }
        if (!roundedPositions.isEmpty()) //unbalanced 2D => too many '('
            throw new Exception("Your bracket notation is unbalanced!!");
        Collections.sort(allpairs, new Comparator() {
            public int compare(Object o, Object o1) {
                return ((Location) o).getStart() - ((Location) o1).getStart();
            }
        });
        return getSecondaryStructure(mediator, "2D", m, allpairs, new HashMap<Location, String>(), new ArrayList<Location>(), new HashMap<Location, String>());
    }

    public static SecondaryStructure getSecondaryStructure(Mediator mediator, String name, Molecule m, List<Location> secondaryInteractions, Map<Location, String> secondaryInteractionsType, List<Location> tertiaryInteractions, Map<Location, String> tertiaryInteractionsType) throws Exception{
        List<Location> helices = new ArrayList<Location>();
        SecondaryStructure ss = new SecondaryStructure(mediator, m, name);
        Location allSingleStrandsLocation = new Location(1, m.size());
        if (!secondaryInteractions.isEmpty()) {
            Collections.sort(secondaryInteractions, new Comparator() {
                public int compare(Object o, Object o1) {
                    return ((Location) o).getStart() - ((Location) o1).getStart();
                }
            });

            Location newHelix = null;
            Location l1 = null, l2 = null;
            //System.out.println("START: "+m.printSequence());
            for (int i = 0; i < secondaryInteractions.size()-1 ; i++) {
                l1 = secondaryInteractions.get(i);
                l2 = secondaryInteractions.get(i+1);
                //System.out.println("l1: "+l1);
                //System.out.println("l2: "+l2);
                if (l1.getStart()+1 == l2.getStart() && l1.getEnd()-1 == l2.getEnd()) { //if the basepairs are contiguous
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
                        tertiaryInteractions.add(new Location(l1));
                        //System.out.println("tertiary interaction "+l1);
                    }
                    else {
                        helices.add(newHelix);
                        allSingleStrandsLocation.remove(newHelix);
                        // System.out.println("end of helix "+newHelix);

                    }
                    newHelix = null;
                }
            }
            //last helix
            if (newHelix != null) {
                helices.add(newHelix);
                //System.out.println("end of helix "+newHelix);
                allSingleStrandsLocation.remove(newHelix);
            }
            else  if (l2 != null) {
                tertiaryInteractions.add(new Location(l2));
                //System.out.println("tertiary interaction "+l2);
            }
            int i=1;
            for (Location _l:helices) {
                Helix helix = ss.addHelix(_l, "H" + (i++));
                if (helix == null) { //pseudoknot, the secondary interactions become tertiary ones
                    System.out.println("Pseudoknot");
                    for (int residuePos = 0; residuePos < _l.getLength()/2 ; residuePos++) {
                        Location __l = new Location(new Location(_l.getStart()+residuePos), new Location(_l.getEnd()-residuePos));
                        tertiaryInteractions.add(__l);
                        System.out.println(__l.toString());
                        if (secondaryInteractionsType.containsKey(__l))
                            tertiaryInteractionsType.put(__l, secondaryInteractionsType.get(__l));
                    }
                }
                else
                    for (BaseBaseInteraction bbi: new ArrayList<BaseBaseInteraction>(helix.getSecondaryInteractions())) {
                        if (secondaryInteractionsType.containsKey(bbi.getLocation())) {
                            char[] chars = secondaryInteractionsType.get(bbi.getLocation()).toCharArray();
                            helix.addSecondaryInteraction(bbi.getLocation(), chars[0], chars[1], chars[2]);
                        }
                    }
                //System.out.println("create helix "+_l);
            }
            for (Location _l:tertiaryInteractions) {
                if (!tertiaryInteractionsType.containsKey(_l))
                    ss.addTertiaryInteraction(_l, BaseBaseInteraction.ORIENTATION_CIS, '(', ')');
                else {
                    char[] chars = tertiaryInteractionsType.get(_l).toCharArray();
                    ss.addTertiaryInteraction(_l, chars[0], chars[1], chars[2]);
                }
            }
        }
        int[] boundaries = allSingleStrandsLocation.getEnds();
        for (int i = 0; i < boundaries.length; i += 2)
            ss.addSingleStrand(new Location(boundaries[i], boundaries[i + 1]), "SS" + (i + 1));
        return ss;
    }
}
