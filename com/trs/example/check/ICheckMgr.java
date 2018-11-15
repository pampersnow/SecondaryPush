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
     * ָ��Ա���򿨣��ϰ���°඼���ɴ� *
     * 
     * @param _employer
     *            ָ��Ա��(ҵ���Լ������־û�����)
     * @param _dtCheckTime
     *            �򿨵�ʱ�䣬���ָ��ΪNull��ʹ�õ�ǰϵͳʱ��
     * @throws WCMException
     *             ���ָ����Ա��������Ч���׳������쳣��
     */
    public void check(Employer _employer, CMyDateTime _dtCheckTime)
            throws WCMException;

    /**
     * ��ȡָ��Ա��ָ�������Ĵ򿨼�¼�����ǵ����ܣ�������������
     * 
     * @param _oExtraFilter
     *            ����Ĺ���������ҵ����еĲ�ѯ����һ��ᶨ��һ��WCMFilter������
     * @return ����ָ�������Ĵ򿨼�¼���ϣ�
     * @throws WCMException
     */
    public CheckInfos queryAll( WCMFilter _oExtraFilter)
            throws WCMException;

    /**
     * ͳ��ָ���·��гٵ�����Ա
     * 
     * @param _nYear
     *            ͳ�Ƶ���
     * @param _nMonth
     *            ͳ�Ƶ���
     * @param _oExtraFilter
     *            ����ļ�������
     * @return ָ��ʱ��ٵ���Ա������
     * @throws WCMException
     *             ������С��1900����ߴ��ڵ�ǰ���׳������쳣�� ����·�С��1���ߴ���12���׳������쳣��
     */
    public Employers queryEmployersLate(int _nYear, int _nMonth,
            WCMFilter _oExtraFilter) throws WCMException;
}
