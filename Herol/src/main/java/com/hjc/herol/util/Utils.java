package com.hjc.herol.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hjc.herol.util.encrypt.XXTeaCoder;

public class Utils {
	public static Logger log = LoggerFactory.getLogger(Utils.class);
	private static Map<String, Logger> loggerMap = new HashMap<String, Logger>();
	private static Map<String, Properties> propMap = new HashMap<String, Properties>();

	public static Logger getLoggerObj(Class<?> classValue)
	{
		String className = classValue.getName();
		if (!loggerMap.containsKey(className)) {
			Logger log = LoggerFactory.getLogger(classValue);
			loggerMap.put(className, log);
		}
		return loggerMap.get(className);
	}
	
	public static StringBuffer exceptionTrace(Class<?> cls, Exception e) {
		Logger log = LoggerFactory.getLogger(cls);
		//log.error("error msg:", e);
		e.printStackTrace();
		// Print our stack trace
		StringBuffer eBuffer = new StringBuffer(e.getMessage() + ",");
		StackTraceElement[] trace = e.getStackTrace();
		for (StackTraceElement traceElement : trace) {
			eBuffer.append("\r\n " + traceElement);
		}
		log.error("post error msg:", eBuffer.toString());
		return eBuffer;
	}
	
	/**
	 * @Title: codeFilter
	 * @Description: 编解码过滤
	 * @param val
	 * @return String
	 * @throws
	 */
	public static String codeFilter(String val, boolean ENCRIPT_DECRIPT) {
		try {
			val = val.contains("%") ? URLDecoder.decode(val, "UTF-8") : val;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String valTmp = val;
		val = ENCRIPT_DECRIPT ? XXTeaCoder.decryptBase64StringToString(val,
				XXTeaCoder.key) : val;
		if (Constants.MSG_LOG_DEBUG) {
			if (val == null) {
				val = valTmp;
			}
			log.info("server received : {}", val);
		}
		return val;
	}
	
	/**
	 * 读取Properties配置
	 */
	public static Properties loadProperties(String fileName){
		fileName = "/"+fileName + ".properties";
		if (propMap.containsKey(fileName)) {
			return propMap.get(fileName);
		}
		Properties properties = new Properties();
		InputStream inputStream = null;
		try {
			inputStream = Utils.class.getResourceAsStream(fileName);
			//Reader reader =  new InputStreamReader(inputStream, Charset.forName("UTF-8"));
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			log.error("properties文件未找到");
		}catch (IOException e) {
			log.error("出现IOException");
		}finally {
			try {
				if (null != inputStream) {
					inputStream.close();
				}
			} catch (IOException e) {
				log.error("jdbc.properties文件流关闭出现异常");
			}
		}
		return properties;
	}
	
	public static String getProperty(String fileName, String key) {
		Properties properties = loadProperties(fileName);
		if (null != properties) {
			return properties.getProperty(key);
		}
		return "";
	}
	
	/**byte数组中取int数值，本方法适用于(由高位到低位 高位在前，低位在后)的顺序。*/
	public static int bytesToInt(byte[] src, int offset) {
		int value = (int)(((src[offset] & 0xff) << 24) | 
				((src[offset+1] & 0xff) << 16) | 
				((src[offset+2] & 0xff) << 8) | 
				(src[offset+3] & 0xff));
/*		for(inti = 0; i < 4; i++) {
            int shift= (4-1-i) * 8;
            value +=(bytes[i] & 0x000000FF) << shift;//往高位游
        }*/
		return value;
	}
	
	public static int bytesToInt(byte[] src) {
		int value = bytesToInt(src, 0);
		return value;
	}
	
	/**  
	    * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序
	    *   
	    * @param src  
	    *            byte数组  
	    * @param offset  
	    *            从数组的第offset位开始  
	    * @return int数值  
	    */    
	public static int bytesToBigInt(byte[] src, int offset) {  
	    int value;    
	    value = (int) ((src[offset] & 0xFF)   
	            | ((src[offset+1] & 0xFF)<<8)   
	            | ((src[offset+2] & 0xFF)<<16)   
	            | ((src[offset+3] & 0xFF)<<24));  
	    return value;  
	} 
	
	public static int bytesToBigInt(byte[] src) {  
		int value = bytesToBigInt(src, 0);
		return value;  
	}
	
	/**将int数值转换为占四个字节的byte数组，本方法适用于(由高位到低位 高位在前，低位在后)的顺序。*/
	public static byte[] intToBytes(int value) {
		byte[] src = new byte[4];
		src[0] = (byte) ((value>>24) & 0xFF);  
	    src[1] = (byte) ((value>>16)& 0xFF);  
	    src[2] = (byte) ((value>>8)&0xFF);    
	    src[3] = (byte) (value & 0xFF);
		return src;
	}
	
	/**  
    * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。
    * @param value  
    *            要转换的int值 
    * @return byte数组 
    */    
	public static byte[] intToBigBytes( int value )   
	{   
	    byte[] src = new byte[4];  
	    src[0] =  (byte) (value & 0xFF);
	    src[1] =  (byte) ((value>>8) & 0xFF);
	    src[2] =  (byte) ((value>>16) & 0xFF);
	    src[3] =  (byte) ((value>>24) & 0xFF);         
	    return src;   
	} 
}
