package com.xh.ssh.polling.service.impl;

import java.util.concurrent.DelayQueue;

import org.springframework.stereotype.Service;

import com.xh.ssh.polling.model.OrderInfo;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月22日
 */
@Service
public class DelayServiceImpl {

	private boolean start;
	private OnDelayedListener listener;
	private DelayQueue<OrderInfo> delayQueue = new DelayQueue<OrderInfo>();

	public static interface OnDelayedListener {
		public void onDelayedArrived(OrderInfo order);
	}

	public void start(OnDelayedListener listener) {
		if (start) {
			return;
		}
		System.out.println("DelayService 启动");
		start = true;
		this.listener = listener;
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						OrderInfo order = delayQueue.take();
						if (DelayServiceImpl.this.listener != null) {
							DelayServiceImpl.this.listener.onDelayedArrived(order);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void add(OrderInfo order) {
		delayQueue.put(order);
	}

	public void remove(String orderNo) {
		OrderInfo[] array = delayQueue.toArray(new OrderInfo[] {});
		if (array == null || array.length <= 0) {
			return;
		}
		OrderInfo target = null;
		for (OrderInfo order : array) {
			if (order.getOrderNo() == orderNo) {
				target = order;
				break;
			}
		}
		if (target != null) {
			delayQueue.remove(target);
		}
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public OnDelayedListener getListener() {
		return listener;
	}

	public void setListener(OnDelayedListener listener) {
		this.listener = listener;
	}

	public DelayQueue<OrderInfo> getDelayQueue() {
		return delayQueue;
	}

	public void setDelayQueue(DelayQueue<OrderInfo> delayQueue) {
		this.delayQueue = delayQueue;
	}

}
