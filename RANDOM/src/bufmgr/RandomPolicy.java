
package bufmgr;

import diskmgr.*;
import global.*;
import java.util.ArrayList;
import java.util.Random; 
  /**
   * class Policy is a subclass of class Replacer use the given replacement
   * policy algorithm for page replacement
   */
class RandomPolicy extends  Replacer {

  //
  // Frame State Constants
  //
  protected static final int AVAILABLE = 10;
  protected static final int REFERENCED = 11;
  protected static final int PINNED = 12;

  //Following are the fields required for LRU and MRU policies:
  /**
   * private field
   * An array to hold number of frames in the buffer pool
   */

    private int  frames[];
 
  /**
   * private field
   * number of frames used
   */   
  private int  nframes;
  private Random rand; 

  /** Clock head; required for the default clock algorithm. */
  protected int head;

  /**
   * This pushes the given frame to the end of the list.
   * @param frameNo	the frame number
   */
  private void update(int frameNo)
  {
     //This function is to be used for LRU and MRU
  }

  /**
   * Class constructor
   * Initializing frames[] pinter = null.
   */
    public RandomPolicy(BufMgr mgrArg)
    {
      super(mgrArg);
      // initialize the frame states
    for (int i = 0; i < frametab.length; i++) {
      frametab[i].state = AVAILABLE;
    }
      // initialize parameters for LRU and MRU
      nframes = 0;
      frames = new int[frametab.length];

    // initialize the clock head for Clock policy
      head = -1;
      rand = new Random();
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
  
  /**
   * Finding a free frame in the buffer pool
   * or choosing a page to replace using your policy
   *
   * @return 	return the frame number
   *		return -1 if failed
   */

 public int pickVictim()
 {
    // if frame is available then return its id s
    ArrayList<Integer> pages = new ArrayList<Integer>(); 
    for (int i = 0; i < frametab.length; i++) {
      if(frametab[i].state == AVAILABLE)
      {
        return i;
      }
      if(frametab[i].state == REFERENCED)
      {
        pages.add(i);
      }
    }
    /* if frame is not available then just add all pages with 
    0 pin count in array called pages */
    
    int size = pages.size();
    /* if the size of page is zero then return -1
     as no page is available for replacement */
    if(size==0)
    {
	   return -1;
    }
    /* if the size is one then return the only page available */
    else if(size==1)
    {
      return pages.get(0);
    }
    /* select index randomly from the array */
    else
    {
      int index = rand.nextInt(size - 1);
      return pages.get(index);
    }
 }

}