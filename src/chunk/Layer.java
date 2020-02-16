package chunk;

import blocks.Block;
import chunk.builder.ChunkModelBuilder;
import engine.model.Model;
import engine.physics.AABB;
import engine.render.IRenderable;
import engine.render.Material;
import engine.render.Renderer;
import engine.render.Transform;
import engine.tools.RoffColor;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Layer implements IRenderable {
    private Model model = null, transparentModel = null;
    private boolean renderable = false;
    private boolean dirty = false;
    private int[] layerOpaque;
    private int y;
    private static Transform transform = new Transform(0,0,0);
    private HashMap<Integer,Layer> blockEditListeners = new HashMap<>();

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
        if(model != null) {
            transform.setPosition(c.getX()*Chunk.WIDTH,y,c.getZ()*Chunk.DEPTH);
            renderer.render(model,transform,getMaterial());
        }
    }

    protected void renderTransparent(Renderer renderer){

        if(transparentModel != null) {
            transform.setPosition(c.getX()*Chunk.WIDTH,y,c.getZ()*Chunk.DEPTH);
            renderer.render(transparentModel,transform,getMaterial());
        }
    }

    public boolean isDirty(){
        return dirty;
    }

    public void onBlockSet(int x,int y,int z,short block){
        //if(block == 1) renderable = true;
        setDirty(true);
        int ly = toLayerY(y);
        Block b = Block.getBlock(block);
        layerOpaque[ly] += b.isOpaque() ? 1 : -1;
        layerOpaque[ly] = Math.max(0,layerOpaque[ly]);
        renderableBlocks += b.isRenderable() ? 1 : 0;
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

    public void onNewBlockSet(int x, int y, int z, short block){
        onBlockSet(x,y,z,block);

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

        DirtyLayerProvider.addLayer(this);

    }

    private void rebuild(int y, Neighbour neighbour) {
        Chunk n = c.getNeighbour(neighbour);
        if(n == null) return;
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

    public void setModel(Model model){
        this.model = model;
    }

    public void setTransparentModel(Model model){
        this.transparentModel = model;
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
        setDirty(false);
        ChunkModelBuilder.addLayer(this);
    }

    public void onBlockEdited(int x,int y,int z,Layer layer) {
        this.rebuild();
    }

    public void executeBlockEdit(Chunk c,int x, int y, int z) {
        int hash = Objects.hash(c,x,y,z);
        if(blockEditListeners.containsKey(hash)) {
            onBlockEdited(x,y,z,blockEditListeners.get(hash));
        }
    }

    public void addBlockEditListener(Chunk c,int x, int y, int z) {
        blockEditListeners.put(Objects.hash(c,x,y,z),c.getLayer(y));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Layer l = (Layer) o;
        return (l.getY() == this.getY() &&
            l.c.getX() == c.getX() &&
            l.c.getZ() == c.getZ());

    }

    @Override
    public int hashCode() {
        int result = Objects.hash(c.getX(),c.getZ(),y);
        return result;
    }
}
