/**
 * 
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
public class MyDocumentMgr implements IMyDocumentMgr {

    /**
     * 
     */
    public MyDocumentMgr() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.trs.document.IMyDocumentMgr#query(com.trs.infra.persistent.WCMFilter)
     */
    public Books query(WCMFilter _o_extraFilter) throws WCMException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.trs.document.IMyDocumentMgr#isEnableRead(com.trs.cms.auth.persistent.User, com.trs.xwcm.test.Book, com.trs.infra.util.CMyDateTime)
     */
    public boolean isEnableRead(User _o_currUser, Book _o_currBook,
            CMyDateTime _o_currDateTime) throws WCMException {
        // TODO Auto-generated method stub
        return false;
    }

}
