
/////////////////////////////////////////////////////////////////////////////////////////
//
//  Kindle-ConceptDemo
// 
//  Source code (C) Copyright 2010 Markus Schmidt (m.schmidt@emtec.com)
//  Published under MIT License (http://en.wikipedia.org/wiki/MIT_License)  
//
/////////////////////////////////////////////////////////////////////////////////////////


package com.nowhere.sample;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.amazon.kindle.kindlet.event.KindleKeyCodes;
import com.amazon.kindle.kindlet.ui.KImage;
import com.amazon.kindle.kindlet.ui.KLabel;
import com.amazon.kindle.kindlet.ui.KindletUIResources;
import com.amazon.kindle.kindlet.ui.KindletUIResources.KFontStyle;




/////////////////////////////////////////////////////////////////////////////////////////
//
//  A class to draw a board with a checker and handle keyboard events
//
//  (Two additional helper classes near the bottom)  
//
//
/////////////////////////////////////////////////////////////////////////////////////////
public class GameBoard extends Container
{
	private static final long serialVersionUID = 1L;


	//
	// board members
	// 
	
	// general images
	Image backgroundImage;
	Image elementImage;
	
	// background
	KImageSnoop backgroundPane;	 // KImageSnoop class definition at bottom of this file
	
	// game elements 
	GameTile checkerPane;
	
	// the bottom label 
	KLabel someLabel;
	
	// other stuff
	Dimension layoutSize;
	KeyAdapter keyListener2;
	
	
	//
	// constructor
	// 
	GameBoard()
	{
		App.logit("GameBoard::Board");
		
		// No layout manager, we move stuff around ourselves 
		// (this is essential for the way we're playing with components) 
		this.setLayout(null);// Important: if you do this, you will need to override "void doLayout() and not call super.doLayout()"

		// Load base images (we will use them later to make scaled versions).
		// Simply drag/drop these files into the same eclipse package/folder where the App.java file resides 
		Toolkit tk= Toolkit.getDefaultToolkit();
		this.backgroundImage= tk.createImage(getClass().getResource("background.gif")); // any 600x800 image
		this.elementImage= tk.createImage(getClass().getResource("checker.png"));	// a circle in a 40x40 with transparent bg

		// for now create empty images (we'll load them when we know which size we need)
		this.backgroundPane= new KImageSnoop(null, "background");
		this.checkerPane= new GameTile(null);
		this.someLabel= new KLabel("Press u/d/r/l/c to move or use the 5-way.", KLabel.CENTER);
		
		// just for the fun of it, use a different font for the label
		Font ff= KindletUIResources.getInstance().getFont(KindletUIResources.KFontFamilyName.MONOSPACE, 21, KFontStyle.PLAIN, false);
		this.someLabel.setFont(ff);

		// Add all th stuff in z-order: top items first, to bottom items last 
		// (background naturally needs to go in last or it will cover everything else)
		this.add(this.checkerPane);
		this.add(this.someLabel);  
		this.add(this.backgroundPane);

		// We catch all keys and have no focus on any of the components 
		// (to avoid surprises, all components should have setFocusable(false))
		this.checkerPane.setFocusable(false);
		this.backgroundPane.setFocusable(false);
		
		// Register (board Panel) for keyboard (focusable with KeyAdapter)
		this.setFocusable(true);  // just doing this for clarification, it's true by default anyway
		this.registerAsKeyboardListener();
		
		App.logit("GameBoard::Board done");
	}

	
	//
	// destructor (Java doesn't require destructors, so this is a homebrew version 
	// that needs be explicitely called from Kindlet destroy())
	// 
	void destroy()
	{
		App.logit("GameBoard::destroy");

		// Deconstruct and aid in garbage collection by cleaning up all references to other objects
		// - see http://forums.kindlecentral.com/forums/thread.jspa?threadID=535&tstart=0 (item Posted: Nov 8, 2010 6:47 PM)

		if (this.keyListener2!=null) {
			this.removeKeyListener(this.keyListener2);
			this.keyListener2= null;
		}

		
		// Remove all childs
		this.removeAll();
		

		// More deconstruct (aid garbage collection by cleaning up all references to other objects)
		this.backgroundPane.setImage(null);
		this.backgroundPane= null;
		
		this.checkerPane.destroy();
		this.checkerPane= null;

		this.someLabel= null;

		
		// Make sure you flush() and null images if you don't need them
		this.backgroundImage.flush();
		this.backgroundImage= null;
		
		this.elementImage.flush();
		this.elementImage= null;

		App.logit("GameBoard::destroy done");
	}
	

	
	//
	// this will be called whenever the framework thinks we might need to layout  
	// our components (prepare to be called more often than you'd expect) 
	//
	public void doLayout()
	{
		App.logit("GameBoard::doLayout for " + this.getSize());
		
		// Note: important!! do *not* call super.doLayout() here
		// 	see http://forums.kindlecentral.com/forums/thread.jspa?threadID=535&tstart=0 (item posted Mar 27, 2011 12:16 PM)
		
		/* !!! nope, don't call super.doLayout() !!! */
		
		
		// get size for arrangement
		final Dimension thissize= this.getSize();
		
		// this check is necessary (doLayout will be called often!)
		if (this.layoutSize!=null && this.layoutSize.equals(thissize)) {
			App.logit("GameBoard::doLayout early exit (same size)");
			return;
		}
		
		
		// Size was actually changed (first sizing or even orientation could have 
		// changed), thus arrange our components
		this.layoutSize= thissize;
		
		Image image= null;
		
		// Make the background cover the whole size and request get a prescaled image to fit 
		// (the prescale will speed up painting later) 
		this.backgroundPane.setBounds(0,0, thissize.width,thissize.height);
		image= this.backgroundImage.getScaledInstance(thissize.width, thissize.height, Image.SCALE_FAST);
		GameBoard.waitForImage(image, this);  // trigger loading and make sure we have the whole thing
		this.backgroundPane.setImage(image);

		// For the sample we use a fixed size for the checker and just start in the middle. 
		// (normally this would have to scale somehow to fit the background scaling)
		// alternately don't set an image at all ... the GameTile will then just draw a circle
		int checkersize= 50;
		this.checkerPane.setBounds(thissize.width/2-checkersize/2,thissize.height/2-checkersize/2, checkersize,checkersize);
		image= this.elementImage.getScaledInstance(checkersize, checkersize, Image.SCALE_SMOOTH);
		GameBoard.waitForImage(image, this);  // trigger loading and make sure we have the whole thing
		this.checkerPane.setImage(image);

		// Arrange the label at the bottom with its preferred height (pref. height will change depending on font) 
		Dimension sz= this.someLabel.getPreferredSize();
		this.someLabel.setBounds(0,thissize.height-sz.height, thissize.width,sz.height);
		
		App.logit("GameBoard::doLayout done");
	}
	
	
	
