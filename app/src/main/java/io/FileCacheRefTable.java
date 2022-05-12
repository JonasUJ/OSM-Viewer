package io;

import collections.Cache;
import collections.Entity;
import collections.RefTable;
import collections.lru.EvictionAware;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileCacheRefTable<E extends Entity & Serializable> extends RefTable<E> {
    private final File file;
    final Cache<FileCacheRefTable<E>, List<E>> cache;

    public FileCacheRefTable(Cache<FileCacheRefTable<E>, List<E>> cache) throws IOException {
        this.cache = cache;
        file = File.createTempFile("filecache", "");
        file.deleteOnExit();
        cache.set(this, new EvictionAwareList<>(file));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<E> values() {
        var v = cache.get(this);

        if (v == null) {
            try (var stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                var list = (EvictionAwareList<E>) stream.readObject();
                list.file = file;
                v = list;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            cache.set(this, v);
        }

        return v;
    }

    private static class EvictionAwareList<E> extends ArrayList<E>
            implements EvictionAware, Serializable {
        private transient File file;

        public EvictionAwareList(File file) {
            this.file = file;
        }

        @Override
        public void onEvicted() {
            try (var stream =
                    new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                stream.writeObject(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
