package com.xh.ssh.polling.dao;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.xh.ssh.polling.model.OrderInfo;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月16日
 */
@Repository
public class OrderInfoDao extends HibernateDaoSupport {

	@Resource(name = "sessionFactory")
	private void setMySessionFactory(SessionFactory sessionFactory) {
		// 这个方法名可以随便写，@Resource可以通过name 或者type来装载的。
		super.setSessionFactory(sessionFactory);
	}

	public Session getSession() {

		return getSessionFactory().getCurrentSession();
	}

	public int saveObject(OrderInfo entity) {
		Assert.notNull(entity, "实体类不能为空");
		return (int) getHibernateTemplate().save(entity);
	}

	public OrderInfo updateObject(OrderInfo entity) {
		Assert.notNull(entity, "实体类不能为空");
		getHibernateTemplate().update(entity);
		return entity;
	}

	public List<OrderInfo> queryList() {
		String hql = "from OrderInfo ";
		Query query = getSession().createQuery(hql);

		return query.list();
	}

}
