package entities;

import biome.Biome;
import biome.BiomeHandler;
import blocks.Block;
import chunk.Chunk;
import chunk.ChunkTools;
import chunk.DirtyLayerProvider;
import engine.Engine;
import engine.camera.Camera;
import engine.camera.Camera3D;
import engine.camera.CameraMath;
import engine.physics.AABB;
import engine.physics.Axis;
import engine.physics.Force;
import engine.physics.Physics;
import engine.render.AABBRenderer;
import engine.render.Renderer;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import tools.RayTracer;
import world.World;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;

public class Player extends PhysicsEntity {
    private Camera3D camera;
    private int jumpTick=-10;
    public Player(World world) {
        super(world);
        camera = (Camera3D)Engine.camera.getCamera("main");
        speed = 0.12f;
        ((AABB)getCollider()).resize(0.8f,1.8f,0.8f);
        ((AABB)getCollider()).setOffset(0.1f,0,0.1f);
        camera.follow(this.getTransform());
        camera.getTransform().setPosition(0.5f,1.8f,0.5f);
        getTransform().setPosition(200000,320,49100);
        //System.out.println((int)Float.MAX_VALUE);
        camera.update();
    }


    @Override
    protected void onUpdate() {
        if(Engine.key.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)){
            Engine.window.unlockMouse();
        }
        if(Engine.key.isKeyPressed(GLFW.GLFW_KEY_TAB)){
            Engine.window.lockMouse();
        }

        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_W)){
            Vector3d dir = camera.getDirection(Camera.Direction.FORWARD);
            move(dir.x,0,dir.y);
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_S)){
            Vector3d dir = camera.getDirection(Camera.Direction.BACKWARD);
            move(dir.x,0,dir.y);
        }

        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_A)){
            Vector3d dir = camera.getDirection(Camera.Direction.LEFT);
            move(dir.x,0,dir.y);
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_D)){
            Vector3d dir = camera.getDirection(Camera.Direction.RIGHT);
            move(dir.x,0,dir.y);
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_SPACE) && isGrounded() && tick-jumpTick > 10){
            jumpTick=tick;
            move(0,0.25f,0, Force.PUSH);
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)){
            move(0,-1.2f,0, Force.ADD);
        }
        //System.out.println(Biome.getBiome((int)getTransform().getPosition().x,(int)getTransform().getPosition().z).getName());
        //if(chunk == null || !chunk.isBuilt())
            //getVelocity().clear(Axis.Y);

        boolean movedMouse = CameraMath.followMouse(camera,Engine.mouse,.002f,0.4f);
        if(movedMouse || moved || !this.velocity.isZero()){
            camera.update();
            if(chunk != null) {
                Vector3d p = getTransform().getPosition();
                ChunkTools.toBlockPosition((int)p.x,(int)p.y,(int)p.z,pos);
                //System.out.println(chunk.getSkyLightValue(pos.x,pos.y,pos.z));
            }
        }

        if(Engine.mouse.isLeftPressed()) {
            destroyBlock();
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) && Engine.mouse.isRightPressed()) {
            rayPlaceBlock(Block.TORCH);
        }
        else if(Engine.mouse.isRightPressed()) {
            rayPlaceBlock(Block.STONE);
        }

        if(chunk != null) {
            Vector3d p = getTransform().getPosition();
            ChunkTools.toBlockPosition((int)p.x,(int)p.y,(int)p.z,pos);
            //System.out.println(chunk.getLightValue(pos.x,pos.y,pos.z));
        }
    }

    private Vector3i pos = new Vector3i(0,0,0);

    @Override
    public float volume() {
        return 8*8*8;
    }

    @Override
    public float weight() {
        return 1.2f;
    }

    @Override
    protected void onRender(Renderer renderer) {
        //AABBRenderer.render(renderer,(AABB)this.getCollider());
        if(ab != null)
            AABBRenderer.render(renderer,ab);
    }

    private static Chunk pickedChunk = null;

    private Vector3i pickedBlockPosition = new Vector3i(0,0,0);
    private AABB ab = new AABB(0,0,0,1,1,1);
    private void rayPlaceBlock(short blockid) {
        RayTracer.TraceResult result = traceBlock();

        if(result.isValid()) {
            Vector3i pos = result.getFace().add(pickedBlockPosition);
            if(!Block.getBlock(blockid).getCollisionBox(pos.x,pos.y,pos.z).isColliding((AABB)this.getCollider())) {
                world.placeBlock(this,pos.x, pos.y, pos.z,blockid);
                DirtyLayerProvider.build();
            }
            //result.getFace()
        }
    }

    private void destroyBlock(){
        /*
        RayTracer.TraceResult result = trace((pos) -> {
            Chunk c = world.getChunk(ChunkTools.toChunkPosition(pos.x,pos.z));
            if(c==null) return false;
            Vector3i blockPos = ChunkTools.toBlockPosition(pos.x,pos.y,pos.z);
            Block b = Block.getBlock(c.getBlock(blockPos.x,blockPos.y,blockPos.z));
            if(b.isSolid()) {
                c.setBlock(blockPos.x, blockPos.y, blockPos.z, Block.AIR);
                return true;
            }
            return false;
        });

         */
        RayTracer.TraceResult result = traceBlock();

        if(result.isValid() && pickedChunk != null) {
            world.placeBlock(this,pickedBlockPosition.x,pickedBlockPosition.y,pickedBlockPosition.z,Block.AIR);
            DirtyLayerProvider.build();
        }

    }
    private Vector3i traceResultSrc = new Vector3i(0,0,0);
    private RayTracer.TraceResult traceBlock() {
        pickedChunk = null;
        return trace((pos) -> {
            Chunk c = world.getChunk(ChunkTools.toChunkPosition(pos.x,pos.z));
            if(c==null) return false;
            Vector3i blockPos = ChunkTools.toBlockPosition(pos.x,pos.y,pos.z,traceResultSrc);
            Block b = Block.getBlock(c.getBlock(blockPos.x,blockPos.y,blockPos.z));
            if(b.canBeDestroyed()) {
                pickedBlockPosition.set(pos);
                pickedChunk = c;
                return true;
            }
            return false;
        });
    }

    private RayTracer.TraceResult trace(Function<Vector3i, Boolean> func) {
        return RayTracer.traceVoxelGrid(this.camera.getPosition(),camera.getForward(), 15f, func);
    }

}
