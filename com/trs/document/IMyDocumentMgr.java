/**
 * 资料相关业务逻辑：
 * 获取系统所有资料(含查询)
 * 判断当前用户是否可阅读
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
     * 获取系统中的资料
     * 
     * @param _extraFilter
     * @return
     * @throws WCMException
     */
    public Books query(WCMFilter _extraFilter) throws WCMException;


    public boolean isEnableRead(User _currUser, Book _currBook,
            CMyDateTime _currDateTime) throws WCMException;

}
