package entities;

import chunk.Chunk;
import chunk.ChunkTools;
import engine.camera.Camera3D;
import engine.model.Model;
import engine.physics.*;
import engine.render.Material;
import engine.render.Renderer;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import world.World;

import java.text.NumberFormat;

public abstract class PhysicsEntity extends Entity implements IPhysicsBody {
    private ICollider collider;
    protected float speed = 2;
    protected Velocity velocity = new Velocity(this);
    protected boolean moved = false;
    protected Chunk chunk;
    private int oldX,oldZ;
    private boolean grounded = false;
    private static Vector2i chunkPos = new Vector2i(0,0);
    public PhysicsEntity(World world,Model model) {
        super(world,model);
        init();
    }

    protected void setCollider(ICollider collider) {
        this.collider = collider;
    }

    public PhysicsEntity(World world) {
        super(world);
        init();
    }

    public PhysicsEntity(World world,Model model, Material material) {
        super(world,model, material);
        init();
    }

    private void init(){
        Vector3d pos = getTransform().getPosition();
        collider = new AABB(this.getTransform(),1,1,1);
        Vector2i p = ChunkTools.toChunkPosition((int)pos.x,(int)pos.z);
        this.chunk = world.getChunk(ChunkTools.toChunkPosition((int)pos.x,(int)pos.z));
        oldX = (int)p.x;
        oldZ = (int)p.y;
    }

    @Override
    public final void update() {
        moved = false;
        onUpdate();
        Vector3d pos = getTransform().getPosition();
        Vector2i cpos = ChunkTools.toChunkPosition(pos.x,pos.z,chunkPos);
        if(oldX != pos.y || oldZ != pos.x) {
            if(chunk != null)
                chunk.removeEntity(this);
            Vector2i cp = ChunkTools.toChunkPosition((int)pos.x,(int)pos.z);
            this.chunk = world.getChunk(cp);
            oldZ = cp.y;
            oldX = cp.x;

            if(chunk != null)
                chunk.addEntity(this);
        }
        tick ++;
    }

    @Override
    public void render(Renderer renderer) {
        super.render(renderer);
        if(getCollider().testFrustum((renderer.getCamera().getFrustum()))){
            onRender(renderer);
        }
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    protected abstract void onRender(Renderer renderer);

    @Override
    public float friction() {
        return grounded ? 0.7f : 0.1f;
    }

    @Override
    public float weight() {
        return 12;
    }

    @Override
    public float volume() {
        return 5*5*5;
    }

    @Override
    public Velocity getVelocity() {
        return velocity;
    }

    @Override
    public ICollider getCollider() {
        return collider;
    }

    @Override
    public CollisionResponse onCollision(ColliderData info) {
        return CollisionResponse.NONE;
    }

    public boolean hasMoved(){
        return moved;
    }


    private Vector3d dir = new Vector3d();
    protected void move(double x,double y,double z) {
        move(x,y,z,Force.ADD);
    }

    protected void move(double x,double y,double z, Force force) {
        moved = true;
        this.getVelocity().addForce(speed*friction()*Physics.AIR_DRAG,dir.set(x,y,z),force);
    }

    protected boolean isGrounded() {
        return grounded;
    }

}
