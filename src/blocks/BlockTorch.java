package blocks;

import blocks.models.BlockModel;
import blocks.models.TorchModel;
import engine.physics.AABB;
import engine.texture.TextureCoordinate;

public class BlockTorch extends Block {
    private static BlockModel torchModel = new TorchModel();
    private static AABB ab = new AABB(0.4f,0,0.4f,0.2f,0.8f,0.2f);
    public BlockTorch(short id) {
        super(id);
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean isLightSource() {
        return true;
    }

    @Override
    public int getEmissionStrength() {
        return 15;
    }

    @Override
    public int getLightPenetration() {
        return 0;
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public boolean isRenderable() {
        return true;
    }

    @Override
    public boolean blocksLight() {
        return false;
    }

    @Override
    public BlockModel getModel() {
        return torchModel;
    }

    @Override
    public TextureCoordinate getFaceTexture(BlockFace face) {
        return torchModel.getTexture(face);
    }

    @Override
    public boolean blocksFace(BlockFace face) {
        return false;
    }

    @Override
    public AABB getCollisionBox(int x, int y, int z) {
        return ab.move(x+0.4f,y,z+0.4f);
    }

    @Override
    public boolean reciveShadows() {
        return false;
    }
}
