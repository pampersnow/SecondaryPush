package com.trs.exchange;

import java.util.ArrayList;
import java.util.List;

import com.trs.TRSWCMBaseTest;
import com.trs.infra.util.CMyException;
import com.trs.infra.util.database.MysqlDB;

public class MyDBManagerTest extends TRSWCMBaseTest {
private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
        .getLogger(MyDBManagerTest.class);
    public MyDBManagerTest(String name) {
        super(name);
    }

    public void testMyDBManager() {
        fail("Not yet implemented");
    }

    public void testGetDbType() {
        fail("Not yet implemented");
    }

    public void testGetSrcNameOfDB() {
        fail("Not yet implemented");
    }

    public void testGetConnection() {
        fail("Not yet implemented");
    }

    public void testFreeConnection() {
        fail("Not yet implemented");
    }

    public void testClose() {
        fail("Not yet implemented");
    }

    public void testGetDBType() {
        fail("Not yet implemented");
    }

    public void testSetParameters() {
        fail("Not yet implemented");
    }

    public void testSqlExecuteQueryString() {
        MyDBManager m_oDBMgr = null;
        try {
            // �趨Դ���ݿ����Ϣ
            // ֧�ֵ�Դ���ݿ����ͣ�OracleDB11G OracleDB SQLServerDB MysqlDB
            // SybaseASEWithJtds
            // DB2UDBV9 KingBaseDB
            m_oDBMgr = new MyDBManager(
                    "MySQL",
                    "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull",
                    "root", "trsadmin", MysqlDB.class);
            
            List lParameters = new ArrayList();
            lParameters.add(new Integer(72));
            String sTitle = m_oDBMgr.sqlExecuteQueryString("select MyTitle from MyData where MyId=?", lParameters) ;
            System.out.println(sTitle);
        } catch (Exception ex) {
            logger.error("����[testSqlExecuteQueryInt]�����쳣��", ex);
            fail("����[testSqlExecuteQueryInt]�����쳣��\n"
                    + CMyException.getStackTraceText(ex));
        } finally {
            if (m_oDBMgr != null)
                m_oDBMgr.close();
        }
    }

    public void testSqlExecuteQueryInt() {
        MyDBManager m_oDBMgr = null;
        try {
            // �趨Դ���ݿ����Ϣ
            // ֧�ֵ�Դ���ݿ����ͣ�OracleDB11G OracleDB SQLServerDB MysqlDB
            // SybaseASEWithJtds
            // DB2UDBV9 KingBaseDB
            m_oDBMgr = new MyDBManager(
                    "MySQL",
                    "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull",
                    "root", "trsadmin", MysqlDB.class);
            
            int nMaxId = m_oDBMgr.sqlExecuteQueryInt("select max(myid) from MyData", null) ;
            System.out.println(nMaxId);
        } catch (Exception ex) {
            logger.error("����[testSqlExecuteQueryInt]�����쳣��", ex);
            fail("����[testSqlExecuteQueryInt]�����쳣��\n"
                    + CMyException.getStackTraceText(ex));
        } finally {
            if (m_oDBMgr != null)
                m_oDBMgr.close();
        }
    }

    public void testSqlExecuteQuery() {
        fail("Not yet implemented");
    }

}
