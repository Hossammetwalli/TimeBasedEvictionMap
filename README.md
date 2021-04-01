# TimeBasedEvictionMap
Design and implement a data structure for time based eviction map. 



Structures Outline:

1)	Usage of Concurrent Hash Map which stores the keys and values only for a specific amount of time, and then expires after that time. 
    the hashmap will give us fast access to any item It should support two operations get and put in O(1) time..

2)	PriorityQueue to store data ordered by entry’s expiry time where the soonest expiring items move to the head of the queue.

This pattern is structured to satisfy special insertion or remove properties and back it up with
concurrent hashmap so we do not re-traverse the structures every time to find elements.

3)	Background thread that check If the expiry time of the most recent entry is in the future the thread waits for
    this expiry time to arrive. Once this time arrives the key will be removed.
    
4)	The assignment written in two classes one for the Map operation and the other for Services as the following: 

      A)	The service class includes two main methods one is for waiting for the element about to expire 
          and the other is for removing it once expired. Additionally, for time accuracy millisecond unites
          is used and additional method to convert nanoseconds to milliseconds.
          
      B)	The map operation class includes three main methods put(), get() and remove().
          The put() method stores the element ordered by expiry time in PriorityQueue list
          and then to the HashMap with key –value pair. Another Method for thread class is used to begin the execution of thread.
