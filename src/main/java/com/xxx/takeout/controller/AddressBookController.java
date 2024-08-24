package com.xxx.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxx.takeout.common.BaseContext;
import com.xxx.takeout.common.R;
import com.xxx.takeout.entity.AddressBook;
import com.xxx.takeout.service.AddressBookService;
import com.xxx.takeout.common.BaseContext;
import com.xxx.takeout.common.R;
import com.xxx.takeout.entity.AddressBook;
import com.xxx.takeout.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 * RestController 是 Spring Framework 中的一个注解，它是 @Controller 和 @ResponseBody 的组合注解，
 * 主要用于简化 RESTful Web 服务的开发。其主要作用是标记一个类为控制器，并将该类中的每个方法的返回值都直接作为 HTTP 响应体返回。
 * 详细解释
 *
*     标识控制器:
*         RestController 标记一个类为 Spring MVC 控制器（Controller），使其能够处理 HTTP 请求。它与 @Controller 的作用类似，都是用来定义控制器类。
*
*     自动序列化为 JSON 或 XML:
*         与 @Controller 不同，@RestController 会自动将控制器方法的返回值序列化为 JSON 或 XML，并将其直接写入到 HTTP 响应体中，而不是返回一个视图名。
*         内部相当于在每个方法上隐式地添加了 @ResponseBody 注解，因此你不需要在每个方法上手动添加 @ResponseBody。
*
*     典型应用场景:
*         RestController 主要用于构建 RESTful Web 服务，其中每个方法都处理特定的 HTTP 请求（如 GET、POST、PUT、DELETE），并直接返回数据给客户端。
*         例如，当你希望控制器的每个方法返回 JSON 数据，而不是返回一个视图时，你可以使用 @RestController。
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址簿条目
     * 如果保存成功，返回 R.success(addressBook)，即包含新增 AddressBook 对象的成功响应。
     * 返回值类型为 R<AddressBook>，封装了新增的地址簿条目数据。
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     * 设置某个地址为默认地址。首先将该用户的所有地址的 isDefault 字段设置为 0，然后将指定地址的 isDefault 字段设置为 1，表示其为默认地址。
     * 返回值:
     *
     *     返回 R.success(addressBook)，即包含设置为默认地址的 AddressBook 对象的成功响应。
     *     返回值类型为 R<AddressBook>，封装了被设置为默认的地址簿条目数据。
     */
    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();

        // 构造 WHERE 条件，表示查找 user_id 等于当前用户 ID 的所有地址簿条目。
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());

        // 构造 SET 子句，将所有符合条件的地址簿条目的 isDefault 字段设置为 0，即取消默认状态。
        wrapper.set(AddressBook::getIsDefault, 0);

        // UPDATE address_book SET is_default = 0 WHERE user_id = ?
        addressBookService.update(wrapper);

        // 将传入的 addressBook 对象的 isDefault 字段设置为 1，即表示该地址为默认地址。
        addressBook.setIsDefault(1);

        // 执行更新操作，基于 addressBook 对象的 id 进行更新。生成并执行如下 SQL：
        // UPDATE address_book SET is_default = 1 WHERE id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 根据id查询地址
     * 功能: 根据地址簿条目的 ID 查询该条目的详细信息。
     * 返回值:
     *
     *     如果查询成功，返回 R.success(addressBook)，即包含查询到的 AddressBook 对象的成功响应。
     *     如果查询失败（地址簿条目不存在），返回 R.error("没有找到该对象")，即失败响应。
     *     返回值类型为 R<AddressBook>，封装了查询到的地址簿条目数据（或错误信息）。
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     * 功能: 查询当前用户的默认地址。
     * 返回值:
     *
     *     如果查询成功，返回 R.success(addressBook)，即包含查询到的默认 AddressBook 对象的成功响应。
     *     如果查询失败（没有默认地址），返回 R.error("没有找到该对象")，即失败响应。
     *     返回值类型为 R<AddressBook>，封装了查询到的默认地址数据（或错误信息）。
     */
    @GetMapping("default")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook) {
            return R.error("没有找到该对象");
        } else {
            return R.success(addressBook);
        }
    }

    /**
     * 查询指定用户的全部地址
     * 功能: 查询当前用户的所有地址簿条目，并按更新时间降序排列。
     * 返回值:
     *
     *     返回 R.success(addressBookService.list(queryWrapper))，即包含查询到的 AddressBook 列表的成功响应。
     *     返回值类型为 R<List<AddressBook>>，封装了当前用户的所有地址簿条目数据。
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        //SQL:select * from address_book where user_id = ? order by update_time desc
        return R.success(addressBookService.list(queryWrapper));
    }
}
