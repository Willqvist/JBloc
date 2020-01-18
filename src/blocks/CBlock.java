package blocks;

import engine.texture.TextureCoordinate;

public class CBlock extends Block {

    private boolean solid = false, opaque = true, renderable = true;
    private int lightPenetration = 0;

    public CBlock(short id, TextureCoordinate textureCoordinate, boolean solid, boolean opaque, boolean renderable, int lightPenetration) {
        super(id, textureCoordinate);
        this.solid = solid;
        this.opaque = opaque;
        this.renderable = renderable;
        this.lightPenetration = lightPenetration;
    }

    @Override
    public boolean isSolid() {
        return solid;
    }

    @Override
    public int getLightPenetration() {
        return lightPenetration;
    }

    @Override
    public boolean isOpaque() {
        return opaque;
    }

    @Override
    public boolean isRenderable() {
        return renderable;
    }
}
