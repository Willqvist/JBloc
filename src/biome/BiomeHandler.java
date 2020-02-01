package biome;

import engine.tools.Range;
import tools.NoiseHelper;
import tools.Voronoi;

import java.util.ArrayList;
import java.util.HashMap;

public class BiomeHandler {
    private static HashMap<BiomeGroup, ArrayList<BiomeType>> biomeGroup = new HashMap<>();
    private static HashMap<BiomeType, Biome> biomes = new HashMap<>();

    //X axis - temperature
    //Y axis - humidity;
    private static BiomeGroup[][] humidityTemperatureMap = new BiomeGroup[101][101];

    private static int size = 0;
    private static void addBiome(Biome b){
        BiomeGroup group = b.getGroup();
        if(!biomeGroup.containsKey(group))
            biomeGroup.put(group,new ArrayList<>());
        biomeGroup.get(group).add(b.getId());
        biomes.put(b.getId(),b);

        Range<Integer> hum = b.getHumidity();
        Range<Integer> temp = b.getTemperature();

        for(int i = hum.min(); i < hum.max(); i++){
            for(int j = temp.min(); j < temp.max(); j++){
                humidityTemperatureMap[j][i] = group;
            }
        }

        size ++;
    }

    public static BiomeGroup getGroupByTempHum(int x, int z){
        int humidityNoise = (int)(NoiseHelper.generateNoise(x,z,1,0.5f,0.007f)*100);
        int temperatureNoise = (int)(NoiseHelper.generateNoise(x+0xFF354,z+0xFF354,1,0.2f,0.007f)*100);
        //if(humidityTemperatureMap[temperatureNoise][humidityNoise] == null)
            //System.out.println(temperatureNoise + " <|> " + humidityNoise);
        return humidityTemperatureMap[temperatureNoise][humidityNoise];
    }

    public static Biome getBiomeByGroup(BiomeGroup group, Voronoi.VoronoiData dat){
        if(group == null) {
            return getBiomeById(BiomeType.FOREST);
        }
        ArrayList<BiomeType> l = biomeGroup.get(group);
        int index = dat.value % l.size();
        return getBiomeById(biomeGroup.get(group).get(index));
    }

    private static void addBiomeNonGeneratedable(Biome b){
        biomes.put(b.getId(),b);
    }
    public static int getBiomeAmount(){
        return size;
    }
    public static Biome getBiomeById(BiomeType type){
        return biomes.get(type);
    }

    static{
        addBiome(new GrassLands());
        addBiome(new ForestBiome());
        addBiome(new Desert());
        addBiome(new ExtremeHills());
        addBiome(new TundraBiome());
        addBiome(new OceanBiome());
        addBiomeNonGeneratedable(new RiverBiome());
        //addBiomeNonGeneratedable(new GrasslandsShore());
    }

    public static boolean hasWater(BiomeType b) {
        switch (b){
            case DESERT:
            case OCEAN:
            case ROCKYLAND:
                return false;
        }
        return true;
    }

    public static Biome getShore(BiomeGroup group, BiomeGroup neighbourGroup) {
        if(group == BiomeGroup.GRASSLANDS && neighbourGroup == BiomeGroup.OCEAN)
            return getBiomeById(BiomeType.GRASSLANDS_SHORE);
        return null;
    }
}
