package com.example.android.snake;

import java.util.ArrayList;

public class Human extends Entity {
	public Human()
	{
		  setHealth(100);
		  Weapon Fists = new Weapon();
		  Fists.setPower(5);
		  Fists.setRange(2);
		  //Weapons.add(Fists);
		  setSelectedWeapon(Fists);
	}

	private Weapon SelectedWeapon;
	public Weapon getSelectedWeapon() {
		return SelectedWeapon;
	}
	public void setSelectedWeapon(Weapon selectedWeapon) {
		SelectedWeapon = selectedWeapon;
	}
	ArrayList<Weapon> Weapons;
	public ArrayList<Weapon> getWeapons() {
		return Weapons;
	}
	public void setWeapons(ArrayList<Weapon> weapons) {
		Weapons = weapons;
	}
}
