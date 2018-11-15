package com.trs.mas;

import java.util.Iterator;
import java.util.Map;

import org.apache.batik.dom.util.HashTable;

import com.trs.cms.content.CMSObj;
import com.trs.cms.content.DefaultObj;
import com.trs.components.common.publish.domain.publisher.PublishTagContext;
import com.trs.components.common.publish.domain.tagparser.TagBeanInfo;
import com.trs.components.common.publish.domain.tagparser.TagParserCMSObjBase;
import com.trs.components.video.VSConfig;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.NullValue;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.DebugTimer;

public class TagParserForMAS extends TagParserCMSObjBase {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(TagParserForMAS.class);

    public TagBeanInfo getBeanInfo() {
        return null;
    }

    protected CMSObj findHost(PublishTagContext _context) throws WCMException {
        // 1 校验UpperHost中是否是MAS对象
        if(_context.getUpperHost().getSubstance() instanceof DefaultObj){
            DefaultObj oMASObj = (DefaultObj)_context.getUpperHost().getSubstance();
            if(oMASObj.containsProperty(MediaHelper.KEY_MAS_ID)){
                return oMASObj;
            }
        }
        
        // 通过ID获取MAS对象
        int nMasId = _context.getAttribute("ID", 0);
        if (nMasId <= 0) {
            logger.warn("TRS_MAS解析不符合预期！可能是上下文对象没有记录MasId？" + "\n上下文对象:[Class="
                    + _context.getUpperHost().getClass().getName() + "][ID="
                    + _context.getUpperHost().getId() + "]" + "\n记录的ID信息为["
                    + _context.getAttribute("ID") + "]");
            return null;
        }

        // 2 从TaskContext的Cache中查看是否已经请求过
        HashTable hMasObjects = (HashTable) _context.getPageContext()
                .getTaskContext().getCachObject(MediaHelper.CACHE_KEY);
        if (hMasObjects == null) {
            hMasObjects = new HashTable();
            _context.getPageContext().getTaskContext()
                    .putInCache(MediaHelper.CACHE_KEY, hMasObjects);
        }
        Integer iObjectKey = new Integer(nMasId);
        Object oValue = hMasObjects.get(iObjectKey);
        if (oValue != null) {
            return (DefaultObj) oValue;
        }

        // 根据ID，从MAS获取相关信息
        Map hMediaInfo = getMediaInfo(_context, nMasId);
        if (hMediaInfo == null)
            throw new WCMException("从MAS获取音视频信息失败！");
        
        // 将获取到的信息转存到WCM对象中
        DefaultObj oDefaultObj = new DefaultObj();
        Iterator itKeys = hMediaInfo.keySet().iterator();
        while (itKeys.hasNext()) {
            String sKey = itKeys.next().toString();
            oDefaultObj.setProperty(sKey.toUpperCase(), hMediaInfo.get(sKey),
                    false);
        }
        oDefaultObj.setAddMode(true);

        // 将新构造的对象放入到Cache中
        hMasObjects.put(iObjectKey, oDefaultObj);

        // 返回新构造的对象
        return oDefaultObj;
    }

    private Map getMediaInfo(PublishTagContext _context, int _nMasId)
            throws WCMException {
        // 获取一些逻辑控制属性
        // 1 尝试次数
        int nMaxGetCount = _context.getAttribute("MaxGetCount", 4);
        // 2 执行时间过长的阀值，超过会输出警告信息
        int nMaxGetTime = _context.getAttribute("MaxGetTime", 600);

        Map hMediaInfo = null;
        // 为了规避MAS不稳定，尝试请求4次
        String sMasURL = ConfigServer.getServer().getSysConfigValue(
                "MAS_HOST_URL",
                " http://192.168.10.36:8080/mas/openapi/pages.do");
        DebugTimer timer = new DebugTimer();
        boolean bSucccess = false;
        for (int i = 0; i < nMaxGetCount; i++) {
            timer.start();
            try {
                hMediaInfo = MediaHelper.getMediaInfo(sMasURL,
                        VSConfig.getAppKey(), _nMasId);
                timer.stop();
                if (timer.getTime() > nMaxGetTime) {
                    logger.warn("Get the media[Id=" + _nMasId
                            + "] info from MAS[" + sMasURL + "] use ["
                            + timer.getTime() + "]ms!");
                }

                return hMediaInfo;
            } catch (Exception e) {
                logger.error("Fail to get the media[Id=" + _nMasId
                        + "] info from MAS[" + sMasURL + "]!", e);
            }
            timer.stop();
            logger.warn("Get[" + (i + 1) + "] the media[Id=" + _nMasId
                    + "] info from MAS[" + sMasURL + "] use ["
                    + timer.getTime() + "]ms!");

            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
        }

        return null;
    }

    protected String getAutoLinkUrl(PublishTagContext _context)
            throws WCMException {
        return null;
    }

    protected void registerHostSpecialProperties() {

    }

    protected String[] parseHostSpecialProperty(int _nPropertyId,
            PublishTagContext _context) throws WCMException {
        return null;
    }

}
