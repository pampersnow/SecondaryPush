package com.trs.example.check;

import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;

public interface IEmployerMgr {
    /**
     * 新增或修改一个员工细信息
     * @param _employer
     * 需要新增或修改的员工信息
     *  @return
     * @throws WCMException
     * 如果员工对象无效抛出参数异常；
     * 新增时，如果一些必填信息没有设定，抛出参数异常
     * 
     */
    public Employer save(Employer _employer)throws WCMException;
    
    /**
     * 删除指定的员工
     * @param _employer
     * 被删除的员工
     * @return
     * 删除成功返回true，否则返回false
     * @throws WCMException
     */
    public boolean delete(Employer _employer)throws WCMException; 
    
    /**
     * 获取指定条件的员工
     * @param _oExtraFilter
     * 指定的查询条件，不指定，返回系统所有的员工
     * @return
     * 指定条件的员工集合
     * @throws WCMException
     */
    public Employers query(WCMFilter _oExtraFilter)throws WCMException;
}
