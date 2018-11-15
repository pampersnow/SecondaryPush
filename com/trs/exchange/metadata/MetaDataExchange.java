package com.trs.exchange.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import com.trs.DreamFactory;
import com.trs.cms.auth.persistent.User;
import com.trs.components.metadata.MetaDataConstants;
import com.trs.components.metadata.center.IMetaViewEmployerMgr;
import com.trs.components.metadata.center.MetaDataCenterClassInfoHelper;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.metadata.definition.IMetaDataDefCacheMgr;
import com.trs.components.metadata.definition.MetaView;
import com.trs.components.metadata.definition.MetaViewField;
import com.trs.components.metadata.service.MetaDataCenterServiceProvider;
import com.trs.components.wcm.content.domain.AppendixMgr;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.ex.util.CMy3WLib;
import com.trs.exchange.DataMigrationLogger;
import com.trs.exchange.ExchangeHelper;
import com.trs.exchange.MyDBManager;
import com.trs.exchange.mas.MediaHelper;
import com.trs.infra.I18NMessage;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.support.file.FilesMan;
import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.ExceptionNumber;
import com.trs.infra.util.html.HtmlElement;
import com.trs.infra.util.html.HtmlElementFinder;

public abstract class MetaDataExchange {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(MetaDataExchange.class);

    protected MyDBManager m_oDBMgr = null;

    private AppendixMgr m_oAppendixMgr = null;

    protected FilesMan filesMan = null; // ȡ�ļ�������

    protected DataMigrationLogger m_oDLogger = null;

    private IMetaViewEmployerMgr m_oMetaViewEmployerMgr = null;

    private IMetaDataDefCacheMgr m_oDataDefCacheMgr = null;
    
    private String m_sMASURL = null;
    private int m_nDirId = 0;

    public MetaDataExchange(MyDBManager _dbMgr) {
        m_oDBMgr = _dbMgr;
        m_oAppendixMgr = (AppendixMgr) DreamFactory
                .createObjectById("AppendixMgr");
        m_oMetaViewEmployerMgr = (IMetaViewEmployerMgr) DreamFactory
                .createObjectById("IMetaViewEmployerMgr");
        m_oDataDefCacheMgr = (IMetaDataDefCacheMgr) DreamFactory
                .createObjectById("IMetaDataDefCacheMgr");
        filesMan = FilesMan.getFilesMan();
    }
    
    public void setMASInfo(String _sMASURL, int _nDirId){
        m_sMASURL = _sMASURL;
        m_nDirId = _nDirId;
        
    }

    public void createMetaViewDatas(User _oCrUser, int _nDstChannelId,
            WCMFilter _filterForQuerySrcData, String _sIdFieldName,
            boolean _bOrderById) throws Exception {
        // 0 ������־�ļ�����ȡ��һ�ν��
        // >=0����ʾ��ָ����¼ID��ʼǨ��
        // -1����ʾ��Ǩ����
        initLogger(_nDstChannelId);
        int nLasMigrationId = m_oDLogger.readLastInfo();
        if (nLasMigrationId == -1) {
            logger.warn("��Ŀ[ID=" + _nDstChannelId + "]�Ѿ�Ǩ���꣬�ظ�ִ�У�");
            return;
        }

        // 1 У��ָ������Ŀ�Ƿ���Ч
        Channel dstChannel = Channel.findById(_nDstChannelId);
        if (dstChannel == null) {
            throw new WCMException("ָ������Ŀ��Ч��[��ĿID=" + _nDstChannelId + "]");
        }
        logger.info("Begin Data Migration to [" + dstChannel + "]..........");
        m_oDLogger.recordStartChannel(_nDstChannelId);

        // 2 �ֶα���ָ��SQL�����ݣ����Ǩ��
        // 2.1 ���㱾��Ǩ����СID�����ID����Ҫ�����һ��Ǩ�Ƶ���־����
        WCMFilter oQueryMinAndMaxFilter = new WCMFilter(
                _filterForQuerySrcData.getFrom(),
                _filterForQuerySrcData.getWhere(), "", "min(" + _sIdFieldName
                        + "), max(" + _sIdFieldName + ")");
        int[] pTemp = queryMinAndMaxId(oQueryMinAndMaxFilter.toSQL());
        int nMinDataId = pTemp[0], nMaxDataId = pTemp[1];
        if (nMinDataId < nLasMigrationId) {
            nMinDataId = nLasMigrationId;
        }

        // 2.2 ����СID��ʼǨ�ƣ�ÿ��Ǩ��ָ����������������
        int nMingrationSize = 3000;
        for (int nTempMaxDataId = nMinDataId; nTempMaxDataId <= nMaxDataId; nMinDataId = nTempMaxDataId) {
            nMinDataId = nTempMaxDataId;
            nTempMaxDataId += nMingrationSize;

            // ����ֶ�Ǩ�Ƶ�SQL
            WCMFilter filterForQueryRnageData = toRangeFilter(
                    _filterForQuerySrcData, nMinDataId, nTempMaxDataId,
                    _sIdFieldName);
            // ��ʽǨ������
            createRangeMetaViewDatas(_oCrUser, dstChannel,
                    filterForQueryRnageData);
        }

        // 2.3 ����ԭʼSQL�������ID��˳���Ӧ��SQL�������ٸ���WCMChnlDoc�е�DocOrder
        // ������ǰ���ID������Ҫ�����{��һƪ���
        if (!_bOrderById) {
            updateDocOrder(dstChannel, _filterForQuerySrcData, _sIdFieldName);
        }

        logger.info("End Data Migration to [" + dstChannel + "]!");
        m_oDLogger.recordEndChannel(_nDstChannelId);
    }

