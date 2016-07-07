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

public class Contrafold  extends Computation {

    public Contrafold(Mediator mediator) {
        super(mediator);
    }

    public SecondaryStructure fold(Molecule m) throws Exception {
        String sequenceInput = ">"+m.getName()+"\n"+m.printSequence();
        Map<String,String> data = new Hashtable<String,String>();
        data.put("data", sequenceInput);
        data.put("tool","contrafold");
        String _2DPrediction= this.postData("compute/2d", data);
        if (_2DPrediction != null && _2DPrediction.length() != 0) {
            BasicDBObject secondaryStructure = null;
            if (BasicDBObject.class.isInstance(JSON.parse(_2DPrediction))) //to keep the compatibility with previous versions of the webservice that returns only a single 2D
                secondaryStructure = (BasicDBObject) JSON.parse(_2DPrediction);
            else
                secondaryStructure = (BasicDBObject) ((BasicDBList) JSON.parse(_2DPrediction)).get(0);
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
            ss.setSource("CONTRAfold");
            ss.setMolecule(m);
            return ss;
        }
        return null;
    }

}
