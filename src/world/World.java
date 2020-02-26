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
import engine.render.CubeMap;
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
    private HashMap<Integer,Chunk> chunkMap = new HashMap<>();
    private HashMap<Integer,Chunk> activeChunks = new HashMap<>();
    private Camera3D camera;
    private Player player;
    private int lastPosX,lastPosZ;
    private ChunkCache cache;
    private ChunkSortComparator sortComparator = new ChunkSortComparator();
    private CubeMap map;
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
        this.map = new CubeMap("day");
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


    public void addEntity(PhysicsEntity entity){
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
        int cpx = ChunkTools.toChunkPosition(pos.x);
        int cpz = ChunkTools.toChunkPosition(pos.z);
        if(!(lastPosX == cpx && lastPosZ == cpz)) {
            lastPosX = cpx;
            lastPosZ = cpz;
            for (int i = -Settings.renderDistance; i <= Settings.renderDistance; i++) {
                for (int j = -Settings.renderDistance; j <= Settings.renderDistance; j++) {
                    int cx = cpx + i;
                    int cz = cpz + j;
                    int worldX = cx * Chunk.WIDTH;
                    int worldZ = cz * Chunk.DEPTH;
                    if (pos.distance(worldX, pos.y, worldZ) > Settings.renderDistance * 23) continue;
                    int hash = Objects.hash(cpx,cpz);
                    if(activeChunks.containsKey(hash)) {
                        System.out.println("HERE DUDE!!");
                        addChunkBuild(activeChunks.get(hash),false);
                    } else {
                        Chunk c = getChunk(cx, cz);
                        if (c == null) {
                            Chunk nc = new Chunk(this, cx, cz);
                            addChunkBuild(nc, false);
                        } else {
                        }
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
                chunkMap.remove(Objects.hash(c.getX(),c.getZ()));
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
        int code = Objects.hash(x,z);
        if(chunkMap.containsKey(code)) return;

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

        chunks.add(c);
        chunkMap.put(code,c);

    }

    public void render(Renderer renderer){
        map.render(renderer);
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
    public short getBlock(int x, int y,int z){
        int cpx = ChunkTools.toChunkPosition(x);
        int cpz = ChunkTools.toChunkPosition(z);
        int bx = ChunkTools.toBlockPosition(x);
        int bz = ChunkTools.toBlockPosition(z);
        //System.out.println(cpx + " | " + cpz);
        Chunk c = getChunk(cpx,cpz);
        if(c == null)
            return Block.AIR;
        return c.getBlock(bx,y,bz);
    }

    @Override
    public int getHeight(int x, int z) {
        int cpx = ChunkTools.toChunkPosition(x);
        int cpz = ChunkTools.toChunkPosition(z);
        Chunk c = getChunk(cpx,cpz);
        if(c == null) return 0;
        int bx = ChunkTools.toBlockPosition(x);
        int bz = ChunkTools.toBlockPosition(z);
        return c.getHeight(bx,bz);
    }

    @Override
    public void start() {

    }

    public synchronized Chunk getChunk(int x, int z){

        if(previousChunk != null && x == previousChunk.getX() && z == previousChunk.getZ()){
            return previousChunk;
        }
        int hash = Objects.hash(x,z);
        previousChunk = chunkMap.get(hash);
        if(previousChunk == null) {
            previousChunk = activeChunks.get(hash);
        }
        return previousChunk;

    }

    public synchronized Chunk getChunk(Vector2i pos){
        return getChunk(pos.x,pos.y);
    }

    public synchronized Chunk getWorldChunk(int x,int z){
        int cpx = ChunkTools.toChunkPosition(x);
        int cpz = ChunkTools.toChunkPosition(z);
        return getChunk(cpx,cpz);
    }

    public void end() {
        ChunkBlockBuilder.join();
        ChunkModelBuilder.stop();
    }

    public synchronized void setBlock(int x, int y, int z, short blockid) {
        int cpx = ChunkTools.toChunkPosition(x);
        int cpz = ChunkTools.toChunkPosition(z);
        Chunk c = getChunk(cpx,cpz);
        if(c == null) {
            c = new Chunk(this,cpx,cpz);
            int bx = ChunkTools.toBlockPosition(x);
            int bz = ChunkTools.toBlockPosition(z);
            c.setBlock(bx,y,bz,blockid);
            activeChunks.put(Objects.hash(cpx,cpz),c);
            return;
        }
        int bx = ChunkTools.toBlockPosition(x);
        int bz = ChunkTools.toBlockPosition(z);
        c.setBlock(bx,y,bz, blockid);
    }

    public void placeBlock(Entity entity, int x, int y, int z, short blockid) {
        int cpx = ChunkTools.toChunkPosition(x);
        int cpz = ChunkTools.toChunkPosition(z);
        Chunk c = getChunk(cpx,cpz);
        if(c == null) {
            return;
        }
        int bx = ChunkTools.toBlockPosition(x);
        int bz = ChunkTools.toBlockPosition(z);
        c.placeBlock(entity,bx,y,bz, blockid);
    }

    private class ChunkSortComparator implements Comparator<Chunk> {

        @Override
        public int compare(Chunk c1, Chunk c2) {
            Vector3d pos = player.getTransform().getPosition();
            double dist = c1.getWorldPosition().distanceSquared((int)pos.x,(int)pos.z);
            double dist2 = c2.getWorldPosition().distanceSquared((int)pos.x,(int)pos.z);
            if(dist < dist2) {
                return 1;
            } else if(dist2 > dist) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
