package blocks.models;

import blocks.Block;
import blocks.BlockFace;
import chunk.Faces;
import engine.texture.TextureCoordinate;

public class TorchModel implements BlockModel {
    private TextureCoordinate top = TextureCoordinate.from(7,5*16+6,2,2, Block.texture);
    private TextureCoordinate side = TextureCoordinate.from(7,5*16+4,2,10,Block.texture);
    @Override
    public float[] getModelFaces(BlockFace face) {
        switch(face) {
            case BOTTOM: return getBottomTexture();
            case LEFT: return getLeftTexture();
            case FRONT: return getFrontTexture();
            case BACK: return getBackTexture();
            case RIGHT: return getRightTexture();
            case TOP: return getTopFace();
        }
        return null;
    }

    @Override
    public TextureCoordinate getTexture(BlockFace face) {
        switch (face) {
            case TOP:
            case BOTTOM:
                return top;
                default:
                    return side;
        }
    }

    private float[] getTopFace() {
        return new float[] {
                0.4f, 0.8f, 0.4f, 0.0f, 1.0f, 0.0f,
                0.4f, 0.8f, 0.6f, 0.0f, 1.0f, 0.0f,
                0.6f, 0.8f, 0.6f, 0.0f, 1.0f, 0.0f,
                0.6f, 0.8f, 0.4f, 0.0f, 1.0f, 0.0f,
        };
    }

    private float[] getLeftTexture() {
        return new float[] {
                0.4f, 0.0f, 0.6f, -1.0f, 0.0f, 0.0f,
                0.4f, 0.8f, 0.6f, -1.0f, 0.0f, 0.0f,
                0.4f, 0.8f, 0.4f, -1.0f, 0.0f, 0.0f,
                0.4f, 0.0f, 0.4f, -1.0f, 0.0f, 0.0f,
        };
    }

    private float[] getRightTexture() {
        return new float[] {
                0.6f, 0.0f, 0.4f, 1.0f, 0.0f, 0.0f,
                0.6f, 0.8f, 0.4f, 1.0f, 0.0f, 0.0f,
                0.6f, 0.8f, 0.6f, 1.0f, 0.0f, 0.0f,
                0.6f, 0.0f, 0.6f, 1.0f, 0.0f, 0.0f,
        };
    }

    private float[] getBottomTexture() {
        return new float[] {
                0.4f, 0.0f, 0.6f, 0.0f, -1.0f, 0.0f,
                0.4f, 0.0f, 0.4f, 0.0f, -1.0f, 0.0f,
                0.6f, 0.0f, 0.4f, 0.0f, -1.0f, 0.0f,
                0.6f, 0.0f, 0.6f, 0.0f, -1.0f, 0.0f,
        };
    }

    private float[] getFrontTexture() {
        return new float[] {
                0.6f, 0.0f, 0.6f, 0.0f, 0.0f, 1.0f,
                0.6f, 0.8f, 0.6f, 0.0f, 0.0f, 1.0f,
                0.4f, 0.8f, 0.6f, 0.0f, 0.0f, 1.0f,
                0.4f, 0.0f, 0.6f, 0.0f, 0.0f, 1.0f,
        };
    }

    private float[] getBackTexture() {
        return new float[] {
                0.4f, 0.0f, 0.4f, 0.0f, 0.0f, -1.0f,
                0.4f, 0.8f, 0.4f, 0.0f, 0.0f, -1.0f,
                0.6f, 0.8f, 0.4f, 0.0f, 0.0f, -1.0f,
                0.6f, 0.0f, 0.4f, 0.0f, 0.0f, -1.0f,
        };
    }



}
