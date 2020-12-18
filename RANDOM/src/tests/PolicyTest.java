package tests;

import global.Convert;
import global.Minibase;
import global.Page;
import global.PageId;
import java.util.*;

/**
 * Test suite for the bufmgr layer.
 */
class PolicyTest extends TestDriver {

  /** The display name of the test suite. */
  private static final String TEST_NAME = "policy tests for LRU, MRU, Clock, FIFO, and Random";

  /**
   * Test application entry point; runs all tests.
   */
   private static final int MAX_PIN_COUNT = 10;
   private static final int MAX_SEQUENCE = 1000;
   private static final int MAX_ITERATIONS = 5;
   private static final int BUF_SIZE_MULTIPLIER = 3;
   private static final int OUTER_PAGE_FRACTION = 10;
   
  public static void main(String argv[]) {

    // create a clean Minibase instance
    
    PolicyTest bmt = new PolicyTest();

    bmt.create_minibase();

    // run all the test cases
    System.out.println("\n" + "Running " + TEST_NAME + "...");
    boolean status = PASS;
    status &= bmt.test1();
    
    bmt = new PolicyTest();
    bmt.create_minibase();
    status &= bmt.test2();

    // display the final results
    System.out.println();
    if (status != PASS) {
      System.out.println("Error(s) encountered during " + TEST_NAME + ".");
    } else {
      System.out.println("All " + TEST_NAME + " completed successfully!");
    }

  } // public static void main (String argv[])

