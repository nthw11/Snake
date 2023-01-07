package com.nthw.snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

class SnakeGame extends SurfaceView implements Runnable{
    // Objects for the game loop/thread
    private Thread mThread = null;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;
    // for playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;
    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;
    // How many points does the player have
    private int mScore;
    // Objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;
    // A snake ssss
    private Snake mSnake;
    // And an apple
    private Apple mApple;

    // This is the constructor method that gets called
// from SnakeActivity
    public SnakeGame(Context context, Point size) {
        super(context);
// Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
// How many blocks of the same size will fit into the height
        mNumBlocksHigh = size.y / blockSize;
// Initialize the SoundPool
        if (Build.VERSION.SDK_INT>=
                Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes
                                    .CONTENT_TYPE_SONIFICATION)
                            .build();
            mSP = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }
        else {
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
// Prepare the sounds in memory
            descriptor = assetManager.openFd(
                    "get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd(
                    "snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);
        } catch (IOException e) {
// Error
        }
        // Initialize the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
// Call the constructors of our two game objects
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);

        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
    }

    // called to start a new game
    public void newGame(){
         // reset the snake
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);

        // get the apple ready for dinner
        mApple.spawn();

        // reset the mScore
        mScore = 0;

        // set up mNextFrameTime so an update can be triggered
        mNextFrameTime = System.currentTimeMillis();
    }

    // handles the game loop
    @Override
    public void run(){
        while (mPlaying){
            if(!mPaused){
                // update 10 times per second
                if(updateRequired()){
                    update();
                }
            }
            draw();
        }
    }

    // check to see if it is time for an update
    public boolean updateRequired(){
        // run at 10 frames per second
        final long TARGET_FPS = 10;
        // there are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        // are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){
            // tenth of a second has passed

            // setup when the next update will be triggered
            mNextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / TARGET_FPS;

            // return true so that the update() and draw() methods are executed
            return true;
        }
        return false;
    }
    // update all the game objects
    public void update(){

        // move the snake
        mSnake.move();

        // did the head of the snake eat the apple?
        if(mSnake.checkDinner(mApple.getLocation())){
            mApple.spawn();

        // add to mScore
            mScore = mScore + 1;

        // play a sound
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);

        }

        // did the snake die?
        if(mSnake.detectDeath()){
            // pause the game ready to start again
            mSP.play(mCrashID, 1, 1, 0, 0, 1);

            mPaused = true;
        }

    }

    // do all the drawing
    public void draw(){
        // get a lock on the mCanvas
        if(mSurfaceHolder.getSurface().isValid()){
            mCanvas = mSurfaceHolder.lockCanvas();

            // fill the screen with a color
            mCanvas.drawColor(Color.argb(255, 26, 128, 182));

            // set the size and color of the mPaint for the text
            mPaint.setColor(Color.argb(255, 255 , 255 , 255));
            mPaint.setTextSize(120);

            // draw the score
            mCanvas.drawText("" + mScore, 20, 120,mPaint);

            // draw the apple and the snake
            mApple.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);

            // draw some text while paused
            if(mPaused) {

                // set the size and color of mPaint for the text
                mPaint.setColor(Color.argb(255, 255, 255,
                        255));
                mPaint.setTextSize(250);
// Draw the message
// We will give this an international upgrade soon
//                mCanvas.drawText("Tap To Play!", 200, 700, mPaint);
                mCanvas.drawText(getResources().
                                getString(R.string.tap_to_play),
                        200, 700, mPaint);
            }

            // unlock the canvas to show graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch (motionEvent.getAction() &MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
                if(mPaused){
                    mPaused = false;
                    newGame();

                    // don't want to process snake direction for this tap
                    return true;
                }
                // let the Snake class handle the input
                mSnake.switchHeading(motionEvent);
                break;

            default:
                break;
        }
        return true;
    }

    // stop the thread
    public void pause(){
        mPlaying = false;
        try{
            mThread.join();
        } catch (InterruptedException e){
            // Error
        }
    }

    // start the thread
    public void resume(){
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}
