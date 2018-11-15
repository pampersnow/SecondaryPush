package com.trs.example.check;

import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyDateTime;

/**
 * @author caohui
 * 
 */
public interface ICheckMgr {
    /**
     * 指定员工打卡，上班和下班都理解成打卡 *
     * 
     * @param _employer
     *            指定员工(业务层约定传入持久化对象)
     * @param _dtCheckTime
     *            打卡的时间，如果指定为Null，使用当前系统时间
     * @throws WCMException
     *             如果指定的员工对象无效，抛出参数异常；
     */
    public void check(Employer _employer, CMyDateTime _dtCheckTime)
            throws WCMException;

    /**
     * 获取指定员工指定条件的打卡记录，考虑到性能，有三个月限制
     * 
     * @param _oExtraFilter
     *            额外的过滤条件（业务层中的查询方法一般会定义一个WCMFilter参数）
     * @return 返回指定条件的打卡记录集合；
     * @throws WCMException
     */
    public CheckInfos queryAll( WCMFilter _oExtraFilter)
            throws WCMException;

    /**
     * 统计指定月份中迟到的人员
     * 
     * @param _nYear
     *            统计的年
     * @param _nMonth
     *            统计的月
     * @param _oExtraFilter
     *            额外的检索条件
     * @return 指定时间迟到的员工集合
     * @throws WCMException
     *             如果年份小于1900年或者大于当前，抛出参数异常； 如果月份小于1后者大于12，抛出参数异常；
     */
    public Employers queryEmployersLate(int _nYear, int _nMonth,
            WCMFilter _oExtraFilter) throws WCMException;
}
