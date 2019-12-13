package shaders;

import engine.shader.AlbedoShader;

public class ChunkShader extends AlbedoShader {

    public ChunkShader()  {
        super("chunk");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttributes();
        super.bindAttribute(5,"light");
    }
}
