package ui;

import engine.tools.RoffColor;
import engine.ui.*;
import engine.ui.Frame;
import engine.ui.Panel;

import java.awt.*;

public class GameView {

    public GameView() {
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

        frame.add(panel);
        panel.add(a);
        a.setBackgroundColor(RoffColor.from(Color.blue));
        Text text = new Text("hejsan","mc");
        text.setWidth(50,Unit.PERCENT);
        //text.setBackgroundColor();
        panel.add(text);

    }

}
