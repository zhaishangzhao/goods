package cn.edu.xmu.presale.dao;

import cn.edu.xmu.ininterface.service.InShopService;
import cn.edu.xmu.ininterface.service.Ingoodservice;
import cn.edu.xmu.ininterface.service.model.vo.ShopToAllVo;
import cn.edu.xmu.ininterface.service.model.vo.SkuToPresaleVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.presale.mapper.PresaleActivityPoMapper;
import cn.edu.xmu.presale.model.po.PresaleActivityPo;
import cn.edu.xmu.presale.model.po.PresaleActivityPoExample;
import cn.edu.xmu.presale.model.vo.PresaleActivityRetVo;
import cn.edu.xmu.presale.model.vo.PresaleActivityVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PresaleDao {

    @Autowired
    @DubboReference(version = "0.0.1", check = false)
    private Ingoodservice goodservice;

    @Autowired
    @DubboReference(version = "0.0.1", check = false)
    private InShopService inShopService;

    @Autowired
    private PresaleActivityPoMapper presaleActivityPoMapper;

    public boolean disableActivity(Long shopId) {
        try {
            PresaleActivityPoExample example = new PresaleActivityPoExample();
            PresaleActivityPoExample.Criteria criteria = example.createCriteria();
            criteria.andShopIdEqualTo(shopId);
            List<Byte> states = new ArrayList<>();
            states.add((byte) 0);
            states.add((byte) 1);
            criteria.andStateIn(states);
            List<PresaleActivityPo> pos = presaleActivityPoMapper.selectByExample(example);
            for (PresaleActivityPo po : pos) {
                po.setGmtModified(LocalDateTime.now());
                po.setState((byte) 2);
                presaleActivityPoMapper.updateByPrimaryKey(po);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ReturnObject inserPresaleActivity(Long shopId, Long id, PresaleActivityVo presaleActivityVo) {
        ReturnObject returnObject = null;
        ShopToAllVo shopToAllVo = inShopService.presaleFindShop(shopId);
        if (presaleActivityVo.getName() == null || presaleActivityVo.getName().length() == 0) {
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (presaleActivityVo.getBeginTime() == null) {
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (presaleActivityVo.getEndTime() == null) {
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (presaleActivityVo.getBeginTime().isBefore(LocalDateTime.now())) {
            System.out.println(3);
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (presaleActivityVo.getPayTime().isBefore(LocalDateTime.now())) {
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (!presaleActivityVo.getEndTime().isAfter(presaleActivityVo.getBeginTime())) {
            returnObject = new ReturnObject(ResponseCode.FIELD_NOTVALID);
            return returnObject;
        }
        if (presaleActivityVo.getRestPayPrice() < 0) {
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (presaleActivityVo.getEndTime().isBefore(LocalDateTime.now()) || presaleActivityVo.getBeginTime().isBefore(LocalDateTime.now())) {
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (shopToAllVo == null) {
            //店铺id不存在
            returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else {
            SkuToPresaleVo skuToPresaleVo = goodservice.presaleFindSku(id);
            if (skuToPresaleVo == null) {
                //sku不存在
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                //判断sku在不在shop里
                if (!goodservice.skuInShopOrNot(shopId, id)) {
                    //不在商店里
                    returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
                } else {
                    //查看sku是否参加预售
                    PresaleActivityPoExample example = new PresaleActivityPoExample();
                    PresaleActivityPoExample.Criteria criteria = example.createCriteria();
                    criteria.andGoodsSkuIdEqualTo(id);
                    List<PresaleActivityPo> pos = presaleActivityPoMapper.selectByExample(example);
                    if (!pos.isEmpty()) {
                        return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
                    }
                    PresaleActivityPo presaleActivityPo = presaleActivityVo.creatPo();
                    presaleActivityPo.setState((byte) 0);
                    presaleActivityPo.setGmtCreate(LocalDateTime.now());
                    presaleActivityPoMapper.insert(presaleActivityPo);
                    PresaleActivityRetVo vo = new PresaleActivityRetVo(presaleActivityPo);
                    vo.setId(presaleActivityPo.getId());
                    vo.setShop(shopToAllVo);
                    vo.setGoodsSku(skuToPresaleVo);
                    returnObject = new ReturnObject(vo);
                }
            }
        }
        return returnObject;
    }

    public ReturnObject<PageInfo<PresaleActivityRetVo>> findAllPresale(Long shopId, Integer timeLine, Long skuId, Integer page, Integer pageSize) {
        PresaleActivityPoExample presaleActivityPoExample = new PresaleActivityPoExample();
        PresaleActivityPoExample.Criteria criteria = presaleActivityPoExample.createCriteria();
        if (page != null && pageSize != null) {
            PageHelper.startPage(page, pageSize);
        }
        if (shopId != null) {
            //shopId不为空
            criteria.andShopIdEqualTo(shopId);
        }
        //时间：0 还未开始的， 1 明天开始的，2 正在进行中的，3 已经结束的
        if (timeLine != null) {
            if (timeLine == 0) {
                //timeLine等于0还没开始的活动
                criteria.andBeginTimeGreaterThan(LocalDateTime.now());
            } else {
                if (timeLine == 1) {
                    //timeLine等于1明天开始的活动
                    criteria.andBeginTimeGreaterThan(LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth() + 1, 0, 0, 0));
                    criteria.andEndTimeLessThan(LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth() + 2, 0, 0, 0));
                } else {
                    if (timeLine == 2) {
                        //timeLine等于2正在进行的活动
                        criteria.andBeginTimeLessThanOrEqualTo(LocalDateTime.now());
                        criteria.andEndTimeGreaterThan(LocalDateTime.now());
                    } else {
                        //timeLine等于3已经结束的活动
                        if (timeLine == 3) {
                            criteria.andEndTimeLessThan(LocalDateTime.now());
                        }
                    }

                }
            }
        }
        if (skuId != null) {
            //spuId不为空
            criteria.andGoodsSkuIdEqualTo(skuId);
        }
        List<Byte> states = new ArrayList<>();
        states.add((byte) 1);
        criteria.andStateIn(states);
        List<PresaleActivityPo> presaleActivityPos = presaleActivityPoMapper.selectByExample(presaleActivityPoExample);
        List<PresaleActivityRetVo> presaleActivityRetVos = new ArrayList<>(presaleActivityPos.size());
        for (PresaleActivityPo po : presaleActivityPos) {
            PresaleActivityRetVo vo = new PresaleActivityRetVo(po);
            vo.setShop(inShopService.presaleFindShop(po.getShopId()));
            vo.setGoodsSku(goodservice.presaleFindSku(po.getGoodsSkuId()));
            presaleActivityRetVos.add(vo);
        }
        PageInfo<PresaleActivityRetVo> voPageInfo = PageInfo.of(presaleActivityRetVos);
        voPageInfo.setPages(PageInfo.of(presaleActivityPos).getPages());
        voPageInfo.setPageNum(page);
        voPageInfo.setPageSize(pageSize);
        voPageInfo.setTotal(PageInfo.of(presaleActivityPos).getTotal());
        /**
         * 还缺少关于sku的信息
         */
        return new ReturnObject<>(voPageInfo);
    }

    public ReturnObject modifyPresale(Long shopId, Long id, PresaleActivityVo vo) {
        ReturnObject returnObject = null;
        if (vo.getName() == null || vo.getName().length() == 0) {
            System.out.println(1);
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (vo.getRestPayPrice() < 0) {
            System.out.println(2);
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (vo.getPayTime().isBefore(LocalDateTime.now())) {
            System.out.println(4);
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (vo.getBeginTime() == null) {
            System.out.println(5);
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (vo.getEndTime() == null) {
            System.out.println(6);
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        //判断开始时间是不是在结束时间之前
        System.out.println(vo.getEndTime());
        System.out.println(vo.getBeginTime());
        if (vo.getEndTime().isBefore(vo.getBeginTime())) {
            System.out.println(7);
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        if (vo.getPayTime().isBefore(vo.getBeginTime())) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (vo.getEndTime().isBefore(LocalDateTime.now())) {
            System.out.println(8);
            return new ReturnObject(ResponseCode.FIELD_NOTVALID);
        }
        PresaleActivityPo presaleActivityPo = presaleActivityPoMapper.selectByPrimaryKey(id);
        if (presaleActivityPo == null) {
            //预售活动的id不存在
            returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else {
            if(!presaleActivityPo.getBeginTime().equals(vo.getBeginTime())&&vo.getBeginTime().isBefore(LocalDateTime.now())){
                System.out.println(9);
                return new ReturnObject(ResponseCode.FIELD_NOTVALID);
            }

            if (!shopId.equals(presaleActivityPo.getShopId())) {
                //预售活动的shopId与路径上的不同
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
            } else {
                if (presaleActivityPo.getState() == 2 || presaleActivityPo.getState() == 1) {
                    //预售活动已经关闭
                    returnObject = new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
                } else {
                    //修改活动信息
                    presaleActivityPo.setName(vo.getName());
                    presaleActivityPo.setAdvancePayPrice(vo.getAdvancePayPrice());
                    presaleActivityPo.setRestPayPrice(vo.getRestPayPrice());
                    presaleActivityPo.setQuantity(vo.getQuantity());
                    presaleActivityPo.setBeginTime(vo.getBeginTime());
                    presaleActivityPo.setPayTime(vo.getPayTime());
                    presaleActivityPo.setEndTime(vo.getEndTime());
                    presaleActivityPo.setGmtModified(LocalDateTime.now());
                    int ret = presaleActivityPoMapper.updateByPrimaryKey(presaleActivityPo);
                    if (ret == 0) {
                        returnObject = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误"));
                    } else {
                        returnObject = new ReturnObject();
                    }
                }
            }
        }
        return returnObject;
    }

    public ReturnObject deletePresale(Long shopId, Long id) {
        ReturnObject returnObject = null;
        PresaleActivityPo presaleActivityPo = presaleActivityPoMapper.selectByPrimaryKey(id);
        if (presaleActivityPo == null) {
            returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else {
            if (!presaleActivityPo.getShopId().equals(shopId)) {
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
            } else {
                if (presaleActivityPo.getState().equals((byte) 1)) {
                    //处在上线拒绝操作
                    return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
                }
                if (presaleActivityPo.getState().equals((byte) 2)) {
                    return new ReturnObject();
                }
                presaleActivityPo.setState((byte) 2);
                presaleActivityPo.setGmtModified(LocalDateTime.now());
                int ret = presaleActivityPoMapper.updateByPrimaryKey(presaleActivityPo);
                if (ret == 0) {
                    returnObject = new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误"));
                } else {
                    returnObject = new ReturnObject();
                }
            }
        }
        return returnObject;
    }

    public ReturnObject queryPresaleofSKU(Long shopId, Long id, Integer state) {
        ReturnObject returnObject = null;
        PresaleActivityPoExample presaleActivityPoExample = new PresaleActivityPoExample();
        PresaleActivityPoExample.Criteria criteria = presaleActivityPoExample.createCriteria();
        criteria.andShopIdEqualTo(shopId);

        if (id != null) {
            criteria.andGoodsSkuIdEqualTo(id);
        }
        if (state != null) {
            if (state > 4    || state < 0) {
                return new ReturnObject(ResponseCode.FIELD_NOTVALID);
            }
            criteria.andStateEqualTo((byte) (int) state);
        } else {
            List<Byte> states = new ArrayList<>();
            states.add((byte) 0);
            states.add((byte) 1);
            criteria.andStateIn(states);
        }
        List<PresaleActivityPo> presaleActivityPos = presaleActivityPoMapper.selectByExample(presaleActivityPoExample);
        if (presaleActivityPos == null) {
            returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else {
            List<PresaleActivityRetVo> presaleActivityRetVos = new ArrayList<>(presaleActivityPos.size());
            for (PresaleActivityPo po : presaleActivityPos) {
                PresaleActivityRetVo vo = new PresaleActivityRetVo(po);
                vo.setShop(inShopService.presaleFindShop(po.getShopId()));
                vo.setGoodsSku(goodservice.presaleFindSku(po.getGoodsSkuId()));
                presaleActivityRetVos.add(vo);
            }
            return new ReturnObject(presaleActivityRetVos);
        }
        return returnObject;
    }

    public ReturnObject presaleOnShelves(Long shopId, Long id) {
        ReturnObject returnObject = null;
        PresaleActivityPo po = presaleActivityPoMapper.selectByPrimaryKey(id);
        if (po == null) {
            returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else {
            if (!po.getShopId().equals(shopId)) {
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
            } else {
                if (po.getState().equals((byte) 2)) {
                    returnObject = new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
                } else {
                    if (po.getState().equals((byte) 1)) {
                        return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
                    }
                    po.setState((byte) 1);
                    po.setGmtModified(LocalDateTime.now());
                    presaleActivityPoMapper.updateByPrimaryKey(po);
                    returnObject = new ReturnObject();
                }
            }
        }
        return returnObject;
    }

    public ReturnObject presaleOffShelves(Long shopId, Long id) {
        ReturnObject returnObject = null;
        PresaleActivityPo po = presaleActivityPoMapper.selectByPrimaryKey(id);
        if (po == null) {
            returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else {
            if (!po.getShopId().equals(shopId)) {
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
            } else {
                if (po.getState().equals((byte) 2)) {
                    returnObject = new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
                } else {
                    if (po.getState().equals((byte) 0)) {
                        return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
                    }
                    po.setState((byte) 0);
                    po.setGmtModified(LocalDateTime.now());
                    presaleActivityPoMapper.updateByPrimaryKey(po);
                    returnObject = new ReturnObject();
                }
            }
        }
        return returnObject;
    }
}
