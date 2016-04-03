package com.graphics.lib.texture;

import java.awt.Color;

public class TestTexture implements Texture {

	private int[][] map;
	private int repeatx = 8;
	private int repeaty = 8;
	private Color colour = new Color(200,0,0);
	private int order = 0;
	//private int repeatx = 1;
	//private int repeaty = 1;
	
	public TestTexture(){
		map = new int[5][5];
		map[0] = new int[] {0,0,1,0,0};
		map[1] = new int[] {0,0,1,0,0};
		map[2] = new int[] {1,1,0,1,1};
		map[3] = new int[] {0,0,1,0,0};
		map[4] = new int[] {0,0,1,0,0};
		
		/*map = new int[7][8];
		map[0] = new int[] {0,0,0,0,0,0,0,0,0};
		map[1] = new int[] {0,0,1,0,0,0,1,0,0};
		map[2] = new int[] {0,0,0,0,0,0,0,0,0};
		map[3] = new int[] {0,0,0,0,1,0,0,0,0};
		map[4] = new int[] {0,1,0,0,0,0,0,1,0};
		map[5] = new int[] {0,0,1,1,1,1,1,0,0};
		map[6] = new int[] {0,0,0,0,0,0,0,0,0};*/
	}
	
	@Override
	public Color getColour(int x, int y) {
		if (x < 0 || y < 0) return null;
		
		if (map[x % 5][y % 5] == 1){
		//if (map[x][y] == 1){
			return colour;
		}
		return null;
	}

	@Override
	public int getHeight() {
		return (map[0].length * repeaty) -1;
	}

	@Override
	public int getWidth() {
		return (map.length * repeatx) -1;
	}

	@Override
	public boolean applyLighting() {
		return true;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
