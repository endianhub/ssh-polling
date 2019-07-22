package com.xh.ssh.polling;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月22日
 */
public class App {

	public static void main(String[] args) throws InterruptedException {
		// JedisPoolUtils.flushAll();
		// for (int i = 0; i < 20; i++) {
		// Long beginTime = System.currentTimeMillis();
		// JedisPoolUtils.set(beginTime.toString(), "Order_" + i);
		// Thread.sleep(100);
		// }
		// Set<String> keys = JedisPoolUtils.keys("*");
		// System.out.println(keys.size());

		// String value = JedisPoolUtils.get("1563779688300");
		// System.out.println(value);
		// OrderInfo info = JSON.parseObject(value, OrderInfo.class);
		// System.out.println(info.toString());

		for (int i = 0; i < 20; i++) {
			System.out.println(i % 2 == 0 ? 0 : 1);
		}
	}

}
