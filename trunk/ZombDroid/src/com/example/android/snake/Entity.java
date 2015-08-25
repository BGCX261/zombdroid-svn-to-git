package com.example.android.snake;

import android.graphics.Point;

public class Entity {
	private Point Location;
	public Point getLocation() {
		return Location;
	}
	public void setLocation(Point location) {
		Location = location;
	}

	private int Health;
	public int getHealth() {
		return Health;
	}
	public void setHealth(int health) {
		Health = health;
	}

}
