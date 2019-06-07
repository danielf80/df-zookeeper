package com.df.zk;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkWatcher {

	private static Logger logger;
	public static void main(String[] args) {
		
		logger = LoggerFactory.getLogger(ZkWatcher.class);
		
		
		String zookeeperUrl = StringUtils.defaultIfBlank(System.getenv("ZOOKEEPER_URL"), "127.0.0.1:2181");
		ZkConnect connector = new ZkConnect();
        try {
			ZooKeeper zk = connector.connect(zookeeperUrl);
			
			for (int c = 0; c < 20 && zk.getState() != ZooKeeper.States.CONNECTED; c++) {
				Thread.sleep(500);
				if (zk.getState() == ZooKeeper.States.AUTH_FAILED ||
					zk.getState() == ZooKeeper.States.CLOSED)
					break;
			}
			
			while (zk.getState() == ZooKeeper.States.CONNECTED) {
				List<String> zNodes = zk.getChildren("/", true);
				for (String zNode: zNodes)
				{
					zNode = "/" + zNode;
					logger.debug("Getting data from: {}", zNode);
					
					byte[] rawdata = zk.getData(zNode, true, zk.exists(zNode, true));
					String data = new String(rawdata);
					logger.info("{}='{}'", zNode, data);
				}
				
				Thread.sleep(1000 * 30);
			}
			logger.warn("ZooKeeper not ready: {}", zk.getState());
		} catch (Exception e) {
			logger.error("Error connecting to ZooKeeper service: {}", zookeeperUrl);
		}
	}

}
