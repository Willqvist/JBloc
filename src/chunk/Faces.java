package chunk;

public class Faces {

    //VERTICES					NORMALS
    public static float[] FRONT = new float[]
            {
                    1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            };
    public static float[] LEFT = new float[]
            {
                    0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
            };
    public static float[] RIGHT = new float[]
            {
                    1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                    1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                    1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            };
    public static float[] BACK = new float[]
            {
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,
                    0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f,
                    1.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f,
                    1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,
            };
    public static float[] TOP = new float[]
            {
                    0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            };
    public static float[] BOTTOM = new float[]
            {
                    0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,
                    1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,
                    1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f,
            };
    public static float[] rescale(float[] face,float x,float y,float z,float x1,float y1,float z1){
        float[] f = new float[face.length];
        for (int i = 0; i < face.length; i += 6) {
            f[i] = face[i] > 0 ? face[i]*x1 : x;
            f[i + 1] = face[i + 1] > 0 ? face[i+1]*y1 : y;
            f[i + 2] = face[i + 2] > 0 ? face[i+2]*z1 : z;
        }
        return f;
    }
    public static float[] getFace(float[] face, float x, float y, float z) {
        float[] f = new float[face.length];
        for (int i = 0; i < face.length; i += 3) {
            f[i] = face[i] + x;
            f[i + 1] = face[i + 1] + y;
            f[i + 2] = face[i + 2] + z;
        }
        return f;
    }
}