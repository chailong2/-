package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共子段的自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    //切点表达式
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){

    }

    /**
     * 前置通知，在通知中给公共子段赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共子段填充");
        //1.获取到当前被拦截的方法上的数据库操作类型
        /**
         *这段代码是用于获取连接点的签名信息。具体而言，它使用joinPoint.getSignature()方法来获取连接点的签名对象，并将其赋值给名为"signature"的变量。
         *通过这个签名对象，我们可以获取连接点的一些关键信息，如方法名、声明类型、参数等。这个代码片段通常用于在切面中获取连接点的详细信息，以便进行后续的处理或记录。
         */
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill=signature.getMethod().getAnnotation(AutoFill.class);
        OperationType value = autoFill.value();
        //2.获取到当前被拦截方法的参数—实体对象
        Object[] args = joinPoint.getArgs();
        if (args==null || args.length==0) {
            return;
        }
        Object arg = args[0];
        //3.准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentid= BaseContext.getCurrentId();
        //4.根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(value==OperationType.INSERT){
            //为四个公共子段赋值
            try {
                Method setCreateTimes = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser=arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                //通过反射为对象赋值
                setCreateTimes.invoke(arg,now);
                setCreateUser.invoke(arg,currentid);
                setUpdateTime.invoke(arg,now);
                setUpdateUser.invoke(arg,currentid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(value==OperationType.UPDATE){
            //为两个公共子段赋值
            try {
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser=arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                //通过反射为对象赋值
                setUpdateTime.invoke(arg,now);
                setUpdateUser.invoke(arg,currentid);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
