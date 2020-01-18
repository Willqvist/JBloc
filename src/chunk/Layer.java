package chunk;

import blocks.Block;
import engine.model.Model;
import engine.physics.AABB;
import engine.render.IRenderable;
import engine.render.Material;
import engine.render.Renderer;
import engine.render.Transform;
import engine.tools.RoffColor;

import java.awt.*;

public class Layer implements IRenderable {
    private Model model = null, transparentModel = null;
    private boolean renderable = false;
    private boolean dirty = false;
    private int[] layerOpaque;
    private int y;
    private static Transform transform = new Transform(0,0,0);
    private Chunk c;
    private int renderableBlocks = 0;
    protected Layer(Chunk c,int y,int height){
        layerOpaque = new int[height];
        this.y = y;
        this.c = c;
    }

    public int getOpaqueBlocks(int y){
        return layerOpaque[toLayerY(y)];
    }
    private static AABB ab = new AABB(transform,16,16,16);
    private static RoffColor red = RoffColor.from(Color.RED), grn = RoffColor.from(Color.GREEN);
    protected void render(Renderer renderer){
        transform.setPosition(c.getX()*Chunk.WIDTH,y,c.getZ()*Chunk.DEPTH);
        if(model != null) {
            renderer.render(model,getTransform(),getMaterial());
        }
    }

    protected void renderTransparentLayer(Renderer renderer) {
        transform.setPosition(c.getX()*Chunk.WIDTH,y,c.getZ()*Chunk.DEPTH);
        if(transparentModel != null) {
            renderer.render(transparentModel,getTransform(),getMaterial());
        }
    }

    public boolean isDirty(){
        return dirty;
    }

    public void onBlockSet(int x,int y,int z,short block){
        setDirty(true);
        int ly = toLayerY(y);
        Block b = Block.getBlock(block);
        layerOpaque[ly] += b.isOpaque() ? 1 : -1;
        layerOpaque[ly] = Math.max(0,layerOpaque[ly]);
        renderableBlocks += b.isRenderable() ? 1 : -1;
    }

    private boolean isYTop(int y){
        return this.y+Chunk.LAYER_HEIGHT-1 == y && this.y != Chunk.HEIGHT-Chunk.LAYER_HEIGHT;
    }

    private boolean isYBottom(int y){
        return this.y == y && y !=0;
    }

    void setDirty(boolean dirty){
        this.dirty = dirty;
    }

    private void rebuildLayer(int x,int y, int z) {
        Chunk n;
        /*
        if((x == 0 && (n=c.getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=c.getNeighbour(Neighbour.RIGHT)) != null))
            return n.getLightValue(ChunkTools.toBlockPosition(x),y,z);
        if((z < 0 && (n=c.getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=c.getNeighbour(Neighbour.BACK)) != null))
            return n.getLightValue(x,y,ChunkTools.toBlockPosition(z));

        //if(c.isOutsideY(y)) return (byte)15;

        if(x < 0 || z >= Chunk.DEPTH || z < 0 || x >= Chunk.WIDTH)
            return;
        */
        DirtyLayerProvider.addLayer(this);
    }

    public void onNewBlockSet(int x,int y,int z,short block){
        onBlockSet(x,y,z,block);

        System.out.println("SETTIGNS BLOCK: " + block + " < " + x + " | " + y + " | " + z);
        if(x == 0)
            rebuild(y,Neighbour.LEFT);
        if(x == Chunk.WIDTH-1)
            rebuild(y,Neighbour.RIGHT);
        if(z == 0) {
            rebuild(y,Neighbour.FRONT);
        }
        if(z == Chunk.DEPTH-1) {
            rebuild(y,Neighbour.BACK);
        }
        if(y % Chunk.LAYER_HEIGHT == Chunk.LAYER_HEIGHT-1 && y <= Chunk.HEIGHT-Chunk.LAYER_HEIGHT) {
            DirtyLayerProvider.addLayer(c.getLayer(y+Chunk.LAYER_HEIGHT));
        }
        if(y % Chunk.LAYER_HEIGHT == 0 && y >= Chunk.LAYER_HEIGHT) {
            DirtyLayerProvider.addLayer(c.getLayer(y-Chunk.LAYER_HEIGHT));
        }

        if(x== 0 && z == 0) {
            Layer l = c.getNeighbour(Neighbour.LEFT).getLayer(y);
            l.rebuild(y,Neighbour.FRONT);
        }
        if(x == Chunk.WIDTH - 1 && z == 0) {
            Layer l = c.getNeighbour(Neighbour.RIGHT).getLayer(y);
            l.rebuild(y,Neighbour.FRONT);
        }
        if(x == 0 && z == Chunk.DEPTH-1) {
            Layer l = c.getNeighbour(Neighbour.LEFT).getLayer(y);
            l.rebuild(y,Neighbour.BACK);
        }
        if(x == Chunk.WIDTH - 1 && z == Chunk.DEPTH-1) {
            Layer l = c.getNeighbour(Neighbour.RIGHT).getLayer(y);
            l.rebuild(y,Neighbour.BACK);
        }

        DirtyLayerProvider.addLayer(this);

    }

    private void rebuild(int y, Neighbour neighbour) {
        Chunk n = c.getNeighbour(neighbour);
        DirtyLayerProvider.addLayer(n.getLayer(y));
        if(y % Chunk.LAYER_HEIGHT == Chunk.LAYER_HEIGHT-1 && y <= Chunk.HEIGHT-Chunk.LAYER_HEIGHT) {
            DirtyLayerProvider.addLayer(n.getLayer(y+Chunk.LAYER_HEIGHT));
        }
        if(y % Chunk.LAYER_HEIGHT == 0 && y >= Chunk.LAYER_HEIGHT) {
            DirtyLayerProvider.addLayer(n.getLayer(y-Chunk.LAYER_HEIGHT));
        }
    }

    private Layer getLayer(LayerNeighbour neighbour){
        Chunk n = null;
        switch (neighbour){
            case BACK:
                return (n=c.getNeighbour(Neighbour.BACK)) == null ? null : n.getLayer(y);
            case LEFT:
                return (n=c.getNeighbour(Neighbour.LEFT)) == null ? null : n.getLayer(y);
            case RIGHT:
                return (n=c.getNeighbour(Neighbour.RIGHT)) == null ? null : n.getLayer(y);
            case FRONT:
                return (n=c.getNeighbour(Neighbour.FRONT)) == null ? null : n.getLayer(y);
            case TOP:
                return c.getLayer(y+Chunk.LAYER_HEIGHT);
            case BOTTOM:
                return c.getLayer(y-Chunk.LAYER_HEIGHT);
        }
        return null;
    }

    public int getRenderables(){
        return renderableBlocks;
    }

    private int toLayerY(int y){
        return y%Chunk.LAYER_HEIGHT;
    }

    protected void setModel(Model model){
        this.model = model;
    }

    public void setTransparentModel(Model model) {
        transparentModel = model;
    }

    public void render() {

    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Material getMaterial() {
        return c.material;
    }

    @Override
    public Transform getTransform() {
        return transform;
    }

    public boolean equals(Layer l) {
        return l.c == c && l.y == y;
    }

    public int getY() {
        return y;
    }

    public Chunk getChunk() {
        return c;
    }

    public void rebuild() {
        Layer l;
        setDirty(false);
        ChunkModelBuilder.addLayerUnsafe(this);
    }

}
