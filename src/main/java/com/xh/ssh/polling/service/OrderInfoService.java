package com.xh.ssh.polling.service;

import java.util.List;

import com.xh.ssh.polling.model.OrderInfo;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月16日
 */
public interface OrderInfoService {

	public int save(OrderInfo entity);

	public OrderInfo update(OrderInfo entity);
	
	public List<OrderInfo> queryList();

}
