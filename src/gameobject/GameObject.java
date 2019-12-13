package gameobject;

import engine.render.ITransformable;
import engine.render.Transform;

public abstract class GameObject implements ITransformable {
    private Transform transform;

    public GameObject(){
        this.transform = new Transform(0,0,0);
    }

    @Override
    public Transform getTransform() {
        return transform;
    }

}
