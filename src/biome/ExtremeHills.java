package biome;


import blocks.Block;
import blocks.IBlockHolder;
import engine.tools.Range;
import tools.NoiseHelper;

public class ExtremeHills extends Biome {
    public ExtremeHills() {
        super("rockyland");
    }

    @Override
    public short getBlock(int x, int y, int z,int height) {
        if(y > height){
            if(y <= WATER_LEVEL)
                return (Block.WATER);
            return (Block.AIR);
        }
        else if(y <= height && y >= height - 80){
            double noise = NoiseHelper.generate3DNoise(x,y,z,2,0.6f,0.02f);
            double noise1 = NoiseHelper.generate3DNoise(x,y+1,z,2,0.6f,0.02f);
            short block = block3D(noise);
            if(block3D(noise1) == Block.AIR && block == Block.STONE)
                return Block.GRASS;
            return block;
        }
        else if(y > height-10){ return (Block.STONE); } // STONE
        return (Block.DIRT);
    }

    private short block3D(double noise) {
        if(noise > 0.2f && noise <= 0.4f)
            return Block.AIR;
        return Block.STONE;
    }

    @Override
    public BiomeType getId() {
        return BiomeType.ROCKYLAND;
    }

    @Override
    public NoiseData getNoiseData() {
        return new NoiseData(3,0.9f,0.007f);
    }

    @Override
    public int smoothRange() {
        return 5;
    }

    @Override
    public void generateStructures(IBlockHolder holder, int x, int y, int z) {

    }

    @Override
    public int maxHeight() {
        return WATER_LEVEL+90;
    }

    @Override
    public int minHeight() {
        return WATER_LEVEL-3;
    }

    @Override
    public BiomeGroup getGroup() {
        return BiomeGroup.FOREST;
    }

    @Override
    protected Range<Integer> getHumidity() {
        return Range.from(50,100);
    }

    @Override
    protected Range<Integer> getTemperature() {
        return Range.from(75,100);
    }
}