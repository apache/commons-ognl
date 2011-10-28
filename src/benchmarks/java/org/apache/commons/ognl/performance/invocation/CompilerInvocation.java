package org.apache.commons.ognl.performance.invocation;

import org.apache.commons.ognl.performance.runtime.RuntimeWrapper;

/**
 * User: mcucchiara
 * Date: 28/10/11
 * Time: 18.42
 */
public class CompilerInvocation
    extends RepeatableInvocation
{
    public CompilerInvocation( RuntimeWrapper runtimeWrapper, int times )
        throws Exception
    {
        super( runtimeWrapper, times );

    }

    @Override
    protected void invoke( Class<?> c )
        throws Exception
    {
        getRuntime().getCompiler();
    }
}
