package com.hjc.herol;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpClientTest {
	public volatile long time = 0;
	
	public static void main(String[] args) throws Exception {
		HttpClientTest test = new HttpClientTest();

		JSONObject obj = new JSONObject();
		obj.put("userid", 2001);
		JSONObject dataJson = new JSONObject();
		dataJson.put("game", "1.0.1");
		obj.put("data", dataJson);
		String data = "data=" + JSON.toJSONString(obj);
		test.loadTest(data);
	}
	
	public void loadTest(final String params) throws MalformedURLException {
		final URL url = new URL("http://127.0.0.1:8586?"+params);
		for (int i = 0; i < 1; i++) {
			new Thread(new Runnable() {
				public void run() {

					try {
						long start = System.currentTimeMillis();
						HttpURLConnection http = (HttpURLConnection) url.openConnection();
						http.setRequestMethod("GET");
						//http.setDoOutput(true);
//						OutputStreamWriter out = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
//						if (params != null) {
//							out.write(params);
//						}
//						out.flush();
//						out.close();

						InputStream in = http.getInputStream();
						BufferedReader read = new BufferedReader(
								new InputStreamReader(in, "UTF-8"));
						String valueString = null;
						StringBuffer bufferRes = new StringBuffer();
						while ((valueString = read.readLine()) != null) {
							bufferRes.append(valueString);
						}
						in.close();
						if (http != null) {
							http.disconnect();// 关闭连接
						}
						long end = System.currentTimeMillis();
						synchronized (this) {
							time += (end - start);
						}
						System.out.println("间隔时间:" + (end - start) / 1000
								+ "s," + bufferRes.toString());
						Thread.sleep(30000);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e.getCause().getMessage());
					}
				}
			}).start();
		}
	}
}
