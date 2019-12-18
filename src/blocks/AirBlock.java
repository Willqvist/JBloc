package blocks;

import engine.texture.TextureCoordinate;

public class AirBlock extends Block {

    public AirBlock() {
        super(AIR);
    }

    @Override
    public TextureCoordinate getFaceTexture(BlockFace face) {
        return null;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isRenderable() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public int getLightPenetration() {
        return 15;
    }
}
