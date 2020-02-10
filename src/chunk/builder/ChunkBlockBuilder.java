package chunk.builder;

import chunk.Chunk;
import chunk.ChunkTools;
import engine.Engine;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkBlockBuilder {
    private static Thread[] threads;
    private static ArrayList<Chunk> chunks = new ArrayList<>();
    private static boolean running = true;
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition cond = lock.newCondition();

    public static void init(int numThreads){
        threads = new Thread[numThreads];
        Engine.window.onExit(ChunkBlockBuilder::onExit);
        for(int i = 0; i < numThreads; i++){
            threads[i] = new ChunkBlockBuilderThread();
            threads[i].start();
        }

    }
    public static synchronized void addChunk(Chunk chunk){
        chunks.add(chunk);
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
    private static ChunkComparator comp = new ChunkComparator();
    private static synchronized Chunk pollFirst() {
        Chunk c = findNearestChunk();
        chunks.remove(c);
        return c;
    }

    private static synchronized Chunk findNearestChunk() {
        if(chunks.size() <= 0) {
            return null;
        }
        Chunk c = chunks.get(0);
        for(int i = 1; i < chunks.size(); i++) {
            Chunk c2 = chunks.get(i);
            if(comp.compare(c,c2) == -1) {
            } else {
                c = c2;
            }
        }
        return c;
    }

    static class ChunkComparator implements Comparator<Chunk>{
        private Vector2f w1=new Vector2f(),w2=new Vector2f();
        private Vector2i w1Cp = new Vector2i();
        private static Vector2i src = new Vector2i();
        @Override
        public int compare(Chunk c1, Chunk c2) {
            w1.set(c1.getWorldPosition());
            w2.set(c2.getWorldPosition());
            w1Cp.set(ChunkTools.toChunkPosition(w1.x,w1.y,src));
            Vector3d position = ChunkModelBuilder.follow.getPosition();
            if(w1Cp.equals(ChunkTools.toChunkPosition(position.x,position.z,src))) {
                return -1;
            }

            w1Cp.set(ChunkTools.toChunkPosition(w2.x,w2.y));
            if(w1Cp.equals(ChunkTools.toChunkPosition(position.x,position.z,src))) {
                return 1;
            }
            boolean c1inFrustum = c1.getCollider().testFrustum(ChunkModelBuilder.follow.getFrustum());
            boolean c2inFrustum = c2.getCollider().testFrustum(ChunkModelBuilder.follow.getFrustum());
            if(c1inFrustum && !c2inFrustum) return -1;
            if(!c1inFrustum && c2inFrustum) return 1;
            return position.distanceSquared(w1.x,position.y,w1.y) < position.distanceSquared(w2.x,position.y,w2.y) ? -1 : 1;
        }
    }

    private static class ChunkBlockBuilderThread extends Thread {
        @Override
        public void run() {
            while(running) {
                if(!isEmpty()) {
                    Chunk c = pollFirst();
                    if(c != null) {
                        c.generateBlocks();
                        Engine.invokeLater(() -> {
                            c.onChunkBuild();
                        });
                    }
                }
            }
        }
    }

}
