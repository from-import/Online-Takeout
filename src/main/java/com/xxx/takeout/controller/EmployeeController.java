package com.xxx.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxx.takeout.entity.Employee;
import com.xxx.takeout.common.R;
import com.xxx.takeout.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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


    // 员工登录
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 查询账号对应密码md5
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(employeeLambdaQueryWrapper);

        // 判断
        if(emp == null){
            return R.error("failed");
        }

        // 密码比对
        if(!emp.getPassword().equals(password)){
            return R.error("failed");
        }

        // 状态查看
        if(emp.getStatus() == 0){
            return R.error("account blocked");
        }

        // 登陆成功
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    // 员工退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("Quit success");
    }


    // 新增员工
    @PostMapping("")
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工{}",employee.getName());
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 当前登录用户
        long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("Added");
    }

    // 员工信息页面展示查询(页面需要Page类)
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize ,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        // 1.分页构造器
        Page pageInfo = new Page(page, pageSize);

        // 2.条件构造器
        // 过滤条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like((name != null),Employee::getName, name);
        // 排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 3.执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    // 更改员工信息(根据ID)
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("更改员工信息：{}", employee.toString());

        long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);

        // 根据 employee 对象中的 id 字段，在数据库中查找对应的记录。
        // 更新：将该记录的字段更新为 employee 对象中的新值。仅更新不为 null 的字段，其他字段保持不变（这依赖于 MyBatis-Plus 的默认行为或配置）。
        employeeService.updateById(employee);
        return R.success("success changed");
    }
}
