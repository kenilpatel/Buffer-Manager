
package bufmgr;

import diskmgr.*;
import global.*;
import java.util.ArrayList;

  /**
   * class MruPolicy is a subclass of class Replacer use the given replacement
   * MRU algorithm for page replacement
   */
class MruPolicy extends  Replacer {

  //
  // Frame State Constants
  //
  protected static final int AVAILABLE = 10;
  protected static final int REFERENCED = 11;
  protected static final int PINNED = 12;

  //ArrayList containing index number of recently used frames 
    private ArrayList<Integer> queue;

  /**
   * Class constructor
  */
    public MruPolicy(BufMgr mgrArg)
    {
      super(mgrArg);
      //Initializing the arraylist
      queue = new ArrayList<Integer>();
      
      // initialize the frame states
    for (int i = 0; i < frametab.length; i++) {
      frametab[i].state = AVAILABLE;
    }

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
   * Notifies the replacer of a pinned page.
   */
  public void pinPage(FrameDesc fdesc) {
    frametab[fdesc.index].state = PINNED;

    /* If page is present in Buffer, remove it 
       then add it again at start of the queue*/

    queue.remove(new Integer(fdesc.index));
    queue.add(0, new Integer(fdesc.index));
        
  }

  /**
   * Notifies the replacer of an unpinned page.
   */
  public void unpinPage(FrameDesc fdesc) {
    if(fdesc.pincnt==0)
    {
      //When pin count becomes 0, we mark the frame as REFERENCED
      frametab[fdesc.index].state = REFERENCED;
    }
    else
    {
      frametab[fdesc.index].state = PINNED;
    }
 
  }


 public int pickVictim()
 {
   //If frame is AVAILABLE, choose it for replacement
    for (int i = 0; i < frametab.length; i++) {
      if(frametab[i].state == AVAILABLE)
      {
        return i;
      }
    }

   /* If no frame is AVAILABLE, choose Most Recently Used frame from 'queue' for replacement
      Here, the frame at the starting of queue is Most Recently Used
   */

    for(int i=0; i<queue.size(); i++)
    {
      int index = queue.get(i);
      if(frametab[index].state == REFERENCED)
      {
        return index;
      }
    }

    //If no frame is available, return -1 to indicate that buffer is full
    return -1;
 }

}