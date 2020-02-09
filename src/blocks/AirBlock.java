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
    public boolean isLightSource() {
        return false;
    }

    @Override
    public int getEmissionStrength() {
        return 15;
    }

    @Override
    public int getLightPenetration() {
        return 2;
    }

    @Override
    public boolean blocksLight() {
        return false;
    }

    @Override
    public int skyLightFalloff() {
        return 0;
    }
}
