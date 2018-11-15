/**
 * 
 */
package com.trs.example.check;

import com.trs.DreamFactory;
import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyDateTime;
import com.trs.webframework.context.MethodContext;
import com.trs.webframework.provider.IQueryServiceProvider;
import com.trs.webframework.provider.ISelfDefinedServiceProvider;

/**
 * @author caohui
 * 
 */
public class CheckServiceProvider implements ISelfDefinedServiceProvider,
        IQueryServiceProvider {
    /**
     * �򿨷���
     * 
     * @param _oMethodContext
     * @throws Exception
     */
    public void check(MethodContext _oMethodContext) throws Exception {
        // 1 ����У��
        int nEmployerId = _oMethodContext.getValue("EmployerId", 0);
        Employer employer = Employer.findById(nEmployerId);
        if (employer == null) {
            throw new WCMException("ָ����Ա�������ڣ�[ID=" + nEmployerId + "]");
        }

        // ��ʱ��Ĭ�ϵ�ǰ
        CMyDateTime dtCheck = CMyDateTime.now();

        // 2 Ȩ���жϣ��ǹ���Աֻ�ܴ��Լ��Ŀ������������û�д���������Ա�����ֹ��򿨣�
        User loginUser = ContextHelper.getLoginUser();
        if (!loginUser.getTrueName().equalsIgnoreCase(employer.getEName())) {
            if (!loginUser.isAdministrator()) {
                throw new WCMException("�����ǹ���Ա�������ֹ������˴򿨣�");
            }
            // ��ͨЭ����Ҫʱ�䣬��ʱ����Դ���
            dtCheck = _oMethodContext.getValue("CheckTime", dtCheck);
        }

        // 3 ���д򿨲���
        ICheckMgr oCheckMgr = (ICheckMgr) DreamFactory
                .createObjectById("ICheckMgr");
        oCheckMgr.check(employer, dtCheck);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trs.webframework.provider.IQueryServiceProvider#query(com.trs.
     * webframework.context.MethodContext)
     */
    public Object query(MethodContext _oMethodContext) throws Throwable {
        // 1 Ȩ���жϣ�ֻ�й���Ա���Կ����򿨼�¼��
        User loginUser = ContextHelper.getLoginUser();
        if (!loginUser.isAdministrator()) {
            throw new WCMException("�����ǹ���Ա�����ܲ鿴�򿨼�¼��");
        }

        // 2 ���ݴ���Ĳ�������Ϸ���������ļ����������Ĳ�ѯ����
        WCMFilter extraFilter = _oMethodContext.getExtraWCMFilter();

        // 3 ��ѯ����
        ICheckMgr oCheckMgr = (ICheckMgr) DreamFactory
                .createObjectById("ICheckMgr");
        return oCheckMgr.queryAll(extraFilter);
    }
}
