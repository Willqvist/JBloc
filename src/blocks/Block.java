package blocks;

import chunk.Faces;
import engine.physics.AABB;
import engine.texture.TextureAtlas;
import engine.texture.TextureCoordinate;
import engine.texture.TextureLoader;

public abstract class Block implements IBlock {
    private static final int NUM_BLOCKS=200;
    private static Block[] blocks = new Block[NUM_BLOCKS];
    public static TextureAtlas texture;
    private TextureCoordinate tex;
    protected short id=0;
    private static AABB ab = new AABB(0,0,0,1,1,1);

    public Block(short id,TextureCoordinate textureCoordinate){
        this.tex = textureCoordinate;
        this.id=id;
    }
    public Block(short id){
        this.id=id;
    }

    @Override
    public float[] getFaceData(BlockFace face) {
        switch(face) {
            case BOTTOM: return Faces.BOTTOM;
            case LEFT: return Faces.LEFT;
            case FRONT: return Faces.FRONT;
            case BACK: return Faces.BACK;
            case RIGHT: return Faces.RIGHT;
            case TOP: return Faces.TOP;
        }
        return null;
    }

    @Override
    public TextureCoordinate getFaceTexture(BlockFace face) {
        return tex;
    }

    @Override
    public short getId() {
        return id;
    }

    public static Block getBlock(short id){
        return blocks[id];
    }

    private static void addBlock(Block block){
        blocks[block.getId()] = block;
    }

    public static short AIR = 0;
    public static short DIRT = 1;
    public static short GRASS = 2;
    public static void init(){
        texture = TextureLoader.loadAtlas("blocks.png",16);
        addBlock(new AirBlock());
        addBlock(new BasicBlock(DIRT,TextureCoordinate.from(0,0, texture)));
        addBlock(new GrassBlock());
    }

    public abstract boolean isSolid();

    public AABB getCollisionBox(int x, int y, int z) {
        return ab.move(x,y,z);
    }

    /**
     * max 15, min 0
     * @return
     */
    public abstract int getLightPenetration();
}
