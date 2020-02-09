package world;

import biome.Biome;
import biome.BiomeHandler;
import biome.generator.NormalBiomGenerator;
import blocks.Block;
import chunk.*;
import chunk.builder.ChunkBlockBuilder;
import chunk.builder.ChunkLightPass;
import chunk.builder.ChunkModelBuilder;
import engine.Engine;
import engine.camera.Camera3D;
import engine.render.AABBRenderer;
import engine.render.Renderer;
import entities.Entity;
import entities.PhysicsEntity;
import entities.Player;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import settings.Settings;

import java.util.*;

public class World implements ChunkProvider{

    private ArrayList<Chunk> chunks = new ArrayList<>();
    private HashMap<Coord,Chunk> chunkMap = new HashMap<>();
    private Camera3D camera;
    private Player player;
    private int lastPosX,lastPosZ;
    private ChunkCache cache;
    private ChunkSortComparator sortComparator = new ChunkSortComparator();
    public World(){
        Biome.setGenerator(new NormalBiomGenerator());
        camera = (Camera3D) Engine.camera.getCamera("main");
        int processors = Runtime.getRuntime().availableProcessors();
        ChunkModelBuilder.setFollow(camera);
        ChunkBlockBuilder.init(2);
        ChunkModelBuilder.init(1);
        //ChunkLightPass.init(2);
        player = new Player(this);
        addEntity(player);
        AABBRenderer.init();
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

        loadChunks();

    }

    public void addEntity(Entity entity){
        System.out.println("this will also be valled");
    }

    private Vector2i cPos = new Vector2i(0,0);

    public void addEntity(PhysicsEntity entity){
        Vector3d pos = entity.getTransform().getPosition();
        Vector2i cp = ChunkTools.toChunkPosition(pos.x,pos.z,cPos);

        Engine.physics.addPhysicsBody(entity);
    }

    public void update(){

        player.update();
        if(player.hasMoved()){
            chunks.sort(sortComparator);
            loadChunks();
        }

    }

    private int sortChunks(Chunk c1, Chunk c2) {
        Vector3d pos = player.getTransform().getPosition();
        double dist = c1.getWorldPosition().distance((int)pos.x,(int)pos.z);
        double dist2 = c2.getWorldPosition().distance((int)pos.x,(int)pos.z);
        return dist < dist2 ? 1 : -1;
    }

    private void loadChunks() {
        Vector3d pos = player.getTransform().getPosition();
        Vector2i cp = ChunkTools.toChunkPosition((int)pos.x,(int)pos.z,cPos);
        if(!(lastPosX == cp.x && lastPosZ == cp.y)) {
            lastPosX = cp.x;
            lastPosZ = cp.y;
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
            cullChunks(pos);
        }
    }


    private void cullChunks(Vector3d pos){
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
        ChunkBlockBuilder.addChunk(c);
    }

    private void addChunk(Chunk c){
        int x = c.getX();
        int z = c.getZ();

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

        if((neigh = getChunk(x+1,z+1)) != null){
            neigh.setNeighbour(c, Neighbour.LEFT_FRONT);
            c.setNeighbour(neigh,Neighbour.RIGHT_BACK);
        }
        if((neigh = getChunk(x-1,z+1)) != null){
            neigh.setNeighbour(c, Neighbour.RIGHT_FRONT);
            c.setNeighbour(neigh,Neighbour.LEFT_BACK);
        }
        if((neigh = getChunk(x+1,z-1)) != null){
            neigh.setNeighbour(c, Neighbour.LEFT_BACK);
            c.setNeighbour(neigh,Neighbour.RIGHT_FRONT);
        }
        if((neigh = getChunk(x-1,z-1)) != null){
            neigh.setNeighbour(c, Neighbour.RIGHT_BACK);
            c.setNeighbour(neigh,Neighbour.LEFT_FRONT);
        }

        Coord cord = new Coord(x,z);
        if(chunkMap.containsKey(cord)) return;
        chunks.add(c);
        chunkMap.put(cord,c);

    }

    public void render(Renderer renderer){
        for(int i = 0; i < chunks.size(); i++){
            //if(chunks.get(i).getCollider().testFrustum(camera.getFrustum()))
                chunks.get(i).render(renderer);
        }

        player.render(renderer);
    }

    public void renderTransparency(Renderer renderer) {
        for(int i = 0; i < chunks.size(); i++){
            //if(chunks.get(i).getCollider().testFrustum(camera.getFrustum()))
                chunks.get(i).renderTransparency(renderer);
        }
    }

    private Chunk previousChunk;
    private Vector3i worldBlockPos = new Vector3i(0,0,0);
    public short getBlock(int x, int y,int z){
        Vector2i p = ChunkTools.toChunkPosition(x,z,cPos);
        Vector3i blockPos = ChunkTools.toBlockPosition(x,y,z,worldBlockPos);
        return getChunk(p.x,p.y).getBlock(blockPos.x,blockPos.y,blockPos.z);
    }

    @Override
    public int getHeight(int x, int z) {
        Chunk c = getChunk(ChunkTools.toChunkPosition(x,z,cPos));
        if(c == null) return 0;

        return c.getHeight(ChunkTools.toBlockPosition(x),ChunkTools.toBlockPosition(z));
    }

    @Override
    public void start() {

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
        ChunkModelBuilder.stop();
    }

    public void setBlock(int x, int y, int z, short blockid) {
        Chunk c = getChunk(ChunkTools.toChunkPosition(x,z,cPos));
        if(c == null) {
            Vector2i p = ChunkTools.toChunkPosition(x,z,cPos);
            return;
        }
        Vector3i bPos = ChunkTools.toBlockPosition(x,y,z,worldBlockPos);
        c.setBlock(bPos.x, bPos.y, bPos.z, blockid);
    }

    private class ChunkSortComparator implements Comparator<Chunk> {

        @Override
        public int compare(Chunk c1, Chunk c2) {
            Vector3d pos = player.getTransform().getPosition();
            double dist = c1.getWorldPosition().distanceSquared((int)pos.x,(int)pos.z);
            double dist2 = c2.getWorldPosition().distanceSquared((int)pos.x,(int)pos.z);
            if(dist < dist2) {
                return -1;
            } else if(dist2 > dist) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
