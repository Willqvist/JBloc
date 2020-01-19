package chunk.builder;

import chunk.Chunk;
import chunk.Layer;
import engine.camera.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Comparator;

public class ChunkComparator implements Comparator<Layer> {
    private Vector2f w1=new Vector2f(),w2=new Vector2f();
    private static Camera follow;

    public static void setFollow(Camera follow) {
        ChunkComparator.follow = follow;
    }

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