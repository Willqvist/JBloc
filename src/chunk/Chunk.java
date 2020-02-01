package chunk;

import biome.Biome;
import blocks.Block;
import blocks.WorldBlock;
import chunk.builder.ChunkLightPass;
import chunk.builder.ChunkModelBuilder;
import engine.Engine;
import engine.materials.MaterialBank;
import engine.materials.StandardMaterial;
import engine.model.ModelBuilder;
import engine.model.ModelInstance;
import engine.physics.AABB;
import engine.physics.*;
import engine.render.AABBRenderer;
import engine.render.Renderer;
import engine.tools.RoffColor;
import entities.Entity;
import entities.PhysicsEntity;
import entities.Player;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import world.World;

import java.awt.*;
import java.lang.Math;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class Chunk implements ICollideable, ICollisionPool {
    public static final int WIDTH=16;
    public static final int HEIGHT=256;
    public static final int DEPTH=16;
    public static final int AREA=WIDTH*DEPTH;
    public static final int LAYER_HEIGHT=16;
    private Layer[] layers = new Layer[HEIGHT/LAYER_HEIGHT];
    private short[] blocks = new short[WIDTH*HEIGHT*DEPTH];
    private int[] lightValue = new int[WIDTH*HEIGHT*DEPTH];
    private ArrayList<int[]> lightSources = new ArrayList<>();
    private byte[] height = new byte[WIDTH*DEPTH];
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

    public int getHeight(int x,int z){
        return height[x*DEPTH+z];
    }

    private void setHeight(int x,int z,int height){
        this.height[x*DEPTH+z] = (byte)height;
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
        }
        lightSources.clear();
        for(int x = 0; x < WIDTH; x++){
            for(int z = 0; z < DEPTH; z++){
                Biome biome = Biome.getBiome(this.x * WIDTH + x, this.z * DEPTH + z);
                int height = biome.getHeight(this.x * WIDTH + x, this.z * DEPTH + z);
                setHeight(x,z,height);
                boolean isSky = true;
                for(int y = Chunk.HEIGHT - 1; y >= 0; y--){
                    short block = biome.getBlock(this.x * WIDTH + x, y, this.z * DEPTH + z, height);
                    Block b = Block.getBlock(block);
                    //float noise = tools.NoiseBuilder.noise((getX()*Chunk.WIDTH+x)/80f,(y*Chunk.WIDTH+y)/200f,(getZ()*Chunk.DEPTH+z)/80f)*1.05f;
                    //short block = (120+(HEIGHT-120)*noise > y ? Block.GRASS : Block.AIR);
                    //if (block == Block.AIR && y < Biome.WATER_LEVEL)
                        //block = Block.WATER;

                    //if((120+(HEIGHT-120)*noise <= y)) {
                        //block = Block.AIR;
                   // }
                    setLightValue(x,y,z,(byte)0);
                    if (block == Block.AIR && isSky) {
                        setLightSource(x,y,z,true);
                        setLightValue(x, y, z, (byte) 15);
                    } else {
                        isSky = false;
                    }

                    if(b.isLightSource() && block != Block.AIR) {
                        setLightSource(x,y,z,true);
                        setLightValue(x,y,z,(byte)b.getEmissionStrength());
                    }
                    setBlock(x,y,z, block,false);
                }
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    //biome.generateStructures(chunkProvider, getWorldPosition().x + x, y, getWorldPosition().y + z);
                }
            }
        }
        ab.resize(Chunk.WIDTH,this.maxHeight+1,Chunk.DEPTH);
    }

    private void calculateLight() {
        /*
        int light = 15;
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int z = 0; z < Chunk.DEPTH; z++) {
                for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                    if (y == Chunk.HEIGHT - 1) {
                        setLightValue(x, y, z, (byte) (Block.getBlock(getBlock(x, y, z)).getLightPenetration()));
                    } else {
                        setLightValue(x, y, z, (byte) Math.max(0,getLightValue(x, y + 1, z) - (15-Block.getBlock(getBlock(x, y, z)).getLightPenetration())));
                    }
                }
            }
        }
         */
    }

    private boolean isOutside(int x,int y,int z) {
        Vector2i pos = ChunkTools.toChunkPosition(x,z);
        return false;
    }

    private Vector3i lightSrc = new Vector3i(0,0,0);
    private void calcLight(int x,int y,int z, int str) {
        if(str <= 0) {
            return;
        }
        /*

        int x = pos.x;
        int z = pos.y;
         */
        Block left = Block.getBlock(getBlock(x-1,y,z));
        Block right = Block.getBlock(getBlock(x+1,y,z));
        Block front = Block.getBlock(getBlock(x,y,z+1));
        Block back = Block.getBlock(getBlock(x,y,z-1));
        Block top = Block.getBlock(getBlock(x,y+1,z));
        Block bottom = Block.getBlock(getBlock(x,y-1,z));
        int newStr = str-1;
        if(newStr > getLightValue(x-1,y,z) && !left.isSolid()) {
            setLightValue(x-1,y,z, newStr);
            calcLight(x-1,y,z, newStr);
        }
        if(newStr > getLightValue(x+1,y,z) && !right.isSolid()) {
            setLightValue(x+1,y,z, newStr);
            calcLight(x+1,y,z, newStr);
        }
        if(newStr > getLightValue(x,y,z+1) && !front.isSolid()) {
            setLightValue(x,y,z+1, newStr);
            calcLight(x,y,z+1, newStr);
        }
        if(newStr > getLightValue(x,y,z-1) && !back.isSolid()) {
            setLightValue(x,y,z-1, newStr);
            calcLight(x,y,z-1, newStr);
        }
        if(newStr > getLightValue(x,y+1,z) && !top.isSolid()) {
            setLightValue(x,y+1,z, newStr);
            calcLight(x,y+1,z, newStr);
        }
        if(newStr > getLightValue(x,y-1,z) && !bottom.isSolid()) {
            setLightValue(x,y-1,z, newStr);
            calcLight(x,y-1,z, newStr);
        }

    }
    private Object lightLock = new Object();
    public void calculateLights() {
        synchronized (lightLock) {
            if(!lightDirty)
                return;
            lightDirty = false;
            setLights(true);
        }
    }

    //TODO: remove lights when all surrounding chunks has been built so that no unecessary lights exist.
    private void setLights(boolean rebuild) {
        if(rebuild) {
            for (int x = 0; x < WIDTH; x++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int y = 0; y < HEIGHT; y++) {
                        setLightValue(x, y, z,0);
                    }
                }
            }
        }
        for(int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if(isLightSource(x-1,y,z) &&
                            isLightSource(x+1,y,z) &&
                            isLightSource(x,y+1,z) &&
                            isLightSource(x,y-1,z) &&
                            isLightSource(x,y,z-1) &&
                            isLightSource(x,y,z+1) &&
                            isLightSource(x,y,z)) {

                    } else if(isLightSource(x,y,z)) {
                        int str = Block.getBlock(getBlock(x,y,z)).getEmissionStrength();
                        setLightValue(x,y,z,Block.getBlock(getBlock(x,y,z)).getEmissionStrength());
                        calcLight(x,y,z,str);
                    }
                    /*
                    setLightValue(x,y,z,(byte)0);
                    short block = getBlock(x,y,z);
                    Block b = Block.getBlock(block);

                    if (block == Block.AIR && isSky) {
                        lightSources.add(new int[]{x, y,z});
                        setLightSource(x,y,z);
                    } else {
                        isSky = false;
                    }


                    if (isSky && block == Block.AIR) {
                        setLightValue(x, y, z, (byte) 15);
                    }
                    else if(b.isLightSource() && block != Block.AIR) {
                        System.out.println("adding torch!!: " + block);
                        lightSources.add(new int[]{x, y,z});
                        setLightSource(x,y,z);
                        setLightValue(x,y,z,(byte)b.getEmissionStrength());
                    }

                     */

                }
            }
        }
    }

    public Layer getLayer(int y){
        return layers[y/LAYER_HEIGHT];
    }

    public void setLightValue(int x,int y, int z,int value) {

        Chunk n;

        if(y < 0 || y >= Chunk.HEIGHT) {
            return;
        }
        if((x < 0 && (n=getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=getNeighbour(Neighbour.RIGHT)) != null)) {
            n.setLightValue(ChunkTools.toBlockPosition(x), y, z, value);
        }
        else if((z < 0 && (n=getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=getNeighbour(Neighbour.BACK)) != null)) {
            n.setLightValue(x, y, ChunkTools.toBlockPosition(z), value);
        } else {
            int curValue = this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z];
            this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z] = curValue & ~0XF | value;
            DirtyLayerProvider.addLayer(getLayer(y));
        }
    }

    private void setLightSource(int x,int y,int z, boolean isLight) {
        int curValue = this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z];
        if(isLight) {
            this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z] = curValue | 0X10;
        } else {
            this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z] = curValue & ~0X10;
        }
    }

    private boolean isLightSource(int x,int y,int z) {
        Chunk n;

        if((x < 0 && (n=getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=getNeighbour(Neighbour.RIGHT)) != null))
            return n.isLightSource(ChunkTools.toBlockPosition(x),y,z);
        if((z < 0 && (n=getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=getNeighbour(Neighbour.BACK)) != null))
            return n.isLightSource(x,y,ChunkTools.toBlockPosition(z));
        if(y >= Chunk.HEIGHT || y < 0) return true;
        //System.out.println((this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z] & 0X10));
        return (this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z] & 0X10) == 0X10;
    }

    public int getLightValue(int x,int y, int z) {
        Chunk n;

        if((x < 0 && (n=getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=getNeighbour(Neighbour.RIGHT)) != null))
            return n.getLightValue(ChunkTools.toBlockPosition(x),y,z);
        if((z < 0 && (n=getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=getNeighbour(Neighbour.BACK)) != null))
            return n.getLightValue(x,y,ChunkTools.toBlockPosition(z));

        if(isOutsideY(y)) return 15;

        if(x < 0 || z >= Chunk.DEPTH || z < 0 || x >= Chunk.WIDTH)
            return 15;
        return this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z] & 0xF;
    }

    private static RoffColor c = RoffColor.from(Color.RED);
    private static RoffColor c2 = RoffColor.from(Color.ORANGE);
    public void render(Renderer renderer){
        /*
        if(!isBuilt()) {
        AABBRenderer.setColor(c);
        AABBRenderer.render(renderer, this.ab);
        } else {
            AABBRenderer.setColor(c2);
            AABBRenderer.render(renderer, this.ab);
        }
         */
        if(!renderable) return;
        //AABBRenderer.setColor(col);
        material.setAlbedoTexture(Block.texture);
        for (int i = 0; i < layers.length; i++) {
            layers[i].render(renderer);
        }
    }

    public void renderTransparency(Renderer renderer) {
        if(!renderable) return;
        //AABBRenderer.setColor(col);
        //AABBRenderer.render(renderer,this.ab);
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

    private void setBlock(int x,int y,int z, short block,boolean built) {
        if(y > maxHeight && Block.getBlock(block).isRenderable()){
            maxHeight = y;
            ab.resize(Chunk.WIDTH,maxHeight,Chunk.DEPTH);
        } else if(y == maxHeight && !Block.getBlock(block).isRenderable()) {
            maxHeight = y;
            setHeight(x,z,y);
        }

        dirty = true;
        lightDirty = true;
        if(!built)
            getLayer(y).onBlockSet(x, y, z, block);
        else {
            if(block == Block.AIR && getBlock(x,y+1,z) == Block.AIR && isLightSource(x,y+1,z)) {
                System.out.println("above was a sky light!!dwwawds ");
                setLightSource(x,y,z,true);
                skyLightRecurse(x,y-1,z,true);
            }
            else if(!Block.getBlock(block).isLightSource()) {
                System.out.println("n ot light source!!");
                if(getBlock(x,y,z) == Block.AIR && isLightSource(x,y,z)) {
                    skyLightRecurse(x,y-1,z,false);
                }
                setLightSource(x,y,z,false);
            } else {
                System.out.println("is light source!!!!!");
                setLightSource(x,y,z,true);
            }
            getLayer(y).onNewBlockSet(x, y, z, block);
        }

        this.blocks[x * HEIGHT * DEPTH + y * DEPTH + z] = block;
    }

    private void skyLightRecurse(int x,int y,int z, boolean isLight) {
        if(y == 0) {
            return;
        }
        short block = getBlock(x,y,z);
        if((isLight || isLightSource(x,y,z)) && block == Block.AIR) {
            setLightSource(x,y,z,isLight);
            skyLightRecurse(x,y-1,z,isLight);
        }
    }

    public void setBlock(int x,int y,int z, short block){
        setBlock(x,y,z,block,built);
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

        return this.blocks[x * HEIGHT * DEPTH + y * DEPTH + z];
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
    private static Vector3d stepMoved = new Vector3d(0,0,0);
    @Override
    public void executeCollisions() {
        physicsEntities.forEach((e)-> {
            Vector3d step = e.getVelocity().getVelocity();
            //System.out.println("STEP: " + step.y + " | " + e.getTransform().getPosition().y + " | " + Thread.currentThread().getName());
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
