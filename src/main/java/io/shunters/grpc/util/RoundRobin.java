package io.shunters.grpc.util;

import java.util.Iterator;
import java.util.List;

/**
 * Created by mykidong on 2017-09-28.
 */
public class RoundRobin<T> {

    private Iterator<Robin<T>> it;
    private List<Robin<T>> list;

    public RoundRobin(List<Robin<T>> list) {
        this.list = list;
        it = list.iterator();
    }

    public T next() {
        // if we get to the end, start again
        if (!it.hasNext()) {
            it = list.iterator();
        }
        Robin<T> robin = it.next();

        return robin.call();
    }

    public static class Robin<T> {
        private T i;

        public Robin(T i) {
            this.i = i;
        }

        public T call() {
            return i;
        }
    }
}
