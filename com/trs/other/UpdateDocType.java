package com.trs.other;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.components.common.publish.PublishConstants;
import com.trs.components.common.publish.domain.PublishServer;
import com.trs.components.common.publish.persistent.element.IPublishContent;
import com.trs.components.common.publish.persistent.element.PublishElementFactory;
import com.trs.components.wcm.content.domain.AppendixMgr;
import com.trs.components.wcm.content.persistent.Appendix;
import com.trs.components.wcm.content.persistent.Appendixes;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.Channels;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyString;
/**
 * @author JYB
 * @date  2018.11
 */
public class UpdateDocType extends BaseStatefulScheduleWorker {

	private static Logger s_logger = Logger
			.getLogger(com.trs.other.FilteContent.class);

	@Override
	protected void execute() throws WCMException {
		String sSfqy = CMyString.showNull(getArgAsString("sfqy"), "0");
		if (!("1".equals(sSfqy))) {
			s_logger.info(" 未启用该策略！");   
			return;
		}
		String siteid = CMyString.showNull(getArgAsString("siteid"), "0");
		String sDocid = CMyString.showNull(getArgAsString("docid"), "0");
		ContextHelper.initContext(User.findByName("admin"));
		User currentUser = ContextHelper.getLoginUser();
		try {
			if(!"0".equals(sDocid)){
				s_logger.info("对一篇文档操作");
				Document tDocument = Document.findById(Integer
						.parseInt(sDocid));
				String content = tDocument.getContent();
				// 判断正文是否为链接
				if (this.isHttpUrl(content)) {
					s_logger.info("更新数据：正文仅有链接");
					tDocument.setType(Document.DOC_TYPE_LINK);
					tDocument.setPropertyWithString("DOCLINK",
							content);
					tDocument.save();
				}
				
				//判断没有正文并且只有一条附件的
				if("".equals(content) || "&nbsp;".equals(content) || content == null){
					AppendixMgr mgr = new AppendixMgr();
					Appendixes appendixes = mgr.getAppendixes(tDocument,10);
					if(appendixes.size() == 1){
						s_logger.info("更新数据：无正文且附件只有一条");
						Appendix appendix = (Appendix)appendixes.getAt(0);
						String appfile = appendix.getFile();
						tDocument.setType(Document.DOC_TYPE_FILE);
						tDocument.setPropertyWithString("DOCFILENAME",
								appfile);
						tDocument.save();
					}
				}
			}else{
				s_logger.info("对所有文档操作");
				Channels channels = this.queryChannels(currentUser, siteid);
				s_logger.info("--------------------->查询到" + channels.size() + "条栏目");
				if (channels != null && channels.size() != 0) {
					for (int i = 0; i < channels.size(); i++) {
						Channel channel = (Channel) channels.getAt(i);
						if (channel == null)
							continue;
						if (channel.getAllChildren(currentUser).size() != 0)
							continue;
						int channelId = channel.getId();
						ArrayList<HashMap<String, String>> documents = this
								.queryData(channelId);
						s_logger.info(channel.getName() + "栏目下有" + documents.size()
								+ "条数据");
						//int size = 0;
						if (documents != null && documents.size() != 0) {
							/*if(documents.size() > 100){
								size = 100;
							}else{
								size = documents.size();
							}*/
							for (int j = 0; j < documents.size(); j++) {
								HashMap<String, String> data = documents.get(j);
								String docid = data.get("DOCID");
								String doccontent = data.get("DOCCONTENT");

								// 判断正文是否为链接
								if (this.isHttpUrl(doccontent)) {
									s_logger.info("更新数据：正文仅有链接");
									Document document = Document.findById(Integer
											.parseInt(docid));
									document.setType(Document.DOC_TYPE_LINK);
									document.setPropertyWithString("DOCLINK",
											doccontent);
									document.save();
									//this.resetPublish(document);
								}							
								//判断没有正文并且只有一条附件的
								if("".equals(doccontent) || "&nbsp;".equals(doccontent) || doccontent == null){
									AppendixMgr mgr = new AppendixMgr();
									Document document = Document.findById(Integer
											.parseInt(docid));
									Appendixes appendixes = mgr.getAppendixes(document,10);
									if(appendixes.size() == 1){
										s_logger.info("更新数据：无正文且附件只有一条");
										Appendix appendix = (Appendix)appendixes.getAt(0);
										String appfile = appendix.getFile();
										document.setType(Document.DOC_TYPE_FILE);
										document.setPropertyWithString("DOCFILENAME",
												appfile);
										document.save();
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void resetPublish(Document document) throws Exception {
		IPublishContent ipContent = PublishElementFactory.makeContentFrom(
				document, null);
		PublishServer publishServer = PublishServer.getInstance();
		publishServer.publishContent(ipContent,
				PublishConstants.PUBLISH_CONTENT);
		s_logger.info("更新数据：(" + document.getTitle() + ")");
	}

	private ArrayList<HashMap<String, String>> queryData(int channelId)
			throws Exception {
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		String sql = "SELECT WD.DOCID,WD.DOCTITLE,WD.DOCCONTENT,WD.DOCHTMLCON FROM WCMDOCUMENT WD WHERE WD.DOCSTATUS = 10 AND WD.DOCTYPE = 20 AND WD.DOCCHANNEL = "
				+ channelId + " ORDER BY WD.DOCID DESC";
		try {
			db = DBManager.getDBManager();
			conn = db.getConnection();
			stmt = conn.createStatement();
			s_logger.info("执行sql:" + sql.toString());
			resultSet = stmt.executeQuery(sql.toString());
			if (resultSet != null) {
				while (resultSet.next()) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("DOCID", resultSet.getString("DOCID") == null ? ""
							: resultSet.getString("DOCID"));
					map.put("DOCTITLE",
							resultSet.getString("DOCTITLE") == null ? ""
									: resultSet.getString("DOCTITLE"));
					map.put("DOCCONTENT",
							resultSet.getString("DOCCONTENT") == null ? ""
									: resultSet.getString("DOCCONTENT"));
					map.put("DOCHTMLCON",
							resultSet.getString("DOCHTMLCON") == null ? ""
									: resultSet.getString("DOCHTMLCON"));
					result.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private Channels queryChannels(User currentUser, String siteid)
			throws Exception {
		StringBuilder sWhere = new StringBuilder("CHNLTYPE = 0 AND SITEID = "
				+ siteid);
		WCMFilter _filter = new WCMFilter("", sWhere.toString(), "CHANNELID");
		Channels channels = Channels.openWCMObjs(currentUser, _filter);
		return channels;
	}

	private boolean isHttpUrl(String urls) throws Exception {
		boolean isurl = false;
		String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
				+ "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";// 设置正则表达式

		Pattern pat = Pattern.compile(regex.trim());
		Matcher mat = pat.matcher(urls.trim());
		isurl = mat.matches();
		if (isurl) {
			isurl = true;
		}
		return isurl;
	}

	@SuppressWarnings("unused")
	private static String getHref(String content) throws Exception {
		String regex = "<a[\\s]+href[\\s]*=[\\s]*\"([^<\"]+)\"";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		Matcher m = p.matcher(content);

		StringBuffer ret = new StringBuffer();
		while (m.find()) {
			ret.append(m.group(1));
		}

		return ret.toString();
	}
}
