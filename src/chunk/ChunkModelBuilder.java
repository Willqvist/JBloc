package chunk;

import blocks.Block;
import blocks.BlockFace;
import engine.Engine;
import engine.camera.Camera3D;
import engine.model.CustomModelAttribute;
import engine.model.ModelAttribute;
import engine.model.ModelBuilder;
import engine.texture.TextureCoordinate;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkModelBuilder implements Runnable{
    private static ChunkModelBuilder[] threads;
    private Thread thread;
    private static final Lock lock = new ReentrantLock();
    private static final Lock outLock = new ReentrantLock();
    private final static Condition emptyQueue  = lock.newCondition();
    protected static ChunkComparator comp = new ChunkComparator();
    private static TreeSet<Layer> layers = new TreeSet<Layer>(comp);
    private static volatile LinkedList<LayerModel> outChunks = new LinkedList<>();
    private static int numThreads = 0;
    private LayerModelBuilder[] builders = new LayerModelBuilder[20];
    private boolean[] occupiedBuilders = new boolean[builders.length];
    private int occupiedBuildersSize = 0;
    private Object waitLock = new Object();
    private LayerModel[] modelPool = new LayerModel[builders.length];
    private static boolean running = true;
    private int id;
    private ModelBuilder opaqueModel,transparentModel;


    private static ModelAttribute[] attributes = new ModelAttribute[]{
            new ModelAttribute(ModelAttribute.Type.POSITION,3),
            new ModelAttribute(ModelAttribute.Type.NORMALS,3),
            new ModelAttribute(ModelAttribute.Type.UV,2),
            new CustomModelAttribute(5,1)
    };

    public static void init(int numThreads){
        ChunkModelBuilder.numThreads = numThreads;
        threads = new ChunkModelBuilder[numThreads];
        Engine.window.onExit(ChunkModelBuilder::onExit);
        for(int i = 0; i < numThreads; i++){
            threads[i] =new ChunkModelBuilder(i);
            threads[i].thread = new Thread(threads[i]);
            threads[i].thread.start();
        }
    }

    private static void onExit() {
        running = false;
        lock();
        emptyQueue.signalAll();
        unlock();
        for(int i = 0; i < numThreads; i++) {
            synchronized (threads[i].waitLock){
                threads[i].waitLock.notify();
            }
        }
    }

    public static void join() {
        for(int i = 0; i < threads.length; i++) {
            try {
                threads[i].thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ChunkModelBuilder(int id){
        for(int i = 0; i < builders.length; i++) {
            builders[i] = new LayerModelBuilder();
            modelPool[i] = new LayerModel();
        }
        this.id = id;
    }
    static int i = 0;
    static int j = 0;

    public static void addChunkUnsafe(Chunk chunk){
        //lock.lock();
        //System.out.println("adding chunk: " + (i++));
        Layer[] layers = chunk.getLayers();
        for(int i = 0; i < layers.length; i++) {
            ChunkModelBuilder.layers.add(layers[i]);
        }
        emptyQueue.signalAll();
        //lock.unlock();
    }

    public static void addChunk(Chunk chunk) {
        lock();
        addChunkUnsafe(chunk);
        unlock();
    }

    public static void addLayerUnsafe(Layer layer){

        layers.add(layer);
        emptyQueue.signal();
    }

    public static void addLayer(Layer layer){
        lock();
        addLayerUnsafe(layer);
        unlock();
    }

    public static void lock(){
        lock.lock();
    }

    public static void unlock(){
        lock.unlock();
    }

    public static void generateChunks(){
        if(outLock.tryLock()) {
            while (!outChunks.isEmpty()) {
                LayerModel model = outChunks.pollFirst();
                model.layer.setModel(model.mBuilder.builders[model.builderIndex].opaqueModelBuilder.build(attributes));
                model.mBuilder.builders[model.builderIndex].opaqueModelBuilder.clear();
                model.mBuilder.occupiedBuilders[model.builderIndex] = false;
                model.mBuilder.occupiedBuildersSize --;
                synchronized (model.mBuilder.waitLock) {
                    model.mBuilder.waitLock.notify();
                }

            }
            outLock.unlock();
        }
    }

    public static boolean hasChunk() {
        return !outChunks.isEmpty();
    }

    private boolean renderLayer(Chunk c,int y){
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

    private void buildLayer(Layer layer, LayerModelBuilder builder){
        if(!layer.getChunk().hasAllNeighbours()) return;
        ModelBuilder opaqueModel = builder.opaqueModelBuilder;
        ModelBuilder transparentModel = builder.transparentModelBuilder;
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
                        if(!(b = Block.getBlock(c.getBlock(x - 1, y, z))).isRenderable())
                            addFace(c,opaqueModel,block,BlockFace.LEFT,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x + 1, y, z))).isRenderable())
                            addFace(c,opaqueModel,block,BlockFace.RIGHT,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x, y-1, z))).isRenderable())
                            addFace(c,opaqueModel,block,BlockFace.BOTTOM,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x, y+1, z))).isRenderable())
                            addFace(c,opaqueModel,block,BlockFace.TOP,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x, y, z+1))).isRenderable())
                            addFace(c,opaqueModel,block,BlockFace.FRONT,x,yl,z,y);

                        if(!(b = Block.getBlock(c.getBlock(x, y, z-1))).isRenderable())
                            addFace(c,opaqueModel,block,BlockFace.BACK,x,yl,z,y);

                    }
                }
            }
        }
    }

    private byte getMinLightValue(Chunk c,BlockFace face,float side[],int i, int x,int y,int z){
        byte min = 0;

        if(face == BlockFace.TOP || face == BlockFace.BOTTOM){
            int dirX = side[i] == 0 ? -1 : 1;
            int dirZ = side[i+2] == 0 ? -1 : 1;
            int dir = face == BlockFace.TOP ? 1 : -1;
            min += c.getLightValue(x,y+dir,z);
            min += c.getLightValue(x+dirX,y+dir,z);
            min += c.getLightValue(x,y+dir,z+dirZ);
            min += c.getLightValue(x+dirX,y+dir,z+dirZ);
        }
        else if(face == BlockFace.FRONT || face == BlockFace.BACK) {
            int dirX = side[i+0] == 0 ? -1 : 1;
            int dirY = side[i+1] == 0 ? -1 : 1;
            int dir = face == BlockFace.FRONT ? 1 : -1;
            min += c.getLightValue(x,y,z+dir);
            min += c.getLightValue(x+dirX,y,z+dir);
            min += c.getLightValue(x,y+dirY,z+dir);
            min += c.getLightValue(x+dirX,y+dirY,z+dir);
        }
        else if(face == BlockFace.LEFT || face == BlockFace.RIGHT) {
            int dir = face == BlockFace.LEFT ? -1 : 1;
            int dirZ = side[i+2] == 0 ? -1 : 1;
            int dirY = side[i+1] == 0 ? -1 : 1;
            min += c.getLightValue(x+dir,y,z);
            min += c.getLightValue(x+dir,y,z+dirZ);
            min += c.getLightValue(x+dir,y+dirY,z);
            min += c.getLightValue(x+dir,y+dirY,z+dirZ);
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
            builder.addFloat(((float)getMinLightValue(c,face,vertices,i,x,yReal,z))/15f);
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
    private float[] getFace(int x,int y,int z,int width,int height,int depth){
        return new float[]{
                x,y,z,
                x,y+height,z,
                x+width,y+height,z,
                x+width,y,z,

                //FRONT
                x,y,z+depth,
                x,y+height,z+depth,
                x+width,y+height,z+depth,
                x+width,y,z+depth,

                //LEFT
                x,y,z,
                x,y,z+depth,
                x,y+height,z+depth,
                x,y+height,z,

                //RIGHT
                x+width,y,z,
                x+width,y,z+depth,
                x+width,y+height,z+depth,
                x+width,y+height,z,

                //TOP
                x,y+height,z,
                x+width,y+height,z,
                x+width,y+height,z+depth,
                x,y+height,z+depth,

                //BOTTOM
                x,y,z,
                x+width,y,z,
                x+width,y,z+depth,
                x,y,z+depth
        };
    }

    protected static Camera3D follow;

    public static void setFollow(Camera3D camera){
        follow = camera;
    }

    private boolean canBuildMore(){
        return occupiedBuildersSize != occupiedBuilders.length;
    }

    private void waitQueue(){
        while(layers.isEmpty() && running){
            try {
                //System.out.println("waiting: " + j + " thread: " + Thread.currentThread().getName());
                emptyQueue.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitBuilders(){
        while(!canBuildMore() && running){
            try {
                //System.out.println("waiting: " + j + " thread: " + Thread.currentThread().getName());
                synchronized (waitLock) {
                    waitLock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int getFreeBuilder(){
        for(int i = 0; i < occupiedBuilders.length; i++){
            if(!occupiedBuilders[i]){
                occupiedBuilders[i] = true;
                occupiedBuildersSize ++;
                return i;
            }
        }
        return -1;
    }

    private LayerModelBuilder getBuilder(int i){
        return builders[i];
    }
    @Override
    public void run() {
        ArrayList<LayerModel> builders = new ArrayList<>();
        while(running) {
            waitBuilders();
            lock();
            waitQueue();
            while(!layers.isEmpty() && canBuildMore()) {
                Layer layer = layers.pollFirst();
                int builder = getFreeBuilder();
                if(builder < 0) break;
                builders.add(modelPool[builder].set(this, builder, layer));
            }
            unlock();
            Iterator<LayerModel> it = builders.iterator();
            while (it.hasNext()) {
                LayerModel l = it.next();
                LayerModelBuilder b = getBuilder(l.builderIndex);
                buildLayer(l.layer,b);
                outLock.lock();
                if(b.opaqueModelBuilder.isEmpty()){
                    occupiedBuildersSize--;
                    occupiedBuilders[l.builderIndex] = false;
                    it.remove();
                }
                outLock.unlock();
            }
            if(builders.size() > 0) {
                outLock.lock();
                builders.forEach(outChunks::add);
                outLock.unlock();
                builders.clear();
            }

        }
    }
    /*
    private static class OutChunk{
        ModelBuilder builder;
        private Layer layer;
        private Object lock;
        public OutChunk(Object lock,Layer layer,ModelBuilder builder) {
            this.builder = builder;
            this.layer = layer;
            this.lock = lock;
        }
    }

     */

    private static class OutChunk{
        Object syncLock;
        ModelBuilder[][] builders;
        private Chunk chunk;
        public OutChunk(Object lock, Chunk c,ModelBuilder[][] builders) {
            this.syncLock = lock;
            this.builders = builders;
            this.chunk = c;
        }
    }

    static class ChunkComparator implements Comparator<Layer>{
        private Vector2f w1=new Vector2f(),w2=new Vector2f();

        @Override
        public int compare(Layer l1, Layer l2) {
            Chunk c1 = l1.getChunk();
            Chunk c2 = l2.getChunk();
            if(c1.equals(c2)){
                return l1.getY() > l2.getY() ? -1 : 1;
            }
            w1.set(c1.getWorldPosition());
            w2.set(c2.getWorldPosition());
            Vector3f position = follow.getTransform().getPosition();
            boolean c1inFrustum = c1.getCollider().testFrustum(follow.getFrustum());
            boolean c2inFrustum = c2.getCollider().testFrustum(follow.getFrustum());
            if(c1inFrustum && !c2inFrustum) return -1;
            if(!c1inFrustum && c2inFrustum) return 1;
            return position.distanceSquared(w1.x,position.y,w1.y) < position.distanceSquared(w2.x,position.y,w2.y) ? -1 : 1;
        }
    }

    private static class LayerModelBuilder{
        private ModelBuilder opaqueModelBuilder = new ModelBuilder(50000);
        private ModelBuilder transparentModelBuilder = new ModelBuilder(50000);

    }

    private static class LayerModel{
        int builderIndex;
        Layer layer;
        ChunkModelBuilder mBuilder;
        public LayerModel set(ChunkModelBuilder mBuilder,int builderIndex, Layer layer) {
            this.builderIndex = builderIndex;
            this.layer = layer;
            this.mBuilder = mBuilder;
            return this;
        }
    }

}
