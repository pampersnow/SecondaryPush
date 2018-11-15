package com.trs.ex.util;

/**
 * Created:         2001.10
 * Last Modified:   2001.10.12
 * Description:
 *      class  CMy3WLib ����  WWW��Դ��ȡ����Ķ����ʵ��
 */

/**
 * <p>
 * Title: TRS ����Э��ƽ̨��TRS WCM��
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * class CMy3WLib ���� WWW��Դ��ȡ����Ķ����ʵ��
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001
 * </p>
 * <p>
 * Company: www.trs.com.cn
 * </p>
 * 
 * @author TRS��Ϣ�������޹�˾
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
     * Ĭ�ϵ�read time out.
     */
    protected static int TIME_OUT = 10000;// 10s

    public static int MAX_TIME = 2000;

    private static Map s_hmProxyInfo = new HashMap(4);

    public CMy3WLib() {
    }

    /**
     * ���ô����������Ϣ
     * 
     * @param _host
     *            �����������ַ
     * @param _port
     *            ����������˿ں�
     * @param _user
     *            ����������û���
     * @param _password
     *            �������������
     */
    public static void setProxyInfo(String _sKey, String _sValue) {
        s_hmProxyInfo.put(_sKey.toUpperCase(), _sValue);
    }

    /**
     * ����ָ����URL
     * 
     * @param p_urlSrcFile
     *            WEBԴ�ļ���URL����
     * @return �����ɹ�����true�����򷵻�false
     * @throws CMyException
     */
    public static boolean getFile(URL p_urlSrcFile) throws CMyException {
        return getFile(p_urlSrcFile, null);
    }

    /**
     * ��ȡָ��URL��Դ�ļ��������浽����Ŀ���ļ�
     * <p>
     * ˵��������ָ��Ŀ���ļ�����������ȡ���
     * 
     * @param p_urlSrcFile
     *            WEBԴ�ļ���URL����
     * @param p_sDstPathFileName
     *            ���浽���ص��ļ����ƣ���·����
     * @return �����ɹ�����true�����򷵻�false
     * @throws CMyException
     */
    public static boolean getFile(URL p_urlSrcFile, String p_sDstPathFileName)
            throws CMyException {
        InputStream isSrcFile = null;

        // ��Դ�ļ�������
        try {
            DebugTimer timer = new DebugTimer();
            timer.start();

            if (p_urlSrcFile.getProtocol().equalsIgnoreCase("HTTPS")) {
                isSrcFile = openSSLURL(p_urlSrcFile);
            } else {
                URLConnection con = p_urlSrcFile.openConnection();

                System.setProperty("sun.net.client.defaultConnectTimeout",
                        String.valueOf(TIME_OUT));// jdk1.4�������,���ӳ�ʱ
                System.setProperty("sun.net.client.defaultReadTimeout",
                        String.valueOf(TIME_OUT)); // jdk1.4�������,��������ʱ

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
                            "��ȡԴ�ļ�ʧ��(CMy3WLib.getFile)"), ex);
        }// end try

        DataInputStream isFileData = null; // Դ�ļ�������
        FileOutputStream osFileDst = null; // ����ļ�������
        byte[] buff = null; // ������
        int nLen;
        // ��ȡָ��Դ�ļ����ݣ����浽����Ŀ���ļ�
        try {
            isFileData = new DataInputStream(isSrcFile); // ����������

            if (p_sDstPathFileName != null) {
                osFileDst = new FileOutputStream(p_sDstPathFileName); // �ļ������
            }

            buff = new byte[2048]; // ������
            while ((nLen = isFileData.read(buff, 0, buff.length)) != -1) { // ��ȡ����
                if (osFileDst != null)
                    osFileDst.write(buff, 0, nLen); // д������
            }// end while
        } catch (Exception ex) {
            throw new CMyException(ExceptionNumber.ERR_FILEOP_FAIL,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label2",
                            "�����ļ�ʧ��(CMy3WLib.getFile)"), ex);
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
            // �����������֤��������������Ҫ����Ĵ���
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
     * ��ȡָ��URL��Դ�ļ��������浽����Ŀ���ļ�
     * <p>
     * ˵��������ָ��Ŀ���ļ�����������ȡ���
     * 
     * @param p_sSrcFileUrl
     *            WEBԴ�ļ���URL����
     * @return �����ɹ�����true�����򷵻�false
     * @throws CMyException
     */
    public static String getURLContent(String p_sSrcFileUrl) throws Exception {
        URL p_urlSrcFile = CMy3WLib.getURL(p_sSrcFileUrl);
        InputStream isSrcFile = null; // Դ�ļ�������(connection)
        DataInputStream isFileData = null; // Դ�ļ�������
        FileOutputStream osFileDst = null; // ����ļ�������
        byte[] buff = null; // ������
        int nLen;
        StringBuffer strHTML = new StringBuffer();

        // ��Դ�ļ�������
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
                            "��ȡԴ�ļ�ʧ��(CMy3WLib.getFile)"), ex);
        }// end try

        // ��ȡָ��Դ�ļ����ݣ����浽����Ŀ���ļ�
        try {
            isFileData = new DataInputStream(isSrcFile); // ����������

            buff = new byte[2048]; // ������
            while ((nLen = isFileData.read(buff, 0, buff.length)) != -1) { // ��ȡ����
                strHTML.append(new String(buff, 0, nLen));
            }// end while
        } catch (Exception ex) {
            throw new CMyException(ExceptionNumber.ERR_FILEOP_FAIL,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label2",
                            "�����ļ�ʧ��(CMy3WLib.getFile)"), ex);
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
     * ��WEB����
     * 
     * @param p_sSrcFileUrl
     *            WEBԴ�ļ���
     *            <p>
     *            �磺http://images.sohu.com/foxanimal.gif
     *            <p>
     *            ftp://ftpserver/filename.ext
     * @return �����ɹ�����true�����򷵻�false
     * @throws CMyException
     */
    public static boolean getFile(String p_sSrcFileUrl) throws CMyException {
        return getFile(p_sSrcFileUrl, null);
    }

    /**
     * ��ȡURL����
     * 
     * @param p_sSrcFileUrl
     *            WEBԴ�ļ���
     *            <p>
     *            �磺http://images.sohu.com/foxanimal.gif
     *            <p>
     *            ftp://ftpserver/filename.ext
     * @return ���Ӷ���URL
     * @throws Exception
     */
    public static URL getURL(String p_sSrcFileUrl) throws Exception {
        String sProxyHost = (String) s_hmProxyInfo.get("PROXY_HOST");
        if (sProxyHost == null || sProxyHost.length() <= 0)
            return new URL(p_sSrcFileUrl);
        // ���ô��������
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", sProxyHost);
        System.getProperties()
                .put("proxyPort", s_hmProxyInfo.get("PROXY_PORT"));

        URL newURL = new URL(p_sSrcFileUrl);

        String sProxyUser = (String) s_hmProxyInfo.get("PROXY_USER");
        // �����û���������
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
     * ��ȡWEB���ļ�
     * 
     * @param p_sSrcFileUrl
     *            WEBԴ�ļ���
     * @param p_sDstPathFileName
     *            Ŀ���ļ���������·����
     * @return �����ɹ�����true�����򷵻�false
     * @throws CMyException
     */
    public static boolean getFile(String p_sSrcFileUrl,
            String p_sDstPathFileName) throws CMyException {
        URL urlSrcFile = null;

        // ����Դ�ļ�URL
        try {
            urlSrcFile = getURL(p_sSrcFileUrl);
        } catch (Exception ex) {
            throw new CMyException(ExceptionNumber.ERR_URL_MALFORMED,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label3",
                            "��Ч��Դ�ļ���ַ(CMy3WLib.getFile)"), ex);
        }// endtry

        // ��ȡָ��Դ�ļ��������浽����Ŀ���ļ�
        return getFile(urlSrcFile, p_sDstPathFileName);
    }// END:getFile

    /**
     * ��ȡWEB���ļ�
     * 
     * @param p_sProtocol
     *            ���紫��Э�顣�磺"http"
     * @param p_sHost
     *            ������ַ
     * @param p_sFile
     *            WEBԴ�ļ�
     * @param p_sDstPathFileName
     *            Ŀ���ļ���������·����
     * @return �����ɹ�����true�����򷵻�false
     * @throws CMyException
     */
    public static boolean getFile(String p_sProtocol, String p_sHost,
            String p_sFile, String p_sDstPathFileName) throws CMyException {
        return getFile(p_sProtocol, p_sHost, -1, p_sFile, p_sDstPathFileName);
    }

    /**
     * ��ȡWEB���ļ�
     * 
     * @param p_sProtocol
     *            ���紫��Э�顣�磺"http"
     * @param p_sHost
     *            ������ַ
     * @param p_nPort
     *            �˿ں�
     * @param p_sFile
     *            Դ�ļ���
     * @param p_sDstPathFileName
     *            Ŀ���ļ���������·����
     * @return �����ɹ�����true�����򷵻�false
     * @throws CMyException
     */
    public static boolean getFile(String p_sProtocol, String p_sHost,
            int p_nPort, String p_sFile, String p_sDstPathFileName)
            throws CMyException {
        URL urlSrcFile = null;

        // ����Դ�ļ�URL
        try {
            urlSrcFile = new URL(p_sProtocol, p_sHost, p_nPort, p_sFile);

        } catch (java.net.MalformedURLException ex) {
            throw new CMyException(ExceptionNumber.ERR_URL_MALFORMED,
                    I18NMessage.get(CMy3WLib.class, "CMy3WLib.label3",
                            "��Ч��Դ�ļ���ַ(CMy3WLib.getFile)"), ex);
        }// endtry

        // ��ȡָ��Դ�ļ��������浽����Ŀ���ļ�
        return getFile(urlSrcFile, p_sDstPathFileName);
    }// END:getFile

    /**
     * ��ָ����sURL����һ��get����
     * 
     * @param sURL
     *            ���͵�Ŀ���ַ
     * @param mContent
     *            ��Ŀ���ַ���͵Ĳ���
     */
    public static ResponseBuddy doGet(String sURL, Map mContent)
            throws CMyException {
        HttpClientBuddy hcb = new HttpClientBuddy();
        return hcb.doGet(sURL, mContent);
    }

    /**
     * ��ָ����sURL����һ��get����
     * 
     * @param sURL
     *            ���͵�Ŀ���ַ
     * @param sContent
     *            ���͵�����
     * @throws CMyException
     */
    public static ResponseBuddy doGet(String sURL, String sContent)
            throws CMyException {
        HttpClientBuddy hcb = new HttpClientBuddy();
        return hcb.doGet(sURL, sContent);
    }

    /**
     * ��ָ����sURL����һ��post����
     * 
     * @param sURL
     *            ���͵�Ŀ���ַ
     * @param mParams
     *            ��Ŀ���ַ���͵Ĳ���
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
    // ������Ժ�ʾ��
    public static void main(String[] args) {
        try {
            // ��������flash
            // String sFlashFile = "http://61.156.17.125/swf6/1399.swf";
            // CMy3WLib.getFile( sFlashFile, "d:\\��Զ�İ���͢.swf" );

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
            // I18NMessage.get(CMy3WLib.class, "CMy3WLib.label4", "�й�"));
            // mContent.put("b", "english");
            // doPost(sUrl, mContent);
            // doPost(sUrl, "a=aaa&b=bbb");
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }// end try
    }
}