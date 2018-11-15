package com.trs.mas;

import java.util.Iterator;
import java.util.Map;

import org.apache.batik.dom.util.HashTable;

import com.trs.cms.content.CMSBaseObjs;
import com.trs.cms.content.DefaultObj;
import com.trs.cms.content.DefaultObjs;
import com.trs.components.common.publish.domain.publisher.PublishTagContext;
import com.trs.components.common.publish.domain.tagparser.BaseTagParser4List;
import com.trs.components.video.VSConfig;
import com.trs.infra.common.WCMException;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.DebugTimer;

public class TagParserForMASS extends BaseTagParser4List {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(TagParserForMASS.class);

    public TagParserForMASS() {
    }

    protected CMSBaseObjs findObjects(PublishTagContext _context)
            throws WCMException {
        String sIds = _context.getAttributeTrim("IDS");
        if (sIds == null || sIds.length() == 0)
            return null;

        int[] pMasIds = CMyString.splitToInt(sIds, ",");
        String sExcludeIds = makeNotCacheIds(_context, pMasIds);
        // 第一次请求需要初始化
        if (sExcludeIds != null) {
            initCache(_context, sExcludeIds);            
        }
        
        // 从Cache构造集合
        return makeObjectsFromCache(_context, pMasIds);
    }
    
    private DefaultObjs makeObjectsFromCache(PublishTagContext _context, int[] _pCachcIds)
            throws WCMException {
        DefaultObjs objs = new DefaultObjs(null);
        objs.setPageSize(300);
        
        HashTable hMasObjects = (HashTable) _context.getPageContext()
                .getTaskContext().getCachObject(MediaHelper.CACHE_KEY);
        for (int i = 0; i < _pCachcIds.length; i++) {
            int nMasId = _pCachcIds[i];
            Integer iObjectKey = new Integer(nMasId);
            DefaultObj oValue = (DefaultObj)hMasObjects.get(iObjectKey);
            if (oValue == null) {
                continue;
            }
            
            objs.addElement(oValue);            
        }
        
        return objs;
    }

    private void initCache(PublishTagContext _context, String _sCacheIds)
            throws WCMException {
        HashTable hMasObjects = (HashTable) _context.getPageContext()
                .getTaskContext().getCachObject(MediaHelper.CACHE_KEY);
        Map[] pMediaInfo = getMediaInfo(_context, _sCacheIds);
        for (int i = 0; i < pMediaInfo.length; i++) {
            Map hMediaInfo = pMediaInfo[i];
            int nMasId = Integer.parseInt(hMediaInfo
                    .get(MediaHelper.KEY_MAS_ID).toString());
            Integer iObjectKey = new Integer(nMasId);
            Object oValue = hMasObjects.get(iObjectKey);
            if (oValue != null) {
                continue;
            }

            // 将获取到的信息转存到WCM对象中
            DefaultObj oDefaultObj = new DefaultObj();
            Iterator itKeys = hMediaInfo.keySet().iterator();
            while (itKeys.hasNext()) {
                String sKey = itKeys.next().toString();
                oDefaultObj.setProperty(sKey.toUpperCase(),
                        hMediaInfo.get(sKey), false);
            }
            oDefaultObj.setAddMode(true);

            // 将新构造的对象放入到Cache中
            hMasObjects.put(iObjectKey, oDefaultObj);
        }

    }

    protected boolean isValidate(PublishTagContext _context)
            throws WCMException {
        return true;
    }

    private String makeNotCacheIds(PublishTagContext _context, int[] _pMasIds)
            throws WCMException {
        StringBuffer sbNotCacheIds = new StringBuffer();
        for (int i = 0; i < _pMasIds.length; i++) {
            int nMasId = _pMasIds[i];
            if (nMasId <= 0)
                continue;

            // 2 从TaskContext的Cache中查看是否已经请求过
            HashTable hMasObjects = (HashTable) _context.getPageContext()
                    .getTaskContext().getCachObject(MediaHelper.CACHE_KEY);
            if (hMasObjects == null) {
                hMasObjects = new HashTable();
                _context.getPageContext().getTaskContext()
                        .putInCache(MediaHelper.CACHE_KEY, hMasObjects);
            }

            Integer iObjectKey = new Integer(nMasId);
            if (hMasObjects.get(iObjectKey) != null)
                continue;

            sbNotCacheIds.append(nMasId);
            sbNotCacheIds.append(',');
        }
        if (sbNotCacheIds.length() <= 0)
            return null;

        sbNotCacheIds.setLength(sbNotCacheIds.length() - 1);
        return sbNotCacheIds.toString();
    }

    private Map[] getMediaInfo(PublishTagContext _context, String _sMasIds)
            throws WCMException {
        // 获取一些逻辑控制属性
        // 1 尝试次数
        int nMaxGetCount = _context.getAttribute("MaxGetCount", 4);
        // 2 执行时间过长的阀值，超过会输出警告信息
        int nMaxGetTime = _context.getAttribute("MaxGetTime", 600);

        Map[] pMediaInfo = null;
        // 为了规避MAS不稳定，尝试请求4次
        String sMasURL = ConfigServer.getServer().getSysConfigValue(
                "MAS_HOST_URL",
                " http://192.168.10.36:8080/mas/openapi/pages.do");
        DebugTimer timer = new DebugTimer();
        boolean bSucccess = false;
        for (int i = 0; i < nMaxGetCount; i++) {
            timer.start();
            try {
                pMediaInfo = MediaHelper.getMediaInfo(sMasURL,
                        VSConfig.getAppKey(), _sMasIds);
                timer.stop();
                if (timer.getTime() > nMaxGetTime) {
                    logger.warn("Get the media[Id=" + _sMasIds
                            + "] info from MAS[" + sMasURL + "] use ["
                            + timer.getTime() + "]ms!");
                }

                return pMediaInfo;
            } catch (Exception e) {
                logger.error("Fail to get the media[Id=" + _sMasIds
                        + "] info from MAS[" + sMasURL + "]!", e);
            }
            timer.stop();
            logger.warn("Get[" + (i + 1) + "] the media[Id=" + _sMasIds
                    + "] info from MAS[" + sMasURL + "] use ["
                    + timer.getTime() + "]ms!");

            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
        }

        return null;
    }

}
