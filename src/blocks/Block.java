package blocks;

import blocks.models.BasicModel;
import blocks.models.BlockModel;
import blocks.models.TorchModel;
import chunk.Faces;
import engine.physics.AABB;
import engine.texture.TextureAtlas;
import engine.texture.TextureCoordinate;
import engine.texture.TextureLoader;

public abstract class Block implements IBlock {
    private static final int NUM_BLOCKS=200;
    private static Block[] blocks = new Block[NUM_BLOCKS];
    private static BlockModel model = new BasicModel();
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
        return getModel().getModelFaces(face);
    }

    @Override
    public BlockModel getModel() {
        return model;
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


    public static final short AIR = 0;
    public static final short DIRT = 1;
    public static final short GRASS = 2;
    public static final short WATER = 3;
    public static final short STONE = 4;
    public static final short SAND = 5;
    public static final short LOG = 6;
    public static final short LEAF = 7;
    public static final short TORCH = 8;
    public static void init(){
        texture = TextureLoader.loadAtlas("blocks.png",16);
        addBlock(new AirBlock());
        addBlock(new GrassBlock());
        addBlock(new BlockLog(LOG,TextureCoordinate.from(4,1,texture)));
        addBlock(new BasicBlock(DIRT,TextureCoordinate.from(2,0, texture)));
        addBlock(new BasicBlock(SAND,TextureCoordinate.from(2,1, texture)));
        addBlock(new BasicBlock(STONE,TextureCoordinate.from(1,0, texture)));
        addBlock(new BasicBlock(WATER,TextureCoordinate.from(13,12, texture),false,true,false,false,10));
        addBlock(new BasicBlock(LEAF,TextureCoordinate.from(4,3, texture),false,true,true,false,5));
        addBlock(new BlockTorch(TORCH));
    }

    public boolean blocksFace(BlockFace face) {
        return true;
    }

    public boolean canBeDestroyed() {
        return true;
    }

    public abstract boolean isSolid();

    public boolean isLightSource() {
        return false;
    }

    public abstract int getEmissionStrength();
    public AABB getCollisionBox(int x, int y, int z) {
        return ab.move(x,y,z);
    }

    /**
     * max 15, min 0
     * @return
     */
    public abstract int getLightPenetration();

    public boolean blocksLight() {
        return true;
    }

    public int skyLightFalloff() {
        return getLightPenetration();
    }

    public boolean reciveShadows() {
        return true;
    }
}
