package DoubleDes.server;


class RoundKeyGenerator
{

    private final static byte[] PC1 =
            {
                    57, 49, 41, 33, 25, 17, 9,
                    1,  58, 50, 42, 34, 26, 18,
                    10, 2,  59, 51, 43, 35, 27,
                    19, 11, 3,  60, 52, 44, 36,
                    63, 55, 47, 39, 31, 23, 15,
                    7,  62, 54, 46, 38, 30, 22,
                    14, 6,  61, 53, 45, 37, 29,
                    21, 13, 5,  28, 20, 12, 4
            };


    private final static byte[] PC2=
            {
                    14, 17, 11, 24, 1,  5,
                    3,  28, 15, 6,  21, 10,
                    23, 19, 12, 4,  26, 8,
                    16, 7,  27, 20, 13, 2,
                    41, 52, 31, 37, 47, 55,
                    30, 40, 51, 45, 33, 48,
                    44, 49, 39, 56, 34, 53,
                    46, 42, 50, 36, 29, 32
            };


    private final static byte[] CIRCULAR_SHIFTS =
            {
                    1, 1, 2, 2,
                    2, 2, 2, 2,
                    1, 2, 2, 2,
                    2, 2, 2, 1
            };



    private int circularLeftShift(int input, int shift)
    {
        return ((input << shift) & DoubleDes.MASK_28_BITS) | (input >> (28 - shift));
    }


    private long permutationChoice1(long input)
    {
        return DoubleDes.genericPermutation(input, PC1, 64);
    }


    private long permutationChoice2(long input)
    {
        return DoubleDes.genericPermutation(input, PC2, 56);
    }


    long[] generateRoundKeys(long input)
    {
        input = permutationChoice1(input);
        int halfA = (int) (input >> 28);			// gets 28 MSBs
        int halfB = (int) (input & DoubleDes.MASK_28_BITS);	// masks 28 LSBs

        long[] roundKeys = new long[DoubleDes.NUM_OF_ROUNDS];

        // generates all the 58 bit round keys for each round of DES and stores them in an array
        for (int i = 0; i < DoubleDes.NUM_OF_ROUNDS; i++)
        {
            halfA = circularLeftShift(halfA, CIRCULAR_SHIFTS[i]);
            halfB = circularLeftShift(halfB, CIRCULAR_SHIFTS[i]);

            long joinedHalves = ((halfA & DoubleDes.MASK_32_BITS) << 28) | (halfB & DoubleDes.MASK_32_BITS);
            roundKeys[i] = permutationChoice2(joinedHalves);
        }
        return roundKeys;
    }
}