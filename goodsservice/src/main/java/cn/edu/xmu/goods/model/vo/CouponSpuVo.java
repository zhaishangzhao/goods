package cn.edu.xmu.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import jdk.vm.ci.meta.Local;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CouponSpuVo {

    private Long id;

    private Long activityId;

    @ApiModelProperty("参与活动的商品sku")
    private List<SimpleSkuVo> simpleSkuVo;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

}