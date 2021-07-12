<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <!-- 网点单个产品的所有服务价格 -->
    <title>安维价格</title>
    <style>
        #editBtn{
            position: fixed;
            left: -140px;
            bottom: 5px;
            width: 140%;
            height: 50px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #e5e5e5;
        }
    </style>
    <script>
        function child(obj) {
            var data = eval(obj);
            $("#servicePointId").val(data.servicePointId);
            $("#servicePointNo").val(data.servicePointNo);
            $("#serviceName").val(data.serviceName);
            $("#servicePointPrimaryName").val(data.servicePointPrimaryName);
            $("#contactInfo").val(data.contactInfo);
            $("#customizePriceFlag").val(data.customizePriceFlag);
            $("#useDefaultPrice").val(data.useDefaultPrice);
            $("#degree").val(data.degree);
            $("#serviceRemotePriceFlag").val(data.serviceRemotePriceFlag);
            $("#remotePriceFlag").val(data.remotePriceFlag);
            $("#remotePriceType").val(data.remotePriceType);

            $("#type").html(data.type);
        }
    </script>
</head>
<body>
    <input type="hidden" id="servicePointId">
    <input type="hidden" id="servicePointNo">
    <input type="hidden" id="serviceName">
    <input type="hidden" id="servicePointPrimaryName">
    <input type="hidden" id="contactInfo">
    <input type="hidden" id="customizePriceFlag">
    <input type="hidden" id="useDefaultPrice">
    <input type="hidden" id="degree">
    <input type="hidden" id="serviceRemotePriceFlag">
    <input type="hidden" id="remotePriceFlag">
    <input type="hidden" id="remotePriceType">


    <div style="padding: 45px;">
        确认该网点师傅价格恢复标准价【<label id="type" style="color: #0096DA;"></label>】吗？
    </div>

    <div id="editBtn">
        <input id="btnSubmit" class="btn btn-primary" type="button" value="恢复标准价" onclick="recoverPrice()" style="margin-left: 410px;margin-top: 10px;width: 95px;height: 37px;background: #0096DA;border-radius: 4px;"/>&nbsp;
        <input id="btnCancel" class="btn" type="button" value="关闭" style="margin-top:10px;width: 85px;height: 37px;border-radius: 4px;"onclick="cancel()"/>
    </div>

<script>
    var this_index = top.layer.index;
    function cancel() {
         top.layer.close(this_index);// 关闭本身
    }

    // 恢复标准价
    function recoverPrice(){
        var data = {
            id : $("#servicePointId").val(),
            customizePriceFlag : 0
        };
        var loadingIndex = layer.msg('正在恢复，请稍等...', {
            icon: 16,
            time: 0,
            shade: 0.3
        });
        var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
        $.ajax({
            url: "${ctx}/md/serviceprice/updateCustomizePriceFlag?serviceRemotePriceFlag=" + serviceRemotePriceFlag,  //默认是form的action， 如果申明，则会覆盖
            type: 'post',               //默认是form的method（get or post），如果申明，则会覆盖
            dataType: 'json',           //html(默认), xml, script, json...接受服务端返回的类型
            data: data,
            success : function (data) {
                if (data.success) {
                    setTimeout(function () {
                        layer.close(loadingIndex);

                        var servicePointId = $("#servicePointId").val();
                        var servicePointNo = $("#servicePointNo").val();
                        var serviceName = $("#serviceName").val();
                        var servicePointPrimaryName = $("#servicePointPrimaryName").val();
                        var contactInfo = $("#contactInfo").val();
                        var customizePriceFlag = 0;
                        var useDefaultPrice = $("#useDefaultPrice").val();
                        var degree = $("#degree").val();
                        var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
                        var remotePriceFlag = 0;
                        var remotePriceType = $("#remotePriceType").val();
                        var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                        if (pframe) {
                            pframe.reloadPrice(servicePointId, servicePointNo, serviceName, servicePointPrimaryName, contactInfo, customizePriceFlag,useDefaultPrice,degree,serviceRemotePriceFlag,remotePriceFlag,remotePriceType);
                        }
                        layerMsg("恢复成功");
                        top.layer.close(this_index);
                    }, 1000);
                } else {
                    layerError("数据恢复错误:" + data.message, "错误提示");
                }
            },
            error : function (data) {

            }
        })
    }
</script>
</body>
</html>