    private WCMFilter toRangeFilter(WCMFilter _filterForQuerySrcData,
            int _nMinDataId, int _nMaxDataId, String _sIdFieldName) {
        String sWhere = " (" + _sIdFieldName + ">=? and " + _sIdFieldName
                + "<?) ";
        if (!CMyString.isEmpty(_filterForQuerySrcData.getWhere())) {
            sWhere += " and " + _filterForQuerySrcData.getWhere();
        }
        WCMFilter filterForQueryRnageData = new WCMFilter(
                _filterForQuerySrcData.getFrom(), sWhere, _sIdFieldName
                        + " asc", _filterForQuerySrcData.getSelect());
        filterForQueryRnageData.addSearchValues(_nMinDataId);
        filterForQueryRnageData.addSearchValues(_nMaxDataId);
        return filterForQueryRnageData;
    }

    private void createRangeMetaViewDatas(User _oCrUser, Channel _dstChannel,
            WCMFilter _filterForQueryRangeSrcData) throws Exception {
        DBManager dbMgrForWCM = DBManager.getDBManager();

        MetaView view = findViewByChannel(_dstChannel);

        // 2 ����ָ��SQL��ѯ�������ݣ�����Ǩ��
        Connection oConn = null;
        PreparedStatement oPreStmt = null;
        ResultSet result = null;
        int nSrcId = 0;
        try {
            oConn = m_oDBMgr.getConnection();
            oPreStmt = oConn.prepareStatement(_filterForQueryRangeSrcData
                    .toSQL());
            m_oDBMgr.setParameters(oPreStmt,
                    _filterForQueryRangeSrcData.getSearchValues());
            result = oPreStmt.executeQuery();
            while (result.next()) {
                // 1 �����µ�Ԫ����
                MetaViewData oMetaData = new MetaViewData(view);

                // 2 ��ȡ���ݣ�SQL�����ֶα�����Ҫ��WCMMetaDataXXXһ��Ŷ����������������������
                ExchangeHelper.readFromRs(oMetaData, result,
                        result.getMetaData(), m_oDBMgr.getDbType(), true);
                nSrcId = oMetaData.getPropertyAsInt("DocOutupId", 0);
                if (nSrcId <= 0)
                    throw new WCMException("��ѯ����������û��ָ��DocOutupId��[SQL="
                            + _filterForQueryRangeSrcData + "]");

                logger.info("Begin to dowith the data[" + nSrcId + "] to "
                        + _dstChannel + ".....");
                m_oDLogger.recordStartData(nSrcId);

                // 3 ������Ŀ����
                oMetaData.setProperty(MetaDataConstants.FIELDNAME_CHANNEL_ID,
                        _dstChannel.getId());

                // 4 �����ڴ���ͼ�Ļ����ֶΡ����෨�ֶΡ������ֶΡ�ö���ֶεȣ�
                // 4.1 ת��һ�·��෨�ֶ�
                MetaDataCenterClassInfoHelper.renderClassInfoFields(oMetaData);
                // 4.2 ���������ֶ�
                renderFields(oMetaData);

                // 5 ����
                // 5.1 ����֮ǰ�����ڲ�Ԥ��������ʵ���������Ҫ�����Լ����߼������Ը�д�������
                doSomethingBeforeSave(oMetaData);
                // 5.2 ��Ᵽ��
                oMetaData.save(_oCrUser);
                if(oMetaData.getDocument() == null){
                    throw new WCMException("Fail to save metaviewdata![SrcId="+nSrcId+"]");
                }
                // 5.3 ����DocOutupId
                dbMgrForWCM
                        .sqlExecuteUpdate(
                                new String[] { "update WCMDocument set DocOutupId=? where DocId=?" },
                                new int[] { nSrcId, oMetaData.getDocumentId() });
                // 5.4 ���ڴ���
                doSomethingAfterSave(oMetaData);

                // 6 ��������������������ɹ�����Ϊ�����ĵ���Ҫͬ������ɾ����
                // ����֧�����ַ�ʽ��1 �����ݿ��ѯ 2 ���ֶ��н���
                // ��ͬ��ʽʵ�ֲ�ͬ�ӿ�

                // �����ݿ��в�ѯ
                WCMFilter[] pFilterQueryAppendix = makeFilterQueryAppendixOfDocument(oMetaData);
                if (pFilterQueryAppendix != null
                        && pFilterQueryAppendix.length > 0) {
                    for (int nFilterIndex = 0; nFilterIndex < pFilterQueryAppendix.length; nFilterIndex++) {
                        createAppendixes(_oCrUser, oMetaData,
                                pFilterQueryAppendix[nFilterIndex]);
                    }
                }
                // �����ĵ��е�ĳ�����Է�����������ϣ�Ȼ���������
                List arAppendixes = makeAppendixesOfDocument(oMetaData);
                if (arAppendixes != null && !arAppendixes.isEmpty()) {
                    for (int nAppendixIndex = 0, nAppendixSize = arAppendixes
                            .size(); nAppendixIndex < nAppendixSize; nAppendixIndex++) {
                        Appendix appendix = (Appendix) arAppendixes
                                .get(nAppendixIndex);
                        saveAppendix(oMetaData, appendix);
                    }
                }

                // 7 ��¼��־
                logger.info("END to dowith the data[" + nSrcId + "] to "
                        + _dstChannel + "!");
                m_oDLogger.recordEndData(nSrcId);

            }
        } catch (Exception ex) {
            if(nSrcId>0){
                error("�����������ݳ������⣡[����ID="+nSrcId+"]", ex);
            }else{
                error("��ѯ���ݳ������⣿[SQL="+_filterForQueryRangeSrcData+"]", ex);
            }
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    logger.error("Failed to close result", e);
                }
            }
            if (oPreStmt != null) {
                try {
                    oPreStmt.close();
                } catch (Exception ex) {
                    logger.error("Failed to close prepared statement", ex);
                }
            }
            if (oConn != null) {
                m_oDBMgr.freeConnection(oConn);
            }
        }
    }

    private void createAppendixes(User _oCrUser, MetaViewData _oMetaData,
            WCMFilter _filterForQueryAppendix) throws Exception {
        // 2 ����ָ��SQL��ѯ�������ݣ�����Ǩ��
        Connection oConn = null;
        PreparedStatement oPreStmt = null;
        ResultSet result = null;

        try {
            oConn = m_oDBMgr.getConnection();
            oPreStmt = oConn.prepareStatement(_filterForQueryAppendix.toSQL());
            m_oDBMgr.setParameters(oPreStmt,
                    _filterForQueryAppendix.getSearchValues());
            result = oPreStmt.executeQuery();
            while (result.next()) {
                // 1 �����µĸ���
                Appendix appendix = Appendix.createNewInstance();

                // 2 ��ȡ���ݣ�SQL�����ֶα�����Ҫ��WCMAppendixһ��Ŷ����������������������
                ExchangeHelper.readFromRs(appendix, result,
                        result.getMetaData(), m_oDBMgr.getDbType());
                logger.info("Begin to dowith the appendix[" + appendix
                        + "] of " + _oMetaData + ".....");

                // 4 ���ڴ��������ĵ����ļ���׺���������͡��������ļ��ȵȣ�
                saveAppendix(_oMetaData, appendix);

                // 7 ��¼��־
                logger.info("END to dowith the appendix[" + appendix.getId()
                        + "] of " + _oMetaData + "!");
            }
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_WCMEXCEPTION,
                    "Failed to query data!", ex);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    logger.error("Failed to close result", e);
                }
            }
            if (oPreStmt != null) {
                try {
                    oPreStmt.close();
                } catch (Exception ex) {
                    logger.error("Failed to close prepared statement", ex);
                }
            }
            if (oConn != null) {
                m_oDBMgr.freeConnection(oConn);
            }
        }
    }

    private void saveAppendix(MetaViewData _oMetaData, Appendix _appendix)
            throws WCMException, Exception {
        String sAppFile = _appendix.getFile();
        if (CMyString.isEmpty(sAppFile)) {
            error("ָ������û������AppFile���ԣ�[�����ĵ�=" + _oMetaData + "]");
        }

        _appendix.setDocId(_oMetaData.getId());
        // 4.1 �������ͣ����û�����ã�Ĭ��Ϊͼ�Ļ�������
        if (_appendix.getFlag() <= 0) {
            _appendix.setFlag(Appendix.FLAG_DOCAPD);
        }
        // 4.2 ������������ش���
        String sWCMFileType = null;
        if (_appendix.getFlag() == Appendix.FLAG_DOCAPD) {
            sWCMFileType = FilesMan.FLAG_PROTECTED;
        } else if (_appendix.getFlag() == Appendix.FLAG_DOCPIC) {
            sWCMFileType = FilesMan.FLAG_PROTECTED;
        }
        if (sWCMFileType != null) {
            // WCM���Ѿ�������ļ�
            if (FilesMan.isValidFile(sAppFile, sWCMFileType)
                    && filesMan.fileExists(sAppFile)) {

            } 
            // ��WCM�ļ���Ҫ���⴦��
            else {
                String sNewFileName = saveFile(_oMetaData, sAppFile, true);
                if (sNewFileName != null) {
                    _appendix.setFile(sNewFileName);
                }
            }
        }

        // 5 ����
        m_oAppendixMgr.addAppendix(_oMetaData.getDocument(), _appendix);
    }

    /**
     * �����ѯ������SQL������ID��?���棬�Ա����Ч�ʣ�
     * SQL���췽���͹����ĵ���ѯSQL���ƣ�ʹ�ñ�������Select���ֶκ�WCMAppendix���ֶ���һ��
     * 
     * @return WCM֧�ֶ��ָ�����WCM�ļ����� ͼƬ������SQL��select 20 AppFlag, MyFileDesc AppDesc,
     *         MyFileDesc AppFileAlt, MyFileName AppFile from MyFiles where
     *         ContentId=?; �ļ�������SQL��select 10 AppFlag, MyFileDesc AppDesc,
     *         MyFileDesc AppFileAlt, MyFileName AppFile from MyFiles where
     *         ContentId=?; ���Ӹ�����SQL��select 40 AppFlag, MyFileDesc AppDesc,
     *         MyFileDesc AppFileAlt, MyFileName AppFile from MyFiles where
     *         ContentId=?;
     */
    protected abstract WCMFilter[] makeFilterQueryAppendixOfDocument(
            MetaViewData _oMetaData);

    /**
     * ���ݴ�Դ���ݿ��ȡ���ĵ�ĳ�����ݽ���������
     * 
     * @param _document
     *            ��Դ���ݿ��¼�н��������ĵ�����
     * @return ���ظ������ϣ�����˳��
     */
    protected abstract List makeAppendixesOfDocument(MetaViewData _oMetaData)
            throws Exception;

    private void dowithHtmlContent(MetaViewData _oMetaData,
            String _sHTMLFieldName) throws WCMException {
        String sHTMLContent = _oMetaData.getPropertyAsString(_sHTMLFieldName);

        String[] arResTagName = { "img", "table", "td", "script", "a" };
        String[] arResSrcName = { "src", "BACKGROUND", "BACKGROUND", "scr",
                "href" };

        int nTagSize = (arResTagName.length > arResSrcName.length ? arResTagName.length
                : arResSrcName.length);

        HtmlElementFinder imgFinder = null; // ������Html�ı��в���IMGԪ��
        HtmlElement element = null;
        HashMap hImgRecs = new HashMap();

        String sResName, sIgnore, sKey;
        boolean bFind = false;
        try {
            // 03. ���δ���������ܵ�ҳ�渽��Ԫ��
            for (int nTagIndex = 0; nTagIndex < nTagSize; nTagIndex++) {
                String currTagName = arResTagName[nTagIndex];

                String currTagSrcName = arResSrcName[nTagIndex];
                imgFinder = new HtmlElementFinder(sHTMLContent);
                // while - ����/�ҵ���һ�������Ǹ�����ҳ��Ԫ��
                while (true) {
                    // ������һ�ε�Ԫ��
                    imgFinder.putElement(element);
                    // ����Ѱ����һ��Ԫ��
                    element = imgFinder.findNextElement(currTagName, true);
                    if (element == null) {
                        break;
                    }

                    // �ж��Ƿ��������
                    sIgnore = element.getAttributeValue("IGNORE");
                    if ("1".equals(sIgnore))
                        continue;
                    sResName = element.getAttributeValue(currTagSrcName);
                    if (sResName == null
                            || (sResName = sResName.trim()).length() == 0)
                        continue;

                    // ���֮ǰ�Ƿ����
                    sKey = sResName.toLowerCase();
                    String sUploadName = (String) hImgRecs.get(sKey);
                    if (sUploadName != null) {
                        element.setAttribute(currTagSrcName, sUploadName);
                        continue;
                    }

                    // ���û�д�����Ҫ����Ԥ������������߿ɷ��ʱ���Ŀ¼���ļ�������WCMData��Upload�У�
                    // �ж�ָ���ļ���׺�Ƿ���Ҫ����
                    String sFileExt = ExchangeHelper.extractFileExt(sResName);

                    // ���AԪ�أ�Ŀǰ��������doc��docx��xls�ȳ����ļ�
                    if (currTagName.equalsIgnoreCase("A")
                            && !(sFileExt.endsWith("doc")
                                    || sFileExt.endsWith("docx") || sFileExt
                                        .endsWith("xls"))) {
                        continue;
                    }
                    
                    // ���Ա����ļ����������ɹ�����һ��WCM������ļ���
                    sUploadName = saveFile(_oMetaData, sResName, false);
                    if(sUploadName == null)continue;                   
                    
                   
                    // ��ԭ���������ó�WCM�����ļ���
                    bFind = true;
                    element.setAttribute(currTagSrcName, sUploadName);

                    // ��¼�Ѿ���������ļ�
                    hImgRecs.put(sKey, sUploadName);
                }// END While

                sHTMLContent = imgFinder.getContent();
            } // end of for (arTagName)

            if (bFind) {
                _oMetaData.setProperty(_sHTMLFieldName, sHTMLContent);
            }
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_WCMEXCEPTION,
                    "����ͼ�Ļ�������ʧ��!", ex);
        }// end try

    }// END: saveHtmlContent()

    private void updateDocOrder(Channel _dstChannel,
            WCMFilter _filterForQuerySrcData, String _sIdFieldName)
            throws Exception {
        DBManager dbMgrForWCM = DBManager.getDBManager();

        Connection oConn = null;
        PreparedStatement oPreStmt = null;
        ResultSet result = null;

        try {
            oConn = m_oDBMgr.getConnection();
            oPreStmt = oConn.prepareStatement(_filterForQuerySrcData.toSQL());
            m_oDBMgr.setParameters(oPreStmt,
                    _filterForQuerySrcData.getSearchValues());
            result = oPreStmt.executeQuery();
            int nDocOrder = 0;
            while (result.next()) {
                int nSrcId = result.getInt(_sIdFieldName);
                logger.info("Begin to update the order of data[" + nSrcId
                        + "]  " + _dstChannel + ".....");
                nDocOrder++;
                dbMgrForWCM
                        .sqlExecuteUpdate(
                                new String[] { "update WCMChnlDoc set DocOrder=?"
                                        + " where ChnlId=? and DocId in("
                                        + "select DocId from WCMDocument where DocOutupId=��)" }, //
                                new int[] { nDocOrder, _dstChannel.getId(),
                                        nSrcId });
                logger.info("End to update the order of data[" + nSrcId + "]  "
                        + _dstChannel + "!");
            }
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_WCMEXCEPTION,
                    "Failed to query data!", ex);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    logger.error("Failed to close result", e);
                }
            }
            if (oPreStmt != null) {
                try {
                    oPreStmt.close();
                } catch (Exception ex) {
                    logger.error("Failed to close prepared statement", ex);
                }
            }
            if (oConn != null) {
                m_oDBMgr.freeConnection(oConn);
            }
        }
    }

    private String saveFile(MetaViewData _oMetaData, String sResName, boolean _bAppendixs)
            throws Exception {
        String sFileExt = ExchangeHelper.extractFileExt(sResName);

        if (sFileExt == null
                || ExchangeHelper.isForbidFileExt(sFileExt)) {
            logger.error("ָ���ļ��ĺ�׺�����ڰ���������Χ֮�ڣ�[�ļ�=" + sResName
                    + "]");
            return null;
        }

        // ��������·�������ػ���HTTP��������ӿڣ�������ʵ��
        String sResFullPahtName = makeResFullPathName(_oMetaData,
                sResName, false);
        if(CMyString.isEmpty(sResFullPahtName))return null;
        

        String sAbsolutePathUpldateFileName = filesMan.getNextFilePathName(
                FilesMan.FLAG_UPLOAD, sFileExt);
        
        
        String sUploadSrc = CMyFile
                .extractFileName(sAbsolutePathUpldateFileName);        
        if (sResFullPahtName.startsWith("http")) {
            CMy3WLib.getFile(sResFullPahtName, sAbsolutePathUpldateFileName);
        } else if (CMyFile.fileExists(sResFullPahtName)) {
            CMyFile.copyFile(sResFullPahtName, sAbsolutePathUpldateFileName);
        } else {
            return null;
        }

        return sUploadSrc;
    }

    private void initLogger(int _nDstChannelId) throws Exception {
        // ��ʼ������Ǩ����־����
        m_oDLogger = new DataMigrationLogger(m_oDBMgr.getSrcNameOfDB(),
                _nDstChannelId);

        // ��ʼ��������Ϣ�����־����
        String sLogFilePath = filesMan.getPathConfigValue(FilesMan.FLAG_NORMAL,
                FilesMan.PATH_LOCAL);
        sLogFilePath = CMyString
                .setStrEndWith(sLogFilePath, File.separatorChar);
        String sLogFileName = sLogFilePath + "exchange_"
                + m_oDBMgr.getSrcNameOfDB() + ".log";

        PatternLayout layout = new PatternLayout(
                "[ExchangeLog] %m - %d - %-5p %x  - %c%l -%-4r [%t] %n");
        WriterAppender appender = new WriterAppender(layout,
                new FileOutputStream(sLogFileName, true));
        appender.setEncoding("UTF-8");
        logger.removeAllAppenders();
        logger.addAppender(appender);

    }

    private int[] queryMinAndMaxId(String _sSQL) throws Exception {
        Connection oConn = null;
        PreparedStatement oPreStmt = null;
        ResultSet result = null;

        try {
            oConn = m_oDBMgr.getConnection();
            oPreStmt = oConn.prepareStatement(_sSQL);
            result = oPreStmt.executeQuery();
            if (result.next()) {
                return new int[] { result.getInt(1), result.getInt(2) };
            }
            throw new WCMException("Not find![SQL=" + _sSQL + "]");
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_WCMEXCEPTION,
                    "Failed to query data![SQL=" + _sSQL + "]", ex);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    logger.error("Failed to close result", e);
                }
            }
            if (oPreStmt != null) {
                try {
                    oPreStmt.close();
                } catch (Exception ex) {
                    logger.error("Failed to close prepared statement", ex);
                }
            }
            if (oConn != null) {
                m_oDBMgr.freeConnection(oConn);
            }
        }

    }

    protected void warn(String _sInfo) throws WCMException {
        error(_sInfo, null);
    }

    protected void warn(String _sInfo, Exception _ex) throws WCMException {
        logger.error(_sInfo, _ex);
    }

    protected void error(String _sInfo) throws WCMException {
        error(_sInfo, null);
    }

    protected void error(String _sInfo, Exception _ex) throws WCMException {
        logger.error(_sInfo, _ex);
        throw new WCMException(_sInfo, _ex);
    }

    /**
     * ����ͼ�Ļ����е�ͼƬ������ͼ����������Դ������·��
     * 
     * @param _oMetaData
     *            ���ݴӱ�Ǩ�Ƶ����ݿ��ж�ȡ���ĸ��ֶ���Ϣ�����Ԫ���ݶ���(MetaViewData)
     * @param _sResName
     *            ͼ�Ļ���������HTML����е�����ֵ������IMG��ǩ�е�Src���ԣ�
     * @param _bAppendix
     *            �Ƿ����ĵ��������ļ�����true����ʾ�Ǹ��� false����ʾ���ĵ�
     * @return ���ַ��ط�ʽ��
     *         1�����Ǩ�Ƴ������ͨ������·�����ʵ��������ļ�������һ������ȫ·�������磺/home/web/data/img/10234.
     *         jpg
     *         2�����Ǩ�Ƴ����ܷ�������·����ֻ��ͨ���������ط�ʽ����ͼƬ�����Է���һ��Ǩ�Ƴ���ɷ��ʵ���··�������磺http://www
     *         .trs.com.cn/news/1.jpg
     * 
     * @throws Exception
     */
    protected abstract String makeResFullPathName(MetaViewData _oMetaData,
            String _sResName, boolean _bAppendix) throws Exception;
    
    /**
     * �����ý����MAS�ɷ��ʵ�·��
     * @param _oMetaData
     * @param _sMediaFileName
     * @return
     * @throws Exception
     */
    protected abstract String makeMedialPathName(MetaViewData _oMetaData,
            String _sMediaFileName) throws WCMException;

    /**
     * �����ĵ�֮ǰԤ�������ݣ�ʵ���߿��Ը�д�������
     * 
     * @param _oMetaData
     */
    protected void doSomethingBeforeSave(MetaViewData _oMetaData) {

    }

    /**
     * �����ĵ�֮����һЩ�����ݣ�ʵ���߿��Ը�д�������
     * 
     * @param _document
     */
    protected void doSomethingAfterSave(MetaViewData _oMetaData) {

    }

    private MetaView findViewByChannel(Channel _channel) throws WCMException {
        MetaView view = m_oMetaViewEmployerMgr.getViewOfEmployer(_channel);
        if (view == null) {
            throw new WCMException(I18NMessage.get(
                    MetaDataCenterServiceProvider.class,
                    "MetaDataCenterServiceProvider.label37",
                    "ָ����Ŀû��������ͼ��[��Ŀ��Ϣ=")
                    + _channel + "]");
        }
        return view;
    }

    private void renderFields(MetaViewData _oViewData) throws Exception {
        // �������еķ��෨�ֶ�
        Map hFields = m_oDataDefCacheMgr.getMetaViewFields(_oViewData
                .getMetaView().getId());
        Iterator iter = hFields.values().iterator();
        String sFieldName, sFieldValue;
        while (iter.hasNext()) {
            MetaViewField field = (MetaViewField) iter.next();
            if (field == null || !field.isFromMainTable())
                continue;

            switch (field.getType()) {
            case MetaDataConstants.FIELD_TYPE_HTML:
            case MetaDataConstants.FIELD_TYPE_HTML_CHAR:
                sFieldName = field.getName();
                sFieldValue = _oViewData.getFinalPropertyAsString(sFieldName);
                if (CMyString.isEmpty(sFieldValue)) {
                    break;
                }
                dowithHtmlContent(_oViewData, sFieldName);
                break;
            case MetaDataConstants.FIELD_TYPE_APPENDIX:
                sFieldName = field.getName();
                sFieldValue = _oViewData.getFinalPropertyAsString(sFieldName);
                if (CMyString.isEmpty(sFieldValue)) {
                    break;
                }
                // �����ļ�������ɹ���������ȥ
                String sWCMFileName = saveFile(_oViewData, sFieldValue, false);
                if(sWCMFileName != null)
                    _oViewData.setProperty(sFieldName, sWCMFileName);
                break;
            case 20://�����ĳ�������ʾ��һ����Ƶ�ֶΣ����ǵ������İ汾û�м���������ƣ���ʱ��ħ�����ְ�
                // �������Ƶ����MAS�������Ƿ���ȷ
                if(m_sMASURL == null || m_sMASURL.length() == 0){
                    throw new WCMException("û��ָ��MAS�������Ϣ!");
                }
                
                // ��ȡ�����ݿ��ѯ���Ķ�ý����Ϣ
                sFieldName = field.getName();
                sFieldValue = _oViewData.getFinalPropertyAsString(sFieldName);
                
                // ���ݶ�ȡ������Ϣת����MAS���õ���Ϣ
                String sMediaFileName = makeMedialPathName(_oViewData, sFieldValue);
                
                // ��MAS�б�������Ƶ��Ϣ����ȡ������ID����¼��Ԫ�����ֶ���
                int nMasId = MediaHelper.createMedia(m_sMASURL, m_nDirId, sMediaFileName);
                _oViewData.setProperty(sFieldName, nMasId);
            default:
                break;
            }
        }
    }
}
