package biome;

import blocks.Block;
import blocks.IBlockHolder;
import chunk.DirtyLayerProvider;
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
        if(y == height){
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
        return new NoiseData(4,2f,0.013f);
    }

    @Override
    public int smoothRange() {
        return 11;
    }

    @Override
    public void generateStructures(IBlockHolder holder, int x, int y, int z,int height) {
        if(y != height+1) return;
        short b = holder.getBlock(x,y-1,z);
        if(b != Block.DIRT && b != Block.GRASS) return;
        double treeArea = NoiseHelper.generateNoise(x,z,3,1f,0.5f);

        if(treeArea > 0.6 && treeArea < 1){
            double isOnTreeSpot = NoiseHelper.generateNoise(x,z,1,0.8f,0.1f);
            if(isOnTreeSpot > 0.8 && isOnTreeSpot < 1){
                //System.out.println("building blocks");
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
