package chunk;

import blocks.IBlockHolder;
import org.joml.Vector2i;

public interface ChunkProvider extends IBlockHolder {
    Chunk getChunk(Vector2i pos);
    Chunk getChunk(int x,int z);
}