	//
	// This is just a simple paint routine to show how user painting is done.
	// 
	// Note: Technically we could paint anything there. In fact we could do away with the KIimage  
	// (background) and GameTile (checker) child components and instead paint the whole board and all 
	// into the graphics context here via g2d.drawImage() calls.
	//
	public void paint(Graphics graphics)
	{
		App.logit("GameBoard::paint in clipBounds " + graphics.getClipBounds());
		
		super.paint(graphics);  // this will paint all the children (i.e. the background-kimage, the checker-kimage and the label)

		
		Graphics2D g2d= (Graphics2D)graphics;	// we prefer G2D because it offers a few more features
		g2d.setColor(Color.BLACK);

		// just as a sample, draw a bit of decoration along the upper/lower borders 
		// (this drawing goes on top of everything else).
		Dimension d= this.getSize();
		for (int i= 0; i<d.width-10; i+=15) {
			g2d.fillOval(i+2,10, 10,10);
		}
		Rectangle rl= this.someLabel.getBounds();
		g2d.setColor(Color.WHITE);
		for (int i= 0; i<d.width-10; i+=15) {
			g2d.fillRect(i+2,rl.y-15, 10,10);
		}
	}

	
	
	//
	// move the element around on the board in response to user interaction
	// (directions 'r'(right), 'l'(left), etc..)
	//
	public void moveElement(final char direction)
	{
		App.logit("GameBoard::moveElement " + direction);

		// moving a lightweight component around on the board is a simple way of leaving it 
		// to the Kindle framework to figure out which areas need to redraw.  
		//
		// Below, when moving the checker, the Kindle will send a redraw to the background 
		// (using a clipping area of where the checker was). It will then do the same for 
		// the new position and will then paint the checker on top. (look for KImageSnoop 
		// messages in the logging output to see how it works.  Also don't miss the 
		// --redrawhightlights option in the simulator.)

		final Rectangle elbo= this.checkerPane.getBounds();
		final Dimension thissize= this.getSize();
		
		switch (direction) {
			case 'C': 
				this.checkerPane.setLocation(thissize.width/2-elbo.width/2, thissize.height/2-elbo.height/2); 
				break;
				
			case 'U': 
				this.checkerPane.setLocation(elbo.x, elbo.y-elbo.height); 
				break;
				
			case 'D': 
				this.checkerPane.setLocation(elbo.x, elbo.y+elbo.height); 
				break;
				
			case 'L': 
				this.checkerPane.setLocation(elbo.x-elbo.width, elbo.y); 
				break;
				
			case 'R': 
				this.checkerPane.setLocation(elbo.x+elbo.width, elbo.y); 
				break;
		}
		
		App.logit("GameBoard::moveElement done (from= " + elbo + " to " + this.checkerPane.getBounds() + ")");
	}
	
	
	//
	// keyboard handler
	// 
	private void registerAsKeyboardListener()
	{
		// register to receive keyboard events
		this.keyListener2=
				new KeyAdapter() 
					{
						public void keyPressed(KeyEvent event) 
						{ 
							GameBoard.this.onKeyboard(event); 
						}
					};
		
		this.addKeyListener(this.keyListener2);
		
		
		// Make keyboard handler receive 5-way keycodes 
		// (this basically kills any kind of component focusing) 
		this.setFocusTraversalKeysEnabled(false);  
	}


