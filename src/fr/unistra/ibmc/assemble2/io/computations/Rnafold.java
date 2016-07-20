package fr.unistra.ibmc.assemble2.io.computations;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.model.Location;
import fr.unistra.ibmc.assemble2.model.Molecule;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.*;
import java.util.*;

public class Rnafold extends Computation {

    public Rnafold(Mediator mediator) {
        super(mediator);
    }

    public SecondaryStructure fold(Molecule m) throws Exception {
        String sequenceInput = ">" + m.getName() + "\n" + m.printSequence();
        if (AssembleConfig.useLocalAlgorithms()) {
            File dataFile = IoUtils.createTemporaryFile("rnafold");
            PrintWriter writer = new PrintWriter(dataFile);
            writer.write(sequenceInput);
            writer.close();
            File scriptFile = IoUtils.createTemporaryFile("fold.sh");
            writer = new PrintWriter(scriptFile);
            writer.write("#!/bin/bash\n");
            writer.write("cd /data ; RNAfold < /data/"+dataFile.getName()+"\n");
            writer.close();
            scriptFile.setExecutable(true);

            ProcessBuilder pb = new ProcessBuilder("docker", "run", "-v", dataFile.getParent()+":/data", "fjossinet/assemble2", "/data/"+scriptFile.getName());
            Process p = pb.start();
            p.waitFor();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            SecondaryStructure ss = FileParser.parseVienna(new StringReader(builder.toString()), mediator);
            ss.setSource("RNAfold");
            ss.setMolecule(m);
            return ss;

        } else {

            Map<String, String> data = new Hashtable<String, String>();
            data.put("data", sequenceInput);
            data.put("tool", "rnafold");
            String _2DPrediction = this.postData("compute/2d", data);
            if (_2DPrediction != null && _2DPrediction.length() != 0) {
                BasicDBObject secondaryStructure = null;
                if (BasicDBObject.class.isInstance(JSON.parse(_2DPrediction)))//to keep the compatibility with previous versions of the webservice that returns only a single 2D
                    secondaryStructure = (BasicDBObject) JSON.parse(_2DPrediction);
                else
                    secondaryStructure = (BasicDBObject) ((BasicDBList) JSON.parse(_2DPrediction)).get(0);
                Iterator helices = ((BasicDBList) secondaryStructure.get("helices")).iterator(),
                        tertiaryInteractions = ((BasicDBList) secondaryStructure.get("tertiaryInteractions")).iterator();
                List<Location> helicalLocations = new ArrayList<Location>();
                List<MutablePair<Location, String>> tertiaryInteractionLocations = new ArrayList<MutablePair<Location, String>>();
                while (helices.hasNext()) {
                    BasicDBObject helix = (BasicDBObject) helices.next();
                    Object location = helix.get("location");

                    BasicDBList ends = null;
                    if (BasicDBObject.class.isInstance(location))
                        ends = (BasicDBList) ((BasicDBObject) location).get("ends");
                    else
                        ends = (BasicDBList) location;
                    BasicDBList strand1 = (BasicDBList) ends.get(0), strand2 = (BasicDBList) ends.get(1);
                    helicalLocations.add(new Location(new Location((Integer) strand1.get(0), (Integer) strand1.get(1)), new Location((Integer) strand2.get(0), (Integer) strand2.get(1))));
                }
                while (tertiaryInteractions.hasNext()) {
                    BasicDBObject tertiaryInteraction = (BasicDBObject) tertiaryInteractions.next();
                    Object location = tertiaryInteraction.get("location");

                    BasicDBList ends = null;
                    if (BasicDBObject.class.isInstance(location))
                        ends = (BasicDBList) ((BasicDBObject) location).get("ends");
                    else
                        ends = (BasicDBList) location;

                    BasicDBList edge1 = (BasicDBList) ends.get(0), edge2 = (BasicDBList) ends.get(1);
                    String type = tertiaryInteraction.get("orientation") + "" + tertiaryInteraction.get("edge1") + "" + tertiaryInteraction.get("edge2");
                    tertiaryInteractionLocations.add(new MutablePair<Location, String>(new Location(new Location((Integer) edge1.get(0)), new Location((Integer) edge2.get(0))), type.toUpperCase()));
                }
                SecondaryStructure ss = new SecondaryStructure(mediator, m, helicalLocations, new ArrayList<MutablePair<Location, String>>(), tertiaryInteractionLocations);
                ss.setSource("RNAfold");
                ss.setMolecule(m);
                return ss;
            }
            return null;
        }
    }
}
