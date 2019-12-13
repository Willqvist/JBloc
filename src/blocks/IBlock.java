package blocks;

import engine.texture.TextureCoordinate;

public interface IBlock {
    TextureCoordinate getFaceTexture(BlockFace face);
    float[] getFaceData(BlockFace face);
    short getId();
    boolean isOpaque();
    boolean isRenderable();
}
