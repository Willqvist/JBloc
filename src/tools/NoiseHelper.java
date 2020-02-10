package tools;

public class NoiseHelper {
    public static long SEED = 535454342;
    public static double generate3DNoise(int xR,int y,int zR,int octaves,float persistance,float frequency){
        int x = xR;
        int z = zR;
        if(octaves == 0) return 1;
        double n = 0;
        double amp = 1;
        double maxValue = 0;
        double freq = frequency;
        for(int l = 0; l < octaves; l++){
            n += NoiseBuilder.noise((x + NoiseHelper.SEED)*freq,(y + NoiseHelper.SEED)*freq,(z+ NoiseHelper.SEED)*freq)*amp;
            maxValue += amp;
            freq *= 2;
            amp *= persistance;
        }
        n /= maxValue;
        n = (n+1)*0.5f;
        return n;
    }
    public static double generate2DNoise(int xR,int zR,float scale,float amp){
        int x = xR;
        int z = zR;
        return NoiseBuilder.noise((x+ NoiseHelper.SEED)*scale,(z+ NoiseHelper.SEED)*scale)*amp;
    }

    private static int groupSize = (int) (Float.MAX_VALUE/100);

    public static double generateNoise(int xR,int zR,int octaves,float persistance,float frequency){
        //System.out.println("generating for X: " + x);
        int x = xR;
        int z = zR;
        if(octaves == 0) return 1;
        double n = 0;
        double amp = 1;
        double maxValue = 0;
        double freq = frequency;
        for(int l = 0; l < octaves; l++){
            n += NoiseBuilder.noise((x + NoiseHelper.SEED)*freq,(z+ NoiseHelper.SEED)*freq)*amp;
            maxValue += amp;
            freq *= 2;
            amp *= persistance;
        }
        n /= maxValue;
        n = (n+1)*0.5f;
        return n;
    }
}
