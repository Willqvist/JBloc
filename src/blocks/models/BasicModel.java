package blocks.models;

import blocks.BlockFace;
import chunk.Faces;
import engine.texture.TextureCoordinate;

public class BasicModel implements BlockModel {
    @Override
    public float[] getModelFaces(BlockFace face) {
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
    public TextureCoordinate getTexture(BlockFace face) {
        return null;
    }
}
