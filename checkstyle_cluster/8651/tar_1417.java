package net.ubiquity.tools.checkstyle.tests;

public class InputDeclarationOrder
{
    static final int FOO2 = 3;

    // error public before package
    public static final int FOO = 3;
    
    private static final int FOO3 = 3;
   
    // eror public before package and private
    public static final int FOO4 = 3;

    private static final String ERROR = "error";

    // error protected before private
    protected static final String ERROR1 = "error";
   
    // error public before private
    public static final String WARNING = "warning";
    
    private int mMaxInitVars = 3;
    
    // error statics should be before instance members
    // error publics before private
    public static final int MAX_ITER_VARS = 3;

    private class InnerClass
    {
        private static final int INNER_FOO = 2;
       
        // error public before private
        public static final int INNER_FOO2 = 2;

        public InnerClass()
        {
            int foo = INNER_FOO;
            foo += INNER_FOO2;
            foo += INNER_FOO3;
        }

        // error member variables should be before methods or ctors
        // error public before private
        public static final int INNER_FOO3 = 2;
    }

    public int getFoo1()
    {
        return mFoo;
    }

    //  error ctors before methods
    public InputDeclarationOrder()
    {
        final String foo = ERROR;
        foo += ERROR1;
        foo += WARNING;
        int fooInt = mMaxInitVars;
        fooInt += MAX_ITER_VARS;
        fooInt += mFoo;
    }

    public static int getFoo2()
    {
        return mFoo;
    }

    public int getFoo()
    {
        return mFoo;
    }

    private static int getFoo2()
    {
        return mFoo;
    }

    // error member variables should be before methods or ctors
    private int mFoo = 0;
}
