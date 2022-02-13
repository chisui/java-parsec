package com.github.chisui.parsec;

import java.io.IOException;
import java.util.Arrays;

/**
 * A source of bytes that allows multiple markers.
 */
public interface Input extends AutoCloseable {

    /**
     * Mark the current position of the input to return to in the future. Markers are only valid until they are closed.
     * Behavior of a closed {@link Marker} is undefined but will usually result in an {@link IOException}. The
     * {@link Input} may also have a limit on how far a {@link Marker} may be behind its head, since usually all
     * data from the oldest {@link Marker} to the head has to be buffered. In that case either {@link #read(int)} or
     * {@link Marker#rewind()} may throw an {@link IOException} depending on the {@link Input} implementation. Due to
     * that reason Markers should be closed as soon as possible.
     *
     * References to a {@link Marker} should also be released as soon as possible, since they may in turn hold
     * references to an Inputs internal state. To use short lived Markers you may use
     * {@link #withMarker(CheckedFunction)}
     *
     * @return a marker of the current position
     * @see Marker
     * @see #withMarker(CheckedFunction)
     * @see #read(int)
     */
    Marker mark();

    /**
     * A Marker
     */
    interface Marker extends AutoCloseable {
        void rewind() throws IOException;
        @Override
        default void close() throws IOException {
        }
    }

    default <A> A withMarker(CheckedFunction<IOException, ? super Marker, ? extends A> f) throws IOException {
        Marker marker = null;
        try (Marker m = mark()) {
            marker = m;
            return f.apply(m);
        } catch (IOException e) {
            marker.rewind();
            throw e;
        }
    }

    /**
     * Read up to size bytes from the input. read bytes will be in {@link Chunk#volatileBytes()} in the range between
     * {@link Chunk#start()} and up to {@link Chunk#end()}. Values of the array outside that range are undefined.
     *
     * The {@link Chunk} behavior of the chunk is only defined until the next call of {@link #read(int)},
     * {@link #close()} or {@link Marker#rewind()}. You should not hold a reference to a {@link Chunk}.
     * If you need to store values copy them out of the array or use {@link Chunk#copy()}.
     *
     * The fact that fewer bytes than requested where read does not indicate the end of data. To check for the end of
     * data use {@link Chunk#isTail()}.
     *
     * @param size number of requested bytes
     * @return the read {@link Chunk}
     * @throws IOException if there was an error retrieving the data
     */
    Chunk read(int size) throws IOException;

    interface Chunk {
        byte[] volatileBytes();
        int start();
        int end();

        default int size() {
            return end() - start();
        }

        boolean isTail();

        default byte[] copy() {
            return Arrays.copyOfRange(volatileBytes(), start(), end());
        }
    }

    @Override
    void close() throws IOException;
}
