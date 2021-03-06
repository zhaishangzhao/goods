package cn.edu.xmu.goods.Controller;

import cn.edu.xmu.goods.GoodsServiceApplication;
import cn.edu.xmu.goods.controller.GoodsController;
import cn.edu.xmu.goods.mapper.*;
import cn.edu.xmu.goods.model.po.GoodsSkuPo;
import cn.edu.xmu.goods.model.po.ShopPo;
import cn.edu.xmu.goods.model.vo.ShopVo;
import cn.edu.xmu.ooad.util.JacksonUtil;
import cn.edu.xmu.ooad.util.JwtHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GoodsServiceApplication.class)
@AutoConfigureMockMvc
@Transactional
@Slf4j
public class GoodsControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    GoodsSpuPoMapper goodsSpuPoMapper;

    @Autowired
    GoodsSkuPoMapper goodsSkuPoMapper;

    @Autowired
    FloatPricePoMapper floatPricePoMapper;

    @Autowired
    BrandPoMapper brandPoMapper;

    @Autowired
    GoodsCategoryPoMapper goodsCategoryPoMapper;

    private static final Logger logger = LoggerFactory.getLogger(GoodsController.class);

    private final String creatTestToken(Long userId, Long departId, int expireTime) {
        String token = new JwtHelper().createToken(userId, departId, expireTime);
        logger.debug("token: " + token);
        return token;
    }

    @Test
    public void create() {
        logger.debug("************************" + creatTestToken(1L, 0L, 1000 * 60 * 60 * 24) + "*****************");
    }

    /**
     * 获得商品sku所有状态
     */
    @Test
    public void getAllSkuState() throws Exception {
        String responseString = this.mvc.perform(get("/goods/skus/states"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{ \"errno\": 0, \"data\": [ { \"name\": \"未上架\", \"code\": 0 }, { \"name\": \"上架\", \"code\": 4 }, { \"name\": \"已删除\", \"code\": 6 }], \"errmsg\": \"成功\" }";
        System.out.println(responseString);
        JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 获得商品sku的详细信息(登录)
     */
    @Test
    public void findGoodsSkuById1() throws Exception {
        String token1 = creatTestToken(1L, 0L, 100);
        String requireJson1 = "{\n" +
                "  \"activityPrice\": 222,\n" +
                "  \"beginTime\": \"2020-12-04 11:24:47\",\n" +
                "  \"endTime\": \"2020-12-22 11:24:49\",\n" +
                "  \"quantity\": 1\n" +
                "}";
        String responseString1 = this.mvc.perform(post("/goods/shops/0/skus/273/floatPrices")
                .header("authorization", token1)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson1))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString1);

        //登陆
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(get("/goods/skus/273")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    @Test
    public void findGoodsSkuById2() throws Exception {
        String responseString = this.mvc.perform(get("/goods/skus/273"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 获得一条商品spu的详细信息(不登录)
     */
    @Test
    public void findGoodsSpuById() throws Exception {
        String responseString = this.mvc.perform(get("/goods/spus/273"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 删除商品spu(未登录)
     *
     * @throws Exception
     */
    @Test
    public void deleteGoodsSpu1() throws Exception {
        String responseString = this.mvc.perform(delete("/goods/shops/1/spus/273")
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 删除商品spu(登录)
     *
     * @throws Exception
     */
    @Test
    public void deleteGoodsSpu2() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(delete("/goods/shops/1/spus/273")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 删除商品spu(spu不存在)
     *
     * @throws Exception
     */
    @Test
    public void deleteGoodsSpu3() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(delete("/goods/shops/1/spus/0")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 删除商品spu(操作的资源不是自己的对象)
     *
     * @throws Exception
     */
    @Test
    public void deleteGoodsSpu4() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(delete("/goods/shops/2/spus/273")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }


    //下架商品测试开始

    /**
     * 下架商品(token正确，单是无权限修改不属于自己店铺的东西)
     *
     * @throws Exception
     */
    @Test
    public void putOffGoodsSpuOnSales1() throws Exception {
        String token = creatTestToken(1L, 1L, 100);
        String responseString = this.mvc.perform(put("/goods/shops/1/skus/273/offshelves")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
    }

    /**
     * 下架商品(下架成功)
     *
     * @throws Exception
     */
    @Test
    public void putOffGoodsSpuOnSales2() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(put("/goods/shops/0/skus/273/offshelves")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
    }

    /**
     * 下架商品(token不对)
     *
     * @throws Exception
     */
    @Test
    public void putOffGoodsSpuOnSales3() throws Exception {
        String token = creatTestToken(1L, 1L, 100);
        String responseString = this.mvc.perform(put("/goods/shops/0/skus/273/offshelves")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
    }

    /**
     * 下架商品(平台管理员修改，但是对应店铺id不对)
     *
     * @throws Exception
     */
    @Test
    public void putOffGoodsSpuOnSales4() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(put("/goods/shops/0/skus/273/offshelves")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
    }
    //下架商品测试结束

    /**
     * 下架商品之后重新上架
     *
     * @throws Exception
     */
    @Test
    public void putGoodsSpuOnSales() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(put("/goods/shops/0/skus/273/offshelves")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());

        responseString = this.mvc.perform(put("/goods/shops/0/skus/273/onshelves")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
        //.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 店家或管理员删除sku
     *
     * @throws Exception
     */
    @Test
    public void deleteGoodsSku1() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
        String responseString = this.mvc.perform(delete("/goods/shops/1/skus/273")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 店家或管理员删除sku(sku不存在)
     *
     * @throws Exception
     */
    @Test
    public void deleteGoodsSku2() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
        String responseString = this.mvc.perform(delete("/goods/shops/1/skus/0")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getState());
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 获得所有品牌
     */
    @Test
    public void getAllBrand() throws Exception {
        String responseString = this.mvc.perform(get("/goods/brands?page=2&pageSize=2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errormessage\":成功,\"data\":{\"total\":18,\"pages\":2,\"pageSize\":10,\"page\":1,\"list\":[{\"id\":2,\"name\":\"查看任意用户信息\",\"imageUrl\":\"123\",\"detail\":0,\"gmtCreate\":\"2020-11-01T09:52:20\",\"gmtModified\":\"2020-11-02T21:51:45\"}";
        System.out.println(responseString);
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 修改商品sku信息
     *
     * @throws Exception
     */
    @Test
    public void changeSkuInfoById1() throws Exception {
        GoodsSkuPo goodsSkuPo = goodsSkuPoMapper.selectByPrimaryKey(273L);
        System.out.println(goodsSkuPo.getInventory());
        String requireJson = "{\n" +
                "  \"name\":\"123\",\n" +
                "  \"originalPrice\":\"123\",\n" +
                "  \"configuration\": \"123\",\n" +
                "  \"weight\":\"123\",\n" +
                "  \"inventory\":\"123\",\n" +
                "  \"detail\":\"123\"\n" +
                "}";
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(put("/goods/shops/0/skus/273")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        goodsSkuPo = goodsSkuPoMapper.selectByPrimaryKey(273L);
        System.out.println(goodsSkuPo.getInventory());
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 修改商品sku信息
     *
     * @throws Exception
     */
    @Test
    public void changeSkuInfoById2() throws Exception {
        GoodsSkuPo goodsSkuPo = goodsSkuPoMapper.selectByPrimaryKey(273L);
        System.out.println(goodsSkuPo.getInventory());
        String requireJson = "{\n" +
                "  \"name\":\"\",\n" +
                "  \"originalPrice\":\"123\",\n" +
                "  \"configuration\": \"123\",\n" +
                "  \"weight\":\"123\",\n" +
                "  \"inventory\":\"123\",\n" +
                "  \"detail\":\"123\"\n" +
                "}";
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(put("/goods/shops/0/skus/273")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        goodsSkuPo = goodsSkuPoMapper.selectByPrimaryKey(273L);
        System.out.println(goodsSkuPo.getInventory());
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 获得所有sku
     *
     * @throws Exception
     */
    @Test
    public void getSkuList1() throws Exception {
        String responseString = this.mvc.perform(get("/goods/skus?shopId=100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 获得所有sku
     *
     * @throws Exception
     */
    @Test
    public void getSkuList2() throws Exception {
        String responseString = this.mvc.perform(get("/goods/skus?pageSize=14"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 管理员失效商品价格浮动
     *
     * @throws Exception
     */
    @Test
    public void invalidFloatPrice1() throws Exception {
        String token = creatTestToken(9999L, 0L, 100);
        String responseString = this.mvc.perform(delete("/goods/shops/0/floatprice/828")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 管理员失效商品价格浮动
     *
     * @throws Exception
     */
    @Test
    public void invalidFloatPrice2() throws Exception {
        String token = creatTestToken(9999L, 0L, 100);
        String responseString = this.mvc.perform(delete("/goods/shops/1/floatprice/827")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * 修改品牌信息
     */
    @Test
    public void modifyBrand1() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getName());
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getDetail());
        String requireJson = "{\n" +
                "  \"name\":\"123\",\n" +
                "  \"detail\":\"123\"\n" +
                "}";
        String responseString = this.mvc.perform(put("/goods/shops/0/brands/71")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getName());
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getDetail());
    }

    /**
     * 修改品牌信息
     */
    @Test
    public void modifyBrand2() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getName());
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getDetail());
        String requireJson = "{\n" +
                "  \"name\":\"123\",\n" +
                "  \"detail\":\"123\"\n" +
                "}";
        String responseString = this.mvc.perform(put("/goods/shops/1/brands/71")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getName());
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getDetail());
    }

    /**
     * 管理员删除品牌
     *
     * @throws Exception
     */
    @Test
    public void deleteBrand1() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(delete("/goods/shops/0/brands/71")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        getAllBrand();
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 管理员删除品牌
     *
     * @throws Exception
     */
    @Test
    public void deleteBrand2() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(delete("/goods/shops/0/brands/70")
                .header("authorization", token))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        getAllBrand();
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 管理员新增商品类目
     *
     * @throws Exception
     */
    @Test
    public void addCategory1() throws Exception {
        String requireJson = "{\n  \"name\":\"家电\"}";
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(post("/goods/shops/0/categories/139/subcategories")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        // JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 管理员新增商品类目
     *
     * @throws Exception
     */
    @Test
    public void addCategory2() throws Exception {
        String requireJson = "{\n  \"name\":\"家电\"}";
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(post("/goods/shops/0/categories/119/subcategories")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        // JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 管理员修改商品类目信息
     *
     * @throws Exception
     */
    @Test
    public void modifyGoodsType() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        System.out.println(goodsCategoryPoMapper.selectByPrimaryKey(122L).getName());
        String requireJson = "{\n  \"name\":\"123\"}";
        String responseString = this.mvc.perform(put("/goods/shops/0/categories/122")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        System.out.println(goodsCategoryPoMapper.selectByPrimaryKey(122L).getName());
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 管理员删除商品类目信息
     *
     * @throws Exception
     */
    @Test
    public void delCategory() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(delete("/goods/shops/0/categories/121")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        System.out.println(goodsCategoryPoMapper.selectByPrimaryKey(122L));// 查询测试是否删除成功
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * spu添加品牌
     *
     * @throws Exception
     */
    @Test
    public void spuAddBrand() throws Exception {
        String token = creatTestToken(1111L, 0L, 100);
        System.out.println(goodsSpuPoMapper.selectByPrimaryKey(273L).getBrandId());
        String responseString = this.mvc.perform(post("/goods/shops/0/spus/273/brands/70")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(goodsSpuPoMapper.selectByPrimaryKey(273L).getBrandId());
    }

    /**
     * 商品分类信息
     *
     * @throws Exception
     */
    @Test
    public void queryType() throws Exception {
        String responseString = this.mvc.perform(get("/goods/categories/199/subcategories"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * sku上传照片
     */
    @Test
    public void uploadSkuImage() throws Exception {
        String token = creatTestToken(1111L, 0L, 100);
        File file = new File("." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "img" + File.separator + "timg.png");
        MockMultipartFile firstFile = new MockMultipartFile("img", "timg.png", "multipart/form-data", new FileInputStream(file));
        String responseString = mvc.perform(MockMvcRequestBuilders
                .multipart("/goods/shops/0/skus/273/uploadImg")
                .file(firstFile)
                .header("authorization", token)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(goodsSkuPoMapper.selectByPrimaryKey(273L).getImageUrl());
        System.out.println(responseString);
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * spu上传照片
     */
    @Test
    public void uploadSpuImage() throws Exception {
        String token = creatTestToken(1111L, 1L, 100);
        File file = new File("." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "img" + File.separator + "timg.png");
        MockMultipartFile firstFile = new MockMultipartFile("img", "timg.png", "multipart/form-data", new FileInputStream(file));
        String responseString = mvc.perform(MockMvcRequestBuilders
                .multipart("/goods/shops/1/spus/273/uploadImg")
                .file(firstFile)
                .header("authorization", token)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 品牌上传照片
     */
    @Test
    public void uploadBrandImage() throws Exception {
        String token = creatTestToken(1111L, 0L, 100);
        File file = new File("." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "img" + File.separator + "timg.png");
        MockMultipartFile firstFile = new MockMultipartFile("img", "timg.png", "multipart/form-data", new FileInputStream(file));
        String responseString = mvc.perform(MockMvcRequestBuilders
                .multipart("/goods/shops/1/brands/71/uploadImg")
                .file(firstFile)
                .header("authorization", token)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(brandPoMapper.selectByPrimaryKey(71L).getImageUrl());
        System.out.println(responseString);
    }

    /**
     * 商品加入分类
     *
     * @throws Exception
     */
    @Test
    public void spuAddCategories() throws Exception {
        String token = creatTestToken(1111L, 0L, 100);
        String responseString = this.mvc.perform(post("/goods/shops/0/spus/277/categories/126")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(goodsSpuPoMapper.selectByPrimaryKey(277L).getCategoryId());
    }

    /**
     * spu删除分类
     *
     * @throws Exception
     */
    @Test
    public void spuDeleteCategories() throws Exception {
        String token = creatTestToken(1111L, 0L, 100);
        System.out.println(goodsSpuPoMapper.selectByPrimaryKey(277L).getCategoryId());
        String responseString = this.mvc.perform(delete("/goods/shops/0/spus/277/categories/127")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(goodsSpuPoMapper.selectByPrimaryKey(277L).getCategoryId());
    }

    /**
     * spu删除品牌
     *
     * @throws Exception
     */
    @Test
    public void spuDeleteBrand() throws Exception {
        String token = creatTestToken(1111L, 0L, 100);
        System.out.println(goodsSpuPoMapper.selectByPrimaryKey(273L).getBrandId());
        String responseString = this.mvc.perform(delete("/goods/shops/0/spus/273/brands/71")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(goodsSpuPoMapper.selectByPrimaryKey(273L).getBrandId());
        System.out.println(responseString);
    }

    //管理员新增商品价格浮动测试 开始

    /**
     * 开始时间大于结束时间
     *
     * @throws Exception
     */
    @Test
    public void addFloatingPrice1() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String bodyValue = "{\n" +
                "  \"activityPrice\": 12,\n" +
                "  \"beginTime\": \"2022-12-15T22:55:00\",\n" +
                "  \"endTime\": \"2022-12-20T22:55:00\",\n" +
                "  \"quantity\": 10\n" +
                "}";
        String responseString = this.mvc.perform(post("/goods/shops/1/skus/8989/floatPrices")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(bodyValue))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 无开始时间
     *
     * @throws Exception
     */
    @Test
    public void addFloatingPrice2() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String requireJson = "{\n" +
                "  \"activityPrice\": 0,\n" +
                "  \"beginTime\": \"\",\n" +
                "  \"endTime\": \"2020-12-07 11:24:46\",\n" +
                "  \"quantity\": 0\n" +
                "}";
        String responseString = this.mvc.perform(post("/goods/shops/0/skus/273/floatPrices")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 无结束时间
     *
     * @throws Exception
     */
    @Test
    public void addFloatingPrice3() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String requireJson = "{\n" +
                "  \"activityPrice\": 0,\n" +
                "  \"beginTime\": \"2020-12-07 11:24:47\",\n" +
                "  \"endTime\": \"\",\n" +
                "  \"quantity\": 0\n" +
                "}";
        String responseString = this.mvc.perform(post("/goods/shops/0/skus/273/floatPrices")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 设置的库存大于总库存
     *
     * @throws Exception
     */
    @Test
    public void addFloatingPrice4() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String requireJson = "{\n" +
                "  \"activityPrice\": 0,\n" +
                "  \"beginTime\": \"2020-12-07 11:24:47\",\n" +
                "  \"endTime\": \"2020-12-07 11:24:49\",\n" +
                "  \"quantity\": 10\n" +
                "}";
        String responseString = this.mvc.perform(post("/goods/shops/0/skus/273/floatPrices")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 正常添加和时间冲突
     *
     * @throws Exception
     */
    @Test
    public void addFloatingPrice5() throws Exception {
        String token1 = creatTestToken(1L, 0L, 100);
        String requireJson1 = "{\n" +
                "  \"activityPrice\": 0,\n" +
                "  \"beginTime\": \"2020-12-07 11:24:47\",\n" +
                "  \"endTime\": \"2020-12-07 11:24:49\",\n" +
                "  \"quantity\": 1\n" +
                "}";
        String responseString1 = this.mvc.perform(post("/goods/shops/0/skus/273/floatPrices")
                .header("authorization", token1)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson1))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString1);

        String token2 = creatTestToken(1L, 0L, 100);
        String requireJson2 = "{\n" +
                "  \"activityPrice\": 0,\n" +
                "  \"beginTime\": \"2020-12-07 11:24:48\",\n" +
                "  \"endTime\": \"2020-12-07 11:24:49\",\n" +
                "  \"quantity\": 0\n" +
                "}";
        String responseString2 = this.mvc.perform(post("/goods/shops/0/skus/273/floatPrices")
                .header("authorization", token2)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson2))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString2);
    }
    //管理员新增商品价格浮动测试 结束

    /**
     * 管理员新增品牌
     *
     * @throws Exception
     */
    @Test
    public void addBrand() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String requireJson = "{\n" +
                "  \"name\":\"123\",\n" +
                "  \"detail\":\"123\"\n" +
                "}";
        System.out.println(requireJson);
        String responseString = this.mvc.perform(post("/goods/shops/0/brands")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    @Test
    public void addSpu() throws Exception {
        String token = creatTestToken(1L, 1L, 100);
        String requireJson = "{\n  \"name\":\"123\",\n  \"description\":\"123\",\n  \"specs\": \"123\"\n}";
        String responseString = this.mvc.perform(put("/goods/shops/1/spus")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 修改商品spu信息
     *
     * @throws Exception
     */
    @Test
    public void changeSpuInfoById() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String requireJson = "{\n  \"name\":\"123\",\n  \"description\":\"123\",\n  \"specs\": \"123\"\n}";
        String responseString = this.mvc.perform(put("/goods/shops/0/spus/273")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    /**
     * 管理员添加新的SKU到SPU中
     *
     * @throws Exception
     */
    @Test
    public void createSku() throws Exception {
        String bodyValue = "{\n" +
                "  \"sn\": \"string\",\n" +
                "  \"name\": \"测试商品\",\n" +
                "  \"originalPrice\": 100,\n" +
                "  \"configuration\": \"ddddd\",\n" +
                "  \"weight\": 10,\n" +
                "  \"imageUrl\": null,\n" +
                "  \"inventory\": 100,\n" +
                "  \"detail\": \"aaaaa\"\n" +
                "}";
        String responseString = this.mvc.perform(post("/goods/shops/0/spus/273/skus")
                .contentType("application/json;charset=UTF-8")
                .content(bodyValue))
                .andReturn().getResponse().getContentAsString();
        String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    @Autowired
    ShopPoMapper shopPoMapper;

    @Test
    public void insertShopTest() {
        String token = creatTestToken(1L, -1L, 100);

        ShopVo vo = new ShopVo();
        vo.setName("test");
        String shopJson = JacksonUtil.toJson(vo);
        String expectedResponse = "";
        String responseString = null;

        try {
            responseString = this.mvc.perform(post("/shop/shops").contentType("application/json;charset=UTF-8").content(shopJson).header("authorization", token))
                    .andExpect(content().contentType("application/json;charset=UTF-8"))
                    .andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //expectedResponse = "{\"errno\":0,\"data\":{\"id\":1,\"name\":\"test\",\"gmtModified\":null},\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        /*try {
            JSONAssert.assertEquals(expectedResponse, responseString, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(responseString);*/
    }

    @Test
    public void getAllShopState() throws Exception {
        String responseString = this.mvc.perform(get("/shop/shops/states"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    @Test
    public void updateShop() throws Exception {
        ShopVo vo = new ShopVo();
        vo.setName("test");
        String shopJson = JacksonUtil.toJson(vo);
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(
                put("/shop/shops/1")
                        .contentType("application/json;charset=UTF-8").content(shopJson).header("authorization", token)
        ).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(shopPoMapper.selectByPrimaryKey((long) 1).getName());
    }

    @Test
    public void shopOnShelves() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(
                put("/shop/shops/1/onshelves")
                        .contentType("application/json;charset=UTF-8")
        ).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    @Test
    public void shopOffShelves() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(
                put("/shop/shops/2/offshelves")
                        .contentType("application/json;charset=UTF-8").header("authorization", token)
        ).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    @Test
    public void deleteShop() throws Exception {
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(
                delete("/shop/shops/1")
                        .contentType("application/json;charset=UTF-8").header("authorization", token)
        ).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn().getResponse().getContentAsString();
        System.out.println(responseString);
    }

    /**
     * 管理员审核店铺
     */
    @Test
    public void auditShop() throws Exception {
        String requireJson = "{\n" +
                "  \"state\":\"2\"\n" +
                "}";
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(put("/shop/shops/0/newshops/2/audit")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        //String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        ShopPo shopPo = shopPoMapper.selectByPrimaryKey(2L);
        System.out.println(shopPo.getState());
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }

    @Test
    public void auditShopPass() throws Exception {
        String requireJson = "{\n" +
                "  \"state\":\"2\"\n" +
                "}";
        String token = creatTestToken(1L, 0L, 100);
        String responseString = this.mvc.perform(put("/shop/shops/0/newshops/1/audit")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(requireJson))
                .andReturn().getResponse().getContentAsString();
        //String expectedResponse = "{\"errno\":0,\"errmsg\":\"成功\"}";
        System.out.println(responseString);
        ShopPo shopPo = shopPoMapper.selectByPrimaryKey(1L);
        System.out.println(shopPo.getState());
        //JSONAssert.assertEquals(expectedResponse, responseString, true);
    }
}
