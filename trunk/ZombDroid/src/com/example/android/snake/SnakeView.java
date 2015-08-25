/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.snake;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * SnakeView: implementation of a simple game of Snake
 * 
 * 
 */
public class SnakeView extends TileView {

    private static final String TAG = "SnakeView";

    /**
     * Current mode of application: READY to run, RUNNING, or you have already
     * lost. static final ints are used instead of an enum for performance
     * reasons.
     */
    private int mMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;
    public static final int WIN = 4;

    /**
     * Current direction the snake is headed.
     */
    private int mDirection = NORTH;
    private int mNextDirection = NORTH;
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int WEST = 4;

    /**
     * Labels for the drawables that will be loaded into the TileView class
     */
    private static final int RED_STAR = 1;
    private static final int YELLOW_STAR = 2;
    private static final int GREEN_STAR = 3;

    /**
     * mScore: used to track the number of apples captured mMoveDelay: number of
     * milliseconds between snake movements. This will decrease as apples are
     * captured.
     */
    private long mScore = 0;
    private long mMoveDelay = 600;
    /**
     * mLastMove: tracks the absolute time when the snake last moved, and is used
     * to determine if a move should be made based on mMoveDelay.
     */
    private long mLastMove;
    
    /**
     * mStatusText: text shows to the user in some run states
     */
    private TextView mStatusText;

    /**
     * mSnakeTrail: a list of Coordinates that make up the snake's body
     * mAppleList: the secret location of the juicy apples the snake craves.
     */
    private ArrayList<Coordinate> mPlayerTrail = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> mZombieList = new ArrayList<Coordinate>();

    /**
     * Everyone needs a little randomness in their life
     */
    private static final Random RNG = new Random();

