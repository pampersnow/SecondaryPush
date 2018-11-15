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
     * 打卡服务
     * 
     * @param _oMethodContext
     * @throws Exception
     */
    public void check(MethodContext _oMethodContext) throws Exception {
        // 1 参数校验
        int nEmployerId = _oMethodContext.getValue("EmployerId", 0);
        Employer employer = Employer.findById(nEmployerId);
        if (employer == null) {
            throw new WCMException("指定的员工不存在！[ID=" + nEmployerId + "]");
        }

        // 打卡时间默认当前
        CMyDateTime dtCheck = CMyDateTime.now();

        // 2 权限判断（非管理员只能打自己的卡，如果其他人没有带卡，管理员可以手工打卡）
        User loginUser = ContextHelper.getLoginUser();
        if (!loginUser.getTrueName().equalsIgnoreCase(employer.getEName())) {
            if (!loginUser.isAdministrator()) {
                throw new WCMException("您不是管理员，不能手工替他人打卡！");
            }
            // 沟通协调需要时间，打卡时间可以传入
            dtCheck = _oMethodContext.getValue("CheckTime", dtCheck);
        }

        // 3 进行打卡操作
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
        // 1 权限判断（只有管理员可以看到打卡记录）
        User loginUser = ContextHelper.getLoginUser();
        if (!loginUser.isAdministrator()) {
            throw new WCMException("您不是管理员，不能查看打卡记录！");
        }

        // 2 根据传入的参数，结合服务的配置文件，构造额外的查询参数
        WCMFilter extraFilter = _oMethodContext.getExtraWCMFilter();

        // 3 查询返回
        ICheckMgr oCheckMgr = (ICheckMgr) DreamFactory
                .createObjectById("ICheckMgr");
        return oCheckMgr.queryAll(extraFilter);
    }
}
