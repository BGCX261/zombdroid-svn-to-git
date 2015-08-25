package com.example.android.snake;

import java.util.Random;

import android.R.string;

public class Weapon 
{
	Random oRandom = new Random();
	private int Range;
	public int getRange() 
	{
		return Range;
	}
	public void setRange(int range) 
	{
		Range = range;
	}
	
	private string Name;
	public string getName() 
	{
		return Name;
	}
	public void setName(string name) {
		Name = name;
	}

	private int Power;
	public int getPower() {
		return Power;
	}
	public void setPower(int power) {
		Power = power;
	}

	private int PowerCurve;
	
	public int getPowerCurve() {
		return PowerCurve;
	}
	public void setPowerCurve(int powerCurve) {
		PowerCurve = powerCurve;
	}

	private int NumberOfTurnsToSetUp;
	public int getNumberOfTurnsToSetUp() 
	{
		return NumberOfTurnsToSetUp;
	}
	public void setNumberOfTurnsToSetUp(int numberOfTurnsToSetUp) 
	{
		NumberOfTurnsToSetUp = numberOfTurnsToSetUp;
	}
	private int TurnsUntilNextShot;

	public int getTurnsUntilNextShot() {
		return TurnsUntilNextShot;
	}
	public void setTurnsUntilNextShot(int turnsUntilNextShot) {
		TurnsUntilNextShot = turnsUntilNextShot;
	}

	private int FireRatePerTurn;
	
	public int getFireRatePerTurn() {
		return FireRatePerTurn;
	}
	public void setFireRatePerTurn(int fireRatePerTurn) {
		FireRatePerTurn = fireRatePerTurn;
	}

	private int Accuracy;
	
	public int getAccuracy() {
		return Accuracy;
	}
	public void setAccuracy(int accuracy) {
		Accuracy = accuracy;
	}

	private int CriticalHit;
	
	public int getCriticalHit() {
		return CriticalHit;
	}
	public void setCriticalHit(int criticalHit) {
		CriticalHit = criticalHit;
	}

	private int ClipSize;
	
	public int getClipSize() {
		return ClipSize;
	}
	public void setClipSize(int clipSize) {
		ClipSize = clipSize;
	}
	private int AmmoInClip;
	
	public int getAmmoInClip() {
		return AmmoInClip;
	}
	public void setAmmoInClip(int ammoInClip) {
		AmmoInClip = ammoInClip;
	}

	private int Ammo;

	public int getAmmo() {
		return Ammo;
	}
	public void setAmmo(int ammo) {
		Ammo = ammo;
	}

	private int ReloadTime;
	
	public int getReloadTime() {
		return ReloadTime;
	}
	public void setReloadTime(int reloadTime) {
		ReloadTime = reloadTime;
	}

	private int AmmoTypes;
	
	public int getAmmoTypes() {
		return AmmoTypes;
	}
	public void setAmmoTypes(int ammoTypes) {
		AmmoTypes = ammoTypes;
	}

	private int CurrentAmmoType;
	public int getCurrentAmmoType() {
		return CurrentAmmoType;
	}
	public void setCurrentAmmoType(int currentAmmoType) {
		CurrentAmmoType = currentAmmoType;
	}
	public boolean Fire()
	{
		int RandomNumber = oRandom.nextInt(100);
		if(RandomNumber < Accuracy)
		{
			Ammo--;
			AmmoInClip--;
			TurnsUntilNextShot = FireRatePerTurn;
			return true;
		}
		else
		{
			return false;
		}
	}
	public void Reload() 
	{
		if(Ammo>ClipSize)
		{
			AmmoInClip = ClipSize;	
		}
		else
		{
			AmmoInClip = Ammo;
		}
	}

}
