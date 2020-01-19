package blocks;

import engine.physics.AABB;
import engine.texture.TextureCoordinate;

public class BasicBlock extends Block {

    private static AABB ab = new AABB(0,0,0,1,1,1);
    private boolean opaque = true, renderable = true, solid = true;
    private int lightPen = 0;
    public BasicBlock(short id, TextureCoordinate textureCoordinate) {
        super(id, textureCoordinate);
    }

    public BasicBlock(short id, TextureCoordinate textureCoordinate, boolean opaque, boolean renderable, boolean solid, int lightPenitration) {
        super(id, textureCoordinate);
        this.opaque = opaque;
        this.renderable = renderable;
        this.solid = solid;
        this.lightPen = lightPenitration;
    }

    @Override
    public boolean isOpaque() {
        return opaque;
    }

    @Override
    public boolean isRenderable() {
        return renderable;
    }

    @Override
    public boolean isSolid() {
        return solid;
    }

    @Override
    public int getLightPenetration() {
        return lightPen;
    }
}
