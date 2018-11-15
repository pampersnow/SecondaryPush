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
        // 1 У��UpperHost���Ƿ���MAS����
        if(_context.getUpperHost().getSubstance() instanceof DefaultObj){
            DefaultObj oMASObj = (DefaultObj)_context.getUpperHost().getSubstance();
            if(oMASObj.containsProperty(MediaHelper.KEY_MAS_ID)){
                return oMASObj;
            }
        }
        
        // ͨ��ID��ȡMAS����
        int nMasId = _context.getAttribute("ID", 0);
        if (nMasId <= 0) {
            logger.warn("TRS_MAS����������Ԥ�ڣ������������Ķ���û�м�¼MasId��" + "\n�����Ķ���:[Class="
                    + _context.getUpperHost().getClass().getName() + "][ID="
                    + _context.getUpperHost().getId() + "]" + "\n��¼��ID��ϢΪ["
                    + _context.getAttribute("ID") + "]");
            return null;
        }

        // 2 ��TaskContext��Cache�в鿴�Ƿ��Ѿ������
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

        // ����ID����MAS��ȡ�����Ϣ
        Map hMediaInfo = getMediaInfo(_context, nMasId);
        if (hMediaInfo == null)
            throw new WCMException("��MAS��ȡ����Ƶ��Ϣʧ�ܣ�");
        
        // ����ȡ������Ϣת�浽WCM������
        DefaultObj oDefaultObj = new DefaultObj();
        Iterator itKeys = hMediaInfo.keySet().iterator();
        while (itKeys.hasNext()) {
            String sKey = itKeys.next().toString();
            oDefaultObj.setProperty(sKey.toUpperCase(), hMediaInfo.get(sKey),
                    false);
        }
        oDefaultObj.setAddMode(true);

        // ���¹���Ķ�����뵽Cache��
        hMasObjects.put(iObjectKey, oDefaultObj);

        // �����¹���Ķ���
        return oDefaultObj;
    }

    private Map getMediaInfo(PublishTagContext _context, int _nMasId)
            throws WCMException {
        // ��ȡһЩ�߼���������
        // 1 ���Դ���
        int nMaxGetCount = _context.getAttribute("MaxGetCount", 4);
        // 2 ִ��ʱ������ķ�ֵ�����������������Ϣ
        int nMaxGetTime = _context.getAttribute("MaxGetTime", 600);

        Map hMediaInfo = null;
        // Ϊ�˹��MAS���ȶ�����������4��
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
