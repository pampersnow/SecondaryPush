package com.trs.web2frame;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;
import com.trs.web2frame.dispatch.Dispatch;
import com.trs.web2frame.eventhandler.ICallbackEventHandler;
import com.trs.web2frame.eventhandler.ICallbackFailureHandler;
import com.trs.web2frame.httpclient.HttpClientBuddy;
import com.trs.web2frame.httpclient.ResponseBuddy;

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

public class WCMServiceCaller {

    public final static ICallbackFailureHandler CALLLBACK_FAILURE_HANDLER_DEFAULT = new ICallbackFailureHandler() {
        public void onFailure(Dispatch _dispatch)
                throws Web2frameClientException {
            ResponseBuddy oResponseBuddy = _dispatch.getResponseBuddy();
            if (isTRSNotLogin(oResponseBuddy)) {
                System.out.println("�û�δ��½.");
            } else if (isResponse500(oResponseBuddy)) {
                System.out.println(_dispatch.getResponseText());
            } else if (isFailure(oResponseBuddy)) {
                System.out.println("StatusCode:"
                        + oResponseBuddy.getStatusCode() + "\n"
                        + _dispatch.getResponseText());
            }
            _dispatch.setFailure(true);
        }
    };

    public static Dispatch UploadFile(String _sFileName) {
        Dispatch oDispatch = null;
        String sFileExt = _sFileName.substring(_sFileName.lastIndexOf('.') + 1);
        try {
            HttpClientBuddy oHttpClientBuddy = new HttpClientBuddy(
                    ServiceConfig.WCM_SERVICE_CHARSET, new String[][] { {
                            "FileExt", sFileExt } });
            ResponseBuddy oResponseBuddy = null;
            oResponseBuddy = oHttpClientBuddy.updateFile(
                    ServiceConfig.WCM_UPLOAD_FILE_URL, CMyFile
                            .readBytesFromFile(_sFileName));
            oDispatch = new Dispatch(oResponseBuddy);
            try {
                if (isTRSNotLogin(oResponseBuddy) || isFailure(oResponseBuddy)) {
                    CALLLBACK_FAILURE_HANDLER_DEFAULT.onFailure(oDispatch);
                }
            } finally {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO �����쳣�����
        }
        return oDispatch;
    }
    
    public static Dispatch UploadFiles(String _sFileName,String ip) {
        Dispatch oDispatch = null;
        String sFileExt = _sFileName.substring(_sFileName.lastIndexOf('.') + 1);
        try {
            HttpClientBuddy oHttpClientBuddy = new HttpClientBuddy(
                    ServiceConfig.WCM_SERVICE_CHARSET, new String[][] { {
                            "FileExt", sFileExt } });
            ResponseBuddy oResponseBuddy = null;
            oResponseBuddy = oHttpClientBuddy.updateFile(
                    "http://" + ip + "/wcm/govfileuploader.do", CMyFile
                            .readBytesFromFile(_sFileName));
            oDispatch = new Dispatch(oResponseBuddy);
            try {
                if (isTRSNotLogin(oResponseBuddy) || isFailure(oResponseBuddy)) {
                    CALLLBACK_FAILURE_HANDLER_DEFAULT.onFailure(oDispatch);
                }
            } finally {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO �����쳣�����
        }
        return oDispatch;
    }

    public static Dispatch uploadFile(String _sFileName, String _sTargetFlag) {
        Dispatch oDispatch = null;
        String sFileExt = _sFileName.substring(_sFileName.lastIndexOf('.') + 1);
        try {
            String sTargetFlag = _sTargetFlag;
            if (CMyString.isEmpty(sTargetFlag)) {
                sTargetFlag = "U0";
            }
            HttpClientBuddy oHttpClientBuddy = new HttpClientBuddy(
                    ServiceConfig.WCM_SERVICE_CHARSET, new String[][] {
                            { "FileExt", sFileExt },
                            { "TargetFlag", _sTargetFlag } });
            ResponseBuddy oResponseBuddy = null;
            oResponseBuddy = oHttpClientBuddy.updateFile(
                    ServiceConfig.WCM_UPLOAD_FILE_URL, CMyFile
                            .readBytesFromFile(_sFileName));
            oDispatch = new Dispatch(oResponseBuddy);
            try {
                if (isTRSNotLogin(oResponseBuddy) || isFailure(oResponseBuddy)) {
                    CALLLBACK_FAILURE_HANDLER_DEFAULT.onFailure(oDispatch);
                }
            } finally {
            }
        } catch (Exception ex) {
            //Ignore.
        }
        return oDispatch;
    }

    public static Dispatch Call(ServiceObject _oServiceObject, boolean _bPost) {
        Dispatch oDispatch = null;
        try {
            HttpClientBuddy oHttpClientBuddy = new HttpClientBuddy(
                    ServiceConfig.WCM_SERVICE_CHARSET);
            ResponseBuddy oResponseBuddy = null;            
            String sQueryURL = ServiceConfig.WCM_SERVICE_URL;
            if(_oServiceObject.getPostData().get("CURRUSER") != null){
                sQueryURL += ("?" + _oServiceObject.getPostData().get("CURRUSER"));
            }
            if (_bPost) {
                oResponseBuddy = oHttpClientBuddy.doPost(
                        sQueryURL, _oServiceObject
                                .toPostXml());
            } else {
                oResponseBuddy = oHttpClientBuddy.doGet(
                        sQueryURL, _oServiceObject
                                .toQueryString());
            }
            oDispatch = new Dispatch(oResponseBuddy);
            try {
                if (isTRSNotLogin(oResponseBuddy)) {
                    _oServiceObject.onFailure(oDispatch);
                }
                if (isFailure(oResponseBuddy)) {
                    _oServiceObject.onFailure(oDispatch);
                } else if (isSuccess(oResponseBuddy)) {
                    _oServiceObject.onSuccess(oDispatch);
                }
            } finally {
                _oServiceObject.onComplete(oDispatch);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO �����쳣�����
        }
        return oDispatch;
    }
    
    public static Dispatch Calls(ServiceObject _oServiceObject, boolean _bPost,String _Ip) {
        Dispatch oDispatch = null;
        try {
            HttpClientBuddy oHttpClientBuddy = new HttpClientBuddy(
                    ServiceConfig.WCM_SERVICE_CHARSET);
            ResponseBuddy oResponseBuddy = null;            
            //String sQueryURL = ServiceConfig.WCM_SERVICE_URL;
            String sQueryURL = "http://" + _Ip + "/wcm/govcenter.do";
            if(_oServiceObject.getPostData().get("CURRUSER") != null){
                sQueryURL += ("?" + _oServiceObject.getPostData().get("CURRUSER"));
            }
            if (_bPost) {
                oResponseBuddy = oHttpClientBuddy.doPost(
                        sQueryURL, _oServiceObject
                                .toPostXml());
            } else {
                oResponseBuddy = oHttpClientBuddy.doGet(
                        sQueryURL, _oServiceObject
                                .toQueryString());
            }
            oDispatch = new Dispatch(oResponseBuddy);
            try {
                if (isTRSNotLogin(oResponseBuddy)) {
                    _oServiceObject.onFailure(oDispatch);
                }
                if (isFailure(oResponseBuddy)) {
                    _oServiceObject.onFailure(oDispatch);
                } else if (isSuccess(oResponseBuddy)) {
                    _oServiceObject.onSuccess(oDispatch);
                }
            } finally {
                _oServiceObject.onComplete(oDispatch);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO �����쳣�����
        }
        return oDispatch;
    }

    private static boolean isTRSNotLogin(ResponseBuddy oResponseBuddy) {
        return "true".equalsIgnoreCase(oResponseBuddy.getHeader("TRSNotLogin"));
    }

    /**
     * @param _responseBuddy
     * @return
     */
    private static boolean isFailure(ResponseBuddy _responseBuddy) {
        return isResponse500(_responseBuddy)
                || _responseBuddy.getStatusCode() > 400;
    }

    /**
     * @param _responseBuddy
     * @return
     */
    private static boolean isSuccess(ResponseBuddy _responseBuddy) {
        return _responseBuddy.getStatusCode() == 200
                && !isFailure(_responseBuddy);
    }

    /**
     * @param oResponseBuddy
     * @return
     */
    private static boolean isResponse500(ResponseBuddy oResponseBuddy) {
        return oResponseBuddy.getStatusCode() == 500
                || "true".equalsIgnoreCase(oResponseBuddy
                        .getHeader("TRSException"));
    }

    public static Dispatch Call(String _sServiceId, String _sMethodName,
            Map _oPostData, boolean _bPost) {
        ServiceObject oServiceObject = new ServiceObject(_sServiceId,
                _sMethodName);
        oServiceObject.setPostData(_oPostData);
        return WCMServiceCaller.Call(oServiceObject, _bPost);
    }
    
    public static Dispatch Calls(String _sServiceId, String _sMethodName,
            Map _oPostData, boolean _bPost,String _Ip) {
        ServiceObject oServiceObject = new ServiceObject(_sServiceId,
                _sMethodName);
        oServiceObject.setPostData(_oPostData);
        return WCMServiceCaller.Calls(oServiceObject, _bPost ,_Ip);
    }

    /**
     * ��
     * 
     * @param _sServiceId
     * @param _sMethodName
     * @param _oPostData
     * @param _arrEventHandlers
     * @param _bPost
     * @return
     */
    public static Dispatch Call(String _sServiceId, String _sMethodName,
            Map _oPostData, ICallbackEventHandler[] _arrEventHandlers,
            boolean _bPost) {
        ServiceObject oServiceObject = new ServiceObject(_sServiceId,
                _sMethodName);
        oServiceObject.setPostData(_oPostData);
        oServiceObject.addEventHandlers(_arrEventHandlers);
        return WCMServiceCaller.Call(oServiceObject, _bPost);
    }

    /**
     * ��������ͬʱ���͵ķ���
     * 
     * @param _sServiceId
     * @param _sServiceName
     * @param _oPostData
     * @param _bPost
     * @return
     */
    public static Dispatch MultiCall(ServiceObject[] _arrServiceObjects) {
        // TODO
        return null;
    }

    /**
     * JSP����
     * 
     * @param _sUrl
     * @param _oPostData
     * @param _bPost
     * @return
     */
    public static Dispatch JspRequest(String _sUrl, Map _oPostData,
            boolean _bPost) {
        // TODO
        return null;
    }

    /**
     * ���������ת����URL��ʽ��QUERYSTRING����
     * 
     * @param _sServiceId
     * @param _sMethodName
     * @param _mapParams
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String makePostParams(String _sServiceId,
            String _sMethodName, Map _mapParams)
            throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();

        sb.append("serviceid").append("=").append(_sServiceId);
        sb.append("&methodname=").append(_sMethodName);

        if (_mapParams != null && _mapParams.size() > 0) {
            Iterator iterParamNames = _mapParams.keySet().iterator();
            for (; iterParamNames.hasNext();) {
                String paramName = (String) iterParamNames.next();
                if (paramName == null) {
                    continue;
                }
                sb.append("&").append(paramName).append("=").append(
                        URLEncoder.encode(_mapParams.get(paramName).toString(),
                                "UTF-8"));
            }
        }
        String result = sb.toString();
        sb.setLength(0);

        return result;
    }

    /**
     * ���������ת����xml�ṹ��POST���
     * 
     * @param _sServiceId
     * @param _sMethodName
     * @param _mapParams
     * @return
     */
    public static String makePostData(String _sServiceId, String _sMethodName,
            Map _mapParams) {
        StringBuffer sb = new StringBuffer();
        sb.append("<post-data>");
        sb.append("<method type=\"" + _sMethodName + "\">" + _sServiceId
                + "</method>");
        if (_mapParams != null && _mapParams.size() > 0) {
            sb.append("<parameters>");
            Iterator iterParamNames = _mapParams.keySet().iterator();
            for (; iterParamNames.hasNext();) {
                String paramName = (String) iterParamNames.next();
                if (paramName == null) {
                    continue;
                }
                sb.append("<" + paramName + ">");
                sb.append("<![CDATA[").append(_mapParams.get(paramName))
                        .append("]]>");
                sb.append("</" + paramName + ">");
            }
            sb.append("</parameters>");
        }
        sb.append("\n</post-data>");
        String result = sb.toString();
        sb.setLength(0);

        return result;
    }

    public static String makeMultiCallPostData(
            ServiceObject[] _arrServiceObjects) {
        // TODO
        return null;
    }
}