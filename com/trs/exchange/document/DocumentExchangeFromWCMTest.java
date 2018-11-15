package com.trs.exchange.document;

import com.trs.TRSWCMBaseTest;
import com.trs.exchange.MyDBManager;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyException;
import com.trs.infra.util.database.SQLServerDB;

public class DocumentExchangeFromWCMTest extends TRSWCMBaseTest {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(DocumentExchangeFromWCMTest.class);

    public DocumentExchangeFromWCMTest(String name) {
        super(name);
    }

    public void testCreateDocuments() {
        MyDBManager oMyDBManager = null;
        try {
            // �趨Դ���ݿ����Ϣ
            // ֧�ֵ�Դ���ݿ����ͣ�OracleDB11G OracleDB SQLServerDB MysqlDB
            // SybaseASEWithJtds
            // DB2UDBV9 KingBaseDB
            oMyDBManager = new MyDBManager(
                    "SQLServer",
                    "jdbc:jtds:sqlserver://192.9.100.217:1433/TRSWCMV6_1071update2",
                    "sa", "trsadmin", SQLServerDB.class);

            // �趨Դϵͳ����ļ�����Ϣ
            DocumentExchangeFromWCM oDocumentExchangeFromWCM = new DocumentExchangeFromWCM(
                    oMyDBManager);
            oDocumentExchangeFromWCM.setWCMDataRootPath("z:/WCMData/");

            // ����Ǩ�Ƶ�����
            WCMFilter oFilterForQuerySrcData = new WCMFilter(
                    "WCMDocument",
                    "DocChannel=25",
                    "DocId desc",
                    "DocId DocOutupId, DocType, DocTitle, DocContent, DocHTMLCon, CrUser, CrTime" +
                    ",DOCSOURCE,DOCSECURITY,DOCABSTRACT,DOCPEOPLE,DOCPLACE,DOCAUTHOR,DOCRELTIME" +
                    ",DOCPUBURL,DOCPUBTIME,DOCEDITOR,DOCAUDITOR" +
                    ",DOCPRO,TITLECOLOR,DOCNO,DocFlag,DocLink,AttachPic,HitsCount,DocFileName" +
                    ",SubDocTitle,OperTime,OperUser,DocSourceName");

            // ��ʼǨ��
            int nDstChannelId = 4;
            oDocumentExchangeFromWCM.createDocuments(m_oLoginUser,
                    nDstChannelId, oFilterForQuerySrcData, "DocId", true);
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
