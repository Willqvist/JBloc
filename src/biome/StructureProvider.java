package biome;

import biome.structures.Structure;
import biome.structures.TreeStructure;

import java.util.ArrayList;

public class StructureProvider {
    private static ArrayList<Structure> structures = new ArrayList<>();

    public static Structure getStructure(int id){
        return structures.get(id);
    }

    private static void createStructure(Structure structure){
        structures.add(structure);
    }
    static{
        createStructure(new TreeStructure());
    }
}
