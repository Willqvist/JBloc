package biome;

import biome.generator.BiomeGenerator;
import blocks.IBlockHolder;
import chunk.Chunk;
import engine.tools.Range;
import tools.NoiseHelper;

public abstract class Biome {
    protected static final int MIN_HEIGHT = 112;
    public static final int WATER_LEVEL = MIN_HEIGHT+40;
    private float averageThreshhold = 1f;
    private static BiomeGenerator generator;
    private String name;
    protected int max = maxHeight(),min = minHeight();
    public Biome(String name){
        this.name = name;
    }
    public int getHeight(int x,int z){
        return generator.getBiomeHeight(this,x,z);
    }
    public double getDensity(int x,int y,int z){
        return generator.getDensity(this,x,y,z);
    }
    public float getAverageThreshhold(){return averageThreshhold;}
    public static void setGenerator(BiomeGenerator gen){
        generator = gen;
    }
    public static Biome getBiome(int x,int z){
        return generator.getBiome(x,z);
    }
    public abstract short getBlock(int x, int y, int z,int height);
    public abstract BiomeType getId();
    public abstract NoiseData getNoiseData();
    public abstract int smoothRange();
    public abstract void generateStructures(IBlockHolder holder, int x, int y, int z);
    public abstract int maxHeight();
    public abstract int minHeight();
    public abstract BiomeGroup getGroup();
    protected abstract Range<Integer> getHumidity();
    protected abstract Range<Integer> getTemperature();

    public String getName() {
        return name;
    }

    public double getHeight(int x, int z, int noise){
        NoiseData dat = getNoiseData();
        return (NoiseHelper.generate3DNoise(x, 0,z, dat.octaves, dat.persistance, dat.frequency) * (this.maxHeight() - this.minHeight()) + this.minHeight()) / Chunk.HEIGHT;
    }
    public double getDensity(int x, int y,int z, int noise){
        NoiseData dat = getNoiseData();
        return NoiseHelper.generate3DNoise(x, y, z, dat.octaves, dat.persistance, dat.frequency);
    }

}