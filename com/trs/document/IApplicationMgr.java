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
public interface IApplicationMgr {
    public TaskInfo apply(User _currUser, Book _currBook,
            CMyDateTime _dtStartTime, CMyDateTime _dtEndTime)
            throws WCMException;

    public TaskInfos queryApplying(WCMFilter _extraFilter) throws WCMException;
   
}
