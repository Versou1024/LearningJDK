/*
 * Copyright (c) 1994, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.io;

/**
 * A data input stream lets an application read primitive Java data
 * types from an underlying input stream in a machine-independent
 * way. An application uses a data output stream to write data that
 * can later be read by a data input stream.
 * <p>
 * DataInputStream is not necessarily safe for multithreaded access.
 * Thread safety is optional and is the responsibility of users of
 * methods in this class.
 *
 * @author  Arthur van Hoff
 * @see     java.io.DataOutputStream
 * @since   JDK1.0
 */
public
class DataInputStream extends FilterInputStream implements DataInput {
    // DataInputStream 是数据输入流。它继承于FilterInputStream。
    // DataInputStream 是用来装饰其它输入流，它“允许应用程序以与机器无关方式从底层输入流中读取基本 Java 数据类型”。应用程序可以使用DataOutputStream(数据输出流)写入由DataInputStream(数据输入流)读取的数据

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param  in   the specified input stream
     */
    public DataInputStream(InputStream in) {
        super(in);
    }

    /**
     * working arrays initialized on demand by readUTF
     */
    private byte bytearr[] = new byte[80];
    private char chararr[] = new char[80];

    /**
     * Reads some number of bytes from the contained input stream and
     * stores them into the buffer array <code>b</code>. The number of
     * bytes actually read is returned as an integer. This method blocks
     * until input data is available, end of file is detected, or an
     * exception is thrown.
     *
     * <p>If <code>b</code> is null, a <code>NullPointerException</code> is
     * thrown. If the length of <code>b</code> is zero, then no bytes are
     * read and <code>0</code> is returned; otherwise, there is an attempt
     * to read at least one byte. If no byte is available because the
     * stream is at end of file, the value <code>-1</code> is returned;
     * otherwise, at least one byte is read and stored into <code>b</code>.
     *
     * <p>The first byte read is stored into element <code>b[0]</code>, the
     * next one into <code>b[1]</code>, and so on. The number of bytes read
     * is, at most, equal to the length of <code>b</code>. Let <code>k</code>
     * be the number of bytes actually read; these bytes will be stored in
     * elements <code>b[0]</code> through <code>b[k-1]</code>, leaving
     * elements <code>b[k]</code> through <code>b[b.length-1]</code>
     * unaffected.
     *
     * <p>The <code>read(b)</code> method has the same effect as:
     * <blockquote><pre>
     * read(b, 0, b.length)
     * </pre></blockquote>
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end
     *             of the stream has been reached.
     * @exception  IOException if the first byte cannot be read for any reason
     * other than end of file, the stream has been closed and the underlying
     * input stream does not support reading after close, or another I/O
     * error occurs.
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public final int read(byte b[]) throws IOException {
        return in.read(b, 0, b.length);
    }

    /**
     * Reads up to <code>len</code> bytes of data from the contained
     * input stream into an array of bytes.  An attempt is made to read
     * as many as <code>len</code> bytes, but a smaller number may be read,
     * possibly zero. The number of bytes actually read is returned as an
     * integer.
     *
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * <p> If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     *
     * <p> The first byte read is stored into element <code>b[off]</code>, the
     * next one into <code>b[off+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     *
     * <p> In every case, elements <code>b[0]</code> through
     * <code>b[off]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.
     *
     * @param      b     the buffer into which the data is read.
     * @param off the start offset in the destination array <code>b</code>
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end
     *             of the stream has been reached.
     * @exception  NullPointerException If <code>b</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     * @exception  IOException if the first byte cannot be read for any reason
     * other than end of file, the stream has been closed and the underlying
     * input stream does not support reading after close, or another I/O
     * error occurs.
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public final int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param      b   the buffer into which the data is read.
     * @exception  EOFException  if this input stream reaches the end before
     *             reading all the bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final void readFully(byte b[]) throws IOException {
        // 从“数据输入流”中读取数据并填满字节数组b中；没有填满数组b则一直读取，直到填满位置。
        // 从字节数组b的位置0开始存储，并且读取的字节个数等于b的长度
        readFully(b, 0, b.length);
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the number of bytes to read.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading all the bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final void readFully(byte b[], int off, int len) throws IOException {
        // 能确保一定读取到文件末尾
        // 因为当读取出来的in.read(b, off + n, len - n)不是len个字节时,会 n+=count 后继续尝试读取一次
        // 例如 len = 100 实际 in输入流 中只有 30 字节
        // 那么读取后 count = 30, n更新为30,然后in.read(b, off + 30, len - 30) -- 读到count为-1
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    /**
     * See the general contract of the <code>skipBytes</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if the contained input stream does not support
     *             seek, or the stream has been closed and
     *             the contained input stream does not support
     *             reading after close, or another I/O error occurs.
     */
    public final int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }

        return total;
    }

    /**
     * See the general contract of the <code>readBoolean</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the <code>boolean</code> value read.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final boolean readBoolean() throws IOException {
        // boolean -- 读取一个字节
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (ch != 0);
    }

    /**
     * See the general contract of the <code>readByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next byte of this input stream as a signed 8-bit
     *             <code>byte</code>.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final byte readByte() throws IOException {
        // byte -- 读取一个字节

        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    /**
     * See the general contract of the <code>readUnsignedByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next byte of this input stream, interpreted as an
     *             unsigned 8-bit number.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see         java.io.FilterInputStream#in
     */
    public final int readUnsignedByte() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    /**
     * See the general contract of the <code>readShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted as a
     *             signed 16-bit number.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading two bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final short readShort() throws IOException {
        // short -- 读取两个字节

        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * See the general contract of the <code>readUnsignedShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted as an
     *             unsigned 16-bit integer.
     * @exception  EOFException  if this input stream reaches the end before
     *             reading two bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final int readUnsignedShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }

    /**
     * See the general contract of the <code>readChar</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted as a
     *             <code>char</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading two bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final char readChar() throws IOException {
        // char -- 读取两个字节
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * See the general contract of the <code>readInt</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next four bytes of this input stream, interpreted as an
     *             <code>int</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading four bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final int readInt() throws IOException {
        // int -- 读取4个字节

        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        // 任何一个 ch 为负数,就表示没有读取到4个字节,抛出 EOFException 即可
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    private byte readBuffer[] = new byte[8];

    /**
     * See the general contract of the <code>readLong</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next eight bytes of this input stream, interpreted as a
     *             <code>long</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading eight bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public final long readLong() throws IOException {
        readFully(readBuffer, 0, 8);
        return (((long)readBuffer[0] << 56) +
                ((long)(readBuffer[1] & 255) << 48) +
                ((long)(readBuffer[2] & 255) << 40) +
                ((long)(readBuffer[3] & 255) << 32) +
                ((long)(readBuffer[4] & 255) << 24) +
                ((readBuffer[5] & 255) << 16) +
                ((readBuffer[6] & 255) <<  8) +
                ((readBuffer[7] & 255) <<  0));
    }

    /**
     * See the general contract of the <code>readFloat</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next four bytes of this input stream, interpreted as a
     *             <code>float</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading four bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.DataInputStream#readInt()
     * @see        java.lang.Float#intBitsToFloat(int)
     */
    public final float readFloat() throws IOException {
        // 利用  Float.intBitsToFloat()
        return Float.intBitsToFloat(readInt());
    }

    /**
     * See the general contract of the <code>readDouble</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next eight bytes of this input stream, interpreted as a
     *             <code>double</code>.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading eight bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @see        java.io.DataInputStream#readLong()
     * @see        java.lang.Double#longBitsToDouble(long)
     */
    public final double readDouble() throws IOException {
        // 利用  Double.longBitsToDouble()
        return Double.longBitsToDouble(readLong());
    }

    private char lineBuffer[];

    /**
     * See the general contract of the <code>readLine</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @deprecated This method does not properly convert bytes to characters.
     * As of JDK&nbsp;1.1, the preferred way to read lines of text is via the
     * <code>BufferedReader.readLine()</code> method.  Programs that use the
     * <code>DataInputStream</code> class to read lines can be converted to use
     * the <code>BufferedReader</code> class by replacing code of the form:
     * <blockquote><pre>
     *     DataInputStream d =&nbsp;new&nbsp;DataInputStream(in);
     * </pre></blockquote>
     * with:
     * <blockquote><pre>
     *     BufferedReader d
     *          =&nbsp;new&nbsp;BufferedReader(new&nbsp;InputStreamReader(in));
     * </pre></blockquote>
     *
     * @return     the next line of text from this input stream.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.BufferedReader#readLine()
     * @see        java.io.FilterInputStream#in
     */
    @Deprecated
    public final String readLine() throws IOException {
        // 1. 读取line的缓冲区
        char buf[] = lineBuffer;

        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

        // 2.空闲量/位移位置/字符c
        int room = buf.length;
        int offset = 0;
        int c;

loop:   while (true) {
            // 3. 读取一个字节
            switch (c = in.read()) {
                // 3.1 -1或者换行符\n,结束循环
              case -1:
              case '\n':
                break loop;

                // 3.2 特殊换行符,"\r\n"
              case '\r':
                int c2 = in.read();
                if ((c2 != '\n') && (c2 != -1)) {
                    if (!(in instanceof PushbackInputStream)) {
                        this.in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream)in).unread(c2);
                }
                break loop;

                // 3.3 正常情况
              default:
                  // 3.3.1 容量不够,需要buf扩容,扩容前将lineBuffer先复制过去
                if (--room < 0) {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(lineBuffer, 0, buf, 0, offset);
                    lineBuffer = buf;
                }
                buf[offset++] = (char) c;
                break;
            }
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

    /**
     * See the general contract of the <code>readUTF</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     a Unicode string.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading all the bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @exception  UTFDataFormatException if the bytes do not represent a valid
     *             modified UTF-8 encoding of a string.
     * @see        java.io.DataInputStream#readUTF(java.io.DataInput)
     */
    public final String readUTF() throws IOException {
        return readUTF(this);
    }

    /**
     * Reads from the
     * stream <code>in</code> a representation
     * of a Unicode  character string encoded in
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a> format;
     * this string of characters is then returned as a <code>String</code>.
     * The details of the modified UTF-8 representation
     * are  exactly the same as for the <code>readUTF</code>
     * method of <code>DataInput</code>.
     *
     * @param      in   a data input stream.
     * @return     a Unicode string.
     * @exception  EOFException            if the input stream reaches the end
     *               before all the bytes.
     * @exception  IOException   the stream has been closed and the contained
     *             input stream does not support reading after close, or
     *             another I/O error occurs.
     * @exception  UTFDataFormatException  if the bytes do not represent a
     *               valid modified UTF-8 encoding of a Unicode string.
     * @see        java.io.DataInputStream#readUnsignedShort()
     */
    public final static String readUTF(DataInput in) throws IOException {
        // 从“数据输入流”中读取“无符号的short类型”的值：
        // 注意：UTF-8输入流的前2个字节是数据的长度
        int utflen = in.readUnsignedShort();
        byte[] bytearr = null;
        char[] chararr = null;

        // 如果in本身是“数据输入流”，
        // 则   设置字节数组bytearr = "数据输入流"的成员bytearr
        //     设置字符数组chararr = "数据输入流"的成员chararr
        // 否则的话，新建数组bytearr和chararr
        if (in instanceof DataInputStream) {
            DataInputStream dis = (DataInputStream)in;
            if (dis.bytearr.length < utflen){
                dis.bytearr = new byte[utflen*2];
                dis.chararr = new char[utflen*2];
            }
            chararr = dis.chararr;
            bytearr = dis.bytearr;
        } else {
            bytearr = new byte[utflen];
            chararr = new char[utflen];
        }

        int c, char2, char3;
        int count = 0;
        int chararr_count=0;

        // 从“数据输入流”中读取数据并存储到字节数组bytearr中；从bytearr的位置0开始存储，存储长度为utflen。
        // 注意，这里是存储到字节数组！而且读取的是全部的数据。
        in.readFully(bytearr, 0, utflen);

        // 将“字节数组bytearr”中的数据 拷贝到 “字符数组chararr”中
        // 注意：这里相当于“预处理的输入流中单字节的符号”，因为UTF-8是1-4个字节可变的。
        while (count < utflen) {
            // 将每个字节转换成int值
            c = (int) bytearr[count] & 0xff;
            // UTF-8的每个字节的值都不会超过127；所以，超过127，则退出。
            if (c > 127) break;
            count++;
            // 将c保存到“字符数组chararr”中
            chararr[chararr_count++]=(char)c;
        }

        // 处理完输入流中单字节的符号之后，接下来我们继续处理。
        while (count < utflen) {
            // 下面语句执行了2步操作。
            // (01) 将字节由 “byte类型” 转换成 “int类型”。
            //      例如， “11001010” 转换成int之后，是 “00000000 00000000 00000000 11001010”
            // (02) 将 “int类型” 的数据左移4位
            //      例如， “00000000 00000000 00000000 11001010” 右移4位之后，变成 “00000000 00000000 00000000 00001100”
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
                // 若 UTF-8 是单字节，即 bytearr[count] 对应是 “0xxxxxxx” 形式；
                // 则 bytearr[count] 对应的int类型的c的取值范围是 0-7。
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++]=(char)c;
                    break;

                // 若 UTF-8 是双字节，即 bytearr[count] 对应是 “110xxxxx  10xxxxxx” 形式中的第一个，即“110xxxxx”
                // 则 bytearr[count] 对应的int类型的c的取值范围是 12-13。
                case 12: case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    char2 = (int) bytearr[count-1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                                "malformed input around byte " + count);
                    chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    break;

                // 若 UTF-8 是三字节，即 bytearr[count] 对应是 “1110xxxx  10xxxxxx  10xxxxxx” 形式中的第一个，即“1110xxxx”
                // 则 bytearr[count] 对应的int类型的c的取值是14 。
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    char2 = (int) bytearr[count-2];
                    char3 = (int) bytearr[count-1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                                "malformed input around byte " + (count-1));
                    chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6)  |
                            ((char3 & 0x3F) << 0));
                    break;

                // 若 UTF-8 是四字节，即 bytearr[count] 对应是 “11110xxx 10xxxxxx  10xxxxxx  10xxxxxx” 形式中的第一个，即“11110xxx”
                // 则 bytearr[count] 对应的int类型的c的取值是15
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                            "malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }
}
