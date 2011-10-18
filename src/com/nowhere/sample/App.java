
/////////////////////////////////////////////////////////////////////////////////////////
//
//  Kindle-ConceptDemo
// 
//  Source code (C) Copyright 2010 Markus Schmidt (m.schmidt@emtec.com)
//  Published under MIT License (http://en.wikipedia.org/wiki/MIT_License)  
//
//
/////////////////////////////////////////////////////////////////////////////////////////

package com.nowhere.sample;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;

import org.apache.log4j.Logger;

import com.amazon.kindle.kindlet.AbstractKindlet;
import com.amazon.kindle.kindlet.KindletContext;



/////////////////////////////////////////////////////////////////////////////////////////
//
//  This demo illustrates a few concepts to build a robust Kindlet
//  - handling life cycle correctly (actually this is rather difficult)
//  - logging program progess (essential to debugging)
//  - aiding garbage collection when destroying 
//  - handling screen sizing to adjust to different kindle models and device orientation
//  - catching five-way controller and general keyboard handling events
//  - understanding screen redraws
//  - painting a game board either with child components or via paint()
//  - loading prescaled images to improve drawing speed 
//
//
//  Some more info about Kindle Development:
//
//  - lessons learned:
//		http://forums.kindlecentral.com/forums/thread.jspa?threadID=535&tstart=0
//  - all my wisdom:
// 		http://forums.kindlecentral.com/forums/profile.jspa?userID=363
//
/////////////////////////////////////////////////////////////////////////////////////////


public class App extends AbstractKindlet
{
	/*****************************************************************************************
    * 
	*   Some basic helper stuff which we will need
    * 
    * 
    * 
	*****************************************************************************************/

	//
	// App-logger.  See Amazon's log4j sample for more details, e.g. you will  
	// want to log to file when running on the actual device. A technique like 
	// this is essential for debugging and understanding how a Kindlet works.
	//
	private static long startTime= System.currentTimeMillis();
	private static Logger logger= Logger.getLogger("App");
	
	// log a string together with time and thread
	public static void logit(String msg)
	{
		double timestamp= (System.currentTimeMillis()-App.startTime)/1000.0;
		logger.info("[" + timestamp + " - " + Thread.currentThread().getName() + "]   " + msg);
	}


	
	//
	// Start() stop() synchronisation and detection.  Necessary to handle screensaver/USB 
	// stuff in a halfway robust way, given the abusive testing that Q/A seems to do.
	//
	private static boolean stopped= false;

	// set the global stop flag
	public static void setStopped(boolean yesno)
	{
		App.stopped= yesno;
	}
	
	// check the global stop flag (especially long running threads call that periodically)
	public static boolean isStopped()
	{
		return App.stopped;
	}
	
	
	
	
	
	/*****************************************************************************************
    * 
	*   Below is the actual Kindlet with its lifecycle calls
    * 
    * 
    * 
	*****************************************************************************************/

	
	/*****************************************************************************************
	* members
	*****************************************************************************************/
	
	KindletContext kindleContext;
	Container rootContainer;
	GameBoard gameBoard;
	boolean initialStartDone;

	
	/*****************************************************************************************
	* lifecycle methods
	*****************************************************************************************/
	
    // ---------------------------------------------------------------------------------------
	//  app is created. only do very very basic stuff here. 
    // ---------------------------------------------------------------------------------------
	public void create(final KindletContext context)
	{
		App.logit("App::create");
				
		this.kindleContext= context;
		this.rootContainer= this.kindleContext.getRootContainer(); 

		App.logit("App::create done");
	}
	
	

    // ---------------------------------------------------------------------------------------
	//  start or restart. this part is intricately modeled, only add code here if you have Timers or Threads
    // ---------------------------------------------------------------------------------------
	public void start()
	{
		App.logit("App::start");

		synchronized (this) {	// avoid having a threaded stop() event intercept start() before being finished
			
	    	App.logit("App::start syncrhonized");
	    	
			App.setStopped(false);
			
	    	super.start();
	
			//
			// Be prepared for start() to be called multiple times during the lifecycle. 
			// E.g. at actual app-start, but also after coming back from screen saver.
			// 
			// We do the app building only on first start  
			//
			if (!this.initialStartDone) {  
				
				App.logit("App::start initial");
				
		    	// run the actual app building after the start() call has completed, 
		    	// because the start() code is limited to 5sec. and it also can't yet 
				// update the screen (e.g. a force-redraw will not work until start() 
				// is done).  
				if (!App.isStopped()) {
					
					Runnable runnable= 
						new Runnable() 
						{
						    public void run() 
						    { 
						    	App.this.initalStart();
						    }
						};
					App.logit("App::start will post runnable");
					EventQueue.invokeLater(runnable);
				}
			}
			else {	 // secondard start() (after screensaver, etc.)

				App.logit("App::start resume after pause");
				
				// usually nothing much to do here, unless you did something 
				// in stop() that needs to be rebuilt.
				
			}
			

			// Note: if you have timers create them here.  Timers/Threads need to stopped/destroyed in the 
			// stop() method because otherwise they would continue running and drain the system while sleeping.  
			// Hence they need to be recreated on both start() types (initial start() and usb/screensaver start()).
			
			/* TODO: build timers and possibly background threads (if any) ... */
		}
		
		App.logit("App::start done");
	}
	
	
	
