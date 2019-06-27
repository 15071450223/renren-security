/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.controller;


import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import io.renren.common.utils.R;
import io.renren.modules.sys.shiro.ShiroUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录相关
 *
 * @author Mark sunlightcs@gmail.com
 */
@Controller
public class SysLoginController {
	@Autowired
	private Producer producer;
	
	@RequestMapping("captcha.jpg")
	public void captcha(HttpServletResponse response)throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        //生成文字验证码
        String text = producer.createText();
        //生成图片验证码
        BufferedImage image = producer.createImage(text);
        //保存到shiro session
        ShiroUtils.setSessionAttribute(Constants.KAPTCHA_SESSION_KEY, text);
        
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
	}
	
	/**
	 * 登录
	 */
	@ResponseBody
	@RequestMapping(value = "/sys/login", method = RequestMethod.POST)
	public R login(String username, String password, String captcha) {
		String kaptcha = ShiroUtils.getKaptcha(Constants.KAPTCHA_SESSION_KEY);
		if(!captcha.equalsIgnoreCase(kaptcha)){
			return R.error("验证码不正确");
		}
		
		try{
			Subject subject = ShiroUtils.getSubject();
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			subject.login(token);
		}catch (UnknownAccountException e) {
			return R.error(e.getMessage());
		}catch (IncorrectCredentialsException e) {
			return R.error("账号或密码不正确");
		}catch (LockedAccountException e) {
			return R.error("账号已被锁定,请联系管理员");
		}catch (AuthenticationException e) {
			return R.error("账户验证失败");
		}
	    
		return R.ok();
	}
	
	/**
	 * 退出
	 */
	@RequestMapping(value = "logout", method = RequestMethod.GET)
	public String logout() {
		ShiroUtils.logout();
		return "redirect:login.html";
	}

	/**
	 * 获取系统信息
	 * @version <1> 2019/6/14 17:50 zhangshen:Created.
	 */
	@ResponseBody
	@RequestMapping(value = "/sys/getSysInfo", method = RequestMethod.POST)
	public R getSysInfo() {
		Map<String, Object> map = new HashMap<String, Object>();
		try {

			//运行时情况
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			//操作系统情况
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

			map.put("td_text1","renren-security【后台管理系统】");
			map.put("td_text2",runtime.getVmName());
			map.put("td_text3","v1.0.0");
			map.put("td_text4",System.getProperty("java.version"));
			map.put("td_text5",os.getName());
			map.put("td_text6",System.getProperty("java.home"));




			System.out.println(System.getProperty("java.home"));
			System.out.println(System.getProperty("java.version"));
			System.out.println(System.getProperty("os.name"));
			//当前项目下路径
			File file = new File("");
			String filePath = file.getCanonicalPath();

			map.put("td_text7",os.getArch());
			map.put("td_text8",filePath);



			//运行时情况
			System.out.printf("jvm.name (JVM名称-版本号-供应商):%s | version: %s | vendor: %s  %n", runtime.getVmName(), runtime.getVmVersion(), runtime.getVmVendor());
			System.out.printf("jvm.spec.name (JVM规范名称-版本号-供应商):%s | version: %s | vendor: %s  %n", runtime.getSpecName(), runtime.getSpecVersion(), runtime.getSpecVendor());
			System.out.printf("jvm.java.version (JVM JAVA版本):%s%n", System.getProperty("java.version"));

			System.out.println("------------------------------------------------------------------------------------------------------");

			//系统概况
			System.out.printf("os.name(操作系统名称-版本号):%s %s %s %n", os.getName(), "version", os.getVersion());
			System.out.printf("os.arch(操作系统内核):%s%n", os.getArch());
			System.out.printf("os.cores(可用的处理器数量):%s %n", os.getAvailableProcessors());
			System.out.printf("os.loadAverage(系统负载平均值):%s %n", os.getSystemLoadAverage());
		} catch (Exception e) {
			return R.error();
		}


		return R.ok(map);
	}
	
}
