package biome.generator;

import biome.Biome;

public abstract class BiomeGenerator {
    public abstract Biome getBiome(int x, int z);

    public abstract int getBiomeHeight(Biome b, int x, int z);

    public abstract double getDensity(Biome b, int x, int y, int z);
}
