package org.apache.commons.ognl.test;

import junit.framework.TestCase;
import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.test.objects.IndexedSetObject;

/**
 * Tests for {@link ognl.ASTChain}.
 */
public class ASTChainTest extends TestCase {

    public void test_Get_Indexed_Value() throws Exception {

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
        IndexedSetObject root = new IndexedSetObject();

        String expr = "thing[\"x\"].val";

        assertEquals(1, Ognl.getValue(expr, context, root));

        Ognl.setValue(expr, context, root, new Integer(2));
        
        assertEquals(2, Ognl.getValue(expr, context, root));
    }
}
