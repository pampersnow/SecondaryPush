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
            // 设定源数据库的信息
            // 支持的源数据库类型：OracleDB11G OracleDB SQLServerDB MysqlDB
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

            // 设定源系统存放文件的信息
            MetaDataExchangeFromWCM oDocumentExchangeFromWCM = new MetaDataExchangeFromWCM(
                    oMyDBManager);
            oDocumentExchangeFromWCM.setWCMDataRootPath("z:/WCMData/");

            // 构造迁移的数据
//            WCMFilter oFilterForQuerySrcData = new WCMFilter(
//                    "WCMDocument",
//                    "DocChannel=25",
//                    "DocId desc",
//                    "DocId DocOutupId, DocType, DocTitle, DocContent, DocHTMLCon, CrUser, CrTime" +
//                    ",DOCSOURCE,DOCSECURITY,DOCABSTRACT,DOCPEOPLE,DOCPLACE,DOCAUTHOR,DOCRELTIME" +
//                    ",DOCPUBURL,DOCPUBTIME,DOCEDITOR,DOCAUDITOR" +
//                    ",DOCPRO,TITLECOLOR,DOCNO,DocFlag,DocLink,AttachPic,HitsCount,DocFileName" +
//                    ",SubDocTitle,OperTime,OperUser,DocSourceName");

            // 开始迁移
            int nDstChannelId = 65;
            oDocumentExchangeFromWCM.createMetaViewDatas(m_oLoginUser,
                    nDstChannelId, oFilterForQuerySrcData, "MyId", true);
        } catch (Exception ex) {
            logger.error("测试[testCreateDocuments]出现异常！", ex);
            fail("测试[testCreateDocuments]出现异常！\n"
                    + CMyException.getStackTraceText(ex));
        } finally {
            if (oMyDBManager != null)
                oMyDBManager.close();
        }

    }

}
