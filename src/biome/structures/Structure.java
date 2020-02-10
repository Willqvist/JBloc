package biome.structures;

import biome.Biome;
import blocks.IBlockHolder;

public interface Structure {

    void build(Biome b, IBlockHolder holder, int x, int y, int z);
    int getId();
}
