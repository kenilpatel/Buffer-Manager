package bufmgr;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;
import java.util.HashMap;

/**
 * <h3>Minibase Buffer Manager</h3>
 * The buffer manager reads disk pages into a mains memory page as needed. The
 * collection of main memory pages (called frames) used by the buffer manager
 * for this purpose is called the buffer pool. This is just an array of Page
 * objects. The buffer manager is used by access methods, heap files, and
 * relational operators to read, write, allocate, and de-allocate pages.
 */
public class BufMgr implements GlobalConst {

    /** Actual pool of pages (can be viewed as an array of byte arrays). */
    protected Page[] bufpool;

    /** Array of descriptors, each containing the pin count, dirty status, etc\
  . */
    protected FrameDesc[] frametab;

    /** Maps current page numbers to frames; used for efficient lookups. */
    protected HashMap<Integer, FrameDesc> pagemap;

    /** The replacement policy to use. */
    protected Replacer replacer;

    private int totPageRequests;

    private int totPageHits;

    private int pageLoadHits;

    private int pageLoadRequests;

    private float aggregateBHR;

    private float pageLoadBHR;
    
//-------------------------------------------------------------



  /**
   * Constructs a buffer mamanger with the given settings.
   * 
   * @param numbufs number of buffers in the buffer pool
   */
  public BufMgr(int numbufs) 
  {   
    //initializing buffer pool and frame table 
    bufpool = new Page[numbufs];
      frametab = new FrameDesc[numbufs];
      
      for(int i = 0; i < frametab.length; i++)
      {
              bufpool[i] = new Page();
            frametab[i] = new FrameDesc(i);
      }
      
      //initializing page map and replacer here. 
      pagemap = new HashMap<Integer, FrameDesc>(numbufs);
      replacer = new RandomPolicy(this);
      totPageRequests = -1;
      totPageHits = 0;
      pageLoadHits = 0;
      pageLoadRequests = -1;
  }

  /**
   * Allocates a set of new pages, and pins the first one in an appropriate
   * frame in the buffer pool.
   * 
   * @param firstpg holds the contents of the first page
   * @param run_size number of pages to allocate
   * @return page id of the first new page
   * @throws IllegalArgumentException if PIN_MEMCPY and the page is pinned
   * @throws IllegalStateException if all pages are pinned (i.e. pool exceeded)
   */

  public PageId newPage(Page firstpg, int run_size)
  {
    //Allocating set of new pages on disk using run size.
    PageId firstpgid = Minibase.DiskManager.allocate_page(run_size);
    try {
      //pin the first page using pinpage() function using the id of firstpage, page firstpg and skipread = PIN_MEMCPY(true)
      pinPage(firstpgid, firstpg, PIN_MEMCPY);
          }
          catch (Exception e) {
            //pinning failed so deallocating the pages from disk
            for(int i=0; i < run_size; i++)
            {   
              firstpgid.pid += i;
                Minibase.DiskManager.deallocate_page(firstpgid);
            }
            return null;
      }
    
    //notifying replacer
      replacer.newPage(pagemap.get(Integer.valueOf(firstpgid.pid)));
      //return the page id of the first page
      return firstpgid; 
  }
  
  /**
   * Deallocates a single page from disk, freeing it from the pool if needed.
   * 
   * @param pageno identifies the page to remove
   * @throws IllegalArgumentException if the page is pinned
   */
  public void freePage(PageId pageno) 
  {  
    //the frame descriptor as the page is in the buffer pool 
    FrameDesc tempfd = pagemap.get(Integer.valueOf(pageno.pid));
    //the page is in the pool so it cannot be null.
      if(tempfd != null)
      {
        //checking the pin count of frame descriptor
          if(tempfd.pincnt > 0)
              throw new IllegalArgumentException("Page currently pinned");
          //remove page as it's pin count is 0, remove the page, updating its pin count and dirty status, the policy and notifying replacer.
          pagemap.remove(Integer.valueOf(pageno.pid));
          tempfd.pageno.pid = INVALID_PAGEID;
          tempfd.pincnt = 0;
          tempfd.dirty = false;
          tempfd.state = RandomPolicy.AVAILABLE;
          replacer.freePage(tempfd);
      }
      //deallocate the page from disk 
      Minibase.DiskManager.deallocate_page(pageno);
  }