    /**
     * Create a simple handler that we can use to cause animation to happen.  We
     * set ourselves as a target and we can use the sleep()
     * function to cause an update/invalidate to occur at a later date.
     */
    private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            SnakeView.this.update();
            SnakeView.this.invalidate();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };


    /**
     * Constructs a SnakeView based on inflation from XML
     * 
     * @param context
     * @param attrs
     */
    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSnakeView();
   }

    public SnakeView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	initSnakeView();
    }

    private void initSnakeView() {
        setFocusable(true);

        Resources r = this.getContext().getResources();
        
        resetTiles(4);
        loadTile(RED_STAR, r.getDrawable(R.drawable.redstar));
        loadTile(YELLOW_STAR, r.getDrawable(R.drawable.yellowstar));
        loadTile(GREEN_STAR, r.getDrawable(R.drawable.greenstar));
    	
    }
    

    /**
     * Given a ArrayList of coordinates, we need to flatten them into an array of
     * ints before we can stuff them into a map for flattening and storage.
     * 
     * @param cvec : a ArrayList of Coordinate objects
     * @return : a simple array containing the x/y values of the coordinates
     * as [x1,y1,x2,y2,x3,y3...]
     */
    private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
        int count = cvec.size();
        int[] rawArray = new int[count * 2];
        for (int index = 0; index < count; index++) {
            Coordinate c = cvec.get(index);
            rawArray[2 * index] = c.x;
            rawArray[2 * index + 1] = c.y;
        }
        return rawArray;
    }

    /**
     * Save game state so that the user does not lose anything
     * if the game process is killed while we are in the 
     * background.
     * 
     * @return a Bundle with this view's state
     */
    public Bundle saveState() {
        Bundle map = new Bundle();

        map.putIntArray("mAppleList", coordArrayListToArray(mZombieList));
        map.putInt("mDirection", Integer.valueOf(mDirection));
        map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
        map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
        map.putLong("mScore", Long.valueOf(mScore));
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mPlayerTrail));

        return map;
    }

    /**
     * Given a flattened array of ordinate pairs, we reconstitute them into a
     * ArrayList of Coordinate objects
     * 
     * @param rawArray : [x1,y1,x2,y2,...]
     * @return a ArrayList of Coordinates
     */
    private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
        ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();

        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 2) {
            Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
            coordArrayList.add(c);
        }
        return coordArrayList;
    }

    /**
     * Restore game state if our process is being relaunched
     * 
     * @param icicle a Bundle containing the game state
     */
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);

        mZombieList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mDirection = icicle.getInt("mDirection");
        mNextDirection = icicle.getInt("mNextDirection");
        mMoveDelay = icicle.getLong("mMoveDelay");
        mScore = icicle.getLong("mScore");
        mPlayerTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
    }

    /*
     * handles key events in the game. Update the direction our snake is traveling
     * based on the DPAD. Ignore events that would cause the snake to immediately
     * turn back on itself.
     * 
     * (non-Javadoc)
     * 
     * @see android.view.View#onKeyDown(int, android.os.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mMode == READY | mMode == LOSE | mMode == WIN) {
                /*
                 * At the beginning of the game, or the end of a previous one,
                 * we should start a new game.
                 */
                initNewGame();
                setMode(RUNNING);
                update();
                return (true);
            }

            if (mMode == PAUSE) {
                /*
                 * If the game is merely paused, we should just continue where
                 * we left off.
                 */
                setMode(RUNNING);
                update();
                return (true);
            }

            
                mNextDirection = NORTH;
            
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            
                mNextDirection = SOUTH;
            
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            
                mNextDirection = WEST;
            
            return (true);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            
                mNextDirection = EAST;
            
            return (true);
        }

        return super.onKeyDown(keyCode, msg);
    }

    /**
     * Sets the TextView that will be used to give information (such as "Game
     * Over" to the user.
     * 
     * @param newView
     */
    public void setTextView(TextView newView) {
        mStatusText = newView;
    }

    private Human Player;
    public Human getPlayer() {
		return Player;
	}

	public void setPlayer(Human player) {
		Player = player;
	}

	private void initNewGame() {
	    mPlayerTrail.clear();
	    mZombieList.clear();
	    Player = new Human();
	    Weapon Pistol = new Weapon();
	    Pistol.setPower(80);
	    Pistol.setRange(8);
	    Pistol.setAmmo(8);
	    Pistol.setAmmoInClip(2);
	    Pistol.setClipSize(5);
	    Pistol.setTurnsUntilNextShot(0);
	    Pistol.setFireRatePerTurn(10);
	    Pistol.setAccuracy(80);
	    Player.setSelectedWeapon(Pistol);
	    
	    
	    mPlayerTrail.add(new Coordinate(12, mYTileCount - 1));
	    
	    
	    mNextDirection = NORTH;
	    
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();
	    addRandomZombie();

	    
	    mMoveDelay = 300;
	    mScore = 0;
	}

	/**
     * Updates the current mode of the application (RUNNING or PAUSED or the like)
     * as well as sets the visibility of textview for notification
     * 
     * @param newMode
     */
    public void setMode(int newMode) {
        int oldMode = mMode;
        mMode = newMode;

        if (newMode == RUNNING & oldMode != RUNNING) {
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            str = res.getText(R.string.mode_pause);
        }
        if (newMode == READY) {
            str = res.getText(R.string.mode_ready);
        }
        if (newMode == LOSE) {
        	//mAppleList.clear();
            str = res.getString(R.string.mode_lose_prefix) + mScore
                  + res.getString(R.string.mode_lose_suffix);
            
        }
        if (newMode == WIN) {
        	mZombieList.clear();
            str = res.getString(R.string.mode_win_prefix);
        }

        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }

    /**
     * Selects a random location within the garden that is not currently covered
     * by the snake. Currently _could_ go into an infinite loop if the snake
     * currently fills the garden, but we'll leave discovery of this prize to a
     * truly excellent snake-player.
     * 
     */
    private void addRandomZombie() {
        Coordinate newCoord = null;
        boolean found = false;
        while (!found) {
            // Choose a new location for our apple
            int newX = 1 + RNG.nextInt(mXTileCount - 2);
            int newY = 1 + RNG.nextInt(mYTileCount - 7);
            newCoord = new Coordinate(newX, newY);

            // Make sure it's not already under the snake
            boolean collision = false;
            int snakelength = mPlayerTrail.size();
            for (int index = 0; index < snakelength; index++) {
                if (mPlayerTrail.get(index).equals(newCoord)) {
                    collision = true;
                }
            }
            // if we're here and there's been no collision, then we have
            // a good location for an apple. Otherwise, we'll circle back
            // and try again
            found = !collision;
        }
        if (newCoord == null) {
            Log.e(TAG, "Somehow ended up with a null newCoord!");
        }
        mZombieList.add(newCoord);
    }


    /**
     * Handles the basic update loop, checking to see if we are in the running
     * state, determining if a move should be made, updating the snake's location.
     */
    public void update() {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateWalls();
                updateZombies();
                updatePlayer();
                
                mLastMove = now;
            }
            mRedrawHandler.sleep(mMoveDelay);
        }

    }

    /**
     * Draws some walls.
     * 
     */
    private void updateWalls() {
        for (int x = 0; x < mXTileCount; x++) {
            setTile(GREEN_STAR, x, 0);
            setTile(GREEN_STAR, x, mYTileCount - 1);
        }
        for (int y = 1; y < mYTileCount - 1; y++) {
            setTile(GREEN_STAR, 0, y);
            setTile(GREEN_STAR, mXTileCount - 1, y);
        }
    }

    /**
     * Draws some apples.
     * 
     */
    private void updateZombies() {
        for (Coordinate zombieCoordinate : mZombieList) 
        {
        	Coordinate head = mPlayerTrail.get(0);
        	double xDistance = zombieCoordinate.x - head.x;
        	double yDistance = zombieCoordinate.y - head.y;
        	if((-4 < xDistance && xDistance < 4) && (-4 < yDistance && yDistance < 4))
        	{
        	boolean IsLeft = false;
        	if(xDistance < 0)
        	{
        		IsLeft = true;
        		xDistance = 0 - xDistance;
        	}
        	boolean IsAbove = false;
        	if(yDistance < 0)
        	{
        		IsAbove = true;
        		yDistance = 0 - yDistance;
        	}
        	if(xDistance > yDistance)
        	{
            	if(!IsLeft)
            	{
            		zombieCoordinate.x = zombieCoordinate.x - 1;
            	}
            	else
            	{
            		zombieCoordinate.x = zombieCoordinate.x + 1;
            	}
        	}
        	else
        	{
            	if(!IsAbove)
            	{
            		zombieCoordinate.y = zombieCoordinate.y - 1;
            	}
            	else
            	{
            		zombieCoordinate.y = zombieCoordinate.y + 1;
            	}
        	}
        	}
        	//move in direction the player is further from
            setTile(RED_STAR, zombieCoordinate.x, zombieCoordinate.y);
        }
    }

    /**
     * Figure out which way the snake is going, see if he's run into anything (the
     * walls, himself, or an apple). If he's not going to die, we then add to the
     * front and subtract from the rear in order to simulate motion. If we want to
     * grow him, we don't subtract from the rear.
     * 
     */
    private void updatePlayer() {

        // grab the Player
        Coordinate head = mPlayerTrail.get(0);
        Coordinate newHead = new Coordinate(1, 1);
        

        mDirection = mNextDirection;

        switch (mDirection) {
        case EAST: {
            newHead = new Coordinate(head.x + 1, head.y);
            break;
        }
        case WEST: {
            newHead = new Coordinate(head.x - 1, head.y);
            break;
        }
        case NORTH: {
            newHead = new Coordinate(head.x, head.y - 1);
            break;
        }
        case SOUTH: {
            newHead = new Coordinate(head.x, head.y + 1);
            break;
        }
        }

        //if the Player reaches the top of the screen
        if(newHead.y < 1)
        {
        	setMode(WIN);
        }

        // Collision detection
        // For now we have a 1-square wall around the entire arena
        if (((head.x < 2)&& mDirection == WEST) ||
    		((head.y < 2)&& mDirection == NORTH)  ||
    		((head.x > mXTileCount - 3)&& mDirection == EAST) ||
    		((head.y > mYTileCount - 3)&& mDirection == SOUTH) ) 
        {  
        	//player is at the edge of the screen trying to move into the fence so dont move them
        }
        else
        {
            //Move player
            mPlayerTrail.add(0, newHead);
            mPlayerTrail.remove(mPlayerTrail.size() - 1);
        }
        
        int index = 0;
        for (Coordinate c : mPlayerTrail) {
            if (index == 0) {
                setTile(YELLOW_STAR, c.x, c.y);
            } else {
                setTile(RED_STAR, c.x, c.y);
            }
            index++;
        }

        // Check For collisions with zombies
        int zombiecount = mZombieList.size();
        for (int Zombieindex = 0; Zombieindex < zombiecount; Zombieindex++) {
            Coordinate c = mZombieList.get(Zombieindex);
            if (c.equals(newHead)) {
                setMode(LOSE);
            }
        }
        
        fireWeapon(head, zombiecount);

    }

	private void fireWeapon(Coordinate head, int zombiecount) 
	{
		//Fire Weapon
		if(Player.getSelectedWeapon().getTurnsUntilNextShot() == 0)
		{
			if(Player.getSelectedWeapon().getAmmo() > 0)
			{
				if(Player.getSelectedWeapon().Fire())
				{
			        for (int Zombieindex = 0; Zombieindex < zombiecount; Zombieindex++) 
			        {
			        	Coordinate zombieCoordinate = mZombieList.get(Zombieindex);
			        	
				    	double xDistance = zombieCoordinate.x - head.x;
				    	double yDistance = zombieCoordinate.y - head.y;
				    	if((-Player.getSelectedWeapon().getRange() < xDistance && xDistance < Player.getSelectedWeapon().getRange()) && (-Player.getSelectedWeapon().getRange() < yDistance && yDistance < Player.getSelectedWeapon().getRange()))
				    	{
				    		zombieCoordinate.x = 0;
							zombieCoordinate.y = 0;
							break;
				    	}
			        }
				}
			}
			else
			{
				Player.getSelectedWeapon().Reload();
				//TODO Reload Weapon
			}
		}
		else
		{
			Player.getSelectedWeapon().setTurnsUntilNextShot(Player.getSelectedWeapon().getTurnsUntilNextShot() - 1);
		}
	}

    /**
     * Simple class containing two integer values and a comparison function.
     * There's probably something I should use instead, but this was quick and
     * easy to build.
     * 
     */
    private class Coordinate {
        public int x;
        public int y;

        public Coordinate(int newX, int newY) {
            x = newX;
            y = newY;
        }

        public boolean equals(Coordinate other) {
            if (x == other.x && y == other.y) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Coordinate: [" + x + "," + y + "]";
        }
    }
    
}
