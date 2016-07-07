package fr.unistra.ibmc.assemble2.gui;


import com.mongodb.BasicDBObject;

import java.util.List;

public interface GenomicDataHandler {

    public BasicDBObject getGenome(String _id);

    public BasicDBObject getAlignment(String _id);

    public BasicDBObject getncRNA(String _id);

    public List<BasicDBObject> getAnnotationsPerGenome(String _id);

    public List<BasicDBObject> getncRNAsPerGenome(String _id);

    public void iWantMoreResiduesOnTheLeft();

    public void iWantMoreResiduesOnTheRight();
}
