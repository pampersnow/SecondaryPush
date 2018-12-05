package com.trs.components.wcm.publish;
import java.io.File;
import java.io.PrintStream;
import com.trs.DreamFactory;
import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.User;
import com.trs.components.common.job.BaseStatefulScheduleWorker;
import com.trs.components.common.publish.PublishConstants;
import com.trs.components.common.publish.domain.template.TemplateExporter;
import com.trs.components.common.publish.domain.template.TemplateMgr;
import com.trs.components.common.publish.persistent.element.IPublishFolder;
import com.trs.components.common.publish.persistent.element.PublishElementFactory;
import com.trs.components.common.publish.persistent.template.Templates;
import com.trs.components.wcm.content.persistent.WebSite;
import com.trs.components.wcm.content.persistent.WebSites;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;
import com.trs.infra.support.file.FilesMan;
import com.trs.infra.util.CMyDateTime;
import com.trs.infra.util.CMyString;

/**
 * @author Administrator
 * 
 */
public class BackupTemplates extends BaseStatefulScheduleWorker {

	/**
     * 
     */
	public BackupTemplates() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.trs.infra.util.job.BaseJob#execute()
	 */
	protected void execute() throws WCMException {
		// 初始化上下文对象
		String userName = getArgAsString("CrUser");
		ContextHelper.initContext(User.getSystem());
		User m_oScheduleUser = User.findByName(userName);
		if (m_oScheduleUser == null) {
			m_oScheduleUser = User.getSystem();
		}

		ContextHelper.setLoginUser(m_oScheduleUser);
		// 获取定时备份模板的路径
		String sDstPath = getArgAsString("DstPath");
		sDstPath = CMyString.isEmpty(sDstPath) ? "/tmp/" : sDstPath.trim();
		sDstPath = CMyString.setStrEndWith(sDstPath, File.separatorChar);
		sDstPath += CMyDateTime.now().toString("yyyyMMdd") + File.separatorChar;

		// 获取所有站点
		WCMFilter filter = new WCMFilter("", "Status>=0", "SiteId");
		WebSites sites = WebSites.openWCMObjs(null, filter);

		// 遍历逐个备份站点中所有模板
		TemplateMgr oTemplateMgr = (TemplateMgr) DreamFactory
				.createObjectById("TemplateMgr");
		try {
			System.out.println("templete backup start！");
			for (int i = 0, nSize = sites.size(); i < nSize; i++) {
				WebSite site = (WebSite) sites.getAt(i);
				System.out
						.println("********************************************"
								+ site);
				if (site == null) {
					continue;
				}
				String sitetype = "cas";
				if (site.getType() == 1)
					sitetype = "pic";
				if (site.getType() == 2)
					sitetype = "vod";
				if (site.getType() == 4)
					sitetype = "sourcedb";
				// 查询出当前站点所有的模板
				IPublishFolder folder = (IPublishFolder) PublishElementFactory
						.makeElementFrom(site);
				Templates templates = oTemplateMgr.getManagedTemplates(folder,
						PublishConstants.TEMPLATE_TYPE_ANY, null, true);
				System.out
						.println("============================================="
								+ templates.isEmpty());
				if (templates.isEmpty()) {
					continue;
				}
				// 导出文档
				TemplateExporter exporter = new TemplateExporter();
				String sExportFile = exporter.export(templates);

				// 将文件移动到指定目录中
				String sDstFilePath = sDstPath + site.getId() + "_" + sitetype
						+ "_" + site.getDataPath() + File.separatorChar;
				File pathFile = new File(sDstFilePath);
				pathFile.mkdirs(); // 创建目标目录
				String sSrcFilePath = FilesMan.getFilesMan().mapFilePath(
						sExportFile, FilesMan.PATH_LOCAL);
				File oSrcFile = new File(sSrcFilePath + sExportFile);
				oSrcFile.renameTo(new File(sDstFilePath + site.getId() + "_"
						+ sitetype + "_" + site.getDataPath() + ".zip"));// 移动文件
			}
			System.out.println("templete backup end!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
