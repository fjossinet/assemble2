package fr.unistra.ibmc.assemble2.io.computations;

import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.model.Residue;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.Pair;

import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Rnaplot extends Computation {

    public Rnaplot(Mediator mediator) {
        super(mediator);
    }

    public Pair<Double, Double> plot(SecondaryStructure ss) throws Exception {
        double minX = 0, minY = 0, maxX = 0, maxY = 0;
        String viennaInput = new StringBuffer(">").append(ss.getMolecule().getName().replace('/', '_')).append("\n").append(ss.getMolecule().printSequence()).append("\n").append(ss.printAsBracketNotation()).toString();
        if (AssembleConfig.useLocalAlgorithms()) {
            File dataFile = IoUtils.createTemporaryFile("rnaplot");
            PrintWriter writer = new PrintWriter(dataFile);
            writer.write(viennaInput);
            writer.close();
            File scriptFile = IoUtils.createTemporaryFile("plot.sh");
            writer = new PrintWriter(scriptFile);
            writer.write("#!/bin/bash\n");
            writer.write("cd /data ; RNAplot -o svg < /data/"+dataFile.getName()+"\n");
            writer.close();
            scriptFile.setExecutable(true);
            if (isDockerInstalled() && isAssemble2DockerImageInstalled()) {

                ProcessBuilder pb = new ProcessBuilder("docker", "run", "-v", dataFile.getParent()+":/data", "fjossinet/assemble2", "/data/"+scriptFile.getName());
                Process p = pb.start();
                p.waitFor();


                File[] files = IoUtils.getTmpDirectory().listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".svg");
                    }
                });

                if (files.length == 1) {
                    List<Double[]> coords = FileParser.parseSVG(new FileReader(files[0]));
                    int pos = 0;
                    for (Double[] coord: coords) {
                        pos++;
                        try {
                            Residue residue = ss.getResidue(pos);
                            residue.setRealCoordinates(coord[0],coord[1]);
                            if (residue.getX() < minX )
                                minX = residue.getX();
                            if (residue.getY() < minY)
                                minY = residue.getY();
                            if (residue.getX() > maxX )
                                maxX = residue.getX();
                            if (residue.getY() > maxY)
                                maxY = residue.getY();
                        }
                        catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                    //the drawing should be located after the (0,0) point
                    for (Residue residue:ss.getResidues()) {
                        residue.setRealCoordinates(residue.getX()+Math.abs(minX), residue.getY()+Math.abs(minY));
                    }
                    ss.setPlotted(true);
                    files[0].delete();

                    return new Pair<Double, Double>(maxX - minX + 1, maxY - minY + 1);

                }

            }
            return null;
        } else {
            Map<String, String> data = new Hashtable<String, String>();
            data.put("data", viennaInput);
            data.put("tool", "rnaplot");
            String coords = this.postData("compute/2dplot", data);
            if (coords != null && coords.length() != 0) {
                BasicDBList coords2D = (BasicDBList) JSON.parse(coords);
                Iterator it = coords2D.iterator();
                int pos = 0;
                while (it.hasNext()) {
                    BasicDBList xy = (BasicDBList) it.next();
                    pos++;
                    try {
                        Residue r = ss.getResidue(pos);
                        double x = 0, y = 0;
                        if (Integer.class.isInstance(xy.get(0)))
                            x = (Integer) xy.get(0);
                        else if (Double.class.isInstance(xy.get(0)))
                            x = (Double) xy.get(0);
                        if (Integer.class.isInstance(xy.get(1)))
                            y = (Integer) xy.get(1);
                        else if (Double.class.isInstance(xy.get(1)))
                            y = (Double) xy.get(1);
                        r.setRealCoordinates(x, y);
                        if (r.getX() < minX)
                            minX = r.getX();
                        if (r.getY() < minY)
                            minY = r.getY();
                        if (r.getX() > maxX)
                            maxX = r.getX();
                        if (r.getY() > maxY)
                            maxY = r.getY();
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
                //the drawing should be located after the (0,0) point
                for (Residue r : ss.getResidues())
                    r.setRealCoordinates(r.getX() + Math.abs(minX), r.getY() + Math.abs(minY));
                ss.setPlotted(true);
            }
            return new Pair<Double, Double>(maxX - minX + 1, maxY - minY + 1);
        }
    }
}

