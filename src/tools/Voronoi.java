package tools;

import org.joml.SimplexNoise;
import org.joml.Vector2f;

public class Voronoi {
    private int seed,cellSize,jitterSize,currentCellX,currentCellY,oddRowOffset;
    private int noise1,noise2,noise3;
    private int seedsX[][] = new int[5][5];
    private int seedsY[][] = new int[5][5];
    public Voronoi(int seed, int cellSize,int jitterSize){
        this.seed = seed;
        this.noise1 = seed+1;
        this.noise2 = seed+2;
        this.noise3 = seed+3;
        this.cellSize = cellSize;
        this.jitterSize = Math.min(cellSize, Math.max(1,jitterSize));
        oddRowOffset = -cellSize/5;
        currentCellX = 9999;
        currentCellY = 9999;
    }
    public void setOddRowOffset(int offset){
        oddRowOffset = Math.min(cellSize, Math.max(-cellSize,offset));
    }

    public int getDistanceAt(int x,int y){
        return 0;
    }
    public synchronized VoronoiData getValueAt(int x,int y){
        VoronoiData data = new VoronoiData();
        int cellX = x/cellSize;
        int cellY = y/cellSize;
        updateCell(cellX,cellY);

        int seedX = seedsX[0][0];
        int seedY = seedsY[0][0];
        int minDist = (seedX-x)*(seedX-x)+(seedY-y)*(seedY-y);
        int minDist2 = minDist;
        for(int xi = 0; xi < 5; xi++){
            for(int yi = 0; yi < 5; yi++){
                seedX = seedsX[xi][yi];
                seedY = seedsY[xi][yi];

                int dist = (seedX-x)*(seedX-x)+(seedY-y)*(seedY-y);
                if(dist < minDist){
                    minDist2 = minDist;
                    minDist = dist;
                    data.nearPosX = seedX;
                    data.nearPosY = seedY;
                    data.value = (int)((SimplexNoise.noise(xi+cellX-2+noise3,yi+cellY-+2+noise3)+1)*50f);
                }else if(dist < minDist2){
                    minDist2 = dist;
                    data.sNearPosX = seedX;
                    data.sNearPosY = seedY;
                    data.distMin2 = minDist2;
                }
            }
        }
        return data;
    }
    private synchronized void updateCell(int cellX,int cellY){
        if(cellX == currentCellX && cellY == currentCellY)
            return;

        int noiseBaseX = cellX-2;
        int noiseBaseY = cellY-2;
        for(int x = 0; x < 5; x++){
            int base = (noiseBaseX+x)*cellSize+cellSize/2;
            int rowOffset = ((noiseBaseX+x) & 0x01)*oddRowOffset;
            for(int z = 0; z < 5; z++){
                int offsetX = (int)(SimplexNoise.noise(noise1+noiseBaseX+x,noise1+noiseBaseY+z)* jitterSize);
                int offsetY = (int)(SimplexNoise.noise(noise2+noiseBaseX+x,noise2+noiseBaseY+z)* jitterSize);
                seedsX[x][z] = base+offsetX;
                seedsY[x][z] = (noiseBaseY+z)*cellSize+rowOffset+offsetY+cellSize/2;
            }
        }
        currentCellY = cellY;
        currentCellX = cellX;
    }
    int nosie2DInt(int x,int y,int seed){
        int n = x+y*57+seed*57*57;
        n = (n << 13) ^ n;
        return ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff);
    }
    public static class VoronoiData{
        public float nearPosX,nearPosY;
        public float sNearPosX,sNearPosY;
        public int value;
        public int distMin2;
    }
}