  /**
   * 
   */
protected boolean test1() {

    System.out.print("\n  Starting Test 1 which is favorable to MRU ... \n");

    // we choose this number to ensure that at least one page
    // will have to be written during this test
    boolean status = PASS;
      
    int numPages = Minibase.BufferManager.getNumUnpinned()+1; //buf frames
    
    int numDiskPages = numPages*BUF_SIZE_MULTIPLIER;
    // System.out.print("numPages: buf and disk: \n" + numPages + "  --- " + numDiskPages);
    Page pg = new Page();
    PageId pid;
    PageId pid2;
    PageId firstPid = new PageId();
    System.out.println("  - Allocate all pages\n");
    try {
      firstPid = Minibase.BufferManager.newPage(pg, numDiskPages);
    } catch (Exception e) {
      System.err.print("*** Could not allocate " + numDiskPages);
      System.err.print(" new pages in the database.\n");
      e.printStackTrace();
      return false;
    }

    
    // unpin that first page... to simplify our loop
    try {
      Minibase.BufferManager.unpinPage(firstPid, UNPIN_CLEAN);
    } catch (Exception e) {
      System.err.print("*** Could not unpin the first new page.\n");
      e.printStackTrace();
      status = FAIL;
    }

    // now nothing is pinned; numPages in buffers and numDiskPages on disk
    
    System.out.print("  - Pin and unpin all pages in the buffer: \n");
    pid = new PageId();
    pid2 = new PageId();
    
    int partBufPages = numDiskPages/OUTER_PAGE_FRACTION; 

    for (pid.pid = firstPid.pid; status == PASS
        && pid.pid < firstPid.pid+partBufPages; pid.pid = pid.pid + 1) {
      try {
        Minibase.BufferManager.pinPage(pid, pg, PIN_DISKIO);
      } catch (Exception e) {
        status = FAIL;
        System.err.print("*** Could not pin new page " + pid.pid + "\n");
        e.printStackTrace();
      }
        if (status == PASS) {
          try {
            Minibase.BufferManager.unpinPage(pid, UNPIN_DIRTY);
          } catch (Exception e) {
            status = FAIL;
            System.err
                .print("*** Could not unpin dirty page " + pid.pid + "\n");
            e.printStackTrace();
          }
        }
        for (pid2.pid = firstPid.pid + partBufPages; status == PASS
        && pid2.pid < firstPid.pid+numDiskPages; pid2.pid = pid2.pid + 1) {

            try {
            Minibase.BufferManager.pinPage(pid2, pg, PIN_DISKIO);
            } catch (Exception e) {
            status = FAIL;
            System.err.print("*** Could not pin new page " + pid2.pid + "\n");
            e.printStackTrace();
        }
        if (status == PASS) {
          try {
            Minibase.BufferManager.unpinPage(pid2, UNPIN_DIRTY);
          } catch (Exception e) {
            status = FAIL;
            System.err.print("*** Could not unpin dirty page " + pid2.pid + "\n");
            e.printStackTrace();
          }
        }
      }
  }
   if (status == PASS){
     
    //invoke to print results
    Minibase.BufferManager.printBhrAndRefCount();
    
    System.out.print("  Test 1 completed successfully.\n");
    System.out.print("\n  2 hits on page 9; rest 1 hit\n");
    System.out.print("\n  higher values of BHR for MRU\n");
    System.out.print("\n  lower (and may be same) values of BHR for LRU and Clock (may be others)\n");
    System.out.print("\n  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
    }
    return status;


  } // protected boolean test1 ()
  
    
  protected boolean test2() {  // testing all policies thru random sequence of page 
                                // (multiple) accesses and check for page faults and BHR
  
    System.out.print("\n  Starting Test 2 (testing all policies; should show difference from policy to policy)... \n");
    System.out.print("\n  Also, SHOULD show different values of BHR's for policies in each round... \n");
    System.out.print("\n  Lower # of page faults --> better replacement poliy... \n");
    System.out.println("\n  higher BHR values --> better replacement poliy... \n");
    

    // we choose this number to ensure #pages > #bufpool
    // will use a seq of page nums to pin and unpin
    boolean status = PASS;
          
    int numPages = Minibase.BufferManager.getNumUnpinned(); //buf frames
    int numDiskPages = numPages*BUF_SIZE_MULTIPLIER;
    
    //*System.out.print("numPages: buf and disk: " + numPages + "  and " + numDiskPages+"\n");
    Page pg = new Page();
    PageId pid;
    PageId firstPid = new PageId();
    System.out.println("  - Allocate all pages\n");
    try {
      firstPid = Minibase.BufferManager.newPage(pg, numDiskPages); //starting at 9
    } catch (Exception e) {
      System.err.print("*** Could not allocate " + numDiskPages);
      System.err.print(" new pages in the database.\n");
      e.printStackTrace();
      return false;
    }

    // unpin that first page... to simplify our loop
    try {
      Minibase.BufferManager.unpinPage(firstPid, UNPIN_CLEAN);
    } catch (Exception e) {
      System.err.print("*** Could not unpin the first new page.\n");
      e.printStackTrace();
      status = FAIL;
    }


    // now nothing is pinned; numPages in buffers and numDiskPages on disk
        
    System.out.print("  - load pages in random order (as well as pin) to generate hits and page faults: \n");
    pid = new PageId();
    
    //just do round robin and see whether it makes a diff between LRU and clock
    System.out.println("entering round robin stage ...\n");
    int rpid;
    int rPage;
    Random randomPage = new Random();
    Random iter = new Random();
    Random pin = new Random();
    iter.setSeed(1347);  //prime num
    pin.setSeed(1938);
    int it = MAX_ITERATIONS*BUF_SIZE_MULTIPLIER;
    for (int j = 1; j <= it; j++){
    
    for ( int i=0; i < numDiskPages; i++){
        
       pid.pid = firstPid.pid + i;
        
        for (int k = 1; k <= pin.nextInt(MAX_PIN_COUNT)+1; k++){
        try {
            Minibase.BufferManager.pinPage(pid, pg, PIN_DISKIO);
                        
            } catch (Exception e) {
            status = FAIL;
            System.err.print("*** Could not pin new page " + pid.pid + "\n");
            e.printStackTrace();
            }             
                
        // Copy the page number + 99999 onto each page. It seems
        // unlikely that this bit pattern would show up there by
        // coincidence.
        int data = pid.pid + 99999;
        Convert.setIntValue(data, 0, pg.getData());    
        
            try {
                Minibase.BufferManager.unpinPage(pid, UNPIN_DIRTY);
                } catch (Exception e) {
                    status = FAIL;
                    System.err.print("*** Could not unpin dirty page " + pid.pid + "\n");
                    e.printStackTrace();
                }
            }  
      
    }
    
    }
    if (status == PASS){   
        //invoke print 
        System.out.println("  Test 2: Round Robin for "+it+" iterations");  
        Minibase.BufferManager.printBhrAndRefCount();
         System.out.print("\n++++++++++++++++++++++++++==============\n");
        }       

  // randomly load pages, pin them and unpin them a large number of times
  // load pages in some order to generate page faults
    Random seq = new Random();
    seq.setSeed(999331); //another prime num;
    pin.setSeed(iter.nextInt(13447)+1);
    randomPage.setSeed(13); //another prime num; 
    
    for (int j = 1; j <= MAX_ITERATIONS; j++){
    
   
    for ( int i=1; i <= seq.nextInt(MAX_SEQUENCE)+1; i++){
    
        rpid = randomPage.nextInt(numDiskPages)+1;
        pid.pid = firstPid.pid + rpid;
        
        for (int k = 1; k <= pin.nextInt(MAX_PIN_COUNT)+1; k++){
        try {
            Minibase.BufferManager.pinPage(pid, pg, PIN_DISKIO);
                        
            } catch (Exception e) {
            status = FAIL;
            System.err.print("*** Could not pin new page " + pid.pid + "\n");
            e.printStackTrace();
            }             
                
        // Copy the page number + 99999 onto each page. It seems
        // unlikely that this bit pattern would show up there by
        // coincidence.
        int data = pid.pid + 99999;
        Convert.setIntValue(data, 0, pg.getData());    
        
            try {
                Minibase.BufferManager.unpinPage(pid, UNPIN_DIRTY);
                } catch (Exception e) {
                    status = FAIL;
                    System.err.print("*** Could not unpin dirty page " + pid.pid + "\n");
                    e.printStackTrace();
                }
            }  
      
    }
    if (status == PASS){   
        //invoke print 
        System.out.println("  Test 2: Iteration: "+j);  
        Minibase.BufferManager.printBhrAndRefCount();
         System.out.print("\n++++++++++++++++++++++++++==============\n");
        }       
    }
     
   if (status == PASS){
    System.out.println("  Test 2 completed successfully.\n");  
    System.out.println("\n  compare page faults for each policy\n");
    }
    return status;
  } // protected boolean test5 ()

} // class BMTest extends TestDriver
