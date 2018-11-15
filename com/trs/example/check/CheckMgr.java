/**
 * 
 */
package com.trs.example.check;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyDateTime;

/**
 * @author caohui
 * 
 */
public class CheckMgr implements ICheckMgr {

    /**
     * 
     */
    public CheckMgr() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.example.check.ICheckMgr#check(com.trs.example.check.Employer,
     * com.trs.infra.util.CMyDateTime)
     */
    public void check(Employer _employer, CMyDateTime _dtCheckTime)
            throws WCMException {
        // 1 参数有效性校验
        if (_employer == null)
            throw new WCMException("没有指定员工！");
        CMyDateTime dtCheck = CMyDateTime.now();
        if (_dtCheckTime != null && !_dtCheckTime.isNull()) {
            dtCheck = _dtCheckTime;
        }

        // 2 获取当天是否有打卡记录，打卡模式是上班还是下班
        // 2.1 构造当天开始时间
        CMyDateTime dtStart = new CMyDateTime();
        try {
            dtStart.setDateTimeWithString(dtCheck
                    .toString("yyyy-MM-dd 00:00:00"));
        } catch (Exception e) {
            throw new WCMException("日期格式化出现问题！", e);
        }
        // 2.2 查询当天是否有记录
        WCMFilter filter = new WCMFilter("",
                "CheckInTime>=? and EmployerName=?", "");
        filter.addSearchValues(dtStart);
        filter.addSearchValues(_employer.getEName());
        CheckInfos checkInfos = CheckInfos.openWCMObjs(null, filter);

        // 3 如果是上班，产生一个CheckInfo记录
        if (checkInfos.isEmpty()) {
            CheckInfo checkInfo = CheckInfo.createNewInstance();
            checkInfo.setEmployerName(_employer.getEName());
            checkInfo.setCheckInTime(dtCheck);
            checkInfo.save(ContextHelper.getLoginUser());
        }
        // 4 如果是下班，修改查询到的那条记录，将工作时间和最后刷卡时间更新上
        else {

            User loginUser = ContextHelper.getLoginUser();
            CheckInfo checkInfo = (CheckInfo) checkInfos.getAt(0);
            checkInfo.validCanEditAndLock(loginUser);

            // 计算工作时间
            int nWorkTime = 0;
            try {
                nWorkTime = (int) dtCheck.dateDiff(CMyDateTime.HOUR,
                        checkInfo.getCheckInTime());
            } catch (Exception e) {
                throw new WCMException("计算工作时间错误", e);
            }

            checkInfo.setCheckOutTime(dtCheck);
            checkInfo.setWorkTime(nWorkTime);
            checkInfo.save(loginUser);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.trs.example.check.ICheckMgr#queryAll(
     * com.trs.infra.persistent.WCMFilter)
     */
    public CheckInfos queryAll(WCMFilter _oExtraFilter)
            throws WCMException {
        // 1 参数校验
        

        // 2 构造三个月限制的查询条件
        // 2.1 计算3个月之前开始的时间
        CMyDateTime dtStart = new CMyDateTime();
        try {
            dtStart.setDateTimeWithString(CMyDateTime.now().toString(
                    "yyyy-MM-dd 00:00:00"));
            dtStart = dtStart.dateAdd(CMyDateTime.DAY_OF_MONTH, -2);
        } catch (Exception e) {
            throw new WCMException("日期格式化出现问题！", e);
        }

        // 2.2 构造Filter
        WCMFilter filter = new WCMFilter("", "CheckInTime>=?",
                CheckInfo.DB_ID_NAME + " desc");
        filter.addSearchValues(dtStart);

        // 2.2 合并传入的查询条件
        filter.mergeWith(_oExtraFilter);

        // 3 返回结果
        return CheckInfos.openWCMObjs(null, filter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.example.check.ICheckMgr#queryEmployersLate(int, int,
     * com.trs.infra.persistent.WCMFilter)
     */
    public Employers queryEmployersLate(int _nYear, int _nMonth,
            WCMFilter _oExtraFilter) throws WCMException {
        // TODO Auto-generated method stub
        return null;
    }

}
