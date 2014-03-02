===============================================================================================================================================
Implementation Description of parallel merge sort:

MergeSortTest.java :
Logical flow of function 'parallelMergeSort':
1.Takes maximum number of processors available for the system (arg = NPROCS) as argument.
2.NPROCS number of threads are created. Each thread sorts [size of input array/NPROCS] number of elements concurrently by calling the sequential merge sort function.
3.Threads are joined and the results are merged to get a sorted array.

MPRecursiveMergeSortTest.java
Logical flow of function 'parallelMergeSort':
1.Takes maximum number of processors available as argument.
2.Recursive call to parallelMergeSort:
1.Initially, NPROCS/2 number of threads are created.
2.Each thread calls parallelMergeSort recursively.
3.The array passed is halved for each recursive call, left half passed to one thread, right half to another thread.
4.The number of threads to be created in each recursive call is halved.
5.When the number of threads = 1, sequential merge sort is called.
6.Threads are joined and the results are merged to get a sorted array

Exploiting More Parallelism :

Generate Random numbers in separate threads
File - MergeSortTest.java and MPRecursiveMergeSortTest.java
NPROCS number of threads are generated. The same input array is passed to return containing random values. NPROCS thread fill in the array (data) within different start and end locations (This avoids implementing shared variables) such that the entire array is filled at the
end. Threads are joined. In this implementation, java.util.random is used instead of COP5618Random. For large number of elements, parallel generation of random numbers instead of sequential for loop makes the program faster.

Threads created for iterations(POINTS_PER_RUN) while calling parallel merge sort
File - MPRecursiveMergeSortTest.java
Method where change is incorporated – Main
Change – Two threads are created, each thread will process POINTS_PER_RUN/2 iterations. To avoid data race, the array timedParallelSort is passed as an argument, and different sections of the array are filled in by different threads. Each iteration in turn calls parallelMergeSort. The argument NPROCS is reduced by two when passed to parallelMergeSort since two threads are being used here.

===============================================================================================================================================
CopyOnWriteBlockingHashMap.java

Follows CopyOnWrite approach to implement the HashMap interface. 
Introduces tryGet functions to -
1. Block the thread and wait until a value is inserted for the key for which get is called.
2. Allows interruption for get function (tryGetInterruptibly).
