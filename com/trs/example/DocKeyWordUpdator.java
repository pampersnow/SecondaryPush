package com.trs.example;

import com.trs.DreamFactory;
import com.trs.components.wcm.content.domain.IDocKeywordMgr;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.components.wcm.content.persistent.Documents;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyString;

public class DocKeyWordUpdator {

    public void appendKeyword() throws Exception {
        DBManager dbMgr = DBManager.getDBManager();
        String[] pUpdateSQL = { "update WCMDocument set DocKeywords=?"
                + " where DocId=?" };
        String[] pKeyword = { "刘金国", "孟建柱", "黄明", "蔡安季", "孟宏伟", "张新枫", "杨焕宁",
                "陈智敏", "周永康", "李伟" };
        for (int i = pKeyword.length - 1; i >= 0; i++) {
            WCMFilter filter = new WCMFilter("",
                    "DocTitle like ? and DocKeyword not like ?", "",
                    "DocId, DocKeywords, DocChannel, DocRelTime,CrTime");
            filter.addSearchValues("%" + pKeyword[i] + "%");
            filter.addSearchValues("%" + pKeyword[i] + "%");

            Documents documents = Documents.openWCMObjs(null, filter);
            for (int nDocumentIndex = 0, nSize = documents.size(); nDocumentIndex < nSize; nDocumentIndex++) {
                Document document = (Document) documents.getAt(nDocumentIndex);
                if (document == null)
                    continue;
                String sDocKeywords = document.getKeywords();
                String sNewKeywords = pKeyword[i];
                if (!CMyString.isEmpty(sDocKeywords)) {
                    sNewKeywords += ";";
                }
                sNewKeywords += sDocKeywords;
                
                // 更新文档的关键词
                dbMgr.sqlExecuteUpdate(pUpdateSQL, new Object[] { sNewKeywords,
                        document.getKey() });
                
                // 更新关联关系
                IDocKeywordMgr docKeywordMgr = (IDocKeywordMgr) DreamFactory
                        .createObjectById("IDocKeywordMgr");
                docKeywordMgr.updateDocKeywords(document, sDocKeywords,
                        sNewKeywords);
            }

        }
    }
}
