public class Hexagon {

    private int index;
    private Coordinates center;
    private Coordinates[] neighbors;
    private int rotationIndex;
    private int ring;

    public Hexagon(Coordinates center, int index) {
        this.center = center;
        this.index = index;
        this.neighbors = new Coordinates[6];
    }

    public Hexagon() {
        //TODO Auto-generated constructor stub
    }

    public int getIndex() {
        return index;
    }

    public Coordinates getCenter() {
        return center;
    }

    public Coordinates[] getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Coordinates[] neighbors) {
        this.neighbors = neighbors;
    }

    public void addNeighbor(Coordinates neighbor) {
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] == null) {
                neighbors[i] = neighbor;
                break;
            }
        }
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setCenter(Coordinates center) {
        this.center = center;
    }

    public void setRotationIndex(int rotationIndex) {
        this.rotationIndex = rotationIndex;
    }

    public int getRotationIndex() {
        return rotationIndex;
    }

    public void setRing(int ring) {
        this.ring = ring;
    }

    public int getRing() {
        return ring;
    }

}
