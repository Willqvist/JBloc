package ui;

import engine.texture.Texture;
import engine.texture.TextureLoader;
import engine.ui.*;
import engine.ui.Frame;
import engine.ui.Panel;
import engine.ui.UIManager;

public class GameView {

    public GameView() {
        WindowComponent frame = UIManager.getFrame();
        Component panel = new Panel(Scale.NONE);
        panel.setWidth(3,Unit.PERCENT);
        panel.setHeight(1,Unit.WIDTH_RATIO);
        panel.setMinDimension(5,5);
        panel.setMaxDimension(80,80);
        Texture crosshair = TextureLoader.load("crosshair.png");
        panel.setBackgroundImage(crosshair);
        panel.setOrigin(Origin.CENTER);
        panel.setPivot(Origin.CENTER);

        Panel p = new Panel(Scale.NONE);
        p.setDimension(20,20);

        frame.add(panel);
        //frame.add(p);
        frame.revalidate();
        /*
        Frame frame = UIManager.getFrame();
        Panel panel = new Panel(Scale.SCALE_TO_FIT);
        panel.setHeight(50);
        panel.setWidth(100);
        panel.setMaxWidth(900);
        panel.setMargin(10,Unit.PIXEL);
        panel.setBackgroundColor(RoffColor.from(Color.red));

        Panel a = new Panel(Scale.NONE);
        a.setPivot(Origin.CENTER);
        a.setOrigin(Origin.RIGHT_TOP);
        a.setDimension(80,80);

        //frame.add(panel);
        panel.add(a);
        a.setBackgroundColor(RoffColor.from(Color.blue));
        Text text = new Text("hejsan","mc");
        text.setWidth(50,Unit.PERCENT);
        //text.setBackgroundColor();
        panel.add(text);

         */

    }

}
