package fr.unistra.ibmc.assemble2.io.computations;

import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.model.Residue;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import fr.unistra.ibmc.assemble2.utils.Pair;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class Rnaplot extends Computation {

    public Rnaplot(Mediator mediator) {
        super(mediator);
    }

    public Pair<Double, Double> plot(SecondaryStructure ss) throws Exception {
        String viennaInput = new StringBuffer(">").append(ss.getMolecule().getName().replace('/', '_')).append("\n").append(ss.getMolecule().printSequence()).append("\n").append(ss.printAsBracketNotation()).toString();
        double minX = 0, minY = 0, maxX = 0, maxY = 0;
        Map<String,String> data = new Hashtable<String,String>();
        data.put("data", viennaInput);
        data.put("tool","rnaplot");
        String coords = this.postData("compute/2dplot", data);
        if (coords != null && coords.length() != 0) {
            BasicDBList coords2D = (BasicDBList)JSON.parse(coords);
            Iterator it = coords2D.iterator();
            int pos = 0;
            while (it.hasNext()) {
                BasicDBList xy = (BasicDBList)it.next();
                pos++;
                try {
                    Residue r = ss.getResidue(pos);
                    double x = 0, y = 0;
                    if (Integer.class.isInstance(xy.get(0)))
                        x =  (Integer)xy.get(0);
                    else if (Double.class.isInstance(xy.get(0)))
                        x =  (Double)xy.get(0);
                    if (Integer.class.isInstance(xy.get(1)))
                        y =  (Integer)xy.get(1);
                    else if (Double.class.isInstance(xy.get(1)))
                        y =  (Double)xy.get(1);
                    r.setRealCoordinates(x,y);
                    if (r.getX() < minX )
                        minX = r.getX();
                    if (r.getY() < minY)
                        minY = r.getY();
                    if (r.getX() > maxX )
                        maxX = r.getX();
                    if (r.getY() > maxY)
                        maxY = r.getY();
                }
                catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
            //the drawing should be located after the (0,0) point
            for (Residue r:ss.getResidues())
                r.setRealCoordinates(r.getX()+Math.abs(minX),r.getY()+Math.abs(minY));
            ss.setPlotted(true);
        }
        return new Pair<Double, Double>(maxX-minX+1, maxY-minY+1);
    }
}

