package blocks;

import blocks.models.BlockModel;
import engine.texture.TextureCoordinate;

public interface IBlock {
    TextureCoordinate getFaceTexture(BlockFace face);
    float[] getFaceData(BlockFace face);
    BlockModel getModel();
    short getId();
    boolean isOpaque();
    boolean isRenderable();
}
