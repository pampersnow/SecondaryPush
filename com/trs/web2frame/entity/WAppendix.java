/**
 * 2008-2-1
 */
package com.trs.web2frame.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trs.infra.util.CMyString;
import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;

/**
 * Title: TRS ����Э��ƽ̨��TRS WCM�� <BR>
 * Description: <BR>
 * TODO <BR>
 * Copyright: Copyright (c) 2004-2005 TRS��Ϣ�������޹�˾ <BR>
 * Company: TRS��Ϣ�������޹�˾(www.trs.com.cn) <BR>
 * 
 * @author TRS��Ϣ�������޹�˾ LY
 * @version 1.0
 */

public class WAppendix {

    private Map m_mpAppendix = null;

    private String m_sUploadFile = null;

    private int m_nAppFlag = 30;

    /** �������ͱ�ʶ���ĵ����� */
    public final static int FLAG_DOCAPD = 10; // �ļ�:Document Appendix

    /** �������ͱ�ʶ���ĵ�ͼƬ */
    public final static int FLAG_DOCPIC = 20; // ͼƬ:Document Picture

    /** �������ͱ�ʶ��HTML */
    public final static int FLAG_HTMLPIC = 30; // HTMLͼƬ:Html Picture

    /** �������ͱ�ʶ�����Ӹ��� */
    public final static int FLAG_LINK = 40; // ����:Link

    /** �������ͱ�ʶ���ĵ����и��� */
    public final static int FLAG_ALL_APD = 50; // �ĵ����и���

    public WAppendix() {
        m_mpAppendix = new HashMap();
    }

    public WAppendix(String _sAppendixFile) {
        this();
        m_sUploadFile = _sAppendixFile;
    }

    public String getUploadFile() {
        return m_sUploadFile;
    }

    public void setAppFlag(int nAppFlag) {
        m_nAppFlag = nAppFlag;
    }

    public int getAppFlag() {
        return m_nAppFlag;
    }

    public void setAppendixMap(Map _mpAppendix) {
        m_mpAppendix = _mpAppendix;
    }

    public void upload() {
        if (getUploadFile() != null) {
            Dispatch oDispatch = WCMServiceCaller.UploadFile(getUploadFile());
            String sFileName = oDispatch.getUploadShowName();
            m_mpAppendix.put("APPFILE", sFileName);
            if (m_mpAppendix.get("APPDESC") == null) {
                m_mpAppendix.put("APPDESC", sFileName);
            }
            if (m_mpAppendix.get("SRCFILE") == null) {
                m_mpAppendix.put("SRCFILE", sFileName);
            }
        }
    }

    public void setFieldValue(String sFieldName, String sFieldValue) {
        m_mpAppendix.put(sFieldName, sFieldValue);
    }

    public String getFieldValue(String sFieldName) {
        return m_mpAppendix.get(sFieldName).toString();
    }

    /**
     * @return
     */
    public String toObjectXml() {
        StringBuffer sbResult = new StringBuffer();
        sbResult.append("<OBJECT ");
        for (Iterator iter = m_mpAppendix.keySet().iterator(); iter.hasNext();) {
            String sFieldName = (String) iter.next();
            String sFieldValue = CMyString.filterForXML(m_mpAppendix.get(
                    sFieldName).toString());
            sbResult.append(sFieldName);
            sbResult.append("=\"");
            sbResult.append(sFieldValue);
            sbResult.append("\" ");
        }
        sbResult.append("/>");
        return sbResult.toString();
    }

    public static void main(String[] args) {
        WAppendix oAppendix = new WAppendix();
        oAppendix.setFieldValue("Id", "0");
        oAppendix.setFieldValue("APPFILE", "abcdef.jpg");
        oAppendix.setFieldValue("SRCFILE", "abcdef.jpg");
        oAppendix.setFieldValue("APPFLAG", "20");
        oAppendix.setFieldValue("APPDESC", "abcdef.jpg");
        System.out.println(oAppendix.toObjectXml());
    }
}