package com.jj.test.mlp;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Welcome to the Software Test. Please make sure you
 * read the instructions carefully.
 *
 * FAQ:
 * Can I use linq? Yes.
 * Can I cheat and look things up on Stack Overflow? Yes.
 * Can I use a database? No.
 */

/// There are two challenges in this file
/// The first one should takes ~10 mins with the
/// second taking between ~30-40 mins.

public class Program {
    public static void main(String[] args)
    {
        IChallenge[] challenges = new IChallenge[]
                {
                        new NumberCalculator(),
                        new RunLengthEncodingChallenge()
                };

        for (int i = 0; i < challenges.length; i++)
        {
            IChallenge challenge = challenges[i];
            String challengeName = challenge.getClass().getSimpleName();

            String result = challenge.winner()
                    ? String.format("You win at challenge %s", challengeName)
                    : String.format("You lose at challenge %s", challengeName);

            System.out.println(result);
        }
    }

    /// <summary>
    /// Challenge Uno - NumberCalculator
    ///
    /// Fill out the TODOs with your own code and make any
    /// other appropriate improvements to this class.
    /// </summary>
    static public class NumberCalculator implements IChallenge
    {
        /**
         *
         * @param numbers assuming at least one element
         * @return  the highest integer
         */
        public int findMax(int[] numbers)
                throws Exception
        {
            // TODO: Find the highest number
            if (numbers == null || numbers.length < 1) {
                throw new Exception("Input integer array can't be empty!");
            }
            int max = numbers[0];
            for (int i = 1; i < numbers.length; i++)
            {
                if (max < numbers[i])
                {
                    max = numbers[i];
                }
            }
            return max;
        }

        public int[] findMax(int[] numbers, int n)
        {
            // TODO: Find the 'n' highest numbers
            // create a min heap
            PriorityQueue<Integer> queue = new PriorityQueue<Integer>(n);
            for (int i = 0; i < numbers.length; i++)
            {
                queue.offer(numbers[i]);
                // maintain a heap of size n
                if (queue.size() > n)
                {
                    queue.poll();
                }
            }

            // get all elements from the heap and put them in reverse order
            int[] result = new int[queue.size()];
            for (int i = queue.size() - 1; i >= 0 ; i--)
            {
                result[i] = queue.poll();
            }

            return result;
        }

        public int[] sort(int[] numbers)
        {
            // TODO: Sort the numbers
            int[] result = numbers.clone();
            //Built-in sorting, using an improved quickSort
            Arrays.sort(result);
            //or do a custom bubble sort
            return result;
        }

        public boolean winner()
        {
            int[] numbers = new int[] { 5, 7, 5, 3, 6, 7, 9 };
            int[] sorted = sort(numbers);
            int[] maxes = findMax(numbers, 2);

            // TODO: Are the following test cases sufficient, to prove your code works
            // as expected? If not either write more test cases and/or describe what
            // other tests cases would be needed.

            // 1. we could test empty numbers array. findMax(int[] numbers) has been written to throw exception
            // 2. for findMax(int[] numbers, int n), we could test what if n >= numbers.length, this has been handled
            try
            {
                return sorted[0] == 3
                        && sorted[sorted.length - 1] == 9
                        && findMax(numbers) == 9
                        && maxes[0] == 9
                        && maxes[1] == 7;
            }
            catch (Exception excep)
            {
                return false;
            }
        }
    }

    /// <summary>
    /// Challenge Due - Run Length Encoding
    ///
    /// RLE is a simple compression scheme that encodes runs of data into
    /// a single data value and a count. It's useful for data that has lots
    /// of contiguous values (for example it was used in fax machines), but
    /// also has lots of downsides.
    ///
    /// For example, aaaaaaabbbbccccddddd would be encoded as
    ///
    /// 7a4b4c5d
    ///
    /// You can find out more about RLE here...
    /// http://en.wikipedia.org/wiki/Run-length_encoding
    ///
    /// In this exercise you will need to write an RLE **Encoder** which will take
    /// a byte array and return an RLE encoded byte array.
    /// </summary>
    static public class RunLengthEncodingChallenge implements IChallenge
    {
        public byte[] encode(byte[] original)
        {
            // TODO: Write your encoder here

            // we don't know in advance the size of result array.
            //      use StringBuffer to construct and then convert to byte[]
            StringBuffer dest = new StringBuffer();
            int originalSize = original.length;
            for (int i = 0; i < originalSize; i++)
            {
                int runLength = 1;
                while (i+1 < originalSize && original[i] == original[i+1]) {
                    runLength++;
                    i++;
                }
                //convert char to byte
                byte b = (byte)runLength;
                //need to convert byte to char
                dest.append((char)b);
                dest.append((char)original[i]);
            }
            return dest.toString().getBytes(Charset.forName("UTF-8"));
        }

        public boolean winner()
        {
            // TODO: Are the following test cases sufficient, to prove your code works
            // as expected? If not either write more test cases and/or describe what
            // other tests cases would be needed.

            // 1. other than just testing numbers, we could test combinations of [0-9] and [A-Z]
            Tuple[] testCases = new Tuple[]
                    {
                        new Tuple<byte[], byte[]>(new byte[]{0x01, 0x02, 0x03, 0x04}, new byte[]{0x01, 0x01, 0x01, 0x02, 0x01, 0x03, 0x01, 0x04}),
                        new Tuple<byte[], byte[]>(new byte[]{0x01, 0x01, 0x01, 0x01}, new byte[]{0x04, 0x01}),
                        new Tuple<byte[], byte[]>(new byte[]{0x01, 0x01, 0x02, 0x02}, new byte[]{0x02, 0x01, 0x02, 0x02})
                    };

            // TODO: What limitations does your algorithm have (if any)?

            // none found

            // TODO: What do you think about the efficiency of this algorithm for encoding data?

            // 1. maybe to create a long enough byte[] for the returned array instead of using StringBuffer to store the result,
            //      and store the count so as to return just the right data

            for (int i = 0; i < testCases.length; i++)
            {
                byte[] encoded = encode((byte[])testCases[i].item1);
                boolean isCorrect = Arrays.equals(encoded, (byte[])testCases[i].item2);

                if (!isCorrect)
                {
                    return false;
                }
            }

            return true;
        }

        public class Tuple<X, Y> {
            public final X item1;
            public final Y item2;
            public Tuple(X x, Y y) {
                this.item1 = x;
                this.item2 = y;
            }
        }
    }
}

