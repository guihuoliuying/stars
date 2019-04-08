package com.stars.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.stars.util.log.CoreLogger;

/**
 * 对象序列化工具
 * 
 * @author huachp
 */
public class SerializeUtil {
	
	static ThreadLocal<Kryo> threadLocal = new ThreadLocal<Kryo>() {
		@Override
		protected Kryo initialValue() {
			return new Kryo();
		}
	};
	
	
	public static byte[] serialize(Object message) {
		try {
			Kryo kryo = threadLocal.get();
			byte[] byteArray = new byte[1024 * 512];	// 512K	
			Output output = new Output(byteArray);
			kryo.writeClassAndObject(output, message);
			output.flush();
			output.close();
			return output.toBytes();
		} catch (Exception e) {
			CoreLogger.error("kryo序列化出错", e);
			throw e;
		}
	}
	
	
	public static Object deserialize(byte[] stream) {
		try {
			Kryo kryo = threadLocal.get();
			Input input = new Input(stream);
			return kryo.readClassAndObject(input);
		} catch (Exception e) {
			CoreLogger.error("kryo反序列化出错", e);
			throw e;
		}
	}
	
	
}
