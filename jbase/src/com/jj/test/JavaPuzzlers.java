package com.jj.test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by yejaz on 12/18/2016.
 */
public class JavaPuzzlers {
    static class Point implements Cloneable{
        int x;
        int y;
        Point copy;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void setCopy(Point copy) {
            this.copy = copy;
        }

        //shallow copying vs deep copying
        //http://stackoverflow.com/questions/869033/how-do-i-copy-an-object-in-java
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
    public static void main(String[] args)
            throws CloneNotSupportedException {

        //puzzler 8
        char x = 'X';
        int i = 0;
        //=>X
        System.out.print(true  ? x : 0);
        //=>88, char is being treated as integer due to the 1st return value from i
        //who would write such a code to mix the returning type
        System.out.println(false ? i : x);
        i = x; //X is assigned to integer 88
        System.out.println(i);
        i ='a'; //'a'=97
        System.out.println(i);
        i ='A'; //'A'=65
        System.out.println(i);
        i ='x'; //'x'=120
        System.out.println(i);
        //so, 'A'=65, 'X'=88, 'Z'=65+26-1=90, 'a'=97, 'x'=120, 'z'=97+26-1=122

        testPasssByValue();
        //test shallow copy
        Point myPoint = new Point(100, 100);
        Point copiedPoint = new Point(50,50);
        copiedPoint.setCopy(new Point(-1, -1));
        myPoint.setCopy(copiedPoint);
        //this does the cloning on the internal object as well, is it realy shallow copying?
        //it looks it does the deep copying
        Point clonedPoint = (Point)myPoint.clone();
        //learning points:
        //clone is tricky to implement correctly.
        //It's better to use Defensive copying, copy constructors(as @egaga reply) or static factory methods.

        String myStr = new String("new is better!");
        String yourStr = "String literal is better!";
        //String's hash is not a final field, but it doesn't return it directly
        // but use it to generate the hashCode, always the same in any calls
        myStr.hashCode();
        //how making immutable would help thread safety and avoid synchronization
        //  it will fail even in compilation time or runtime
        //  can be shared between threads without synchronization in concurrent env
        //  this will boost performance since no need to do heavy synchronization
        //  also we could return an immutable object in the getter method directly, no need to create a copy
        //  Disadvantages: can't be reused to do setter for other cases.
        //      need to create a lot of immutable objects like String, so lots of garbage
        //      for GC to decide which to collect

        //native call to return the mem location from the String Constant Pool
        String s2 = myStr.intern();

        String[] dogs = {"fido", "clover", "gus", "aiko"};
        //a fixed size list backed by an Array
        List dogList = Arrays.asList(dogs);
        //ok
        dogs[0] = "fluffy";
        //throw exception as it's trying to add it to dogs which is passed to dogList as internal array
        dogList.add("spot");

        System.out.println("end here");
    }

    //pass by reference or pass by value: pass Object reference by value
    public static void testPasssByValue()
        throws CloneNotSupportedException{
        Point pnt1 = new Point(0,0);
        Point pnt2 = new Point(0,0);
        System.out.println("X: " + pnt1.x + " Y: " +pnt1.y);
        System.out.println("X: " + pnt2.x + " Y: " +pnt2.y);
        System.out.println(" ");
        tricky(pnt1,pnt2);
        System.out.println("X: " + pnt1.x + " Y:" + pnt1.y);
        System.out.println("X: " + pnt2.x + " Y: " +pnt2.y);

        Point pnt3 = new Point(100,100);
        Point pnt4 = new Point(0,0);
        System.out.println("X: " + pnt3.x + " Y: " +pnt3.y);
        System.out.println("X: " + pnt4.x + " Y: " +pnt4.y);
        System.out.println(" ");
        swapPoints(pnt3,pnt4);
        System.out.println("X: " + pnt3.x + " Y:" + pnt3.y);
        System.out.println("X: " + pnt4.x + " Y: " +pnt4.y);
    }

    //pnt1's address is copied to arg1, same for pnt2 to arg2
    //so now both pnt1 and arg1 is pointed to (0,0), same for pnt2 and arg2
    //http://www.javaworld.com/article/2077424/learn-java/does-java-pass-by-reference-or-pass-by-value.html
    public static void tricky(Point arg1, Point arg2)
    {
        //pnt1 is now (100,100)
        arg1.x = 100;
        arg1.y = 100;
        //temp is pointed to arg1/pnt1 and it's (100,100)
        Point temp = arg1;
        //arg1 is pointed to arg2, now it's (0,0)
        arg1 = arg2;
        //arg2 is pointed to temp, now it's (100,100)
        arg2 = temp;
        //pnt2 is still pointed to (0,0)
    }

    public static void swapPoints(Point arg1, Point arg2)
        throws CloneNotSupportedException{
        Point temp = (Point)arg1.clone();
        arg1.x = arg2.x;
        arg1.y = arg2.y;
        arg2.x = temp.x;
        arg2.y = temp.y;
    }
}
