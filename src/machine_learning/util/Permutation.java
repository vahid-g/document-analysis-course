/*************************************************************************
 *  Compilation:  javac Permutation.java
 *  Execution:    java Permutation N
 *  
 *  Prints a pseudorandom permution of the integers 0 through N.
 *
 *    % java Shuffle 6
 *    5 0 2 3 1 4 
 *    . * . . . . 
 *    . . . . * . 
 *    . . * . . . 
 *    . . . * . . 
 *    . . . . . * 
 *    * . . . . . 
 *
 *************************************************************************/

package machine_learning.util;

public class Permutation { 
	
   public static void main(String[] args) { 
	   
	   int N = 20;
	   int[] a = Permutation.permute(N);
		   
	   // print permutation
	   for (int i = 0; i < N; i++)
	      System.out.print(a[i] + " ");
	   System.out.println("");

	   // print checkerboard visualization
	   for (int i = 0; i < N; i++) {
	      for (int j = 0; j < N; j++) {
	         if (a[j] == i) System.out.print("* ");
	         else           System.out.print(". ");
	      }
	      System.out.println("");
	   }
   }
   
   public static int[] permute(int N) {

      int[] a = new int[N];

      // insert integers 0..N-1
      for (int i = 0; i < N; i++)
         a[i] = i;

      // shuffle
      for (int i = 0; i < N; i++) {
         int r = (int) (Math.random() * (i+1));     // int between 0 and i
         int swap = a[r];
         a[r] = a[i];
         a[i] = swap;
      }
      
      return a;
   }
}
