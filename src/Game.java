import blocks.Block;
import engine.Application;
import engine.Engine;
import engine.camera.Camera;
import engine.camera.Camera3D;
import engine.materials.MaterialBank;
import engine.materials.StandardMaterial;
import engine.render.Renderer;
import engine.tools.MatrixTools;
import engine.ui.FontProvider;
import engine.ui.font.Hiero;
import engine.ui.font.HieroFontParser;
import shaders.ChunkShader;
import ui.GameView;
import world.World;

public class Game implements Application {
    private Camera camera, top;
    private World world;
    @Override
    public void init() {
        Block.init();
       // Hiero font = HieroFontParser.getInstance().parse("/res/fonts/mc.fnt");

        //FontProvider.getProvider().addFont("mc",font);

        StandardMaterial chunkMaterial = new StandardMaterial();
        chunkMaterial.setShader(new ChunkShader());
        MaterialBank.addMaterial("chunk",chunkMaterial);
        camera = new Camera3D(60,Engine.window.getWidth(),Engine.window.getHeight(),.1f,900f);
        Engine.camera.addCamera("main",camera);
        Engine.window.lockMouse();
        this.world = new World();
        MatrixTools.setOrigin(camera);

        Engine.window.onResize((w,h)->{
            camera.setViewport(w,h);
        });

        //new GameView();
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
        Engine.window.enableDoubleSideRender(true);
        world.renderTransparency(renderer);
        Engine.window.enableDoubleSideRender(false);
        //renderer.render(instance);
    }

    @Override
    public void end() {
        world.end();
    }
}
