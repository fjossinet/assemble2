package fr.unistra.ibmc.assemble2.model;


public abstract class AbstractAtomAtomInteraction implements AtomAtomInteraction {

    protected String a1, a2;
    protected Location location;

    protected AbstractAtomAtomInteraction(String a1, String a2, Location location) {
        this.a1 = a1;
        this.a2 = a2;
        this.location = location;
    }

    public String getAtom1() {
        return a1;
    }

    public String getAtom2() {
        return a2;
    }

    public Location getLocation() {
        return location;
    }
}
