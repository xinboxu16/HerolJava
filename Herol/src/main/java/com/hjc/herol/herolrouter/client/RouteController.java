package com.hjc.herol.herolrouter.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;
import com.hjc.herol.herolrouter.client.service.AccountService;
import com.hjc.herol.herolrouter.server.NotifyService;
import com.hjc.herol.herolrouter.server.ServerService;
import com.hjc.herol.util.Helper;

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
	public static final String JUNZHU_CACHE_STRING = "junzhu_cache";
	
	@RequestMapping(value="/loginOrRegist", method=RequestMethod.POST)
	public void loginOrRegist(HttpServletRequest request, HttpServletResponse response)
	{
		String username = request.getParameter("name");
		String password = request.getParameter("pwd");
		
		JSONObject ret = new JSONObject();
		if (username == null) {
			ret.put("code", 1);
			ret.put("msg", "用户名不能为空");
			writeJson(ret, response, request.getRemoteAddr());
		}
		
		if (password == null) {
			ret.put("code", 1);
			ret.put("msg", "密码不能为空");
			writeJSON(ret, response, request.getRemoteAddr());
			return;
		}
		
		username = username.trim();
		password = password.trim();
		
		int state = accountService.loginOrRegist(username, password, 0);
	}
}
