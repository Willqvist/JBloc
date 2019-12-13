package blocks;

import engine.physics.AABB;
import engine.texture.TextureCoordinate;

public class GrassBlock extends Block {
    private TextureCoordinate top = TextureCoordinate.from(0,0, texture);
    private TextureCoordinate bottom = TextureCoordinate.from(2,0, texture);
    private TextureCoordinate side = TextureCoordinate.from(3,0, texture);
    public GrassBlock() {
        super(GRASS);
    }



    @Override
    public TextureCoordinate getFaceTexture(BlockFace face) {
        if(face.isSide()){
            return side;
        }else if(face == BlockFace.BOTTOM){
            return bottom;
        }
        return top;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public boolean isRenderable() {
        return true;
    }
}
