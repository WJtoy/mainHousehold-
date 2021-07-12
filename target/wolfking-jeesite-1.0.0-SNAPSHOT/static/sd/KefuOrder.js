/**
 * 客服订单操作方法
 * 依赖layer弹窗
 */
var KefuOrder={
    init:function(){
    	this.rownum=0;
        this.orderIds = [];
        this.data={};
        this.rootUrl="";
        this.reload = false;
        this.customerId = "";
        this.version = "1.2";
        this.clickTag = 0;
    },
    //添加上门服务项目for待回访
    addServiceForFollowUp:function(id,parentIndex){
        var self = this;
        var service_index = top.layer.open({
            type: 2,
            id:'layer_addService',
            zIndex:19891016,
            title:'添加服务明细',
            content: self.rootUrl+"/sd/order/kefuOrderList/service/addServiceForFollowUp?orderId="+ id + "&parentIndex=" + (parentIndex || ''),
            area: ['1000px', '750px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //修改上门服务项目for待回访
    editServiceForFollowUp:function(orderId,detailId,quarter,parentIndex){
        var self = this;
        var service_index = top.layer.open({
            type: 2,
            id:'layer_addService',
            zIndex:19891016,
            title:'修改服务明细',
            content: self.rootUrl+"/sd/order/kefuOrderList/service/editServiceForFollowUp?orderId="+ orderId + "&detailId=" + detailId + "&quarter=" + (quarter || '') + "&parentIndex=" + (parentIndex || ''),
            area: ['1000px', '750px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //确认上门for待回访
    confirmDoorForFollowUp:function(id,quarter,parentIndex,confirmType)
    {
        if(!confirmType){
            confirmType = 0;//客服
        }
        var self = this;
        var clicktag = 0;
        top.layer.confirm('确定要执行[确认上门]操作吗?', {icon: 3, title:'系统确认'}, function(index){
            if(clicktag == 1){
                return false;
            }
            clicktag = 1;
            top.layer.close(index);//关闭本身
            // do something
            var loadingIndex;
            var ajaxSuccess = 0;
            var data = { orderId : id,quarter: quarter,confirmType: confirmType};
            $.ajax({
                async: false,
                cache: false,
                type : "POST",
                url : self.rootUrl + "/sd/order/kefuOrderList/service/confirmDoorAutoForFollowUp",
                data : data,
                beforeSend: function () {
                    loadingIndex = top.layer.msg('正在提交，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                },
                complete: function () {
                    if(loadingIndex) {
                        top.layer.close(loadingIndex);
                    }
                    //失败
                    if(ajaxSuccess == 0) {
                        setTimeout(function () {
                            clicktag = 0;
                        }, 2000);
                    }
                },
                success : function(data)
                {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success)
                    {
                        layerMsg("确认上门成功!");
                        if(confirmType === 0) {//客服
                            var pid = 0;
                            if (parentIndex) {
                                pid = parentIndex;
                            } else {
                                var orderDetail = getGlobalVar('layerFrameConfig.orderDetail', "json");
                                if (orderDetail && orderDetail != null && orderDetail != undefined) {
                                    pid = orderDetail.index;
                                }
                            }
                            if (pid > 0) {
                                var layero = $("#layui-layer" + pid, top.document);
                                var iframeWin = top[layero.find('iframe')[0]['name']];
                                iframeWin.reload('tabService');
                                ajaxSuccess = 1;
                                return false;
                            }
                        }else{
                            //网点，刷新列表
                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(iframe != undefined) {
                                iframe.repage();
                            }else{
                                layerMsg('请手动刷新列表',false);
                            }
                        }
                    }else
                    {
                        layerError(data.message, "错误提示");
                    }
                },
                error : function(e)
                {
                    ajaxLogout(e.responseText,null,"确认上门错误，请重试!");
                }
            });
            return false;
        });
        return false;

    },
    //回访失败处理：预约到期
    toArriveAppointment:function(id,orderNo,quarter,parentIndex){
        if(!id){return false;}
        var self = this;
        var clicktag = 0;
        top.layer.confirm('确定将该订单转到预约超期列表吗?', {icon: 3, title:'系统确认'}, function(index) {
            if(clicktag == 1){
                return false;
            }
            clicktag = 1;
            top.layer.close(index);//关闭本身
            // do something
            if (id.length > 0) {
                var loadingIndex;
                var ajaxSuccess = 0;
                var postData = {orderId: id, quarter: quarter};
                $.ajax({
                    cache: false,
                    type: "POST",
                    async: false,
                    url: self.rootUrl + "/sd/order/kefuOrderList/service/toArriveAppointment",
                    data: postData,
                    beforeSend: function () {
                        loadingIndex = layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        //失败
                        if(ajaxSuccess == 0) {
                            setTimeout(function () {
                                clicktag = 0;
                            }, 2000);
                        }
                    },
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data) {
                            if (data && data.success == true) {
                                ajaxSuccess = 1;
                                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(iframe != undefined) {
                                    iframe.repage();
                                }
                            }
                            else {
                                layerError("提交失败!",true);
                            }
                        }
                        return false;
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"预约超期处理失败，请重试!");
                    }
                });
            }
        });
    },
};

$(function(){
    KefuOrder.init();
});

