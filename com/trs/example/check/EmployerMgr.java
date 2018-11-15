/**
 * 
 */
package com.trs.example.check;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;

/**
 * @author caohui
 * 
 */
public class EmployerMgr implements IEmployerMgr {

    /**
     * 
     */
    public EmployerMgr() {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.example.check.IEmployerMgr#save(com.trs.example.check.Employer)
     */
    public Employer save(Employer _employer) throws WCMException {
        User loginUser = ContextHelper.getLoginUser();
        
        // 修改之前需要先锁定对象
        if (!_employer.isAddMode()) {
            _employer.validCanEditAndLock(loginUser);
        }
        _employer.save(loginUser);

        return _employer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.example.check.IEmployerMgr#delete(com.trs.example.check.Employer)
     */
    public boolean delete(Employer _employer) throws WCMException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.example.check.IEmployerMgr#query(com.trs.infra.persistent.WCMFilter
     * )
     */
    public Employers query(WCMFilter _oExtraFilter) throws WCMException {
        // TODO Auto-generated method stub
        return null;
    }

}
