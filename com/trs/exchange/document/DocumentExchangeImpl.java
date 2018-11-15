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
     * 处理图文混排中的图片、背景图、附件等资源的完整路径
     * @param _document
     * 根据从被迁移的数据库中读取到的各字段信息构造的Document对象
     * @param _sResName
     * 图文混排内容中HTML标记中的属性值（比如IMG标签中的Src属性）
     * @param _bAppendix
     *            是否是文档附件的文件名，true：表示是附件 false：表示是文档 
     * @return
     * 两种返回方式：
     * 1、如果迁移程序可以通过物理路径访问到关联的文件，返回一个物理全路径，比如：/home/web/data/img/10234.jpg
     * 2、如果迁移程序不能访问物理路径，只能通过网络下载方式保存图片，所以返回一个迁移程序可访问的网路路径，比如：http://www.trs.com.cn/news/10234.jpg
     * 
     * @throws Exception
     */
    protected String makeResFullPathName(Document _document,  String _sResName, boolean _bAppendix) throws Exception {
        //  方式1
        // return "/home/web/data/img/" + _sResName;

        //  方式2
        // return "http://www.trs.com.cn/news/" + _sResName;

        return null;
    }

    /**
     * 构造查询附件的SQL，数据ID用?代替，以便提高效率；     
     * SQL构造方法和构造文档查询SQL类似，使用别名，将Select中字段和WCMAppendix表字段名一致
     * @return
     * WCM支持多种附件（WCM文件名）
     * 图片附件的SQL：select 20 AppFlag, MyFileDesc AppDesc, MyFileDesc AppLinkAlt, MyFileName AppFile from MyFiles where ContentId=?;
     * 文件附件的SQL：select 10 AppFlag, MyFileDesc AppDesc, MyFileDesc AppLinkAlt, MyFileName AppFile from MyFiles where ContentId=?;
     * 链接附件的SQL：select 40 AppFlag, MyFileDesc AppDesc, MyFileDesc AppLinkAlt, MyFileName AppFile from MyFiles where ContentId=?;
     */
    protected WCMFilter[] makeFilterQueryAppendixOfDocument(Document _document) {
//        // 图片附件
//        WCMFilter filter1 = new WCMFilter("MyFiles", "ContentId=?", ""
//                , "20 AppFlag, MyFileDesc AppDesc, MyFileDesc AppLinkAlt, MyFileName AppFile");
//        filter1.addSearchValues(_document.getOutUpId());
//        
//        return new WCMFilter[]{filter1};
        
        // 返回null，表示不按照SQL方式迁移，如果另外一种方式也返回Null，表示不迁移附件        
        return null;
    }

    /**
     * 根据从源数据库读取的文档某个数据解析出附件
     * @param _document
     * 从源数据库记录中解析出的文档对象
     * @return List<Appendix>
     * 返回附件集合（包含顺序）
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
        
        
        // 返回null，表示不按照某个字段中的值i迁移，如果另外一个方式也返回null，表示不迁移附件
        return null;
    }

}
