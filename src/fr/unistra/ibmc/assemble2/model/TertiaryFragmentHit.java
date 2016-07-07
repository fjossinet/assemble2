package fr.unistra.ibmc.assemble2.model;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class TertiaryFragmentHit {
    protected String tertiaryStructureId;
    protected List<MutablePair<String,Location>> fragments;
    protected String description, pdbID;

    public TertiaryFragmentHit(String tertiaryStructureId, String description) {
        this.tertiaryStructureId = tertiaryStructureId;
        this.fragments = new ArrayList<MutablePair<String, Location>>();
        this.description = description;
    }

    public String getPdbID() {
        return pdbID;
    }

    public void setPdbID(String pdbID) {
        this.pdbID = pdbID;
    }

    public void addFragment(MutablePair<String,Location> fragment) {
        this.fragments.add(fragment);
    }

    public String getTertiaryStructureId() {
        return tertiaryStructureId;
    }

    public List<MutablePair<String, Location>> getFragments() {
        return fragments;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return this.getDescription();
    }
}
