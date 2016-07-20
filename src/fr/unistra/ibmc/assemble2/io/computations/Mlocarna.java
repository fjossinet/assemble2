package fr.unistra.ibmc.assemble2.io.computations;


import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.model.AlignedMolecule;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.Pair;

import java.io.*;
import java.util.*;

public class Mlocarna extends Computation {

    public static boolean useForFoldingLandscape = false;

    public Mlocarna(Mediator mediator) {
        super(mediator);
    }

    public Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> align(String fastaContent, String referenceMoleculeId) throws Exception {
        if (AssembleConfig.useLocalAlgorithms()) {
            File dataFile = IoUtils.createTemporaryFile("mlocarna");
            PrintWriter writer = new PrintWriter(dataFile);
            writer.write(fastaContent);
            writer.close();

            ProcessBuilder pb = new ProcessBuilder("docker", "run", "-v", dataFile.getParent()+":/data", "fjossinet/assemble2", "mlocarna", "/data/"+dataFile.getName());
            Process p = pb.start();
            p.waitFor();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            StringBuffer output = new StringBuffer();
            boolean save = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Perform progressive alignment ..."))
                    save = true;
                else if (save) {
                    if (line.startsWith("alifold"))
                        output.append("2D\t"+line.split("\\s+")[1]+"\n");
                    else
                        output.append(line+"\n");
                }
            }
            System.out.println(output.toString().trim());
            Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> result =  FileParser.parseClustal(new StringReader(output.toString().trim()), mediator, referenceMoleculeId);
            for (SecondaryStructure ss: result.getFirst().getSecond())
                ss.setSource("Mlocarna");
            return result;
        } else {
            Map<String, String> data = new Hashtable<String, String>();
            data.put("data", fastaContent);
            data.put("tool", "mlocarna");
            String _2DPrediction = this.postData("compute/2d", data);
            System.out.println(_2DPrediction);
            if (_2DPrediction != null && _2DPrediction.length() != 0) {
                Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> result = FileParser.parseClustal(new StringReader(_2DPrediction), mediator, referenceMoleculeId);
                for (SecondaryStructure ss : result.getFirst().getSecond())
                    ss.setSource("Mlocarna");
                return result;
            }
            return null;
        }
    }

}
