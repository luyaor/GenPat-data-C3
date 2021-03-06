import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Describe class AllTests
 * @author Rick Giles
 * @version 28-Jun-2003
 */
public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for J2EE checks");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(EntityBeanCheckTest.class));
        suite.addTest(new TestSuite(EntityBeanEjbCreateCheckTest.class));
        suite.addTest(new TestSuite(MessageBeanCheckTest.class));
        suite.addTest(new TestSuite(SessionBeanCheckTest.class));
        suite.addTest(new TestSuite(SessionBeanEjbCreateCheckTest.class));
        //$JUnit-END$
        return suite;
    }
}
