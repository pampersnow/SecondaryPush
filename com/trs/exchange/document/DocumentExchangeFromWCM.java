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
            // ͼƬ����
            new WCMFilter(
                    "WCMAppendix",
                    "AppDocId=? and AppFlag=20",
                    "APPSERN",
                    "SrcFile,AppFile,FileExt, AppFileType, AppFlag,  AppDesc, AppLinkAlt, AppProp,AppTime,AppAuthor,CRUSER,CrTime"),
            // �ļ�����
            new WCMFilter(
                    "WCMAppendix",
                    "AppDocId=? and AppFlag=10",
                    "APPSERN",
                    "SrcFile,AppFile,FileExt, AppFileType, AppFlag,  AppDesc, AppLinkAlt, AppProp,AppTime,AppAuthor,CRUSER,CrTime"),
            // ���Ӹ���
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
     * ����ͼ�Ļ����е�ͼƬ������ͼ����������Դ������·��
     * 
     * @param _document
     *            ���ݴӱ�Ǩ�Ƶ����ݿ��ж�ȡ���ĸ��ֶ���Ϣ�����Document����
     * @param _sResName
     *            ͼ�Ļ���������HTML����е�����ֵ������IMG��ǩ�е�Src���ԣ�
     * @param _bAppendix
     *            �Ƿ����ĵ��������ļ�����true����ʾ�Ǹ��� false����ʾ���ĵ�
     * @return ���ַ��ط�ʽ��
     *         1�����Ǩ�Ƴ������ͨ������·�����ʵ��������ļ�������һ������ȫ·�������磺/home/web/data/img/10234.
     *         jpg
     *         2�����Ǩ�Ƴ����ܷ�������·����ֻ��ͨ���������ط�ʽ����ͼƬ�����Է���һ��Ǩ�Ƴ���ɷ��ʵ���··�������磺http://www
     *         .trs.com.cn/news/10234.jpg
     * 
     * @throws Exception
     */
    protected String makeResFullPathName(Document _document, String _sResName,
            boolean _bAppendix) throws Exception {
        try {
            String sFileName = CMyFile.extractFileName(_sResName);
            
            // ��Ϊ��WCM���ļ�����Ŀ��WCM�ļ�·���������һ��
            // Ψһ��������WCMData��ͬ����
            String sAbsolutePath = m_oDstWCMFilesMan.mapFilePath(sFileName,
                    FilesMan.PATH_LOCAL);
            int nPos = sAbsolutePath.toUpperCase().indexOf("WCMDATA");
            if (nPos <= 0)
                throw new WCMException("Ŀ��WCMData��Ԥ�ڹ���һ�£�[" + sAbsolutePath
                        + "]");
            return m_sWCMDataPath + sAbsolutePath.substring(nPos + 8)
                    + sFileName;
        } catch (Exception e) {
            warn("Fail to dowith the file["+_sResName+"]!Document:["+_document+"]", e);
            return _sResName;
        }
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
    protected WCMFilter[] makeFilterQueryAppendixOfDocument(Document _document) {
        // WCM�����Ǹ��汾��������Ϣ���Ǵ����Appendix���У�����ʵ������ӿھͿ�����
        // ���ǵ�ÿƪ�ĵ��ĸ���SQL�����ƣ�����һ���ڲ�������ÿ�ν�������һ�²���

        // �趨��ǰ���ĵ�ID����
        for (int i = 0; i < m_pFilterForQueryAppendixes.length; i++) {
            WCMFilter filter = m_pFilterForQueryAppendixes[i];
            filter.addSearchValues(0, _document.getOutUpId());
        }

        return m_pFilterForQueryAppendixes;
    }

    /**
     * ���ݴ�Դ���ݿ��ȡ���ĵ�ĳ�����ݽ���������
     * 
     * @param _document
     *            ��Դ���ݿ��¼�н��������ĵ�����
     * @return List<Appendix> ���ظ������ϣ�����˳��
     */
    protected List makeAppendixesOfDocument(Document _document)
            throws Exception {
        // ����null����ʾ������ĳ���ֶ��е�ֵiǨ�ƣ��������һ����ʽҲ����null����ʾ��Ǩ�Ƹ���
        return null;
    }

}
