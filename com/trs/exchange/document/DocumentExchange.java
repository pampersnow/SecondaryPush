package com.trs.exchange.document;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import com.trs.DreamFactory;
import com.trs.cms.auth.persistent.User;
import com.trs.components.wcm.content.domain.AppendixMgr;
import com.trs.components.wcm.content.domain.DocumentMgr;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.ex.util.CMy3WLib;
import com.trs.exchange.DataMigrationLogger;
import com.trs.exchange.ExchangeHelper;
import com.trs.exchange.MyDBManager;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.support.file.FilesMan;
import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.ExceptionNumber;
import com.trs.infra.util.html.HtmlElement;
import com.trs.infra.util.html.HtmlElementFinder;

public abstract class DocumentExchange {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(DocumentExchange.class);

    protected MyDBManager m_oDBMgr = null;

    private DocumentMgr m_oDocumentMgr = null;

    private AppendixMgr m_oAppendixMgr = null;

    protected FilesMan filesMan = null; // 取文件管理者

    protected DataMigrationLogger m_oDLogger = null;

    public DocumentExchange(MyDBManager _dbMgr) {
        m_oDBMgr = _dbMgr;
        m_oDocumentMgr = (DocumentMgr) DreamFactory
                .createObjectById("DocumentMgr");
        m_oAppendixMgr = (AppendixMgr) DreamFactory
                .createObjectById("AppendixMgr");
        filesMan = FilesMan.getFilesMan();
    }

