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
        // 1 ������Ч��У��
        if (_employer == null)
            throw new WCMException("û��ָ��Ա����");
        CMyDateTime dtCheck = CMyDateTime.now();
        if (_dtCheckTime != null && !_dtCheckTime.isNull()) {
            dtCheck = _dtCheckTime;
        }

        // 2 ��ȡ�����Ƿ��д򿨼�¼����ģʽ���ϰ໹���°�
        // 2.1 ���쵱�쿪ʼʱ��
        CMyDateTime dtStart = new CMyDateTime();
        try {
            dtStart.setDateTimeWithString(dtCheck
                    .toString("yyyy-MM-dd 00:00:00"));
        } catch (Exception e) {
            throw new WCMException("���ڸ�ʽ���������⣡", e);
        }
        // 2.2 ��ѯ�����Ƿ��м�¼
        WCMFilter filter = new WCMFilter("",
                "CheckInTime>=? and EmployerName=?", "");
        filter.addSearchValues(dtStart);
        filter.addSearchValues(_employer.getEName());
        CheckInfos checkInfos = CheckInfos.openWCMObjs(null, filter);

        // 3 ������ϰ࣬����һ��CheckInfo��¼
        if (checkInfos.isEmpty()) {
            CheckInfo checkInfo = CheckInfo.createNewInstance();
            checkInfo.setEmployerName(_employer.getEName());
            checkInfo.setCheckInTime(dtCheck);
            checkInfo.save(ContextHelper.getLoginUser());
        }
        // 4 ������°࣬�޸Ĳ�ѯ����������¼��������ʱ������ˢ��ʱ�������
        else {

            User loginUser = ContextHelper.getLoginUser();
            CheckInfo checkInfo = (CheckInfo) checkInfos.getAt(0);
            checkInfo.validCanEditAndLock(loginUser);

            // ���㹤��ʱ��
            int nWorkTime = 0;
            try {
                nWorkTime = (int) dtCheck.dateDiff(CMyDateTime.HOUR,
                        checkInfo.getCheckInTime());
            } catch (Exception e) {
                throw new WCMException("���㹤��ʱ�����", e);
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
        // 1 ����У��
        

        // 2 �������������ƵĲ�ѯ����
        // 2.1 ����3����֮ǰ��ʼ��ʱ��
        CMyDateTime dtStart = new CMyDateTime();
        try {
            dtStart.setDateTimeWithString(CMyDateTime.now().toString(
                    "yyyy-MM-dd 00:00:00"));
            dtStart = dtStart.dateAdd(CMyDateTime.DAY_OF_MONTH, -2);
        } catch (Exception e) {
            throw new WCMException("���ڸ�ʽ���������⣡", e);
        }

        // 2.2 ����Filter
        WCMFilter filter = new WCMFilter("", "CheckInTime>=?",
                CheckInfo.DB_ID_NAME + " desc");
        filter.addSearchValues(dtStart);

        // 2.2 �ϲ�����Ĳ�ѯ����
        filter.mergeWith(_oExtraFilter);

        // 3 ���ؽ��
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
