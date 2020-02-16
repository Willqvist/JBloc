package biome;

import blocks.Block;
import blocks.IBlockHolder;
import engine.tools.Range;

public class Desert extends Biome {
    protected Desert() {
        super("Desert");
    }

    @Override
    public short getBlock(int x, int y, int z,int height) {
        if(y <= height) return (Block.SAND); // SAND;
        if(y <= WATER_LEVEL) return (Block.WATER);
        return (Block.AIR);
    }

    @Override
    public BiomeType getId() {
        return BiomeType.DESERT;
    }

    @Override
    public NoiseData getNoiseData() {
        return new NoiseData(3,0.8f,0.02f);
    }

    @Override
    public int smoothRange() {
        return 11;
    }

    @Override
    public void generateStructures(IBlockHolder holder, int x, int y, int z, int height) {
    }

    @Override
    public int maxHeight() {
        return WATER_LEVEL+30;
    }

    @Override
    public int minHeight() {
        return WATER_LEVEL;
    }

    @Override
    public BiomeGroup getGroup() {
        return BiomeGroup.DESERT;
    }

    @Override
    protected Range<Integer> getHumidity() {
        return Range.from(0,50);
    }

    @Override
    protected Range<Integer> getTemperature() {
        return Range.from(75,100);
    }
}
