package com.hjc.herol.herolrouter.server;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.hjc.herol.util.cache.MemcacheSupport;

/**
*@Entity注释指名这是一个实体Bean，@Table注释指定了Entity所要映射带数据库表，其中@Table.name()用来指定映射表的表名。
*如果缺省@Table注释，系统默认采用类名作为映射表的表名。实体Bean的每个实例代表数据表中的一行数据，行中的一列对应实例中的一个属性。
*@Column注释定义了将成员属性映射到关系表中的哪一列和该列的结构信息
*@Id注释指定表的主键
*/
@Table(name = "ServerConfig")
@Entity
public class ServerConfig implements MemcacheSupport {
	private static final long serialVersionUID = -1307168290491135062L;
	
	@Id
	private int id;
	private String name;
	private String ip;
	private int port;
	private String remark;
	private int max;
	/**
	 * @Fields state : 服务器状态 0-新服 ，1-空闲，2-繁忙，3-爆满，4-维护
	 */
	private int state;
	
	
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public long getIdentifier()
	{
		return this.id;
	}
}
