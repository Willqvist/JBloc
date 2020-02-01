package chunk.builder;

import chunk.Chunk;
import chunk.ChunkTools;
import engine.Engine;
import engine.storage.BiStorage;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkLightPass {
    private static Thread[] threads;
    private static ArrayList<BiStorage<Chunk,Runnable>> chunks = new ArrayList<>();
    private static boolean running = true;
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition cond = lock.newCondition();

    public static void init(int numThreads){
        threads = new Thread[numThreads];
        for(int i = 0; i < numThreads; i++){
            threads[i] = new ChunkLightPass.ChunkLightPassThread();
            threads[i].start();
        }

    }
    public static synchronized void addChunk(Chunk chunk, Runnable callback){
        chunks.add(new BiStorage<>(chunk,callback));
        lock.lock();
        cond.signalAll();
        lock.unlock();
    }

    private static void onExit() {
        running = false;
        lock.lock();
        cond.signalAll();
        lock.unlock();
    }

    private static boolean isEmpty() {
        while(chunks.isEmpty() && running) {
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

    public static void join() {
        onExit();
        for(int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private static ChunkBlockBuilder.ChunkComparator comp = new ChunkBlockBuilder.ChunkComparator();
    private static synchronized BiStorage<Chunk,Runnable> pollFirst() {
        BiStorage<Chunk,Runnable> c = findNearestChunk();
        chunks.remove(c);
        return c;
    }

    private static synchronized BiStorage<Chunk,Runnable> findNearestChunk() {
        if(chunks.size() <= 0) {
            return null;
        }
        BiStorage<Chunk,Runnable> c = chunks.get(0);
        for(int i = 1; i < chunks.size(); i++) {
            BiStorage<Chunk,Runnable> c2 = chunks.get(i);
            if(comp.compare(c.getFirst(),c2.getFirst()) == -1) {
            } else {
                c = c2;
            }
        }
        return c;
    }

    static class ChunkComparator implements Comparator<Chunk> {
        private Vector2f w1=new Vector2f(),w2=new Vector2f();
        private Vector2i w1Cp = new Vector2i();

        @Override
        public int compare(Chunk c1, Chunk c2) {
            w1.set(c1.getWorldPosition());
            w2.set(c2.getWorldPosition());
            w1Cp.set(ChunkTools.toChunkPosition(w1.x,w1.y));
            Vector3d position = ChunkModelBuilder.follow.getPosition();
            if(w1Cp.equals(ChunkTools.toChunkPosition(position.x,position.z))) {
                return -1;
            }

            w1Cp.set(ChunkTools.toChunkPosition(w2.x,w2.y));
            if(w1Cp.equals(ChunkTools.toChunkPosition(position.x,position.z))) {
                return 1;
            }
            boolean c1inFrustum = c1.getCollider().testFrustum(ChunkModelBuilder.follow.getFrustum());
            boolean c2inFrustum = c2.getCollider().testFrustum(ChunkModelBuilder.follow.getFrustum());
            if(c1inFrustum && !c2inFrustum) return -1;
            if(!c1inFrustum && c2inFrustum) return 1;
            return position.distanceSquared(w1.x,position.y,w1.y) < position.distanceSquared(w2.x,position.y,w2.y) ? -1 : 1;
        }
    }

    private static class ChunkLightPassThread extends Thread {
        @Override
        public void run() {
            while(running) {
                if(!isEmpty()) {
                    BiStorage<Chunk,Runnable> c = pollFirst();
                    if(c != null) {
                        //c.getFirst().calculateLights();
                        Engine.invokeLater(()-> {
                            c.getLast().run();
                        });
                    }
                }
            }
        }
    }
}
