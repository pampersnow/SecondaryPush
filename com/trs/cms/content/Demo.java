package com.trs.cms.content;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trs.cms.auth.persistent.User;
import com.trs.components.stat.BonusRule;

public class Demo {

	public BonusRule start(String xml,User loginUser,int id,Class paramClass)throws Exception{
		BonusRule bonusRule = null;
		if(xml.indexOf("PRICEPERUNIT") != -1){
			List list = getContext(xml);
			String price = list.get(2).toString();
			float f = Float.parseFloat(price);
			System.out.println(f);
			if(f < 0){
				System.out.println("输入参数有误！");
				return null;
			}else{
				bonusRule = (BonusRule)WCMObjHelper.toWCMObj(xml, loginUser, id, BonusRule.class);
			}
		}
		return bonusRule;
	}
		private static List getContext(String html) {  
		      List resultList = new ArrayList();  
		      Pattern p = Pattern.compile(">([^</]+)</");//正则表达式 commend by danielinbiti  
		      Matcher m = p.matcher(html );//  
		      while (m.find()) {  
		          resultList.add(m.group(1));//  
		      }  
		      return resultList;  
		  }
}
