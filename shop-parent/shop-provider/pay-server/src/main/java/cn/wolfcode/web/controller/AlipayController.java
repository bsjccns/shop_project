package cn.wolfcode.web.controller;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.config.AlipayProperties;
import cn.wolfcode.domain.PaySuccessVo;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import cn.wolfcode.feign.SeckillFeign;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/aliPay")
public class AlipayController {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayProperties alipayProperties;
    @Autowired
    private SeckillFeign seckillFeign;

//    @PostMapping("/refund")
//    public Result<Boolean> refund(@RequestBody RefundVo vo) {
//        // 创建退款请求对象
//        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
//
//        // 构建请求参数
//        JSONObject bizContent = new JSONObject();
//        bizContent.put("out_trade_no", vo.getOutTradeNo());
//        bizContent.put("refund_amount", vo.getRefundAmount());
//        bizContent.put("refund_reason", vo.getRefundReason());
//        request.setBizContent(bizContent.toString());
//
//        try {
//            // 执行退款操作
//            AlipayTradeRefundResponse response = alipayClient.execute(request);
//            log.info("[支付宝接口] 获取退款响应结果：{}", JSON.toJSONString(response));
//
//            if (!"10000".equals(response.getCode())) {
//                throw new BusinessException(new CodeMsg(500500, response.getMsg()));
//            }
//
//            // 判断是否退款成功
//            if (!"Y".equals(response.getFundChange())) {
//                // 如果未收到 fundChange=Y，也不代表退款一定失败，可以再次调用退款查询接口查询是否真正退款成功
//                AlipayTradeFastpayRefundQueryRequest refundQueryRequest = new AlipayTradeFastpayRefundQueryRequest();
//
//                bizContent = new JSONObject();
//                bizContent.put("out_trade_no", vo.getOutTradeNo());
//                bizContent.put("out_request_no", vo.getOutTradeNo());
//
//                refundQueryRequest.setBizContent(bizContent.toString());
//                AlipayTradeFastpayRefundQueryResponse refundQueryResponse = alipayClient.execute(refundQueryRequest);
//                log.info("[支付宝接口] 查询退款结果：{}", JSON.toJSONString(refundQueryResponse));
//
//                if (!"REFUND_SUCCESS".equals(refundQueryResponse.getRefundStatus())) {
//                    // 查询到的也是退款失败
//                    return Result.success(false);
//                }
//            }
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        return Result.success(true);
//    }
//
//    @PostMapping("/doPay")
//    public Result<String> doPay(@RequestBody PayVo vo) {
//        try {
//            AlipayTradePagePayRequest request = this.buildRequest(vo);
//            //请求
//            String body = alipayClient.pageExecute(request).getBody();
//            // 请求到支付宝以后，预订单创建成功，会返回一个 HTML 片段，实现跳转到支付宝页面
//            System.out.println(body);
//            return Result.success(body);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Result.error(new CodeMsg(500401, e.getMessage()));
//        }
//    }
//
//    @PostMapping("/checkRSASignature")
//    public Result<Boolean> checkRSASignature(@RequestBody Map<String, String> params) throws AlipayApiException {
//        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType());
//        return Result.success(signVerified);
//    }
//
//    private AlipayTradePagePayRequest buildRequest(PayVo vo) {
//        // 设置请求参数
//        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
//        alipayRequest.setReturnUrl(vo.getReturnUrl());
//        alipayRequest.setNotifyUrl(vo.getNotifyUrl());
//
//        JSONObject json = new JSONObject();
//        json.put("out_trade_no", vo.getOutTradeNo());
//        json.put("total_amount", vo.getTotalAmount());
//        json.put("subject", vo.getSubject());
//        json.put("body", vo.getBody());
//        json.put("product_code", "FAST_INSTANT_TRADE_PAY");
//
//        alipayRequest.setBizContent(json.toJSONString());
//        return alipayRequest;
//    }

    /**
     * 向支付宝发起退款请求
     * @param vo
     * @return
     */

