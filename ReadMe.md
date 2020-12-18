Buffer Manager

It is very important part of DBMS system and there exists several buffer management techniques which greatly impact the performance of DBMS system. In this project I have implmented several policies such as LRU, MRU, Random, FIFO, Clock etc.

In some context LRU is useful but as we know there is no silver bullet so here I compared a performance for each replacement policies by giving it a same type of input. One of the input contains the sequential flooding in which MRU outperforms LRU


Instructions to run the program

1) Copy the given code.
2) You need to set the SRCPATH and BINPATH to the directory in which you have put your code.
3) It is much easier to use makefile and the make command for compiling and executing your code and testing it.
4) If you are using Linux, make is already there. If you are using windows, you need to install Cygwin to get into a linux-like command prompt.
5) You may want to install Cygwin if you are going to use windows environment for this project. You also need the Java compiler and the runtime environment.
6) make bufmgr
7) make bmtest