package com.soldiergame.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

import sun.rmi.runtime.Log;

public class SoldierGame extends ApplicationAdapter {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Rectangle soldier;

	private Texture soldierSheet;
	private ArrayList<Animation<TextureRegion>> walkAnimation;
	private ArrayList<TextureRegion> stoppedAnimation;
	private float stateTime;

	private static final int SHEET_COLS = 9, SHEET_ROWS = 8;

	// order corresponding to sheet row
	private static final int WALK_LEFT_UP = 0;
	private static final int WALK_UP = 1;
	private static final int WALK_RIGHT_UP = 2;
	private static final int WALK_RIGHT = 3;
	private static final int WALK_LEFT = 4;
	private static final int WALK_LEFT_DOWN = 5;
	private static final int WALK_DOWN = 6;
	private static final int WALK_RIGHT_DOWN = 7;

	private static final int WALK_STOPPED = 10;

	private int lastDirection;

	// This version only controls keyboard movement in 8 directions
	// Next version will allow mobile players move any direction with joystick

	@Override
	public void create () {
		// Init camera and batch
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		// Get animation sheet
		soldierSheet = new Texture(Gdx.files.internal("soldier.png"));

		// Creating temp matrix with all the animations
		TextureRegion[][] tmp = TextureRegion.split(soldierSheet,
				soldierSheet.getWidth() / SHEET_COLS,
				soldierSheet.getHeight() / SHEET_ROWS);

		// Creating temp walk matrix (all columns except last, which is idle image)
		TextureRegion[][] walk = new TextureRegion[SHEET_ROWS][SHEET_COLS-1];

		// Init array of texture regions that will contain all the idles
		stoppedAnimation = new ArrayList<TextureRegion>();

		// Iterating tmp matrix:
		// --> last column of each row is added to stoppedAnimation
		// --> The others columns are inserted into walk matrix, but with inverted j because animation
		//		order is inverted at the sheet.
		int index = 0;
		for (int i = 0; i < SHEET_ROWS; i++) {

			for (int j = 0; j < SHEET_COLS; j++) {
				if (j == SHEET_COLS-1)
				{
					stoppedAnimation.add(tmp[i][j]);
				}
				else
				{
					int newJ = (SHEET_COLS-2) - j;
					walk[index][newJ] = tmp[i][j];
				}
			}
			index++;
		}

		// Creating arraylist of Animations (each animation object 'equals' each row of walk matrix)
		walkAnimation = new ArrayList<Animation<TextureRegion>>();
		for (int i = 0; i < walk.length; i++)
		{
			float frameDuration = 0.1f;
			//if (i == WALK_UP || i == WALK_DOWN)
			//	frameDuration = 0.12f;
			walkAnimation.add(new Animation<TextureRegion>(frameDuration, walk[i]));
		}

		// Init soldier rectangle with dimensions and coords
		soldier = new Rectangle();
		soldier.width = soldierSheet.getWidth() / SHEET_COLS;
		soldier.height = soldierSheet.getHeight() / SHEET_ROWS;
		soldier.x = 800 / 2;
		soldier.y = 20;

		// setting init direction
		lastDirection = WALK_UP;

		// init stateTime
		stateTime = 0f;
	}

	@Override
	public void render () {
		// Setting color background
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// update camera and batch
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		// update stateTime
		stateTime += Gdx.graphics.getDeltaTime();

		TextureRegion currentFrame = null;
		int direction;
		float delta = Gdx.graphics.getDeltaTime();

		// Get keyboard arrows pressed info
		boolean pressedLeft = Gdx.input.isKeyPressed(Input.Keys.LEFT);
		boolean pressedUp = Gdx.input.isKeyPressed(Input.Keys.UP);
		boolean pressedRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		boolean pressedDown = Gdx.input.isKeyPressed(Input.Keys.DOWN);

		int countPressed = 0;
		countPressed = pressedLeft ? countPressed+=1 : countPressed;
		countPressed = pressedUp ? countPressed+=1 : countPressed;
		countPressed = pressedRight ? countPressed+=1 : countPressed;
		countPressed = pressedDown ? countPressed+=1 : countPressed;

		// Controls the movement (direction will be used for animation array index)
		if (countPressed <= 2)
		{
			if(pressedLeft) {
				// this if block controls --> left, left-right(stopped), leftUp and leftDown
				if (pressedRight)
					direction = WALK_STOPPED;
				else if (pressedDown)
				{
					soldier.x -= 100 * delta;
					soldier.y -= 100 * delta;
					direction = WALK_LEFT_DOWN;
				}
				else if (pressedUp)
				{
					soldier.x -= 100 * delta;
					soldier.y += 100 * delta;
					direction = WALK_LEFT_UP;
				}
				else
				{
					soldier.x -= 150 * delta;
					direction = WALK_LEFT;
				}
			}
			else if(pressedRight) {
				// this if block controls --> right, rightDown and rightUp
				if (pressedDown)
				{
					soldier.x += 100 * delta;
					soldier.y -= 100 * delta;
					direction = WALK_RIGHT_DOWN;
				}
				else if (pressedUp)
				{
					soldier.x += 100 * delta;
					soldier.y += 100 * delta;
					direction = WALK_RIGHT_UP;
				}
				else
				{
					soldier.x += 150 * delta;
					direction = WALK_RIGHT;
				}
			}
			else if(pressedUp) {
				// this if block controls --> up, upDown
				if (pressedDown)
				{
					direction = WALK_STOPPED;
				}
				else
				{
					soldier.y += 150 * delta;
					direction = WALK_UP;
				}
			}
			else if(pressedDown) {
				// this if block controls --> down
				direction = WALK_DOWN;
				soldier.y -= 150 * delta;
			}
			else // no pressed
				direction = WALK_STOPPED;

			// Controls windows limit
			if(soldier.x < 0) soldier.x = 0;
			if(soldier.x > 800 - soldier.width) soldier.x = 800 - soldier.width;
			if(soldier.y < 0) soldier.y = 0;
			if(soldier.y > 480 - soldier.height) soldier.y = 480 - soldier.height;

			// Set current frame, if its in movement gets the animation by direction and the frame by the stateTime
			// If its stopped get the idle by the direction
			if (direction != WALK_STOPPED)
			{
				currentFrame = walkAnimation.get(direction).getKeyFrame(stateTime, true);
				lastDirection = direction;
			}
			else
				currentFrame = stoppedAnimation.get(lastDirection);


		}
		else // If more than 3 arrows pressed just stopped
		{
			currentFrame = stoppedAnimation.get(lastDirection);
		}

		// draws the result
		batch.begin();
		batch.draw(currentFrame, soldier.x, soldier.y);
		batch.end();


	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
