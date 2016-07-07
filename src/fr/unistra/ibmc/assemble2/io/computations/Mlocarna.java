package fr.unistra.ibmc.assemble2.io.computations;


import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.model.AlignedMolecule;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.Pair;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;

public class Mlocarna extends Computation {

    public static boolean useForFoldingLandscape = false;

    public Mlocarna(Mediator mediator) {
        super(mediator);
    }

    public Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> align(String fastaContent, String referenceMoleculeId) throws Exception {
        Map<String,String> data = new Hashtable<String,String>();
        data.put("data", fastaContent);
        data.put("tool","mlocarna");
        String _2DPrediction= this.postData("compute/2d", data);
        if (_2DPrediction != null && _2DPrediction.length() != 0) {
            Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> result =  FileParser.parseClustal(new StringReader(_2DPrediction), mediator, referenceMoleculeId);
            for (SecondaryStructure ss: result.getFirst().getSecond())
                ss.setSource("Mlocarna");
            return result;
        }
        return null;
    }

}
