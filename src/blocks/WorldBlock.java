package blocks;

import engine.physics.AABB;

public class WorldBlock {
    private int x,y,z;
    private short block;

    public WorldBlock(int x, int y, int z, short block) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }

    public AABB getBoundingBox(){
        return Block.getBlock(block).getCollisionBox(x,y,z);
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

    public short getBlock() {
        return block;
    }
}