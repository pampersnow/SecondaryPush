package com.trs.exchange.metadata;

import java.io.File;

import com.trs.TRSWCMBaseTest;
import com.trs.exchange.MyDBManager;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyException;
import com.trs.infra.util.database.MysqlDB;

public class MetaDataExchangeImplTest extends TRSWCMBaseTest {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(MetaDataExchangeImplTest.class);

    public MetaDataExchangeImplTest(String name) {
        super(name);
    }

    /**
     * 测试简单元数据迁移
     */
    public void testCreateMetaViewDatas() {
        MyDBManager oMyDBManager = null;
        try {
            // 设定源数据库的信息
            // 支持的源数据库类型：OracleDB11G OracleDB SQLServerDB MysqlDB
            // SybaseASEWithJtds
            // DB2UDBV9 KingBaseDB
            oMyDBManager = new MyDBManager(
                    "MySQL",
                    "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull",
                    "root", "trsadmin", MysqlDB.class);
            
            WCMFilter oFilterForQuerySrcData = new WCMFilter("MyData", "", "",
                        "MyTitle CTitle, MyContent CContent, MyId DocOutupId");

            // 设定源系统存放文件的信息
            MetaDataExchangeImpl oMetaDataExchange = new MetaDataExchangeImpl(
                    oMyDBManager);
            
            // 目标栏目
            int nDstChannelId = 65;
            
            // 开始迁移
            oMetaDataExchange.createMetaViewDatas(m_oLoginUser,
                    nDstChannelId, oFilterForQuerySrcData, "MyId", true);
        } catch (Exception ex) {
            logger.error("测试[testCreateMetaViewDatas]出现异常！", ex);
            fail("测试[testCreateMetaViewDatas]出现异常！\n"
                    + CMyException.getStackTraceText(ex));
        } finally {
            if (oMyDBManager != null)
                oMyDBManager.close();
        }
    }
    
    
    /**
     * 测试包含视频的元数据迁移
     */
    public void testCreateMetaViewDatasContainMedia() {
        MyDBManager oMyDBManager = null;
        try {
            // 设定源数据库的信息
            // 支持的源数据库类型：OracleDB11G OracleDB SQLServerDB MysqlDB
            // SybaseASEWithJtds
            // DB2UDBV9 KingBaseDB
            oMyDBManager = new MyDBManager(
                    "MySQL",
                    "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull",
                    "root", "trsadmin", MysqlDB.class);
            
            WCMFilter oFilterForQuerySrcData = new WCMFilter("MyData", "", "",
                        "MyTitle CTitle, MyContent CContent, MyId DocOutupId, MyMediaFile MyMediaFile");

            // 设定源系统存放文件的信息
            MetaDataExchangeImpl oMetaDataExchange = new MetaDataExchangeImpl(
                    oMyDBManager);
            
            // 设置MAS相关信息
            int nDirId = 1; // Mas目录地址
            oMetaDataExchange.setMASInfo("http://127.0.0.1:8181/mas/", nDirId);
            
            // 目标栏目
            int nDstChannelId = 56;
            
            // 开始迁移
            oMetaDataExchange.createMetaViewDatas(m_oLoginUser,
                    nDstChannelId, oFilterForQuerySrcData, "MyId", true);
        } catch (Exception ex) {
            logger.error("测试[testCreateMetaViewDatas]出现异常！", ex);
            fail("测试[testCreateMetaViewDatas]出现异常！\n"
                    + CMyException.getStackTraceText(ex));
        } finally {
            if (oMyDBManager != null)
                oMyDBManager.close();
        }
    }
   
}
