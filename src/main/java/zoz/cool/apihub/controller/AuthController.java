package zoz.cool.apihub.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册管理
 */
@Slf4j
@RestController
@ResponseBody
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "认证管理")
public class AuthController {

}
