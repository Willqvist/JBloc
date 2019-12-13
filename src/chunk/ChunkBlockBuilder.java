package chunk;

import engine.Engine;
import engine.model.ModelAttribute;
import engine.model.ModelBuilder;
import entities.Entity;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkBlockBuilder implements Runnable {
    private static Thread[] threads;
    private static final Lock lock = new ReentrantLock();
    private final static Condition emptyQueue  = lock.newCondition();
    private static TreeSet<Chunk> chunks = new TreeSet<Chunk>(new ChunkComparator());
    private int id = 0;
    private static boolean running = true;
    public ChunkBlockBuilder(int id){
        this.id = id;
    }

    public static void init(int numThreads){
        threads = new Thread[numThreads];
        Engine.window.onExit(ChunkBlockBuilder::onExit);
        for(int i = 0; i < numThreads; i++){
            threads[i] = new Thread(new ChunkBlockBuilder(i));
            threads[i].start();
        }

    }
    public static void addChunkUnsafe(Chunk chunk){
        chunks.add(chunk);
        emptyQueue.signalAll();
    }

    public static void addChunk(Chunk chunk){
        lock();
        addChunkUnsafe(chunk);
        unlock();
    }

    public static void lock(){
        lock.lock();
    }

    public static void unlock(){
        lock.unlock();
    }

    public static void addChunks(Chunk ...chunks){
        lock.lock();
        for(int i = 0; i < chunks.length; i++) {
            addChunkUnsafe(chunks[i]);
        }
        lock.unlock();
    }

    private static void onExit() {
        running = false;
        lock();
        emptyQueue.signalAll();
        unlock();
    }

    public static boolean tryLock() {
        return lock.tryLock();
    }

    private void waitQueue(){
        while(chunks.isEmpty() && running){
            try {
                //System.out.println("waiting: " + j + " thread: " + Thread.currentThread().getName());
                emptyQueue.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        while(running) {
            lock.lock();
            waitQueue();
            if(!chunks.isEmpty()) {
                Chunk c = chunks.pollFirst();
                lock.unlock();
                c.generateBlocks();
                c.onChunkBuild();
            }
        }
    }

    public static void join() {
        for(int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

}
