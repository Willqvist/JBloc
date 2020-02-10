package biome;

import blocks.Block;
import blocks.IBlockHolder;
import engine.tools.Range;

public class TundraBiome extends Biome {
    public TundraBiome() {
        super("Tundra");
    }

    @Override
    public short getBlock(int x, int y, int z, int height) {
        if(y > height){
            if(y <= WATER_LEVEL)
                return Block.WATER;
            return Block.AIR;
        }
        else if(y == height) return Block.STONE; // SNOW
        else if(y > height - 5) return Block.DIRT;
        return Block.STONE; // STONE
    }

    @Override
    public BiomeType getId() {
        return BiomeType.TUNDRA;
    }

    @Override
    public NoiseData getNoiseData() {
        return new NoiseData(2,0.6f,0.01f);
    }

    @Override
    public int smoothRange() {
        return 9;
    }

    @Override
    public void generateStructures(IBlockHolder holder, int x, int y, int z) {

    }

    @Override
    public int maxHeight() {
        return Biome.WATER_LEVEL+30;
    }

    @Override
    public int minHeight() {
        return Biome.WATER_LEVEL-3;
    }

    @Override
    public BiomeGroup getGroup() {
        return BiomeGroup.COLD;
    }

    @Override
    protected Range<Integer> getHumidity() {
        return Range.from(0,75);
    }

    @Override
    protected Range<Integer> getTemperature() {
        return Range.from(0,25);
    }
}
