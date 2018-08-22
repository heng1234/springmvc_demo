package com.spring.loren.controller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spring.loren.annotation.LorenAutowired;
import com.spring.loren.annotation.LorenController;
import com.spring.loren.annotation.LorenRequestMapping;
import com.spring.loren.annotation.LorenRequestParam;
import com.spring.loren.service.HlvyService;
@LorenController
@LorenRequestMapping("/loren")
public class HlvyController {

	@LorenAutowired("hlvyServiceImpl")
	private HlvyService hlvyService;//spring通过map.get("hlvyServiceImpl")拿到给他实例化
	
	@LorenRequestMapping("/query")
	public void query(HttpServletRequest request,HttpServletResponse response,
			@LorenRequestParam("name") String name,@LorenRequestParam("pwd") String pwd) {
		String res = hlvyService.findAll(name, pwd);//拿到返回的数据
		
		PrintWriter pw;//io对象
		
		try {
			pw = response.getWriter();
			pw.write(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
