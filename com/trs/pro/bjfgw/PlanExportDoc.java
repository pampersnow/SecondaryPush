package com.trs.pro.bjfgw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.support.config.ConfigServer;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;

public class PlanExportDoc extends BaseStatefulScheduleWorker{

	private static Logger s_logger = Logger.getLogger(com.trs.pro.bjfgw.PlanExportDoc.class);
	
	public void start(String dataid)throws WCMException{
		ContextHelper.initContext(User.findByName("admin"));
		
		String doctochnlids = "职责-机构职责$621111,职责-领导介绍$621141,职责-直属机构、事业单位$621171,职责-其他$623111,法律法规-法律$6218972,法律法规-法规$6218982,法律法规-规章$6218992,规范性文件$6218952,其他文件$6218962,中长期规划$6219223,近期规划$6219233,工作计划$6219243,财政预算$6219253,计划-其他$6219263,职责-行政许可$6219284,职责-行政审批$6219294,职责-其他$6219324,突发公共事件$6219355,政务动态$6219365,会议动态$6219375,工作动态$6219395,意见征集$6219405,培训动态$6219415,政府采购$6219445,工程招标$6219455,人事动态$6219465,廉政动态$6219475,统计调查$6219485,动态-其他$6219495,结果公示-工作总结$6219505,结果公示-行政许可$6219515,结果公示-行政审批$6219525,行政征收$6219535,行政处罚$6219304,其他行政执法执权$6219555,检查评选公示$6219565,意见反馈公示$6219585,结果公示-其他$6219595";//导出的栏目id
		int time = 43200;//监控n分钟之内发布的文档
		//String XmlFilePath = "/wcm/sjzfxxgk/up";//(旧)生成XML文件的存放路径F:\\xml
		String XmlFilePath = "/wcm3/wcm/sjzfxxgk/up";//(新)
		String InfoType = "2|6218952,6218962,6218972,6218982,6218992;3|6219223,6219233,6219243,6219253,6219263;4|6219284,6219294,6219324;5|6219355,6219365,6219375,6219395,6219405,6219415,6219445,6219455,6219465,6219475,6219485,6219495,6219505,6219515,6219525,6219535,6219304,6219555,6219565,6219585,6219595";//类别标识
		String siteid = "3";//站点ID
		String WCMDate = "/wcm/TRSWCMV65/WCMData/";
		
		HashMap hashMap = new HashMap();
	    String [] docChnls = doctochnlids.split(",");
	    for(int i= 0 ; i<docChnls.length ;i++) {
	    	String chnltochnl = docChnls[i];
	    	hashMap.put(chnltochnl.substring(0, chnltochnl.indexOf("$")), chnltochnl.substring(chnltochnl.indexOf("$")+1));
	    }
		String start_time = getTimeColumn(time);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date day = new Date();
		String now = df.format(day);
		String docid = getDocid(start_time,now,siteid);
		if(!"".equals(dataid)){
			docid = dataid;
		}
		if(!docid.equals("")) {
			String [] docids = docid.split(",");
			for( int i = 0;i< docids.length;i++) {
				int id = Integer.parseInt(docids[i]);
				Document document = Document.findById(id);
				String docrelwords = document.getRelateWords();
				if(docrelwords == null || docrelwords.equals("")) continue;
				Iterator it = hashMap.keySet().iterator();  
		        while(it.hasNext()) {  
		            String key = (String)it.next();
		            String chnlid = (String) hashMap.get(key);//市政府的栏目id
		            if(docrelwords.indexOf(key) > -1 ) {
		            	File file = new File(XmlFilePath);
		            	if(!file.exists()){
		            		file.mkdir();
		            	}
		            	String newfilepath = mkdirFile(XmlFilePath);//生成带有时间戳的子文件夹
		            	String appendixid = getAppendix(document,newfilepath,WCMDate);
		            	buildXMLDoc(newfilepath,chnlid,document,InfoType,appendixid);
		            	break;
		            }
		        }
			}
		}
		ContextHelper.clear();
	}
	
