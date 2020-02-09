package blocks.models;

import blocks.BlockFace;
import engine.texture.TextureCoordinate;

public interface BlockModel {
    float[] getModelFaces(BlockFace face);
    TextureCoordinate getTexture(BlockFace face);
}
