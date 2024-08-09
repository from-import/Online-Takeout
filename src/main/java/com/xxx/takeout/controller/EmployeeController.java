package com.xxx.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xxx.takeout.entity.Employee;
import com.xxx.takeout.common.R;
import com.xxx.takeout.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
    public R<String> save(@RequestBody Employee employee){
        log.info("新增员工{}",employee.getName());
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        return null;
    }
}
