package fr.unistra.ibmc.assemble2.model;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class Annotation {
    private int start, end;
    private DBObject basicDBObject;

    public Annotation(DBObject basicDBObject) {
        this.basicDBObject = basicDBObject;
        BasicDBList genomicPositions = (BasicDBList) basicDBObject.get("genomicPositions");
        this.start = (Integer)genomicPositions.get(0);
        this.end = (Integer)genomicPositions.get(1);
    }

    public DBObject getBasicDBObject() {
        return this.basicDBObject;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Double getScore() {
        return (Double)this.basicDBObject.get("score");
    }

    public boolean isPlusOrientation() {
        return this.basicDBObject.get("genomicStrand").equals("+");
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return this.end-this.start+1;
    }

    public String get_id() {
        return (String)this.basicDBObject.get("_id");
    }

    public String getAnnotationClass() {
        return (String)this.basicDBObject.get("class");
    }
}
