package com.trs.exchange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import com.trs.infra.support.file.FilesMan;
import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;

public class DataMigrationLogger {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(DataMigrationLogger.class);

    private String m_sLogFileName = null;

    private String m_sCharset = "UTF-8";

    private final static String CHANNEL_START = "Begin Migration Channel to [";

    private final static String CHANNEL_END = "End Migration Channel to [";

    private final static String DATA_START = "Begin Migration Data [";

    private final static String DATA_END = "End Migration Data [";
    
    private final static String FLAG_LOG = "[ExchangeLog]";

    public DataMigrationLogger(String _sSrcNameOfDB, int _nDstChannelId)
            throws Exception {
        FilesMan filesMan = FilesMan.getFilesMan();
        String sLogFilePath = filesMan.getPathConfigValue(FilesMan.FLAG_NORMAL,
                FilesMan.PATH_LOCAL);
        sLogFilePath = CMyString
                .setStrEndWith(sLogFilePath, File.separatorChar);
        m_sLogFileName = sLogFilePath + "exchange_" + _sSrcNameOfDB + "_"
                + _nDstChannelId + ".log";

        PatternLayout layout = new PatternLayout(
                FLAG_LOG + "%m - %d - %-5p %x  - %c%l -%-4r [%t] %n");
        WriterAppender appender = new WriterAppender(layout,
                new FileOutputStream(m_sLogFileName, true));
        appender.setEncoding(m_sCharset);
        logger.removeAllAppenders();
        logger.addAppender(appender);

        System.out.println("Log File:" + m_sLogFileName);
    }

    public void recordStartChannel(int _nChannelId) {
        record(CHANNEL_START, _nChannelId);
    }

    public void recordEndChannel(int _nChannelId) {
        record(CHANNEL_END, _nChannelId);
    }

    public void recordStartData(int _nDataId) {
        record(DATA_START, _nDataId);
    }

    public void recordEndData(int _nDataId) {
        record(DATA_END, _nDataId);
    }

    public void record(String _sLogPre, int _nId) {
        logger.info(_sLogPre + _nId + "]");
    }

    public int readLastInfo() throws Exception {
        if (!CMyFile.fileExists(m_sLogFileName))
            return 0;

        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(m_sLogFileName, "r");
            long len = rf.length();
            long start = rf.getFilePointer();
            long nextend = start + len - 1;
            String line;
            int c = -1;
            while (nextend >= start) {
                rf.seek(nextend);
                c = rf.read();
                if (c != '\n' && c != '\r'  && c != 32) {
                    nextend--;
                    continue;
                }
                
                nextend--;
                rf.seek(nextend);                
                c = rf.read();          
                line = "";
                while (c != '\n' && c != '\r') {
                    line = ((char)c) + line;
                    
                    if(nextend==0)break;
                    
                    nextend--;
                    rf.seek(nextend);                
                    c = rf.read();
                }
               
                if (line == null || !line.startsWith(FLAG_LOG))
                    continue;
                
                line = line.substring(FLAG_LOG.length());

                line = new String(line.getBytes(), m_sCharset).trim();

                System.out.println("find");
                System.out.println(line);
                // 刚刚迁移就停掉了
                if (line.startsWith(CHANNEL_START)) {
                    return 0;
                }
                // 已迁移完
                else if (line.startsWith(CHANNEL_END)) {
                    return -1;
                }
                // 刚开始迁移文档
                else if (line.startsWith(DATA_START)) {
                    int nPos = line.indexOf(']', DATA_START.length());
                    String sTemp = line.substring(DATA_START.length(), nPos)
                            .trim();
                    return Integer.parseInt(sTemp);
                }
                // 已迁移完某篇文档
                else if (line.startsWith(DATA_END)) {
                    int nPos = line.indexOf(']', DATA_END.length());
                    String sTemp = line.substring(DATA_END.length(), nPos)
                            .trim();
                    return Integer.parseInt(sTemp) + 1;
                }

                throw new Exception("未知的日志[content=" + line + "]");

            }
        } catch (Exception e) {
            throw new Exception("读日志文件出现异常![" + m_sLogFileName + "]", e);
        }

        finally {
            try {
                if (rf != null)
                    rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

}
