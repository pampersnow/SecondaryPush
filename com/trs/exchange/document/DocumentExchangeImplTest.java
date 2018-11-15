package com.trs.exchange.document;

import com.trs.TRSWCMBaseTest;
import com.trs.exchange.MyDBManager;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyException;
import com.trs.infra.util.database.MysqlDB;

public class DocumentExchangeImplTest extends TRSWCMBaseTest {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(DocumentExchangeImplTest.class);

    public DocumentExchangeImplTest(String name) {
        super(name);
    }

    public void testCreateDocuments() {
        try {
            exchangeeDocuments();
        } catch (Exception ex) {
            logger.error("测试[testCreateDocuments]出现异常！", ex);
            fail("测试[testCreateDocuments]出现异常！\n"
                    + CMyException.getStackTraceText(ex));
        }
    }

    private void exchangeeDocuments() throws Exception {

        // 1 设置目标数据库信息
        // 支持的源数据库类型：OracleDB11G OracleDB SQLServerDB MysqlDB SybaseASEWithJtds
        // DB2UDBV9 KingBaseDB
        MyDBManager oMyDBManager = new MyDBManager(
                "MySQL",
                "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull",
                "root", "trsadmin", MysqlDB.class);
        try {
            // 2 构造待迁移数据的查询语句
            // Select MyTitle DocTitle, MyContent DocContent, 10 DocType, MyId DocOutupId
            // from MyData;
            WCMFilter oSrcFilter = new WCMFilter("MyData", "", "",
                    "MyTitle DocTitle, MyContent DocContent, 10 DocType, MyId DocOutupId");

            // 3 利用迁移实现类，开始数据迁移
            DocumentExchangeImpl oDocumentExchangeImpl = new DocumentExchangeImpl(
                    oMyDBManager);
            int nDstChannelId = 4;
            boolean bOrderById = true;
            oDocumentExchangeImpl.createDocuments(m_oLoginUser, nDstChannelId,
                    oSrcFilter, "MyId", bOrderById);
        } finally {
            oMyDBManager.close();
        }

    }

}
