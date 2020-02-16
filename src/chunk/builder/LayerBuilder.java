package chunk.builder;

import blocks.Block;
import blocks.BlockFace;
import chunk.Chunk;
import chunk.Layer;
import chunk.Neighbour;
import engine.model.ModelBuilder;
import engine.texture.TextureCoordinate;
import settings.Settings;

public class LayerBuilder {

    private boolean renderLayer(Chunk c, int y){
        int topOp = y+1 >= Chunk.HEIGHT ? 0 : c.getLayer(y+1).getOpaqueBlocks(y+1);
        int botOp = y-1 < 0 ? 0 : c.getLayer(y-1).getOpaqueBlocks(y-1);
        if(!c.hasAllNeighbours()) return false;
        return c.getLayer(y).getOpaqueBlocks(y) < Chunk.AREA ||
                topOp < Chunk.AREA ||
                botOp < Chunk.AREA ||
                c.getNeighbour(Neighbour.LEFT).getLayer(y).getOpaqueBlocks(y) < Chunk.AREA ||
                c.getNeighbour(Neighbour.RIGHT).getLayer(y).getOpaqueBlocks(y) < Chunk.AREA ||
                c.getNeighbour(Neighbour.FRONT).getLayer(y).getOpaqueBlocks(y) < Chunk.AREA ||
                c.getNeighbour(Neighbour.BACK).getLayer(y).getOpaqueBlocks(y) < Chunk.AREA ;
    }

