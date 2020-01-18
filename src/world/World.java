package world;

import blocks.Block;
import chunk.*;
import engine.Engine;
import engine.camera.Camera3D;
import engine.physics.IPhysicsBody;
import engine.render.AABBRenderer;
import engine.render.Renderer;
import entities.Entity;
import entities.PhysicsEntity;
import entities.Player;
import entities.Stone;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import settings.Settings;

import java.text.NumberFormat;
import java.util.*;

public class World implements ChunkProvider{

    private ArrayList<Chunk> chunks = new ArrayList<>();
    private HashMap<Coord,Chunk> chunkMap = new HashMap<>();
    private Camera3D camera;
    private Player player;
    private int lastPosX,lastPosZ;
    private ChunkCache cache;
    public World(){
        camera = (Camera3D) Engine.camera.getCamera("main");
        player = new Player(this);
        addEntity(player);
        AABBRenderer.init();
        int processors = Runtime.getRuntime().availableProcessors();
        ChunkBlockBuilder.init(1);
        ChunkModelBuilder.init(4);
        ChunkModelBuilder.setFollow(camera);
        this.cache = new ChunkCache();
        /*
        ChunkBlockBuilder.lock();
            for(int i = 0; i < 32; i++){
                for(int j = 0; j < 32; j++){
                    addChunk(new Chunk(i,j),false);
                }
            }
        ChunkBlockBuilder.unlock();

         */

    }

    public void addEntity(Entity entity){
        System.out.println("this will also be valled");
    }

    public void addEntity(PhysicsEntity entity){
        Vector3f pos = entity.getTransform().getPosition();
        Vector2i cp = ChunkTools.toChunkPosition(pos.x,pos.z);

        Engine.physics.addPhysicsBody(entity);
    }

    public void update(){
        if(ChunkModelBuilder.hasChunk()){
            ChunkModelBuilder.generateChunks();
        }
        player.update();
        if(player.hasUpdated()) {
            for(int i = 0; i < chunks.size(); i++){
                chunks.get(i).testFrustum(camera.getFrustum());
            }
        }

        if(player.hasMoved()){

            Vector3f pos = player.getTransform().getPosition();
            Vector2i cp = ChunkTools.toChunkPosition((int)pos.x,(int)pos.z);
            if(!(lastPosX == cp.x && lastPosZ == cp.y)) {
                lastPosX = cp.x;
                lastPosZ = cp.y;

                if(ChunkBlockBuilder.tryLock()) {
                    for (int i = -Settings.renderDistance; i <= Settings.renderDistance; i++) {
                        for (int j = -Settings.renderDistance; j <= Settings.renderDistance; j++) {
                            int cx = cp.x + i;
                            int cz = cp.y + j;
                            int worldX = cx * Chunk.WIDTH;
                            int worldZ = cz * Chunk.DEPTH;
                            if (pos.distance(worldX, pos.y, worldZ) > Settings.renderDistance * 23) continue;
                            Chunk c = getChunk(cx, cz);
                            if (c == null) {
                                Chunk nc = new Chunk(this,cx, cz);
                                addChunkBuild(nc, false);
                            } else {
                            /*
                        if(pos.distance(worldX,pos.y,worldZ) > Settings.renderDistance*23){
                            c.setRenderable(false);
                        }else{
                            c.setRenderable(true);
                        }

                             */


                            }

                    /*
                    for (int i = 0; i < chunks.size(); i++) {
                        Chunk c = chunks.get(i);
                        if (c.getWorldPosition().distance(pos.x, pos.z) > Settings.renderDistance) {
                            chunks.remove(i);
                            i--;
                        }
                    }

                     */
                        }
                    }
                    ChunkBlockBuilder.unlock();
                    cullChunks(pos);
                }
            }
        }

    }



    private void cullChunks(Vector3f pos){
        Iterator<Chunk> it = chunks.iterator();
        while(it.hasNext()){
            Chunk c = it.next();
            int worldX = c.getX() * Chunk.WIDTH;
            int worldZ = c.getZ() * Chunk.DEPTH;
            if (pos.distance(worldX, pos.y, worldZ) > Settings.renderDistance * 23){
                cord.set(c.getX(),c.getZ());
                chunkMap.remove(cord);
                c.destroy();
                cache.addCache(c);
                it.remove();
            }
        }

    }


    private void addChunkBuild(Chunk c,boolean safe) {
        addChunk(c);
        if(safe)
            ChunkBlockBuilder.addChunk(c);
        else
            ChunkBlockBuilder.addChunkUnsafe(c);
    }

    private void addChunk(Chunk c){
        int x = c.getX();
        int z = c.getZ();
        Coord cord = new Coord(x,z);
        if(chunkMap.containsKey(cord)) return;
        chunks.add(c);
        chunkMap.put(cord,c);

        Chunk neigh;
        if((neigh = getChunk(x-1,z)) != null){
            neigh.setNeighbour(c, Neighbour.RIGHT);
            c.setNeighbour(neigh,Neighbour.LEFT);
        }
        if((neigh = getChunk(x+1,z)) != null){
            neigh.setNeighbour(c, Neighbour.LEFT);
            c.setNeighbour(neigh,Neighbour.RIGHT);
        }
        if((neigh = getChunk(x,z-1)) != null){
            neigh.setNeighbour(c, Neighbour.BACK);
            c.setNeighbour(neigh,Neighbour.FRONT);
        }
        if((neigh = getChunk(x,z+1)) != null){
            neigh.setNeighbour(c, Neighbour.FRONT);
            c.setNeighbour(neigh,Neighbour.BACK);
        }
    }

    public void render(Renderer renderer){
        Engine.window.enableDoubleSideRender(false);
        for(int i = 0; i < chunks.size(); i++){
            chunks.get(i).render(renderer);
        }

        Engine.window.enableDoubleSideRender(true);

        for(int i = 0; i < chunks.size(); i++){
            chunks.get(i).renderTransparent(renderer);
        }
        player.render(renderer);
    }

    private Chunk previousChunk;

    public short getBlock(int x, int y,int z){
        Vector2i p = ChunkTools.toChunkPosition(x,z);
        Vector3i blockPos = ChunkTools.toBlockPosition(x,y,z);
        return getChunk(p.x,p.y).getBlock(blockPos.x,blockPos.y,blockPos.z);
    }

    private static Coord cord = new Coord(0,0);

    public Chunk getChunk(int x, int z){

        if(previousChunk != null && x == previousChunk.getX() && z == previousChunk.getZ()){
            return previousChunk;
        }
        cord.set(x,z);
        previousChunk = chunkMap.get(cord);
        return previousChunk;

    }

    public Chunk getChunk(Vector2i pos){
        return getChunk(pos.x,pos.y);
    }

    public void end() {
        ChunkBlockBuilder.join();
        ChunkModelBuilder.join();
    }

    public void setBlock(int x, int y, int z, short blockid) {
        Chunk c = getChunk(ChunkTools.toChunkPosition(x,z));
        Vector3i bPos = ChunkTools.toBlockPosition(x,y,z);
        c.setBlock(bPos.x, bPos.y, bPos.z, blockid);
    }
}