	protected void execute() throws WCMException {
		s_logger.error(":::::::::::::::::::::::::::定时导出XML文件开始,执行时间："+CMyDateTime.now());
		ContextHelper.initContext(User.findByName("admin"));
		
		String doctochnlids = getArgAsString("DocWordToChnl");//导出的栏目id
		int time = Integer.parseInt(getArgAsString("TIME"));//监控n分钟之内发布的文档
		String XmlFilePath = getArgAsString("XmlFilePath");//生成XML文件的存放路径F:\\xml
		String InfoType = getArgAsString("InfoType");//类别标识
		String siteid = getArgAsString("siteid");//站点ID
		String WCMDate = getArgAsString("WCMDate");
		HashMap hashMap = new HashMap();
	    String [] docChnls = doctochnlids.split(",");
	    for(int i= 0 ; i<docChnls.length ;i++) {
	    	String chnltochnl = docChnls[i];
	    	hashMap.put(chnltochnl.substring(0, chnltochnl.indexOf("$")), chnltochnl.substring(chnltochnl.indexOf("$")+1));
	    }
		String start_time = getTimeColumn(time);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date day = new Date();
		String now = df.format(day);
		String docid = getDocid(start_time,now,siteid);
		if(!docid.equals("")) {
			String [] docids = docid.split(",");
			for( int i = 0;i< docids.length;i++) {
				int id = Integer.parseInt(docids[i]);
				Document document = Document.findById(id);
				String docrelwords = document.getRelateWords();
				if(docrelwords == null || docrelwords.equals("")) continue;
				Iterator it = hashMap.keySet().iterator();  
		        while(it.hasNext()) { 
		            String key = (String)it.next();
		            String chnlid = (String) hashMap.get(key);//市政府的栏目id
		            if(docrelwords.indexOf(key) > -1 ) {
		            	System.out.println("市政府的栏目id:::"+chnlid);
		            	String newfilepath = mkdirFile(XmlFilePath);//生成带有时间戳的子文件夹
		            	//String appendixid = getAppendix(document,newfilepath,WCMDate);
		            	s_logger.debug(":::::::::::::::::::::::::::即将生成XML，子文件夹："+newfilepath);
		            	buildXMLDoc(newfilepath,chnlid,document,InfoType,"");
		            	//break;
		            }
		        }
			}
		}
		ContextHelper.clear();
		s_logger.error(":::::::::::::::::::::::::::定时导出XML文件结束,执行时间："+CMyDateTime.now());
	}
	
