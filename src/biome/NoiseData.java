package biome;

import engine.tools.MathTools;

public class NoiseData {
    int octaves;
    float frequency,persistance;

    public NoiseData(int octaves, float persistance, float frequency) {
        this.octaves = octaves;
        this.frequency = frequency;
        this.persistance = persistance;
    }
    public double lerpFreq(NoiseData n2, float diff){
        return MathTools.lerp(this.frequency,n2.frequency,diff);
    }
    public int lerpOctaves(NoiseData n2, float diff){
        return (int)MathTools.lerp(this.octaves,n2.octaves,diff);
    }
    public double lerpPers(NoiseData n2, float diff){
        return MathTools.lerp(this.persistance,n2.persistance,diff);
    }

    public int getOctaves() {
        return octaves;
    }

    public float getFrequency() {
        return frequency;
    }

    public float getPersistance() {
        return persistance;
    }
}
