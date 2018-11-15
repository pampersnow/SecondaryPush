/**
 * �������ҵ���߼���
 * ��ȡϵͳ��������(����ѯ)
 * �жϵ�ǰ�û��Ƿ���Ķ�
 */
package com.trs.document;

import com.trs.cms.auth.persistent.User;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyDateTime;
import com.trs.xwcm.test.Book;
import com.trs.xwcm.test.Books;

/**
 * @author caohui
 * 
 */
public interface IMyDocumentMgr {
    /**
     * ��ȡϵͳ�е�����
     * 
     * @param _extraFilter
     * @return
     * @throws WCMException
     */
    public Books query(WCMFilter _extraFilter) throws WCMException;


    public boolean isEnableRead(User _currUser, Book _currBook,
            CMyDateTime _currDateTime) throws WCMException;

}