	/*
	 * 定时统计需要推送栏目的发布文档id
	 * @param start_time 统计开始时间
	 * @param nowtime 统计结束时间
	 * @return docid 返回发布文档id字符串
	 */
	private String getDocid(String start_time,String nowtime,String siteid) {
		String docid = "";
		String sql = "select docid from wcmchnldoc where docstatus = 10 and siteid ="+siteid+" and modal = 1 and  docpubtime between to_date('"+start_time+"', 'yyyy-MM-dd hh24:mi:ss') and to_date('"+nowtime+"', 'yyyy-MM-dd hh24:mi:ss')";
		//String sql = "select docid from wcmchnldoc where docstatus = 10 and siteid ="+siteid+" and modal = 1 and  docpubtime between '"+start_time+"' and '"+nowtime+"'";
		s_logger.debug("sql--定时统计需要推送栏目的发布文档id:"+sql);
		DBManager dbManager = null;
		Connection conn = null;
		Statement psmt = null;
		ResultSet rs = null;
		try {
			dbManager = DBManager.getDBManager();
			conn = dbManager.getConnection();
			psmt = conn.createStatement();
			rs = psmt.executeQuery(sql);
			if(rs != null){
				while (rs.next()) {
					docid += rs.getString("docid") + ",";
				}	
			}
		} catch (WCMException e) {
			s_logger.debug("定时统计需要推送栏目的发布文档id抛出异常："+e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO: handle exception
			s_logger.debug("定时统计需要推送栏目的发布文档id抛出异常："+e);
			e.printStackTrace();
		} finally {
			if (conn != null) {
				dbManager.freeConnection(conn);
			}
			try {
				if (rs != null) {
					rs.close();
				}
				if (psmt != null) {
					psmt.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				s_logger.debug("定时统计需要推送栏目的发布文档id，关闭结果集抛出异常："+e);
				e.printStackTrace();
			}
			psmt = null;
			conn = null;
			rs = null;
		}
		return docid;
	}
	
	/*
	 * 根据文档id获取附件id
	 * @param docid 文档id
	 * @return getAppendixid 返回附件Appendixid字符串
	 */
	private String getAppendixid(int docid) {
		String appendixid = "";
		String sql = "select appendixid from WCMAPPENDIX where appdocid = " + docid;
		s_logger.debug("sql--根据文档id获取附件id:"+sql);
		DBManager dbManager = null;
		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			dbManager = DBManager.getDBManager();
			conn = dbManager.getConnection();
			psmt = conn.prepareStatement(sql);
			rs = psmt.executeQuery();
			while (rs.next()) {
				appendixid += rs.getString("appendixid") + ",";
			}	
		} catch (WCMException e) {
			s_logger.debug("根据文档id获取附件id抛出异常："+e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO: handle exception
			s_logger.debug("根据文档id获取附件id抛出异常："+e);
			e.printStackTrace();
		} finally {
			if (conn != null) {
				dbManager.freeConnection(conn);
			}
			try {
				if (rs != null) {
					rs.close();
				}
				if (psmt != null) {
					psmt.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				s_logger.debug("根据文档id获取附件id，关闭结果集抛出异常："+e);
				e.printStackTrace();
			}
			psmt = null;
			conn = null;
			rs = null;
		}
		return appendixid;
	}
	/*
     * 获取几分钟前的时间
     * @param column 计划调度传参，参数为：统计几分钟前至今的数据
     */
	private String getTimeColumn (int column) {
		String timecolum = "" ;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - column) ; //把时间设置为当前时间前column分钟
		timecolum = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());//获取到完整的时间
        return timecolum;
    }
	
	/*
     * 指定文件夹下创建子文件夹
     * @param path 计划调度传的参数，本地生成xml文件（未加时间戳之前）的路径
     */
    public String mkdirFile(String path) {
    	String newPath = "";
	    	/*SimpleDateFormat format =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time=format.format(System.currentTimeMillis());
			Date date = format.parse(time);
    		String name = "param"+date.getTime();*/
    		String name = "param"+new Date().getTime();
			double random = Math.random()*1000000 ;
			String code = String.valueOf(random).substring(0,4);
			newPath = path+"/"+name+code;//指定新路径
			File file = new File(newPath);//定义一个文件流
			file.mkdir();//创建文件夹
			
		 
	     return newPath;
	     } 
    /*
     * 导出XML文件
     * @param XmlFilePath 生成xml文件的地址
     * @param chnlid 对应市政府公开的栏目id
     * @param document wcm的document对象
     * @param InfoType 类别标识，由计划调度参数传递过来： 栏目一,栏目二|1$栏目三,栏目四|2
     * @param appendixname 附件名称，多个，由","分开
     */
    public void buildXMLDoc(String XmlFilePath,String chnlid,Document document,String InfoType,String appendixid) throws WCMException{     
		System.out.println(document.getTitle()+"的地址是"+chnlid);
    	try {
			HashMap hashMap = new HashMap();
		    String [] info_types = InfoType.split(";");
		    for(int i= 0 ; i<info_types.length ;i++) {
		    	String info_type = info_types[i];
		    	hashMap.put(info_type.substring(0, info_type.indexOf("|")), info_type.substring(info_type.indexOf("|")+1));
		    }
		    Iterator it = hashMap.keySet().iterator();
		    String  info_type_id = "";
		    while(it.hasNext()) {  
	            String key = (String)it.next();
	            String value = (String) hashMap.get(key);//市政府的栏目id
	            if(value.indexOf(chnlid) > -1) {
	            	info_type_id = key;
	            	//break;
	            }else{
	            	info_type_id = "1";
	            }
	        }
			//创建根节点 并设置它的属性 ;     
			Element root = new Element("ROOT");     
			//将根节点添加到文档中；     
			org.jdom.Document Doc = new org.jdom.Document(root);           
			
			//创建节点对接系统信息  
			Element ele_sys = new Element("SYSTEM");    
			//对接系统标识
			ele_sys.addContent(new Element("SYSTEM_ID").addContent(new CDATA("standAlte_sfzggw")));
			//对接系统校验码
			ele_sys.addContent(new Element("CHECK_CODE").addContent(new CDATA("J4Z!w664AxUJ1Cx+5RiSDbga&uH2ou")));
			//对接机构标识
			ele_sys.addContent(new Element("SYS_ORGAN_ID").addContent(new CDATA("62")));
			root.addContent(ele_sys); 
			Element ele_ref = new Element("REF");    
			//服务端目录信息标识
			ele_ref.addContent(new Element("HOST_INFO_ID").addContent(new CDATA("")));
			//客户端目录信息标识，如果曾经上行过此信息，服务端将把上次传给服务端的客户端标识反馈；否则为空
			String docid =  String.valueOf(document.getDocId());
			ele_ref.addContent(new Element("GUEST_INFO_ID").addContent(new CDATA(docid)));
			root.addContent(ele_ref);  
			//目录信息节点
			Element ele_loginfo = new Element("CATALOGINFO");    
			//类别标识:机构职能	1,法规文件2,规划计划3,行政职责4,业务动态5。通过配置文件获取类别标识：key分别为1，2，3，4，5，value分别为相对应的服务端栏目id
			ele_loginfo.addContent(new Element("INFO_TYPE_ID").addContent(new CDATA(info_type_id)));
			//目录标识：子栏目：1895,1896,1897,1898,1899。父栏目：1894
			ele_loginfo.addContent(new Element("CATALOG_ID").addContent(new CDATA(chnlid)));
			//目标栏目
			//ele_loginfo.addContent(new Element("CATALOG_NAME").addContent(new CDATA(this.getCnameByCid(chnlid))));
			//索引号，公开过的信息存在索引号，未公开过的信息索引号为undefined
			ele_loginfo.addContent(new Element("INDEX_NUMBER").addContent(new CDATA("undefined")));
			//文号，某些信息可能不存在文号
			String wenhao = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getProperty("WENHAO")),""),"");
			if(wenhao.replaceAll(" ", "").toLowerCase().equals("null")) 
				wenhao = "";
			ele_loginfo.addContent(new Element("DOC_NUMBER").addContent(new CDATA(wenhao)));
			//信息名称
			ele_loginfo.addContent(new Element("INFO_NAME").addContent(new CDATA(document.getTitle())));
			//关键词
			String keyword = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getKeywords()),""),"");
			if(keyword.replaceAll(" ", "").toLowerCase().equals("null")) 
				keyword = "　";
			ele_loginfo.addContent(new Element("KEYWORD").addContent(new CDATA(keyword)));
			//内容概述
			String abs = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getAbstract()),""),"");
			if(abs.replaceAll(" ", "").toLowerCase().equals("null")) 
				abs = "　";
			ele_loginfo.addContent(new Element("CONT_SUMRY").addContent(new CDATA(abs)));
			//备注
			ele_loginfo.addContent(new Element("RMRK").addContent(new CDATA("")));
			//载体类型
			String carrier_type = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getProperty("AYSJ_RESULT")),""),"");
			if(carrier_type.replaceAll(" ", "").toLowerCase().equals("纸质"))
				carrier_type = "2057afe3e9e249369d8a12d9ab8c72eb;";
			else if(carrier_type.replaceAll(" ", "").toLowerCase().equals("胶卷"))
				carrier_type = "5f77a15438ca4f51acd99758c074a2f5;";
			else if(carrier_type.replaceAll(" ", "").toLowerCase().equals("磁带"))
				carrier_type = "9afcd2d2d30a4f47828facc3aec5880b;";
			else if(carrier_type.replaceAll(" ", "").toLowerCase().equals("磁盘"))
				carrier_type = "499c142670524b7e9e72203c0173ef06;";
			else if(carrier_type.replaceAll(" ", "").toLowerCase().equals("光盘"))
				carrier_type = "6f12d9462ee44e9b8619fe01da0971ab;";
			else 
				carrier_type = "9758b60bb1164696b5ada900487bf3f7;";
			ele_loginfo.addContent(new Element("CARRIER_TYPE").addContent(new CDATA(carrier_type)));
			//其他载体类型
			ele_loginfo.addContent(new Element("OTHER_CARRIER_TYPE").addContent(new CDATA("9758b60bb1164696b5ada900487bf3f7;")));
			//类别记录形式
			String record_from = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getProperty("AYSJ_TYPE")),""),"");
			if(record_from.replaceAll(" ", "").toLowerCase().equals("文本"))
				record_from = "36decd92e590447f84a5b3d7eced1294;";
			else if(record_from.replaceAll(" ", "").toLowerCase().equals("图表"))
				record_from = "376cc424aab645398c0ab980f6f435f8;";
			else if(record_from.replaceAll(" ", "").toLowerCase().equals("照片"))
				record_from = "c7ee38ff48d640deb840d12714e4e37b;";
			else if(record_from.replaceAll(" ", "").toLowerCase().equals("影音"))
				record_from = "fd29d66b5f764b9989180844a5125bd7;";
			else 
				record_from = "36decd92e590447f84a5b3d7eced1294;";
			ele_loginfo.addContent(new Element("RECORD_FORM").addContent(new CDATA(record_from)));	
			//其他记录形式
			ele_loginfo.addContent(new Element("OTHER_RECORD_FORM").addContent(new CDATA("36decd92e590447f84a5b3d7eced1294;")));
			//公开形式:网站公开
			ele_loginfo.addContent(new Element("PUB_METHOD").addContent(new CDATA("9f6449199ef94ed19b489af08a19b622;")));
			//其他公开形式
			ele_loginfo.addContent(new Element("OTHER_PUB_METHOD").addContent(new CDATA("722650f1ad874dfdb49b2aedab036070;")));
			//公开类别:主动公开
			ele_loginfo.addContent(new Element("PUB_TYPE").addContent(new CDATA("cc0a2c7e0e0441248c731dd8588bf5da;")));
			//公开责任部门
			ele_loginfo.addContent(new Element("PUB_DUTY_DEPT").addContent(new CDATA("北京市发展和改革委员会")));
			//公开日期
			String pub_date = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getPubTime()),""),"");
			if(pub_date.replaceAll(" ", "").toLowerCase().equals("")) 
				pub_date = CMyDateTime.now().toString();
			ele_loginfo.addContent(new Element("PUB_DATE").addContent(new CDATA(pub_date)));
			
			
			//生成日期
			String generate_date = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getProperty("RUN_DATE")),""),"");
			if(generate_date.replaceAll(" ", "").toLowerCase().equals("")) 
				generate_date = pub_date;
			ele_loginfo.addContent(new Element("GENERATE_DATE").addContent(new CDATA(generate_date+ ".0")));
			
			
			//信息有效性
			String is_valid = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getProperty("AYSJ_STARTTIME")),""),"");
			if(is_valid.replaceAll(" ", "").toLowerCase().equals("null")) 
				is_valid = "8866f682d54841f78d13c602153f5fa2;";
			else if(is_valid.replaceAll(" ", "").toLowerCase().equals("是"))
				is_valid = "8866f682d54841f78d13c602153f5fa2;";
			else if(is_valid.replaceAll(" ", "").toLowerCase().equals("否"))
				is_valid = "830d5a17b6044a0c9838520061f91ecc;";
			ele_loginfo.addContent(new Element("IS_VALID").addContent(new CDATA(is_valid)));
			root.addContent(ele_loginfo);  
			Element ele_stat = null;
			String info_name = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getTitle()),""),"");
			String info_cont = CMyString.showEmpty(CMyString.showNull(String.valueOf(document.getHtmlContent()),""),"");
			
			//公示结果
			if("1".equals(info_type_id)){
				ele_stat = type_1(docid,info_name,info_cont);
			}
			//法律法规
			if(info_type_id.equals("2")){
				ele_stat = type_2(docid,info_name,info_cont);
			}
			//规划
			if(info_type_id.equals("3")){
				ele_stat = type_3(docid,info_name,info_cont);
			}
			//行政职责
			if(info_type_id.equals("4")){
				ele_stat = type_4(docid,info_name,info_cont);
			}
			//业务动态
			if(info_type_id.equals("5")){
				ele_stat = type_5(docid,info_name,info_cont);
			}
				
			if(ele_stat != null){
				root.addContent(ele_stat);
			}
			
			//信息内容中包含的外部文件
			String regEx = " src\\=\\\"(.*?)\\\"";
    		Pattern p = Pattern.compile(regEx); 
    		Matcher m = p.matcher(info_cont);
    		int temp_1 = 0;
    		Element ele_exte = new Element("INFOEXTERNALFILES");
    		while (m.find()) {
    			String filename = m.group(1);
    			Element ele_file = new Element("FILE");  
    			//文件名，通过文件名定位文件
    			ele_file.addContent(new Element("FILE_NAME").addContent(new CDATA(filename)));
    			//在富文本中的文件URL，通过URL定位富文本连接位置
    			ele_file.addContent(new Element("FILE_URL").addContent(new CDATA(filename)));
    			ele_exte.addContent(ele_file); 
    			temp_1++;
    		}
			root.addContent(ele_exte); 
			if (temp_1 == 0)
				root.removeContent(ele_exte);
						
			int temp_2 = 0;
			Element ele_attas =new Element("ATTACHMENTS");    
			if(!appendixid.equals("")){
				String[] appendixids = appendixid.split(",");
				s_logger.debug("附件数组的长度"+appendixids.length);
				
				for(int i = 0;i < appendixids.length;i++){
					Appendix appendix;
					try {
						appendix = Appendix.findById(Integer.parseInt(appendixids[i]));
		    			String filename = appendix.getFile();
		    			String filedesc = appendix.getDesc();
		    			filedesc = filedesc.substring(0,filedesc.lastIndexOf("."));
						Element ele_atta = new Element("ATTACHMENT");  
						//附件标识，附件名：attachment_附件标识.dat，如attachment_2343.dat
						ele_atta.addContent(new Element("ATCH_ID").addContent(new CDATA(appendixids[i])));
						//附件名称
						ele_atta.addContent(new Element("ATCH_NAME").addContent(new CDATA(filedesc)));
						//文件名
						ele_atta.addContent(new Element("FILE_NAME").addContent(new CDATA(filename)));
						ele_attas.addContent(ele_atta); 
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (WCMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					temp_2++;
					}
			}
			root.addContent(ele_attas); 
			if (temp_2 == 0)
				root.removeContent(ele_attas);
			root.clone();
			
			//参数为设置缩进效果、换行、编码格式
			XMLOutputter XMLOut = new XMLOutputter("	",true,"GBK");  
			FileOutputStream fileOutputStream = new FileOutputStream(XmlFilePath+"/param.xml");
			XMLOut.output(Doc, fileOutputStream);
			fileOutputStream.close();
			XMLOut = null;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    private Element type_2(String docid,String info_name,String info_cont) {
    	//法规文件类信息内容,可以根据已经获取到的类别类型标识输出的xml标签
		Element ele_stat = new Element("INFOCONTENT");    
		//法规类信息标识
		ele_stat.addContent(new Element("STAT_INFO_ID").addContent(new CDATA(docid)));
		//信息名称
		ele_stat.addContent(new Element("INFO_NAME").addContent(new CDATA(info_name)));
		ele_stat.addContent(new Element("INFO_CONT").addContent(new CDATA(info_cont)));		
		return ele_stat;
	}
    private Element type_3(String docid,String info_name,String info_cont) {
    	//规划计划类信息内容
		Element ele_stat = new Element("INFOCONTENT");    
		//规划计划类信息标识
		ele_stat.addContent(new Element("PLAN_INFO_ID").addContent(new CDATA(docid)));
		//信息名称
		ele_stat.addContent(new Element("INFO_NAME").addContent(new CDATA(info_name)));
		//正文
		ele_stat.addContent(new Element("INFO_CONT").addContent(new CDATA(info_cont)));		
		return ele_stat;
	}
    private Element type_4(String docid,String info_name,String info_cont) {
    	//业务工作信息内容
		Element ele_stat = new Element("INFOCONTENT");    
		//业务工作信息标识
		ele_stat.addContent(new Element("WORK_INFO_ID").addContent(new CDATA(docid)));
		//受理事项（办事事项：非办理类：0和办理类：1）
		ele_stat.addContent(new Element("WORK_ITEM").addContent(new CDATA("87a9ef9dc42c43c3948f812ab0942ecf;")));	
		//信息名称
		ele_stat.addContent(new Element("INFO_NAME").addContent(new CDATA(info_name)));
		//正文
		ele_stat.addContent(new Element("INFO_CONT").addContent(new CDATA(info_cont)));		
		return ele_stat;
	}
    private Element type_5(String docid,String info_name,String info_cont) {
    	//动态类信息内容
		Element ele_stat = new Element("INFOCONTENT");    
		//动态类信息标识
		ele_stat.addContent(new Element("DYNA_INFO_ID").addContent(new CDATA(docid)));
		//信息类别（目录：1，目录与链接：2，目录与信息：3）
		ele_stat.addContent(new Element("INFO_TYPE").addContent(new CDATA("8cc7e52c09bf417cb38bc6a0653f5736;")));	
		//信息链接
		ele_stat.addContent(new Element("INFO_LINK").addContent(new CDATA("")));	
		//信息名称
		ele_stat.addContent(new Element("INFO_NAME").addContent(new CDATA(info_name)));
		//正文
		ele_stat.addContent(new Element("INFO_CONT").addContent(new CDATA(info_cont)));		
		return ele_stat;
	}
    private Element type_1(String docid,String info_name,String info_cont) {
    	//动态类信息内容
		Element ele_stat = new Element("INFOCONTENT");    
		//动态类信息标识
		ele_stat.addContent(new Element("DYNA_INFO_ID").addContent(new CDATA(docid)));
		//信息类别（目录：1，目录与链接：2，目录与信息：3）
		ele_stat.addContent(new Element("INFO_TYPE").addContent(new CDATA("8cc7e52c09bf417cb38bc6a0653f5736;")));	
		//信息链接
		ele_stat.addContent(new Element("INFO_LINK").addContent(new CDATA("")));	
		//信息名称
		ele_stat.addContent(new Element("INFO_NAME").addContent(new CDATA(info_name)));
		//正文
		ele_stat.addContent(new Element("INFO_CONT").addContent(new CDATA(info_cont)));		
		return ele_stat;
	}
    /*
     * 获取附件
     * @param document wcm的document对象
     * @param newfilepath 生成xml文件的路径，即附件下载后的路径
     * return appendixname 返回多个附件名称
     * */
    public String getAppendix(Document document,String newfilepath,String LOCALURL) {
    	//String LOCALURL = "/wcm/TRSWCMV65/WCMData/";	//wcm本机访问地址G:\TRS\TRSWCMV7\WCMData
    	//String LOCALURL = "G:\\TRS\\TRSWCMV7\\WCMData\\";	//wcm本机访问地址    	
    	String appendixid = "";
    	String value = "";
    	int docid = document.getDocId();
    	try {
    		appendixid = getAppendixid(docid);
    		if(!appendixid.equals("")){
    			String[] apdids = appendixid.split(",");
        		for(int i = 0;i < apdids.length;i++) {
        			int apdid = Integer.parseInt(apdids[i]);
        			Appendix appendix = Appendix.findById(apdid);
        			String filename = appendix.getFile();
        			int fileext = appendix.getFlag();
        			String filehz = appendix.getFileExt();
        			String file_url = "";
        			if(appendix.getDesc().indexOf("正文-") < 0 ){
        				if (fileext == 10) {
        					file_url = LOCALURL + "protect/" + filename.substring(0, 8) + "/" + filename.substring(0,10) + "/" + filename;
        					/**********新增修改部分************/
        					if(!filehz.equals("exe") && !filehz.equals("jspx") && !filehz.equals("bat") && !filehz.equals("dll") && !filehz.equals("so") && !filehz.equals("acm") && !filehz.equals("com") && !filehz.equals("cpl") && !filehz.equals("drv") && !filehz.equals("scr") && !filehz.equals("sys")){
        						copyfile(file_url,newfilepath,filename);
        						//renamefile(newfilepath+"/"+filename,newfilepath+"/"+"attachment_"+apdid+".dat");
        					}
        				} else if ( fileext == 20) {
        					file_url = LOCALURL + "webpic/" + filename.substring(0, 8) + "/" + filename.substring(0,10) + "/" + filename;
        					/**********新增修改部分************/
        					if(!filehz.equals("exe") && !filehz.equals("jspx") && !filehz.equals("bat") && !filehz.equals("dll") && !filehz.equals("so") && !filehz.equals("acm") && !filehz.equals("com") && !filehz.equals("cpl") && !filehz.equals("drv") && !filehz.equals("scr") && !filehz.equals("sys")){
        						copyfile(file_url,newfilepath,filename);
        						//renamefile(newfilepath+"/"+filename,newfilepath+"/"+"attachment_"+apdid+".dat");
        					}
        				}
        				value += apdid + ",";
        			}
        		}
    		}
    		String dochtmlcon = document.getHtmlContent();
    		String regEx = " src\\=\\\"(.*?)\\\"";
    		Pattern p = Pattern.compile(regEx); 
    		Matcher m = p.matcher(dochtmlcon);
    		while (m.find()) {
    			String filename = m.group(1);
    			String file_url = LOCALURL + "webpic/" + filename.substring(0, 8) + "/" + filename.substring(0,10) + "/" + filename;
    			copyfile(file_url,newfilepath,filename);
    		}
		} catch (WCMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return value;
	}
    /*
     * 复制文件
     * @param sourcefile 源路径，即服务器附件所在路径
     * @param tofile 生成xml文件的路径，即附件下载后的路径
     * @param filename 下载后附件名称
     * */
    public void copyfile(String sourcefile,String tofile,String filename){
    	try{
			FileInputStream fis=new FileInputStream(sourcefile);//可替换为任何路径何和文件名
			FileOutputStream fos=new FileOutputStream(tofile+"/"+filename);//可替换为任何路径何和文件名
			int in=fis.read();
			while(in!=-1){
				fos.write(in);
			in=fis.read();
			}
			fos.close();
			fis.close(); 
		}catch (IOException e){
			s_logger.debug("复制文件发生异常："+e);
		}
    }
    /*
     * 修改文件名和后缀名
     * @param sourcefile 源路径，即服务器附件所在路径
     * @param tofile 生成xml文件的路径，即附件下载后的路径
     * @param filename 下载后附件名称
     * */
    public void renamefile(String sourcefile,String filename){
    	File file = new File(sourcefile);
    	file.renameTo(new File(filename));
    }
}
