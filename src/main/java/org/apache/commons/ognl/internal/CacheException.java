package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.OgnlException;

/**
 * User: Maurizio Cucchiara
 * Date: 10/9/11
 * Time: 6:14 PM
 */
public class CacheException
    extends OgnlException
{
    public CacheException( Throwable e )
    {
       super(e.getMessage(),e);
    }
}
