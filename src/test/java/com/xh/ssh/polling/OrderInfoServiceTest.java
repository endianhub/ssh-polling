package com.xh.ssh.polling;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.xh.ssh.polling.common.redis.JedisPoolUtils;
import com.xh.ssh.polling.model.OrderInfo;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月16日
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/spring-config.xml" })
public class OrderInfoServiceTest {

	// @Test
	public void save() throws InterruptedException {
		// JedisPoolUtils.flushAll();
		// long beginTime = System.currentTimeMillis();
		// for (int i = 0; i < 20; i++) {
		// // 定义最早的订单的创建时间
		// beginTime += 3000L;
		// String orderNo = String.valueOf(System.currentTimeMillis());
		// OrderInfo info = new OrderInfo();
		// info.setId(i);
		// info.setOrderNo(orderNo);
		// info.setPayStatus(0);
		// info.setTotalPrice(new BigDecimal("120.00"));
		// info.setCloseTime(new Date(beginTime + 3000));
		// info.setCreateTime(new Date(beginTime));
		// info.setDelayedStatus("CREATED");
		//
		// queue.add(info);
		// }
		//
		// Thread.sleep(10 * 60 * 1000);
	}

	@Test
	public void startOrder() throws InterruptedException {
		JedisPoolUtils.flushAll();
		for (int i = 0; i < 10; i++) {
			Long beginTime = System.currentTimeMillis();
			// 1 插入到待收货队列
			OrderInfo info = new OrderInfo();
			info.setId(beginTime.intValue());
			info.setOrderNo(beginTime.toString());
			info.setPayStatus((i % 2 == 0) ? 0 : 1);
			info.setTotalPrice(new BigDecimal("120.00"));
			info.setCloseTime(new Date(beginTime + 3000));
			info.setCreateTime(new Date(beginTime));

			System.out.println("订单order" + beginTime + "入队列");
			// 2插入到redis
			JedisPoolUtils.set(info.getOrderNo(), JSON.toJSONString(info));
			// JedisPoolUtils.expire(info.getOrderNo(), 1 * 60);
			JedisPoolUtils.expire(info.getOrderNo(), 15);

			System.out.println("订单order" + info.getOrderNo() + "入redis缓存");
			Thread.sleep(100);
		}

		Thread.sleep(10 * 60 * 1000);
	}

	public static void main(String[] args) throws InterruptedException {
		OrderInfoServiceTest test = new OrderInfoServiceTest();
		test.startOrder();
	}
}
