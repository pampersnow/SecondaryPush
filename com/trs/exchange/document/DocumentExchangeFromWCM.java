package com.trs.exchange.document;

import java.io.File;
import java.util.List;

import com.trs.components.wcm.content.persistent.Document;
import com.trs.exchange.MyDBManager;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.support.file.FilesMan;
import com.trs.infra.util.CMyFile;
import com.trs.infra.util.CMyString;

public class DocumentExchangeFromWCM extends DocumentExchange {
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

    public DocumentExchangeFromWCM(MyDBManager _dbMgr) {
        super(_dbMgr);
    }

    public void setWCMDataRootPath(String _sWCMDataRootPath) {
        m_sWCMDataPath = _sWCMDataRootPath;
        m_sWCMDataPath = CMyString.setStrEndWith(_sWCMDataRootPath,
                File.separatorChar);
        m_oDstWCMFilesMan = FilesMan.getFilesMan();
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
     *         .trs.com.cn/news/10234.jpg
     * 
     * @throws Exception
     */
    protected String makeResFullPathName(Document _document, String _sResName,
            boolean _bAppendix) throws Exception {
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
            warn("Fail to dowith the file["+_sResName+"]!Document:["+_document+"]", e);
            return _sResName;
        }
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
    protected WCMFilter[] makeFilterQueryAppendixOfDocument(Document _document) {
        // WCM无论那个版本，附件信息都是存放在Appendix表中，所以实现这个接口就可以了
        // 考虑到每篇文档的附件SQL都类似，构造一个内部变量，每次仅仅调整一下参数

        // 设定以前的文档ID参数
        for (int i = 0; i < m_pFilterForQueryAppendixes.length; i++) {
            WCMFilter filter = m_pFilterForQueryAppendixes[i];
            filter.addSearchValues(0, _document.getOutUpId());
        }

        return m_pFilterForQueryAppendixes;
    }

    /**
     * 根据从源数据库读取的文档某个数据解析出附件
     * 
     * @param _document
     *            从源数据库记录中解析出的文档对象
     * @return List<Appendix> 返回附件集合（包含顺序）
     */
    protected List makeAppendixesOfDocument(Document _document)
            throws Exception {
        // 返回null，表示不按照某个字段中的值i迁移，如果另外一个方式也返回null，表示不迁移附件
        return null;
    }

}
