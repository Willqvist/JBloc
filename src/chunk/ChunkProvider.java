package chunk;

import org.joml.Vector2i;

public interface ChunkProvider {
    Chunk getChunk(Vector2i pos);
    Chunk getChunk(int x,int z);
}
