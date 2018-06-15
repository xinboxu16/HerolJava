package com.hjc.herol;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpClientTest {
	public volatile long time = 0;
	public volatile int index = 0;
	
	public static void main(String[] args) throws Exception {
		HttpClientTest test = new HttpClientTest();

		JSONObject obj = new JSONObject();
		obj.put("userid", 2001);
		JSONObject dataJson = new JSONObject();
		dataJson.put("game", "1.0.1");
		obj.put("data", dataJson);
		String data = "data=" + JSON.toJSONString(obj);
		test.loadTest(data);
		
		//test.downloadTest("http://127.0.0.1:8586/files/cocos2d-x-2.2.6.zip","C:\\Users\\lenovo\\AppData\\Local\\CurlTest\\file", "cocos2d-x-2.2.6");
	}
	
	public void loadTest(final String params) throws MalformedURLException {
		//final URL url = new URL("http://127.0.0.1:8586?"+params);
		final URL url = new URL("http://127.0.0.1:8586");
		for (int i = 0; i < 1000; i++) {
			new Thread(new Runnable() {
				public void run() {

					try {
						long start = System.currentTimeMillis();
						HttpURLConnection http = (HttpURLConnection) url.openConnection();
						//http.setRequestMethod("GET");
						//post部分开始
						http.setDoOutput(true);
						OutputStreamWriter out = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
						if (params != null) {
							out.write(params);
						}
						out.flush();
						out.close();
						//post部分结束

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
	
	/**
     * 从输入流中获取字节数组 toByteArray会引起内存超出错误
     * @param inputStream
     * @return
     * @throws IOException
     */
    public byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
	
	public void createThread(final int index, final URL url, final String downloadDir, final String fileName)
	{
		//final URL url = new URL(urlPath);
		new Thread(new Runnable(){

			public void run() {
				File file = null;
				try {
					long start = System.currentTimeMillis();
					HttpURLConnection http = (HttpURLConnection) url.openConnection();
					http.setRequestMethod("GET");
					
					// 设置字符编码
					http.setRequestProperty("Charset", "UTF-8");
					
					// 文件大小
					int fileLength = http.getContentLength();
					// 文件名
					String filePathUrl = http.getURL().getFile();
					String fileFullName = filePathUrl.substring(filePathUrl.lastIndexOf(File.separatorChar) + 1);
					
					System.out.println("file length---->" + fileLength);

					InputStream inputStream = http.getInputStream();
					
					String path = downloadDir + File.separatorChar + fileFullName;
					file = new File(path);
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					
					OutputStream out = new FileOutputStream(file);
					
					byte[] buffer=new byte[2048];
					int count=0;
		            int finished=0;
		            int size=fileLength;
		            while((count=inputStream.read(buffer))!=-1){
		            	if(count!=0){
		                    out.write(buffer,0,count);
		                    finished+=count;
		                   System.out.printf("---->%1$.2f%%\n",(double)finished/size*100);
		            	}else{
		            		break;
		            	}
		            }
					
					if (http != null) {
						http.disconnect();// 关闭连接
					}
					inputStream.close();
					out.close();
					//Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(e.getCause().getMessage());
				}
			}
			
		}).start();
	}
	
	public void downloadTest(final String urlPath, final String downloadDir, final String fileName) throws MalformedURLException {
		for (int i = 0; i < 5; i++) {
			createThread(i, new URL("http://127.0.0.1:8586/files/cocos2d-x-2.2.6"+String.valueOf(i)+".zip"), "C:\\Users\\lenovo\\AppData\\Local\\CurlTest", fileName);
		}
	}
}