	//
	// translate reiceived keyboard events into suitable moveElements calls
	// 
	private void onKeyboard(KeyEvent event)
	{
		int keycode= event.getKeyCode();
		
		App.logit("GameBoard::onKeyboard" + event + " *************");

		// 
		// deal with action keys (here we just turn 5-ways into actual letters) 
		// 
		if (event.isActionKey()) {

            if (keycode==KindleKeyCodes.VK_BACK) {
            	// consume event ... these days VK_BACK should not exit the app 
            	keycode= 0; 
				event.consume();
            }
            else {
            	// convert 5-way events to letters (these will be handled further down below)
				switch (keycode) {
					case KindleKeyCodes.VK_FIVE_WAY_UP: keycode= 'U'; break;
					case KindleKeyCodes.VK_FIVE_WAY_DOWN: keycode= 'D'; break;
					case KindleKeyCodes.VK_FIVE_WAY_RIGHT: keycode= 'R'; break;
					case KindleKeyCodes.VK_FIVE_WAY_LEFT: keycode= 'L'; break;
					default: keycode= 0; break; // ignore
	            }
				event.consume();
            }
        }
		
		
		//
		// handle anything that was not consumed above (normal lettersmovement keystrokes) 
		//
		switch (keycode) {
			case 'U':
			case 'D':
			case 'L':
			case 'R':
				this.moveElement((char)keycode);
				break;
				
			case 0:
				// this was used above to do nothing
				break;
		}
		
		App.logit("GameBoard::onKeyboard done");
	}

	

	//
	// generic helper function to wrap a waitForImage call
	// 
	public static void waitForImage(Image image, Component component)
	{
		App.logit("GameBoard::waitforimage");
		
		java.awt.MediaTracker media_tracker = new java.awt.MediaTracker(component);

		image.getWidth(null);  // this will trigger an image loader

		int id = 1234; // just an arbitrary number as id 
		media_tracker.addImage(image,id);
		
		// try to wait for image to be loaded to avoid artefacts from 
		// drawing partially loaded bitmaps.
		try {
		    media_tracker.waitForID(id);
		}
		catch(Exception e) {
			App.logit("GameBoard::waitforimage Image loading interrupted : " + e);
		}
		
		App.logit("GameBoard::waitforimage done");
	}
	
	
	

	/////////////////////////////////////////////////////////////////////////////////////////
	//
	//  Helper class for a game board element.
	// 
	//  The checker comes in two flavors, either with self-drawing an image or with  
	//  using draw primitives, depending on if an image is passed on the constructor or 
	//  if null is passed.
	//
	/////////////////////////////////////////////////////////////////////////////////////////
	
	private class GameTile extends Container
	{
		private static final long serialVersionUID= 1L;
		
		Image image;
		
		public GameTile(Image image)
		{
			this.setImage(image);
		}

		public void destroy()
		{
			if (this.image!=null) {
				this.image.flush();
				this.image= null;
			}
		}
		
		
		public void setImage(Image image)
		{
			if (this.image!=null) {
				this.image.flush();
				this.image= null;
			}
			
			this.image= image;
		}
		
		
		public void paint(Graphics graphics)
		{
			App.logit("GameTile::paint in clipBounds " + graphics.getClipBounds());

			super.paint(graphics);  // this will paint all the children (if any)
			
			Graphics2D g2d= (Graphics2D)graphics;	// we prefer G2D because it offers a few more features
			
			// if we have an image draw it, otherwise just use g2d to draw a black circle
			if (this.image!=null) {
				// alternately we could draw the image here
				g2d.drawImage(this.image, 0,0, null);
			}
			else {
				Dimension d= this.getSize();
				g2d.setColor(Color.BLACK);
				g2d.fillOval(2,2,d.width-4-1,d.height-4-1);
			}
		}
	}

	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	//  Helper class like KImage.  
	// 
	//  Purpose for the override is to show where and when screen  updates redraw
	//
	/////////////////////////////////////////////////////////////////////////////////////////

	private class KImageSnoop extends KImage
	{
		private static final long serialVersionUID = 1L;

		String nameToLog;
		
		
		public KImageSnoop(Image image, String name_to_log) 
		{
			super(image);
			this.nameToLog= name_to_log;
		}

		
		public void paint(Graphics graphics) 
		{
			App.logit("KImageSnoop::paint for " + this.nameToLog + " in clipBounds " + graphics.getClipBounds());
			super.paint(graphics);
		}
	}
}
	
	
