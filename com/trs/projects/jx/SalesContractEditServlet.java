package com.trs.projects.jx;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.cms.process.SubmitFlow;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;
import com.trs.web2frame.WCMServiceCaller;
import com.trs.web2frame.dispatch.Dispatch;
import com.trs.web2frame.util.JsonHelper;

/**
 * @description:销售合同信息保存
 * @author kong.dejing@trs.com.cn
 * @date 2017-6-26 下午5:02:31
 */
public class SalesContractEditServlet extends HttpServlet{
	private static Logger s_logger = Logger.getLogger(com.trs.projects.jx.SalesContractEditServlet.class);
	public void service(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			
			String username = request.getParameter("username");
			ContextHelper.initContext(User.findByName(username));
			String chnlid = ConfigServer.getServer().getSysConfigValue("PRO_JX_SALES", "0");
			String xmjb = new String(request.getParameter("xmjb").getBytes("ISO-8859-1"),"UTF-8");//项目级别
			String htmc = new String(request.getParameter("htmc").getBytes("ISO-8859-1"),"UTF-8");//合同名称
			String htbh = new String(request.getParameter("htbh").getBytes("ISO-8859-1"),"UTF-8");//合同编号
			String xstd = new String(request.getParameter("xstd").getBytes("ISO-8859-1"),"UTF-8");//销售团队
			String khjl = new String(request.getParameter("khjl").getBytes("ISO-8859-1"),"UTF-8");//客户经理
			String jf = new String(request.getParameter("jf").getBytes("ISO-8859-1"),"UTF-8");//甲方
			String khlx = new String(request.getParameter("khlx").getBytes("ISO-8859-1"),"UTF-8");//客户类型
			String hy = new String(request.getParameter("hy").getBytes("ISO-8859-1"),"UTF-8");//行业
			String qy = new String(request.getParameter("qy").getBytes("ISO-8859-1"),"UTF-8");//区域
			String qysj = new String(request.getParameter("qysj").getBytes("ISO-8859-1"),"UTF-8");//签约时间
			String qyjd = new String(request.getParameter("qyjd").getBytes("ISO-8859-1"),"UTF-8");//签约季度
			String wbxx = new String(request.getParameter("wbxx").getBytes("ISO-8859-1"),"UTF-8");//外包信息
			String fkcs = new String(request.getParameter("fkcs").getBytes("ISO-8859-1"),"UTF-8");//付款次数
			String jfcs = new String(request.getParameter("jfcs").getBytes("ISO-8859-1"),"UTF-8");//甲方处室
			String htje = new String(request.getParameter("htje").getBytes("ISO-8859-1"),"UTF-8");//合同金额
			String wbje = new String(request.getParameter("wbje").getBytes("ISO-8859-1"),"UTF-8");//外包合同金额
			String swlxr = new String(request.getParameter("swlxr").getBytes("ISO-8859-1"),"UTF-8");//商务联系人
			String swlxfs = new String(request.getParameter("swlxfs").getBytes("ISO-8859-1"),"UTF-8");//商务联系方式
			String sjlxr = new String(request.getParameter("sjlxr").getBytes("ISO-8859-1"),"UTF-8");//审计联系人
			String sjlxfs = new String(request.getParameter("sjlxfs").getBytes("ISO-8859-1"),"UTF-8");//审计联系方式
			String sqgzl = new String(request.getParameter("sqgzl").getBytes("ISO-8859-1"),"UTF-8");//售前工作量
			String jhssgzl = new String(request.getParameter("jhssgzl").getBytes("ISO-8859-1"),"UTF-8");//计划实施工作量

			String strEvidence = CMyString.showNull((String) request.getSession(true).getAttribute("saleenclosure"),"");//附件
			request.getSession(true).removeAttribute("saleenclosure");
			
			Map map = new HashMap();
			Dispatch oDispatch;
			CMyDateTime date = CMyDateTime.now();
			map.put("ObjectId", "0");
			map.put("CHANNELID", chnlid);
			map.put("prolevel", xmjb);
			map.put("name", htmc);
			map.put("CRUSER",username);
			map.put("code", htbh);
			map.put("steam", xstd);
			map.put("sales", khjl);
			map.put("fparty", jf);
			map.put("ctype", khlx);
			map.put("industry", hy);
			map.put("area", qy);
			map.put("signtime", qysj);
			map.put("signquarter", qyjd);
			map.put("svalue", htje);
			map.put("outvalue", wbje);
			map.put("outinfo", wbxx);
			map.put("paytimes", fkcs);
			map.put("business", swlxr);
			map.put("businessphone", swlxfs);
			map.put("caudit", sjlxr);
			map.put("cauditphone", sjlxfs);
			map.put("fpost", jfcs);
			map.put("presales", sqgzl);
			map.put("planworked", jhssgzl);
			if(strEvidence != null && strEvidence != ""){
				oDispatch = WCMServiceCaller.UploadFile(strEvidence);
				map.put("enclosure", oDispatch.getUploadShowName());
			}
			oDispatch = WCMServiceCaller.Call("wcm6_MetaDataCenter", "savemetaviewdata", map,
			        true);
			Map json = oDispatch.getJson();
        	String DocId = JsonHelper.getValueAsString(json, "METAVIEWDATA.METADATAID");
        	Document document = Document.findById(Integer.parseInt(DocId));
        	SubmitFlow sf = new SubmitFlow();
        	sf.submitNewFlow(document,User.findByName(username),"销售提交合同","PRO_JX_JFLC"); 
        	out.print("<script language='JavaScript'>alert('提交成功！');window.location.href='./jx/salescontract_edit.jsp';</script>");
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				s_logger.info("保存销售合同信息报错："+e.getMessage()+"----"+CMyDateTime.now());	
			}finally{
				ContextHelper.clear();
			}
	}
}
