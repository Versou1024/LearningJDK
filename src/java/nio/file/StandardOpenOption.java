/*
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

/**
 * Defines the standard open options.
 *
 * @since 1.7
 */

public enum StandardOpenOption implements OpenOption {
    /**
     * Open for read access.
     */
    READ,
    // 打开以进行读取访问

    /**
     * Open for write access.
     */
    WRITE,
    // 打开以进行写访问。

    /**
     * If the file is opened for {@link #WRITE} access then bytes will be written
     * to the end of the file rather than the beginning.
     *
     * <p> If the file is opened for write access by other programs, then it
     * is file system specific if writing to the end of the file is atomic.
     */
    APPEND,
    // 如果打开文件以进行WRITE访问，则字节将被写入文件的末尾而不是开头。
    // 如果文件被其他程序打开以进行写访问，那么如果写入文件末尾是原子的，则它是文件系统特定的

    /**
     * If the file already exists and it is opened for {@link #WRITE}
     * access, then its length is truncated to 0. This option is ignored
     * if the file is opened only for {@link #READ} access.
     */
    TRUNCATE_EXISTING,
    // 如果文件已经存在并且它被打开以进行WRITE访问，则其长度被截断为 0。如果文件仅以READ访问权限打开，则忽略此选项。

    /**
     * Create a new file if it does not exist.
     * This option is ignored if the {@link #CREATE_NEW} option is also set.
     * The check for the existence of the file and the creation of the file
     * if it does not exist is atomic with respect to other file system
     * operations.
     */
    CREATE,
    // 如果不存在，则创建一个新文件。
    // 如果还设置了CREATE_NEW选项，则忽略此选项。检查文件是否存在以及如果文件不存在则创建文件对于其他文件系统操作而言是原子操作。

    /**
     * Create a new file, failing if the file already exists.
     * The check for the existence of the file and the creation of the file
     * if it does not exist is atomic with respect to other file system
     * operations.
     */
    CREATE_NEW,
    // 创建一个新文件，如果文件已经存在则失败。
    // 检查文件是否存在以及如果文件不存在则创建文件对于其他文件系统操作而言是原子操作。

    /**
     * Delete on close. When this option is present then the implementation
     * makes a <em>best effort</em> attempt to delete the file when closed
     * by the appropriate {@code close} method. If the {@code close} method is
     * not invoked then a <em>best effort</em> attempt is made to delete the
     * file when the Java virtual machine terminates (either normally, as
     * defined by the Java Language Specification, or where possible, abnormally).
     * This option is primarily intended for use with <em>work files</em> that
     * are used solely by a single instance of the Java virtual machine. This
     * option is not recommended for use when opening files that are open
     * concurrently by other entities. Many of the details as to when and how
     * the file is deleted are implementation specific and therefore not
     * specified. In particular, an implementation may be unable to guarantee
     * that it deletes the expected file when replaced by an attacker while the
     * file is open. Consequently, security sensitive applications should take
     * care when using this option.
     *
     * <p> For security reasons, this option may imply the {@link
     * LinkOption#NOFOLLOW_LINKS} option. In other words, if the option is present
     * when opening an existing file that is a symbolic link then it may fail
     * (by throwing {@link java.io.IOException}).
     */
    DELETE_ON_CLOSE,
    // 关闭时删除。当此选项存在时，实现会尽最大努力在通过适当的close方法关闭文件时尝试删除文件。
    // 如果未调用close方法，则在 Java 虚拟机终止时（正常情况下，如 Java 语言规范所定义，或在可能的情况下，异常情况下），将尽最大努力尝试删除文件。

    /**
     * Sparse file. When used with the {@link #CREATE_NEW} option then this
     * option provides a <em>hint</em> that the new file will be sparse. The
     * option is ignored when the file system does not support the creation of
     * sparse files.
     */
    SPARSE,
    // 稀疏文件。当与CREATE_NEW选项一起使用时，此选项会提示新文件将是稀疏的。当文件系统不支持创建稀疏文件时，该选项将被忽略。

    /**
     * Requires that every update to the file's content or metadata be written
     * synchronously to the underlying storage device.
     *
     * @see <a href="package-summary.html#integrity">Synchronized I/O file integrity</a>
     */
    SYNC,
    // 要求对文件内容或元数据的每次更新都同步写入底层存储设备。

    /**
     * Requires that every update to the file's content be written
     * synchronously to the underlying storage device.
     *
     * @see <a href="package-summary.html#integrity">Synchronized I/O file integrity</a>
     */
    DSYNC;
    // 要求对文件内容的每次更新都同步写入底层存储设备
}
