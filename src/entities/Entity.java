package entities;

import chunk.Chunk;
import chunk.ChunkTools;
import engine.model.Model;
import engine.render.Material;
import engine.render.Renderer;
import gameobject.RenderableGameObject;
import org.joml.Vector2i;
import org.joml.Vector3f;
import world.World;

public abstract class Entity extends RenderableGameObject {

    protected World world;
    protected int tick = 0;

    public Entity(World world, Model model) {
        super(model);
        this.world = world;
    }

    public Entity(World world,Model model, Material material) {
        super(model, material);
        this.world = world;
    }

    public Entity(World world) {
        super(null, null);
        this.world = world;
    }

    public void update(){
        onUpdate();
        tick ++;
    }

    protected abstract void onUpdate();

    public void render(Renderer renderer){}
}
