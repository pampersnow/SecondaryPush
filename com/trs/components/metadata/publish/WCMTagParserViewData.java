package com.trs.components.metadata.publish;
/*
 * History          Who         What
 * 2009-11-11       wenyh       ���wml֧��
 */
/**
 * TRS_ViewData������
 */


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trs.DreamFactory;
import com.trs.cms.content.CMSObj;
import com.trs.components.common.publish.PublishConstants;
import com.trs.components.common.publish.domain.publisher.PublishPathCompass;
import com.trs.components.common.publish.domain.publisher.PublishSyncGlobalTuner;
import com.trs.components.common.publish.domain.publisher.PublishTagContext;
import com.trs.components.common.publish.domain.tagparser.TagBeanInfo;
import com.trs.components.common.publish.domain.tagparser.TagItem;
import com.trs.components.common.publish.domain.tagparser.TagParseHelper;
import com.trs.components.common.publish.domain.tagparser.TagParserContentBase;
import com.trs.components.common.publish.parser.HTMLContentParseHelper;
import com.trs.components.common.publish.persistent.element.IPublishContent;
import com.trs.components.common.publish.persistent.element.IPublishElement;
import com.trs.components.common.publish.persistent.element.IPublishFolder;
import com.trs.components.common.publish.persistent.element.PublishElementFactory;
import com.trs.components.common.publish.util.RelativeURLHelper;
import com.trs.components.metadata.MetaDataConstants;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.metadata.definition.ClassInfo;
import com.trs.components.metadata.definition.ClassInfos;
import com.trs.components.metadata.definition.IMetaDataDefCacheMgr;
import com.trs.components.metadata.definition.MetaView;
import com.trs.components.metadata.definition.MetaViewField;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.components.wcm.content.persistent.WebSite;
import com.trs.components.wcm.publish.tagparser.WCMAppendixGenerator;
import com.trs.infra.I18NMessage;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.support.file.FileHelper;
import com.trs.infra.support.file.FilesMan;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyException;
import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.CMyUnzip;
import com.trs.infra.util.ExceptionNumber;

/**
 * @author �ܻ�
 * 
 */
public class WCMTagParserViewData extends TagParserContentBase {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(WCMTagParserViewData.class);

    private IMetaDataDefCacheMgr m_oDataDefCacheMgr = null;

    private final static int CHNL_DESC = 100;

    private final static int CHNL_NAME = 200;

    private final static int HOME_SITE = CHNL_NAME + 1;

    private final static int HOME_SITENAME = HOME_SITE + 1;

    private static ArrayList FIELDS_DEFAULT_LIST_OF_METADATA = new ArrayList(4);
    static {
        FIELDS_DEFAULT_LIST_OF_METADATA
                .add(MetaDataConstants.FIELDNAME_METADATA_ID);
        FIELDS_DEFAULT_LIST_OF_METADATA
                .add(MetaDataConstants.FIELDNAME_CHANNEL_ID);
        FIELDS_DEFAULT_LIST_OF_METADATA.add("CRTIME");
    }

    /**
     * 
     */
    public WCMTagParserViewData() {
        m_oDataDefCacheMgr = (IMetaDataDefCacheMgr) DreamFactory
                .createObjectById("IMetaDataDefCacheMgr");
    }

