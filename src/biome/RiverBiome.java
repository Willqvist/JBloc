package biome;

import blocks.Block;
import blocks.IBlockHolder;
import engine.tools.Range;
import tools.NoiseHelper;

public class RiverBiome extends Biome {
    public RiverBiome() {
        super("River");
    }

    @Override
    public short getBlock(int x, int y, int z,int height) {
        if(y > WATER_LEVEL) return (Block.AIR);
        if(y == WATER_LEVEL) return (Block.WATER);
        if(y >= height) return (Block.WATER);
        else if(y > height - 5){
            if(NoiseHelper.generate3DNoise(x,y,z,2,0.4f,0.4f) > 0.6f)
                return Block.DIRT;
            else
                return Block.SAND; // SAND;
        }
        return Block.SAND; // STONE
    }

    @Override
    public BiomeType getId() {
        return BiomeType.RIVER;
    }

    @Override
    public NoiseData getNoiseData() {
        return new NoiseData(4,0.6f,0.008f);
    }

    @Override
    public void generateStructures(IBlockHolder holder, int x, int y, int z, int height) {

    }

    @Override
    public int smoothRange() {
        return 13;
    }

    @Override
    public int maxHeight() {
        return WATER_LEVEL-2;
    }

    @Override
    public int minHeight() {
        return WATER_LEVEL-6;
    }

    @Override
    public BiomeGroup getGroup() {
        return null;
    }

    @Override
    protected Range<Integer> getHumidity() {
        return null;
    }

    @Override
    protected Range<Integer> getTemperature() {
        return null;
    }
}
