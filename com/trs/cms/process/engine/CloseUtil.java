/*
 * Title: 	  TRS 身份服务器
 * Copyright: Copyright (c) 2004-2005, TRS信息技术有限公司. All rights reserved.
 * License:   see the license file.
 * Company:   TRS信息技术有限公司(www.trs.com.cn)
 * 
 * Created on 2004-12-3
 */
package com.trs.cms.process.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * 调试相关的工具类. <BR>
 * @author TRS信息技术有限公司
 */
public class CloseUtil {

    private static final Logger LOG = Logger.getLogger(CloseUtil.class);

    /**
     * 关闭给定的输入流. <BR>
     * 为调用者方便起见, 不再抛出异常. 这也意味着调用方不关心是否有异常, 该方法在最终清理资源时比较有用, 因为那时已经不关心异常了.
     * @param inStream
     */
    public static void closeInputStream(InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {
                LOG.error("error on close the inputstream.", e);
            }
        }
    }

    /**
     * 关闭给定的输出流. <BR>
     * 为调用者方便起见, 不再抛出异常. 这也意味着调用方不关心是否有异常, 该方法在最终清理资源时比较有用, 因为那时已经不关心异常了.
     * @param outStream
     */
    public static void closeOutputStream(OutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                LOG.error("error on close the outputstream.", e);
            }
        }
    }
    
    /**
     * 关闭给定的输出流. <BR>
     * 为调用者方便起见, 不再抛出异常. 这也意味着调用方不关心是否有异常, 该方法在最终清理资源时比较有用, 因为那时已经不关心异常了.
     */
    public static void closeWriter(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                LOG.error("fail close writer=" + writer, e);
            }
        }
    }
    
    /**
     * 关闭给定的输入流. <BR>
     * 为调用者方便起见, 不再抛出异常. 这也意味着调用方不关心是否有异常, 该方法在最终清理资源时比较有用, 因为那时已经不关心异常了.
     */
    public static void closeReader(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.error("fail close reader=" + reader, e);
            }
        }
    }

    /**
     * 关闭给定的Socket.
     * @param socket 给定的Socket
     */
    public static void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                LOG.error("fail on close socket: " + socket, e);
            }
        }
    }
}