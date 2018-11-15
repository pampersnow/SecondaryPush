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
        String[] pKeyword = { "�����", "�Ͻ���", "����", "�̰���", "�Ϻ�ΰ", "���·�", "�����",
                "������", "������", "��ΰ" };
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
                
                // �����ĵ��Ĺؼ���
                dbMgr.sqlExecuteUpdate(pUpdateSQL, new Object[] { sNewKeywords,
                        document.getKey() });
                
                // ���¹�����ϵ
                IDocKeywordMgr docKeywordMgr = (IDocKeywordMgr) DreamFactory
                        .createObjectById("IDocKeywordMgr");
                docKeywordMgr.updateDocKeywords(document, sDocKeywords,
                        sNewKeywords);
            }

        }
    }
}
