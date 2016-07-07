package fr.unistra.ibmc.assemble2.io.computations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.*;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.model.TertiaryFragmentHit;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;

public class DataHandler extends Computation {

    public DataHandler(Mediator mediator) {
        super(mediator);
    }

    public List<Residue3D> getTertiaryStructure(String tertiaryStructureId, String mongoDBName) throws Exception {
        List<Residue3D> residue3Ds = new ArrayList<Residue3D>();
        JsonObject molecule = null, tertiaryStructure = null;
        JsonParser jsonParser = new JsonParser();
        if (this.mediator.getMongo() == null) { //if no MongoDB linked to Assemble2, we use the webservices
            Map<String,String> data = new Hashtable<String,String>();
            data.put("coll","tertiaryStructures");
            data.put("id",tertiaryStructureId);
            String result = null;
            if (AssembleConfig.getFragmentsLibrary().equals("Redundant"))
                result = this.postData("pdb", data);
            else if (AssembleConfig.getFragmentsLibrary().equals("Non redundant"))
                result = this.postData("rna3dhub", data);

            tertiaryStructure = jsonParser.parse(result).getAsJsonObject();
            data = new Hashtable<String,String>();
            data.put("coll","ncRNAs");
            data.put("id",tertiaryStructure.get("rna").getAsString().split("@ncRNAs")[0]);
            if (AssembleConfig.getFragmentsLibrary().equals("Redundant"))
                result = this.postData("pdb", data);
            else if (AssembleConfig.getFragmentsLibrary().equals("Non redundant"))
                result = this.postData("rna3dhub", data);
            molecule =  jsonParser.parse(result).getAsJsonObject();
        } else {
            BasicDBObject query = new BasicDBObject("_id", tertiaryStructureId);
            tertiaryStructure = jsonParser.parse(this.mediator.getMongo().getDB(mongoDBName).getCollection("tertiaryStructures").find(query).iterator().next().toString()).getAsJsonObject();
            query = new BasicDBObject("_id", tertiaryStructure.get("rna").getAsString().split("@ncRNAs")[0]);
            molecule = jsonParser.parse(this.mediator.getMongo().getDB(mongoDBName).getCollection("ncRNAs").find(query).iterator().next().toString()).getAsJsonObject();
        }
        Molecule rna = new Molecule(molecule.get("name").getAsString(),molecule.get("sequence").getAsString());
        TertiaryStructure ts = new TertiaryStructure(rna);
        JsonObject residues = tertiaryStructure.get("residues").getAsJsonObject();
        for (int pos = 1 ; pos <= rna.size() ; pos++) {
            Residue3D residue3D  = ts.addResidue3D(pos);
            JsonArray atoms = residues.get(""+pos).getAsJsonObject().get("atoms").getAsJsonArray();
            for (Object atom:atoms) {
                Residue3D.Atom a = residue3D.getAtom(((JsonObject)atom).get("name").getAsString());
                if (a != null) {
                    JsonArray coords = ((JsonObject)atom).get("coords").getAsJsonArray();
                    double x = coords.get(0).getAsDouble(),
                            y = coords.get(1).getAsDouble(),
                            z = coords.get(2).getAsDouble();
                    a.setCoordinates((float)x,(float)y,(float)z);
                }
            }
            residue3Ds.add(residue3D);
        }
        return residue3Ds;
    }

