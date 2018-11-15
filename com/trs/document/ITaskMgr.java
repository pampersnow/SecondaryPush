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
     * ��ȡָ���û��ɿ�����������
     * @param _oCurrUser
     * ָ���û�
     * @param _oExtraFilter
     * ����ļ�������
     * @return
     * @throws WCMException
     */
    public TaskInfos query(User _oCurrUser, WCMFilter _oExtraFilter)
            throws WCMException;
}
