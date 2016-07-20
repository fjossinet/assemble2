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

public class Rnasubopt extends Computation {

    public static int RANDOM_SAMPLE = 10;

    public Rnasubopt(Mediator mediator) {
        super(mediator);
    }

    public List<SecondaryStructure> fold(Molecule m) throws Exception {
        String sequenceInput = ">" + m.getName() + "\n" + m.printSequence();
        List<SecondaryStructure> secondaryStructures = new ArrayList<SecondaryStructure>();
        if (AssembleConfig.useLocalAlgorithms()) {
            File dataFile = IoUtils.createTemporaryFile("rnasubopt");
            PrintWriter writer = new PrintWriter(dataFile);
            writer.write(sequenceInput);
            writer.close();
            File scriptFile = IoUtils.createTemporaryFile("fold.sh");
            writer = new PrintWriter(scriptFile);
            writer.write("#!/bin/bash\n");
            writer.write("cd /data ; RNAsubopt -p "+RANDOM_SAMPLE+" < /data/"+dataFile.getName()+"\n");
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
            String header = null;
            for (String l: builder.toString().trim().split("\n")) {
                if (l.startsWith(">"))
                    header = l;
                else     {
                    StringReader r = new StringReader(header+'\n'+m.printSequence()+"\n"+l);
                    SecondaryStructure ss = FileParser.parseVienna(r, mediator);
                    ss.setSource("RNAsubopt");
                    ss.setMolecule(m);
                    secondaryStructures.add(ss);
                }
            }
            return secondaryStructures;
        } else {
            Map<String, String> data = new Hashtable<String, String>();
            data.put("data", sequenceInput);
            data.put("tool", "rnasubopt");
            data.put("random_sample", "" + RANDOM_SAMPLE);
            String _2DPredictions = this.postData("compute/2d", data);
            if (_2DPredictions != null && _2DPredictions.length() != 0) {
                for (Object _2DPrediction : (BasicDBList) JSON.parse(_2DPredictions)) {
                    BasicDBObject secondaryStructure = (BasicDBObject) _2DPrediction;
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
                    ss.setSource("RNAsubopt");
                    ss.setMolecule(m);
                    secondaryStructures.add(ss);
                }
            }
            return secondaryStructures;
        }
    }
}
