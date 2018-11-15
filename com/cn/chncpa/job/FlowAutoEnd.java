package com.cn.chncpa.job;

import com.trs.DreamFactory;
import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.User;
import com.trs.cms.auth.persistent.Users;
import com.trs.cms.process.FlowContentFactory;
import com.trs.cms.process.IFlowContent;
import com.trs.cms.process.IFlowServer;
import com.trs.cms.process.definition.Flow;
import com.trs.cms.process.definition.FlowNode;
import com.trs.cms.process.engine.FlowContext;
import com.trs.cms.process.engine.FlowDoc;
import com.trs.cms.process.engine.FlowDocs;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;
import com.trs.infra.util.job.BaseStatefulJob;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2016/11/9.
 */
public class FlowAutoEnd extends BaseStatefulJob {
    private static Logger s_logger = Logger.getLogger(FlowAutoAdopt.class);

    public FlowAutoEnd() {}
    protected void execute() throws WCMException {
        /****************前奏***************/
        String RWMS = CMyString.showNull(getArgAsString("RWMS"), "定时任务二");//任务描述
        String sSfqy = CMyString.showNull(getArgAsString("sfqy"),"0");//是否启用，0不启用 1启用
        if(!("1".equals(sSfqy))){
            s_logger.info(RWMS+" 未启用该策略！");
            return;
        }
        s_logger.info(RWMS+" 执行开始"+new CMyDateTime().now().toString());
        /****************前奏结束***************/
        ContextHelper.initContext(User.findByName("admin"));
        User currentUser = ContextHelper.getLoginUser();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Group group = null;
        Users mscs = null;
        User msc = null;
        FlowDoc flowDoc = null;
        MetaViewData metaViewData = null;
        IFlowContent oldFlowContent = null;
        Document currObj = null;
        IFlowServer flowServer = null;
        IFlowContent flowContent = null;
        FlowContext flowContext = null;
        try{
            //获取下一节点操作组(秘书处)
            group = Group.findById(9);
            //获取秘书处所有管理员
            mscs = group.getUsers(currentUser);
            //定义字符串集合
            StringBuilder arr = new StringBuilder();
            //获取所有秘书处管理员
            for(int i = 0;i < mscs.size();i++){
                msc = (User)mscs.getAt(i);
                if(msc != null){
                    arr.append(msc.getId() + ",");
                }
            }
            //转成数组
            String[] ids = arr.toString().split(",");
            //定义集合
            ArrayList list = new ArrayList();
            //定义filter
            String sWhere = "TOUSERID = ? and WORKED = ? and ISOBJDELETED = ? and (FLAG = ? or FLAG = ?)";
            WCMFilter _filter = new WCMFilter("",sWhere,"");
            //查询所有秘书处管理员的待办
            for (int j = 0; j < ids.length; j++) {
                _filter.addSearchValues(0,ids[j]);						//操作人ID
                _filter.addSearchValues(1,0);							//是否操作过
                _filter.addSearchValues(2,0);							//是否被删除
                _filter.addSearchValues(3,0);							//文档标记0待审核
                _filter.addSearchValues(4,1);							//文档标记1拒绝后再次提交待审核
                list.add(FlowDocs.openWCMObjs(currentUser,_filter));	//添加到集合中
                break;
            }
            //获取当前时间
            long thistime = new Date().getTime();
            //遍历所有待办
            for (int k = 0; k < list.size(); k++) {
                FlowDocs flowDocs = (FlowDocs)list.get(k);
                for (int l = 0; l < flowDocs.size(); l++) {
                    //待办流文档
                    flowDoc = (FlowDoc)flowDocs.getAt(l);
                    //获取文档对象ID
                    int objid = flowDoc.getContentId();
                    //获取文档对象
                    metaViewData = MetaViewData.findById(objid);
                    //获取文稿截止日日期
                    String jzrrq = metaViewData.getPropertyAsString("GJDLJGRQ");
                    if(jzrrq == null || "".equals(jzrrq)){
                        jzrrq = metaViewData.getPropertyAsString("JZRQ");
                    }
                    //转为时间戳
                    long thattime = format.parse(jzrrq).getTime();
                    //提前三天
                    thattime = thattime - 259200000;
                    //如果达到当前时间或者大于当前时间
                    if(thistime == thattime || thistime > thattime){
                        //设置状态为处理过
                        flowDoc.setWorked(true);
                        //更新旧文档状态
                        flowDoc.update();
                        //获取流转文档
                        oldFlowContent = flowDoc.getFlowContent();
                        //转换成文档对象
                        currObj = (Document)oldFlowContent.getSubinstance();
                        //声明IFlowServer
                        flowServer = (IFlowServer) DreamFactory.createObjectById("IFlowServer");
                        //创建新的文档工作流
                        flowContent = FlowContentFactory.makeFlowContent(currObj);
                        //判断工作流是否存在
                        if (flowContent.getOwnerFlow() == null) {
                            return;
                        }
                        //声明工作流域对象(1)当前操作者(2)工作流内容(3)操作描述(4)指定操作人集合(5)工作流对象
                        flowContext = new FlowContext(flowDoc.getToUser(), flowContent,"系统自动审核通过",mscs, Flow.findById(6));
                        //设置当前节点和下一节点
                        flowContext.setCurrNode(FlowNode.findById(24));
                        flowContext.setNextNode(FlowNode.findById(22));
                        //提交
                        flowServer.submitTo(flowContext);
                    }
                }
            }
        }catch (Exception e){

        }
    }
}