    public List<Residue3D> getResidue3DByMolecularLocation(String moleculeId, Location location, String tertiaryStructureId) throws Exception {
        List<Residue3D> residue3Ds = new ArrayList<Residue3D>();
        JsonParser jsonParser = new JsonParser();
        JsonObject tertiaryStructure = null, molecule = null;

        if (this.mediator.getPDBMongo() == null) { //if no MongoDB linked to Assemble2, we use the webservices

            Map<String,String> data = new Hashtable<String,String>();
            data.put("coll","tertiaryStructures");
            data.put("id",tertiaryStructureId);
            String result = null;
            if (AssembleConfig.getFragmentsLibrary().equals("Redundant"))
                result = this.postData("pdb", data);
            else if (AssembleConfig.getFragmentsLibrary().equals("Non redundant"))
                result = this.postData("rna3dhub", data);

            tertiaryStructure = jsonParser.parse(result).getAsJsonObject();

            data = new Hashtable<String,String>();
            data.put("coll","ncRNAs");
            data.put("id",moleculeId);
            if (AssembleConfig.getFragmentsLibrary().equals("Redundant"))
                result = this.postData("pdb", data);
            else if (AssembleConfig.getFragmentsLibrary().equals("Non redundant"))
                result = this.postData("rna3dhub", data);
            molecule = jsonParser.parse(result).getAsJsonObject();

        } else {
            BasicDBObject query = new BasicDBObject("_id", tertiaryStructureId);
            tertiaryStructure = jsonParser.parse(mediator.getPDBMongo().getCollection("tertiaryStructures").find(query).iterator().next().toString()).getAsJsonObject();
            query = new BasicDBObject("_id", moleculeId);
            molecule = jsonParser.parse(mediator.getPDBMongo().getCollection("ncRNAs").find(query).iterator().next().toString()).getAsJsonObject();
        }

        Molecule rna = new Molecule(molecule.get("name").getAsString(),molecule.get("sequence").getAsString());
        TertiaryStructure ts = new TertiaryStructure(rna);
        JsonObject residues = tertiaryStructure.get("residues").getAsJsonObject();
        for (int pos:location.getSinglePositions()) {
            Residue3D residue3D  = ts.addResidue3D(pos);
            JsonArray atoms = residues.get(""+pos).getAsJsonObject().get("atoms").getAsJsonArray();
            for (Object atom:atoms) {
                Residue3D.Atom a = residue3D.getAtom(((JsonObject)atom).get("name").getAsString());
                if (a != null) {
                    JsonArray coords = ((JsonObject)atom).get("coords").getAsJsonArray();
                    double x = coords.get(0).getAsDouble(),
                            y = coords.get(1).getAsDouble(),
                            z = coords.get(2).getAsDouble();
                    a.setCoordinates((float)x,(float)y,(float)z);
                }
            }
            residue3Ds.add(residue3D);
        }
        return residue3Ds;
    }

    public List<TertiaryFragmentHit> findJunctions(List<String> query) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonArray junctions = null;

        if (this.mediator.getPDBMongo() == null) { //if no MongoDB linked to Assemble2, we use the webservices
            Map<String,String> data = new Hashtable<String,String>();
            data.put("coll","junctions");
            data.put("query","{\"location\":{\"$size\":"+query.size()+"}}");
            String result = null;
            if (AssembleConfig.getFragmentsLibrary().equals("Redundant"))
                result = this.postData("pdb", data);
            else if (AssembleConfig.getFragmentsLibrary().equals("Non redundant"))
                result = this.postData("rna3dhub", data);
            junctions = jsonParser.parse(result).getAsJsonArray();
            if (junctions.size() == 0) { //perhaps an old version of the database
                data = new Hashtable<String,String>();
                data.put("coll","junctions");
                data.put("query","{\"crown\":{\"$size\":"+query.size()+"}}");
                if (AssembleConfig.getFragmentsLibrary().equals("Redundant"))
                    result = this.postData("pdb", data);
                else if (AssembleConfig.getFragmentsLibrary().equals("Non redundant"))
                    result = this.postData("rna3dhub", data);
                junctions = jsonParser.parse(result).getAsJsonArray();
            }
        } else {
            BasicDBObject _query = new BasicDBObject("crown", new BasicDBObject("$size",query.size()));
            junctions = new JsonArray();
            Iterator<DBObject> it = mediator.getPDBMongo().getCollection("junctions").find(_query).iterator();
            while (it.hasNext())
                junctions.add(jsonParser.parse(it.next().toString()));
        }

        Iterator junctionsIterator = junctions.iterator();

