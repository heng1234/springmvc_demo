package com.spring.loren.service.impl;

import com.spring.loren.annotation.LorenService;
import com.spring.loren.service.HlvyService;
/**
 * 
 * @author heng
 *<p>Title: HlvyServiceImpl</p>  
 * @date 2018年8月22日
 */
@LorenService("hlvyServiceImpl")
public class HlvyServiceImpl implements HlvyService {

	/**
	 *  测试业务层service方法
	 */
	public String findAll(String name, String pwd) {
	
		return "name:"+name+",pwd:"+pwd;
	}

}
