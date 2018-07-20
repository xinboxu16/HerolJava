package com.hjc.herol.herolrouter.client;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ContextLoader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hjc.herol.herolrouter.client.model.Account;
import com.hjc.herol.herolrouter.client.service.AccountService;
import com.hjc.herol.herolrouter.server.NotifyService;
import com.hjc.herol.herolrouter.server.ServerConfig;
import com.hjc.herol.herolrouter.server.ServerService;
import com.hjc.herol.util.Constants;
import com.hjc.herol.util.Helper;
import com.hjc.herol.util.SpringUtil;
import com.hjc.herol.util.cache.redis.RedisUtil;
import com.hjc.herol.util.hibernate.HibernateUtil;

/*
 1.到 /home 的请求会由 get() 方法来处理，而到 /home/index 的请求会由 index() 来处理
	@RestController  
	@RequestMapping("/home")  
	public class IndexController {  
	    @RequestMapping("/")  
	    String get() {  
	        //mapped to hostname:port/home/  
	        return "Hello from get";  
	    }  
	    @RequestMapping("/index")  
	    String index() {  
	        //mapped to hostname:port/home/index/  
	        return "Hello from index";  
	    }  
	}
2.@RequestMapping 来处理多个 URI
	@RestController  
	@RequestMapping("/home")  
	public class IndexController {  
	  
	    @RequestMapping(value = {  
	        "",  
	        "/page",  
	        "page*",  
	        "view/*
	    })  
	    String indexMultipleMapping() {  
	        return "Hello from index multiple mapping.";  
	    }  
	}
	
	如你在这段代码中所看到的，@RequestMapping 支持统配符以及ANT风格的路径。前面这段代码中，如下的这些 URL 都会由 indexMultipleMapping() 来处理： 
	localhost:8080/home
	localhost:8080/home/
	localhost:8080/home/page
	localhost:8080/home/pageabc
	localhost:8080/home/view/
	localhost:8080/home/view/view
*/  

@Controller
@RequestMapping(value = "/route")//这个注解会将 HTTP 请求映射到 MVC 和 REST 控制器的处理方法上
public class RouteController extends Helper<RouteController>{
	/**
	 * 使用 @Autowired 注释来除去 setter 方法。当时使用 为自动连接属性传递的时候，Spring 会将这些传递过来的值或者引用自动分配给那些属性
	 * 在使用@Autowired时，首先在容器中查询对应类型的bean
	 * 如果查询结果刚好为一个，就将该bean装配给@Autowired指定的数据
	 * 如果查询的结果不止一个，那么@Autowired会根据名称来查找。
	 * 如果查询的结果为空，那么会抛出异常。解决方法时，使用required=false
	 */
	@Autowired
	private AccountService accountService;
	@Autowired
	private ServerService serverService;
	@Autowired
	private NotifyService notifyService;
	
	public static final String COUNTRY_CACHE = "country_cache";
	public static final String JUNZHU_CACHE = "junzhu_cache";
	
	@RequestMapping(value="/loginOrRegist", method=RequestMethod.POST)
	public void loginOrRegist(HttpServletRequest request, HttpServletResponse response)
	{
		String username = request.getParameter("name");
		String password = request.getParameter("pwd");
		
		JSONObject ret = new JSONObject();
		if (username == null) {
			ret.put("code", 1);
			ret.put("msg", "用户名不能为空");
			writeJSON(ret, response, request.getRemoteAddr());
		}
		
		if (password == null) {
			ret.put("code", 1);
			ret.put("msg", "密码不能为空");
			writeJSON(ret, response, request.getRemoteAddr());
			return;
		}
		
		username = username.trim();
		password = password.trim();
		
		int state = ((AccountService)SpringUtil.getBean("account")).loginOrRegist(username, password, 0);
		ret.put("code", state);
		switch (state) {
		case 100:
			ret.put("msg", "登陆成功");
			break;
		case 101:
			ret.put("msg", "登录密码错误");
		case 200:
			ret.put("msg", "注册成功");
			break;
		case 201:
			ret.put("msg", "密码至少6位数");
			break;
		}
		if (state == 100 || state == 200) {
			Account account = ((AccountService)SpringUtil.getBean("account")).getAccount(username);
			ret.put("userId", account.getId());
			ret.put("lastServer", account.getLastServer());
			List<ServerConfig> serverConfigs = HibernateUtil.list(ServerConfig.class, "order by id DESC");
			JSONArray servers = new JSONArray();
			for (ServerConfig serverConfig : serverConfigs) {
				JSONArray serverArr = new JSONArray();
				serverArr.add(serverConfig.getId());
				serverArr.add(serverConfig.getIp());
				serverArr.add(serverConfig.getPort());
				serverArr.add(serverConfig.getState());
				serverArr.add(serverConfig.getName());
				servers.add(serverArr);
			}
			ret.put("server", servers);
		}
		writeJSON(ret, response, request.getRemoteAddr());
	}
	
