package chunk;

import biome.Biome;
import blocks.Block;
import blocks.WorldBlock;
import chunk.builder.ChunkModelBuilder;
import engine.Engine;
import engine.materials.MaterialBank;
import engine.materials.StandardMaterial;
import engine.physics.AABB;
import engine.physics.*;
import engine.render.AABBRenderer;
import engine.render.Renderer;
import engine.tools.RoffColor;
import entities.Entity;
import entities.PhysicsEntity;
import org.joml.*;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Chunk implements ICollideable, ICollisionPool {
    public static final int WIDTH=16;
    public static final int HEIGHT=256;
    public static final int DEPTH=16;
    public static final int AREA=WIDTH*DEPTH;
    public static final int LAYER_HEIGHT=16;
    private Layer[] layers = new Layer[HEIGHT/LAYER_HEIGHT];
    private short[] blocks = new short[WIDTH*HEIGHT*DEPTH];
    private int[] lightValue = new int[WIDTH*HEIGHT*DEPTH];
    private short[] height = new short[WIDTH*DEPTH];
    private boolean built=false;
    private Chunk[] neightbours = new Chunk[Neighbour.values().length-2];
    private int maxHeight=0;
    //old model if dirty = true, model needs to be remodeled...
    private boolean dirty = true, lightDirty = true;
    private boolean renderable = true;
    private int x,z;
    private AABB ab;
    protected static StandardMaterial material;
    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<PhysicsEntity> physicsEntities = new ArrayList<>();
    private ChunkProvider chunkProvider;
    private ChunkState state = ChunkState.STABLE;
    private HashSet<Layer> affectedLayers = new HashSet<Layer>();
    private ArrayList<NodeLight> removedLights = new ArrayList<>();
    private HashSet<NodeLight> addedLights = new HashSet<>();
    private boolean building = false;
    public Chunk(ChunkProvider chunkProvider, int x, int z){
        this.x = x;
        this.z = z;
        this.chunkProvider = chunkProvider;
        ab = new AABB(x*Chunk.WIDTH,0,z*Chunk.DEPTH,Chunk.WIDTH,Chunk.HEIGHT,Chunk.DEPTH);
        for(int i = 0; i < layers.length; i++){
            layers[i] = new Layer(this,i*LAYER_HEIGHT,LAYER_HEIGHT);
        }

        if(material == null) {
            material = MaterialBank.getMaterial("chunk",StandardMaterial.class);
        }

    }

    public short getHeight(int x,int z){
        return height[x*DEPTH+z];
    }

    private void setHeight(int x,int z,int height){
        this.height[x*DEPTH+z] = (short)height;
        if(height > maxHeight) {
            maxHeight = height;
        }
    }

    public Layer[] getLayers(){
        return layers;
    }

    public boolean isBuilt(){
        return built;
    }

    private Object buildLock = new Object();
    public void generateBlocks(){
        synchronized (buildLock) {
            if (built) return;
            built = true;
            building = true;
        }
        for(int x = 0; x < WIDTH; x++){
            for(int z = 0; z < DEPTH; z++){
                Biome biome = Biome.getBiome(this.x * WIDTH + x, this.z * DEPTH + z);
                int height = biome.getHeight(this.x * WIDTH + x, this.z * DEPTH + z);
                setHeight(x,z,height);
                building = true;
                for(int y = Chunk.HEIGHT - 1; y >= 0; y--){
                    short block = biome.getBlock(this.x * WIDTH + x, y, this.z * DEPTH + z, height);
                    if(getBlock(x,y,z) == Block.AIR)
                        setBlock(x,y,z, block);
                }
                building= false;
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    biome.generateStructures(chunkProvider, getWorldPosition().x + x, y, getWorldPosition().y + z, height);
                }
            }
        }
        //long a = System.currentTimeMillis();
        fillLightList();
        //System.out.println("IT TOOK : " + (System.currentTimeMillis()-a) +"ms");
        ab.resize(Chunk.WIDTH,this.maxHeight+1,Chunk.DEPTH);
    }

    private void fillLightList() {
        for(int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    boolean propSky = shouldPropagate(x, y, z, true);
                    boolean propBlock = shouldPropagate(x, y, z, false);
                    synchronized (addedLights) {
                        if (propSky && propBlock) {
                            addedLights.add(new NodeLight(x, y, z, 0, 2, this));
                        } else if (propBlock) {
                            addedLights.add(new NodeLight(x, y, z, 0, 0, this));
                        } else if (propSky) {
                            addedLights.add(new NodeLight(x, y, z, 0, 1, this));
                        }
                    }
                    /*
                    if (shouldPropagate(x, y, z, false)) {
                        addedLights.add(new NodeLight(x, y, z, 0, 1, this));
                    }

                     */
                }
            }
        }
    }

    private Chunk getChunk(int x,int y,int z) {
        Chunk n;
        if((x < 0 && (n=getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=getNeighbour(Neighbour.RIGHT)) != null))
            return n.getChunk(ChunkTools.toBlockPosition(x),y,z);
        if((z < 0 && (n=getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=getNeighbour(Neighbour.BACK)) != null))
            return n.getChunk(x,y,ChunkTools.toBlockPosition(z));
        return this;
    }

    private static final int[][] dirs = new int[][]{
            {-1,0,0},
            {1,0,0},
            {0,1,0},
            {0,-1,0},
            {0,0,1},
            {0,0,-1},
    };

    private int toIndex(int x,int y,int z){
        return x * HEIGHT * DEPTH + y * DEPTH + z;
    }

    private Block getBlockObj(int x,int y,int z) {
        return Block.getBlock(getBlock(x,y,z));
    }

    private Object lightLock = new Object();

    private void propegateLight(int x,int y,int z, int str, boolean sky) {
        if(str <= 0) return;
        setLightValue(x,y,z,str,sky);
        for(int i = 0; i < dirs.length; i++) {
            int x1 = x+dirs[i][0];
            int y1 = y+dirs[i][1];
            int z1 = z+dirs[i][2];
            Block b = getBlockObj(x1,y1,z1);
            int lightShouldBe = str-b.getLightPenetration();
            if(lightShouldBe > getLightValue(x1,y1,z1,sky) && !b.blocksLight())
                propegateLight(x1,y1,z1,lightShouldBe,sky);
        }
    }

    private void propegateLightRemoval(int x,int y,int z, int str, boolean sky) {
        if(str <= 0) return;
        setLightValue(x,y,z,0,sky);
        for(int i = 0; i < dirs.length; i++) {
            int x1 = x+dirs[i][0];
            int y1 = y+dirs[i][1];
            int z1 = z+dirs[i][2];
            Block b = getBlockObj(x1,y1,z1);
            Chunk c = getChunk(x1,y1,z1);
            int val;
            if((val=getLightValue(x1,y1,z1,sky)) < str && !b.blocksLight() && val != 0)
                propegateLightRemoval(x1,y1,z1,str-b.getLightPenetration(),sky);
            else if(getLightValue(x1,y1,z1,sky) >= str && !b.blocksLight()) {
                int cx = x1 < 0 ? Chunk.WIDTH+x1 : x1 >= Chunk.WIDTH ? Chunk.WIDTH-x1 : x1;
                int cz = z1 < 0 ? Chunk.DEPTH+z1 : z1 >= Chunk.DEPTH ? Chunk.DEPTH-z1 : z1;
                synchronized (addedLights) {
                    addedLights.add(new NodeLight(x1, y1, z1, getLightValue(cx, y1, cz, true), sky ? 1 : 0, c));
                }
            }
        }
    }

    private void propLight(int x,int y,int z, boolean sky) {
        if(shouldPropagate(x,y,z,sky)) {
            propegateLight(x, y, z, getLightValue(x, y, z, sky),sky);
        }
    }

    private void calcLightRemoval() {
        for(NodeLight light : removedLights) {
            int str = light.getVal();
            int x = light.getX();
            int y = light.getY();
            int z = light.getZ();
            propegateLightRemoval(x,y,z,str,true);
        }
        removedLights.clear();
    }

    private void propagateLights() {
        calcLightRemoval();
        synchronized (addedLights) {
            for (NodeLight light : addedLights) {
                int x = light.getX();
                int y = light.getY();
                int z = light.getZ();
                if (light.getType() == 1 || light.getType() == 2 && getSkyLightValue(x, y, z) < light.getVal())
                    light.getC().propLight(x, y, z, true);
                else if (light.getType() == 0 || light.getType() == 2)
                    light.getC().propLight(x, y, z, false);
            }
        }

        addedLights.clear();
    }

    private boolean shouldPropagate(int x, int y, int z, boolean sky) {
        int curLight = getLightValue(x,y,z,sky);
        if(getLightValue(x,y,z,sky) == 0) return false;
        for(int i = 0; i < dirs.length; i++) {
            int x1 = x+dirs[i][0];
            int y1 = y+dirs[i][1];
            int z1 = z+dirs[i][2];
            int lightSurround = getLightValue(x1,y1,z1, sky);
            Block b = getBlockObj(x1,y1,z1);
            if(!b.blocksLight() && lightSurround < curLight-b.getLightPenetration()) {
                return true;
            }
        }
        return false;
    }


    public void calculateLights() {
        if (!lightDirty)
            return;
        lightDirty = false;
        propagateLights();
    }

    public Layer getLayer(int y){
        return layers[y/LAYER_HEIGHT];
    }

    public void setLightValue(int x,int y, int z,int value, boolean isSky) {
        Chunk n;
        if(y < 0 || y >= Chunk.HEIGHT) {
            return;
        }
        if((x < 0 && (n=getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=getNeighbour(Neighbour.RIGHT)) != null)) {
            n.setLightValue(ChunkTools.toBlockPosition(x), y, z, value,isSky);
        }
        else if((z < 0 && (n=getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=getNeighbour(Neighbour.BACK)) != null)) {
            n.setLightValue(x, y, ChunkTools.toBlockPosition(z), value,isSky);
        }
        else if(isOutsideXZ(x,z)) {
            return;
        }
        else {
            getLayer(y).setLightValue(x,y,z);
            value = Math.min(15,Math.max(0,value));
            int gate = isSky ? 0XF : 0XF0;
            int shift = isSky ? 4 : 0;
            this.lightValue[toIndex(x,y,z)] = (this.lightValue[toIndex(x,y,z)] & gate) | (value << shift);
        }
    }

    public void setBlockLightValue(int x,int y,int z ,int val) {
        setLightValue(x,y,z,val,false);
        if(!building && built)
            DirtyLayerProvider.addLayer(getLayer(y));
    }
    public void setSkyLightValue(int x,int y,int z ,int val) {
        setLightValue(x,y,z,val,true);
        if(!building && built)
            DirtyLayerProvider.addLayer(getLayer(y));
    }

    public int getMaxLightValue(int x,int y,int z) {
        return getSkyLightValue(x,y,z);
    }

    public int getLightValue(int x,int y, int z,boolean isSky) {
        Chunk n;

        if((x < 0 && (n=getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=getNeighbour(Neighbour.RIGHT)) != null))
            return n.getLightValue(ChunkTools.toBlockPosition(x),y,z,isSky);
        if((z < 0 && (n=getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=getNeighbour(Neighbour.BACK)) != null))
            return n.getLightValue(x,y,ChunkTools.toBlockPosition(z),isSky);
        if(isOutsideY(y)) return 15;
        int shift = isSky ? 4 : 0;
        if(x < 0 || z >= Chunk.DEPTH || z < 0 || x >= Chunk.WIDTH)
            return 15;
        return (this.lightValue[toIndex(x,y,z)] >> shift) & 0xF;
    }

    public int getSkyLightValue(int x,int y,int z) {
        return getLightValue(x,y,z,true);
    }

    public int getBlockLightValue(int x,int y,int z) {
        return getLightValue(x,y,z,false);
    }

    private static RoffColor c = RoffColor.from(Color.RED);
    private static RoffColor c2 = RoffColor.from(Color.ORANGE);
    public void render(Renderer renderer){

        AABBRenderer.setColor(c2);
        AABBRenderer.render(renderer, this.ab);
        if(!renderable) return;
        //AABBRenderer.setColor(col);
        material.setAlbedoTexture(Block.texture);
        for (int i = 0; i < layers.length; i++) {
            layers[i].render(renderer);
        }
    }

    public void renderTransparency(Renderer renderer) {
        if(!renderable) return;
        material.setAlbedoTexture(Block.texture);
        for(int i = 0; i < layers.length; i++){
            layers[i].renderTransparent(renderer);
        }
    }


    private void onNeighbourIsBuilt(Neighbour neighbour){
        shouldGenerateModel();
    }
    private void shouldGenerateModel(){
        if(canBuildModel())
            buildModel();
    }

    private void buildModel(){
        if(dirty) {
            dirty = false;
            ChunkModelBuilder.addChunk(this);
        }
    }

    public void onChunkBuild(){
        Chunk c;
        built = true;
        boolean someNull = false;
        if((c=getNeighbour(Neighbour.RIGHT)) != null) c.onNeighbourIsBuilt(Neighbour.LEFT); else someNull = true;
        if((c=getNeighbour(Neighbour.LEFT)) != null) c.onNeighbourIsBuilt(Neighbour.RIGHT); else someNull = true;
        if((c=getNeighbour(Neighbour.FRONT)) != null) c.onNeighbourIsBuilt(Neighbour.BACK); else someNull = true;
        if((c=getNeighbour(Neighbour.BACK)) != null) c.onNeighbourIsBuilt(Neighbour.FRONT); else someNull = true;
        if((c=getNeighbour(Neighbour.RIGHT_BACK)) != null) c.onNeighbourIsBuilt(Neighbour.LEFT_FRONT); else someNull = true;
        if((c=getNeighbour(Neighbour.RIGHT_FRONT)) != null) c.onNeighbourIsBuilt(Neighbour.LEFT_BACK); else someNull = true;
        if((c=getNeighbour(Neighbour.LEFT_FRONT)) != null) c.onNeighbourIsBuilt(Neighbour.RIGHT_BACK); else someNull = true;
        if((c=getNeighbour(Neighbour.LEFT_BACK)) != null) c.onNeighbourIsBuilt(Neighbour.RIGHT_FRONT); else someNull = true;
        if(someNull) return;
        shouldGenerateModel();
    }

    private boolean canBuildModel() {
        Chunk c;
        if(!this.isBuilt()) return false;
        if((c=getNeighbour(Neighbour.RIGHT)) == null || !c.isBuilt()) return false;
        if((c=getNeighbour(Neighbour.LEFT)) == null || !c.isBuilt()) return false;
        if((c=getNeighbour(Neighbour.FRONT)) == null || !c.isBuilt()) return false;
        if((c=getNeighbour(Neighbour.BACK)) == null || !c.isBuilt()) return false;

        if((c=getNeighbour(Neighbour.RIGHT_BACK)) == null || !c.isBuilt()) return false;
        if((c=getNeighbour(Neighbour.LEFT_BACK)) == null || !c.isBuilt()) return false;
        if((c=getNeighbour(Neighbour.RIGHT_FRONT)) == null || !c.isBuilt()) return false;
        if((c=getNeighbour(Neighbour.LEFT_FRONT)) == null || !c.isBuilt()) return false;

        return true;
    }

    public void setNeighbour(Chunk c,Neighbour neighbour){
        neightbours[neighbour.ordinal()] = c;
    }
    public Chunk getNeighbour(Neighbour neighbour){
        return neightbours[neighbour.ordinal()];
    }

    private int getBiggestNeighbourLightValue(int x,int y,int z,boolean isSky) {
        return Math.max(getLightValue(x-1,y,z,isSky),
               Math.max(getLightValue(x+1,y,z,isSky),
               Math.max(getLightValue(x,y+1,z,isSky),
               Math.max(getLightValue(x,y-1,z,isSky),
               Math.max(getLightValue(x,y,z+1,isSky),
               getLightValue(x,y,z-1,isSky))))));
    }

    private Vector3i getNeighbourBiggestLightValue(int x,int y,int z,boolean sky) {
        int big = 0;
        Vector3i res = new Vector3i();
        for(int i = 0; i < dirs.length; i++) {
            int x1 = x+dirs[i][0];
            int y1 = y+dirs[i][1];
            int z1 = z+dirs[i][2];
            if(getLightValue(x1,y1,z1,sky) > big) {
                res.set(x1,y1,z1);
                big = getLightValue(x1,y1,z1,sky);
            }
        }
        return res;
    }

    private void removeSkyLightBelow(int x,int y,int z, boolean prop) {
        for(int y1 = y-1; y1 >= 0; y1--) {
            if(getBlock(x,y1,z) != Block.AIR) return;
            if(prop)
                removedLights.add(new NodeLight(x,y1,z,getSkyLightValue(x,y1,z),1,this));
            setSkyLightValue(x,y1,z,0);
        }
    }

    private void addSkyLightBelow(int x,int y,int z, boolean prop) {
        for(int y1 = y-1; y1 >= 0; y1--) {
            if(!(getBlock(x,y1,z) == Block.AIR && getSkyLightValue(x,y1,z) < 15)) return;
            synchronized (addedLights) {
                if (prop)
                    addedLights.add(new NodeLight(x, y1, z, 15, 1, this));
            }
            setSkyLightValue(x,y1,z,15);

        }
    }

    public void placeBlock(Entity e, int x,int y,int z, short block) {
        if(y > maxHeight && Block.getBlock(block).isRenderable()){
            setHeight(x,z,y);
        } else if(y == maxHeight && !Block.getBlock(block).isRenderable()) {
            setHeight(x,z,y);
        }
        if(isOutsideY(y)) return;
        dirty = true;
        lightDirty = true;
        Block newBlock = Block.getBlock(block);
        Block currentBlock = Block.getBlock(getBlock(x,y,z));
        if(currentBlock.isLightSource()) {
        }

        if(newBlock.isLightSource()) {
        }
        else if(block == Block.AIR && getSkyLightValue(x,y+1,z) == 15 && getBlock(x,y+1,z) == Block.AIR) {
            setSkyLightValue(x,y,z,15);
            synchronized (addedLights) {
                addedLights.add(new NodeLight(x, y, z, 15, 1, this));
            }
        }
        else if(newBlock.isSolid()) {
            int oldL = getSkyLightValue(x,y,z);
            setBlockLightValue(x,y,z,0);
            setSkyLightValue(x,y,z,0);
            removedLights.add(new NodeLight(x,y,z,oldL,1,this));
            if (oldL == 15 && getBlock(x, y, z) == Block.AIR) {
                removeSkyLightBelow(x, y, z,true);
            }
        }
        if(!newBlock.blocksLight()) {
            Vector3i neigh = getNeighbourBiggestLightValue(x,y,z,true);
            if(getLightValue(neigh.x,neigh.y,neigh.z,true) != 0)
                synchronized (addedLights) {
                    addedLights.add(new NodeLight(neigh.x, neigh.y, neigh.z, getLightValue(neigh.x, neigh.y, neigh.z, true), 1, this));
                }
        }

        if(built && !building && block == Block.AIR && getSkyLightValue(x,y+1,z) == 15 && getBlock(x,y+1,z) == Block.AIR) {
            addSkyLightBelow(x, y, z,true);
        }
        getLayer(y).placeBlock(x,y,z,block);
        fillBlockArray(x,y,z,block);
    }

    public void setBlock(int x,int y,int z, short block) {
        if(y > maxHeight && Block.getBlock(block).isRenderable()){
            setHeight(x,z,y);
        } else if(y == maxHeight && !Block.getBlock(block).isRenderable()) {
            setHeight(x,z,y);
        }
        if(isOutsideY(y)) return;
        dirty = true;
        lightDirty = true;
        Block newBlock = Block.getBlock(block);
        Block currentBlock = Block.getBlock(getBlock(x,y,z));
       // System.out.println(getSkyLightValue(x,y+1,z));
        if(currentBlock.isLightSource()) {
        }

        if(newBlock.isLightSource()) {
        }
        else if(block == Block.AIR && getSkyLightValue(x,y+1,z) == 15 && getBlock(x,y+1,z) == Block.AIR) {
            setSkyLightValue(x,y,z,15);
            //addedLights.add(new NodeLight(x,y,z,15,1,this));
        }
        else if(newBlock.isSolid()) {
            int oldL = getSkyLightValue(x,y,z);
            setBlockLightValue(x,y,z,0);
            setSkyLightValue(x,y,z,0);
            //removedLights.add(new NodeLight(x,y,z,oldL,1,this));
            if(built && !building) {
                if (oldL == 15 && getBlock(x, y, z) == Block.AIR) {
                    removeSkyLightBelow(x, y, z,false);
                }
            }
        }else if(!newBlock.blocksLight()) {
            Vector3i neigh = getNeighbourBiggestLightValue(x,y,z,true);
            //if(getLightValue(neigh.x,neigh.y,neigh.z,true) != 0)
                //addedLights.add(new NodeLight(neigh.x,neigh.y,neigh.z,getLightValue(neigh.x,neigh.y,neigh.z,true),1,this));
        }

        if(built && !building && block == Block.AIR && getSkyLightValue(x,y+1,z) == 15 && getBlock(x,y+1,z) == Block.AIR) {
            addSkyLightBelow(x, y, z,false);
        }
        getLayer(y).onBlockSet(x,y,z,block);
        fillBlockArray(x,y,z,block);
    }

    private void fillBlockArray(int x,int y,int z,short block) {
        this.blocks[x * HEIGHT * DEPTH + y * DEPTH + z] = block;
    }

    public short getBlock(int x,int y,int z){
        Chunk n;

        if((x < 0 && (n=getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=getNeighbour(Neighbour.RIGHT)) != null))
            return n.getBlock(ChunkTools.toBlockPosition(x),y,z);
        if((z < 0 && (n=getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=getNeighbour(Neighbour.BACK)) != null))
            return n.getBlock(x,y,ChunkTools.toBlockPosition(z));

        if(isOutsideY(y)) return Block.AIR;

        if(x < 0 || z >= Chunk.DEPTH || z < 0 || x >= Chunk.WIDTH)
            return Block.AIR;

        return this.blocks[toIndex(x,y,z)];
    }

    private void onNeighbourDestroy(Neighbour neighbour){
        setNeighbour(null,neighbour);
    }

    public void destroy(){
        Chunk c;
        if((c=getNeighbour(Neighbour.RIGHT)) != null) c.onNeighbourDestroy(Neighbour.LEFT);
        if((c=getNeighbour(Neighbour.LEFT)) != null) c.onNeighbourDestroy(Neighbour.RIGHT);
        if((c=getNeighbour(Neighbour.FRONT)) != null) c.onNeighbourDestroy(Neighbour.BACK);
        if((c=getNeighbour(Neighbour.BACK)) != null) c.onNeighbourDestroy(Neighbour.FRONT);
        if((c=getNeighbour(Neighbour.LEFT_FRONT)) != null) c.onNeighbourDestroy(Neighbour.RIGHT_BACK);
        if((c=getNeighbour(Neighbour.LEFT_BACK)) != null) c.onNeighbourDestroy(Neighbour.RIGHT_FRONT);
        if((c=getNeighbour(Neighbour.RIGHT_FRONT)) != null) c.onNeighbourDestroy(Neighbour.LEFT_BACK);
        if((c=getNeighbour(Neighbour.RIGHT_BACK)) != null) c.onNeighbourDestroy(Neighbour.LEFT_FRONT);
    }

    private boolean isOutsideY(int y){return y < 0 || y >= HEIGHT; }
    private boolean isOutsideXZ(int x,int z){ return x < 0 || z < 0 || x >= WIDTH || z >= DEPTH; }

    public int getX() {
        return x;
    }
    public int getZ(){
        return z;
    }

    @Override
    public ICollider getCollider() {
        return ab;
    }

    @Override
    public CollisionResponse onCollision(ColliderData info) {
        return CollisionResponse.PUSH;
    }



    private static Vector2i worldPos = new Vector2i(0,0);

    public Vector2i getWorldPosition() {
        return worldPos.set(x*Chunk.WIDTH,z*Chunk.DEPTH);
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean hasAllNeighbours() {
        return getNeighbour(Neighbour.LEFT) != null &&
                getNeighbour(Neighbour.RIGHT) != null &&
                getNeighbour(Neighbour.FRONT) != null &&
                getNeighbour(Neighbour.BACK) != null &&
                getNeighbour(Neighbour.LEFT_FRONT) != null &&
                getNeighbour(Neighbour.LEFT_BACK) != null &&
                getNeighbour(Neighbour.RIGHT_FRONT) != null &&
                getNeighbour(Neighbour.RIGHT_BACK) != null;
    }

    public void rebuild() {
        if(!dirty || !canBuildModel()) return;

        for(int i = 0; i < layers.length; i++){
            layers[i].rebuild();
        }
        dirty = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return x == chunk.x &&
                z == chunk.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public List<IPhysicsBody> getEntities() {
        return null;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);

    }

    public void addEntity(PhysicsEntity entity) {
        physicsEntities.add(entity);
        if(physicsEntities.size() == 1) {
            Engine.physics.addCollisionPool(this);
        }
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public void removeEntity(PhysicsEntity entity) {
        physicsEntities.remove(entity);
        if(entities.size() == 0) {
            Engine.physics.removeCollisionPool(this);
        }
    }

    private ArrayList<WorldBlock> collisionResult = new ArrayList<>();
    private Vector3i colBlockSize = new Vector3i(),colBlockPos = new Vector3i();
    private Vector2i cPos = new Vector2i(0,0);
    private Vector3i colBlockPos3 = new Vector3i(0,0,0);
    private List<WorldBlock> getCollidingBlocks(AABB ab, Vector3d offset) {
        collisionResult.clear();
        Vector3d pos = ab.getPosition();

        colBlockPos.set((int)Math.floor(pos.x + (offset.x < 0 ? offset.x : 0)),(int)Math.floor(pos.y + (offset.y < 0 ? offset.y : 0)),(int)Math.floor(pos.z + (offset.z < 0 ? offset.z : 0)));
        colBlockSize.set((int)Math.floor(pos.x + ab.getWidth() + (offset.x > 0 ? offset.x : 0))+1,(int)Math.floor(pos.y + ab.getHeight() + (offset.y > 0 ? offset.y : 0))+1,(int)Math.floor(pos.z + ab.getDepth() + (offset.z > 0 ? offset.z : 0))+1);

        for (int x = colBlockPos.x; x < colBlockSize.x; ++x) {
            for (int y = colBlockPos.y; y < colBlockSize.y; ++y) {
                for (int z = colBlockPos.z; z < colBlockSize.z; ++z) {
                    Block b;
                    Chunk c = chunkProvider.getChunk(ChunkTools.toChunkPosition(x,z,cPos));
                    //System.out.println("CHUNK: " + ChunkTools.toChunkPosition(x,z) + " | " + x + " | " + z);
                    Vector3i blockPos = ChunkTools.toBlockPosition(x,y,z,colBlockPos3);
                    if (c != null && (b = Block.getBlock(c.getBlock(blockPos.x,blockPos.y,blockPos.z))).isSolid())
                        collisionResult.add(new WorldBlock(x,y,z,b.getId()));
                }
            }
        }
        return collisionResult;
    }
    private static Vector3d offset = new Vector3d(0,0,0);
    private static Vector3d orgStep = new Vector3d(0,0,0);
    @Override
    public void executeCollisions() {
        physicsEntities.forEach((e)-> {
            Vector3d step = e.getVelocity().getVelocity();
            orgStep.set(step);
            if(e.getCollider().getType() != ICollider.Type.AABB) return;
            offset.set(step.x,step.y,step.z);
            List<WorldBlock> blocks = getCollidingBlocks((AABB)e.getCollider(), offset);
            AABB ab = (AABB)e.getCollider();
            e.setGrounded(false);
            if(blocks.size() == 0) return;

            //corner collision
            blocks.forEach((b) -> {
                offset.x = b.getBoundingBox().xClipping(ab, offset.x, false);
                offset.z = b.getBoundingBox().zClipping(ab, offset.z, false);
            });

            //if is in corner!! push out depending opn what axis is the shortest.
            if (orgStep.x != offset.x && orgStep.z != offset.z) {
                if (Math.abs(offset.x) < Math.abs(offset.z)) {
                    e.getTransform().translate(offset.x, 0, 0);
                    e.getVelocity().clear(Axis.X);
                } else if (Math.abs(offset.x) > Math.abs(offset.z)) {
                    e.getTransform().translate(0, 0, offset.z);
                    e.getVelocity().clear(Axis.Z);
                } else {
                    e.getTransform().translate(offset.x, 0, offset.z);
                    e.getVelocity().clear(Axis.X);
                    e.getVelocity().clear(Axis.Z);
                }
            }
            offset.set(orgStep);

            //y collision
            blocks.forEach((b) -> {
                offset.y = b.getBoundingBox().yClipping(ab, offset.y, false);
            }
            );
            if (offset.y != orgStep.y) {
                e.getTransform().translate(0, offset.y, 0);
                e.getVelocity().clear(Axis.Y);

            }

            //x collision
            blocks.forEach((b) ->
                offset.x = b.getBoundingBox().xClipping(ab, offset.x,true)
            );
            if (offset.x != orgStep.x) {
                e.getVelocity().clear(Axis.X);
                e.getTransform().translate(offset.x, 0, 0);
            }

            //z collision
            blocks.forEach((b) ->
                offset.z = b.getBoundingBox().zClipping(ab, offset.z,true)
            );
            if (offset.z != orgStep.z) {
                e.getVelocity().clear(Axis.Z);
                e.getTransform().translate(0, 0, offset.z);
            }

            e.setGrounded(orgStep.y != offset.y && orgStep.y < 0);


        });
    }
}
