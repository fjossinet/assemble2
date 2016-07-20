package fr.unistra.ibmc.assemble2.io.computations;

import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.model.AlignedMolecule;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.Pair;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Rnalifold extends Computation {

    public Rnalifold(Mediator mediator) {
        super(mediator);
    }

    public Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> fold(String clustalwWithoutBN) throws Exception {

        StringBuffer buffer = new StringBuffer();

        if (!clustalwWithoutBN.split("\n")[0].matches(".*CLUSTAL.*")) {
            buffer.append("CLUSTALW\n");
        }

        buffer.append(clustalwWithoutBN);

        String[] result = null;

        if (AssembleConfig.useLocalAlgorithms()) {
            return null;
        } else {

            Map<String, String> data = new Hashtable<String, String>();
            data.put("data", buffer.toString());
            data.put("tool", "rnalifold");
            String _2DPrediction = this.postData("compute/2d", data);
            if (_2DPrediction != null && _2DPrediction.length() != 0) {
                result = this.postData("compute/2d", data).split("\n");
            }
            String bn = result[result.length - 1].split("\\s+")[0];
            String clustalwWithBN = new StringBuffer(clustalwWithoutBN).append("\n").append("2D\t").append(bn).append("\n").toString();
            return FileParser.parseClustal(new StringReader(clustalwWithBN), mediator, null);
        }
    }
}
