package com.soldiergame.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.soldiergame.game.SoldierGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Soldier";
		config.width = 800;
		config.height = 480;
		new LwjglApplication(new SoldierGame(), config);
	}
}