    @PostMapping("/refund")
    Result<Boolean> refund(@RequestBody RefundVo vo){
        AlipayTradeRefundRequest alipay_request = new AlipayTradeRefundRequest();

        AlipayTradeRefundModel model=new AlipayTradeRefundModel();
        model.setOutTradeNo(vo.getOutTradeNo()); //

        model.setRefundAmount(vo.getRefundAmount());
        model.setRefundReason(vo.getRefundReason());

        alipay_request.setBizModel(model);


        try {
            AlipayTradeRefundResponse alipay_response=alipayClient.execute(alipay_request);
            if (alipay_response.isSuccess()){
                if ("Y".equalsIgnoreCase(alipay_response.getFundChange())){
                    //退款成功
                    return Result.success(true);
                }else {
                    //发起退款查询接口的时间不能离退款请求时间太短，建议之间间隔10秒以上。
                    // 退款没有成功 请求退款查询接口
                    AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
                    AlipayTradeFastpayRefundQueryModel model1=new AlipayTradeFastpayRefundQueryModel();
                    model1.setOutTradeNo(vo.getOutTradeNo());
                    model1.setOutRequestNo(vo.getOutTradeNo()); //如果在退款请求时未传入，则该值为创建交易时的商户订单号。
                    request.setBizModel(model1);
                    AlipayTradeFastpayRefundQueryResponse alipay_response1=alipayClient.execute(request);
                    if (alipay_response1.getCode().equals("10000")&&alipay_response1.getRefundStatus().equalsIgnoreCase("REFUND_SUCCESS")){
                        // 退款成功
                        return Result.success(true);
                    }
                }
            }else {
                log.info("退款请求失败:{}",alipay_response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return Result.success(false);
        }
        return  Result.success(false);
    }

    /**
     * 同步回调 用于跳转到商家订单页面
     * @param request
     * @return
     */

    @GetMapping("/return_url")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response){
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values =  requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        //验签
        try {

            log.info("异步回调：交易参数{}",params);
            boolean verify_result = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), "RSA2");
            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//

            if(verify_result) {
                // 订单号
                String out_trade_no = request.getParameter("out_trade_no");
                response.sendRedirect("http://localhost/order_detail.html?orderNo="+out_trade_no);
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 支付宝异步通知接口
     * @param request
     * @return
     */
        @PostMapping("/notify_url")
        public String notifyUrl(HttpServletRequest request){
            //获取支付宝POST过来反馈信息
            Map<String,String> params = new HashMap<String,String>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values =  requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
                params.put(name, valueStr);
            }
            //验签
            try {

                log.info("异步回调：交易参数{}",params);
                boolean verify_result = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), "RSA2");
                //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//

                if(verify_result) {
                    // 订单号
                    String out_trade_no = request.getParameter("out_trade_no");
                    //支付宝交易号

                    String trade_no = request.getParameter("trade_no");

                    //交易状态
                    String trade_status = request.getParameter("trade_status");
                    //交易金额
                    String total_amount = request.getParameter("total_amount");

                    if (trade_status.equals("TRADE_FINISHED")) {
                        log.info("异步回调：交易完成");
                    } else if (trade_status.equals("TRADE_SUCCESS")) {
                        log.info("异步回调：交易成功 :{}", out_trade_no);
                        PaySuccessVo paySuccessVo = new PaySuccessVo(out_trade_no, trade_no, total_amount);
                        Result<String> result = seckillFeign.updateTrain(paySuccessVo);
                        if (result.hasError()) {
                            throw new BusinessException(new CodeMsg(500, "更新订单状态失败"));
                        }
                    }
                    return "success";
                }

            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            return "fail";
        }


    /**
     * 预支付接口
     * @param vo
     * @return  返回一个 HTML 片段，实现跳转到支付宝页面
     */
  @PostMapping("/doPay")
  public  Result<String> doPay(@RequestBody PayVo vo){
        // 构造请求参数以调用接口
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();

        // 设置商户订单号
        model.setOutTradeNo(vo.getOutTradeNo());

        // 设置订单总金额
        model.setTotalAmount(vo.getTotalAmount());

        // 设置订单标题
        model.setSubject(vo.getSubject());

        // 设置产品码
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 设置异步通知地址
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        // 设置同步地址
        request.setReturnUrl(alipayProperties.getReturnUrl());

        request.setBizModel(model);
        // 第三方代调用模式下请设置app_auth_token
        // request.putOtherTextParam("app_auth_token", "<-- 请填写应用授权令牌 -->");

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
            if (response.isSuccess()){
                // 请求到支付宝以后，预订单创建成功，会返回一个 HTML 片段，实现跳转到支付宝页面
                log.info("支付宝接口调用成功 {}", response.getBody());
                return Result.success(response.getBody());
            }else{
                log.error("调用失败 {}", response.getMsg());
                return Result.error(response.getMsg());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }



}