  /**
   * Pins a disk page into the buffer pool. If the page is already pinned, this
   * simply increments the pin count. Otherwise, this selects another page in
   * the pool to replace, flushing it to disk if dirty.
   * 
   * @param pageno identifies the page to pin
   * @param page holds contents of the page, either an input or output param
   * @param skipRead PIN_MEMCPY (replace in pool); PIN_DISKIO (read the page in)
   * @throws IllegalArgumentException if PIN_MEMCPY and the page is pinned
   * @throws IllegalStateException if all pages are pinned (i.e. pool exceeded)
   */
  public void pinPage(PageId pageno, Page page, boolean skipRead) 
  {  
    //the frame descriptor as the page is in the buffer pool 
    int index = Integer.valueOf(pageno.pid);
    FrameDesc tempfd = pagemap.get(Integer.valueOf(pageno.pid));
    if(tempfd != null)
    {
      //if the page is in the pool and already pinned then by using PIN_MEMCPY(true) throws an exception "Page pinned PIN_MEMCPY not allowed" 
          if(skipRead)
            throw new IllegalArgumentException("Page pinned so PIN_MEMCPY not allowed");
          else
          {
            //else the page is in the pool and has not been pinned so incrementing the pincount and setting Policy status to pinned
            tempfd.pincnt++;
            tempfd.state = RandomPolicy.PINNED;
              page.setPage(bufpool[tempfd.index]);
              if(index > 8)
              {
                totPageHits = totPageHits  + 1;
                totPageRequests = totPageRequests + 1;
                pageLoadHits = pageLoadHits + 1;
              }
              return;
          }
    }
    else
    {
      //as the page is not in pool choosing a victim
          int i = replacer.pickVictim();
          //if buffer pool is full throws an Exception("Buffer pool exceeded")
          if(i < 0)
            throw new IllegalStateException("Buffer pool exceeded");
                
          tempfd = frametab[i];
          
          //if the victim is dirty writing it to disk 
          if(tempfd.pageno.pid != -1)
          {
            pagemap.remove(Integer.valueOf(tempfd.pageno.pid));
            if(tempfd.dirty)
                Minibase.DiskManager.write_page(tempfd.pageno, bufpool[i]);
            
          }
          //reading the page from disk to the page given and pinning it. 
          if(skipRead)
            bufpool[i].copyPage(page);
          else
              Minibase.DiskManager.read_page(pageno, bufpool[i]);
          page.setPage(bufpool[i]);
    }
        //updating frame descriptor and notifying to replacer
        tempfd.pageno.pid = pageno.pid;
          tempfd.pincnt = 1;
          tempfd.dirty = false;
          pagemap.put(Integer.valueOf(pageno.pid), tempfd);
          tempfd.state =RandomPolicy.PINNED;
          if(index > 8)
          { 
            pageLoadRequests = pageLoadRequests + 1;
            totPageRequests = totPageRequests + 1;
          }
          replacer.pinPage(tempfd);
   
  }

  /**
   * Unpins a disk page from the buffer pool, decreasing its pin count.
   * 
   * @param pageno identifies the page to unpin
   * @param dirty UNPIN_DIRTY if the page was modified, UNPIN_CLEAN otherrwise
   * @throws IllegalArgumentException if the page is not present or not pinned
   */
  public void unpinPage(PageId pageno, boolean dirty) 
  {  
    //the frame descriptor as the page is in the buffer pool 
    FrameDesc tempfd = pagemap.get(Integer.valueOf(pageno.pid));
    
    //if page is not present an exception is thrown as "Page not present"
      if(tempfd == null)
          throw new IllegalArgumentException("Page not present");
      
      //if the page is present but not pinned an exception is thrown as "page not pinned"
      if(tempfd.pincnt == 0)
      {
          throw new IllegalArgumentException("Page not pinned");
      } 
      else
      {
        //unpinning the page by decrementing pincount and updating the frame descriptor and notifying replacer
          tempfd.pincnt--;
          tempfd.dirty = dirty;
          if(tempfd.pincnt== 0)
          tempfd.state = RandomPolicy.REFERENCED;
          replacer.unpinPage(tempfd);
          return;
      }
  }

  /**
   * Immediately writes a page in the buffer pool to disk, if dirty.
   */
  public void flushPage(PageId pageno) 
  {  
    for(int i = 0; i < frametab.length; i++)
      //checking for pageid or id the pageid is the frame descriptor and the dirty status of the page
          if((pageno == null || frametab[i].pageno.pid == pageno.pid) && frametab[i].dirty)
          {
            //writing down to disk if dirty status is true and updating dirty status of page to clean
              Minibase.DiskManager.write_page(frametab[i].pageno, bufpool[i]);
              frametab[i].dirty = false;
          }
  }

  /**
   * Immediately writes all dirty pages in the buffer pool to disk.
   */
  public void flushAllPages() 
  {
    for(int i=0; i<frametab.length; i++) 
      flushPage(frametab[i].pageno);
  }

  /**
   * Gets the total number of buffer frames.
   */
  public int getNumBuffers() 
  {
    return bufpool.length;
  }

  /**
   * Gets the total number of unpinned buffer frames.
   */
  public int getNumUnpinned() 
  {
    int numUnpinned = 0;
    for(int i=0; i<frametab.length; i++) 
    {
      if(frametab[i].pincnt == 0)
        numUnpinned++;
    }
    return numUnpinned;
  }


  public void printBhrAndRefCount(){ 
  
    //print counts:
    System.out.println("totPageHits: "+totPageHits);
    System.out.println("totPageRequests: "+totPageRequests);
    System.out.println("pageLoadHits: "+pageLoadHits);
    System.out.println("pageLoadRequests: "+pageLoadRequests);
    System.out.println("+----------------------------------------+");
    
    
    //compute BHR1 and BHR2 
    aggregateBHR = (float)totPageHits/totPageRequests;   
    pageLoadBHR = (float)pageLoadHits/pageLoadRequests;
  
    System.out.print("Aggregate BHR (BHR1):");
    System.out.printf("%9.5f\n", aggregateBHR);
    System.out.print("Load-based BHR (BHR2):");
    System.out.printf("%9.5f\n", pageLoadBHR);
    System.out.println("+----------------------------------------+");
    
     
  }


} // public class BufMgr implements GlobalConst