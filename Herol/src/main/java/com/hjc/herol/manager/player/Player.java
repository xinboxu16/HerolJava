package com.hjc.herol.manager.player;

import java.util.Map;

import com.hjc.herol.manager.hero.HeroInfo;
import com.hjc.herol.manager.treasure.TreasureInfo;

public class Player {
	public long _id;
	public String name;
	public int coin;
	public int yuanbao;
	public int fightGroup;
	public int cups;
	/**
	 * Map<key,values> Map<int,int> map=new Map<int,int>();这样做为什么会出错
	 * 泛型的声明必须是一个类,int是基本数据类型而不是一个类,这里应该用int的封装类Integer做声明,也就是Map<Integer,Integer> ,
	 * 另外等号右边Map是一个接口不能直接实例化,应该用其实现类比如HashMap<Integer,Integer>()
	 */
	public Map<Integer, HeroInfo> heros;
	public Map<Integer, TreasureInfo> treasures;

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public int getYuanbao() {
		return yuanbao;
	}

	public void setYuanbao(int yuanbao) {
		this.yuanbao = yuanbao;
	}

	public int getFightGroup() {
		return fightGroup;
	}

	public void setFightGroup(int fightGroup) {
		this.fightGroup = fightGroup;
	}

	public int getCups() {
		return cups;
	}

	public void setCups(int cups) {
		this.cups = cups;
	}

	public Map<Integer, HeroInfo> getHeros() {
		return heros;
	}

	public void setHeros(Map<Integer, HeroInfo> heros) {
		this.heros = heros;
	}

	public Map<Integer, TreasureInfo> getTreasures() {
		return treasures;
	}

	public void setTreasures(Map<Integer, TreasureInfo> treasures) {
		this.treasures = treasures;
	}
}
