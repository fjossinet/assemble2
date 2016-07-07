package fr.unistra.ibmc.assemble2.io.computations;


import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.model.Location;
import fr.unistra.ibmc.assemble2.model.Molecule;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;

public class Rnasubopt extends Computation {

    public static int RANDOM_SAMPLE = 10;

    public Rnasubopt(Mediator mediator) {
        super(mediator);
    }

    public List<SecondaryStructure> fold(Molecule m) throws Exception {
        List<SecondaryStructure> secondaryStructures = new ArrayList<SecondaryStructure>();
        String sequenceInput = ">"+m.getName()+"\n"+m.printSequence();
        Map<String,String> data = new Hashtable<String,String>();
        data.put("data", sequenceInput);
        data.put("tool","rnasubopt");
        data.put("random_sample",""+RANDOM_SAMPLE);
        String _2DPredictions= this.postData("compute/2d", data);
        if (_2DPredictions != null && _2DPredictions.length() != 0) {
            for (Object _2DPrediction:(BasicDBList) JSON.parse(_2DPredictions)) {
                BasicDBObject  secondaryStructure = (BasicDBObject) _2DPrediction;
                Iterator helices = ((BasicDBList) secondaryStructure.get("helices")).iterator(),
                        tertiaryInteractions = ((BasicDBList) secondaryStructure.get("tertiaryInteractions")).iterator();
                List<Location> helicalLocations = new ArrayList<Location>();
                List<MutablePair<Location, String>> tertiaryInteractionLocations = new ArrayList<MutablePair<Location, String>>();
                while (helices.hasNext()) {
                    BasicDBObject helix =  (BasicDBObject) helices.next();
                    Object location = helix.get("location");

                    BasicDBList ends = null;
                    if (BasicDBObject.class.isInstance(location))
                        ends = (BasicDBList)((BasicDBObject)location).get("ends");
                    else
                        ends = (BasicDBList)location;
                    BasicDBList strand1 = (BasicDBList)ends.get(0), strand2 = (BasicDBList)ends.get(1);
                    helicalLocations.add(new Location(new Location((Integer)strand1.get(0), (Integer)strand1.get(1)), new Location((Integer)strand2.get(0), (Integer)strand2.get(1))));
                }
                while (tertiaryInteractions.hasNext()) {
                    BasicDBObject tertiaryInteraction =  (BasicDBObject) tertiaryInteractions.next();
                    Object location = tertiaryInteraction.get("location");

                    BasicDBList ends = null;
                    if (BasicDBObject.class.isInstance(location))
                        ends = (BasicDBList)((BasicDBObject)location).get("ends");
                    else
                        ends = (BasicDBList)location;

                    BasicDBList edge1 = (BasicDBList)ends.get(0), edge2 = (BasicDBList)ends.get(1);
                    String type =  tertiaryInteraction.get("orientation")+""+tertiaryInteraction.get("edge1")+""+tertiaryInteraction.get("edge2");
                    tertiaryInteractionLocations.add(new MutablePair<Location, String>(new Location(new Location((Integer)edge1.get(0)), new Location((Integer)edge2.get(0))),type.toUpperCase()));
                }
                SecondaryStructure ss = new SecondaryStructure(mediator, m, helicalLocations, new ArrayList<MutablePair<Location,String>>(), tertiaryInteractionLocations);
                ss.setSource("RNAsubopt");
                ss.setMolecule(m);
                secondaryStructures.add(ss);
            }
        }
        return secondaryStructures;
    }
}
