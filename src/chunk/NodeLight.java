package chunk;

import java.util.Objects;

public class NodeLight {
    private int x,y,z,val,type;
    private Chunk c;

    public NodeLight(int x, int y, int z, int val, int type, Chunk c) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.val = val;
        this.type = type;
        this.c = c;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getVal() {
        return val;
    }

    public int getType() {
        return type;
    }

    public Chunk getC() {
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeLight nodeLight = (NodeLight) o;
        return x == nodeLight.x &&
                y == nodeLight.y &&
                z == nodeLight.z &&
                type == nodeLight.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, type);
    }
}
