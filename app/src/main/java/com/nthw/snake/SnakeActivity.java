package com.nthw.snake;

//import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;

public class SnakeActivity extends Activity {

    // declare an instance of SnakeGame
    SnakeGame mSnakeGame;

    // Set the game up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        // initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // create a new instance of the SnakeGame class
        mSnakeGame = new SnakeGame(this, size);

        // make snakeGame the view of the Activity
        setContentView(mSnakeGame);
//        setContentView(R.layout.activity_main);

    }
    // start the thread in snakeGame
    @Override
    protected void onResume(){
        super.onResume();
        mSnakeGame.resume();
    }

    // stop the thread in snakeGame
    @Override
    protected void onPause(){
        super.onPause();
        mSnakeGame.pause();
    }
}