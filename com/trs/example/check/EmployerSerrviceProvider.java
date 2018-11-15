package com.trs.example.check;

import com.trs.DreamFactory;
import com.trs.ajaxservice.WCMAJAXServiceHelper;
import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.infra.common.WCMException;
import com.trs.webframework.context.MethodContext;
import com.trs.webframework.provider.IGlueServiceProvider;

public class EmployerSerrviceProvider implements IGlueServiceProvider {

    public void delete(MethodContext _oArg0) throws Throwable {
    }

    public Object query(MethodContext _oArg0) throws Throwable {
        return null;
    }

    public int save(MethodContext _oMethodContext) throws Throwable {
        // 1 Ȩ���жϣ��ݶ�ֻ�й���Ա��������
        User loginUser = ContextHelper.getLoginUser();
        if (!loginUser.isAdministrator()) {
            throw new WCMException("�����ǹ���Ա����������Ա����");
        }

        // 2 �������ֵ���õ�������
        // 2.1 ���ݴ����ObjectId����Ԫ��ʵ��
        Employer employer = null;
        int nObjectId = _oMethodContext.getObjectId();
        if (nObjectId == 0) {// ����
            employer = Employer.createNewInstance();
        } else {// �޸�
            employer = Employer.findById(nObjectId);
            if (employer == null) {
                throw new WCMException("ָ����Ա�������ڣ�[ID=" + nObjectId + "]");
            }
        }
        // 2.2 ��������������õ�ʵ����
        final String[] pLogiicFields = { "ObjectId" };
        employer = (Employer) WCMAJAXServiceHelper.setWCMObjectProperties(
                loginUser, _oMethodContext, employer, pLogiicFields);

        // 3 ���棬����
        IEmployerMgr oEmployerMgr = (IEmployerMgr) DreamFactory
                .createObjectById("IEmployerMgr");
        employer = oEmployerMgr.save(employer);
        return employer.getId();
    }

    public Object findById(MethodContext _oArg0) throws Throwable {
        return null;
    }

    public Object findByIds(MethodContext _oArg0) throws Throwable {
        return null;
    }

}
