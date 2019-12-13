package gameobject;

import engine.materials.MaterialBank;
import engine.model.Model;
import engine.render.IRenderable;
import engine.render.Material;

public abstract class RenderableGameObject extends GameObject implements IRenderable {

    private Model model;
    private Material material;

    public RenderableGameObject(Model model){
        super();
        this.model = model;
        this.material = MaterialBank.getMaterial("standard");
    }

    public RenderableGameObject(Model model, Material material){
        super();
        this.model = model;
        this.material = material;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

}
