package com.puppycrawl.tools.checkstyle.checks.imports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;

public class AccessResultTest extends TestCase
{
    public void testNormal()
    {
        assertEquals("ALLOWED", AccessResult.ALLOWED.getLabel());
        assertEquals(AccessResult.ALLOWED.toString(), AccessResult.ALLOWED
                .getLabel());
        assertTrue(AccessResult.DISALLOWED.equals(AccessResult.DISALLOWED));
        assertFalse(AccessResult.DISALLOWED.equals(this));
        assertEquals(AccessResult.DISALLOWED.hashCode(),
                AccessResult.DISALLOWED.hashCode());
        final AccessResult revived = AccessResult
                .getInstance(AccessResult.UNKNOWN.getLabel());
        assertEquals(AccessResult.UNKNOWN, revived);
        assertTrue(AccessResult.UNKNOWN == revived);
    }

    public void testBadname()
    {
        try {
            AccessResult.getInstance("badname");
            fail("should not get here");
        }
        catch (IllegalArgumentException ex) {
            ;
        }
    }

    public void testSerial() throws Exception
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(AccessResult.ALLOWED);
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final AccessResult revivied = (AccessResult) ois.readObject();
        assertNotNull(revivied);
        assertTrue(revivied == AccessResult.ALLOWED);
    }
}
