package chunk;

import blocks.Block;
import blocks.WorldBlock;
import engine.Engine;
import engine.materials.MaterialBank;
import engine.materials.StandardMaterial;
import engine.physics.AABB;
import engine.physics.*;
import engine.render.AABBRenderer;
import engine.render.Renderer;
import entities.Entity;
import entities.PhysicsEntity;
import org.joml.*;
import world.World;

import java.lang.Math;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Chunk implements ICollideable, ICollisionPool {
    public static final int WIDTH=16;
    public static final int HEIGHT=256;
    public static final int DEPTH=16;
    public static final int AREA=WIDTH*DEPTH;
    public static final int LAYER_HEIGHT=16;
    private Layer[] layers = new Layer[HEIGHT/LAYER_HEIGHT];
    private short[] blocks = new short[WIDTH*HEIGHT*DEPTH];
    private byte[] lightValue = new byte[WIDTH*HEIGHT*DEPTH];
    private boolean built=false;
    private Chunk[] neightbours = new Chunk[4];
    private int height=0;
    //old model if dirty = true, model needs to be remodeled...
    private boolean dirty = true;
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
        ab = new AABB(x*Chunk.WIDTH,0,z*Chunk.DEPTH,Chunk.WIDTH,0,Chunk.DEPTH);
        for(int i = 0; i < layers.length; i++){
            layers[i] = new Layer(this,i*LAYER_HEIGHT,LAYER_HEIGHT);
        }

        if(material == null) {
            material = MaterialBank.getMaterial("chunk",StandardMaterial.class);
        }

    }

    public Layer[] getLayers(){
        return layers;
    }

    public boolean isBuilt(){
        return built;
    }

    public void generateBlocks(){
        if(built) return;
        for(int x = 0; x < WIDTH; x++){
            for(int z = 0; z < DEPTH; z++){
                float noise = SimplexNoise.noise((getX()*Chunk.WIDTH+x)/100f,(getZ()*Chunk.DEPTH+z)/100f)*0.1f;
                for(int y = 0; y < Chunk.HEIGHT; y++){
                    short block = (120+(HEIGHT-120)*noise > y ? Block.GRASS : Block.AIR);
                    if( y < 115 && block == Block.AIR) {
                        block = Block.WATER;
                    }
                    setBlock(x,y,z, block);
                    setLightValue(x,y,z, (byte) Block.getBlock(block).getLightPenetration());
                }
            }
        }
        ab.resize(Chunk.WIDTH,height+1,Chunk.DEPTH);
    }

    protected Layer getLayer(int y){
        return layers[y/LAYER_HEIGHT];
    }

    public void setLightValue(int x,int y, int z,byte value) {
        this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z] = value;
    }

    public byte getLightValue(int x,int y, int z) {
        Chunk n;

        if((x < 0 && (n=getNeighbour(Neighbour.LEFT)) != null )|| (x >= Chunk.WIDTH && (n=getNeighbour(Neighbour.RIGHT)) != null))
            return n.getLightValue(ChunkTools.toBlockPosition(x),y,z);
        if((z < 0 && (n=getNeighbour(Neighbour.FRONT)) != null )|| (z >= Chunk.DEPTH && (n=getNeighbour(Neighbour.BACK)) != null))
            return n.getLightValue(x,y,ChunkTools.toBlockPosition(z));

        if(isOutsideY(y)) return (byte)15;

        if(x < 0 || z >= Chunk.DEPTH || z < 0 || x >= Chunk.WIDTH)
            return (byte)15;
        return this.lightValue[x * HEIGHT * DEPTH + y * DEPTH + z];
    }

    private boolean shouldRender = false;

    public void testFrustum(FrustumIntersection frustumIntersection) {
        shouldRender = getCollider().testFrustum(frustumIntersection);
    }

    public void render(Renderer renderer){
        if(!renderable || !shouldRender) return;
        material.setAlbedoTexture(Block.texture);
        for(int i = 0; i < layers.length; i++){
            layers[i].render(renderer);
        }
    }

    public void renderTransparent(Renderer renderer){
        if(!renderable || !shouldRender) return;
        for(int i = 0; i < layers.length; i++){
            layers[i].renderTransparentLayer(renderer);
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
        return true;
    }

    public void setNeighbour(Chunk c,Neighbour neighbour){
        neightbours[neighbour.ordinal()] = c;
    }
    public Chunk getNeighbour(Neighbour neighbour){
        return neightbours[neighbour.ordinal()];
    }

    public void setBlock(int x,int y,int z, short block){
        if(y > height && Block.getBlock(block).isRenderable()){
            height = y;
            ab.resize(Chunk.WIDTH,height,Chunk.DEPTH);
        }

        dirty = true;
        if(!built)
            getLayer(y).onBlockSet(x, y, z, block);
        else
            getLayer(y).onNewBlockSet(x,y,z,block);

        this.blocks[x * HEIGHT * DEPTH + y * DEPTH + z] = block;
        setLightValue(x,y,z,(byte)Block.getBlock(block).getLightPenetration());
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

    protected boolean hasAllNeighbours() {
        return getNeighbour(Neighbour.LEFT) != null &&
                getNeighbour(Neighbour.RIGHT) != null &&
                getNeighbour(Neighbour.FRONT) != null &&
                getNeighbour(Neighbour.BACK) != null;
    }

    public void rebuild() {
        if(!dirty || !canBuildModel()) return;

        ChunkModelBuilder.lock();
        for(int i = 0; i < layers.length; i++){
            layers[i].rebuild();
        }
        ChunkModelBuilder.unlock();
        ChunkModelBuilder.unlock();
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
    private List<WorldBlock> getCollidingBlocks(AABB ab, Vector3f offset) {
        collisionResult.clear();
        Vector3f pos = ab.getPosition();

        colBlockPos.set((int)Math.floor(pos.x + (offset.x < 0 ? offset.x : 0)),(int)Math.floor(pos.y + (offset.y < 0 ? offset.y : 0)),(int)Math.floor(pos.z + (offset.z < 0 ? offset.z : 0)));
        colBlockSize.set((int)Math.floor(pos.x + ab.getWidth() + (offset.x > 0 ? offset.x : 0))+1,(int)Math.floor(pos.y + ab.getHeight() + (offset.y > 0 ? offset.y : 0))+1,(int)Math.floor(pos.z + ab.getDepth() + (offset.z > 0 ? offset.z : 0))+1);

        for (int x = colBlockPos.x; x < colBlockSize.x; ++x) {
            for (int y = colBlockPos.y; y < colBlockSize.y; ++y) {
                for (int z = colBlockPos.z; z < colBlockSize.z; ++z) {
                    Block b;
                    Chunk c = chunkProvider.getChunk(ChunkTools.toChunkPosition(x,z));
                    Vector3i blockPos = ChunkTools.toBlockPosition(x,y,z);
                    if ((b = Block.getBlock(c.getBlock(blockPos.x,blockPos.y,blockPos.z))).isSolid())
                        collisionResult.add(new WorldBlock(x,y,z,b.getId()));
                }
            }
        }
        return collisionResult;
    }
    private static Vector3f offset = new Vector3f(0,0,0);
    @Override
    public void executeCollisions() {
        physicsEntities.forEach((e)-> {
            Vector3f step = e.getVelocity().getVelocity();
            //System.out.println(step.y);
            if(e.getCollider().getType() != ICollider.Type.AABB) return;
            offset.set(step.x,step.y,step.z);
            List<WorldBlock> blocks = getCollidingBlocks((AABB)e.getCollider(), offset);
            //System.out.println(blocks.size() + " | " + step.y);
            AABB ab = (AABB)e.getCollider();
            e.setGrounded(false);
            if(blocks.size() == 0) return;
            //y collision
            blocks.forEach((b) ->
            {
                offset.y = b.getBoundingBox().yClipping(ab, offset.y);
            });
            if (offset.y != step.y) {
                e.getTransform().translate(0, offset.y, 0);

            }

            //x collision
            blocks.forEach((b) ->
            {

                offset.x = b.getBoundingBox().xClipping(ab, offset.x);
            });
            if (offset.x != step.x) {
                e.getVelocity().clear(Axis.X);
                e.getTransform().translate(offset.x, 0, 0);
            }

            //z collision
            blocks.forEach((b) ->
            {
                offset.z = b.getBoundingBox().zClipping(ab, offset.z);
            });
            if (offset.z != step.z) {
                e.getTransform().translate(0, 0, offset.z);
                e.getVelocity().clear(Axis.Z);
            }

            e.setGrounded(step.y != offset.y && step.y < 0);

            if(step.y != offset.y) {
                e.getVelocity().clear(Axis.Y);
            }


        });
    }
}
