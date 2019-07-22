package com.xh.ssh.polling.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月16日
 */
@Entity
@Table(name = "order_info")
public class OrderInfo implements Delayed, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	@Column(name = "order_no")
	private String orderNo;
	@Column(name = "pay_status")
	private Integer payStatus;
	@Column(name = "total_price")
	private BigDecimal totalPrice;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "pay_time")
	private Date payTime;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "end_time")
	private Date endTime;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "close_time")
	private Date closeTime;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "update_time")
	private Date updateTime;
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "create_time")
	private Date createTime;

	@Transient
	private String delayedStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Integer getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(Integer payStatus) {
		this.payStatus = payStatus;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "OrderInfo [id=" + id + ", orderNo=" + orderNo + ", payStatus=" + payStatus + ", totalPrice=" + totalPrice + ", payTime=" + payTime + ", endTime=" + endTime
				+ ", closeTime=" + closeTime + ", updateTime=" + updateTime + ", createTime=" + createTime + "]";
	}

	public String getDelayedStatus() {
		return delayedStatus;
	}

	public void setDelayedStatus(String delayedStatus) {
		this.delayedStatus = delayedStatus;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		// 下面用到unit.convert()方法，其实在这个小场景不需要用到，只是学习一下如何使用罢了
		long l = unit.convert(closeTime.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		return l;
	}

	@Override
	public int compareTo(Delayed o) {
		// 这里根据取消时间来比较，如果取消时间小的，就会优先被队列提取出来
		return this.getCloseTime().compareTo(((OrderInfo) o).getCloseTime());
	}

}
