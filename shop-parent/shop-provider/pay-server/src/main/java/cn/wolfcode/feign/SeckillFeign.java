package cn.wolfcode.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.PaySuccessVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("seckill-service")
public interface SeckillFeign {
    @PostMapping("/orderPay/success")
    Result<String> updateTrain(@RequestBody PaySuccessVo paySuccessVo);
}
