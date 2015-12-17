package com.graphics.lib.texture;

import java.awt.Color;

public class TestTexture implements Texture {

	private int[][] map;
	private int repeatx = 10;
	private int repeaty = 10;
	
	public TestTexture(){
		map = new int[3][3];
		map[0] = new int[] {1,0,0};
		map[1] = new int[] {1,0,0};
		map[2] = new int[] {1,0,0};
	}
	
	@Override
	public Color getColour(int x, int y) {
		if (x < 0 || y < 0) return null;
		
		if (map[x % 3][y % 3] == 1){
			return Color.yellow;
		}
		return null;
	}

	@Override
	public int getHeight() {
		return (map.length * repeaty) -1;
	}

	@Override
	public int getWidth() {
		return (map[0].length * repeatx) -1;
	}

}
