package cn.edu.xmu.flashsale.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.edu.xmu.flashsale.model.vo.*;
import cn.edu.xmu.flashsale.model.bo.*;
import cn.edu.xmu.flashsale.service.FlashSaleService;
import org.slf4j.Logger;
import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import io.swagger.annotations.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.apache.dubbo.config.annotation.DubboReference;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

import cn.edu.xmu.ininterface.service.model.vo.*;
import cn.edu.xmu.ininterface.service.*;
import reactor.core.publisher.Flux;

/**
 * 权限控制器
 *
 * @author zhai
 */
@Api(value = "秒杀服务", tags = "flashsale")
@RestController
@RequestMapping(value = "/flashsale", produces = "application/json;charset=UTF-8")
public class FlashSaleController {

    private static final Logger logger = LoggerFactory.getLogger(FlashSaleController.class);

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private HttpServletResponse httpServletResponse;

    @Autowired
    @DubboReference(version = "0.0.1", check = false)
    private Ingoodservice goodservice;

    /**
     * 查询某一时段秒杀活动详情
     *
     * @param id
     * @author Abin
     */
    @ApiOperation(value = "查询某一时段秒杀活动详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, dataType = "Long", paramType = "path", value = "时间段id"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作的资源id不存在")
    })
    @GetMapping("/timesegments/{id}/flashsales")
    public Flux<FlashSaleItemRetVo> queryTopicsByTime(@PathVariable Long id) {
        return flashSaleService.getFlashSale(id).map(x -> (FlashSaleItemRetVo) x.createVo());
    }

    /**
     * 获取当前时段秒杀列表
     *
     * @param id
     * @return
     * @author Abin
     */
    @ApiOperation(value = "获取当前时段秒杀列表")
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("/flashsales/current")
    public Object getCurrentFlash(Long id) {
        LocalDateTime localDateTime = LocalDateTime.now();
        //id调用其他模块获取
        return flashSaleService.getFlashSale(id).map(x -> (FlashSaleItemRetVo) x.createVo());
    }

    /**
     * 管理员修改秒杀活动
     *
     * @param id
     * @param flashSaleInputVo
     * @return
     * @author zhai
     */
    @ApiOperation(value = "管理员修改秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "did", required = true, dataType = "Integer", paramType = "path", value = "店铺id"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path", value = "秒杀id"),
            @ApiImplicitParam(name = "flashSaleInputVo", value = "可修改的秒杀活动信息", required = true, dataType = "FlashSaleInputVo", paramType = "body"),

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 500, message = "服务器内部错误"),
            @ApiResponse(code = 503, message = "秒杀日期不能为空"),
            @ApiResponse(code = 505, message = "操作的资源id不是自己的对象"),

    })
    @Audit
    @PutMapping("/shops/{did}/flashsales/{id}")
    public Object updateflashsale(@PathVariable Long id, @Validated @RequestBody FlashSaleInputVo flashSaleInputVo, BindingResult bindingResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateflashsale: id = " + id);
        }
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (returnObject != null) {
            logger.info("incorrect data received while updateflashsale flashSaleID = " + id);
            return returnObject;
        }
        ReturnObject returnObj = flashSaleService.updateFlashSale(id, flashSaleInputVo);
        return Common.decorateReturnObject(returnObj);
    }

    /**
     * 平台管理员删除某个时段秒杀
     *
     * @param id
     * @return
     * @author zhai
     */
    @ApiOperation(value = "平台管理员删除某个时段秒杀")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "did", required = true, dataType = "Integer", paramType = "path", value = "店铺id"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path", value = "秒杀id"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 500, message = "服务器内部错误"),
            @ApiResponse(code = 504, message = "操作的资源id不存在"),
            @ApiResponse(code = 505, message = "操作的资源id不是自己的对象"),
    })
    @Audit
    @DeleteMapping("/shops/{did}/flashsales/{id}")
    public Object deleteflashsale(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteFlashSale: id = " + id);
        }
        ReturnObject returnObj = flashSaleService.deleteFlashSale(id);
        return Common.decorateReturnObject(returnObj);

    }

    @ApiOperation(value = "管理员上线秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "did", required = true, dataType = "Integer", paramType = "path", value = "店铺id"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path", value = "秒杀id"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("/shops/{did}/flashsales/{id}/onshelves")
    public Object onShelvesflashsale(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("onShelvesFlashSale: id = " + id);
        }
        ReturnObject returnObj = flashSaleService.onshelvesFlashSale(id);
        return Common.decorateReturnObject(returnObj);
    }

    @ApiOperation(value = "管理员下线秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "did", required = true, dataType = "Integer", paramType = "path", value = "店铺id"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path", value = "秒杀id"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("/shops/{did}/flashsales/{id}/offshelves")
    public Object offShelvesflashsale(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("offShelvesFlashSale: id = " + id);
        }
        ReturnObject returnObj = flashSaleService.offshelvesFlashSale(id);
        return Common.decorateReturnObject(returnObj);

    }


    /**
     * @param id
     * @param skuInputVo
     * @param bindingResult
     * @return
     */
    @ApiOperation(value = "平台管理员向秒杀活动添加商品SKU")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "did", required = true, dataType = "Integer", paramType = "path", value = "店铺id"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path", value = "秒杀活动id"),
            @ApiImplicitParam(name = "skuInputVo", value = "向秒杀活动添加的SKU信息", required = true, dataType = "SkuInputVo", paramType = "body"),

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("/shops/{did}/flashsales/{id}/flashitems")
    public Object addSkuOfTopic(@PathVariable Long id, @Validated @RequestBody SkuInputVo skuInputVo, BindingResult bindingResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("addSKUofTopic id = " + id);
        }
        Object ret = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (ret != null) {
            logger.info("incorrect data received while addSKUofTopic flashSaleID = " + id);
            return ret;
        }
        ReturnObject returnObject = null;
        SkuToFlashSaleVo skuToFlashSaleVo = goodservice.flashFindSku(skuInputVo.getSkuId());
        if (skuToFlashSaleVo == null) {
            logger.info("该商品不存在");
            returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else {
            FlashSaleItem flashSaleItem = flashSaleService.addFlashSaleItem(id, skuInputVo);
            if (flashSaleItem == null) {
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST, String.format("商品添加失败"));
            } else {
                FlashSaleOutputVo flashSaleOutputVo = new FlashSaleOutputVo(flashSaleItem, skuToFlashSaleVo);
                returnObject = new ReturnObject<>(flashSaleOutputVo);
            }
        }
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 平台管理员在秒杀活动删除商品SKU
     *
     * @param id
     * @param fid
     * @return
     * @author zhai
     */
    @ApiOperation(value = "平台管理员在秒杀活动删除商品SKU")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "did", required = true, dataType = "Integer", paramType = "path", value = "店铺id"),
            @ApiImplicitParam(name = "fid", required = true, dataType = "Integer", paramType = "path", value = "秒杀活动id"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path", value = "秒杀活动项Id")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("/shops/{did}/flashsales/{fid}/flashitems/{id}")
    public Object deleteFlashSaleSku(@PathVariable Long id, @PathVariable Long fid) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteFlashSaleSku: id = " + id);
        }
        ReturnObject returnObj = flashSaleService.deleteFlashSaleSku(fid, id);
        return Common.decorateReturnObject(returnObj);
    }

    /**
     * 平台管理员在某个时间段下新建秒杀
     *
     * @param did
     * @param id
     * @param flashSaleInputVo
     * @return
     */
    @ApiOperation(value = "平台管理员在某个时间段下新建秒杀")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "did", required = true, dataType = "Integer", paramType = "path", value = "店铺id"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path", value = "秒杀时间段"),
            @ApiImplicitParam(name = "flashInputVo", required = true, dataType = "FlashInputVo", paramType = "body", value = "添加的秒杀信息")
    })
    @Audit
    @PostMapping("/shops/{did}/timesegments/{id}/flashsales")
    public Object createFlash(@PathVariable Long did, @PathVariable Long id, @Validated @RequestBody FlashSaleInputVo flashSaleInputVo) {
        if (logger.isDebugEnabled()) {
            logger.debug("createFlashSale : id = " + id);
        }
        if (did == 0) {
            ReturnObject returnObject = flashSaleService.createFlash(id, flashSaleInputVo);
            return Common.decorateReturnObject(returnObject);
        } else {
            return new ReturnObject<>(ResponseCode.AUTH_NOT_ALLOW);
        }
    }
}
