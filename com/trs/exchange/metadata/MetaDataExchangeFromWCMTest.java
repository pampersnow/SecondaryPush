package com.trs.exchange.metadata;

import com.trs.TRSWCMBaseTest;
import com.trs.exchange.MyDBManager;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyException;
import com.trs.infra.util.database.MysqlDB;
import com.trs.infra.util.database.SQLServerDB;

public class MetaDataExchangeFromWCMTest extends TRSWCMBaseTest {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(MetaDataExchangeFromWCMTest.class);

    public MetaDataExchangeFromWCMTest(String name) {
        super(name);
    }

    public void testCreateMetaViewDatas() {
        MyDBManager oMyDBManager = null;
        try {
            // �趨Դ���ݿ����Ϣ
            // ֧�ֵ�Դ���ݿ����ͣ�OracleDB11G OracleDB SQLServerDB MysqlDB
            // SybaseASEWithJtds
            // DB2UDBV9 KingBaseDB
//            oMyDBManager = new MyDBManager(
//                    "SQLServer",
//                    "jdbc:jtds:sqlserver://192.9.100.217:1433/TRSWCMV6_1071update2",
//                    "sa", "trsadmin", SQLServerDB.class);            
            oMyDBManager = new MyDBManager(
                    "SQLServer",
                    "jdbc:jtds:sqlserver://192.9.100.217:1433/TRSWCMV6_1071update2",
                    "sa", "trsadmin", SQLServerDB.class);
            
            WCMFilter oFilterForQuerySrcData = new WCMFilter("MyData", "", "",
                        "MyTitle CTitle, MyContent CContent, MyId DocOutupId");

            // �趨Դϵͳ����ļ�����Ϣ
            MetaDataExchangeFromWCM oDocumentExchangeFromWCM = new MetaDataExchangeFromWCM(
                    oMyDBManager);
            oDocumentExchangeFromWCM.setWCMDataRootPath("z:/WCMData/");

            // ����Ǩ�Ƶ�����
//            WCMFilter oFilterForQuerySrcData = new WCMFilter(
//                    "WCMDocument",
//                    "DocChannel=25",
//                    "DocId desc",
//                    "DocId DocOutupId, DocType, DocTitle, DocContent, DocHTMLCon, CrUser, CrTime" +
//                    ",DOCSOURCE,DOCSECURITY,DOCABSTRACT,DOCPEOPLE,DOCPLACE,DOCAUTHOR,DOCRELTIME" +
//                    ",DOCPUBURL,DOCPUBTIME,DOCEDITOR,DOCAUDITOR" +
//                    ",DOCPRO,TITLECOLOR,DOCNO,DocFlag,DocLink,AttachPic,HitsCount,DocFileName" +
//                    ",SubDocTitle,OperTime,OperUser,DocSourceName");

            // ��ʼǨ��
            int nDstChannelId = 65;
            oDocumentExchangeFromWCM.createMetaViewDatas(m_oLoginUser,
                    nDstChannelId, oFilterForQuerySrcData, "MyId", true);
        } catch (Exception ex) {
            logger.error("����[testCreateDocuments]�����쳣��", ex);
            fail("����[testCreateDocuments]�����쳣��\n"
                    + CMyException.getStackTraceText(ex));
        } finally {
            if (oMyDBManager != null)
                oMyDBManager.close();
        }

    }

}
