package com.bruin.jmh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 参考 <a href="https://www.cnblogs.com/wupeixuan/p/13091381.html#335208470">...</a>
 * @author xiongwenwen
 * @since 2023/8/10 10:45 AM
 */
// 平均时间 & 整体吞吐量
@BenchmarkMode(value = {Mode.AverageTime, Mode.Throughput})
// 迭代5次，每次时间1s 代码预热 JVM根据JIT机制编译为机器码
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 5)
// 每个进程多少个线程测试
@Threads(4)
// fork 多少个进程测试
@Fork(1)
// 指定对象的作用范围
@State(value = Scope.Benchmark)
// 统计时间单位
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ObjectMapperTest {
    String json = "{\"address\":\"test_e92257aa41f3\",\"addressKeyword\":\"test_989e7af92a09\",\"appointable\":true,\"appointableEndAt\":\"test_9c0690b4c90b\",\"appointableStartAt\":\"test_3211f1622ab5\",\"appointmentType\":82,\"backgroundSwitch\":true,\"beforeMinutes\":96,\"boxFeeVO\":{\"boxFee\":40.16,\"boxFeeStatus\":47,\"go\":39,\"goShowFlag\":false,\"maxPrice\":94.63,\"selfShowFlag\":true,\"selfTake\":14,\"unIncludeTaxBoxFee\":52.28},\"busyMaxCupLimit\":37,\"busyMaxCupStatus\":false,\"busyState\":12,\"chatCodeImgUrl\":\"test_f885ff7992a3\",\"chatIds\":[\"test_68f92cb76cfa\"],\"city\":\"test_c526ebe3fbbe\",\"cityCode\":\"test_0d8393f2f6bf\",\"cityEn\":\"test_610001c6bc1f\",\"closeAt\":\"test_50d47389ac23\",\"closeMsgPic\":{\"contentType\":\"test_33d43f09804f\",\"createdAt\":\"2028-12-06 08:17:01\",\"height\":\"test_cfd242fe2a0e\",\"id\":79,\"imageType\":\"test_7f184b2ca286\",\"originName\":\"test_3aa22d2b92ed\",\"path\":\"test_24205a7d5ac8\",\"size\":\"test_c437b41bdea5\",\"updatedAt\":\"2022-12-14 19:21:47\",\"userId\":48,\"width\":\"test_161b4554996e\"},\"closeMsgPicUrl\":\"test_3556806fecb7\",\"closePic\":{\"contentType\":\"test_beeda78cd08e\",\"createdAt\":\"2027-06-03 14:20:28\",\"height\":\"test_5b01bc97b9e8\",\"id\":97,\"imageType\":\"test_08512aebe12b\",\"originName\":\"test_115f7401d4e6\",\"path\":\"test_e014593438b0\",\"size\":\"test_f1354fc485a6\",\"updatedAt\":\"2013-08-24 22:30:20\",\"userId\":9,\"width\":\"test_3af07d205a4c\"},\"closePicUrl\":\"test_2493aedfedd1\",\"closedContent\":\"test_737871778a20\",\"closedCovPicId\":56,\"closedLabel\":\"test_5e29964a8f66\",\"closedMsgPicId\":30,\"code\":\"test_1c439ef47ef8\",\"collectionFlag\":false,\"companyId\":5,\"contactName\":\"test_9d34d53001ef\",\"contactPhone\":\"test_2140ce8f1f9a\",\"country\":\"test_29d16a47fccf\",\"countryCode\":\"test_fa8fc22a08e1\",\"countryEn\":\"test_09b4b56b635d\",\"coverPicId\":40,\"createdAt\":\"2015-11-10 23:16:10\",\"cupLimit\":11,\"currency\":58,\"customRegionId\":77,\"dailyFenceSupport\":false,\"dateVersion\":7,\"daysOfWeek\":\"test_e50460b069a0\",\"deletedAt\":\"2033-04-22 01:44:55\",\"deliveryCloseAt\":\"test_c4e77a2698bb\",\"deliveryDistance\":68,\"deliveryFee\":87.59,\"deliveryOpenAt\":\"test_f8ad4e48d474\",\"deliveryReason\":\"test_d1bc29c391e3\",\"disableOrderType\":32,\"distance\":72,\"district\":\"test_2247932b4bce\",\"districtCode\":\"test_a6d70e84b8fa\",\"districtEn\":\"test_4d60676ba00e\",\"estimateTime\":38,\"estimateTimeType\":63,\"fenceTypeId\":94,\"floors\":[{\"busyState\":34,\"categoryIds\":[75],\"defaultFloor\":68,\"floorImage\":{\"contentType\":\"test_d6a7aad8c246\",\"createdAt\":\"2029-04-27 03:16:16\",\"height\":\"test_e03c60d027ef\",\"id\":72,\"imageType\":\"test_c84dc988c459\",\"originName\":\"test_3de26739b237\",\"path\":\"test_104457df2c99\",\"size\":\"test_5e5000d65ef1\",\"updatedAt\":\"2030-09-07 10:14:19\",\"userId\":84,\"width\":\"test_a341d96b38a2\"},\"grayImage\":{\"contentType\":\"test_1f283f414fed\",\"createdAt\":\"2030-10-26 10:26:39\",\"height\":\"test_d4d64c315a95\",\"id\":69,\"imageType\":\"test_b0917a63ce71\",\"originName\":\"test_61de1e5d6fde\",\"path\":\"test_e974428255bd\",\"size\":\"test_31bf8c91f6de\",\"updatedAt\":\"2029-01-18 14:40:45\",\"userId\":92,\"width\":\"test_40b1578cd8f5\"},\"id\":45,\"index\":3,\"isActived\":false,\"makingCupsCount\":16,\"makingOrderCount\":93,\"maxCupLimit\":56,\"minCupLimit\":83,\"name\":\"test_1f77d52d5a9f\",\"supportPremade\":false,\"takeawayStatus\":75,\"typeName\":\"test_cf7eda596413\"}],\"freshSurplusStock\":87,\"freshTotalStock\":13,\"gaodeCityCode\":\"test_208b8d7e0916\",\"grouponAbility\":false,\"healthReportSwitch\":false,\"id\":15,\"includeGrouponPolicy\":false,\"inspirationActionShowChannel\":18,\"inspirationActionText\":\"test_83d80c06c3bd\",\"inspirationActionType\":75,\"inspirationActionUrl\":\"test_f2ccf75caac0\",\"inspirationImage\":{\"contentType\":\"test_3c41c56079c9\",\"createdAt\":\"2016-01-24 11:18:18\",\"height\":\"test_53e8820618a9\",\"id\":22,\"imageType\":\"test_6d187d4f3546\",\"originName\":\"test_dba3d3ef44c4\",\"path\":\"test_dbc62937f48c\",\"size\":\"test_b043b2e7c9c8\",\"updatedAt\":\"2017-02-21 20:14:47\",\"userId\":24,\"width\":\"test_2d4fd00b4c1b\"},\"inspirationImageUrl\":\"test_750ec322e8a8\",\"isActived\":false,\"isCashier\":false,\"isEnable\":true,\"isEstimateTime\":false,\"isInspiration\":true,\"isOpen\":false,\"isOverseas\":true,\"isPeakTakeaway\":true,\"isShowOfficial\":true,\"isSupportGuestOrdering\":true,\"isSyncPosOrder\":false,\"isTakeawayStation\":true,\"lastOperatedAt\":\"2027-01-17 23:18:28\",\"lastOperatedAtTimestamp\":41,\"latitude\":\"test_2c874188cc07\",\"licensePicIds\":\"test_514878cea6ff\",\"longitude\":\"test_e95e4ee42cb1\",\"makingCupsCount\":40,\"makingOrderCount\":11,\"minCharge\":49,\"name\":\"test_953c5f703b6e\",\"nameEn\":\"test_67c6f8172d74\",\"nearbyShopCount\":68,\"no\":\"test_b8227f86a80a\",\"onlyTakeaway\":false,\"openAt\":\"test_e8255111576e\",\"outerId\":47,\"policyId\":39,\"promotion\":{\"endAt\":\"2032-08-19 18:28:15\",\"isNewShopLabel\":true,\"isShowMenu\":false,\"label\":\"test_c2c730dc6391\",\"shopLabelDays\":28,\"shopLabelExpireTime\":\"2023-08-12 07:37:21\",\"showMenuTip\":\"test_9b1bdb3897f9\",\"startAt\":\"2023-05-14 04:46:33\"},\"province\":\"test_cbe3cbe3cd56\",\"qrStaffCodeImgUrl\":\"test_12168bf08048\",\"qrcode\":\"test_f033bac9cd05\",\"qrcodeId\":65,\"regionCode\":\"test_b414d10d9880\",\"regionId\":23,\"sceneCode\":\"test_ca412a135f78\",\"sceneCodeId\":45,\"shopBusinessDayList\":[{\"closeAt\":\"test_cc47dab120bd\",\"closeDay\":\"test_8458f1f47e7b\",\"createdAt\":\"2032-12-26 22:23:09\",\"deletedAt\":\"2023-04-12 23:13:11\",\"id\":7,\"openAt\":\"test_b2ba2d6090b8\",\"openDay\":\"test_70f35bb341cf\",\"shopId\":32,\"type\":54,\"updatedAt\":\"2017-07-14 02:29:42\"}],\"shopCurrency\":{\"code\":\"test_ea39140387ee\",\"id\":10,\"name\":\"test_3f5ccd0ff3a1\",\"regionId\":47,\"symbol\":\"test_aab0bba2853e\"},\"shopEnterpriseDiscountActivityInfo\":{\"enterMenuPrompt\":\"test_47b6e0058c9c\",\"menuDiscount\":68.97,\"orderDiscountCopywriting\":\"test_488da35fea14\",\"orderDiscountDesc\":\"test_713dd2c14a2c\",\"orderDiscountIcon\":\"test_a628f6c3354f\"},\"shopImagePath\":{},\"shopLabels\":[{\"applyShop\":false,\"borderColor\":\"test_875f97fd4d0f\",\"copyWritingColor\":\"test_109568974d4d\",\"displayLimitFlag\":true,\"endTime\":\"test_d564af19a23a\",\"id\":5,\"innerName\":\"test_ae4e4dbfac69\",\"name\":\"test_2db7cc28797e\",\"shopId\":18,\"showChannel\":33,\"sort\":3,\"startTime\":\"test_ee602d25214f\"}],\"shopType\":\"test_51fec92b52f4\",\"shopUpperLimit\":0,\"showChannel\":11,\"support\":true,\"supportBusiness\":true,\"supportDdTakeaway\":true,\"supportFnTakeaway\":false,\"supportGroupon\":false,\"supportJdTakeaway\":false,\"supportLineDistance\":false,\"supportMtTakeaway\":false,\"supportPremade\":false,\"supportRideDistance\":false,\"supportSfTakeaway\":false,\"supportShipping\":true,\"supportTakeaway\":true,\"supportTdTakeaway\":true,\"supportZkTakeaway\":true,\"syncBoh\":false,\"takeawayChannel\":76,\"takeawayLastOperateAt\":\"2030-05-21 17:41:38\",\"takeawayLastOperateAtTimestamp\":31,\"takeawayServeType\":79,\"takeawayStatus\":65,\"taxRate\":{\"showTaxExcludePrice\":true,\"showTaxIncludePrice\":false,\"showTaxRateInBill\":true,\"showTaxRateInMenu\":true},\"teaSurplusStock\":98,\"teaTotalStock\":21,\"timeInterval\":79,\"tips\":\"test_7663572a2492\",\"unitBoxSeconds\":57,\"unitBoxShares\":43,\"updatedAt\":\"2023-03-16 23:19:30\",\"zoneId\":{},\"zoneName\":\"test_a30c8a6ad6d6\",\"zoneOffset\":{\"id\":\"test_f014f23e0963\",\"totalSeconds\":36}}";

    @State(value = Scope.Benchmark)
    public static class BenchmarkState {
        ObjectMapper OBJ = new ObjectMapper();
        ThreadLocal<ObjectMapper> MAP_THREAD = new ThreadLocal<>();
    }

    /**
     * 全局只有一个ObjectMapper
     */
    @Benchmark
    public Map globalTest(BenchmarkState state) throws JsonProcessingException {
        Map map = state.OBJ.readValue(json, Map.class);
        return map;
    }

    /**
     * ObjectMapper 线程共享
     */
    @Benchmark
    public Map globalThreadLocalTest(BenchmarkState state) throws JsonProcessingException {
        if (null == state.MAP_THREAD.get()) {
            state.MAP_THREAD.set(new ObjectMapper());
        }
        Map map = state.MAP_THREAD.get().readValue(json, Map.class);
        return map;
    }

    /**
     * 每次new一个ObjectMapper
     */
    @Benchmark
    public Map localTest(BenchmarkState state) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map map = objectMapper.readValue(json, Map.class);

        return map;
    }

    public static void main(String[] args) throws RunnerException {
        Options opts = new OptionsBuilder().include(ObjectMapperTest.class.getName())
                .resultFormat(ResultFormatType.CSV)
                .build();
        new Runner(opts).run();
    }
}
