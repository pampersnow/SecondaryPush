package com.trs.cms.process;

import java.util.HashMap;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.User;
import com.trs.cms.process.IFlowContent;
import com.trs.cms.process.definition.FlowAction;
import com.trs.cms.process.engine.ExecuteContext;
import com.trs.cms.process.engine.IActionHandler;
import com.trs.components.metadata.center.MetaViewData;
import com.trs.components.metadata.center.MetaViewDatas;
import com.trs.components.wcm.content.persistent.Appendixes;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.webframework.controler.JSPRequestProcessor;
import com.trs.infra.persistent.db.DBManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RemoveAttachments implements IActionHandler {


	public RemoveAttachments() {
		
	}

	public boolean execute(ExecuteContext _context, FlowAction _action)
			throws WCMException {
		//声明文档
		Document document = null;
		//获取工作流中文档信息
		IFlowContent flowContent = _context.getFlowContent();
		//实例化文档
		if(flowContent.getSubinstance() instanceof Document){
			//判断工作流中是否为文档
			document = (Document)flowContent.getSubinstance();
		}
		//获取下一节点名称
		String nodeName = _context.getFlowContext().getBranch().getNextNodeName();
		//判断操作
		if("退回发布人".equals(nodeName)){
			//获取该文档的所有附件
			Appendixes appendixes = Appendixes.findAppendixesByObj(document);
			//删除所有附件
			appendixes.removeAll();
		}else if("结束".equals(nodeName)){
			this.create_statistics(document);
		}
		return true;
	}
	
	public void create_statistics(Document document)throws WCMException{
		ContextHelper.initContext(User.findByName("admin"));
		User currentUser = ContextHelper.getLoginUser();
        //文稿信息
		MetaViewData mvd = MetaViewData.findById(document.getId());
        String hyid = mvd.getPropertyAsString("HYMC");
        //文稿所属会议
        MetaViewData meeting = MetaViewData.findById(Integer.parseInt(hyid));
		HashMap map = new HashMap();
		map.put("ObjectId", new Integer(0));
		map.put("viewid", new Integer(28));
		map.put("CHANNELID", new Integer(90));
		map.put("CRUSER", currentUser.getName());

        //单位名称
        String ggdws = mvd.getPropertyAsString("GGDW");
        String dydw = ggdws.split(";")[0];
        map.put("UNIT_NAME",dydw);

        //会议开始时间
        String hykssj = meeting.getPropertyAsString("START_TIME");
        hykssj = hykssj.substring(0,7) + "-02";
        map.put("MEETING_START",hykssj);

        //对口组 & 子组/报告人组会
        String hylx = mvd.getPropertyAsString("HYLX");
        switch(Integer.parseInt(hylx)){
            case 1 :    //全会
                map.put("GROUP_TYPE",mvd.getPropertyAsString("GROUP_NAME"));
                map.put("GROUP_CHILDREN","");
                map.put("GROUP_MEETING",mvd.getPropertyAsString("GROUP_NAME"));
                break;
            case 2 :    //报告人组会
                map.put("GROUP_TYPE",mvd.getPropertyAsString("GROUP_NAME"));
                map.put("GROUP_CHILDREN",mvd.getPropertyAsString("GROUP_MEETING"));
                map.put("GROUP_MEETING",mvd.getPropertyAsString("GROUP_MEETING") + "/" + mvd.getPropertyAsString("GROUP_NAME"));
                break;
            case 3 :    //焦点组
                map.put("GROUP_TYPE",mvd.getPropertyAsString("GROUP_MEETING"));
                map.put("GROUP_CHILDREN","");
                map.put("GROUP_MEETING",mvd.getPropertyAsString("GROUP_MEETING"));
                break;
        }

        //文稿审查程序
        String wgsccx = mvd.getPropertyAsString("WGSCCX");
        if(wgsccx == null || "".equals(wgsccx)){
            map.put("WGSCCX","");
        }else{
            if("1".equals(wgsccx)){
                map.put("WGSCCX","审查");
            }else{
                map.put("WGSCCX","备案");
            }
        }

        //文稿级别
        String wgjb = mvd.getPropertyAsString("WGJB");
        if(wgjb == null || "".equals(wgjb)){
            map.put("WORD_LEVEL","");
        }else{
            map.put("WORD_LEVEL",wgjb);
        }

        //提交名义
        String tjmy = mvd.getPropertyAsString("TJMY");
        if("中国".equals(tjmy)){
            map.put("SUBMIT_NAME","主管部门");
        }else{
            map.put("SUBMIT_NAME",tjmy);
        }

        //组团方式
        if(tjmy.indexOf("成员") != -1){
            map.put("GROUP_MODE","小M");
        }else if(tjmy.indexOf("中国") != -1){
            map.put("GROUP_MODE","大M");
        }

        /*提交信息*/
        JSPRequestProcessor processor = new JSPRequestProcessor();
        String sServiceId = "wcm6_MetaDataCenter";
        String sMethodName = "savemetaviewdata";
		processor.excute(sServiceId, sMethodName, map);
	}
}
