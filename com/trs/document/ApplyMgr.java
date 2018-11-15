/**
 * 
 */
package com.trs.document;

import com.trs.cms.auth.persistent.User;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyDateTime;
import com.trs.task.TaskInfo;
import com.trs.task.TaskInfos;
import com.trs.xwcm.test.Book;

/**
 * @author caohui
 * 
 */
public class ApplyMgr implements IApplicationMgr {

    /**
     * 
     */
    public ApplyMgr() {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.document.IApplicationMgr#apply(com.trs.cms.auth.persistent.User,
     * com.trs.xwcm.test.Book, com.trs.infra.util.CMyDateTime,
     * com.trs.infra.util.CMyDateTime)
     */
    public TaskInfo apply(User _o_currUser, Book _o_currBook,
            CMyDateTime _o_dtStartTime, CMyDateTime _o_dtEndTime)
            throws WCMException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.document.IApplicationMgr#queryApplying(com.trs.infra.persistent
     * .WCMFilter)
     */
    public TaskInfos queryApplying(WCMFilter _extraFilter) throws WCMException {
        // 1 构造查询正在申请的借阅请求 Filter(Status=)
        // 1.1 ddsa
        // 1.2 fdsfdsfs

        // 2 合并外界传入的查询条件

        // 3 查询并返回

        return null;
    }

}
