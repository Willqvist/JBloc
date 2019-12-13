package chunk;

import org.joml.Vector2i;
import org.joml.Vector3i;

public class ChunkTools {
    private static Vector2i pos = new Vector2i(0,0);
    private static Vector3i blockPos = new Vector3i(0,0,0);
    public static Vector2i toChunkPosition(float x, float z){
        pos.set((int)(Math.floor(x / (Chunk.WIDTH*1f))),(int)(Math.floor(z / (Chunk.DEPTH*1f))));
        return pos;
    }
    public static Vector3i toBlockPosition(int x, int y,int z){
        blockPos.set(Math.floorMod(x,Chunk.WIDTH),Math.min(Chunk.HEIGHT,Math.max(0,y)),Math.floorMod(z,Chunk.DEPTH));
        return blockPos;
    }

    public static int toBlockPosition(int val){
        return Math.floorMod(val,Chunk.WIDTH);
    }
}
