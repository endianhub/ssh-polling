package com.xh.ssh.polling.service.polling;

import java.util.Set;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.xh.ssh.polling.common.redis.JedisPoolUtils;
import com.xh.ssh.polling.common.tool.ThreadPoolUtils;
import com.xh.ssh.polling.model.OrderInfo;
import com.xh.ssh.polling.service.impl.DelayServiceImpl;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月18日
 */
public class PollingTask {

	@Resource
	DelayServiceImpl delayService;

	/**
	 * <b>Title: 定时任务</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 */
	public void execute() {
		System.out.println("\n\n\n>>>>>>>>>>>>系统启动完成");

		// 自动取消订单
		// delayService.start(new DelayServiceImpl.OnDelayedListener() {
		//
		// @Override
		// public void onDelayedArrived(OrderInfo order) {
		// // 异步来做
		// ThreadPoolUtils.execute(new Runnable() {
		// public void run() {
		// String orderId = order.getOrderNo();
		// // 查库判断是否需要自动取消订单
		// int surpsTime = JedisPoolUtils.ttl(orderId).intValue();
		// System.out.println("redis键:" + orderId + ";剩余过期时间:" + surpsTime);
		// if (surpsTime > 0) {
		// System.out.println("没有需要取消的订单!");
		// } else {
		// System.out.println("自动取消订单，删除队列和redis:" + orderId);
		// // 从队列中删除
		// delayService.remove(orderId);
		// // 从redis删除
		// JedisPoolUtils.del(orderId);
		// // todo 对订单进行取消订单操作
		// }
		// }
		// });
		// }
		// });

		// 自动回调
		delayService.start(new DelayServiceImpl.OnDelayedListener() {

			@Override
			public void onDelayedArrived(OrderInfo order) {
				// 异步来做
				ThreadPoolUtils.execute(new Runnable() {
					public void run() {
						String orderId = order.getOrderNo();
						System.out.println("发送post请求，查询订单是否支付成功....");
						if (order.getPayStatus() == 1) {
							System.out.println("支付成功");
							// 从队列中删除
							delayService.remove(orderId);
							// 从redis删除
							JedisPoolUtils.del(orderId);
						} else {
							System.out.println("未支付成功，检查订单是否超时");
							// 如果是未成功的则判断订单是否超时，如果超时则取消订单
							// 查库判断是否需要自动取消订单
							int surpsTime = JedisPoolUtils.ttl(orderId).intValue();
							System.out.println("redis键:" + orderId + ";剩余过期时间:" + surpsTime);
							if (surpsTime > 0) {
								System.out.println("没有需要取消的订单!");
							} else {
								System.out.println("自动取消订单，删除队列和redis:" + orderId);
								// 从队列中删除
								delayService.remove(orderId);
								// 从redis删除
								JedisPoolUtils.del(orderId);
								// todo 对订单进行取消订单操作
							}
						}
					}
				});
			}

		});

		// 查找需要入队的订单
		ThreadPoolUtils.execute(new Runnable() {
			public void run() {
				System.out.println("查找需要入队的订单");
				Set<String> keys = JedisPoolUtils.keys("*");
				if (keys == null || keys.size() <= 0) {
					return;
				}
				System.out.println("需要入队的订单keys：" + keys);
				for (String key : keys) {
					String value = JedisPoolUtils.get(key);
					int surpsTime = JedisPoolUtils.ttl(key).intValue();
					System.out.println("redis键:" + key + ";剩余过期时间:" + surpsTime);
					if (value != null) {
						OrderInfo info = JSON.parseObject(value, OrderInfo.class);
						delayService.add(info);
						System.out.println("订单自动入队：" + info.toString());
					}
				}
			}
		});
	}

}
