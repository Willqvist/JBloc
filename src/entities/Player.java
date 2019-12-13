package entities;

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
import engine.render.AABBRenderer;
import engine.render.Renderer;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
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
        speed = 0.092f;
        ((AABB)getCollider()).resize(0.8f,1.8f,0.8f);
        ((AABB)getCollider()).setOffset(0.1f,0,0.1f);
        camera.follow(this.getTransform());
        camera.getTransform().setPosition(0.5f,1.8f,0.5f);
        getTransform().setPosition(0,150,0);
        camera.update();
    }

    @Override
    protected void onUpdate() {

        if(Engine.key.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)){
            Engine.window.unlockMouse();
        }

        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_W)){
            Vector3f dir = camera.getDirection(Camera.Direction.FORWARD);
            move(dir.x,0,dir.y);
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_S)){
            Vector3f dir = camera.getDirection(Camera.Direction.BACKWARD);
            move(dir.x,0,dir.y);
        }

        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_A)){
            Vector3f dir = camera.getDirection(Camera.Direction.LEFT);
            move(dir.x,0,dir.y);
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_D)){
            Vector3f dir = camera.getDirection(Camera.Direction.RIGHT);
            move(dir.x,0,dir.y);
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_SPACE) && isGrounded() && tick-jumpTick > 10){
            jumpTick=tick;
            move(0,0.25f,0, Force.PUSH);
        }
        if(Engine.key.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)){
            move(0,-1.2f,0, Force.ADD);
        }
        boolean movedMouse = CameraMath.followMouse(camera,Engine.mouse,.002f,0.4f);
        if(movedMouse || moved || !this.velocity.isZero()){
            camera.update();
        }

        if(Engine.mouse.isLeftPressed()) {
            destroyBlock();
        }

        if(Engine.mouse.isRightPressed()) {
            rayPlaceBlock(Block.GRASS);
        }


        if(chunk == null) {
            getVelocity().clear(Axis.Y);
        }
    }

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
            Chunk c = world.getChunk(ChunkTools.toChunkPosition(pos.x,pos.z));
            Vector3i bPos = ChunkTools.toBlockPosition(pos.x,pos.y,pos.z);
            if(!Block.getBlock(blockid).getCollisionBox(pos.x,pos.y,pos.z).isColliding((AABB)this.getCollider())) {
                c.setBlock(bPos.x, bPos.y, bPos.z, blockid);
                c.setLightValue(bPos.x, bPos.y, bPos.z, (byte)0);
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
            Chunk c = world.getChunk(ChunkTools.toChunkPosition(pickedBlockPosition.x,pickedBlockPosition.z));
            Vector3i blockPos = ChunkTools.toBlockPosition(pickedBlockPosition.x,pickedBlockPosition.y,pickedBlockPosition.z);
            c.setLightValue(blockPos.x, blockPos.y, blockPos.z, (byte)15);
            c.setBlock(blockPos.x, blockPos.y, blockPos.z, Block.AIR);
            DirtyLayerProvider.build();
        }

    }

    private RayTracer.TraceResult traceBlock() {
        pickedChunk = null;
        return trace((pos) -> {
            Chunk c = world.getChunk(ChunkTools.toChunkPosition(pos.x,pos.z));
            if(c==null) return false;
            Vector3i blockPos = ChunkTools.toBlockPosition(pos.x,pos.y,pos.z);
            Block b = Block.getBlock(c.getBlock(blockPos.x,blockPos.y,blockPos.z));
            if(b.isSolid()) {
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
