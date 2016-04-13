
NAME : SIDDHARTH MARK JOSEPH
SUID : 384284085

DESIGN DECISIONS

1. In the Mapper function using the neighbours of a particular node I have created 2 hop paths
2. Consider the following example
C1: C2, C3, C5
In the above example for the string on the right hand side of  ":"  I have created all possible combinations i.e. C2_C3, C2_C5, C3_C2, C3_C5, C5_C2, C5_C3 and then insert C1 in the middle to create a two hop path i.e. C2_C1_C3, C2_C1_C5, C3_C1_C2, C3_C1_C5, C5_C1_C2, C5_C1_C3. So in this situttion C2 becomes the first node becomes the key and what follows becomes the value. C2 is the key and C1_C3 becomes the value.
3. In the Reducer function we eliminate the redundant path. For C1 we get path C2_C4 and C3_C4. Then I used a HashMap to count the number of 2 hops and wrote to the Output


Files Used: 
1. "input2.txt"