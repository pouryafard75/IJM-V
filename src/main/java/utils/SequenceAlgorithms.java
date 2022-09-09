

package utils;

import tree.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SequenceAlgorithms {
    private SequenceAlgorithms() {}

    /**
     * Returns the longest common subsequence between two strings.
     *
     * @return a list of size 2 int arrays that corresponds
     *     to match of index in sequence 1 to index in sequence 2.
     */
    public static List<int[]> longestCommonSubsequence(String s0, String s1) {
        int[][] lengths = new int[s0.length() + 1][s1.length() + 1];
        for (int i = 0; i < s0.length(); i++)
            for (int j = 0; j < s1.length(); j++)
                if (s0.charAt(i) == (s1.charAt(j)))
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                else
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);

        return extractIndexes(lengths, s0.length(), s1.length());
    }

    /**
     * Returns the hunks of the longest common subsequence between s1 and s2.
     * @return the hunks as a list of int arrays of size 4 with start index and end index of sequence 1
     *     and corresponding start index and end index in sequence 2.
     */
    public static List<int[]> hunks(String s0, String s1) {
        List<int[]> lcs = longestCommonSubsequence(s0 ,s1);
        List<int[]> hunks = new ArrayList<int[]>();
        int inf0 = -1;
        int inf1 = -1;
        int last0 = -1;
        int last1 = -1;
        for (int i = 0; i < lcs.size(); i++) {
            int[] match = lcs.get(i);
            if (inf0 == -1 || inf1 == -1) {
                inf0 = match[0];
                inf1 = match[1];
            } else if (last0 + 1 != match[0] || last1 + 1 != match[1]) {
                hunks.add(new int[] {inf0, last0 + 1, inf1, last1 + 1});
                inf0 = match[0];
                inf1 = match[1];
            } else if (i == lcs.size() - 1) {
                hunks.add(new int[] {inf0, match[0] + 1, inf1, match[1] + 1});
                break;
            }
            last0 = match[0];
            last1 = match[1];
        }
        return hunks;
    }

    /**
     * Returns the longest common sequence between two strings as a string.
     */
    public static String longestCommonSequence(String s1, String s2) {
        int start = 0;
        int max = 0;
        for (int i = 0; i < s1.length(); i++) {
            for (int j = 0; j < s2.length(); j++) {
                int x = 0;
                while (s1.charAt(i + x) == s2.charAt(j + x)) {
                    x++;
                    if (((i + x) >= s1.length()) || ((j + x) >= s2.length())) break;
                }
                if (x > max) {
                    max = x;
                    start = i;
                }
            }
        }
        return s1.substring(start, (start + max));
    }

    /**
     * Returns the longest common subsequence between the two list of nodes. This version use
     *     type and label to ensure equality.
     *
     * @see Tree#hasSameTypeAndLabel(Tree)
     * @return a list of size 2 int arrays that corresponds
     *     to match of index in sequence 1 to index in sequence 2.
     */
    public static List<int[]> longestCommonSubsequenceWithTypeAndLabel(List<Tree> s0, List<Tree> s1) {
        int[][] lengths = new int[s0.size() + 1][s1.size() + 1];
        for (int i = 0; i < s0.size(); i++)
            for (int j = 0; j < s1.size(); j++)
                if (s0.get(i).hasSameTypeAndLabel(s1.get(j)))
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                else
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);

        return extractIndexes(lengths, s0.size(), s1.size());
    }

    /**
     * Returns the longest common subsequence between the two list of nodes. This version use
     *     type to ensure equality.
     *
     * @see Tree#hasSameType(Tree)
     * @return a list of size 2 int arrays that corresponds
     *     to match of index in sequence 1 to index in sequence 2.
     */
    public static List<int[]> longestCommonSubsequenceWithType(List<Tree> s0, List<Tree> s1) {
        int[][] lengths = new int[s0.size() + 1][s1.size() + 1];
        for (int i = 0; i < s0.size(); i++)
            for (int j = 0; j < s1.size(); j++)
                if (s0.get(i).hasSameType(s1.get(j)))
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                else
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);

        return extractIndexes(lengths, s0.size(), s1.size());
    }

    /**
     * Returns the longest common subsequence between the two list of nodes. This version use
     *     isomorphism to ensure equality.
     *
     * @see Tree#isIsomorphicTo(Tree)
     * @return a list of size 2 int arrays that corresponds
     *     to match of index in sequence 1 to index in sequence 2.
     */
    public static List<int[]> longestCommonSubsequenceWithIsomorphism(List<Tree> s0, List<Tree> s1) {
        int[][] lengths = new int[s0.size() + 1][s1.size() + 1];
        for (int i = 0; i < s0.size(); i++)
            for (int j = 0; j < s1.size(); j++)
                if (s0.get(i).isIsomorphicTo(s1.get(j)))
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                else
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);

        return extractIndexes(lengths, s0.size(), s1.size());
    }

    /**
     * Returns the longest common subsequence between the two list of nodes. This version use
     *     isomorphism to ensure equality.
     *
     * @see Tree#isIsoStructuralTo(Tree)
     * @return a list of size 2 int arrays that corresponds
     *     to match of index in sequence 1 to index in sequence 2.
     */
    public static List<int[]> longestCommonSubsequenceWithIsostructure(List<Tree> s0, List<Tree> s1) {
        int[][] lengths = new int[s0.size() + 1][s1.size() + 1];
        for (int i = 0; i < s0.size(); i++)
            for (int j = 0; j < s1.size(); j++)
                if (s0.get(i).isIsoStructuralTo(s1.get(j)))
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                else
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);

        return extractIndexes(lengths, s0.size(), s1.size());
    }

    private static List<int[]> extractIndexes(int[][] lengths, int length1, int length2) {
        List<int[]> indexes = new ArrayList<>();

        for (int x = length1, y = length2; x != 0 && y != 0; ) {
            if (lengths[x][y] == lengths[x - 1][y]) x--;
            else if (lengths[x][y] == lengths[x][y - 1]) y--;
            else {
                indexes.add(new int[] {x - 1, y - 1});
                x--;
                y--;
            }
        }
        Collections.reverse(indexes);
        return indexes;
    }
}