        List<TertiaryFragmentHit> hits = new ArrayList<TertiaryFragmentHit>();
        while (junctionsIterator.hasNext()) {
            JsonObject junction  = (JsonObject)junctionsIterator.next();
            JsonArray location = (JsonArray)junction.get("location");
            if (location == null) //this is an old description of the junction
                location = (JsonArray)junction.get("crown");
            List<Integer[]> fragments = new ArrayList<Integer[]>();
            for (int i=0 ; i < location.size() ; i++) {
                JsonArray fragmentEnds = location.get(i).getAsJsonArray();
                fragments.add(new Integer[]{fragmentEnds.get(0).getAsInt(),fragmentEnds.get(1).getAsInt()});
            }
            List<String> junction_description = Arrays.asList(junction.get("description").getAsString().split(" "));

            if (query.size() == 1 && junction_description.get(0).matches(query.get(0))) {
                String tertiary_structure_id =((JsonObject)junction.get("tertiary-structure")).get("id").getAsString().split("@")[0];
                TertiaryFragmentHit hit = new TertiaryFragmentHit(tertiary_structure_id, junction.get("description").getAsString());
                hit.setPdbID(((JsonObject)junction.get("tertiary-structure")).get("source").getAsString());
                hit.addFragment(new MutablePair<String, Location>(junction.get("molecule").getAsString().split("@")[0], new Location(((JsonArray) location.get(0)).get(0).getAsInt(), ((JsonArray) location.get(0)).get(1).getAsInt())));
                hits.add(hit);
            }
            else {
                List<String> concatenation = new ArrayList<String>();
                concatenation.addAll(junction_description);
                concatenation.addAll(junction_description);
                for (int j=0; j< query.size() ; j++ ) {
                    List<String> description = concatenation.subList(j,j+query.size());
                    int i = 0;
                    while (i < query.size() && description.get(i).matches(query.get(i)))
                        i++;
                    if (i == query.size()) { //we have a hit
                        String tertiary_structure_id = ((JsonObject)junction.get("tertiary-structure")).get("id").getAsString().split("@")[0];
                        TertiaryFragmentHit hit = new TertiaryFragmentHit(tertiary_structure_id, junction.get("description").getAsString());
                        hit.setPdbID(((JsonObject)junction.get("tertiary-structure")).get("source").getAsString());
                        List<Integer[]> _concatenation =  new ArrayList<Integer[]>();
                        _concatenation.addAll(fragments);
                        _concatenation.addAll(fragments);
                        for (Integer[] fragment:_concatenation.subList(j,j+query.size()))
                            hit.addFragment(new MutablePair<String, Location>(junction.get("molecule").getAsString().split("@")[0], new Location(fragment[0], fragment[1])));
                        hits.add(hit);
                        break;
                    }
                }
            }
        }
        return hits;
    }

    public List<TertiaryFragmentHit> findSingleStrandInJunctions(SingleStrand singleStrand) throws Exception {
        List<TertiaryFragmentHit> hits = new ArrayList<TertiaryFragmentHit>();
        JsonArray junctions = null;
        JsonParser jsonParser = new JsonParser();

        if (this.mediator.getPDBMongo() == null) { //if no MongoDB linked to Assemble2, we use the webservices
            Map<String,String> data = new Hashtable<String,String>();
            data.put("coll","junctions");
            String result = null;
            if (AssembleConfig.getFragmentsLibrary().equals("Redundant"))
                result = this.postData("pdb", data);
            else if (AssembleConfig.getFragmentsLibrary().equals("Non redundant"))
                result = this.postData("rna3dhub", data);
            junctions = jsonParser.parse(result).getAsJsonArray();
        } else {
            junctions = new JsonArray();
            Iterator<DBObject> it = mediator.getPDBMongo().getCollection("junctions").find().iterator();
            while (it.hasNext())
                junctions.add(jsonParser.parse(it.next().toString()));
        }

        int length = singleStrand.getLength();
        Iterator junctionsIterator = junctions.iterator();

        while (junctionsIterator.hasNext()) {
            JsonObject junction  = (JsonObject)junctionsIterator.next();
            JsonArray location = (JsonArray)junction.get("location");
            if (location == null) //this is an old description of the junction
                location = (JsonArray)junction.get("crown");
            List<Integer[]> fragments = new ArrayList<Integer[]>();
            for (int i=0 ; i < location.size() ; i++) {
                JsonArray fragmentEnds = location.get(i).getAsJsonArray();
                fragments.add(new Integer[]{fragmentEnds.get(0).getAsInt(),fragmentEnds.get(1).getAsInt()});
            }
            int i=0;
            for (Integer[] fragment : fragments) {
                Location fragment_location = new Location(fragment[0],fragment[1]);
                if (fragment_location.getLength() == length+2) {
                    TertiaryFragmentHit hit = new TertiaryFragmentHit(((JsonObject)junction.get("tertiary-structure")).get("id").getAsString().split("@")[0], junction.get("description").getAsString().split(" ")[i]);
                    hit.setPdbID(((JsonObject)junction.get("tertiary-structure")).get("source").getAsString());
                    MutablePair<String,Location> p = new MutablePair<String, Location>(junction.get("molecule").getAsString().split("@")[0], fragment_location);
                    hit.addFragment(p);
                    hits.add(hit);
                }
                i++;
            }
        }
        return hits;
    }

    public JsonArray getAllncRNAsFromRfam3Ds() throws Exception {
        JsonParser jsonParser = new JsonParser();
        Map<String,String> data = new Hashtable<String,String>();
        data.put("coll","junctions");
        String result = this.postData("rfam3ds", data);
        return jsonParser.parse(result).getAsJsonArray();
    }
}
