package blocks;

public interface IBlockHolder {
    void setBlock(int x, int y, int z, short block);
    short getBlock(int x, int y, int z);
    int getHeight(int x, int z);
    void start();
    void end();
}
