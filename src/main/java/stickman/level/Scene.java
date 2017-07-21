package stickman.level;

import java.util.*;

import stickman.display.Display;
import stickman.entity.*;
import stickman.player.Player;
import stickman.resources.Resources;

public class Scene
{
	private HashMap <Image, List<String>> dialogsPerScene;
	private Display display = new Display();
	private Image scene;
	private List<String> dialogs;
	public boolean isOver = false;

	public Scene(HashMap<Image, List<String>> scenesAndDialogs) {
        dialogsPerScene = scenesAndDialogs;
		display = new Display();
		isOver = false;
	}

	public void play() {
		while(dialogsPerScene.keySet().iterator().hasNext()) {
			scene = dialogsPerScene.keySet().iterator().next();
			dialogs = dialogsPerScene.remove(scene);
			print();
		}
	}

	private void print() {
		while(!dialogs.isEmpty()) {
			String dialog = dialogs.remove(0);
			display.render(scene, dialog);
			display.print();
			Display.sleep();
		}
	}
}
