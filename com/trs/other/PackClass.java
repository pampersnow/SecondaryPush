package com.trs.other;


import com.trs.infra.common.WCMException;
import com.trs.webframework.controler.JSPRequestProcessor;

public class PackClass {

	public Integer publicPack(String nRand,String oRand,JSPRequestProcessor processor)throws WCMException{
		String sServiceId = "wcm61_role", sMethodName = "save";
		Integer oRoleId = 0;
		if(nRand != null && oRand != null && !"".equals(nRand) && !"".equals(oRand)){
			if(Integer.parseInt(nRand) != Integer.parseInt(oRand)){
				System.out.println("验证码不对");
			}else{
				oRoleId = (Integer)processor.excute(sServiceId,sMethodName);
			}
		}else{
			System.out.println("空参");
		}
		return oRoleId;
	}
	
	public Integer publicPackToUser(JSPRequestProcessor processor,String nRand,String oRand,String sServiceId,String sMethodName)throws WCMException{
		Integer oUserId = 0;
		if(nRand != null && oRand != null && !"".equals(nRand) && !"".equals(oRand)){
			if(Integer.parseInt(nRand) != Integer.parseInt(oRand)){
				System.out.println("验证码不对");
			}else{
				oUserId = (Integer)processor.excute(sServiceId,sMethodName);
			}
		}else{
			System.out.println("空参");
		}
		
		return oUserId;
	}
}
