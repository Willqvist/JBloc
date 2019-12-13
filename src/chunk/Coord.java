package chunk;

import java.util.Objects;

public class Coord {
    private int x,z;

    public Coord(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public void set(int x,int z){
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coord coord = (Coord) o;
        return x == coord.x &&
                z == coord.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
