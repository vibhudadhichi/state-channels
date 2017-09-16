package papyrus.channel.node.integration;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

class Util {
    private static final long MAX_WAIT = 600000L;

    static <T> T waitFor(Supplier<T> supplier, Predicate<T> condition) throws InterruptedException {
        AtomicReference<T> reference = new AtomicReference<>();
        waitFor(()-> {
            T t = supplier.get();
            reference.set(t);
            return condition.test(t);
        });
        return reference.get();
    }

    static void waitFor(Supplier<Boolean> condition) throws InterruptedException {
        long start = System.currentTimeMillis();
        long sleep = 10L;
        do {
            long left = (start + MAX_WAIT) - System.currentTimeMillis();
            
            if (left < 0)
                throw new IllegalStateException("Timeout waiting for condition " + condition);

            Thread.sleep(Math.min(sleep, left));
            
            sleep = Math.min(5000L, (long) (1.5 * sleep));
            
        } while (!condition.get());
    }
}