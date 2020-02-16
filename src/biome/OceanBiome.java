package biome;

import blocks.Block;
import blocks.IBlockHolder;
import engine.tools.Range;

public class OceanBiome extends Biome {
    public OceanBiome() {
        super("Ocean");
    }

    @Override
    public short getBlock(int x, int y, int z, int height) {
        if(y > WATER_LEVEL) return (Block.AIR);
        if(y > height) return (Block.WATER);
        return (Block.DIRT);
    }

    @Override
    public BiomeType getId() {
        return BiomeType.OCEAN;
    }

    @Override
    public NoiseData getNoiseData() {
        return new NoiseData(3,0.4f,0.008f);
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
        return WATER_LEVEL-5;
    }

    @Override
    public int minHeight() {
        return MIN_HEIGHT-15;
    }

    @Override
    public BiomeGroup getGroup() {
        return BiomeGroup.OCEAN;
    }

    @Override
    protected Range<Integer> getHumidity() {
        return Range.from(50,100);
    }

    @Override
    protected Range<Integer> getTemperature() {
        return Range.from(25,75);
    }


}
