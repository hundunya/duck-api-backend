package cn.hdy.api.project.controller;

import cn.hdy.sdk.client.project.model.User;
import org.springframework.web.bind.annotation.*;

/**
 * @author 混沌鸭
 **/
@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping("/get")
    public String getNameUsingGet(String name){
        return "GET: 你的名称是："+name;
    }

    @GetMapping("/post/name")
    public String getUsernameUsingGet(@RequestParam String name){
        return "POST: 你的名称是："+name;
    }

    @PostMapping("/post/name")
    public String getNameUsingPost(@RequestParam String name){
        return "POST: 你的名称是："+name;
    }

    @PostMapping("/post/username")
    public String getUsernameUsingPost(@RequestBody User user){
        return "你的用户名是："+user.getUsername();
    }
}
