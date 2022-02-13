package com.github.chisui.parsec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

@Getter
public class StreamInput implements Input {

    public static final int DEFAULT_INITIAL_MARKER_CAPACITY = 8;
    public static final int DEFAULT_CHUNK_SIZE = 1024;
    public static final int DEFAULT_MAX_CHUNK_COUNT = 128;

    private final InputStream stream;
    private final int initialMarkerCapacity;
    private final int chunkSize;
    private final int maxChunkCount;
    private Window window;
    private int chunkCount;

    public static StreamInput of(@NonNull InputStream stream) {
        return new StreamInput(stream,
                DEFAULT_INITIAL_MARKER_CAPACITY,
                DEFAULT_CHUNK_SIZE,
                DEFAULT_MAX_CHUNK_COUNT);
    }

    public static StreamInput of(
            @NonNull InputStream stream,
            int initialMarkerCapacity,
            int chunkSize,
            int maxChunkCount) {
        return new StreamInput(stream,
                requirePositive("initialMarkerCapacity", initialMarkerCapacity),
                requirePositive("chunkSize", chunkSize),
                requirePositive("maxChunkCount", maxChunkCount));
    }

    private StreamInput(
            InputStream stream,
            int initialMarkerCapacity,
            int chunkSize,
            int maxChunkCount) {
        this.stream = stream;
        this.initialMarkerCapacity = initialMarkerCapacity;
        this.chunkSize = chunkSize;
        this.maxChunkCount = maxChunkCount;
        this.window = new Window();
    }

    @Getter
    private class Window implements Input.Chunk, AutoCloseable {

        private final List<Marker> markers = new ArrayList<>(initialMarkerCapacity);
        private byte[] volatileBytes = new byte[chunkSize];
        private int start = 0;
        private int read = 0;
        private int end = 0;
        private boolean isTail;
        private Window prev;
        private Window next;

        @Override
        public boolean isTail() {
            return isTail;
        }

        @Override
        public byte[] volatileBytes() {
            return volatileBytes;
        }

        @Override
        public int start() {
            return start;
        }

        @Override
        public int end() {
            return end;
        }

        private Window read(int size) throws IOException {
            checkReadable();
            if (hasBufferedInput()) {
                return readFromBuffer(size);
            } else if (!isTail && isFull()) {
                return readFromNextWindow(size);
            } else {
                return readFromStream(size);
            }
        }

        private Window readFromStream(int size) throws IOException {
            int toRead = min(volatileBytes.length - end, size);
            int readBytes = stream.read(volatileBytes, end, toRead);
            start = end;
            if (readBytes >= 0) {
                read += readBytes;
                end = read;
            }
            isTail = readBytes < toRead;
            return this;
        }

        private Window readFromBuffer(int size) {
            start = end;
            end = min(end + size, read);
            return this;
        }

        private Window readFromNextWindow(int size) throws IOException {
            if (chunkCount >= maxChunkCount) {
                throw new IOException("max chunk count of " + maxChunkCount + " reached");
            }
            if (next == null) {
                window = new Window();
                next = window;
                chunkCount++;
                if (markers.isEmpty()) {
                    close();
                } else {
                    window.prev = this;
                }
            }
            return next.read(size);
        }

        private boolean isFull() {
            return end == volatileBytes.length;
        }

        private boolean hasBufferedInput() {
            return read > end;
        }

        private void checkReadable() throws IOException {
            if (window != this && isClosed()) {
                throw new IOException("Window closed");
            }
        }

        public Marker mark() {
            Marker marker = new Marker(new WeakReference<>(window), end);
            markers.add(marker);
            return marker;
        }

        private void rewind(int pos) throws IOException {
            checkReadable();
            setPos(pos);
            window = this;
            resetUpstream();
        }

        private void setPos(int pos) {
            start = pos;
            end = pos;
        }

        private void resetUpstream() {
            for (Window w = next; w != null; w = w.next) {
                w.setPos(0);
            }
        }

        private void discard(Marker marker) throws IOException {
            if (markers.remove(marker) && prev == null) {
                for (Window w = this; w != window && !w.markers.isEmpty(); w = w.next) {
                    w.close();
                }
            }
        }

        private boolean isClosed() {
            return volatileBytes == null;
        }

        @Override
        public void close() throws IOException {
            if (prev != null && !markers.isEmpty()) {
                throw new IOException("can only close dangling tails");
            }
            if (!isClosed()) {
                chunkCount--;
            }
            volatileBytes = null;
        }
    }

    @Override
    public Input.Marker mark() {
        return window.mark();
    }

    private static class Marker implements Input.Marker {
        private final WeakReference<Window> window;
        private final int offset;

        private Marker(WeakReference<Window> window, int offset) {
            this.window = window;
            this.offset = offset;
        }

        @Override
        public void rewind() throws IOException {
            Window w = this.window.get();
            if (w == null) {
                throw new IOException("Can not rewind to closed Marker");
            }
            w.rewind(offset);
        }

        @Override
        public void close() throws IOException {
            Window w = this.window.get();
            if (w != null) {
                w.discard(this);
            }
        }

    }
    @Override
    public Chunk read(int size) throws IOException {
        return window.read(size);
    }

    @Override
    public void close() throws IOException {
        stream.close();
        window.close();
    }

    private static int requirePositive(String name, int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("Expected " + name + " to be positive but got " + i);
        }
        return i;
    }
}
