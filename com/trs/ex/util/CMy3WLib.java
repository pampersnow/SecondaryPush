package com.trs.ex.util;

/**
 * Created:         2001.10
 * Last Modified:   2001.10.12
 * Description:
 *      class  CMy3WLib ——  WWW资源获取对象的定义和实现
 */

/**
 * <p>
 * Title: TRS 内容协作平台（TRS WCM）
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * class CMy3WLib —— WWW资源获取对象的定义和实现
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: www.trs.com.cn
 * </p>
 * 
 * @author TRS信息技术有限公司
 * @version 1.0
 */

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.trs.infra.I18NMessage;
import com.trs.infra.util.BASE64EncoderStream;
import com.trs.infra.util.CMyException;
import com.trs.infra.util.DebugTimer;
import com.trs.infra.util.ExceptionNumber;
import com.trs.infra.util.HttpClientBuddy;
import com.trs.infra.util.ResponseBuddy;

public class CMy3WLib {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(CMy3WLib.class);

    /**
     * 默认的read time out.
     */
    protected static int TIME_OUT = 10000;// 10s

    public static int MAX_TIME = 2000;

    private static Map s_hmProxyInfo = new HashMap(4);

    public CMy3WLib() {
    }

    /**
     * 设置代理服务器信息
     * 
     * @param _host
     *            代理服务器地址
     * @param _port
     *            代理服务器端口号
     * @param _user
     *            代理服务器用户名
     * @param _password
     *            代理服务器密码
     */
    public static void setProxyInfo(String _sKey, String _sValue) {
        s_hmProxyInfo.put(_sKey.toUpperCase(), _sValue);
    }

    /**
     * 访问指定的URL
     * 
     * @param p_urlSrcFile
     *            WEB源文件的URL对象
     * @return 操作成功返回true；否则返回false
     * @throws CMyException
     */
    public static boolean getFile(URL p_urlSrcFile) throws CMyException {
        return getFile(p_urlSrcFile, null);
    }

    /**
     * 获取指定URL的源文件，并保存到本地目标文件
     * <p>
     * 说明：若不指定目标文件，则抛弃获取结果
     * 
     * @param p_urlSrcFile
     *            WEB源文件的URL对象
     * @param p_sDstPathFileName
     *            保存到本地的文件名称（含路径）
     * @return 操作成功返回true；否则返回false
     * @throws CMyException
     */
    public static boolean getFile(URL p_urlSrcFile, String p_sDstPathFileName)
            throws CMyException {
        InputStream isSrcFile = null;

        // 打开源文件数据流
        try {
            DebugTimer timer = new DebugTimer();
            timer.start();

            if (p_urlSrcFile.getProtocol().equalsIgnoreCase("HTTPS")) {
                isSrcFile = openSSLURL(p_urlSrcFile);
            } else {
                URLConnection con = p_urlSrcFile.openConnection();

                System.setProperty("sun.net.client.defaultConnectTimeout",
                        String.valueOf(TIME_OUT));// jdk1.4换成这个,连接超时
                System.setProperty("sun.net.client.defaultReadTimeout",
                        String.valueOf(TIME_OUT)); // jdk1.4换成这个,读操作超时

                con.setConnectTimeout(TIME_OUT);
                con.setReadTimeout(TIME_OUT);

                isSrcFile = con.getInputStream();
                // isSrcFile = p_urlSrcFile.openStream();
            }

            timer.stop();
            if (timer.getTime() >= MAX_TIME) {
                logger.warn("Download the file use[" + timer.getTime()
                        + "]ms! [from=" + p_urlSrcFile.toString() + "]  [to="
                        + p_sDstPathFileName + "]");
            }

        } catch (Exception ex) {
            if (isSrcFile != null)
                try {
                    isSrcFile.close();
                } catch (Exception ex2) {
                }
            throw new CMyException(ExceptionNumber.ERR_NET_OPENSTREAM,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label1",
                            "获取源文件失败(CMy3WLib.getFile)"), ex);
        }// end try

