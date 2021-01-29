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
	private static final int[] WANTED_ROWS = {2,4,5,7};

	private static final int WALK_UP = 0;
	private static final int WALK_RIGHT = 1;
	private static final int WALK_LEFT = 2;
	private static final int WALK_DOWN = 3;
	private static final int WALK_STOPPED = 4;

	private int lastDirection;


	@Override
	public void create () {

		// Version que se mueve en 4 direcciones únicamente con las flechas del teclado,
		// no comento nada porque al final voy a hacer que se mueva en diagonal también.


		lastDirection = 0;
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);


		soldierSheet = new Texture(Gdx.files.internal("soldier.png"));

		TextureRegion[][] tmp = TextureRegion.split(soldierSheet,
				soldierSheet.getWidth() / SHEET_COLS,
				soldierSheet.getHeight() / SHEET_ROWS);
		TextureRegion[][] walk = new TextureRegion[SHEET_ROWS/2][SHEET_COLS-1];

		int index = 0;
		int newIndex = 0;
		stoppedAnimation = new ArrayList<TextureRegion>();
		for (int i = 0; i < SHEET_ROWS; i++) {
			boolean wanted = false;
			for (int k = 0; k < WANTED_ROWS.length; k++){
				if ((i+1) == WANTED_ROWS[k])
					wanted = true;
			}
			if (!wanted)
				continue;

			for (int j = 0; j < SHEET_COLS; j++) {
				if (j == SHEET_COLS-1)
				{
					stoppedAnimation.add(tmp[i][j]);
				}
				else
				{
					int newJ = (SHEET_COLS-2) - j;
					walk[newIndex][newJ] = tmp[i][j];
				}
			}
			newIndex++;
		}

		walkAnimation = new ArrayList<Animation<TextureRegion>>();
		for (int i = 0; i < walk.length; i++)
		{
			float frameDuration = 0.1f;
			if (i == WALK_UP || i == WALK_DOWN)
				frameDuration = 0.12f;
			walkAnimation.add(new Animation<TextureRegion>(frameDuration, walk[i]));
		}


		soldier = new Rectangle();
		soldier.width = soldierSheet.getWidth() / SHEET_COLS;
		soldier.height = soldierSheet.getHeight() / SHEET_ROWS;
		soldier.x = 800 / 2;
		soldier.y = 20;




		stateTime = 0f;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		stateTime += Gdx.graphics.getDeltaTime();

		TextureRegion currentFrame = null;
		int direction;
		float delta = Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			soldier.x -= 200 * delta;
			direction = WALK_LEFT;
		}

		else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			soldier.x += 200 * delta;
			direction = WALK_RIGHT;
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			soldier.y += 185 * delta;
			direction = WALK_UP;
		}
		else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			direction = WALK_DOWN;
			soldier.y -= 185 * delta;
		}
		else
			direction = WALK_STOPPED;

		if(soldier.x < 0) soldier.x = 0;
		if(soldier.x > 800 - soldier.width) soldier.x = 800 - soldier.width;
		if(soldier.y < 0) soldier.y = 0;
		if(soldier.y > 480 - soldier.height) soldier.y = 480 - soldier.height;

		if (direction != WALK_STOPPED)
		{
			currentFrame = walkAnimation.get(direction).getKeyFrame(stateTime, true);
			lastDirection = direction;
		}
		else
			currentFrame = stoppedAnimation.get(lastDirection);

		batch.begin();
		batch.draw(currentFrame, soldier.x, soldier.y);
		batch.end();


	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
