package biome;

import blocks.Block;
import blocks.IBlockHolder;
import engine.tools.Range;
import tools.NoiseHelper;

public class GrassLands extends Biome {
    public GrassLands() {
        super("Grasslands");
    }

    @Override
    public short getBlock(int x, int y, int z,int height) {
        if(y > height){
            if(y <= WATER_LEVEL)
                return Block.WATER;
            return Block.AIR;
        }
        else if(y == height){
            if(y < WATER_LEVEL)
                return Block.DIRT;
            return Block.GRASS;
        }
        else if(y > height - 5) return Block.DIRT;
        return Block.STONE; // STONE!;
    }

    @Override
    public BiomeType getId() {
        return BiomeType.GRASSLANDS;
    }

    @Override
    public NoiseData getNoiseData() {
        return new NoiseData(4,0.8f,0.013f);
    }

    @Override
    public int smoothRange() {
        return 11;
    }

    @Override
    public void generateStructures(IBlockHolder holder, int x, int y, int z) {
        if(y != holder.getHeight(x,z)+1) return;
        if(holder.getBlock(x,y-1,z) != Block.GRASS) return;
        double treeArea = NoiseHelper.generateNoise(x,z,3,0.8f,0.003f);
        if(treeArea > 0.4){
            double isOnTreeSpot = NoiseHelper.generateNoise(x,z,1,0.8f,1);
            if(isOnTreeSpot >= 0.8){

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
        return Range.from(25,50);
    }
}
