package chunk;

import java.util.HashSet;
import java.util.Iterator;

public class DirtyLayerProvider {
    private static HashSet<Layer> layers = new HashSet<>();

    public static void addLayer(Layer layer){
        layer.setDirty(true);
        layers.add(layer);
    }

    public static void build(){
        ChunkModelBuilder.lock();
        Iterator<Layer> layers = DirtyLayerProvider.layers.iterator();
        while(layers.hasNext()){
            layers.next().rebuild();
        }
        DirtyLayerProvider.layers.clear();
        ChunkModelBuilder.unlock();
    }
}
