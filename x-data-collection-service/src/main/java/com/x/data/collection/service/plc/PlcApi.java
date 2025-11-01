//package com.x.data.collection.service.plc;
//
//import com.google.common.util.concurrent.ListenableFuture;
//import com.x.data.collection.service.plc.read.PlcReadReq;
//import com.x.data.collection.service.plc.read.PlcReadRes;
//import com.x.data.collection.service.plc.write.PlcWriteReq;
//import com.x.data.collection.service.plc.write.PlcWriteRes;
//import retrofit2.http.*;
//
//import java.util.List;
//
//public interface PlcApi {
//
//    /**
//     * http://{doamin}:10001/read/{uid}
//     */
//    @POST("/PLC/Read/{uid}")
//    @Headers({"Content-Type:application/json;encoding=utf-8"})
//    ListenableFuture<PlcReadRes> getPlcData(@Body List<PlcReadReq> request, @Path("uid") String uid);
//
//    @POST("/PLC/Write/{uid}")
//    @Headers({"Content-Type:application/json;encoding=utf-8"})
//    ListenableFuture<PlcWriteRes> setPlcData(@Body List<PlcWriteReq> request, @Path("uid") String uid);
//
//    @GET("/PLC/Status/ReadService")
//    @Headers({"Content-Type:application/json;encoding=utf-8"})
//    ListenableFuture<PlcStatus> getPlcStatus();
//}
