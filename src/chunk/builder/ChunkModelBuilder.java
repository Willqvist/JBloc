package chunk.builder;

import chunk.Chunk;
import chunk.Layer;
import engine.Engine;
import engine.camera.Camera;
import engine.model.CustomModelAttribute;
import engine.model.Model;
import engine.model.ModelAttribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkModelBuilder {

    protected static Camera follow;
    private static ChunkBuilderThread[] builderThreads;
    private static TreeSet<Layer> layers = new TreeSet<>(new ChunkComparator());
    private static HashSet<Chunk> chunks = new HashSet<>();
    private static LayerBuilder layerModelBuilder = new LayerBuilder();
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition cond = lock.newCondition();
    private static boolean running = true;
    private static ModelAttribute[] attributes = new ModelAttribute[]{
            new ModelAttribute(ModelAttribute.Type.POSITION,3),
            new ModelAttribute(ModelAttribute.Type.NORMALS,3),
            new ModelAttribute(ModelAttribute.Type.UV,2),
            new CustomModelAttribute(5,1)
    };

    public static void init(int numThreads) {
        builderThreads = new ChunkBuilderThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            builderThreads[i] = new ChunkBuilderThread();
            builderThreads[i].start();
        }
    }

    public static void setFollow(Camera follow) {
        ChunkModelBuilder.follow = follow;
        ChunkComparator.setFollow(follow);
    }

    public static synchronized void addChunk(Chunk c) {
        chunks.add(c);
        Layer[] layers = c.getLayers();
        for(int i = 0; i < layers.length; i++ ){
           addLayer(layers[i]);
        }
        lock.lock();
        cond.signalAll();
        lock.unlock();
    }

    public static synchronized void addLayer(Layer layer) {
        chunks.add(layer.getChunk());
        layers.add(layer);
        lock.lock();
        cond.signal();
        lock.unlock();
    }

    private static synchronized Layer pollLayer() {
        return layers.pollFirst();
    }

    public static void stop() {
        lock.lock();
        cond.signalAll();
        lock.unlock();
        for(int i = 0; i < builderThreads.length; i++) {
            builderThreads[i].end();
        }
    }

    private static boolean isEmpty() {
        while(layers.isEmpty() && running) {
            lock.lock();
            try {
                cond.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
        }
        return !running;
    }

    private static void removeChunk(Chunk c) {
        chunks.remove(c);
    }

    private static class ChunkBuilderThread extends Thread {

        private static final int NUM_BUILDERS = 20;
        private LayerModelBuilder[] builders = new LayerModelBuilder[NUM_BUILDERS];
        private volatile boolean running = true;
        //private List<LayerModelBuilder> layerModelBuilders = new ArrayList<>();
        private ChunkBuilderThread() {
            for(int i = 0; i < NUM_BUILDERS; i++) {
                builders[i] = new LayerModelBuilder();
            }
        }

        private LayerModelBuilder getFreeBuilder() {
            for(int i = 0; i < NUM_BUILDERS; i++) {
               if(!builders[i].isOccupied())
                   return builders[i];
            }
            return null;
        }

        private boolean hasFreeBuilder() {
            return getFreeBuilder() != null;
        }

        public void end() {
            this.running = false;
        }

        @Override
        public void run() {
            super.run();
            while(running) {
                while (!isEmpty() && hasFreeBuilder()) {
                    LayerModelBuilder builder = getFreeBuilder();
                    builder.setOccupied(true);
                    Layer layer = pollLayer();
                    if(layer == null) {
                        builder.setOccupied(false);
                        continue;
                    }
                    long a = System.nanoTime();
                    layer.getChunk().calculateLights();
                    a = System.nanoTime();
                    layerModelBuilder.buildLayer(layer, builder);
                    Engine.invokeLater(() -> {
                        Model m = builder.getOpaqueModelBuilder().build(attributes);

                        if(layer.getChunk().getX() == -5 && layer.getChunk().getZ() == -20) {
                            //System.out.println("error chunk layer building: " + layer.getY() + " | " + m);
                        }
                        layer.setModel(m);
                        layer.setTransparentModel(builder.getTransparentModelBuilder().build(attributes));
                        builder.getOpaqueModelBuilder().clear();
                        builder.getTransparentModelBuilder().clear();
                        builder.setOccupied(false);

                    });
                }
            }
        }
    }
}