    public void createDocuments(User _oCrUser, int _nDstChannelId,
            WCMFilter _filterForQuerySrcData, String _sIdFieldName,
            boolean _bOrderById) throws Exception {
        // 0 定义日志文件并读取上一次结果
        // >=0：表示从指定记录ID开始迁移
        // -1：表示已迁移完
        initLogger(_nDstChannelId);
        int nLasMigrationId = m_oDLogger.readLastInfo();
        if (nLasMigrationId == -1) {
            logger.warn("栏目[ID=" + _nDstChannelId + "]已经迁移完，重复执行！");
            return;
        }

        // 1 校验指定的栏目是否有效
        Channel dstChannel = Channel.findById(_nDstChannelId);
        if (dstChannel == null) {
            throw new WCMException("指定的栏目无效！[栏目ID=" + _nDstChannelId + "]");
        }
        logger.info("Begin Data Migration to [" + dstChannel + "]..........");
        m_oDLogger.recordStartChannel(_nDstChannelId);

        // 2 分段遍历指定SQL的数据，逐段迁移
        // 2.1 计算本次迁移最小ID和最大ID，需要结合上一次迁移的日志来算
        WCMFilter oQueryMinAndMaxFilter = new WCMFilter(
                _filterForQuerySrcData.getFrom(),
                _filterForQuerySrcData.getWhere(), "", "min(" + _sIdFieldName
                        + "), max(" + _sIdFieldName + ")");
        int[] pTemp = queryMinAndMaxId(oQueryMinAndMaxFilter.toSQL());
        int nMinDataId = pTemp[0], nMaxDataId = pTemp[1];
        if (nMinDataId < nLasMigrationId) {
            nMinDataId = nLasMigrationId;
        }

        // 2.2 从最小ID开始迁移，每次迁移指定条数（不管排序）
        int nMingrationSize = 3000;
        for (int nTempMaxDataId = nMinDataId; nTempMaxDataId <= nMaxDataId; nMinDataId = nTempMaxDataId) {
            nMinDataId = nTempMaxDataId;
            nTempMaxDataId += nMingrationSize;

            // 构造分段迁移的SQL
            WCMFilter filterForQueryRnageData = toRangeFilter(
                    _filterForQuerySrcData, nMinDataId, nTempMaxDataId,
                    _sIdFieldName);
            // 正式迁移数据
            createRangeDocuments(_oCrUser, dstChannel, filterForQueryRnageData);
        }

        // 2.3 根据原始SQL构造计算ID和顺序对应的SQL，逐条再更新WCMChnlDoc中的DocOrder
        // 如果不是按照ID排序，需要重新調整一篇順序
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

    private void createRangeDocuments(User _oCrUser, Channel _dstChannel,
            WCMFilter _filterForQueryRangeSrcData) throws Exception {
        // 2 遍历指定SQL查询到的数据，逐条迁移
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
                // 1 产生新的文档
                Document document = Document.createNewInstance();

                // 2 读取数据（SQL语句的字段别名需要和WCMDocument一致哦！！！！！！！！！！）
                ExchangeHelper.readFromRs(document, result,
                        result.getMetaData(), m_oDBMgr.getDbType());
                nSrcId = document.getOutUpId();
                if (nSrcId <= 0)
                    throw new WCMException("查询语句可能有误，没有指定DocOutupId？[SQL="
                            + _filterForQueryRangeSrcData + "]");

                logger.info("Begin to dowith the data[" + nSrcId + "] to "
                        + _dstChannel + ".....");
                m_oDLogger.recordStartData(nSrcId);

                // 3 设置栏目属性
                document.setChannel(_dstChannel);

                // 4 做后期处理（文档类型、图文混排字段等等）
                // 4.1 文档类型，如果没有设置，默认为图文混排类型
                if (document.getType() <= 0) {
                    document.setType(Document.DOC_TYPE_HTML);
                }
                // 4.2 根据类型做相关处理
                switch (document.getType()) {
                // 4.2.1 处理图文混排中的内容（内容中的图片等等）
                case Document.DOC_TYPE_HTML:
                    dowithHtmlContent(document);
                    break;
                // 4.2.2 处理外部文件
                case Document.DOC_TYPE_FILE:
                    String sFileName = document
                            .getPropertyAsString("DOCFILENAME");
                    String sFileExt = ExchangeHelper.extractFileExt(sFileName);
                    if (ExchangeHelper.isForbidFileExt(sFileExt)) {
                        logger.error("指定文件的后缀名不在白名单允许范围之内！[文件=" + sFileName
                                + "]");
                    } else {
                        String sNewFileName = saveFile(sFileName,
                                ExchangeHelper.extractFileExt(sFileName));
                        document.setProperty("DOCFILENAME", sNewFileName);
                    }
                    break;
                default:
                    break;
                }

                // 5 保存
                // 5.1 保存之前调用内部预处理方法，实现者如果想要加入自己的逻辑，可以覆写这个方法
                doSomethingBeforeSave(document);
                // 5.2 入库保存
                document = m_oDocumentMgr.save(document);
                // 5.3 后期处理
                doSomethingAfterSave(document);

                // 6 处理附件（如果附件处理不成功，作为事务，文档需要同步彻底删除）
                // 附件支持两种方式：1 从数据库查询 2 从字段中解析
                // 不同方式实现不同接口

                // 从数据库中查询
                WCMFilter[] pFilterQueryAppendix = makeFilterQueryAppendixOfDocument(document);
                if (pFilterQueryAppendix != null
                        && pFilterQueryAppendix.length > 0) {
                    for (int nFilterIndex = 0; nFilterIndex < pFilterQueryAppendix.length; nFilterIndex++) {
                        createAppendixes(_oCrUser, document,
                                pFilterQueryAppendix[nFilterIndex]);
                    }
                }
                // 根据文档中的某个属性反解出附件集合，然后逐个保存
                List arAppendixes = makeAppendixesOfDocument(document);
                if (arAppendixes != null && !arAppendixes.isEmpty()) {
                    for (int nAppendixIndex = 0, nAppendixSize = arAppendixes
                            .size(); nAppendixIndex < nAppendixSize; nAppendixIndex++) {
                        Appendix appendix = (Appendix) arAppendixes
                                .get(nAppendixIndex);
                        saveAppendix(document, appendix);
                    }
                }

                // 7 记录日志
                logger.info("END to dowith the data[" + nSrcId + "] to "
                        + _dstChannel + "!");
                m_oDLogger.recordEndData(nSrcId);

            }
        } catch (Exception ex) {
            if(nSrcId>0){
                error("处理这条数据出现问题！[数据ID="+nSrcId+"]", ex);
            }else{
                error("查询数据出现问题？[SQL="+_filterForQueryRangeSrcData+"]", ex);
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

    private void createAppendixes(User _oCrUser, Document _document,
            WCMFilter _filterForQueryAppendix) throws Exception {
        // 2 遍历指定SQL查询到的数据，逐条迁移
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
                // 1 产生新的附件
                Appendix appendix = Appendix.createNewInstance();

                // 2 读取数据（SQL语句的字段别名需要和WCMAppendix一致哦！！！！！！！！！！）
                ExchangeHelper.readFromRs(appendix, result,
                        result.getMetaData(), m_oDBMgr.getDbType());
                logger.info("Begin to dowith the appendix[" + appendix
                        + "] of " + _document + ".....");

                // 4 后期处理（所属文档、文件后缀，附件类型、附件的文件等等）
                saveAppendix(_document, appendix);

                // 7 记录日志
                logger.info("END to dowith the appendix[" + appendix.getId()
                        + "] of " + _document + "!");
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

    private void saveAppendix(Document _document, Appendix _appendix)
            throws WCMException, Exception {
        String sAppFile = _appendix.getFile();
        if (CMyString.isEmpty(sAppFile)) {
            error("指定附件没有设置AppFile属性！[所属文档=" + _document + "]");
        }

        _appendix.setDocId(_document.getId());
        // 4.1 附件类型，如果没有设置，默认为图文混排类型
        if (_appendix.getFlag() <= 0) {
            _appendix.setFlag(Appendix.FLAG_DOCAPD);
        }
        // 4.2 根据类型做相关处理
        String sWCMFileType = null;
        if (_appendix.getFlag() == Appendix.FLAG_DOCAPD) {
            sWCMFileType = FilesMan.FLAG_PROTECTED;
        } else if (_appendix.getFlag() == Appendix.FLAG_DOCPIC) {
            sWCMFileType = FilesMan.FLAG_PROTECTED;
        }
        if (sWCMFileType != null) {
            // 判断后缀有效性
            String sFileExt = _appendix.getFileExt();
            if (CMyString.isEmpty(sFileExt)) {
                sFileExt = ExchangeHelper.extractFileExt(_appendix.getFile());
                _appendix.setFileExt(sFileExt);
            }
            if (sFileExt == null || ExchangeHelper.isForbidFileExt(sFileExt)) {
                error("指定附件的后缀名不在白名单允许范围之内！[文件=" + _appendix.getFile() + "]");
            }

            // WCM中已经有这个文件
            if (FilesMan.isValidFile(sAppFile, sWCMFileType)
                    && filesMan.fileExists(sAppFile)) {

            } else {
                String sFullFileName = makeResFullPathName(_document, sAppFile,
                        true);
                String sNewFileName = saveFile(sFullFileName, sFileExt);
                if (sNewFileName != null) {
                    _appendix.setFile(sNewFileName);
                }
            }
        }

        // 5 保存
        m_oAppendixMgr.addAppendix(_document, _appendix);
    }

    /**
     * 构造查询附件的SQL，数据ID用?代替，以便提高效率；
     * SQL构造方法和构造文档查询SQL类似，使用别名，将Select中字段和WCMAppendix表字段名一致
     * 
     * @return WCM支持多种附件（WCM文件名） 图片附件的SQL：select 20 AppFlag, MyFileDesc AppDesc,
     *         MyFileDesc AppFileAlt, MyFileName AppFile from MyFiles where
     *         ContentId=?; 文件附件的SQL：select 10 AppFlag, MyFileDesc AppDesc,
     *         MyFileDesc AppFileAlt, MyFileName AppFile from MyFiles where
     *         ContentId=?; 链接附件的SQL：select 40 AppFlag, MyFileDesc AppDesc,
     *         MyFileDesc AppFileAlt, MyFileName AppFile from MyFiles where
     *         ContentId=?;
     */
    protected abstract WCMFilter[] makeFilterQueryAppendixOfDocument(
            Document _document);

    /**
     * 根据从源数据库读取的文档某个数据解析出附件
     * 
     * @param _document
     *            从源数据库记录中解析出的文档对象
     * @return 返回附件集合（包含顺序）
     */
    protected abstract List makeAppendixesOfDocument(Document _document)
            throws Exception;

    private void dowithHtmlContent(Document _document) throws WCMException {
        String _sHTMLContent = _document.getHtmlContent();

        String[] arResTagName = { "img", "table", "td", "script", "a"};
        String[] arResSrcName = { "src", "BACKGROUND", "BACKGROUND", "scr",
                "href"};

        int nTagSize = (arResTagName.length > arResSrcName.length ? arResTagName.length
                : arResSrcName.length);

        HtmlElementFinder imgFinder = null; // 用于在Html文本中查找IMG元素
        HtmlElement element = null;
        HashMap hImgRecs = new HashMap();

        String sResName, sIgnore, sKey;
        boolean bFind = false;
        try {
            // 03. 依次处理各个可能的页面附件元素
            for (int nTagIndex = 0; nTagIndex < nTagSize; nTagIndex++) {
                String currTagName = arResTagName[nTagIndex];

                String currTagSrcName = arResSrcName[nTagIndex];
                imgFinder = new HtmlElementFinder(_sHTMLContent);
                // while - 遍历/找到下一个可能是附件的页面元素
                while (true) {
                    // 保存上一次的元素
                    imgFinder.putElement(element);
                    // 继续寻找下一个元素
                    element = imgFinder.findNextElement(currTagName, true);
                    if (element == null) {
                        break;
                    }

                    // 判断是否符合条件
                    sIgnore = element.getAttributeValue("IGNORE");
                    if ("1".equals(sIgnore))
                        continue;
                    sResName = element.getAttributeValue(currTagSrcName);
                    if (sResName == null
                            || (sResName = sResName.trim()).length() == 0)
                        continue;

                    // 检查之前是否处理过
                    sKey = sResName.toLowerCase();
                    String sUploadName = (String) hImgRecs.get(sKey);
                    if (sUploadName != null) {
                        element.setAttribute(currTagSrcName, sUploadName);
                        continue;
                    }

                    // 如果没有处理需要进行预处理（从网络或者可访问本地目录将文件拷贝到WCMData的Upload中）
                    // 判断指定文件后缀是否需要处理
                    String sFileExt = ExchangeHelper.extractFileExt(sResName);

                    // 针对A元素，目前仅仅处理doc、docx、xls等常见文件
                    if (currTagName.equalsIgnoreCase("A")
                            && !(sFileExt.endsWith("doc")
                                    || sFileExt.endsWith("docx") || sFileExt
                                        .endsWith("xls"))) {
                        continue;
                    }

                    if (sFileExt == null
                            || ExchangeHelper.isForbidFileExt(sFileExt)) {
                        logger.error("指定文件的后缀名不在白名单允许范围之内！[文件=" + sResName
                                + "]");
                        continue;
                    }

                    // 构造完整路径（本地或者HTTP），抽象接口，由子类实现
                    String sResFullPahtName = makeResFullPathName(_document,
                            sResName, false);
                    if (sResFullPahtName != null) {
                        try {
                            // 保存下载指定的文件
                            sUploadName = saveFile(sResFullPahtName, sFileExt);
                        }
                        // 拦截异常，便于输出上下文信息
                        catch (Exception e) {
                            throw new WCMException(
                                    "处理图文混排中的资源出现异常！[File=" + sResFullPahtName
                                            + "][" + _document + "]", e);
                        }
                        if (FilesMan.isValidFile(sUploadName,
                                FilesMan.FLAG_UPLOAD)) {
                            bFind = true;
                        }
                    }

                    // 拷贝或者下载文件
                    element.setAttribute(currTagSrcName, sUploadName);

                    // 记录已经处理过的文件
                    hImgRecs.put(sKey, sUploadName);
                }// END While

                _sHTMLContent = imgFinder.getContent();
            } // end of for (arTagName)

            if (bFind) {
                _document.setHtmlContent(_sHTMLContent);
                _document.setContent(ExchangeHelper
                        .parserInnerText(_sHTMLContent));
            }
        } catch (Exception ex) {
            throw new WCMException(ExceptionNumber.ERR_WCMEXCEPTION,
                    "保存图文混排内容失败!", ex);
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
                dbMgrForWCM.sqlExecuteUpdate(
                        new String[] { "update WCMChnlDoc set DocOrder=?"
                                + " where ChnlId=? and DocId in("
                                + "select DocId from WCMDocument where DocOutupId=？)" }, //
                        new int[] { nDocOrder, _dstChannel.getId(), nSrcId });
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

    private String saveFile(String _sResFullPahtName, String _sFileExt)
            throws Exception {

        String sAbsolutePathUpldateFileName = filesMan.getNextFilePathName(
                FilesMan.FLAG_UPLOAD, _sFileExt);
        String sUploadSrc = CMyFile
                .extractFileName(sAbsolutePathUpldateFileName);
        if (_sResFullPahtName.startsWith("http")) {
            CMy3WLib.getFile(_sResFullPahtName, sAbsolutePathUpldateFileName);
        } else if (CMyFile.fileExists(_sResFullPahtName)) {
            CMyFile.copyFile(_sResFullPahtName, sAbsolutePathUpldateFileName);
        } else {
            sUploadSrc = _sResFullPahtName;
        }

        return sUploadSrc;
    }

    private void initLogger(int _nDstChannelId) throws Exception {
        // 初始化数据迁移日志对象
        m_oDLogger = new DataMigrationLogger(m_oDBMgr.getSrcNameOfDB(),
                _nDstChannelId);

        // 初始化调试信息输出日志对象
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
     * 处理图文混排中的图片、背景图、附件等资源的完整路径
     * 
     * @param _document
     *            根据从被迁移的数据库中读取到的各字段信息构造的Document对象
     * @param _sResName
     *            图文混排内容中HTML标记中的属性值（比如IMG标签中的Src属性）
     * @param _bAppendix
     *            是否是文档附件的文件名，true：表示是附件 false：表示是文档
     * @return 两种返回方式：
     *         1、如果迁移程序可以通过物理路径访问到关联的文件，返回一个物理全路径，比如：/home/web/data/img/10234.
     *         jpg
     *         2、如果迁移程序不能访问物理路径，只能通过网络下载方式保存图片，所以返回一个迁移程序可访问的网路路径，比如：http://www
     *         .trs.com.cn/news/1.jpg
     * 
     * @throws Exception
     */
    protected abstract String makeResFullPathName(Document _document,
            String _sResName, boolean _bAppendix) throws Exception;

    /**
     * 保存文档之前预处理数据，实现者可以覆写这个方法
     * 
     * @param _document
     */
    protected void doSomethingBeforeSave(Document _document) {

    }

    /**
     * 保存文档之后做一些操作据，实现者可以覆写这个方法
     * 
     * @param _document
     */
    protected void doSomethingAfterSave(Document _document) {

    }
}
