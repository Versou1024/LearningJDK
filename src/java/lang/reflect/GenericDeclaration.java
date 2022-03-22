/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.reflect;

/**
 * A common interface for all entities that declare type variables.
 *
 * @since 1.5
 */
public interface GenericDeclaration extends AnnotatedElement {
    /**
     * 说明：GenericDeclaration有三个子类，java.lang.Class，java.lang.reflect.Constructor和java.lang.reflect.Method，
     * 也就是说，在Java中，只有在类，类的构造方法和类的方法这三个语法元素上可以声明泛型变量
     */
    /**
     * Returns an array of {@code TypeVariable} objects that
     * represent the type variables declared by the generic
     * declaration represented by this {@code GenericDeclaration}
     * object, in declaration order.  Returns an array of length 0 if
     * the underlying generic declaration declares no type variables.
     *
     * @return an array of {@code TypeVariable} objects that represent
     *     the type variables declared by this generic declaration
     * @throws GenericSignatureFormatError if the generic
     *     signature of this generic declaration does not conform to
     *     the format specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     */
    public TypeVariable<?>[] getTypeParameters();
}
