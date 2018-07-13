package com.hjc.herol.net;

/**
 * 
 * @ClassName: ProtoIds
 * @Description: 存储协议号，添加协议号时，既要定义静态常量的协议号，也要在init方法中调用regist注册，方便协议号的管理
 * 
 */
public class ProtoIds {
	public static final short TEST = 10000;// 测试网络延迟
	public static final short GET_PVP_SERVER = 10001;// 获取Pvp服务器信息
	public static final short CREATE_ROLE = 1;// 创建角色
	public static final short PLAYER_QUERY = 2;// 查询玩家信息
	public static final short PLAYER_SET_GROUP = 3;// 设置出战卡组
	public static final short HERO_SET_GROUP = 4;// 设置英雄所属卡组
	public static final short HERO_QUERY = 5;// 查询英雄
	public static final short HERO_UP_LEVEL = 6;// 升级英雄
	public static final short TREASURE_QUERY = 7;// 查询宝箱
	public static final short TREASURE_HERO_PICK = 8;// 抽取英雄
	
	/** 登录请求 **/
	public static final short EXIT_SCENE = 10002;// 退出场景
	public static final short FIGHT_ENTER_SCENE = 11;// 进入游戏场景
	public static final short FIGHT_SKILL = 12;// 释放技能
	public static final short FIGHT_PICK_HERO = 13;// 释放技能
}
