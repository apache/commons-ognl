package org.apache.commons.ognl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.reflect.Method;

/**
 * Superclass for OGNL exceptions, incorporating an optional encapsulated exception.
 */
public class OgnlException
    extends Exception
{
    // cache initCause method - if available..to be used during throwable constructor
    // to properly setup superclass.

    private static final long serialVersionUID = -842845048743721078L;

    static Method initCause;
    static
    {
        try
        {
            initCause = OgnlException.class.getMethod( "initCause", new Class[] { Throwable.class } );
        }
        catch ( NoSuchMethodException e )
        {
            /** ignore */
        }
    }

    /**
     * The root evaluation of the expression when the exception was thrown
     */
    private Evaluation evaluation;

    /**
     * Why this exception was thrown.
     * 
     * @serial
     */
    private Throwable reason;

    /** Constructs an OgnlException with no message or encapsulated exception. */
    public OgnlException()
    {
        this( null, null );
    }

    /**
     * Constructs an OgnlException with the given message but no encapsulated exception.
     * 
     * @param msg the exception's detail message
     */
    public OgnlException( String msg )
    {
        this( msg, null );
    }

    /**
     * Constructs an OgnlException with the given message and encapsulated exception.
     * 
     * @param msg the exception's detail message
     * @param reason the encapsulated exception
     */
    public OgnlException( String msg, Throwable reason )
    {
        super( msg );
        this.reason = reason;

        if ( initCause != null )
        {
            try
            {
                initCause.invoke( this, reason );
            }
            catch ( Exception ignored )
            {
                /** ignore */
            }
        }
    }

    /**
     * Returns the encapsulated exception, or null if there is none.
     * 
     * @return the encapsulated exception
     */
    public Throwable getReason()
    {
        return reason;
    }

    /**
     * Returns the Evaluation that was the root evaluation when the exception was thrown.
     * 
     * @return The {@link Evaluation}.
     */
    public Evaluation getEvaluation()
    {
        return evaluation;
    }

    /**
     * Sets the Evaluation that was current when this exception was thrown.
     * 
     * @param value The {@link Evaluation}.
     */
    public void setEvaluation( Evaluation value )
    {
        evaluation = value;
    }

    /**
     * Returns a string representation of this exception.
     * 
     * @return a string representation of this exception
     */
    @Override
    public String toString()
    {
        if ( reason == null )
        {
            return super.toString();
        }

        return super.toString() + " [" + reason + "]";
    }

    /**
     * Prints the stack trace for this (and possibly the encapsulated) exception on System.err.
     */
    @Override
    public void printStackTrace()
    {
        printStackTrace( System.err );
    }

    /**
     * Prints the stack trace for this (and possibly the encapsulated) exception on the given print stream.
     */
    @Override
    public void printStackTrace( java.io.PrintStream s )
    {
        synchronized ( s )
        {
            super.printStackTrace( s );
            if ( reason != null )
            {
                s.println( "/-- Encapsulated exception ------------\\" );
                reason.printStackTrace( s );
                s.println( "\\--------------------------------------/" );
            }
        }
    }

    /**
     * Prints the stack trace for this (and possibly the encapsulated) exception on the given print writer.
     */
    @Override
    public void printStackTrace( java.io.PrintWriter s )
    {
        synchronized ( s )
        {
            super.printStackTrace( s );
            if ( reason != null )
            {
                s.println( "/-- Encapsulated exception ------------\\" );
                reason.printStackTrace( s );
                s.println( "\\--------------------------------------/" );
            }
        }
    }
}
