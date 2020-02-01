import engine.Core;
import engine.CoreAttributes;
import engine.render.LWJGLRenderer;

public class Launcher {
    public static void main(String[] args){

        CoreAttributes attributes = CoreAttributes.create().setFps(60).setUps(60).setHeight(720).setWidth(1080);
        LWJGLRenderer renderer = new LWJGLRenderer();
        Core.init(attributes,new Game(),renderer);
    }
}
