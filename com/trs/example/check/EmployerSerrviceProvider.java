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
        // 1 权限判断，暂定只有管理员可以新增
        User loginUser = ContextHelper.getLoginUser();
        if (!loginUser.isAdministrator()) {
            throw new WCMException("您不是管理员，不能新增员工！");
        }

        // 2 将传入的值设置到对象上
        // 2.1 根据传入的ObjectId构造元素实例
        Employer employer = null;
        int nObjectId = _oMethodContext.getObjectId();
        if (nObjectId == 0) {// 新增
            employer = Employer.createNewInstance();
        } else {// 修改
            employer = Employer.findById(nObjectId);
            if (employer == null) {
                throw new WCMException("指定的员工不存在！[ID=" + nObjectId + "]");
            }
        }
        // 2.2 将传入的数据设置到实例中
        final String[] pLogiicFields = { "ObjectId" };
        employer = (Employer) WCMAJAXServiceHelper.setWCMObjectProperties(
                loginUser, _oMethodContext, employer, pLogiicFields);

        // 3 保存，返回
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
