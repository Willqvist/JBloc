import blocks.Block;
import engine.ApplicationAdapter;
import engine.Engine;
import engine.camera.Camera;
import engine.camera.Camera2D;
import engine.camera.Camera3D;
import engine.materials.MaterialBank;
import engine.materials.StandardMaterial;
import engine.render.Renderer;
import engine.ui.FontProvider;
import engine.ui.font.Hiero;
import engine.ui.font.HieroFontParser;
import shaders.ChunkShader;
import ui.GameView;
import world.World;

public class Game extends ApplicationAdapter {
    private Camera camera, top;
    private World world;
    @Override
    public void init() {
        Block.init();
        Hiero font = HieroFontParser.getInstance().parse("/res/fonts/mc.fnt");

        FontProvider.getProvider().addFont("mc",font);

        StandardMaterial chunkMaterial = new StandardMaterial();
        chunkMaterial.setShader(new ChunkShader());
        MaterialBank.addMaterial("chunk",chunkMaterial);
        camera = new Camera3D(70,Engine.window.getWidth(),Engine.window.getHeight(),.01f,1000f);
        Engine.camera.addCamera("main",camera);
        Engine.window.lockMouse();
        this.world = new World();

        Engine.window.onResize((w,h)->{
            camera.setViewport(w,h);
        });

        new GameView();
    }

    @Override
    public void update() {
        world.update();
    }

    @Override
    public void render(Renderer renderer) {
        renderer.setRenderMode(Renderer.RenderMode.QUADS);
        renderer.begin(camera);
        world.render(renderer);
        //renderer.render(instance);
    }

    @Override
    public void end() {
        world.end();
    }
}
