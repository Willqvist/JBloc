package blocks;

import blocks.models.BlockModel;
import blocks.models.TorchModel;
import engine.texture.TextureCoordinate;

public class BlockTorch extends Block {
    private static BlockModel torchModel = new TorchModel();
    public BlockTorch(short id) {
        super(id);
    }

    @Override
    public boolean isSolid() {
        return false;
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
        return 1;
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
}
