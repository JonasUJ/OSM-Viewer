package io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class ProgressBarInputStream extends FilterInputStream {
    private final ProgressBar bar;
    private final double len;
    private long cur = 0;

    protected ProgressBarInputStream(InputStream in, ProgressBar bar, long len) {
        super(in);
        this.bar = bar;
        this.len = len;
    }

    @Override
    public int read() throws IOException {
        return update(super.read());
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return update(super.read(buf));
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return update(super.read(buf, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        return update(super.skip(n));
    }

    private int update(int n) {
        if (n != -1) {
            cur += n;
            Platform.runLater(() -> bar.setProgress(cur / len));
        }

        return n;
    }

    private long update(long n) {
        if (n != -1) {
            cur += n;
            Platform.runLater(() -> bar.setProgress(cur / len));
        }

        return n;
    }
}
