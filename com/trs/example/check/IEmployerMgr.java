package com.trs.example.check;

import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;

public interface IEmployerMgr {
    /**
     * �������޸�һ��Ա��ϸ��Ϣ
     * @param _employer
     * ��Ҫ�������޸ĵ�Ա����Ϣ
     *  @return
     * @throws WCMException
     * ���Ա��������Ч�׳������쳣��
     * ����ʱ�����һЩ������Ϣû���趨���׳������쳣
     * 
     */
    public Employer save(Employer _employer)throws WCMException;
    
    /**
     * ɾ��ָ����Ա��
     * @param _employer
     * ��ɾ����Ա��
     * @return
     * ɾ���ɹ�����true�����򷵻�false
     * @throws WCMException
     */
    public boolean delete(Employer _employer)throws WCMException; 
    
    /**
     * ��ȡָ��������Ա��
     * @param _oExtraFilter
     * ָ���Ĳ�ѯ��������ָ��������ϵͳ���е�Ա��
     * @return
     * ָ��������Ա������
     * @throws WCMException
     */
    public Employers query(WCMFilter _oExtraFilter)throws WCMException;
}
