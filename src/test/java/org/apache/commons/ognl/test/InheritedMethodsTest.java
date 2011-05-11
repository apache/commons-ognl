/**
 * 
 */
package org.apache.commons.ognl.test;

import junit.framework.TestCase;
import org.apache.commons.ognl.Node;
import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.test.objects.BaseBean;
import org.apache.commons.ognl.test.objects.FirstBean;
import org.apache.commons.ognl.test.objects.Root;
import org.apache.commons.ognl.test.objects.SecondBean;


/**
 * Tests functionality of casting inherited method expressions.
 * 
 */
public class InheritedMethodsTest extends TestCase
{
    
    private static Root ROOT = new Root();
    
    public void test_Base_Inheritance()
    throws Exception
    {
        OgnlContext context = (OgnlContext)Ognl.createDefaultContext(null);
        String expression = "map.bean.name";
        BaseBean first = new FirstBean();
        BaseBean second = new SecondBean();
        
        ROOT.getMap().put("bean", first);
        
        Node node = Ognl.compileExpression(context, ROOT, expression);
        
        assertEquals(first.getName(), node.getAccessor().get(context, ROOT));
        
        ROOT.getMap().put("bean", second);
        
        assertEquals(second.getName(), node.getAccessor().get(context, ROOT));
    }
}
