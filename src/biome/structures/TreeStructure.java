package biome.structures;

import biome.Biome;
import blocks.Block;
import blocks.IBlockHolder;

public class TreeStructure implements Structure {
    @Override
    public void build(Biome b, IBlockHolder holder, int x, int y, int z) {
        buildOakTree(holder,x,y,z);
    }

    private void buildOakTree(IBlockHolder holder, int x, int y_, int z){
        int height = 5;
        int rad = 2;
        int y = y_;
        for(int i = 0; i <= height; i++){
            holder.setBlock(x,y+i,z, Block.LOG);
        }

        //BOTTOM CROWN
        for(int i = -rad; i <= rad; i++) {
            for(int j = -rad; j <= rad; j++) {
                for(int a = 0; a <= 1; a++) {
                        if(!(a==0 && i == -rad && j == -rad) && !(a==1 && i == rad && j == rad) && holder.getBlock(x + i, y+height-2+a, z + j) == Block.AIR)
                        holder.setBlock(x + i, y+height-2+a, z + j, Block.LEAF);
                }
            }
        }

        //TOP CROWN
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                for(int a = 0; a <= 2; a++) {
                        if (!(a == 0 && i == -1 && j == -1) && !(a == 1 && i == 1 && j == 1 && i == -1 && j == -1) && holder.getBlock(x + i, y + height-1 + a, z + j) == Block.AIR)
                            holder.setBlock(x + i, y + height-1 + a, z + j, Block.LEAF);
                }
            }
        }
    }
    private void buildBirchTree(IBlockHolder holder, int x, int y, int z){}
    private void buildJungleTree(IBlockHolder holder, int x, int y, int z){}

    @Override
    public int getId() {
        return 0;
    }
}
