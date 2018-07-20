package com.hjc.herol.herolrouter.client.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.hjc.herol.util.cache.MemcacheSupport;

//@Entity说明这个class是实体类，并且使用默认的orm规则，即class名即数据库表中表名，class字段名即表中的字段名
//果想改变这种默认的orm规则，就要使用@Table来改变class名与数据库中表名的映射规则，@Column来改变class中字段名与db中表的字段名的映射规则
@Entity
public class Account implements MemcacheSupport {
	private static final long serialVersionUID = 8653559269984386584L;
	
	//通过annotation来映射hibernate实体的,基于annotation的hibernate主键标识为@Id, 
	//TABLE：使用一个特定的数据库表格来保存主键。 
	//SEQUENCE：根据底层数据库的序列来生成主键，条件是数据库支持序列。 
	//IDENTITY：主键由数据库自动生成（主要是自动增长型） 
	//AUTO：主键由程序控制。 
	@Id
	private long id;
	private String name;
	private String pwd;
	//@Column注解来标识实体类中属性与数据表中字段的对应关系
	//columnDefinition属性表示创建表时，该字段创建的SQL语句，一般用于通过Entity生成表定义时使用。（也就是说，如果DB中表已经建好，该属性没有必要使用。）
	@Column(columnDefinition = "INT default 0")
	private int channel;
	@Column(columnDefinition = "INT default 0")
	private long lastServer;
	private Date createtime;
	private Date lastlogintime;

	@Override
	public long getIdentifier() {
		// TODO Auto-generated method stub
		return id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getLastlogintime() {
		return lastlogintime;
	}

	public void setLastlogintime(Date lastlogintime) {
		this.lastlogintime = lastlogintime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public long getLastServer() {
		return lastServer;
	}

	public void setLastServer(long lastServer) {
		this.lastServer = lastServer;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
