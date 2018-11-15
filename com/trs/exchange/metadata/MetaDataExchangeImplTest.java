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
     * ���Լ�Ԫ����Ǩ��
     */
    public void testCreateMetaViewDatas() {
        MyDBManager oMyDBManager = null;
        try {
            // �趨Դ���ݿ����Ϣ
            // ֧�ֵ�Դ���ݿ����ͣ�OracleDB11G OracleDB SQLServerDB MysqlDB
            // SybaseASEWithJtds
            // DB2UDBV9 KingBaseDB
            oMyDBManager = new MyDBManager(
                    "MySQL",
                    "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull",
                    "root", "trsadmin", MysqlDB.class);
            
            WCMFilter oFilterForQuerySrcData = new WCMFilter("MyData", "", "",
                        "MyTitle CTitle, MyContent CContent, MyId DocOutupId");

            // �趨Դϵͳ����ļ�����Ϣ
            MetaDataExchangeImpl oMetaDataExchange = new MetaDataExchangeImpl(
                    oMyDBManager);
            
            // Ŀ����Ŀ
            int nDstChannelId = 65;
            
            // ��ʼǨ��
            oMetaDataExchange.createMetaViewDatas(m_oLoginUser,
                    nDstChannelId, oFilterForQuerySrcData, "MyId", true);
        } catch (Exception ex) {
            logger.error("����[testCreateMetaViewDatas]�����쳣��", ex);
            fail("����[testCreateMetaViewDatas]�����쳣��\n"
                    + CMyException.getStackTraceText(ex));
        } finally {
            if (oMyDBManager != null)
                oMyDBManager.close();
        }
    }
    
    
    /**
     * ���԰�����Ƶ��Ԫ����Ǩ��
     */
    public void testCreateMetaViewDatasContainMedia() {
        MyDBManager oMyDBManager = null;
        try {
            // �趨Դ���ݿ����Ϣ
            // ֧�ֵ�Դ���ݿ����ͣ�OracleDB11G OracleDB SQLServerDB MysqlDB
            // SybaseASEWithJtds
            // DB2UDBV9 KingBaseDB
            oMyDBManager = new MyDBManager(
                    "MySQL",
                    "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull",
                    "root", "trsadmin", MysqlDB.class);
            
            WCMFilter oFilterForQuerySrcData = new WCMFilter("MyData", "", "",
                        "MyTitle CTitle, MyContent CContent, MyId DocOutupId, MyMediaFile MyMediaFile");

            // �趨Դϵͳ����ļ�����Ϣ
            MetaDataExchangeImpl oMetaDataExchange = new MetaDataExchangeImpl(
                    oMyDBManager);
            
            // ����MAS�����Ϣ
            int nDirId = 1; // MasĿ¼��ַ
            oMetaDataExchange.setMASInfo("http://127.0.0.1:8181/mas/", nDirId);
            
            // Ŀ����Ŀ
            int nDstChannelId = 56;
            
            // ��ʼǨ��
            oMetaDataExchange.createMetaViewDatas(m_oLoginUser,
                    nDstChannelId, oFilterForQuerySrcData, "MyId", true);
        } catch (Exception ex) {
            logger.error("����[testCreateMetaViewDatas]�����쳣��", ex);
            fail("����[testCreateMetaViewDatas]�����쳣��\n"
                    + CMyException.getStackTraceText(ex));
        } finally {
            if (oMyDBManager != null)
                oMyDBManager.close();
        }
    }
   
}
