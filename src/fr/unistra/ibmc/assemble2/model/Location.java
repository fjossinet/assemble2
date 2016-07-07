package fr.unistra.ibmc.assemble2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Location implements Serializable {

    private List<Block> blocks;

    public Location() {
        this.blocks = new ArrayList<Block>();
    }

    /**
     * Return all the single positions in this location ordered from the start to the end
     * @return
     */
    public int[] getSinglePositions() {
        int[] positions = new int[this.getLength()];
        int count=0;
        for (Block b:this.blocks)
            for (int i=b.start; i<=b.end;i++)
                positions[count++]=i;
        return positions;
    }

    /**
     * Construct a location with A string as description
     *
     * @param location the description of the location. For example: 1-20,30:6, 234-456,1218 means positions from 1 to 20 and 30 to 35 and 234 to 456 and 1218.
     */
    public Location(String location) throws Exception {
        this();
        String[] blocks = location.trim().split(",");
        String[] ends = null, _ends = null;
        try {
            for (String b : blocks) {
                ends = b.split("-");
                _ends = b.split(":");
                if (ends.length == 2)
                    this.addBlock(new Block(Integer.parseInt(ends[0]), Integer.parseInt(ends[1])));
                else if (_ends.length == 2)
                    this.addBlock(new Block(Integer.parseInt(_ends[0]), Integer.parseInt(_ends[0])+Integer.parseInt(_ends[1])-1));
                else if (ends.length == 1 && _ends.length == 1)
                    this.addBlock(new Block(Integer.parseInt(_ends[0]), Integer.parseInt(_ends[0])));
                else
                    throw new Exception("Unknown location description "+location);
            }
        }
        catch (NumberFormatException e) {
            System.out.println(location);
            throw new Exception("The description of the location contains an alpha character"+location);
        }
    }

    public Location(int pos) {
        this(pos, pos);
    }

    /**
     * The Location contains all the positions between start and end
     *
     * @param start
     * @param end
     */
    public Location(int start, int end) {
        this();
        if (start != 0 && end != 0)
            this.addBlock(new Block(start, end));
    }

    public Location(Location l1, Location l2) {
        this();
        for (Block b : l1.blocks)
            this.addBlock(new Block(b));
        for (Block b : l2.blocks)
            this.addBlock(new Block(b));
    }

    public Location(Location l) {
        this();
        for (Block b : l.blocks)
            this.addBlock(new Block(b));
    }

    protected Location(List<Block> blocks) {
        this();
        for (Block b : blocks)
            this.addBlock(new Block(b));
    }

    /**
     * Return a new Location from the intersection with the Location given as argument
     * Intersection means all the positions in common between the two Location objects.
     *
     * @param l
     * @return
     */
    public Location intersectionOf(Location l) {
        Location new_location = new Location();
        for (Block b1 : this.blocks)
            for (Block b2 : l.blocks)
                if (b1.intersect(b2)) {
                    //System.out.println(b1+" intersects "+b2);
                    //System.out.println("Result: "+b1.intersectionOf(b2));
                    new_location.addBlock(b1.intersectionOf(b2));
                }
        /*
 else
     System.out.println(b1+" not intersects "+b2);*/
        return new_location;
    }

    public boolean hasAtLeastOnePositionInCommonWith(Location l) {
        int[] boundaries = this.getEnds();
        for (int i = 0; i < boundaries.length - 1; i += 2) {
            for (int j = boundaries[i]; j <= boundaries[i + 1]; j++)
                if (l.hasPosition(j)) {
                    return true;
                }
        }
        return false;
    }

    /**
     * Return a new Location from the union of the current Location with the Location given as argument.
     * Union means the sum of all the positions found in the two Location objects.
     *
     * @param l
     * @return
     */
    public Location unionOf(Location l) {
        Location new_location = new Location();
        for (Block b1 : this.blocks)
            new_location.addBlock(new Block(b1));
        for (Block b2 : l.blocks)
            new_location.addBlock(new Block(b2));
        return new_location;
    }


    /**
     * Return a new Location from the difference of the current Location with the Location given as argument
     * Difference means all the positions not found in the Location given as argument
     *
     * @param l
     * @return
     */
    public Location differenceOf(Location l) {
        Location new_location = new Location();
        for (Block b1 : this.blocks) {
            BLOCK:
            for (int i = b1.start; i <= b1.end; i++) {
                for (Block b2 : l.blocks)
                    if (b2.contains(i))
                        continue BLOCK;
                new_location.addBlock(new Block(i, i));
            }
        }
        return new_location;
    }

    /**
     * Return the ends for this Location
     * For example, the Location 1-12,23-37 has for ends : 1, 12, 23, 37
     *
     * @return
     */
    public int[] getEnds() {
        int[] ends = new int[this.blocks.size() * 2];
        int i = 0;
        for (Block b : this.blocks) {
            ends[i] = b.start;
            ends[i + 1] = b.end;
            i += 2;
        }
        return ends;
    }

    /**
     * Add the Location given as argument to the current Location object
     *
     * @param l
     */
    public void add(Location l) {
        for (Block b : l.blocks)
            this.addBlock(new Block(b));
    }

    /**
     * Add a new contiguous region to the current Location object
     *
     * @param start
     * @param end
     */
    public void add(int start, int end) {
        this.addBlock(new Block(start, end));
    }

    public void add(int pos) {
        this.add(pos, pos);
    }

    public void remove(int pos) {
        this.remove(new Location(pos,pos));
    }

    public int getLength() {
        int length = 0;
        for (Block b : this.blocks)
            length += b.getLength();
        return length;
    }

    /**
     * Remove the Location given as argument from the current Location object
     *
     * @param l
     */
    public void remove(Location l) {
        Location difference = this.differenceOf(l);
        this.blocks.clear();
        this.add(difference);
    }

    public boolean intersect(Location l) {
        return (l.getStart() >= this.getStart() && l.getStart() <= this.getEnd()) || (l.getEnd() >= this.getStart() && l.getEnd() <= this.getEnd());
    }

    public boolean contains(Location l) {
        return l.getStart() >= this.getStart() && l.getEnd() <= this.getEnd();
    }

    public boolean strictlyContains(Location l) {
        for (Block b1 : this.blocks)
            for (Block b2 : l.blocks)
                if (!b1.contains(b2))
                    return false;
        return true;
    }

    public boolean hasPosition(int pos) {
        for (Block b : this.blocks)
            if (pos >= b.start && pos <= b.end)
                return true;
        return false;
    }

    public boolean hasPosition(Location location) {
        for (Block sourceBlock : location.blocks) {
            for (Block b : this.blocks)
                if (!sourceBlock.isIncludedIn(b))
                    return false;
        }
        return true;
    }

    /**
     * Included or equal
     *
     * @param l
     * @return
     */
    public boolean isIncludedIn(Location l) {
        return l.getStart() <= this.getStart() && l.getEnd() >= this.getEnd();
    }

    /**
     * Only included
     *
     * @param l
     * @return
     */
    public boolean isStrictlyIncludedIn(Location l) {
        return this.isIncludedIn(l) && !this.matchWith(l);
    }

    public boolean isIndependantWith(Location l) {
        return !(this.intersect(l) || this.contains(l) || this.isIncludedIn(l));
    }

    public boolean matchWith(Location l) {
        return this.matchWith(this.blocks, l.blocks);
    }

    public int getStart() {
        return this.blocks.size() == 0 ? 0 : this.blocks.get(0).start;
    }

    public int getEnd() {
        return this.blocks.size() == 0 ? 0 : this.blocks.get(this.blocks.size() - 1).end;
    }

    /**
     * Test if the Location is made with only one position
     *
     * @return
     */
    public boolean isSinglePosition() {
        return this.blocks.size() == 1 && this.blocks.get(0).isSinglePosition();
    }

    public boolean isEmpty() {
        return this.blocks.size() == 0;
    }

    /**
     * Format the location with the absolute numbering system
     * @return
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        if (this.isEmpty())
            return buff.append("EMPTY").toString();
        else if (this.isSinglePosition())
            return buff.append(this.getStart()).toString();
        for (Block b : this.blocks) {
            if (b.isSinglePosition())
                buff.append(b.start);
            else {
                buff.append(b.start);
                buff.append(":");
                buff.append(b.end-b.start+1);
            }
            if (this.blocks.indexOf(b) != this.blocks.size()-1)
                buff.append(",");
        }
        return buff.toString();
    }

    /**
     * Format the location with a given numbering system for the residues
     * @return
     */
    public String toString(Map<Integer,String> residuesLabels) {
        StringBuffer buff = new StringBuffer();
        if (this.isEmpty())
            return buff.append("EMPTY").toString();
        else if (this.isSinglePosition())
            return buff.append(residuesLabels.get(this.getStart())).toString();
        for (Block b : this.blocks) {
            if (b.isSinglePosition())
                buff.append(residuesLabels.get(b.start));
            else {
                buff.append(residuesLabels.get(b.start));
                buff.append(":");
                buff.append(b.end-b.start+1);
            }
            if (this.blocks.indexOf(b) != this.blocks.size()-1)
                buff.append(",");
        }
        return buff.toString();
    }


    private boolean matchWith(List<Block> blocks_1, List<Block> blocks_2) {
        //If at least two blocks in the lists don't match, then the two lists don't match
        if (blocks_1.isEmpty() || blocks_2.isEmpty())
            return false;
        for (Block b1 : blocks_1)
            for (Block b2 : blocks_2)
                if (!b1.matchWith(b2))
                    return false;
        return true;
    }

    /**
     * To add a new Block in the list of Blocks of the Location object, this method is used instead to add it directly to the List<Block> blocks attribute.
     * This method update the start and end attribute each time a new Block is added.
     * The new Block is placed inside the Blocks list according to its start and end boundaries. And the method tests if the new block can merge with a Block already registered
     *
     * @param newBlock
     */
    protected void addBlock(Block newBlock) {
        //where to insert the new Block object in the list of Blocks holded by the Location object ?
        List<Block> blocksToRemoved = new ArrayList<Block>();
        Block bl = null;
        int i = 0;
        boolean merged = false;
        for (; i < this.blocks.size(); i++) {
            bl = this.blocks.get(i);
            if (newBlock.isBefore(bl) && !newBlock.isBeside(bl))
                break;
            else if (newBlock.intersect(bl) || newBlock.isBeside(bl)) {
                //System.out.println("merge");
                newBlock.merge(bl);
                blocksToRemoved.add(bl);
                merged = true;
                //its necessary to continue to see if the new Block can merge with other blocks
                continue;
            } else if (merged)
                break;
        }
        if (merged) {
            this.blocks.add(i, newBlock);
            this.blocks.removeAll(blocksToRemoved);
        } else if (i == this.blocks.size())
            this.blocks.add(newBlock);
        else
            this.blocks.add(i, newBlock);
    }

    /**
     * Return a new Location translated from the range value
     *
     * @param range
     * @return
     */
    public Location translate(int range) {
        List<Block> new_blocks = new ArrayList<Block>(this.blocks.size());
        for (Block b : this.blocks)
            new_blocks.add(new Block(b.start + range, b.end + range));
        return new Location(new_blocks);
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!Location.class.isInstance(o))
            return false;
        Location otherLocation = (Location) o;
        if (otherLocation.getEnds().length != this.getEnds().length)
            return false;
        for (int i = 0; i < this.getEnds().length; i++)
            if (this.getEnds()[i] != otherLocation.getEnds()[i])
                return false;
        return true;
    }

    public int hashCode() {
        StringBuffer buffer = new StringBuffer();
        for (int i : this.getEnds()) {
            buffer.append(i);
            buffer.append(",");
        }
        return buffer.toString().hashCode();
    }


    /**
     * Define a contiguous set of positions
     *
     * @author fjossinet
     */

    private final class Block implements Serializable {
        private int start, end;

        private Block(int start, int end) {
            if (start < end) {
                this.start = start;
                this.end = end;
            } else {
                this.start = end;
                this.end = start;
            }
        }

        private Block(Block b) {
            this.start = b.start;
            this.end = b.end;
        }

        private int getLength() {
            return this.end - this.start + 1;
        }

        private boolean matchWith(Block b) {
            return b.start == this.start && b.end == this.end;
        }

        private boolean isIncludedIn(Block b) {
            return this.start >= b.start && this.end <= b.end;
        }

        private boolean intersect(Block b) {
            return (b.start >= this.start && b.start <= this.end) || (b.end >= this.start && b.end <= this.end) || b.isIncludedIn(this) || this.isIncludedIn(b);
        }

        private boolean isBefore(Block b) {
            return this.end < b.start;
        }

        private boolean isAfter(Block b) {
            return this.start > b.end;
        }

        public boolean isBeside(Block bl) {
            return (this.start - 1 == bl.end || this.end + 1 == bl.start || bl.start - 1 == this.end || bl.end + 1 == this.start);
        }

        private void merge(Block b) {
            if (b.start < this.start)
                this.start = b.start;
            if (b.end > this.end)
                this.end = b.end;
        }

        /**
         * Return a new Block resulting from the intersection with the Block given as argument
         *
         * @param b
         * @return null if no intersection
         */
        private Block intersectionOf(Block b) {
            if (this.isIncludedIn(b))
                return new Block(this.start, this.end);
            else if (b.isIncludedIn(this))
                return new Block(b.start, b.end);
            else if (b.start >= this.start && b.start <= this.end)
                return new Block(b.start, this.end);
            else if (b.end >= this.start && b.end <= this.end)
                return new Block(this.start, b.end);
            else
                return null;
        }

        private boolean isSinglePosition() {
            return this.start == this.end;
        }

        public String toString() {
            return "block " + this.start + "-" + this.end;
        }

        public boolean contains(int position) {
            return position >= this.start && position <= this.end;
        }

        public boolean contains(Block b) {
            return b.start >= this.start && b.end <= this.end;
        }

        /*
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!Block.class.isInstance(o))
                return false;
            Block otherBlock = (Block)o;
            return otherBlock.start == this.start && otherBlock.end == this.end;
        }

        public int hashCode() {
            return new StringBuffer().append(this.start).append("-").append(this.end).toString().hashCode();
        }*/
    }

    public static void main(String[] args) {
        Location l = new Location();
        l.add(new Location(1, 2));
        l.remove(new Location(1, 2));
        System.out.println(l);
    }
}

