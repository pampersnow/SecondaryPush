package com.trs.exchange.mas;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.trs.infra.common.WCMException;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.HttpClientBuddy;
import com.trs.infra.util.ResponseBuddy;

public class MediaHelper {
    protected static int TIME_OUT = 2000;// 2s
    
    private static String URI_MIGRATE = "sysapi/migrate.do";
    
    private final static String KEY_MAS_ID = "masId";
    private final static String KEY_ERROR_INFO = "err";

    public static int createMedia(String _sURLOfMASApp, int _nDirId,
            String _sFileName) throws Exception {

        StringBuffer sbURL = new StringBuffer();
        sbURL.append(CMyString.setStrEndWith(_sURLOfMASApp, '/'));
        sbURL.append(URI_MIGRATE);
        
        StringBuffer sbParameters = new StringBuffer();
        try {            
            sbParameters.append("dirId=").append(_nDirId);
            sbParameters.append("&filePath=").append(
                    URLEncoder.encode(_sFileName, "UTF-8"));      
        } catch (Exception e) {
            throw new WCMException("Fail to save media! MAS������Ч��[�����ַ="+sbURL+"��"+sbParameters+"]", e);
        }
        
        // �ƺ���Ч��
//        System.setProperty("sun.net.client.defaultConnectTimeout",
//                String.valueOf(TIME_OUT));// jdk1.4�������,���ӳ�ʱ
//        System.setProperty("sun.net.client.defaultReadTimeout",
//                String.valueOf(TIME_OUT)); // jdk1.4�������,��������ʱ
        
        HttpClientBuddy oHttpClientBuddy = new HttpClientBuddy("UTF-8");        
        ResponseBuddy oResponseBuddy = oHttpClientBuddy.doGet(
                sbURL.toString(), sbParameters.toString());
        if (oResponseBuddy.getStatusCode() != 200) {
            throw new Exception("ָ����MAS��URL��Ч��[�����ַ=" + sbURL + "?"
                    + sbParameters + "]");
        }
        
        String sResponseText = oResponseBuddy.getBodyAsString();
        Map hResult = null;
        try {
            ObjectMapper oJSONMap = new ObjectMapper();
            hResult = (Map)oJSONMap.readValue(sResponseText, Map.class);
        } catch (Exception e) {
            throw new WCMException("MAS����ֵ������Ԥ�ڣ�[�����ַ=" + sbURL + "?"
                    + sbParameters + "][" + sResponseText + "]", e);
        }
        
        if (hResult.containsKey(KEY_ERROR_INFO)) {
            throw new WCMException("��MAS���������쳣��[�����ַ=" + sbURL + "?"
                    + sbParameters + "][err:" + hResult.get(KEY_ERROR_INFO) + "]");
        }
        if(!hResult.containsKey(KEY_MAS_ID)){
            throw new WCMException("Mas�ı�Լ���Ĺ���[�����ַ=" + sbURL + "?"
                    + sbParameters + "]��������Ϊ"+sResponseText);
        }
        
        int nMasId = Integer.parseInt(hResult.get(KEY_MAS_ID).toString());
        if (nMasId == -1) {
            throw new WCMException("�ύ��Ƶ���ɹ���[�����ַ=" + sbURL + "?"
                    + sbParameters + "]������ϢΪ��\n"
                    + hResult.get(KEY_ERROR_INFO));
        }

        return nMasId;        
    }

    public static void main(String[] args) {
        try {
            String sResponseText = "{\"masId\":10,\"originFilename\":\"XXX.mkv\"}";
            ObjectMapper oJSONMap = new ObjectMapper();
            Map hResult = (Map) oJSONMap.readValue(sResponseText, Map.class);
            Iterator itKeys = hResult.keySet().iterator();
            while (itKeys.hasNext()) {
                String sKey = (String) itKeys.next();
                System.out.println(sKey);
                System.out.println(hResult.get(sKey));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
