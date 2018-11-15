package com.trs.exchange;

import junit.framework.TestCase;

import com.trs.infra.util.CMyException;



public class DataMigrationLoggerTest extends TestCase {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(DataMigrationLoggerTest.class);

    public DataMigrationLoggerTest(String name) {
        super(name);
    }

    public void testDataMigrationLogger() {
        fail("Not yet implemented");
    }

    public void testRecordStartChannel() {
        fail("Not yet implemented");
    }

    public void testRecordEndChannel() {
        fail("Not yet implemented");
    }

    public void testRecordStartData() {
        fail("Not yet implemented");
    }

    public void testRecordEndData() {
        fail("Not yet implemented");
    }

    public void testRecord() {
        fail("Not yet implemented");
    }

    public void testReadLastInfo() {
        try {
            DataMigrationLogger oDataMigrationLogger = new DataMigrationLogger("MySQL", 4);
            assertEquals("��־��¼û��Ǩ����?", -1, oDataMigrationLogger.readLastInfo());
        } catch (Exception ex) {
            logger.error("����[testReadLastInfo]�����쳣��", ex);
            fail("����[testReadLastInfo]�����쳣��\n"
                    + CMyException.getStackTraceText(ex));
        }
        
    }

}
