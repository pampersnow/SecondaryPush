/**
 * 
 */
package com.trs.document;

import com.trs.cms.auth.persistent.User;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.task.TaskInfos;

/**
 * @author caohui
 * 
 */
public interface ITaskMgr {
    /**
     * 获取指定用户可看的所有任务
     * @param _oCurrUser
     * 指定用户
     * @param _oExtraFilter
     * 额外的检索条件
     * @return
     * @throws WCMException
     */
    public TaskInfos query(User _oCurrUser, WCMFilter _oExtraFilter)
            throws WCMException;
}
