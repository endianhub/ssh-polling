package com.xh.ssh.polling.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.xh.ssh.polling.common.tool.LogUtils;
import com.xh.ssh.polling.dao.OrderInfoDao;
import com.xh.ssh.polling.model.OrderInfo;
import com.xh.ssh.polling.service.OrderInfoService;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月16日
 */
@Service
public class OrderInfoServiceImpl implements OrderInfoService {

	@Resource
	private OrderInfoDao dao;

	@Override
	public int save(OrderInfo entity) {
		return dao.saveObject(entity);
	}

	@Override
	public OrderInfo update(OrderInfo entity) {
		LogUtils.info(this.getClass(), "更新操作");
		return dao.updateObject(entity);
	}

	@Override
	public List<OrderInfo> queryList() {

		return dao.queryList();
	}

}
