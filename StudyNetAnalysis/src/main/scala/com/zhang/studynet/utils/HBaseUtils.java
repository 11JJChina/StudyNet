package com.zhang.studynet.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;

public class HBaseUtils {

    private Configuration configuration = null;
    private Connection connection = null;
    private static HBaseUtils instance = null;

    private HBaseUtils(){
        try {
            configuration = new Configuration();
            //指定要访问的zk服务器
            configuration.set("hbase.zookeeper.quorum", "hadoop01:2181");
            //得到Hbase连接
            connection = ConnectionFactory.createConnection(configuration);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获得HBase连接实例
     */

    public static synchronized HBaseUtils getInstance(){
        if(instance == null){
            instance = new HBaseUtils();
        }
        return instance;
    }

    /**
     *由表名得到一个表的实例
     * @param tableName
     * @return
     */
    public HTable getTable(String tableName) {
        HTable hTable = null;
        try {
            hTable = (HTable)connection.getTable(TableName.valueOf(tableName));
        }catch (Exception e){
            e.printStackTrace();
        }
        return hTable;
    }





}