	public JSONArray regist(String username, String password) {
		JSONArray ret = new JSONArray();
		if (username == null) {
			ret.add(1);
			ret.add("用户名不能为空");
			return ret;
		}
		if (password == null) {
			ret.add(1);
			ret.add("密码不能为空");
			return ret;
		}
		
		username = username.trim();
		password = password.trim();
		
		int state = ((AccountService)SpringUtil.getBean("account")).regist(username, password, 0);
		switch (state) {
		case 0:
			ret.add(0);
			ret.add("注册成功");
			break;
		case 1:
			ret.add(1);
			ret.add("账号已存在");
			break;
		case 2:
			ret.add(2);
			ret.add("密码至少8位数");
			break;
		default:
			break;
		}
		return ret;
	}
	
	@RequestMapping(value="/regist", method=RequestMethod.POST)
	public void regist(HttpServletRequest request, HttpServletResponse response) {
		String username = request.getParameter("name");
		String password = request.getParameter("pwd");
		
		JSONArray jsonArray = regist(username, password);
		
		writeJSON(jsonArray, response, request.getRemoteAddr());
	}
	
	public JSONObject chooseServer(long userid, long server) {
		Account account = null;
		JSONObject result = new JSONObject();
		try {
			account = HibernateUtil.find(Account.class, (userid-server)/1000);
			if (account != null) {
				account.setLastServer(server);
				HibernateUtil.save(account);
			}
			JSONArray array = new JSONArray();
			// 国家人数
			String nums = RedisUtil.getInstance().hget(RedisUtil.GLOBAL_DB, COUNTRY_CACHE, String.valueOf(server));
			if (nums == null) {
				nums = "0#0#0";
			}
			String num1 = nums.split("#")[0];
			String num2 = nums.split("#")[1];
			String num3 = nums.split("#")[2];
			array.add(num1);
			array.add(num2);
			array.add(num3);
			
			// 玩家是否存在
			String servers = RedisUtil.getInstance().hget(RedisUtil.GLOBAL_DB, JUNZHU_CACHE, String.valueOf(account.getId()));
			servers = (servers == null) ? "-1#-1" : servers;
			if (Arrays.asList(servers.split("#")).contains(String.valueOf(server))) {
				result.put("new", false);
			} else {
				result.put("new", true);
				result.put("country", array);
			}
			int sum = Integer.parseInt(num1) + Integer.parseInt(num2) + Integer.parseInt(num3);
			result.put("sum", sum);
			log.info("账号{}登录了服务器{},服务器总人数{},魏国{}人，蜀国{}人，吴国{}人", userid, server, sum, Integer.parseInt(num1), Integer.parseInt(num2), Integer.parseInt(num3));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/chooseServer", method = RequestMethod.POST)
	public void chooseServer(HttpServletRequest request, HttpServletResponse response)
	{
		long userid = Long.parseLong(request.getParameter("userid"));
		long server = Long.parseLong(request.getParameter("server"));
		
		JSONObject result = chooseServer(userid, server);
		
		writeJSON(result, response, request.getRemoteAddr());
	}
	
	public JSONArray login(String username, String password) {
		JSONArray ret = new JSONArray();
		if (username == null) {
			ret.add(1);
			ret.add("用户名不能为空");
			return ret;
		}
		if (password == null) {
			ret.add(1);
			ret.add("密码不能为空");
			return ret;
		}
		username = username.trim();
		password = password.trim();
		int state = ((AccountService)SpringUtil.getBean("account")).login(username, password, 0);
		switch (state) {
		case 0:
			ret.add(0);
			ret.add("登录成功");
			Account account = ((AccountService)SpringUtil.getBean("account")).getAccount(username);
			ret.add(account.getId());
			break;
		case 1:
			ret.add(1);
			ret.add("账号不存在");
			break;
		case 2:
			ret.add(2);
			ret.add("密码错误");
			break;
		}
		return ret;
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public void login(HttpServletRequest request, HttpServletResponse response) {
		String username = request.getParameter("name");
		String password = request.getParameter("pwd");
		JSONArray ret = login(username, password);
		writeJSON(ret, response, request.getRemoteAddr());
	}
	
	public JSONArray getServers() {
		List<ServerConfig> serverConfigs = HibernateUtil.list(ServerConfig.class, "");
		JSONArray ret = new JSONArray();
		for (ServerConfig serverConfig : serverConfigs) {
			JSONArray server = new JSONArray();
			server.add(serverConfig.getIp());
			server.add(serverConfig.getPort());
			server.add(serverConfig.getState());
			server.add(serverConfig.getName());
			ret.add(server);
		}
		return ret;
	}
	
	@RequestMapping(value = "/getServers")
	public void getServers(HttpServletRequest request, HttpServletResponse response) {
		JSONArray ret = getServers();
		writeJSON(ret, response, request.getRemoteAddr());
	}
	
	protected void writeJSON(Object msg, HttpServletResponse response, String remoteAddr) {
		String result = JSON.toJSONString(msg);
		log.info("ip:{},write:{}", remoteAddr, result);
		try {
			response.getWriter().write(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
