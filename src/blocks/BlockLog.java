package blocks;

import engine.texture.TextureCoordinate;

public class BlockLog extends Block {
    private TextureCoordinate top = TextureCoordinate.from(1,9, texture);
    private TextureCoordinate bottom = TextureCoordinate.from(2,0, texture);
    private TextureCoordinate side;
    public BlockLog(short id, TextureCoordinate side) {
        super(id);
        this.side = side;
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
    public boolean isLightSource() {
        return false;
    }

    @Override
    public int getEmissionStrength() {
        return 0;
    }

    @Override
    public int getLightPenetration() {
        return 0;
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