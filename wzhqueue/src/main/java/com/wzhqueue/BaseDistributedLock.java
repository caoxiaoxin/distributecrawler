package com.wzhqueue;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class BaseDistributedLock implements DistributedLock {

	private static Logger logger = LoggerFactory.getLogger(BaseDistributedLock.class);

	private ZooKeeper zooKeeper;
	private String rootPath;
	private String lockNamePre;
	private String currentLockPath;
	private static int MAX_RETRY_COUNT = 10;
	

	private void init() {
		try {
			Stat stat = zooKeeper.exists(rootPath, false);
			if (stat == null) {
				zooKeeper.create(rootPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (Exception e) {
			logger.error("create rootPath error", e);
		}
	}

	private String getLockNodeNumber(String str, String lockName) {
		int index = str.lastIndexOf(lockName);
		if (index >= 0) {
			index += lockName.length();
			return index <= str.length() ? str.substring(index) : "";
		}
		return str;
	}

	private List<String> getSortedChildren() throws Exception {
		List<String> children = zooKeeper.getChildren(rootPath, false);
		if (children != null && !children.isEmpty()) {
			Collections.sort(children, new Comparator<String>() {
				public int compare(String lhs, String rhs) {
					return getLockNodeNumber(lhs, lockNamePre).compareTo(getLockNodeNumber(rhs, lockNamePre));
				}
			});
		}
		logger.info("sort childRen:{}", children);
		return children;
	}

	private void deleteLockNode() {
		try {
			zooKeeper.delete(currentLockPath, -1);
		} catch (Exception e) {
			logger.error("unLock error", e);

		}
	}


	private boolean waitToLock(long startMillis, Long millisToWait) throws Exception {

		boolean haveTheLock = false;
		boolean doDelete = false;

		try {
			while (!haveTheLock) {
				logger.info("get Lock Begin");
				List<String> children = getSortedChildren();
				String sequenceNodeName = currentLockPath.substring(rootPath.length() + 1);

				int ourIndex = children.indexOf(sequenceNodeName);

				if (ourIndex < 0) {
					logger.error("not find node:{}", sequenceNodeName);
					throw new Exception("ڵûҵ: " + sequenceNodeName);
				}


				boolean isGetTheLock = ourIndex == 0;

				String pathToWatch = isGetTheLock ? null : children.get(ourIndex - 1);

				if (isGetTheLock) {
					logger.info("get the lock,currentLockPath:{}", currentLockPath);
					haveTheLock = true;
				} else {
					String previousSequencePath = rootPath.concat("/").concat(pathToWatch);
					final CountDownLatch latch = new CountDownLatch(1);
					final Watcher previousListener = new Watcher() {
						public void process(WatchedEvent event) {
							if (event.getType() == EventType.NodeDeleted) {
								latch.countDown();
							}
						}
					};

					zooKeeper.exists(previousSequencePath, previousListener);

					if (millisToWait != null) {
						millisToWait -= (System.currentTimeMillis() - startMillis);
						startMillis = System.currentTimeMillis();
						if (millisToWait <= 0) {
							doDelete = true; // timed out - delete our node
							break;
						}

						latch.await(millisToWait, TimeUnit.MICROSECONDS);
					} else {
						latch.await();
					}
				}
			}
		} catch (Exception e) {
			logger.error("waitToLock exception", e);
			doDelete = true;
			throw e;
		} finally {
			if (doDelete) {
				deleteLockNode();
			}
		}
		logger.info("get Lock end,haveTheLock=" + haveTheLock);
		return haveTheLock;
	}

	private String createLockNode(String path) throws Exception {
		Stat stat = zooKeeper.exists(rootPath, false);
		// жһ¸Ŀ¼Ƿ
		if (stat == null) {
			zooKeeper.create(rootPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		return zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
	}


	protected Boolean attemptLock(long time, TimeUnit unit) throws Exception {
		final long startMillis = System.currentTimeMillis();
		final Long millisToWait = (unit != null) ? unit.toMillis(time) : null;

		boolean hasTheLock = false;
		boolean isDone = false;
		int retryCount = 0;

		while (!isDone) {
			isDone = true;
			try {
				currentLockPath = createLockNode(rootPath.concat("/").concat(lockNamePre));
				hasTheLock = waitToLock(startMillis, millisToWait);

			} catch (Exception e) {
				if (retryCount++ < MAX_RETRY_COUNT) {
					isDone = false;
				} else {
					throw e;
				}
			}
		}

		return hasTheLock;
	}
}