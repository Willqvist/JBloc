package biome;

import blocks.Block;
import blocks.IBlockHolder;
import engine.tools.Range;
import tools.NoiseHelper;

public class ForestBiome extends Biome {
    public ForestBiome() {
        super("Forest");
    }

    @Override
    public short getBlock(int x, int y, int z,int height) {
        if(y > height){
            if(y <= WATER_LEVEL)
                return Block.WATER;
            return Block.AIR;
        }
        else if(y == height) {
            double noise = NoiseHelper.generate3DNoise(x,y,z,2,0.6f,0.02f);
            if(noise > 0.6f)
                return Block.AIR;
            return Block.GRASS;
        }
        else if(y > height - 5) return Block.DIRT;
        return Block.STONE; // STONE HERE;
    }

    @Override
    public BiomeType getId() {
        return BiomeType.FOREST;
    }

    @Override
    public NoiseData getNoiseData() {
        return new NoiseData(3,0.6f,0.011f);
    }

    @Override
    public int smoothRange() {
        return 11;
    }

    @Override
    public void generateStructures(IBlockHolder holder, int x, int y, int z, int height) {
        if(y != holder.getHeight(x,z)+1) return;
        System.out.println("generating tree!!");
        short b = holder.getBlock(x,y-1,z);
        if(b != Block.DIRT && b != Block.GRASS && holder.getBlock(x,y,z) == Block.AIR) return;
        double treeArea = NoiseHelper.generateNoise(x,z,3,1f,0.5f);
        if(treeArea > 0.5 && treeArea < 1 && true){
            double isOnTreeSpot = NoiseHelper.generateNoise(x,z,1,0.5f,4);
            if(isOnTreeSpot > 0.8 && isOnTreeSpot < 1){
                StructureProvider.getStructure(0).build(this,holder,x,y,z);
            }
        }
    }

    @Override
    public int maxHeight() {
        return WATER_LEVEL+15;
    }

    @Override
    public int minHeight() {
        return WATER_LEVEL-2;
    }

    @Override
    public BiomeGroup getGroup() {
        return BiomeGroup.GRASSLANDS;
    }

    @Override
    protected Range<Integer> getHumidity() {
        return Range.from(0,50);
    }

    @Override
    protected Range<Integer> getTemperature() {
        return Range.from(50,75);
    }
}
