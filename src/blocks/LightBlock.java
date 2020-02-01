package blocks;

import engine.texture.TextureCoordinate;

public class LightBlock extends Block {

    public LightBlock(short id, TextureCoordinate textureCoordinate) {
        super(id, textureCoordinate);
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
        return 15;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isRenderable() {
        return true;
    }
}
