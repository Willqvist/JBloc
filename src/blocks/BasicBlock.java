package blocks;

import engine.physics.AABB;
import engine.texture.TextureCoordinate;

public class BasicBlock extends Block {

    private static AABB ab = new AABB(0,0,0,1,1,1);

    public BasicBlock(short id, TextureCoordinate textureCoordinate) {
        super(id, textureCoordinate);
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public boolean isRenderable() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public int getLightPenetration() {
        return 0;
    }
}
