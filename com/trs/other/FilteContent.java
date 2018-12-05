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
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.Channels;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.persistent.db.DBManager;
import com.trs.infra.util.CMyString;

public class FilteContent extends BaseStatefulScheduleWorker {

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
		ContextHelper.initContext(User.findByName("admin"));
		User currentUser = ContextHelper.getLoginUser();
		try {
			Channels channels = this.queryChannels(currentUser,siteid);
			s_logger.info("查询到" + channels.size() + "条栏目");
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
					if (documents != null && documents.size() != 0) {
						for (int j = 0; j < documents.size(); j++) {
							HashMap<String, String> data = documents.get(j);
							String docid = data.get("DOCID");
							String content = data.get("DOCHTMLCON");
							String newContent = content;
							if (content.indexOf("？") != -1) {
								content = content.replace("？", "");
							}
							if (content.indexOf(">&lt;!") != -1
									|| content.indexOf("--&gt;") != -1) {
								content = this.filteText(content);
							}
							if (!content.equals(newContent)) {
								Document document = Document.findById(Integer
										.parseInt(docid));
								document.setHtmlContent(content);
								document.save();
								/*IPublishContent ipContent = PublishElementFactory
										.makeContentFrom(document, null);
								PublishServer publishServer = PublishServer
										.getInstance();
								publishServer.publishContent(ipContent,
										PublishConstants.PUBLISH_CONTENT);*/
								s_logger.info("更新数据：(" + document.getTitle()
										+ ")");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String filteText(String content) throws Exception {
		String result = "";
		if (!"".equals(content) && !"&nbsp;".equals(content)) {
			content = content.replace("&lt;", "<");
			content = content.replace("&gt;", ">");

			String regEx_o = "<\\!--.*-->";
			Pattern p_o = Pattern.compile(regEx_o, Pattern.CASE_INSENSITIVE);
			Matcher m_o = p_o.matcher(content);
			result = m_o.replaceAll("");
		}
		return result;
	}

	private ArrayList<HashMap<String, String>> queryData(int channelId)
			throws Exception {
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		DBManager db = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		String sql = "SELECT WD.DOCID,WD.DOCHTMLCON FROM WCMDOCUMENT WD WHERE WD.DOCSTATUS = 10 AND WD.DOCCHANNEL = "
				+ channelId;
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

	private Channels queryChannels(User currentUser,String siteid) throws Exception {
		StringBuilder sWhere = new StringBuilder(
				"CHNLTYPE = 0 AND SITEID = "+siteid);
		WCMFilter _filter = new WCMFilter("", sWhere.toString(), "CHANNELID");
		Channels channels = Channels.openWCMObjs(currentUser, _filter);
		return channels;
	}
}
