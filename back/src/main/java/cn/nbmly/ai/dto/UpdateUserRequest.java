package cn.nbmly.ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UpdateUserRequest {

    @Size(max = 255, message = "昵称长度不能超过255个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @URL(message = "头像地址格式不正确")
    private String avatar;
}