    // ---------------------------------------------------------------------------------------
	//  first start after create(). most of your app building code will go here. 
    // ---------------------------------------------------------------------------------------
	public void initalStart() 
	{
		App.logit("App::initalStart");

		// don't bother with executing this when someone tried to stopped the app right when 
		// starting (if you believe this is insane, wait until you submit your app and get  
		// Q/A results).
		if (App.isStopped()) {
			App.logit("App::initalStart early exit (stopped)");
			return; 
		}
		
		
		//
		// Finally we are ready to put our stuff together.  Here follows your init code.
		//
		
		/* TODO: below goes all our fancy app start code */
		
		
		// Make a root-layout which will stretch our content to full size. 
		//
		// Note: In most real apps, using a CardLayout will be the best choice in order to 
		// switch between multiple 'screens', e.g. for being able to switch between a game board, 
		// intro menu and help panel. For this simple demo a BorderLayout will do.
		this.rootContainer.setLayout(new BorderLayout());

		this.gameBoard= new GameBoard();

		// Add game board.  The game bboard will covers as much of the main area as possible  
		// and will have its doLayout() called (the root's BorderLayout does all this) .
		this.rootContainer.add(this.gameBoard, BorderLayout.CENTER); 


		// Request focus to route keyboard events to the board (it has a keyboard listener installed).
		// 
		// Note: Only do this on first start.  On subsequent calls to start() (after screensaver, USB, etc.)
		// the Kindle will remember and refocus the component that had the focus when it went to sleep)
		// - see http://forums.kindlecentral.com/forums/thread.jspa?threadID=535&tstart=0 (item posted Jul 13, 2011 8:36 AM) 
		//  see http://forums.kindlecentral.com/forums/thread.jspa?threadID=889&tstart=0
		
		this.gameBoard.requestFocus(); // this will actually happen asynchoronously 
		
		
		// After this point, this function will not be called again. 
		this.initialStartDone= true;
	
		App.logit("App::initalStart done");
	}
	
	
	
    // ---------------------------------------------------------------------------------------
	//  temporary or final stop. don't change this code unless you use Timers or Threads
    // ---------------------------------------------------------------------------------------
	public void stop()
	{
		App.logit("App::stop");

		// In order to handle stop() correctly, longish operations in threads throughout the program should 
		// call App.isStopped() periodically and exit quicky (the framework system requires all threads to 
		// exit within 5 seconds when stop() is called)
		
		App.setStopped(true);	// keep this outside "synchronized" 
								// (we might even want to abort a start() call in progress)

		
		synchronized (this) {	// avoid having threaded stop() and start() calls execute at the same time (yes, this can happen)
			
	    	App.logit("App::stop syncrhonized");

			// Note: stop() can be called multiple times throughout the lifecycle, i.e. stop() 
	    	// does not mean that the app actually ends.  Stop() is also called before going to 
	    	// screensaver/USB.

	    	// Note: do *not* save the game state in stop().  On USB events the file system may already be 
	    	// invalid. Instead save your stuff in destroy() or during gameplay directly after a user interaction.
	    	// - see http://forums.kindlecentral.com/forums/thread.jspa?threadID=813&tstart=0
	    	// - see http://forums.kindlecentral.com/forums/thread.jspa?threadID=814&tstart=0

			// Note: if you have timers, destroy them here.  Timers need to be stopped/destroyed in 
	    	// the stop() method because otherwise they would continue to run and drain the battery 
	    	// during screensaver (and thus they will need to be recreated on every start() iteration).

			/* TODO: ... tear down timers if you have any... */
		}
		
		App.logit("App::stop done");
	}
	
	
	
    // ---------------------------------------------------------------------------------------
	//  app is almost done with.  your cleanup code goes here.
    // ---------------------------------------------------------------------------------------
	public void destroy()
	{
		App.logit("App::destroy");
		
		// Actual deconstruction. destroy() will be called after stop() is complete and 
		// when all threads have finished.

		
		// Good time to save game state if necessary (see comment in stop())
		/* TODO: ... save game state/options to file ... */
		
		
		//
		// Cleanup and deconstruction 
		//
		
		/* TODO: below goes your app exit code. Clean up as much stuff as you can. */

		// Deconstruct and null out all object pointers (see comment in this.gameBoard.destroy())
		// - see http://forums.kindlecentral.com/forums/thread.jspa?threadID=535&tstart=0 (item Posted: Nov 8, 2010 6:47 PM)

		// Note: do not do this in the stop() call because there it may have been just a screensaver stop() and  
		// the system will expect the exactly same components to remain on screen when it wakes up again
		this.rootContainer.setLayout(null);  // no need to waste time on implicit root-layout calls down from here 
		this.rootContainer.removeAll();
		this.gameBoard.destroy();
		
		
		// Null out pointers
		// - see http://forums.kindlecentral.com/forums/thread.jspa?threadID=535&tstart=0 (item Posted: Nov 8, 2010 6:47 PM)
		this.gameBoard= null;

		
		// Call system garbage collection to release instances where possible. Doesn't hurt and may 
		// help to convince Q/A that we're not wasting memory
		// 	see http://forums.kindlecentral.com/forums/thread.jspa?messageID=851&#851
		System.gc();
		
		App.logit("App::destroy done.\n\nOver and out!");
	}
}  