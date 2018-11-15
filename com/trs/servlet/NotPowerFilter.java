package com.trs.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trs.cms.ContextHelper;
import com.trs.cms.auth.persistent.Group;
import com.trs.cms.auth.persistent.Groups;
import com.trs.cms.auth.persistent.User;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.infra.common.WCMException;
import com.trs.infra.util.CMyString;
import com.trs.presentation.util.LoginHelper;

public class NotPowerFilter implements Filter {

	private String sRedictPage = "redictPage";
	
	public void destroy() {}

	public void doFilter(ServletRequest _request, ServletResponse _response,
			FilterChain _chain) throws IOException, ServletException {
		HttpServletRequest localHttpServletRequest = (HttpServletRequest)_request;
	    HttpServletResponse localHttpServletResponse = (HttpServletResponse)_response;
	    ServletContext localServletContext = localHttpServletRequest.getSession().getServletContext();
	    
	    //1.获取当前登陆用户
	    User loginUser = ContextHelper.getLoginUser();
	    //2.获取当前请求的栏目的ID
	    String ChannelId = localHttpServletRequest.getParameter("ChannelId");
		try {
			//3.根据用户与组织判断是否执行
			boolean isCan = this.getBooleanByLoginUserAndChannelId(loginUser,ChannelId);
			System.out.println("-------------->"+isCan);
			//4.根据返回值判断是否放行
		    if(isCan){
		    	_chain.doFilter(localHttpServletRequest, localHttpServletResponse);
		    }else{
		    	localHttpServletRequest.getRequestDispatcher("/notPower.jsp").forward(localHttpServletRequest, localHttpServletResponse);
		    }
		} catch (WCMException e) {
			e.printStackTrace();
		}
	    
	}

	public void init(FilterConfig arg0) throws ServletException {}

	private boolean getBooleanByLoginUserAndChannelId(User loginUser,String ChannelId)throws IOException, ServletException,WCMException{
		boolean isCan = false;
		if("admin".equals(loginUser.getName())){
			isCan = true;
			//ContextHelper.setLoginUser(loginUser);
		}else{
			String channelName = Channel.findById(Integer.parseInt(ChannelId)).getName();
			Groups groups = loginUser.getGroups();
			for (int i = 0; i < groups.size(); i++) {
				Group group = (Group) groups.getAt(i);
				if(group == null)continue;
				if(group.getName().equals(channelName)){
					isCan = true;
					//ContextHelper.setLoginUser(loginUser);
				}
			}
		}
		return isCan;
	} 
}
