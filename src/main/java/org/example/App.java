package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App {
    private static final int MAX_THREADS_COUNT = 4;
    private static final int WORKING_TIME_MS = 5000;
    private static final boolean CHECK_CORRECT = false;
    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS_COUNT);
    private static final ReentrantLock globalMutex = new ReentrantLock();
    private static final Random random = new Random();
    public static final AtomicInteger ops = new AtomicInteger();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        for (int threadNum = 1; threadNum <= MAX_THREADS_COUNT; threadNum++) {
            System.out.println("Threads count: " + threadNum);
            for (Integer x : List.of(0, 10, 50)) {
                BstSeq bstSeq = new BstSeq(0);
                BstPar bstPar = new BstPar(0);

                List<Integer> keys = IntStream.range(1, 100_001).boxed()
                        .collect(Collectors.toList());
                prepopulate(bstSeq, bstPar, keys);

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                long deadline = System.currentTimeMillis() + WORKING_TIME_MS;
                for (int i = 0; i < threadNum; i++) {
                    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(
                            () -> executeProcess(x, bstSeq, bstPar, keys, deadline),
                            executor
                    );
                    futures.add(completableFuture);
                }
                for (CompletableFuture<Void> future : futures) {
                    future.get();
                }

                int ops = App.ops.get() / (WORKING_TIME_MS / 1000);
                System.out.println("x: " + x + "\tops: " + ops);

                if (CHECK_CORRECT) {
                    List<Integer> nodesFromSeq = bstSeq.inorderTraversal();
                    List<Integer> nodesFromPar = bstPar.inorderTraversal();
                    Collections.sort(nodesFromSeq);
                    Collections.sort(nodesFromPar);
                    assert (nodesFromSeq.equals(nodesFromPar));
                }
                App.ops.set(0);
            }
            System.out.println();
        }
        executor.shutdown();
    }

    private static void prepopulate(BstSeq bstSeq, BstPar bstPar, List<Integer> keys) {
        Collections.shuffle(keys);

        keys.forEach(k -> {
            if (random.nextInt(2) == 0) {
                bstSeq.insert(k);
                bstPar.insert(k);
            }
        });
    }

    private static void executeProcess(Integer x, BstSeq bstSeq, BstPar bstPar, List<Integer> keys, long deadline) {
        while (System.currentTimeMillis() <= deadline) {
            Integer key = keys.get(random.nextInt(keys.size()));
            int p = random.nextInt(101);
            if (p < x) {
                if (CHECK_CORRECT) {
                    globalMutex.lock();
                    bstSeq.insert(key);
                    globalMutex.unlock();
                }
                bstPar.insert(key);
            } else if (p < 2 * x) {
                if (CHECK_CORRECT) {
                    globalMutex.lock();
                    bstSeq.delete(key);
                    globalMutex.unlock();
                }
                bstPar.delete(key);
            } else if (p >= 2 * x) {
                if (CHECK_CORRECT) {
                    globalMutex.lock();
                    bstSeq.contains(key);
                    globalMutex.unlock();
                }
                bstPar.contains(key);
            }
            ops.getAndIncrement();
        }
    }
}