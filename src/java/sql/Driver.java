/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

import java.util.logging.Logger;

/**
 * The interface that every driver class must implement.
 * <P>The Java SQL framework allows for multiple database drivers.
 *
 * <P>Each driver should supply a class that implements
 * the Driver interface.
 *
 * <P>The DriverManager will try to load as many drivers as it can
 * find and then for any given connection request, it will ask each
 * driver in turn to try to connect to the target URL.
 *
 * <P>It is strongly recommended that each Driver class should be
 * small and standalone so that the Driver class can be loaded and
 * queried without bringing in vast quantities of supporting code.
 *
 * <P>When a Driver class is loaded, it should create an instance of
 * itself and register it with the DriverManager. This means that a
 * user can load and register a driver by calling:
 * <p>
 * {@code Class.forName("foo.bah.Driver")}
 * <p>
 * A JDBC driver may create a {@linkplain DriverAction} implementation in order
 * to receive notifications when {@linkplain DriverManager#deregisterDriver} has
 * been called.
 * @see DriverManager
 * @see Connection
 * @see DriverAction
 */
public interface Driver {
    // 每个驱动程序类必须实现的接口。
    // Java SQL 框架允许多个数据库驱动程序。
    // 每个驱动程序都应提供一个实现驱动程序接口的类。
    // DriverManager 将尝试加载尽可能多的驱动程序，然后对于任何给定的连接请求，它将依次要求每个驱动程序尝试连接到目标 URL。
    // 强烈建议每个 Driver 类应该是小而独立的，这样 Driver 类就可以被加载和查询，而不需要引入大量的支持代码。
    // 当一个 Driver 类被加载时，它应该创建一个自身的实例并将其注册到 DriverManager。这意味着用户可以通过调用来加载和注册驱动程序：

    /**
     * Attempts to make a database connection to the given URL.
     * The driver should return "null" if it realizes it is the wrong kind
     * of driver to connect to the given URL.  This will be common, as when
     * the JDBC driver manager is asked to connect to a given URL it passes
     * the URL to each loaded driver in turn.
     *
     * <P>The driver should throw an <code>SQLException</code> if it is the right
     * driver to connect to the given URL but has trouble connecting to
     * the database.
     *
     * <P>The {@code Properties} argument can be used to pass
     * arbitrary string tag/value pairs as connection arguments.
     * Normally at least "user" and "password" properties should be
     * included in the {@code Properties} object.
     * <p>
     * <B>Note:</B> If a property is specified as part of the {@code url} and
     * is also specified in the {@code Properties} object, it is
     * implementation-defined as to which value will take precedence. For
     * maximum portability, an application should only specify a property once.
     *
     * @param url the URL of the database to which to connect
     * @param info a list of arbitrary string tag/value pairs as
     * connection arguments. Normally at least a "user" and
     * "password" property should be included.
     * @return a <code>Connection</code> object that represents a
     *         connection to the URL
     * @exception SQLException if a database access error occurs or the url is
     * {@code null}
     */
    Connection connect(String url, java.util.Properties info)
        throws SQLException;
    // 尝试与给定 URL 建立数据库连接。如果驱动程序意识到连接到给定 URL 的驱动程序类型错误，则应返回“null”。
    // 这很常见，因为当要求 DriverManager 连接到给定的 URL 时，它会依次将 URL 传递给每个加载的驱动程序,即调用每个 Driver.connect() 方法

    /**
     * Retrieves whether the driver thinks that it can open a connection
     * to the given URL.  Typically drivers will return <code>true</code> if they
     * understand the sub-protocol specified in the URL and <code>false</code> if
     * they do not.
     *
     * @param url the URL of the database
     * @return <code>true</code> if this driver understands the given URL;
     *         <code>false</code> otherwise
     * @exception SQLException if a database access error occurs or the url is
     * {@code null}
     */
    boolean acceptsURL(String url) throws SQLException;
    // 检索驱动程序是否认为它可以打开到给定 URL 的连接。
    // 通常，如果驱动程序理解 URL 中指定的子协议，则返回true ，否则返回false。
    // 例如: jdbc:mysql://172.25.50.28/sdkdeveloper
    // 根据协议就是 jdbc:mysql 因此 com.mysql.cj.jdbc.Driver 驱动器是可以接受该url


    /**
     * Gets information about the possible properties for this driver.
     * <P>
     * The <code>getPropertyInfo</code> method is intended to allow a generic
     * GUI tool to discover what properties it should prompt
     * a human for in order to get
     * enough information to connect to a database.  Note that depending on
     * the values the human has supplied so far, additional values may become
     * necessary, so it may be necessary to iterate though several calls
     * to the <code>getPropertyInfo</code> method.
     *
     * @param url the URL of the database to which to connect
     * @param info a proposed list of tag/value pairs that will be sent on
     *          connect open
     * @return an array of <code>DriverPropertyInfo</code> objects describing
     *          possible properties.  This array may be an empty array if
     *          no properties are required.
     * @exception SQLException if a database access error occurs
     */
    DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info)
                         throws SQLException;

    // 驱动器的主/次版本号

    /**
     * Retrieves the driver's major version number. Initially this should be 1.
     *
     * @return this driver's major version number
     */
    int getMajorVersion();

    /**
     * Gets the driver's minor version number. Initially this should be 0.
     * @return this driver's minor version number
     */
    int getMinorVersion();


    /**
     * Reports whether this driver is a genuine JDBC
     * Compliant&trade; driver.
     * A driver may only report <code>true</code> here if it passes the JDBC
     * compliance tests; otherwise it is required to return <code>false</code>.
     * <P>
     * JDBC compliance requires full support for the JDBC API and full support
     * for SQL 92 Entry Level.  It is expected that JDBC compliant drivers will
     * be available for all the major commercial databases.
     * <P>
     * This method is not intended to encourage the development of non-JDBC
     * compliant drivers, but is a recognition of the fact that some vendors
     * are interested in using the JDBC API and framework for lightweight
     * databases that do not support full database functionality, or for
     * special databases such as document information retrieval where a SQL
     * implementation may not be feasible.
     * @return <code>true</code> if this driver is JDBC Compliant; <code>false</code>
     *         otherwise
     */
    boolean jdbcCompliant();
    // 是否完整支持JDBC API和完全支持SQL 92入门级

    //------------------------- JDBC 4.1 -----------------------------------

    /**
     * Return the parent Logger of all the Loggers used by this driver. This
     * should be the Logger farthest from the root Logger that is
     * still an ancestor of all of the Loggers used by this driver. Configuring
     * this Logger will affect all of the log messages generated by the driver.
     * In the worst case, this may be the root Logger.
     *
     * @return the parent Logger for this driver
     * @throws SQLFeatureNotSupportedException if the driver does not use
     * {@code java.util.logging}.
     * @since 1.7
     */
    public Logger getParentLogger() throws SQLFeatureNotSupportedException;
}
