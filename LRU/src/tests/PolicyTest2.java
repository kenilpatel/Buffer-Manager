package tests;

import global.Convert;
import global.Minibase;
import global.Page;
import global.PageId;
import java.util.*;

/**
 * Test suite for the bufmgr layer.
 */
class PolicyTest2 extends TestDriver {

  /** The display name of the test suite. */
  private static final String TEST_NAME = "policy tests for random ";

  /**
   * Test application entry point; runs all tests.
   */
  public static void main(String argv[]) {

    // create a clean Minibase instance
    
    PolicyTest2 bmt = new PolicyTest2();

    bmt.create_minibase();

    // run all the test cases
    System.out.println("\n" + "Running " + TEST_NAME + "...");
    boolean status = PASS;
    status &= bmt.test4();
   

    // display the final results
    
    if (status != PASS) {
      System.out.println("Error(s) encountered during " + TEST_NAME + ".");
    } else {
      System.out.println("All " + TEST_NAME + " completed successfully!");
    }

  } // public static void main (String argv[])

protected boolean test4() {

     System.out.print("\n  started Test 4 ...");

    // we choose this number to ensure that at least one page
    // will have to be written during this test
    boolean status = PASS;
    int numPages = Minibase.BufferManager.getNumUnpinned() + 1;
    Page pg = new Page();
    PageId pid, rPid;
    PageId lastPid;
    PageId firstPid = new PageId();
    ArrayList <Integer> pinnedPages = new ArrayList<Integer>();
    HashMap <Integer, Integer> pinCount = new HashMap<Integer, Integer>();
    System.out.println("  - Allocate a bunch of new pages");
    try {
      firstPid = Minibase.BufferManager.newPage(pg, numPages*3);
    } catch (Exception e) {
      System.err.print("*** Could not allocate " + numPages*3);
      System.err.print(" new pages in the database.\n");
      e.printStackTrace();
      return false;
    }
System.out.print("\n  Random pinning and unpinning of pages ");
    // unpin that first page... to simplify our loop
    try {
      Minibase.BufferManager.unpinPage(firstPid, UNPIN_CLEAN);
    } catch (Exception e) {
      System.err.print("*** Could not unpin the first new page.\n");
      e.printStackTrace();
      status = FAIL;
    }
    Random random = new Random();
    random.setSeed(1000);
    Random randomU = new Random();
    randomU.setSeed(100);

    Random randomR = new Random();
    randomR.setSeed(4444);
    System.out.print("  - Write something on each one\n");
    pid = new PageId();
    rPid = new PageId();
    lastPid = new PageId();

    for (pid.pid = firstPid.pid, lastPid.pid = pid.pid + (numPages*3/4); status == PASS
        && pid.pid < lastPid.pid; pid.pid = pid.pid + 1) {
      rPid.pid=random.nextInt(lastPid.pid-firstPid.pid-15)+firstPid.pid;
      try {
        Minibase.BufferManager.pinPage(rPid, pg, PIN_DISKIO);
        
      } catch (Exception e) {
        status = FAIL;
        System.err.print("*** Could not pin new page " + rPid.pid + "\n");
        e.printStackTrace();
      }
      if(pinCount.get(rPid.pid) == null){
      pinnedPages.add(rPid.pid);
        pinCount.put(rPid.pid, 1);
      }
      else{
        pinCount.put(rPid.pid, pinCount.get(rPid.pid)+1);
      }
    }

    for(int i=0; i<300; i++){
      rPid.pid = pinnedPages.get(randomR.nextInt(pinnedPages.size()));
      try {
        Minibase.BufferManager.pinPage(rPid, pg, PIN_DISKIO);
        
        pinCount.put(rPid.pid, pinCount.get(rPid.pid)+1);
      } catch (Exception e) {
        status = FAIL;
        System.err.print("*** Could not pin new page " + rPid.pid + "\n");
        e.printStackTrace();
      }
    }

    for (pid.pid = firstPid.pid, lastPid.pid = pid.pid+numPages*3; pid.pid < lastPid.pid; pid.pid = pid.pid + 1){
       int change = randomU.nextInt(pinnedPages.size());
       rPid.pid=pinnedPages.get(change);
       // System.out.println("change "+change+" rPid "+rPid.pid+" pinCount val "+pinCount.get(rPid.pid));

        if (status == PASS) {
          
            try {
            Minibase.BufferManager.unpinPage(rPid, UNPIN_CLEAN);
            } catch (Exception e) {
            status = FAIL;
            System.err
                .print("*** Could not unpin dirty page " + rPid.pid + "\n");
            e.printStackTrace();
          }
          if (pinCount.get(rPid.pid) == 1 && pinCount.get(rPid.pid)!= null){
              pinnedPages.remove(change);
              pinCount.remove(rPid.pid);
            }
            else
              pinCount.put(rPid.pid, pinCount.get(rPid.pid)-1);
            
          
        }
    }

      
if (status == PASS){
     
    //involke print and cleanup
    Minibase.BufferManager.printBhrAndRefCount();
    
    System.out.println("  Test 4 completed successfully.\n");
    System.out.println("max references vary from 16 to 12, high BHR1 and BHR2 values");
    }

    return status;

  } // protected boolean test4 ()

} // class BMTest extends TestDriver
