
package bufmgr;

import diskmgr.*;
import global.*;
import java.util.ArrayList;
import java.util.Random; 
  /**
   * class ClockPolicy is a subclass of class Replacer use the given replacement
   * CLOCK algorithm for page replacement
   */
class ClockPolicy extends  Replacer {

  //
  // Frame State Constants
  //
  protected static final int AVAILABLE = 10;
  protected static final int REFERENCED = 11;
  protected static final int PINNED = 12;

  /** Clock head; required for the clock algorithm. */
  protected int head;

  /**
   * Class constructor
   */
    public ClockPolicy(BufMgr mgrArg)
    {
      super(mgrArg);

    // initialize the frame state to AVAILABLE
    for (int i = 0; i < frametab.length; i++) {
      frametab[i].state = AVAILABLE;
    }

    // initialize the clock head for Clock policy
      head = -1;
    }

  /**
   * Notifies the replacer of a new page.
   */
  public void newPage(FrameDesc fdesc) {
    // no need to update frame state
  }

  /**
   * Notifies the replacer of a free page.
   */
  public void freePage(FrameDesc fdesc) {
    fdesc.state = AVAILABLE;
  }

  /**
   * Notifies the replacer of a pined page.
   */
  public void pinPage(FrameDesc fdesc) {
    frametab[fdesc.index].state = PINNED;
  }

  /**
   * Notifies the replacer of an unpinned page.
   */
  public void unpinPage(FrameDesc fdesc) {
    if(fdesc.pincnt==0)
    {
      frametab[fdesc.index].state = REFERENCED;
    }
    else
    {
      frametab[fdesc.index].state = PINNED;
    }
  }

 public int pickVictim()
 {
   
  int nr = 0; //number of rotations
  int size = frametab.length; //size of frametab array

  //For 2 rotations, check if any frame is available
  while(nr<2)
  { // increment head variable
    head = head  + 1;

    if(head != head % size)
    { 
      nr = nr + 1; //increment number of rotations
      head = head % size;
    }

    //If frame state is AVAILABLE, choose it for replacement
    if(frametab[head].state == AVAILABLE)
    {
      return head;
    }

    //If frame state is 'REFERENCED', change it's state to 'AVAILABLE' and move ahead
    if(frametab[head].state == REFERENCED)
    {
      frametab[head].state = AVAILABLE;
    }
  }

  //If no frame is available, return -1 to indicate that buffer is full
  return -1;
 }

}