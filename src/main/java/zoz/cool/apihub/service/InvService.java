package zoz.cool.apihub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

@Service
@Slf4j
public class InvService {
    /**
     * 对象转
     * 将大写开头的字段名转换为小写开头的字段名，并将sourceObject的字段值赋值给targetObject的同名字段
     */
    public void convertAndCopyFields(Object sourceObject, Object targetObject) {
        Field[] sourceFields = sourceObject.getClass().getDeclaredFields();
        Class<?> targetClass = targetObject.getClass();

        for (Field sourceField : sourceFields) {
            try {
                sourceField.setAccessible(true);
                String sourceFieldName = sourceField.getName();
                String targetFieldName = Character.toLowerCase(sourceFieldName.charAt(0)) + sourceFieldName.substring(1);
                Field targetField;
                try {
                    targetField = targetClass.getDeclaredField(targetFieldName);
                } catch (NoSuchFieldException e) {
                    // Field does not exist in target object, skip to the next field
                    continue;
                }
                targetField.setAccessible(true);
                Object value = sourceField.get(sourceObject);
                targetField.set(targetObject, value);
            } catch (IllegalAccessException e) {
                // Handle exception or log as needed
                log.warn("字段{}赋值失败", sourceField.getName(), e);
            }
        }
    }
}
