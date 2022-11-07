package com.zs.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zs.reggie.common.R;
import com.zs.reggie.pojo.AddressBook;
import com.zs.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询用户所有地址
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook, HttpServletRequest request){
        Long userId = (Long) request.getSession().getAttribute("user");
        addressBook.setUserId(userId);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId()!=null,AddressBook::getUserId,userId);
        queryWrapper.orderByAsc(AddressBook::getIsDefault);
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 添加地址
     * @param addressBook
     * @param request
     * @return
     */
    @PostMapping
    public R<String> addAddressBook(@RequestBody  AddressBook addressBook,HttpServletRequest request){
        Long userId = (Long) request.getSession().getAttribute("user");
        if(userId!=null){
            addressBook.setUserId(userId);
            addressBookService.save(addressBook);
        }
        return R.success("添加成功");
    }

    /**
     * 设为默认地址
     * @param addressBook
     * @param request
     * @return
     */
    @PutMapping("/default")
    public R<String> putDefaultAddress(@RequestBody AddressBook addressBook,HttpServletRequest request){
        Long userId = (Long) request.getSession().getAttribute("user");
        Long addressId = addressBook.getId();
        //先把所有的地址默认地址设为0
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,AddressBook::getUserId,userId);
        List<AddressBook> list = addressBookService.list(queryWrapper);
        list.forEach(item->{
            item.setIsDefault(0);
        });
        addressBookService.updateBatchById(list);
        //再将本次地址设为默认地址
        AddressBook thisAddress = addressBookService.getById(addressBook.getId());
        thisAddress.setIsDefault(1);
        addressBookService.updateById(thisAddress);
        return R.success("修改成功");
    }

    /**
     * 根据id得到用户地址信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> getAddressById(@PathVariable("id") Long id){
        if(id!=null){
            AddressBook address = addressBookService.getById(id);
            return R.success(address);
        }

        return R.error("请求失败");
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> updateAddress(@RequestBody AddressBook addressBook){
        //log.info("addressBook={}",addressBook.toString());
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    /**
     * 删除地址
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteAddress(Long ids){
        //log.info("ids={}",ids);
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }

    /**
     * 查询该用户默认地址
     * @param request
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefaultAddress(HttpServletRequest request){
        Long userId = (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,AddressBook::getUserId,userId);
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook address = addressBookService.getOne(queryWrapper);
        if(address!=null){
            return R.success(address);
        }else{
            return R.error("没有查询到该对象");
        }

    }
}


