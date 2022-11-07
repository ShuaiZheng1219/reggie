package com.zs.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zs.reggie.common.R;
import com.zs.reggie.pojo.Employee;
import com.zs.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /*
        1、密码md5加密
        2、根据username查询数据库，如果没有查询到返回结果
        3、密码比对，密码不正确，返回结果
        4、查看员工状态，如果禁用，返回结果
        5、登陆成功，将员工id放入session并返回登录成功结果
        */
        //1、加密
        String password = employee.getPassword();
        String s = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        if(emp==null){
            return R.error("用户不存在，登陆失败");
        }
        //3、密码比对
        if(!emp.getPassword().equals(s)){
            return R.error("密码错误，登陆失败");
        }
        //4、查看状态
        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }
        //5、成功
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    //登出
    @PostMapping("/logout")
    public R logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success(null);
    }
    //新增员工
    @PostMapping
    public R<String> addEmployee(HttpServletRequest request,@RequestBody Employee employee){
        //设置初始密码，md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //下面直接自动填充了
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser((Long) request.getSession().getAttribute("employee"));
//        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        //可能会出现异常，如果重复添加，使用全局异常捕获
        employeeService.save(employee);

        return R.success("增加成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("分页请求,page={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构建条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(name!=null,Employee::getName,name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 员工信息修改
     * @param request 得到此时登陆对象，保存修改信息的对象
     * @param employee 修改的对象
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("id={},status={}",employee.getId(),employee.getStatus());
        log.info("当前线程id为{}",Thread.currentThread().getId());
        //得到此时登录对象
        Long id = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(id);
        employeeService.updateById(employee);
        return R.success("操作成功");
    }

    @GetMapping("/{id}")
    public R<Employee> queryUpdateObject(@PathVariable("id") Long id){
        log.info("修改id为{}的用户",id);
        Employee emp = employeeService.getById(id);
        return R.success(emp);
    }
}
