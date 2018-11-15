package com.trs.exchange.metadata;

import java.io.File;
import java.util.List;

import com.trs.components.metadata.center.MetaViewData;
import com.trs.exchange.MyDBManager;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.support.file.FilesMan;
import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;

public class MetaDataExchangeFromWCM extends MetaDataExchange {
    private String m_sWCMDataPath = "g:/wcmdata/";

    private WCMFilter[] m_pFilterForQueryAppendixes = {
            // 图片附件
            new WCMFilter(
                    "WCMAppendix",
                    "AppDocId=? and AppFlag=20",
                    "APPSERN",
                    "SrcFile,AppFile,FileExt, AppFileType, AppFlag,  AppDesc, AppLinkAlt, AppProp,AppTime,AppAuthor,CRUSER,CrTime"),
            // 文件附件
            new WCMFilter(
                    "WCMAppendix",
                    "AppDocId=? and AppFlag=10",
                    "APPSERN",
                    "SrcFile,AppFile,FileExt, AppFileType, AppFlag,  AppDesc, AppLinkAlt, AppProp,AppTime,AppAuthor,CRUSER,CrTime"),
            // 链接附件
            new WCMFilter(
                    "WCMAppendix",
                    "AppDocId=? and AppFlag=40",
                    "APPSERN",
                    "SrcFile,AppFile,FileExt, AppFileType, AppFlag,  AppDesc, AppLinkAlt, AppProp,AppTime,AppAuthor,CRUSER,CrTime") };

    private FilesMan m_oDstWCMFilesMan = null;

    public void setWCMDataRootPath(String _sWCMDataRootPath) {
        m_sWCMDataPath = _sWCMDataRootPath;
        m_sWCMDataPath = CMyString.setStrEndWith(_sWCMDataRootPath,
                File.separatorChar);
        m_oDstWCMFilesMan = FilesMan.getFilesMan();
    }

    public MetaDataExchangeFromWCM(MyDBManager _dbMgr) {
        super(_dbMgr);
    }

    protected WCMFilter[] makeFilterQueryAppendixOfDocument(
            MetaViewData _oMetaData) {
        // WCM无论那个版本，附件信息都是存放在Appendix表中，所以实现这个接口就可以了
        // 考虑到每篇文档的附件SQL都类似，构造一个内部变量，每次仅仅调整一下参数

        // 设定以前的文档ID参数
        for (int i = 0; i < m_pFilterForQueryAppendixes.length; i++) {
            WCMFilter filter = m_pFilterForQueryAppendixes[i];
            try {
                filter.addSearchValues(0, _oMetaData.getDocument().getOutUpId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return m_pFilterForQueryAppendixes;
    }

    protected List makeAppendixesOfDocument(MetaViewData _oMetaData)
            throws Exception {
        return null;
    }

    protected String makeResFullPathName(MetaViewData _oMetaData,
            String _sResName, boolean _bAppendix) throws Exception {
        try {
            String sFileName = CMyFile.extractFileName(_sResName);

            // 因为是WCM的文件，和目标WCM文件路径构造规则一样
            // 唯一区别在于WCMData不同导致
            String sAbsolutePath = m_oDstWCMFilesMan.mapFilePath(sFileName,
                    FilesMan.PATH_LOCAL);
            int nPos = sAbsolutePath.toUpperCase().indexOf("WCMDATA");
            if (nPos <= 0)
                throw new WCMException("目标WCMData和预期规则不一致！[" + sAbsolutePath
                        + "]");
            return m_sWCMDataPath + sAbsolutePath.substring(nPos + 8)
                    + sFileName;
        } catch (Exception e) {
            warn("Fail to dowith the file[" + _sResName + "]!Document:["
                    + _oMetaData + "]", e);
            return _sResName;
        }
    }

    protected String makeMedialPathName(MetaViewData _oMetaData,
            String _sMediaFileName) throws WCMException {
        // TODO Auto-generated method stub
        return null;
    }

}