    public void buildLayer(Layer layer, LayerModelBuilder builder){
        if(!layer.getChunk().hasAllNeighbours()) return;
        ModelBuilder opaqueModel = builder.getOpaqueModelBuilder();
        ModelBuilder transparentModel = builder.getTransparentModelBuilder();
        Chunk c = layer.getChunk();
        Block b;
        int yl = 0;
        for(int y = layer.getY() + Chunk.LAYER_HEIGHT-1; y >= layer.getY(); y--){
            if(layer.getRenderables() == 0){
                break;
            }

            if(!renderLayer(c,y))
                continue;

            yl = y % Chunk.LAYER_HEIGHT;
            for(int x = 0; x < Chunk.WIDTH; x++){
                for(int z = 0; z < Chunk.DEPTH; z++){
                    Block block = Block.getBlock(c.getBlock(x,y,z));
                    if(block.isRenderable()){
                        if(!(b = Block.getBlock(c.getBlock(x - 1, y, z))).isRenderable() || (!b.isOpaque() && block.isOpaque()) || !b.blocksFace(BlockFace.RIGHT) || !block.blocksFace(BlockFace.LEFT))
                            addFace(c,block.isOpaque() ? opaqueModel : transparentModel,block, BlockFace.LEFT,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x + 1, y, z))).isRenderable() || (!b.isOpaque() && block.isOpaque()) || !b.blocksFace(BlockFace.LEFT) || !block.blocksFace(BlockFace.RIGHT))
                            addFace(c,block.isOpaque() ? opaqueModel : transparentModel,block,BlockFace.RIGHT,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x, y-1, z))).isRenderable() || (!b.isOpaque() && block.isOpaque()) || !b.blocksFace(BlockFace.TOP) || !block.blocksFace(BlockFace.BOTTOM))
                            addFace(c,block.isOpaque() ? opaqueModel : transparentModel,block,BlockFace.BOTTOM,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x, y+1, z))).isRenderable() || (!b.isOpaque() && block.isOpaque()) || !b.blocksFace(BlockFace.BOTTOM) || !block.blocksFace(BlockFace.TOP))
                            addFace(c,block.isOpaque() ? opaqueModel : transparentModel,block,BlockFace.TOP,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x, y, z+1))).isRenderable() || (!b.isOpaque() && block.isOpaque()) || !b.blocksFace(BlockFace.BACK) || !block.blocksFace(BlockFace.FRONT))
                            addFace(c,block.isOpaque() ? opaqueModel : transparentModel,block,BlockFace.FRONT,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x, y, z-1))).isRenderable() || (!b.isOpaque() && block.isOpaque()) || !b.blocksFace(BlockFace.FRONT) || !block.blocksFace(BlockFace.BACK))
                            addFace(c,block.isOpaque() ? opaqueModel : transparentModel,block,BlockFace.BACK,x,yl,z,y);

                    }
                }
            }
        }
    }

    private byte getMinLightValue(Chunk c,BlockFace face,float side[],int i, int x,int y,int z){
        byte min = 0;
        if(!Settings.smoothLightning) {
            if(face == BlockFace.TOP || face == BlockFace.BOTTOM) {
               return (byte)c.getMaxLightValue(x,y+((face==BlockFace.TOP) ? 1 : -1),z);
            }
            if(face == BlockFace.LEFT || face == BlockFace.RIGHT) {
                return (byte)c.getMaxLightValue(x+((face==BlockFace.LEFT) ? -1 : 1),y,z);
            }
            if(face == BlockFace.FRONT || face == BlockFace.BACK) {
                return (byte)c.getMaxLightValue(x,y,z+((face==BlockFace.FRONT) ? 1 : -1));
            }
        }
        if(face == BlockFace.TOP || face == BlockFace.BOTTOM){
            int dirX = side[i] == 0 ? -1 : 1;
            int dirZ = side[i+2] == 0 ? -1 : 1;
            int dir = face == BlockFace.TOP ? 1 : -1;
            min += c.getMaxLightValue(x,y+dir,z);
            min += c.getMaxLightValue(x+dirX,y+dir,z);
            min += c.getMaxLightValue(x,y+dir,z+dirZ);
            min += c.getMaxLightValue(x+dirX,y+dir,z+dirZ);
        }
        else if(face == BlockFace.FRONT || face == BlockFace.BACK) {
            int dirX = side[i+0] == 0 ? -1 : 1;
            int dirY = side[i+1] == 0 ? -1 : 1;
            int dir = face == BlockFace.FRONT ? 1 : -1;
            min += c.getMaxLightValue(x,y,z+dir);
            min += c.getMaxLightValue(x+dirX,y,z+dir);
            min += c.getMaxLightValue(x,y+dirY,z+dir);
            min += c.getMaxLightValue(x+dirX,y+dirY,z+dir);
        }
        else if(face == BlockFace.LEFT || face == BlockFace.RIGHT) {
            int dir = face == BlockFace.LEFT ? -1 : 1;
            int dirZ = side[i+2] == 0 ? -1 : 1;
            int dirY = side[i+1] == 0 ? -1 : 1;
            min += c.getMaxLightValue(x+dir,y,z);
            min += c.getMaxLightValue(x+dir,y,z+dirZ);
            min += c.getMaxLightValue(x+dir,y+dirY,z);
            min += c.getMaxLightValue(x+dir,y+dirY,z+dirZ);
        }

        return (byte)(min/4);
    }

    private void addFace(Chunk c, ModelBuilder builder, Block block, BlockFace face, int x,int y,int z,int yReal){
        float[] vertices = block.getFaceData(face);
        TextureCoordinate cord = block.getFaceTexture(face);
        int ti = 0;
        for (int i = 0; i < vertices.length; i += 6) {
            builder.addFloat(vertices[i]+x);
            builder.addFloat(vertices[i+1]+y);
            builder.addFloat(vertices[i+2]+z);
            builder.addFloat(vertices[i+3]);
            builder.addFloat(vertices[i+4]);
            builder.addFloat(vertices[i+5]);

            if(face == BlockFace.BOTTOM || face == BlockFace.TOP){
                builder.addFloat(cord.getOffsetX() + (1-vertices[i]) * cord.getWidth());
                builder.addFloat(cord.getOffsetY() + (1-vertices[i+2]) * cord.getHeight());
            }else if(face == BlockFace.FRONT ||face == BlockFace.BACK){
                builder.addFloat(cord.getOffsetX() + (1-vertices[i]) * cord.getWidth());
                builder.addFloat(cord.getOffsetY() + (1-vertices[i+1]) * cord.getHeight());
            }else{
                builder.addFloat(cord.getOffsetX() + (1-vertices[i+2]) * cord.getWidth());
                builder.addFloat(cord.getOffsetY() + (1-vertices[i+1]) * cord.getHeight());
            }
            if(block.reciveShadows()) {
                builder.addFloat(((float) getMinLightValue(c, face, vertices, i, x, yReal, z)) / 15f);
            }else {
                builder.addFloat(1);
            }
            ti += 2;
            /*
            builder.addFloats(
                            //vertices
                     vertices[i]+x,
                            vertices[i+1]+y,
                            vertices[i+2]+z
                            //normals
                            //vertices[i+3],
                            //vertices[i+4],
                            //vertices[i+5]
            );

             */
        }
    }
}
