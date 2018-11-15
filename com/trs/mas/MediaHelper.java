package com.trs.mas;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.trs.components.video.VSConfig;
import com.trs.infra.common.WCMException;
import com.trs.infra.util.HttpClientBuddy;
import com.trs.infra.util.ResponseBuddy;

public class MediaHelper {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(MediaHelper.class);

    private static String METHOD_NAME_GET_MEDIAINFO = "exQuery";

    public final static String KEY_MAS_ID = "masId";

    public final static String KEY_ERROR_INFO = "err";

    public static String getMasAppKey() {
        return VSConfig.getAppKey();
    }

    public static Map[] getMediaInfo(String _sURLOfMASApp, String _sAppKey,
            String _sMediaIds) throws Exception {
        StringBuffer sbParameters = new StringBuffer();
        sbParameters.append("method=").append(METHOD_NAME_GET_MEDIAINFO);
        sbParameters.append("&id=").append(_sMediaIds);
        sbParameters.append("&appKey=").append(_sAppKey);

        if (logger.isDebugEnabled()) {
            logger.debug(_sURLOfMASApp + "?" + sbParameters);
        }

        String sResponseText = null;
        HttpClientBuddy oHttpClientBuddy = new HttpClientBuddy("UTF-8");
        ResponseBuddy oResponseBuddy = oHttpClientBuddy.doGet(
                _sURLOfMASApp.toString(), sbParameters.toString());
        if (oResponseBuddy.getStatusCode() != 200) {
            throw new Exception("指定的MAS的URL无效？[服务地址=" + _sURLOfMASApp + "?"
                    + sbParameters + "]");
        }
        sResponseText = oResponseBuddy.getBodyAsString();
        // MAS支持多个数据，为了适应老服务，先尝试探测一下类型
        // add by caohui@2013-6-15 下午6:50:52
        Map[] pResult = null;
        try {
            ObjectMapper oJSONMap = new ObjectMapper();
            pResult = (Map[]) oJSONMap.readValue(sResponseText, Map[].class);
        } catch (Exception e) {
            // MAS支持多个数据，为了适应老服务，先尝试探测一下类型
            // add by caohui@2013-6-15 下午6:50:52
            try {
                ObjectMapper oJSONMap = new ObjectMapper();
                pResult = new Map[] { (Map) oJSONMap.readValue(sResponseText,
                        Map.class) };
            } catch (Exception e2) {
                throw new WCMException("MAS返回值不符合预期？[服务地址=" + _sURLOfMASApp
                        + "?" + sbParameters + "][" + sResponseText + "]", e);
            }
        }

        if (pResult == null || pResult.length <= 0) {
            throw new WCMException("MAS返回值不符合预期？[服务地址=" + _sURLOfMASApp + "?"
                    + sbParameters + "][" + sResponseText + "]");
        }

        Map hResult = pResult[0];
        if (hResult.containsKey(KEY_ERROR_INFO)) {
            throw new WCMException("和MAS交互出现异常？[服务地址=" + _sURLOfMASApp + "?"
                    + sbParameters + "][err:" + hResult.get(KEY_ERROR_INFO)
                    + "]");
        }
        if (!hResult.containsKey(KEY_MAS_ID)) {
            throw new WCMException("Mas改变约定的规则！[服务地址=" + _sURLOfMASApp + "?"
                    + sbParameters + "]返回内容为" + sResponseText);
        }

        return pResult;
    }

    public static Map getMediaInfo(String _sURLOfMASApp, String _sAppKey,
            int _nMediaId) throws Exception {
        Map[] pResult = getMediaInfo(_sURLOfMASApp, _sAppKey,
                String.valueOf(_nMediaId));
        return pResult[0];
    }

    final static String CACHE_KEY = "MASObjects";

}