        DataInputStream isFileData = null; // 源文件数据流
        FileOutputStream osFileDst = null; // 输出文件数据流
        byte[] buff = null; // 缓冲区
        int nLen;
        // 获取指定源文件数据，保存到本地目标文件
        try {
            isFileData = new DataInputStream(isSrcFile); // 数据输入流

            if (p_sDstPathFileName != null) {
                osFileDst = new FileOutputStream(p_sDstPathFileName); // 文件输出流
            }

            buff = new byte[2048]; // 缓冲区
            while ((nLen = isFileData.read(buff, 0, buff.length)) != -1) { // 读取数据
                if (osFileDst != null)
                    osFileDst.write(buff, 0, nLen); // 写入数据
            }// end while
        } catch (Exception ex) {
            throw new CMyException(ExceptionNumber.ERR_FILEOP_FAIL,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label2",
                            "保存文件失败(CMy3WLib.getFile)"), ex);
        } finally {
            if (isSrcFile != null)
                try {
                    isSrcFile.close();
                } catch (Exception ex) {
                }
            if (isFileData != null)
                try {
                    isFileData.close();
                } catch (Exception ex) {
                }
            if (osFileDst != null)
                try {
                    osFileDst.close();
                } catch (Exception ex) {
                }
        }// end try

        return true; // OK!
    }

    // ========================add by
    // lpj@2007-8-17=====================================//

    private static InputStream openSSLURL(URL p_urlSrcFile) {
        try {

            URLConnection urlConnection = p_urlSrcFile.openConnection();
            // 如果服务器端证书是收信人则不需要下面的代码
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tm, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            HttpsURLConnection sslConnection = (HttpsURLConnection) urlConnection;
            sslConnection.setSSLSocketFactory(ssf);
            return urlConnection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class MyX509TrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    // ============================end add by
    // lpj=======================================//

    /**
     * 获取指定URL的源文件，并保存到本地目标文件
     * <p>
     * 说明：若不指定目标文件，则抛弃获取结果
     * 
     * @param p_sSrcFileUrl
     *            WEB源文件的URL对象
     * @return 操作成功返回true；否则返回false
     * @throws CMyException
     */
    public static String getURLContent(String p_sSrcFileUrl) throws Exception {
        URL p_urlSrcFile = CMy3WLib.getURL(p_sSrcFileUrl);
        InputStream isSrcFile = null; // 源文件输入流(connection)
        DataInputStream isFileData = null; // 源文件数据流
        FileOutputStream osFileDst = null; // 输出文件数据流
        byte[] buff = null; // 缓冲区
        int nLen;
        StringBuffer strHTML = new StringBuffer();

        // 打开源文件数据流
        try {
            isSrcFile = p_urlSrcFile.openStream();
        } catch (Exception ex) {
            if (isSrcFile != null)
                try {
                    isSrcFile.close();
                } catch (Exception ex2) {
                }
            throw new CMyException(ExceptionNumber.ERR_NET_OPENSTREAM,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label1",
                            "获取源文件失败(CMy3WLib.getFile)"), ex);
        }// end try

        // 获取指定源文件数据，保存到本地目标文件
        try {
            isFileData = new DataInputStream(isSrcFile); // 数据输入流

            buff = new byte[2048]; // 缓冲区
            while ((nLen = isFileData.read(buff, 0, buff.length)) != -1) { // 读取数据
                strHTML.append(new String(buff, 0, nLen));
            }// end while
        } catch (Exception ex) {
            throw new CMyException(ExceptionNumber.ERR_FILEOP_FAIL,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label2",
                            "保存文件失败(CMy3WLib.getFile)"), ex);
        } finally {
            if (isSrcFile != null)
                try {
                    isSrcFile.close();
                } catch (Exception ex) {
                }
            if (isFileData != null)
                try {
                    isFileData.close();
                } catch (Exception ex) {
                }
            if (osFileDst != null)
                try {
                    osFileDst.close();
                } catch (Exception ex) {
                }
        }// end try

        return strHTML.toString(); // OK!
    }

    /**
     * 访WEB链接
     * 
     * @param p_sSrcFileUrl
     *            WEB源文件名
     *            <p>
     *            如：http://images.sohu.com/foxanimal.gif
     *            <p>
     *            ftp://ftpserver/filename.ext
     * @return 操作成功返回true；否则返回false
     * @throws CMyException
     */
    public static boolean getFile(String p_sSrcFileUrl) throws CMyException {
        return getFile(p_sSrcFileUrl, null);
    }

    /**
     * 获取URL对象
     * 
     * @param p_sSrcFileUrl
     *            WEB源文件名
     *            <p>
     *            如：http://images.sohu.com/foxanimal.gif
     *            <p>
     *            ftp://ftpserver/filename.ext
     * @return 链接对象URL
     * @throws Exception
     */
    public static URL getURL(String p_sSrcFileUrl) throws Exception {
        String sProxyHost = (String) s_hmProxyInfo.get("PROXY_HOST");
        if (sProxyHost == null || sProxyHost.length() <= 0)
            return new URL(p_sSrcFileUrl);
        // 设置代理服务器
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", sProxyHost);
        System.getProperties()
                .put("proxyPort", s_hmProxyInfo.get("PROXY_PORT"));

        URL newURL = new URL(p_sSrcFileUrl);

        String sProxyUser = (String) s_hmProxyInfo.get("PROXY_USER");
        // 设置用户名和密码
        if (sProxyUser == null || sProxyUser.length() <= 0)
            return newURL;

        String authString = sProxyUser + ":"
                + s_hmProxyInfo.get("PROXY_PASSWORD");
        String auth = "Basic "
                + new String(BASE64EncoderStream.encode(authString.getBytes()));
        URLConnection conn = newURL.openConnection();
        conn.setRequestProperty("Proxy-Authorization", auth);
        // conn.setReadTimeout(TIME_OUT);
        return newURL;
    }

    /**
     * 获取WEB上文件
     * 
     * @param p_sSrcFileUrl
     *            WEB源文件名
     * @param p_sDstPathFileName
     *            目标文件名（包含路径）
     * @return 操作成功返回true；否则返回false
     * @throws CMyException
     */
    public static boolean getFile(String p_sSrcFileUrl,
            String p_sDstPathFileName) throws CMyException {
        URL urlSrcFile = null;

        // 解析源文件URL
        try {
            urlSrcFile = getURL(p_sSrcFileUrl);
        } catch (Exception ex) {
            throw new CMyException(ExceptionNumber.ERR_URL_MALFORMED,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label3",
                            "无效的源文件地址(CMy3WLib.getFile)"), ex);
        }// endtry

        // 获取指定源文件，并保存到本地目标文件
        return getFile(urlSrcFile, p_sDstPathFileName);
    }// END:getFile

    /**
     * 获取WEB上文件
     * 
     * @param p_sProtocol
     *            网络传输协议。如："http"
     * @param p_sHost
     *            主机地址
     * @param p_sFile
     *            WEB源文件
     * @param p_sDstPathFileName
     *            目标文件名（包含路径）
     * @return 操作成功返回true；否则返回false
     * @throws CMyException
     */
    public static boolean getFile(String p_sProtocol, String p_sHost,
            String p_sFile, String p_sDstPathFileName) throws CMyException {
        return getFile(p_sProtocol, p_sHost, -1, p_sFile, p_sDstPathFileName);
    }

    /**
     * 获取WEB上文件
     * 
     * @param p_sProtocol
     *            网络传输协议。如："http"
     * @param p_sHost
     *            主机地址
     * @param p_nPort
     *            端口号
     * @param p_sFile
     *            源文件名
     * @param p_sDstPathFileName
     *            目标文件名（包含路径）
     * @return 操作成功返回true；否则返回false
     * @throws CMyException
     */
    public static boolean getFile(String p_sProtocol, String p_sHost,
            int p_nPort, String p_sFile, String p_sDstPathFileName)
            throws CMyException {
        URL urlSrcFile = null;

        // 解析源文件URL
        try {
            urlSrcFile = new URL(p_sProtocol, p_sHost, p_nPort, p_sFile);

        } catch (java.net.MalformedURLException ex) {
            throw new CMyException(ExceptionNumber.ERR_URL_MALFORMED,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label3",
                            "无效的源文件地址(CMy3WLib.getFile)"), ex);
        }// endtry

        // 获取指定源文件，并保存到本地目标文件
        return getFile(urlSrcFile, p_sDstPathFileName);
    }// END:getFile

    /**
     * 向指定的sURL发送一个get请求
     * 
     * @param sURL
     *            发送的目标地址
     * @param mContent
     *            向目标地址发送的参数
     */
    public static ResponseBuddy doGet(String sURL, Map mContent)
            throws CMyException {
        HttpClientBuddy hcb = new HttpClientBuddy();
        return hcb.doGet(sURL, mContent);
    }

    /**
     * 向指定的sURL发送一个get请求
     * 
     * @param sURL
     *            发送的目标地址
     * @param sContent
     *            发送的内容
     * @throws CMyException
     */
    public static ResponseBuddy doGet(String sURL, String sContent)
            throws CMyException {
        HttpClientBuddy hcb = new HttpClientBuddy();
        return hcb.doGet(sURL, sContent);
    }

    /**
     * 向指定的sURL发送一个post请求
     * 
     * @param sURL
     *            发送的目标地址
     * @param mParams
     *            向目标地址发送的参数
     */
    public static ResponseBuddy doPost(String sURL, Map mContent)
            throws CMyException {
        return doPost(sURL, mContent, HttpClientBuddy.DEFAULT_CONTENT_ENCODING);
    }

    public static ResponseBuddy doPost(String sURL, String sContent)
            throws CMyException {
        return doPost(sURL, sContent, HttpClientBuddy.DEFAULT_CONTENT_ENCODING);
    }

    /**
     * 
     * @param sURL
     * @param mContent
     * @param sEncoding
     */
    public static ResponseBuddy doPost(String sURL, Map mContent,
            String sEncoding) throws CMyException {
        HttpClientBuddy hcb = new HttpClientBuddy(sEncoding);
        return hcb.doPost(sURL, mContent, false);
    }

    /**
     * 
     * @param sURL
     * @param sContent
     * @param sEncoding
     * @throws CMyException
     */
    public static ResponseBuddy doPost(String sURL, String sContent,
            String sEncoding) throws CMyException {
        HttpClientBuddy hcb = new HttpClientBuddy(sEncoding);
        return hcb.doPost(sURL, sContent.toString(), false);
    }

    public static void setTimeout(int timeout) {
        TIME_OUT = timeout * 1000;
    }

    // ===================================================================
    // 对象测试和示例
    public static void main(String[] args) {
        try {
            // 测试下载flash
            // String sFlashFile = "http://61.156.17.125/swf6/1399.swf";
            // CMy3WLib.getFile( sFlashFile, "d:\\永远的阿根廷.swf" );

            // CMy3WLib.setProxyInfo("192.9.200.7", "8080", "", "");
            // CMy3WLib.setProxyInfo("PROXY_HOST", "192.9.200.7");
            // CMy3WLib.setProxyInfo("PROXY_PORT", "8080");
            // CMy3WLib
            // .getFile(
            // "https://intranet.trs.com.cn/webpic/W0200707/W020070711/W020070711429350287054.jpg"
            // ,
            // "c:\\test.jpg");
            // CMy3WLib.getFile( "http://www.sohu.com.cn/","d:\\sohu.htm" );
            // CMy3WLib.getFile( "ftp://wanghaiyang/test.txt", "d:\\test.txt" );
            // CMy3WLib.getFile( "http", "www.sohu.com.cn", "",
            // "d:\\sohuHomePage.htm" );

            String sUrl = "http://127.0.0.1:9999/wcm/test.jsp";
            URL url = new URL(sUrl);
            System.out.println(url.toString());

            // Map mContent = new HashMap();
            // mContent.put("a",
            // I18NMessage.get(CMy3WLib.class, "CMy3WLib.label4", "中国"));
            // mContent.put("b", "english");
            // doPost(sUrl, mContent);
            // doPost(sUrl, "a=aaa&b=bbb");
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }// end try
    }
}