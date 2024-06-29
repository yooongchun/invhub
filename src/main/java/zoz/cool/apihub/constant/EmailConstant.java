package zoz.cool.apihub.constant;

public class EmailConstant {
    public static final String TEMPLATE_FILENAME = "verifyCodeEmailTemplate";
    public static final String SUBJECT = "【发票管家】验证码";
    public static final String TEMPLATE_PARAMS = "{\"code\":\"%s\"}";
    public static final String VERIFY_CODE_TEMPLATE = "<div>【发票管家】欢迎注册<a href=\"https://zoz.cool\">发票管家<a/>！你的验证码是 <h3>%s</h3>，有效期为<h3>%d分钟</h3>，请勿泄露给他人。如果这不是你在操作，请忽略！</div>";

}

