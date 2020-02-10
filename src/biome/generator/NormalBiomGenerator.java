package biome.generator;

import biome.*;
import chunk.Chunk;
import org.joml.Vector2f;
import tools.NoiseHelper;
import tools.Voronoi;

import static tools.Voronoi.VoronoiData;

public class NormalBiomGenerator extends BiomeGenerator {

    //SEED
    private Voronoi voronoi,groupVoronoi;
    public NormalBiomGenerator(){
        voronoi = new Voronoi((int) (NoiseHelper.SEED+0xFFF2D),128,32);
        groupVoronoi = new Voronoi((int) (NoiseHelper.SEED+0xF2D),1024*4,256*5);
    }
    private boolean isRiver(int x,int z){
        double noise = NoiseHelper.generateNoise(x,z,1,0.8f,0.001f);
        return noise > 0.48f+NoiseHelper.generate2DNoise(x,z,1/35f,0.02f)*0.02f && noise < 0.5f;
    }
    @Override
    public Biome getBiome(int x, int z){
        int noise = noise(x,z);
        return getBiome(x,z,noise);
    }
    private Biome getBiome(int x, int z, int noise){
        Biome b = getBiomeWithoutOverlap(x,z,noise);
        if(b.getGroup() != BiomeGroup.OCEAN && isRiver(x+noise,z+noise) && BiomeHandler.hasWater(b.getId())) return BiomeHandler.getBiomeById(BiomeType.RIVER);
        return b;
    }
    private Biome getBiomeWithoutOverlap(int x, int z, int noise){
        VoronoiData groupDat,dat;
        synchronized (voronoi) {
            groupDat = groupVoronoi.getValueAt(x + noise, z + noise);
            dat = voronoi.getValueAt(x + noise, z + noise);
        }
            BiomeGroup group = BiomeHandler.getGroupByTempHum((int)groupDat.nearPosX,(int) groupDat.nearPosY);
            BiomeGroup neighbourGroup = BiomeHandler.getGroupByTempHum((int)groupDat.nearPosX,(int) groupDat.nearPosY);

            boolean isShore = Vector2f.distanceSquared(groupDat.sNearPosX,groupDat.sNearPosY,x + noise, z + noise) -
                    Vector2f.distanceSquared(groupDat.nearPosX,groupDat.nearPosY,x + noise, z + noise) < 20;
            if (isShore) {
                Biome b = BiomeHandler.getShore(group, neighbourGroup);
                return BiomeHandler.getBiomeById(BiomeType.RIVER);
               //if (b != null)
                 //   return BiomeHandler.getBiomeById(BiomeType.RIVER);
            }


            return BiomeHandler.getBiomeByGroup(group, dat);
    }

    private int noise(int x,int z){
        return (int)(NoiseHelper.generateNoise(x,z,2,0.5f,0.02f)*32);
    }

    @Override
    public int getBiomeHeight(Biome b, int x, int z) {
        return getBiomeHeight(b,x,z,true);
    }

    private int getBiomeHeight(Biome b, int x, int z, boolean overlap) {
        double average = 0;
        int averageSize = b.smoothRange();
        int ah = averageSize/2;
        boolean otherBiome = true;
        if(ah != 0) {
            for (int i = -ah; i <= ah; i++) {
                for (int j = -ah; j <= ah; j++) {
                    int noise = noise(x + i, z + j);
                    Biome bi;
                    bi = getBiome(x + i, z + j, noise);
                    otherBiome = bi.getId() == b.getId() && otherBiome;
                    NoiseData dat = bi.getNoiseData();
                    average += bi.getHeight(x+i,z+j,noise);
                }
            }
            average /= averageSize * averageSize;
        }
        else{
            int noise = noise(x, z);
            Biome bi = getBiome(x, z, noise);
            NoiseData dat = bi.getNoiseData();
            average = (NoiseHelper.generateNoise(x + noise, z + noise, dat.getOctaves(), dat.getPersistance(), dat.getFrequency()) * (bi.maxHeight() - Biome.WATER_LEVEL-3) + Biome.WATER_LEVEL-3) / Chunk.HEIGHT;
        }
        return (int)(average* Chunk.HEIGHT);
    }
    /*
    private float getOuterSize(Biome b,int x,int z,int ah){
        for(int a = -1; a <= 1; a+= 2) {
            for (int i = -ah; i <= ah; i++) {
                int noise = noise(x + i, z + ah*a);
                Biome bi;
                bi = getBiome(x + i, z + ah*a, noise);
                NoiseData dat = bi.getNoiseData();
                average += bi.getHeight(x + i, z + ah*a, noise);
            }
        }
    }
     */
    @Override
    public double getDensity(Biome b, int x, int y, int z) {
        return b.getDensity(x,y,z,noise(x, z));
    }
    private Vector2f bilinearPos = new Vector2f();
    private float[] heights = new float[4];
    private int sampleRate = 6;

}
