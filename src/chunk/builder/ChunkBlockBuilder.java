package chunk.builder;

import chunk.Chunk;
import engine.Engine;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkBlockBuilder {
    private static Thread[] threads;
    private static TreeSet<Chunk> chunks = new TreeSet<>(new ChunkComparator());
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

    private static synchronized Chunk pollFirst() {
        return chunks.pollFirst();
    }

    static class ChunkComparator implements Comparator<Chunk>{
        private Vector2f w1=new Vector2f(),w2=new Vector2f();

        @Override
        public int compare(Chunk c1, Chunk c2) {
            w1.set(c1.getWorldPosition());
            w2.set(c2.getWorldPosition());
            Vector3f position = ChunkModelBuilder.follow.getTransform().getPosition();
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
                    c.generateBlocks();
                    Engine.invokeLater(()-> {
                        c.onChunkBuild();
                    });
                }
            }
        }
    }

}
