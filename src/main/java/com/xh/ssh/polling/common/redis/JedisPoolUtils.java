package com.xh.ssh.polling.common.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xh.ssh.polling.common.tool.LogUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019年7月18日
 */
public class JedisPoolUtils {

	private static int index = 6;
	/** 默认缓存时间 */
	private static final int DEFAULT_CACHE_SECONDS = 60 * 60 * 1;// 单位秒 设置成一个钟
	public static JedisPool jedisPool = null;

	/**
	 * <b>Title: 获取RedisPool实例（单例）</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @return
	 */
	public static JedisPool getJedisPoolInstance() {
		if (jedisPool == null) {
			synchronized (JedisPoolUtils.class) {
				if (jedisPool == null) {
					JedisPoolConfig poolConfig = new JedisPoolConfig();
					// 设置最大连接总数
					poolConfig.setMaxTotal(60);
					// 设置最大空闲数
					poolConfig.setMaxIdle(30);
					// 设置最小空闲数
					poolConfig.setMinIdle(8);
					// 设置最大等待时间
					poolConfig.setMaxWaitMillis(5 * 1000);
					// 在获取连接的时候检查有效性, 默认false
					poolConfig.setTestOnBorrow(false);
					// 在空闲时检查有效性, 默认false
					poolConfig.setTestOnReturn(false);
					// 是否启用pool的jmx管理功能, 默认true
					poolConfig.setJmxEnabled(true);
					// Idle时进行连接扫描
					poolConfig.setTestWhileIdle(true);
					// 是否启用后进先出, 默认true
					poolConfig.setLifo(true);
					// 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
					// poolConfig.setTimeBetweenEvictionRunsMillis(-1);
					// 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
					// poolConfig.setNumTestsPerEvictionRun(10);
					// 表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
					// poolConfig.setMinEvictableIdleTimeMillis(60000);
					// 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
					poolConfig.setBlockWhenExhausted(true);
					// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
					// poolConfig.setSoftMinEvictableIdleTimeMillis(1800000);

					jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379);
					LogUtils.info(JedisPoolUtils.class, "=== JedisClientTool init:" + jedisPool + " ===");
				}
			}
		}
		return jedisPool;
	}

	/**
	 * <b>Title: 从连接池中获取一个 Jedis 实例（连接）</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @return Jedis 实例
	 */
	public static Jedis getJedisInstance() {

		return getJedisPoolInstance().getResource();
	}

	/**
	 * <b>Title: 处理JedisException，写入日志并返回连接是否中断。</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param jedisException
	 * @return
	 */
	public static void handleJedisException(Exception jedisException) {
		if (jedisException instanceof JedisConnectionException) {
			LogUtils.info(JedisPoolUtils.class, "Redis connection lost.");
		} else if (jedisException instanceof JedisDataException) {
			if ((jedisException.getMessage() != null) && (jedisException.getMessage().indexOf("READONLY") != -1)) {
				LogUtils.info(JedisPoolUtils.class, "Redis connection  are read-only slave.");
			}
		} else {
			LogUtils.info(JedisPoolUtils.class, "Jedis exception happen.");
		}
	}

	/**
	 * <b>Title: 释放redis资源</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param jedis
	 */
	public static void releaseResource(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
			// LogUtils.info(JedisPoolUtils.class, "redis close.");
		}
	}

	/**
	 * <b>Title: 删除Redis中的所有key</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * @return 
	 * 
	 */
	public static String flushAll() {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			return jedis.flushAll();
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache清空失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 删除当前选择数据库中的所有key</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * @return 
	 * 
	 */
	public static String flushDB() {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.flushDB();
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache清空失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 判断一个key是否存在</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param keys
	 * @return
	 */
	public static Long exists(String... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.exists(keys);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Boolean exists(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.exists(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return false;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 根据缓存键清除Redis缓存中的值</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param keys
	 * @return
	 */
	public static Long del(String... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.del(keys);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long del(byte[]... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.del(keys);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回值的类型</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static String type(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.type(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static String type(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.type(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回满足给定pattern的所有key</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param pattern
	 * @return
	 */
	public static Set<String> keys(String pattern) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.keys(pattern);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Set<byte[]> keys(byte[] pattern) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.keys(pattern);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回当前数据库中key的数目</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @return
	 */
	public static Long dbsize() {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.dbSize();
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 将当前数据库中的key转移到有dbindex索引的数据库</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param dbIndex
	 * @return
	 */
	public static Long move(String key, int dbIndex) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.move(key, dbIndex);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long move(byte[] key, int dbIndex) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.move(key, dbIndex);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache删除失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 设置超时</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param seconds 超时时间（单位为秒）
	 * @return
	 */
	public static Long expire(String key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.expire(key, seconds);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache设置超时时间失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long expire(byte[] key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.expire(key, seconds);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache设置超时时间失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 设置超时 - 时间戳(秒)</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param unixTime 时间戳(秒)
	 * @return
	 */
	public static Long expireAt(String key, int unixTime) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.expireAt(key, unixTime);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache设置超时时间失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long expireAt(byte[] key, int unixTime) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.expireAt(key, unixTime);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache设置超时时间失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 获取key的过期时间。如果key存在过期时间，返回剩余生存时间(秒)；如果key是永久的，返回-1；如果key不存在或者已过期，返回-2。</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static Long ttl(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.ttl(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache设置超时时间失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long ttl(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.ttl(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache设置超时时间失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 移除key的过期时间，将其转换为永久状态。如果返回1，代表转换成功。如果返回0，代表key不存在或者之前就已经是永久状态。</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static Long persist(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.persist(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache设置超时时间失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long persist(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.persist(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache设置超时时间失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/****************************************************************************************************
	 * 
	 * 
	 * String操作
	 * 
	 * 
	 * 
	 ****************************************************************************************************/

	/**
	 * <b>Title: 保存一个对象到Redis中</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key 键
	 * @param value 缓存对象
	 * @return
	 */
	public static String set(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);// 选择redis库号---不指定库号，默认存入到0号库
			return jedis.set(key, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache保存失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static String set(byte[] keyBytes, byte[] valueBytes) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.set(keyBytes, valueBytes);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, "Cache保存失败：" + e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 根据缓存键获取Redis缓存中的值</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.get(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static byte[] get(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.get(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 给名称为key的string赋予上一次的value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static String getSet(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.getSet(key, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static byte[] getSet(byte[] keyBytes, byte[] valueBytes) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.getSet(keyBytes, valueBytes);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回库中多个string（它们的名称为key1，key2…）的value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param keys
	 * @return
	 */
	public static List<String> mget(String... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.mget(keys);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static List<byte[]> get(byte[]... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.mget(keys);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 如果不存在名称为key的string，则向库中添加string，名称为key，值为value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Long setnx(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.setnx(key, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long setnx(byte[] key, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.setnx(key, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 向库中添加string（名称为key，值为value）同时，设定过期时间time</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param value
	 * @param seconds
	 * @return
	 */
	public static String setex(String key, String value, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.setex(key, seconds, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static String setex(byte[] key, byte[] value, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.setex(key, seconds, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 同时给多个string赋值，名称为key i的string赋值value i</b>
	 * <p>Description: key1, value1, key2, value2,…key N, value N</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param keysvalues
	 * @return
	 */
	public static String mset(String... keysvalues) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.mset(keysvalues);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static String mset(byte[]... keysvalues) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.mset(keysvalues);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 如果所有名称为key i的string都不存在，则向库中添加string，名称key i赋值为value i</b>
	 * <p>Description: msetnx(key1, value1, key2, value2,…key N, value N)：</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param keysvalues
	 * @return
	 */
	public static Long msetnx(String... keysvalues) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.msetnx(keysvalues);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long msetnx(byte[]... keysvalues) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.msetnx(keysvalues);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/****************************************************************************************************
	 * 
	 * 
	 * List操作
	 * 
	 * 
	 * 
	 ****************************************************************************************************/

	/**
	 * <b>Title: 在名称为key的list尾添加一个值为value的元素</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param value
	 * @return 
	 */
	public static Long rpush(String key, String... value) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.rpush(key, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long rpush(byte[] keyBytes, byte[]... valueBytes) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.rpush(keyBytes, valueBytes);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 在名称为key的list头添加一个值为value的 元素</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param value
	 * @return 
	 */
	public static Long lpush(String key, String... value) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lpush(key, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long lpush(byte[] keyBytes, byte[]... valueBytes) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lpush(keyBytes, valueBytes);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 获取key所有元素</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static List<String> lrange(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lrange(key, 0, -1);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static List<byte[]> lrange(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lrange(key, 0, -1);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回名称为key的list中index位置的元素</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param indexSubscript
	 * @return
	 */
	public static String lindex(String key, long indexSubscript) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lindex(key, indexSubscript);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static byte[] lindex(byte[] key, long indexSubscript) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lindex(key, indexSubscript);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 给名称为key的list中index位置的元素赋值为value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param value
	 * @param indexSubscript
	 * @return
	 */
	public static String lset(String key, String value, long indexSubscript) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lset(key, indexSubscript, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static String lset(byte[] key, byte[] value, long indexSubscript) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lset(key, indexSubscript, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回名称为key的list的长度</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static Long llen(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.llen(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long llen(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.llen(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回并删除名称为key的list中的首元素</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static String lpop(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lpop(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static byte[] lpop(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.lpop(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回并删除名称为key的list中的尾元素</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static String rpop(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.rpop(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static byte[] rpop(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.rpop(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/****************************************************************************************************
	 * 
	 * 
	 * Hash操作
	 * 
	 * 
	 ****************************************************************************************************/

	/**
	 * <b>Title: 添加Map类型的值</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param hash
	 * @return
	 */
	public static Long hset(String key, Map<String, String> hash) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hset(key, hash);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long hset(byte[] key, Map<byte[], byte[]> hash) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hset(key, hash);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 向名称为key的hash中添加元素field i<—>value i</b>
	 * <p>Description: hmset(key, field1, value1,…,field N, value N)</p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param hash
	 * @return
	 */
	public static String hmset(String key, Map<String, String> hash) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hmset(key, hash);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static String hmset(byte[] key, Map<byte[], byte[]> hash) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hmset(key, hash);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 向名称为key的hash中添加元素field<—>value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public static Long hset(String key, String field, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hset(key, field, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long hset(byte[] key, byte[] field, byte[] value) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hset(key, field, value);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回名称为key的hash中field对应的value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public static String hget(String key, String field) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hget(key, field);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static byte[] hget(byte[] key, byte[] field) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hget(key, field);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回名称为key的hash中field i对应的value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	public static List<String> hmget(String key, String... fields) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hmget(key, fields);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static List<byte[]> hmget(byte[] key, byte[]... fields) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hmget(key, fields);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 名称为key的hash中是否存在键为field的域</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public static Boolean hexists(String key, String field) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hexists(key, field);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Boolean hexists(byte[] key, byte[] field) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hexists(key, field);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 删除名称为key的hash中键为field的域</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	public static Long hdel(String key, String... fields) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hdel(key, fields);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long hdel(byte[] key, byte[]... fields) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hdel(key, fields);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回名称为key的hash中元素个数</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static Long hlen(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hlen(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Long hlen(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hlen(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回名称为key的hash中所有键</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static Set<String> hkeys(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hkeys(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Set<byte[]> hkeys(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hkeys(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回名称为key的hash中所有键对应的value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static List<String> hvals(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hvals(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static List<byte[]> hvals(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hvals(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 返回名称为key的hash中所有的键（field）及其对应的value</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @return
	 */
	public static Map<String, String> hgetAll(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hgetAll(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	public static Map<byte[], byte[]> hgetAll(byte[] key) {
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(index);
			return jedis.hgetAll(key);
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e.getMessage());
			LogUtils.error(JedisPoolUtils.class, e);
			return null;
		} finally {
			releaseResource(jedis);
		}
	}

	/**
	 * <b>Title: 增加业务幂等性验证</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean isExist(String key) {
		Jedis jedis = null;
		boolean isExist = false;
		try {
			jedis = getJedisInstance();
			jedis.select(index);// 选择redis库号---不指定库号，默认存入到0号库
			// 如果数据存在则返回0，不存在返回1
			if (jedis.setnx(key, key) == 0) {
				return true;
			}
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e);
			return false;
		} finally {
			// 业务操作完成，将连接返回给连接池
			releaseResource(jedis);
		}
		return isExist;
	}

	/**
	 * <b>Title: 增加业务幂等性验证</b>
	 * <p>Description: </p>
	 * 
	 * @author H.Yang
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean isExist(String key, String value) {
		Jedis jedis = null;
		boolean isExist = false;
		try {
			jedis = getJedisInstance();
			jedis.select(index);// 选择redis库号---不指定库号，默认存入到0号库
			// 如果数据存在则返回0，不存在返回1
			if (jedis.setnx(key, value) == 0) {
				return true;
			}
		} catch (Exception e) {
			handleJedisException(e);
			LogUtils.error(JedisPoolUtils.class, e);
			return false;
		} finally {
			// 业务操作完成，将连接返回给连接池
			releaseResource(jedis);
		}
		return isExist;
	}

}