    public String[] parseSubItems(PublishTagContext _context)
            throws WCMException {
        TagItem oTagItem = _context.getTagItem();
        List tagChildren = oTagItem.getChildren();
        PublishTagContext tagContext = new PublishTagContext(_context, oTagItem);
        tagContext.setUpperHost(PublishElementFactory.makeElementFrom(m_host));
        return TagParseHelper.parseItems(tagChildren, tagContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.components.common.publish.domain.tagparser.TagParserCMSObjBase
     * #parse
     * (com.trs.components.common.publish.domain.publisher.PublishTagContext)
     */
    public String[] parse(PublishTagContext _context) throws WCMException {
        m_host = findHost(_context);
        if (m_host == null)
            return null;

        if (_context.getAttribute("ParseIFNotNull") == null) {
            // ����һЩ����tag�����
            TagItem oTagItem = _context.getTagItem();
            // comment by caohui@2009-2-24 ����08:41:44
            // ��ΪֻҪ�����ñ�ͽ���
            // boolean bContainConditionItem =
            // oTagItem.containsChild("TRS_CONDITION");
            boolean bContainConditionItem = containsTagItemChild(oTagItem);
            if (bContainConditionItem) {
                return parseSubItems(_context);
            }

            return parsePropety(_context);
        }
        // ���������IFNotNull�����߼�����ҪԤ������ֵ��Ȼ���ж�
        else {
            String[] pResult = parsePropety(_context);
            if (!_context.getAttribute("ParseIFNotNull", false)) {
                return pResult;
            }
            // ��������˲�Ϊ�ռ���������߼���������ڽ����������ж�
            if (pResult == null
                    || (pResult.length == 1 && CMyString.isEmpty(pResult[0]))) {
                return null;
            }

            // ��Ϊ�ռ���������������Ļָ���Ȩ��û������ñ�
            TagItem oTagItem = _context.getTagItem();
            List tagChildren = oTagItem.getChildren();
            return TagParseHelper.parseItems(tagChildren, _context);
        }
    }

    private String[] parsePropety(PublishTagContext _context)
            throws WCMException {
        MetaViewData viewData = (MetaViewData) m_host;
        String sFieldName = _context.getAttribute("Field");
        MetaViewField viewField = m_oDataDefCacheMgr.getMetaViewField(viewData
                .getMetaView().getId(), sFieldName);
        int nViewFieldType = -1;
        if (viewField != null) {
            nViewFieldType = viewField.getType();
        }
        switch (nViewFieldType) {
        case MetaDataConstants.FIELD_TYPE_APPENDIX:
            return generateAppendix(_context);
        case MetaDataConstants.FIELD_TYPE_HTML:
        case MetaDataConstants.FIELD_TYPE_HTML_CHAR:
            return parseHtmlContent(viewData.getPropertyAsString(sFieldName),
                    _context);
        case MetaDataConstants.FIELD_TYPE_CLASS:
            return makeClassInfo(_context, viewData, sFieldName);
        case MetaDataConstants.FIELD_TYPE_MULTITEXT:
        case MetaDataConstants.FIELD_TYPE_NORMALTEXT:
            return parseText(_context, viewData.getPropertyAsString(sFieldName));
        case MetaDataConstants.FIELD_TYPE_LINK:
            String sValue = viewData.getPropertyAsString(sFieldName);
            if (sValue == null || (sValue = sValue.trim()).length() <= 0)
                return null;

            sValue = RelativeURLHelper.makeLinkValue(_context, sValue);
            return parseProperty(sValue, _context);
        default:
            return super.parse(_context);
        }
    }

    /**
     * @param _context
     * @param propertyAsString
     * @return
     * @throws WCMException
     */
    private String[] parseText(PublishTagContext _context, String sText)
            throws WCMException {
        if (sText == null || !_context.getAttribute("WML", false)) {
            return super.parse(_context);
        }

        return makeWmlContent(_context, sText);
    }

    private String[] makeWmlContent(PublishTagContext _context, String _sResult)
            throws WCMException {
        IPublishElement element = _context.getPageElement();
        if (!(element instanceof IPublishContent)) {
            return super.parse(_context);
        }
        int nMaxNum = _context.getAttribute("PAGENUM", 900);
        if (nMaxNum <= 0) {
            return super.parse(_context);
        }

        String sFileName = PublishPathCompass
                .makeDetailPageFileName((IPublishContent) element);
        String sFileExt = _context.getPageContext().getTemplate()
                .getOutputFileExt();

        List list = new ArrayList(5);
        StringBuffer buff = new StringBuffer(1024);
        int nEnLen = 1;
        int nChLen = 3;
        int nCurrLen = 0;
        int nPageIndex = 0;
        int nGet = 0; // �Ѿ�ȡ�õ��ַ��ȣ����ȣ�Ӣ���ַ��1�������ַ��3��
        String temp = null;
        char[] chars = _sResult.toCharArray();
        char ch = 0;
        for (int i = 0, len = chars.length; i < len; i++) {
            ch = chars[i];
            nCurrLen = (ch <= 0x7f) ? nEnLen : nChLen;
            nGet += nCurrLen;

            // ���ﵽ��󳤶ȣ���Ҫ����������
            if (nGet > nMaxNum) {
                temp = TagParseHelper.formatTextIfNeeded(_context,
                        buff.toString());
                buff.setLength(0);
                buff.append(temp);

                // ��ҳ����
                buff.append("<br />");
                buff.append(makeCurrNavContent(sFileName, sFileExt, nPageIndex,
                        true));

                // ������
                list.add(buff.toString());

                buff.setLength(0);
                nPageIndex++;
                nGet = nCurrLen;
            }

            // ���浽��ʱ������
            buff.append(ch);
        }
        int nImgCount = 0;
        try {
            temp = _context.getPageContext().getExtraAttribute(
                    PublishConstants.PGVARNAME_WMLIMAGECOUNT);
            nImgCount = Integer.parseInt(temp) - 1;// ���ҳ���,��0��ʼ
        } catch (Exception ex) {
        }

        if (buff.length() > 0) {
            // ��ʽ����ǰ���
            temp = TagParseHelper.formatTextIfNeeded(_context, buff.toString());
            buff.setLength(0);
            buff.append(temp);

            // ׷�����һҳ�����
            if (nPageIndex > 0 || nImgCount > nPageIndex) {
                // ׷�ӷ�ҳ����
                buff.append("<br />");
                buff.append(makeCurrNavContent(sFileName, sFileExt, nPageIndex,
                        nImgCount > nPageIndex));
            }

            // ������
            list.add(buff.toString());
            buff.setLength(0);
        }

        buff.setLength(0);
        // ��ʾͼƬ,�շ�ҳ
        nImgCount -= nPageIndex;
        while (--nImgCount >= 0) {
            buff.append("<br />");
            buff.append(makeCurrNavContent(sFileName, sFileExt, ++nPageIndex,
                    nImgCount > 0));
            list.add(buff.toString());
            buff.setLength(0);
        }

        String[] result = new String[list.size()];
        result = (String[]) list.toArray(result);
        return result;
    }

    private static String makeCurrNavContent(String _sFileName,
            String _sFileExt, int _nIndex, boolean _bDisplayPre) {
        StringBuffer sbNavContent = new StringBuffer();
        if (_nIndex > 0) {
            String sURL = null;
            if (_nIndex == 1) {
                sURL = _sFileName + "." + _sFileExt;
            } else {
                sURL = _sFileName + "_" + (_nIndex - 1) + "." + _sFileExt;
            }
            sbNavContent.append("<a href=\""
                    + sURL
                    + "\">&lt;&lt;"
                    + I18NMessage.get(WCMTagParserViewData.class,
                            "WCMTagParserViewData.label1", "��һҳ") + "</a>");
            sbNavContent.append("&nbsp;&nbsp;");
        }

        if (_bDisplayPre) {
            String sURL = _sFileName + "_" + (_nIndex + 1) + "." + _sFileExt;
            sbNavContent.append("<a href=\""
                    + sURL
                    + "\">"
                    + I18NMessage.get(WCMTagParserViewData.class,
                            "WCMTagParserViewData.label2", "��һҳ")
                    + "&gt;&gt;</a>");
        }

        return sbNavContent.toString();
    }

    /**
     * @param _context
     * @param _viewData
     * @param _sFieldName
     * @return
     * @throws WCMException
     */
    private String[] makeClassInfo(PublishTagContext _context,
            MetaViewData _viewData, String _sFieldName) throws WCMException {
        ClassInfos classInfos = ClassInfos.findByIds(null,
                _viewData.getPropertyAsString(_sFieldName));
        if (classInfos.isEmpty())
            return null;

        String sClassFieldName = _context.getAttribute("ClassField");
        if (CMyString.isEmpty(sClassFieldName)) {
            sClassFieldName = "CNAME";
        }

        boolean bFullPath = _context.getAttribute("FullPath", false);
        String sSeperator = _context.getAttribute("SEPERATOR");
        if (CMyString.isEmpty(sSeperator))
            sSeperator = "\\";
        boolean bDisplayParentName = _context.getAttribute("DisplayParentName",
                false);

        StringBuffer sbClassPaths = new StringBuffer();
        for (int i = 0, nSize = classInfos.size(); i < nSize; i++) {
            ClassInfo classInfo = (ClassInfo) classInfos.getAt(i);
            if (classInfo == null)
                continue;

            // ׷�Ӷ������·��
            if (bFullPath) {
                String sClassInfoPath = classInfo
                        .getPropertyAsString(sClassFieldName);
                ClassInfo parentClassInfo = ClassInfo.findById(classInfo
                        .getParentId());

                while (parentClassInfo != null) {
                    sClassInfoPath = parentClassInfo
                            .getPropertyAsString(sClassFieldName)
                            + sSeperator
                            + sClassInfoPath;
                    parentClassInfo = ClassInfo.findById(parentClassInfo
                            .getParentId());
                    if (parentClassInfo == null || parentClassInfo.isRoot())
                        break;
                }

                sbClassPaths.append(sClassInfoPath);
            }
            // ���쵥�������·��
            else if (bDisplayParentName) {
                ClassInfo parentClassInfo = ClassInfo.findById(classInfo
                        .getParentId());
                if (parentClassInfo != null && parentClassInfo.isRoot()) {
                    parentClassInfo = classInfo;
                }

                sbClassPaths.append(parentClassInfo == null ? classInfo
                        .getPropertyAsString(sClassFieldName) : parentClassInfo
                        .getPropertyAsString(sClassFieldName));
            } else {
                // ׷�Ӷ������·��
                sbClassPaths.append(classInfo
                        .getPropertyAsString(sClassFieldName));

            }

            sbClassPaths.append(";");
        }
        if (sbClassPaths.length() <= 0)
            return null;

        sbClassPaths.setLength(sbClassPaths.length() - 1);

        return new String[] { sbClassPaths.toString() };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.components.common.publish.domain.tagparser.TagParserCMSObjBase
     * #parseHostNormalProperty(java.lang.String,
     * com.trs.components.common.publish.domain.publisher.PublishTagContext)
     */
    protected String[] parseHostNormalProperty(String _sName,
            PublishTagContext _context) throws WCMException {
        Object oValue;

        MetaViewData viewData = (MetaViewData) m_host;
        boolean bIsLabel = _context.getAttribute("isLabel", true);
        String sDelim = _context.getAttribute("Delim");

        if (_sName.charAt(0) == PublishConstants.TAGID_ATTRIBUTE_FLAG) {
            oValue = m_host.getAttributeValue(_sName.substring(1));
        } else if (!bIsLabel
                || (m_host.getProperty(_sName) instanceof CMyDateTime)) {// ����֧��DateFormat�������D��String
            oValue = m_host.getProperty(_sName);
        } else {// ��Ҫ��Ӧ�Ƿǿ򣬻��ߵ�ѡΪ���ֵ����
            oValue = viewData.getRealProperty(_sName, sDelim);
        }

        if (oValue == null)
            return null;

        return parseProperty(oValue, _context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.components.common.publish.domain.tagparser.TagParserCMSObjBase
     * #parseHostSpecialProperty(int,
     * com.trs.components.common.publish.domain.publisher.PublishTagContext)
     */
    protected String[] parseHostSpecialProperty(int _nPropertyId,
            PublishTagContext _context) {

        if (m_host == null) {
            logger.error(_context.getTagItem()
                    + I18NMessage.get(WCMTagParserViewData.class,
                            "WCMTagParserViewData.label3",
                            "����ʧ�ܣ����MetaViewDataû���ҵ���"));
            return null;
        }

        try {
            Channel channel = ((MetaViewData) m_host).getChannel();
            switch (_nPropertyId) {
            case CHNL_DESC:
                String sDesc = channel.getDesc();
                if (sDesc == null || (sDesc = sDesc.trim()).length() <= 0) {
                    sDesc = channel.getName();
                }
                return this.parseProperty(sDesc, _context);
            case CHNL_NAME:
                String sName = channel.getName();
                return this.parseProperty(sName, _context);
            case HOME_SITE:
            case HOME_SITENAME: {
                return parseHomeSite(channel, _context, _nPropertyId);
            }
            default:
                break;
            }
        } catch (Exception e) {
            // logger me
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.components.common.publish.domain.tagparser.TagParserCMSObjBase
     * #registerHostSpecialProperties()
     */
    protected void registerHostSpecialProperties() {
        this.registerHostSpecialProperty("CHNLDESC", CHNL_DESC);
        this.registerHostSpecialProperty("CHNLNAME", CHNL_NAME);
        this.registerHostSpecialProperty("HOMESITE", HOME_SITE);
        this.registerHostSpecialProperty("HOMESITENAME", HOME_SITENAME);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.components.common.publish.domain.tagparser.ITagParser#getBeanInfo
     * ()
     */
    public TagBeanInfo getBeanInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.components.common.publish.domain.tagparser.TagParserCMSObjBase
     * #findHost
     * (com.trs.components.common.publish.domain.publisher.PublishTagContext)
     */
    protected CMSObj findHost(PublishTagContext _context) throws WCMException {
        if (m_host != null) {
            return m_host;
        }
        //����������ṩ�˿ɷ��ض�����ֱ�ӷ���
        //Object oFormTaskObject = _context.getPageContext().getTaskContext().getCachObject(MetaViewData.METAVIEWDATA_OBJ_IN_PREVIEW);
        Object oFormTaskObject = null;
        if (oFormTaskObject != null /*&& oFormTaskObject instanceof MetaViewData*/) {
            return (MetaViewData) oFormTaskObject;
        }
        // �жϵ�ǰupperHost�Ƿ���Document
        IPublishElement upperHost = _context.getUpperHost();
        CMSObj obj = upperHost.getSubstance();
        if (obj instanceof MetaViewData) {
            return obj;
        } else if (obj instanceof Document) {
            // ����ǰ��ȡ������ͼ���󻺴�����
            MetaView view = getMetaViewCachedByTagContext(_context,
                    (Document) obj);
            return getViewDataCachedByTagContext(_context, obj, view);
        } else if (obj instanceof MetaViewField) {
            // ��Ҫ�ж�һ�µ�ǰ�ñ��Ƿ���TRS_RelNews�£�����ڣ���Ҫ
            int nCount = 1, nMaxCount = 100;
            PublishTagContext tempContext = _context.getParent();
            while (tempContext != null) {
                if (nCount > nMaxCount) {
                    logger.error(nCount
                            + I18NMessage.get(WCMTagParserViewData.class,
                                    "WCMTagParserViewData.label4",
                                    "�ζ�û���ҵ�UppserHost��ViewDAta"));
                    break;
                }
                nCount++;

                CMSObj tempObj = tempContext.getUpperHost().getSubstance();
                if (tempObj instanceof MetaViewData) {
                    return tempObj;
                } else if (tempObj instanceof Document) {
                    // ����ǰ��ȡ������ͼ���󻺴�����
                    MetaView view = getMetaViewCachedByTagContext(tempContext,
                            (Document) tempObj);
                    return getViewDataCachedByTagContext(tempContext, tempObj,
                            view);
                }

                tempContext = tempContext.getParent();
            }

            CMSObj pageObj = _context.getPageElement().getSubstance();
            MetaView view = getMetaViewCachedByTagContext(_context,
                    (Document) pageObj);
            return getViewDataCachedByTagContext(_context, pageObj, view);
        } else {
            throw new WCMException(I18NMessage.get(WCMTagParserViewData.class,
                    "WCMTagParserViewData.label5",
                    "TRS_ViewData�ñ겻��Ӧ�õ���ǰ�����£�[UpperHost=")
                    + obj.getClassName(true) + "]");
        }
    }

    private MetaView getMetaViewCachedByTagContext(
            PublishTagContext _oTagContext, Document _document)
            throws WCMException {
        // add by caohui@2010-4-22 ����09:07:59
        // ȡ�����ñ��ϵ�Cache��ֱ�Ӵ�����Cache��ȡ
        // �����ܷ����������漰�����View��ȡ
        // �Ѿ���View����Cache��
        MetaView view = MetaView.findById(_document.getKindId());
        if (view == null) {
            throw new WCMException(I18NMessage.get(WCMTagParserViewData.class,
                    "WCMTagParserViewData.label6", "��ǰ�ĵ���������ͼ�����ڣ�[DocId=")
                    + _document.getId()
                    + ",ViewId="
                    + _document.getKindId()
                    + "]");
        }
        return view;

        // Map hExtraAttributes = _oTagContext.getExtraAttributes();
        // if (hExtraAttributes == null) {
        // hExtraAttributes = new HashMap(1);
        // _oTagContext.setExtraAttributes(hExtraAttributes);
        // }
        // String sKey = "Curr.View." + _document.getId();
        // MetaView view = (MetaView) hExtraAttributes.get(sKey);
        // if (view == null) {
        // view = MetaView.findById(_document.getKindId());
        // if (view == null) {
        // throw new WCMException("��ǰ�ĵ���������ͼ�����ڣ�[DocId="
        // + _document.getId() + ",ViewId="
        // + _document.getKindId() + "]");
        // }
        // hExtraAttributes.put(sKey, view);
        // }
        // return view;
    }

    private MetaViewData getViewDataCachedByTagContext(
            PublishTagContext _oTagContext, CMSObj _host, MetaView _view)
            throws WCMException {
        Map hExtraAttributes = _oTagContext.getExtraAttributes();
        if (hExtraAttributes == null) {
            hExtraAttributes = new HashMap(1);
            _oTagContext.setExtraAttributes(hExtraAttributes);
        }
        String sKey = "Curr.ViewData." + _host.getId();
        MetaViewData viewData = (MetaViewData) hExtraAttributes.get(sKey);
        if (viewData != null)
            return viewData;

        // add by caohui@2010-4-22 ����06:08:02
        // todo����Ҫ�Ի��Ǻ���Ҫ����ǰ�Ѿ�Ԥ����������������⣬����û���޸�
        // TODO ��ݸ��ڵ㻺���SelectFields��װ�����
        viewData = new MetaViewData(_view);
        // viewData.loadById(_host.getId(), viewData.getSelectFields());
        String sSelectFields = getSelectFields(_view.getTrueTableName(),
                "TRS_VIEWDATA", _oTagContext);
        viewData.loadById(_host.getId(), sSelectFields);
        hExtraAttributes.put(sKey, viewData);
        return viewData;
    }

    /**
     * @param _context
     * @return
     */
    private String[] generateAppendix(PublishTagContext _context)
            throws WCMException {
        // 1. �жϵ�ǰ�������Ч�ԣ��ļ����Ƿ���Ч���Ƿ����
        String sFieldName = _context.getAttribute("Field");
        String sFileName = m_host.getPropertyAsString(sFieldName);
        if (CMyString.isEmpty(sFileName)) {
            return new String[] { "" };
        }

        // 1.2 ��ȡ�ļ���
        sFileName = CMyFile.extractFileName(sFileName);

        // 1.3 �ж��ǲ���WCM��Protect�ļ�
        if (!FilesMan.isValidFile(sFileName, FilesMan.FLAG_PROTECTED)
                && !FilesMan.isValidFile(sFileName, FilesMan.FLAG_WEBFILE)) {
            _context.getPageContext().addErrorLog(
                    PublishConstants.TASK_STATUS_FAILED,
                    I18NMessage.get(WCMTagParserViewData.class,
                            "WCMTagParserViewData.label7", "��ظ���[")
                            + sFileName
                            + I18NMessage.get(WCMTagParserViewData.class,
                                    "WCMTagParserViewData.label8",
                                    "]������Ч��WCM�ļ���"), m_tagItem.toString());
            return new String[] { "NotValidFileName" };
        }

        // 1.4 �ж��ļ��Ƿ����
        // 1.4.1 �����ͼƬ�ļ����������˴�С����Ҫת��Ϊ�µ��ļ���ַ
        String sAbsoluteFieldName = FileHelper.makeAbsoluteFilePathOfImage(
                sFileName, _context.getAttribute("InWidth", 0));
        sFileName = CMyFile.extractFileName(sAbsoluteFieldName);
        File file = new File(sAbsoluteFieldName);
        if (!file.exists()) {
            if (!_context.isPreview())
                _context.getPageContext().addErrorLog(
                        PublishConstants.TASK_STATUS_FAILED,
                        I18NMessage.get(WCMTagParserViewData.class,
                                "WCMTagParserViewData.label7", "��ظ���[")
                                + sFileName
                                + I18NMessage.get(WCMTagParserViewData.class,
                                        "WCMTagParserViewData.label9",
                                        "]�����ڣ�[AbsoluteFile=")
                                + sAbsoluteFieldName + "]",
                        m_tagItem.toString());
            return new String[] { "NotExistFile" };
        }

        // 1.5 ���Ҫ�󷵻��ļ���С��ֱ�ӷ����ļ���С
        if (_context.getAttribute("ReturnFileSize", false)) {
            return new String[] { FileHelper.convertFileSize(file.length()) };
        }

        // 2. �ж�Ŀ���ļ��Ƿ��Ѿ��ϴ����жϱ�׼��pub���Ƿ�����ļ���������Ѿ����ˣ���ô���ٷַ�
        IPublishContent oApdOwner = (IPublishContent) _context.getUpperHost();
        PublishPathCompass compass = _context.getPathCompass();
        boolean bPreview = _context.isPreview();
        String sLocalPath = compass.getLocalPath(oApdOwner, bPreview);
        if (!CMyFile.fileExists(sLocalPath + sFileName)) {
            PublishSyncGlobalTuner.insureLocalPathExists(sLocalPath);

            // �������Ҫ��ѹ���ļ�����Ҫ�����⴦��
            String sUnZipPathField = _context
                    .getAttributeTrim("UnZipPathField");
            String sFileExt = CMyFile.extractFileExt(sFileName);
            if (!CMyString.isEmpty(sUnZipPathField)
                    && "ZIP".equalsIgnoreCase(sFileExt)) {
                String sUnZipPath = m_host.getPropertyAsString(sUnZipPathField);
                if (!CMyString.isEmpty(sUnZipPath)) {
                    try {
                        doUnZipAndDistribute(_context, sFileName, sUnZipPath);
                        return new String[] { sUnZipPath };
                    } catch (Exception e) {
                        throw new WCMException("Fail to unzip ["
                                + sAbsoluteFieldName + "]!", e);
                    }
                }
            }

            try {
                CMyFile.copyFile(sAbsoluteFieldName, sLocalPath + sFileName);
                // ������Ԥ��ģʽ������Ҫ�ַ�������������
                if (!bPreview) {
                    _context.getFileDistributeShip().distributeFile(
                            sAbsoluteFieldName, oApdOwner, null, true);
                }
            } catch (Exception e) {
                throw new WCMException(I18NMessage.get(
                        WCMTagParserViewData.class,
                        "WCMTagParserViewData.label10", "�ַ��ļ�ʧ�ܣ�[UpperHost=")
                        + _context.getUpperHost().getInfo()
                        + "][FileName="
                        + sFileName + "]", e);
            }

        }

        // 3. ����������·�������ؽ��
        // boolean sUrlIsAbs = _context.getAttribute("UrlIsAbs", false);
        // String sResult = null;
        //
        // if (!sUrlIsAbs) {
        // sResult = HtmlUtil.calRelativePath(
        // compass.getAbsoluteHttpPath(oApdOwner),
        // _context.getPageHttpPath())
        // + sFileName;
        // } else {
        // if (bPreview) {
        // sResult = compass.getPreviewHttpPath(oApdOwner) + sFileName;
        // } else {
        // sResult = compass.getAbsoluteHttpPath(oApdOwner) + sFileName;
        // }
        // }
        // ͳһ�ɸ�������������·�����ж�������
        // add by caohui@2011-7-22 ����03:29:49
        String sResult = WCMAppendixGenerator.makeAppendixURL(_context,
                oApdOwner, sFileName);
        return new String[] { sResult };
    }

    private void doUnZipAndDistribute(PublishTagContext _context,
            String _sZipFileName, String _sUnZipPathName) throws Exception {
        FilesMan filesMan = FilesMan.getFilesMan();
        String sAbsoluteZipFilePath = filesMan.mapFilePath(_sZipFileName,
                FilesMan.PATH_LOCAL);
        sAbsoluteZipFilePath = CMyString.setStrEndWith(sAbsoluteZipFilePath,
                File.separatorChar);

        String sNewPath = sAbsoluteZipFilePath + _sUnZipPathName
                + File.separatorChar;
        boolean bPreview = _context.isPreview();
        String sFolderLocalPath = _context.getPathCompass().getLocalPath(
                _context.getPageFolder(), bPreview);
        String sContentLocalPath = _context.getPathCompass().getLocalPath(
                _context.getUpperHost(), bPreview);
        // ����Ѿ���ѹ���Ͳ��ٽ�ѹ��Ҳ���ٷַ�
        if (new File(sContentLocalPath + _sZipFileName).exists()) {
            return;
        }

        // ��Ϊѹ���ļ��Ŀ��ܴ���һ��Ŀ¼��������Ҫ���ӵ�rename�������Ƚ�ѹ����ʱĿ¼��Ȼ����rename
        String sUnzipPath = sAbsoluteZipFilePath
                + CMyFile.excludeFileExt(_sZipFileName) + File.separatorChar;
        CMyUnzip myUnzip = new CMyUnzip();
        myUnzip.addStrictFile(ConfigServer.getServer().getInitProperty(
                "FILE_UPLOAD_ALLOW_SUFFIX")
                + ",shtml,shtm");
        myUnzip.setZipFile(sAbsoluteZipFilePath + _sZipFileName);
        myUnzip.unzip(sUnzipPath, true);

        File newFilePath = new File(sNewPath);
        if (newFilePath.exists()) {
            String sBackupPath = sNewPath.substring(0, sNewPath.length() - 1)
                    + "_" + System.currentTimeMillis() + File.separatorChar;
            newFilePath.renameTo(new File(sBackupPath));

            newFilePath = new File(sNewPath);
        }

        File file = new File(sUnzipPath);
        File oRenameFile = file;
        File[] pSubFiles = file.listFiles();
        if (pSubFiles.length == 1 && pSubFiles[0].isDirectory()) {
            oRenameFile = pSubFiles[0];
        }
        if (!oRenameFile.renameTo(newFilePath)) {
            throw new Exception("Fail rename " + oRenameFile.getAbsolutePath()
                    + " to " + sNewPath);
        }

        // ��Ŀ¼������Preview����PubĿ¼��
        try {
            CMyFile.copyFileDir(sNewPath, sFolderLocalPath, true);

            // ������Ԥ��ģʽ������Ҫ�ַ�������������
            if (!bPreview) {
                // �ײ�ַ�Ŀ¼ʵ�ֵ������⣬�Լ�д�ݹ麯��ʵ��
                distributePath(_context, _context.getPageFolder(), sNewPath,
                        _sUnZipPathName);
            }

            // ����ZIP�ļ�Ϊ�˱�ʾ�Ѿ��ַ���
            CMyFile.copyFile(sAbsoluteZipFilePath + _sZipFileName,
                    sContentLocalPath + _sZipFileName, true);
        } catch (Exception e) {
            throw new WCMException(I18NMessage.get(WCMTagParserViewData.class,
                    "WCMTagParserViewData.label10", "�ַ��ļ�ʧ�ܣ�[UpperHost=")
                    + _context.getUpperHost().getInfo()
                    + "][FileName="
                    + _sZipFileName + "]", e);
        }
    }

    private void distributePath(PublishTagContext _context,
            IPublishElement _dstElement, String _sSrcFilePath,
            String _sSubPathName) throws WCMException {
        File[] pFiles = new File(_sSrcFilePath).listFiles();
        for (int i = 0; i < pFiles.length; i++) {
            File oFile = pFiles[i];
            // ֱ�ӷַ�
            if (oFile.isFile()) {
                _context.getFileDistributeShip().distributeFile(
                        oFile.getAbsolutePath(), _dstElement, _sSubPathName,
                        false);
                continue;
            }

            // ��Ҫ��һ����Ŀ¼���ſ����÷ַ�ʱ����Ŀ¼Ҳ����
            String sSubPathName = _sSubPathName;
            if (CMyString.isEmpty(sSubPathName)) {
                sSubPathName = "";
            } else {
                sSubPathName += '/';
            }
            sSubPathName += oFile.getName();

            // Ȼ��ݹ�ַ�
            distributePath(_context, _dstElement, oFile.getAbsolutePath(),
                    sSubPathName);

        }

    }

    /**
     * ��ȡ�ĵ���������
     * 
     * @return �ĵ�����������
     * @throws WCMException
     *             ��ȡʧ�ܣ����׳��쳣��
     */
    private String[] parseHtmlContent(String _sHTMLContent,
            PublishTagContext _context) throws WCMException {
        if (CMyString.isEmpty(_sHTMLContent)) {
            return null;
        }

        // wenyh@2009-10-29 comment:wml.
        if (_context.getAttribute("WML", false)
                && _context.getAttribute("PAGENUM", 0) > 0) {
            String sText = _sHTMLContent.replaceAll("<{1}[^>]{1,}>{1}", "");
            return parseText(_context, sText);
        }

        String sResult = null;
        try {
            sResult = (new HTMLContentParseHelper()).makeHTMLContentParsed(
                    _context, _sHTMLContent);
            
            String sCleanTagsAttrs = _context.getAttribute("cleanattrs4tags");
            if (!CMyString.isEmpty(sCleanTagsAttrs)) {
                //sResult = CMyString.cleanAttributes4Tags(sResult,sCleanTagsAttrs);
            }

            String sExcludeTags = _context.getAttribute("excludetags");
            if (!CMyString.isEmpty(sExcludeTags)) {
                //sResult = CMyString.excludeTags(sResult, sExcludeTags);
            }

            String sIncludeTags = _context.getAttribute("includetags");
            if (!CMyString.isEmpty(sIncludeTags)) {
                //sResult = CMyString.includeTags(sResult, sIncludeTags);
            }
            
            // add by liuhm @2013-03-06
            // �ñ��ֲ���д�ĸ�ʽ�����Կ�������ǰ��û֧�ֵģ��ڴ��ƶ��Ż�������������Ҫ��ʽ��ΪinnerHTML���������˴˴���
            sResult = TagParseHelper.formatTextIfNeeded(_context, sResult);
            
            // CC@2012-06-15 comment: filter for num value
            // ��Ϊtry������в�û�е��õ��������NUM���ˣ����������Ҫ����϶�NUM���߼�����
            if (_context.getAttribute("NUM", 0) > 0) {
                sResult = TagParseHelper
                        .truncateTextIfNeeded(_context, sResult);
            }
            // ֻ��ϸ��ҳ�����Ҫ��ҳ
            if (_context.getPageElement() instanceof IPublishContent) {
                return TagParseHelper.separatePages(sResult);
            }

            return new String[] { sResult };
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_WCMEXCEPTION,
                    I18NMessage.get(WCMTagParserViewData.class,
                            "WCMTagParserViewData.label11", "���� ")
                            + m_host
                            + I18NMessage.get(WCMTagParserViewData.class,
                                    "WCMTagParserViewData.label12", " ������ʧ��!"),
                    ex);
        }
    }// END:getContent()

    private static boolean containsTagItemChild(TagItem _currTagItem) {
        List m_children = _currTagItem.getChildren();
        if (m_children == null) {
            return false;
        }

        // else
        Object child;
        for (int i = 0, nSize = m_children.size(); i < nSize; i++) {
            child = m_children.get(i);
            if (child != null && (child instanceof TagItem)) {
                return true;
            }
        }

        // else, not found
        return false;
    }

    private String[] parseHomeSite(Channel channel, PublishTagContext _context,
            int _nPropId) throws WCMException {
        if (channel == null) {
            return null;
        }

        WebSite site = channel.getSite();
        if (site == null) {
            return null;
        }
        String sHomeName = _nPropId == HOME_SITE ? site.getDesc() : site
                .getName();
        try {
            // to truncate text
            String sResult = TagParseHelper.truncateTextIfNeeded(_context,
                    sHomeName);
            sResult = TagParseHelper.formatTextIfNeeded(_context, sResult);

            // to render link
            if (_context.getAttribute("AUTOLINK", false)) {
                IPublishFolder folder = (IPublishFolder) PublishElementFactory
                        .makeElementFrom(site);
                sResult = TagParseHelper.addAutoLink(folder.getRootDomain(),
                        sResult, sHomeName, _context);
            }

            return new String[] { sResult };
        } catch (Exception ex) {
            throw new WCMException(I18NMessage.get(WCMTagParserViewData.class,
                    "WCMTagParserViewData.label11", "���� ")
                    + channel
                    + I18NMessage.get(WCMTagParserViewData.class,
                            "WCMTagParserViewData.label15", " ������վ��ʧ��!"), ex);
        }
    }

    private TagItem findObjectsTagItem(TagItem _currParentTagItem) {
        if (_currParentTagItem == null)
            return null;

        String sTagName = _currParentTagItem.getName().toUpperCase();
        // ���жϸ��ñ��Ƿ�����Ҫ�������ϵݹ�Ѱ�ҵ��ñ�
        if (sTagName.equals(PublishConstants.TAGNAME_RECORD)
                || sTagName.equals(PublishConstants.TAGNAME_EXISTSDATA)
                || sTagName.equals(PublishConstants.TAGNAME_NOTEXISTSDATA)
                || sTagName.equals("TRS_CONDITION")) {
            return findObjectsTagItem(_currParentTagItem.getParent());
        }

        return _currParentTagItem;
    }

    protected String getSelectFields(String _sTableName,
            String _sElementTagName, PublishTagContext _tagContext) {
        // �����ϸ��ҳ�棬��ʱȷ��ΪSelectFieldsΪ*
        if (_tagContext.getPageElement() instanceof IPublishContent)
            return "*";

        // �ҵ����ϸ��ñ꣬������е�TRS_ViewData�ñ깹���SelectFields
        TagItem oObjectsTagItem = findObjectsTagItem(_tagContext.getTagItem()
                .getParent());
        if (oObjectsTagItem == null)
            return "*";
        // �����л�ȡ����ViewData�ֶε�ֵ
        String sSelectAll = oObjectsTagItem.getAttribute("SelectAll");
        if ("true".equalsIgnoreCase(sSelectAll)) {
            return "*";
        }

        // ���Tag���Ƿ񻺴��˼�����Lazy Load
        String sKey = (_sTableName + ".SELECT").toUpperCase();
        String sSelectFields = oObjectsTagItem.getExtraAttributeValue(sKey);
        if (sSelectFields == null) {
            synchronized (oObjectsTagItem) {
                sSelectFields = oObjectsTagItem.getExtraAttributeValue(sKey);
                if (sSelectFields == null) {
                    sSelectFields = makeSelectFields(_sTableName,
                            _sElementTagName, oObjectsTagItem, _tagContext);

                    // ��������TagItem�ϣ������μ���
                    oObjectsTagItem.setExtraAttribute(sKey, sSelectFields);
                }
            }
        }
        return sSelectFields;
    }

    /**
     * @param _tagContext
     * @return
     */
    private String makeSelectFields(String _sTableName, String _sTagName,
            TagItem _oObjectsTagItem, PublishTagContext _tagContext) {

        ArrayList arDefaultFields = FIELDS_DEFAULT_LIST_OF_METADATA;

        String sSelectFields = (String) arDefaultFields.get(0);
        for (int i = 1, nSize = arDefaultFields.size(); i < nSize; i++) {
            sSelectFields += "," + arDefaultFields.get(i);
        }

        ArrayList listChildFields = TagParseHelper.makeSelectFields(
                _oObjectsTagItem, _sTagName);

        DBManager dbMgr = DBManager.getDBManager();
        for (int i = 0, nSize = listChildFields.size(); i < nSize; i++) {
            // Already Add
            String sFieldName = (String) listChildFields.get(i);
            if (arDefaultFields.indexOf(sFieldName) >= 0)
                continue;

            // Check Field Exists
            try {
                if (dbMgr.getFieldInfo(_sTableName, sFieldName) == null) {
                    _tagContext.addWarning(_sTagName
                            + I18NMessage.get(WCMTagParserViewData.class,
                                    "WCMTagParserViewData.label16",
                                    "�ñ�ָ����Field[")
                            + sFieldName
                            + I18NMessage.get(WCMTagParserViewData.class,
                                    "WCMTagParserViewData.label17", "]��")
                            + _sTableName
                            + I18NMessage.get(WCMTagParserViewData.class,
                                    "WCMTagParserViewData.label18", "���в����ڣ�"));
                    continue;
                }
            } catch (WCMException ex) {
                ex.printStackTrace();
                try {
                    _tagContext.addWarning(I18NMessage.get(
                            WCMTagParserViewData.class,
                            "WCMTagParserViewData.label19", "��ȡָ����Field[")
                            + sFieldName
                            + I18NMessage.get(WCMTagParserViewData.class,
                                    "WCMTagParserViewData.label20", "]��")
                            + _sTableName
                            + I18NMessage.get(WCMTagParserViewData.class,
                                    "WCMTagParserViewData.label21", "���з����쳣��")
                            + CMyException.getStackTraceText(ex));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Add Field
            sSelectFields += "," + sFieldName;
        }

        return sSelectFields;
    }

}