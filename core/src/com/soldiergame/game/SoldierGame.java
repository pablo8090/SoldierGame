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
import com.badlogic.gdx.math.Vector3;

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

	private static final int WIDTH = 800;
	private static final int HEIGHT = 480 ;

	private int lastDirection;

	// Mobile joystick
	private static final int CENTER_JOYSTICK_X = WIDTH / 2;
	private static final int CENTER_JOYSTICK_Y = HEIGHT / 2;
	private static final int JOYSTICK_DIAMETER = 76;
	private static final int JOYSTICK_RADIUS = JOYSTICK_DIAMETER / 2;

	private static final int VELOCITY = 75; // PIXELS PER SECOND





	// This version only controls keyboard movement in 8 directions
	// Next version will allow mobile players move any direction with joystick

	@Override
	public void create () {
		// Init camera and batch
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, WIDTH, HEIGHT);

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
					// -1 because last column is another animation
					// and -1 because sheet_cols is index based 1
					// -j for invert
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
		int direction = -1;
		float delta = Gdx.graphics.getDeltaTime();

		// Mobile joystick
		boolean screenStillsTouched;
		Vector3 touchPos = null;
		if(Gdx.input.isTouched()) {
			screenStillsTouched = true;
			touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
		}
		else
		{
			screenStillsTouched = false;
			direction = WALK_STOPPED;
		}

		if (screenStillsTouched)
		{

			// Vector reference, related.
			Vector3 joyReference = new Vector3();
			joyReference.x = 0;
			joyReference.y = JOYSTICK_RADIUS;



			// Make touchpos related
			Vector3 touchPosRelated = vectorRelatedJoystick(touchPos);

			// Gets Alpha --> angle between reference vector and touchpos vector
			double cosAlpha;

			double escalarProductRT = (joyReference.x * touchPosRelated.x) + (joyReference.y * touchPosRelated.y);
			double modulusProductRT =  Math.sqrt((Math.pow(joyReference.x, 2) + Math.pow(joyReference.y, 2))) // Modulus center-reference
										* Math.sqrt((Math.pow(touchPosRelated.x, 2) + Math.pow(touchPosRelated.y, 2))); // Modulus center-touchPos(related to center)

			cosAlpha = escalarProductRT / modulusProductRT;

			double alpha = Math.acos(cosAlpha);

			alpha = Math.toDegrees(alpha);

			// Gets distanceToMove making a proporcional vector of touchPos vector, by a given modulus
			float distance = Gdx.graphics.getDeltaTime() * VELOCITY;
			Vector3 distanceToMove = getProporcionalVectorByModulus(touchPosRelated, distance);


			soldier.x += distanceToMove.x;
			soldier.y += distanceToMove.y;

			if (CENTER_JOYSTICK_X > touchPos.x)
				alpha = 360 - alpha;

			direction = getDirectionByAngle(alpha);



		}
		else
		{
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
			if (countPressed <= 2) {
				if (pressedLeft) {
					// this if block controls --> left, left-right(stopped), leftUp and leftDown
					if (pressedRight)
						direction = WALK_STOPPED;
					else if (pressedDown) {
						soldier.x -= 100 * delta;
						soldier.y -= 100 * delta;
						direction = WALK_LEFT_DOWN;
					} else if (pressedUp) {
						soldier.x -= 100 * delta;
						soldier.y += 100 * delta;
						direction = WALK_LEFT_UP;
					} else {
						soldier.x -= 150 * delta;
						direction = WALK_LEFT;
					}
				} else if (pressedRight) {
					// this if block controls --> right, rightDown and rightUp
					if (pressedDown) {
						soldier.x += 100 * delta;
						soldier.y -= 100 * delta;
						direction = WALK_RIGHT_DOWN;
					} else if (pressedUp) {
						soldier.x += 100 * delta;
						soldier.y += 100 * delta;
						direction = WALK_RIGHT_UP;
					} else {
						soldier.x += 150 * delta;
						direction = WALK_RIGHT;
					}
				} else if (pressedUp) {
					// this if block controls --> up, upDown
					if (pressedDown) {
						direction = WALK_STOPPED;
					} else {
						soldier.y += 150 * delta;
						direction = WALK_UP;
					}
				} else if (pressedDown) {
					// this if block controls --> down
					direction = WALK_DOWN;
					soldier.y -= 150 * delta;
				} else // no pressed
					direction = WALK_STOPPED;
			}
			else // If more than 3 arrows pressed just stopped
			{
				currentFrame = stoppedAnimation.get(lastDirection);
			}

		}


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







		// draws the result
		batch.begin();
		batch.draw(currentFrame, soldier.x, soldier.y);
		batch.end();


	}

	private int getDirectionByAngle(double alpha) {



		if (alpha > 337.5 || alpha < 22.5)
		{
			return WALK_UP;
		}

		if (alpha <= 337.5 && alpha >= 292.5)
		{
			return WALK_LEFT_UP;
		}

		if (alpha > 247.5 && alpha < 292.5)
		{
			return WALK_LEFT;
		}

		if (alpha >= 202.5 && alpha <= 247.5)
		{
			return WALK_LEFT_DOWN;
		}

		if (alpha > 157.5 && alpha < 202.5)
		{
			return WALK_DOWN;
		}

		if (alpha >= 112.5 && alpha <= 157.5)
		{
			return WALK_RIGHT_DOWN;
		}

		if (alpha > 67.5 && alpha < 112.5)
		{
			return WALK_RIGHT;
		}

		if (alpha >= 22.5 && alpha <= 67.5)
		{
			return WALK_RIGHT_UP;
		}

		return -1;
	}

	private Vector3 getProporcionalVectorByModulus(Vector3 touchPos, float distance) {
		Vector3 propVector = new Vector3();
		double touchPosModulus = Math.sqrt((Math.pow(touchPos.x, 2) + Math.pow(touchPos.y, 2)));
		propVector.x = (float)((touchPos.x / touchPosModulus) * distance);
		propVector.y = (float)((touchPos.y / touchPosModulus) * distance);
		return propVector;
	}

	private Vector3 vectorRelatedJoystick(Vector3 vector)
	{
		Vector3 newVector = new Vector3();
		newVector.x = vector.x - CENTER_JOYSTICK_X;
		newVector.y = vector.y - CENTER_JOYSTICK_Y;
		return newVector;
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
