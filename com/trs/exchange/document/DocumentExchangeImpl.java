package com.trs.exchange.document;

import java.util.List;

import com.trs.components.wcm.content.persistent.Document;
import com.trs.exchange.MyDBManager;
import com.trs.infra.persistent.WCMFilter;

public class DocumentExchangeImpl extends DocumentExchange {
    
    public DocumentExchangeImpl(MyDBManager _dbMgr) {
        super(_dbMgr);
    }
    
    
    /**
     * ����ͼ�Ļ����е�ͼƬ������ͼ����������Դ������·��
     * @param _document
     * ���ݴӱ�Ǩ�Ƶ����ݿ��ж�ȡ���ĸ��ֶ���Ϣ�����Document����
     * @param _sResName
     * ͼ�Ļ���������HTML����е�����ֵ������IMG��ǩ�е�Src���ԣ�
     * @param _bAppendix
     *            �Ƿ����ĵ��������ļ�����true����ʾ�Ǹ��� false����ʾ���ĵ� 
     * @return
     * ���ַ��ط�ʽ��
     * 1�����Ǩ�Ƴ������ͨ������·�����ʵ��������ļ�������һ������ȫ·�������磺/home/web/data/img/10234.jpg
     * 2�����Ǩ�Ƴ����ܷ�������·����ֻ��ͨ���������ط�ʽ����ͼƬ�����Է���һ��Ǩ�Ƴ���ɷ��ʵ���··�������磺http://www.trs.com.cn/news/10234.jpg
     * 
     * @throws Exception
     */
    protected String makeResFullPathName(Document _document,  String _sResName, boolean _bAppendix) throws Exception {
        //  ��ʽ1
        // return "/home/web/data/img/" + _sResName;

        //  ��ʽ2
        // return "http://www.trs.com.cn/news/" + _sResName;

        return null;
    }

    /**
     * �����ѯ������SQL������ID��?���棬�Ա����Ч�ʣ�     
     * SQL���췽���͹����ĵ���ѯSQL���ƣ�ʹ�ñ�������Select���ֶκ�WCMAppendix���ֶ���һ��
     * @return
     * WCM֧�ֶ��ָ�����WCM�ļ�����
     * ͼƬ������SQL��select 20 AppFlag, MyFileDesc AppDesc, MyFileDesc AppLinkAlt, MyFileName AppFile from MyFiles where ContentId=?;
     * �ļ�������SQL��select 10 AppFlag, MyFileDesc AppDesc, MyFileDesc AppLinkAlt, MyFileName AppFile from MyFiles where ContentId=?;
     * ���Ӹ�����SQL��select 40 AppFlag, MyFileDesc AppDesc, MyFileDesc AppLinkAlt, MyFileName AppFile from MyFiles where ContentId=?;
     */
    protected WCMFilter[] makeFilterQueryAppendixOfDocument(Document _document) {
//        // ͼƬ����
//        WCMFilter filter1 = new WCMFilter("MyFiles", "ContentId=?", ""
//                , "20 AppFlag, MyFileDesc AppDesc, MyFileDesc AppLinkAlt, MyFileName AppFile");
//        filter1.addSearchValues(_document.getOutUpId());
//        
//        return new WCMFilter[]{filter1};
        
        // ����null����ʾ������SQL��ʽǨ�ƣ��������һ�ַ�ʽҲ����Null����ʾ��Ǩ�Ƹ���        
        return null;
    }

    /**
     * ���ݴ�Դ���ݿ��ȡ���ĵ�ĳ�����ݽ���������
     * @param _document
     * ��Դ���ݿ��¼�н��������ĵ�����
     * @return List<Appendix>
     * ���ظ������ϣ�����˳��
     */
    protected List makeAppendixesOfDocument(Document _document)throws Exception{
//        String sAppendixInfo = _document.getPropertyAsString("MyFiles");
//        String[] pAppendixInfo = sAppendixInfo.split(";");
//        ArrayList arResult = new ArrayList(pAppendixInfo.length);
//        for (int i = 0; i < pAppendixInfo.length; i++) {
//            Appendix appendix = Appendix.createNewInstance();
//            appendix.setFile(pAppendixInfo[i]);            
//            arResult.add(appendix);
//        }
//        return arResult;
        
        
        // ����null����ʾ������ĳ���ֶ��е�ֵiǨ�ƣ��������һ����ʽҲ����null����ʾ��Ǩ�Ƹ���
        return null;
    }

}
