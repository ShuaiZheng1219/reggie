package com.zs.reggie.utils;


import org.apache.commons.mail.HtmlEmail;

/**
 * @Author Jun
 * @Date 2019/4/28 16:53
 * @Description 发送邮件的工具类
 */
public class SendMailUtil {
    //邮箱验证码
    public static boolean sendEmail(String emailAddress, String code) {
        try {
            HtmlEmail email = new HtmlEmail();//不用更改
            email.setHostName("smtp.163.com");//需要修改，126邮箱为smtp.126.com,163邮箱为163.smtp.com，QQ为smtp.qq.com
            email.setCharset("UTF-8");
            email.addTo(emailAddress);// 客户机邮箱
            //email为服务器邮箱  name为服务器邮箱备注
            email.setFrom("zs19981219@163.com", "瑞吉外卖");
            email.setAuthentication("zs19981219@163.com", "SNXDCGXKKBHXMVNF");//需要更改服务端邮箱地址和授权码
            email.setSubject("注册验证码");
            email.setMsg("尊敬的用户您好,您本次注册的验证码是:\n" + "<h1 style=\"color:red;\">" + code + "</h1>");//此处填写邮件内容,可以为验证码添加颜色，邮件正文实际上也是HTML

            // email.setSSLOnConnect(false);
            //启用ssl加密
            email.setSSLOnConnect(true);
            //使用465端口(不设置也可，ssl默认为465)
            email.setSslSmtpPort("465");
            email.send();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


