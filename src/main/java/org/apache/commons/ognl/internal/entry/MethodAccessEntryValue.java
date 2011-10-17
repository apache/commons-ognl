package org.apache.commons.ognl.internal.entry;

/**
* User: Maurizio Cucchiara
* Date: 10/17/11
* Time: 1:13 AM
*/
public class MethodAccessEntryValue
{
    private boolean isAccessible;

    private boolean notPublic;

    public MethodAccessEntryValue( boolean accessible )
    {
        this.isAccessible = accessible;
    }

    public MethodAccessEntryValue( boolean accessible, boolean notPublic )
    {
        isAccessible = accessible;
        this.notPublic = notPublic;
    }

    public boolean isAccessible( )
    {
        return isAccessible;
    }

    public boolean isNotPublic( )
    {
        return notPublic;
    }
}
