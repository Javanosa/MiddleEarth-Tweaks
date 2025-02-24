package metweaks.client.gui;

import lotr.client.gui.LOTRGuiButtonOptions;

public class GuiButtonOnOff extends LOTRGuiButtonOptions {
	
	public boolean state;

	public GuiButtonOnOff(int id, int x, int y, int width, int height, String name) {
		super(id, x, y, width, height, name);
	}
	
	public void setState(boolean state) {
		super.setState(state);
		this.state = state;
	}

}
