
package cloud.hytora.simplejson.impl.parser;

import java.io.IOException;
import java.io.Writer;


public class WritingBuffer extends Writer {

    /**
     * The writer instance
     */
    private final Writer writer;

    /**
     * The filling amount
     */
    private int fill;

    /**
     * The current buffer chars
     */
    private final char[] buffer;

    public WritingBuffer(Writer writer, int bufferSize) {
        this.fill = 0;
        this.writer = writer;
        this.buffer = new char[bufferSize];
    }

    @Override
    public void write(int c) throws IOException {
        if (fill > buffer.length-1) {
            this.flush();
        }
        this.buffer[fill++ ] =(char)c;
    }
    @Override
    public void write(String str, int off, int len) throws IOException {
        if (this.fill > this.buffer.length - len) {
            this.flush();
            if (len > buffer.length) {
                this.writer.write(str, off, len);
                return;
            }
        }
        str.getChars(off, off + len, this.buffer, this.fill);
        this.fill += len;
    }

    @Override
    public void write(char[] buff, int off, int len) throws IOException {
        if (this.fill > this.buffer.length - len) {
            this.flush();
            if (len > this.buffer.length) {
                this.writer.write(buff, off, len);
                return;
            }
        }
        System.arraycopy(buff, off, this.buffer, this.fill, len);
        this.fill += len;
    }


    @Override
    public void flush() throws IOException {
        this.writer.write(this.buffer, 0, this.fill);
        this.fill = 0;
    }

    @Override
    public void close() throws IOException {
    }
}
