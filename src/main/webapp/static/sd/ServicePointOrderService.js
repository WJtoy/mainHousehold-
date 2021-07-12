/**
 * 网点工单处理
 */
var ServicePointOrderService = {
    init: function () {
        this.rownum = 0;
        this.orderIds = [];
        this.data = {};
        this.rootUrl = "";
        this.reload = false;
        this.customerId = "";
        this.version = "1.2";
        this.clickTag = 0;
    },
    //网点派单给具体的安维人员
    servicePointPlan: function (orderId, orderNo, quarter) {
        var self = this;
        var planIndex = top.layer.open({
            type: 2,
            id: 'layer_servicePointPlan',
            zIndex: 19891015,
            title: '派单 [' + orderNo + ']',
            content: self.rootUrl + "/servicePoint/sd/orderOperation/servicePointPlan?orderId=" + orderId + "&quarter=" + (quarter || ''),
            area: ['1000px', '620px'],
            shade: 0.3,
            maxmin: false,
            success: function (layero, index) {
            }
        });
    },
    //网点预约时间
    servicePointPending: function (orderId, orderNo, quarter, reservationTimes) {
        var self = this;
        top.layer.open({
            type: 2,
            id: 'layer_servicePointPending',
            zIndex: 19891015,
            title: '工单[' + orderNo + ']已预约' + reservationTimes + '次',
            content: self.rootUrl + "/servicePoint/sd/orderOperation/servicePointPending?orderId=" + orderId + "&quarter=" + quarter || '',
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function (layero, index) {
            },
            end: function () {
            }
        });
    },
    //确认上门
    servicePointConfirmDoorAuto: function (orderId, quarter, parentIndex) {
        var self = this;
        var clicktag = 0;
        top.layer.confirm('确定要执行[确认上门]操作吗?', {icon: 3, title: '系统确认'}, function (index) {
            if (clicktag === 1) {
                return false;
            }
            clicktag = 1;
            top.layer.close(index);//关闭本身
            // do something
            var loadingIndex;
            var ajaxSuccess = 0;
            var data = {orderId: orderId, quarter: quarter};
            $.ajax({
                async: false,
                cache: false,
                type: "POST",
                url: self.rootUrl + "/servicePoint/sd/orderOperation/servicePointConfirmDoorAuto",
                data: data,
                beforeSend: function () {
                    loadingIndex = top.layer.msg('正在提交，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                },
                complete: function () {
                    if (loadingIndex) {
                        top.layer.close(loadingIndex);
                    }
                    //失败
                    if (ajaxSuccess === 0) {
                        setTimeout(function () {
                            clicktag = 0;
                        }, 2000);
                    }
                },
                success: function (data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success) {
                        layerMsg("确认上门成功!");
                        var iframe = getActiveTabIframe();
                        if (iframe !== undefined) {
                            iframe.repage();
                        } else {
                            layerMsg('请手动刷新列表', false);
                        }
                    } else {
                        layerError(data.message, "错误提示");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText, null, "确认上门错误，请重试!");
                }
            });
            return false;
        });
        return false;

    },
    //网点完成服务
    servicePointComplete: function (orderId, orderNo, quarter) {
        var self = this;
        top.layer.open({
            type: 2,
            id: 'layer_servicePointComplete',
            zIndex: 19891015,
            title: '工单[' + orderNo + '] 完成服务',
            content: self.rootUrl + "/servicePoint/sd/orderOperation/servicePointComplete?orderId=" + orderId + "&quarter=" + (quarter || ''),
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function (layero, index) {
            },
            end: function () {
            }
        });
    },
    //维护进度跟踪(网点)
    servicePointTracking: function (orderId, quarter) {
        if (!orderId) {
            return false;
        }
        var self = this;
        var trackingIndex = top.layer.open({
            type: 2,
            id: 'layer_servicePointTracking',
            zIndex: 19891015,
            title: '跟踪进度',
            content: self.rootUrl + "/servicePoint/sd/orderOperation/servicePointTracking?orderId=" + orderId + "&quarter=" + (quarter || ''),
            area: ['1000px', '640px'],
            shade: 0.3,
            maxmin: true,
            success: function (layero, index) {
            },
            end: function () {

            }
        });
    },
    // 保存跟踪进度
    saveServicePointTracking: function (flag, thisIndex, parentIndex) {
        var self = this;
        var remarks = $("#remarks", "#trackingForm").val();
        if (Utils.isEmpty(remarks)) {
            layerError("请输入跟踪内容.", "错误提示");
            clickTag = 0;
            return false;
        }
        var $btnSubmit = $("#btnSaveTracking");
        $btnSubmit.attr('disabled', 'disabled');
        var ajaxSuccess = 0;
        var loadingIndex;
        $.ajax({
            type: "POST",
            url: self.rootUrl + "/servicePoint/sd/orderOperation/saveServicePointTracking?" + (new Date()).getTime(),
            data: $("#trackingForm").serialize(),
            beforeSend: function () {
                loadingIndex = layer.msg('正在提交，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
            },
            complete: function () {
                if (loadingIndex) {
                    layer.close(loadingIndex);
                }
                if (ajaxSuccess == 0) {
                    setTimeout(function () {
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                    }, 2000);
                }
            },
            success: function (data) {
                if (ajaxLogout(data)) {
                    return false;
                }
                $('#btnSaveTracking').removeAttr('disabled');
                if (data && data.success == true) {
                    if (flag && flag == "close") {
                        top.layer.close(thisIndex);
                    } else if (flag && flag == "closeAndReloadParent") {
                        top.layer.close(thisIndex);
                        if (parentIndex) {
                            var layero = $("#layui-layer" + parentIndex, top.document);
                            var iframeWin = top[layero.find('iframe')[0]['name']];
                            iframeWin.reload();
                        } else {
                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if (iframe != undefined) {
                                iframe.repage();
                            }
                        }
                    } else {
                        reload();
                    }
                    ajaxSuccess = 1;
                } else if (data && data.message) {
                    layerError(data.message, "错误提示");
                } else {
                    layerError("跟踪进度错误", "错误提示");
                }
                return false;
            },
            error: function (e) {
                ajaxLogout(e.responseText, null, "保存跟踪进度错误，请重试!");
            }
        });
        return false;
    },
    //查看新方式上传的完成图片
    photoListNew: function (orderId, quarter, isNewOrder) {
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var photoIndex = top.layer.open({
            type: 2,
            id: 'layer_photoListNew',
            zIndex: 19891015,
            title: '完成照片查看',
            content: self.rootUrl + "/servicePoint/sd/orderItemComplete/orderAttachmentForm?orderId=" + orderId + "&quarter=" + quarter,
            area: ['936px','762px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true
        });
    },
    //查看完成图片(旧方式上传的完成图片）
    photoList: function (id, quarter) {
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var photoIndex = top.layer.open({
            type: 2,
            id: 'layer_photolist',
            zIndex: 19891015,
            title: '完成照片查看',
            content: self.rootUrl + "/servicePoint/sd/orderItemComplete/viewDetailAttachment?orderId=" + id + "&quarter=" + quarter,
            area: [(w - 40) + 'px', (h - 40) + 'px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true
        });
    },
    //处理
    servicePointReplyReminder: function (reminderId, quarter) {
        if (!reminderId) {
            return false;
        }
        var self = this;
        var screen = getScreenWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id: 'layer_servicePointReplyReminder',
            zIndex: 19891016,
            title: '催单-回复',
            content: self.rootUrl + "/servicePoint/sd/reminder/servicePointReplyReminderForm?reminderId=" + reminderId + "&quarter=" + quarter,
            area: ['1385px', '800px'],
            shade: 0.3,
            maxmin: false,
            success: function (layero, index) {
            },
            end: function () {
            }
        });
    },
    //查看
    servicePointViewReminder: function (reminderId, orderId, quarter) {
        if (!reminderId) {
            return false;
        }
        var self = this;
        var screen = getScreenWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id: 'layer_reminder_form',
            zIndex: 19891016,
            title: '催单',
            content: self.rootUrl + "/servicePoint/sd/reminder/servicePointViewReminder?reminderId=" + reminderId + "&orderId=" + (orderId || '') + "&quarter=" + quarter,
            area: ['1385px', '800px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: false,
            success: function (layero, index) {
            },
            end: function () {
            }
        });
    },
    //装载催单项目列表
    showReminderItems: function (data) {
        if (data && data.items.length > 0) {
            var tmpl = document.getElementById('tpl-items').innerHTML;
            var doTtmpl = doT.template(tmpl);
            var html = doTtmpl(data);
            $("#tabItems").html(html);
            $('a[data-toggle=tooltip]', "#tabItems").darkTooltip();
        } else {
            $("#tabItems").append("<table id='tb_items' style='display:none;'></table>无记录");
        }
    },
    //装载待确认催单项目列表
    showReminderConfirmItems: function (model) {
        if (model && model.items && model.items.length > 0) {
            var tmpl = document.getElementById('tpl-items').innerHTML;
            var doTtmpl = doT.template(tmpl);
            var html = doTtmpl(model);
            $("#tabItems").html(html);
            $('a[data-toggle=tooltip]', "#tabItems").darkTooltip();
        } else {
            $("#tabItems").append("<table id='tb_items' style='display:none;'></table>无记录");
        }
    },
    //浏览订单明细
    showOrderDetailInfo: function (orderId, quarter, layerId) {
        var self = this;
        var zindex = 19891015;
        if (layerId) {
            zindex = 19891019;
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id: layerId || 'layer_orderdetail',
            zIndex: zindex,
            title: '订单详情',
            content: self.rootUrl + "/servicePoint/sd/orderInfo/showOrderDetailInfo?orderId=" + orderId + "&quarter=" + (quarter || ''),
            shade: 0.3,
            shadeClose: true,
            area: ['1200px', '800px'],
            maxmin: false,
            success: function (layero, index) {
            },
            end: function () {
            }
        });
        setCookie('layer.parent.id', orderDetail_index);
    },
    //订单详情-工单辅材
    auxiliaryMaterial_showDetailInfo: function (orderId, quarter, forceRefresh) {
        var self = this;
        var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
        var loadingIndex = top.layer.msg('正在加载工单辅材列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });
        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl + "/servicePoint/sd/orderInfo/orderAuxiliaryMaterialInfo?orderId=" + orderId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if (ajaxLogout(data)) {
                    return false;
                }
                if (data.success) {
                    if (data.data && data.data.items && data.data.remarks) {
                        var tmpl = document.getElementById('tpl-orderAuxiliaryMaterialList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabAuxiliaryMaterials").html(html);
                        //让模板的自定义提示生效
                        $('a[data-toggle=tooltip]').darkTooltip();
                    } else {
                        $("#tabAuxiliaryMaterials").empty().append("<table id='tabAuxiliaryMaterials' style='display:none;'></table>无记录");
                    }
                } else {
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText, null, "装载辅材信息错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },
    //浏览完成图片，不可新增和编辑
    browsePhotoList: function (id, quarter) {
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var photoIndex = top.layer.open({
            type: 2,
            id: 'layer_photolistNew',
            zIndex: 19891015,
            title: '完成照片查看',
            content: self.rootUrl + "/servicePoint/sd/orderItemComplete/browseOrderAttachment?orderId=" + id + "&quarter=" + quarter,
            area: [(w - 40) + 'px', (h - 40) + 'px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true,
            success: function (layero) {
            }
        });
    },
    //订单详情页
    showProcessOrderDetail:function(id,quarter,orderType,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'true';
        }
        var realUrl = self.rootUrl+"/servicePoint/sd/processOrderList/service/orderDetailInfo?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        if(orderType == 3 || orderType === 4){
            realUrl = self.rootUrl+"/servicePoint/sd/processOrderList/service/orderDetailInfoForReturn?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_orderdetail',
            zIndex: 19891015,
            title:'订单详情',
            content: realUrl,
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(iframe != undefined){
                    var repageFlag = $("#repageFlag",iframe.document).val();
                    if(repageFlag == "true"){
                        iframe.repage();
                    }
                }
            }
        });
        setCookie('layer.parent.id',orderDetail_index);
    },
    //待派单订单详情页
    showKefuOrderDetailForPlan:function(id,quarter,orderType,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'true';
        }
        var realUrl = self.rootUrl+"/servicePoint/sd/processOrderList/service/orderDetailInfoForPlan?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        if(orderType == 3 || orderType === 4){
            realUrl = self.rootUrl+"/servicePoint/sd/processOrderList/service/orderDetailInfoForReturn?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_orderdetail',
            zIndex: 19891015,
            title:'订单详情',
            content: realUrl,
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
                setCookie('layer.parent.id',index);
            },
            end:function(){
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(iframe != undefined){
                    var repageFlag = $("#repageFlag",iframe.document).val();
                    if(repageFlag == "true"){
                        iframe.repage();
                    }
                }
            }
        });
    },
    //回访订单详情页
    showFollowUpFailOrderDetail:function(id,quarter,orderType,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'true';
        }
        var realUrl = self.rootUrl+"/servicePoint/sd/processOrderList/service/orderDetailInfoForFollowUp?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        if(orderType == 3 || orderType === 4){
            realUrl = self.rootUrl+"/servicePoint/sd/processOrderList/service/orderDetailInfoForReturn?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_orderdetail',
            zIndex: 19891015,
            title:'订单详情',
            content: realUrl,
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(iframe != undefined){
                    var repageFlag = $("#repageFlag",iframe.document).val();
                    if(repageFlag == "true"){
                        iframe.repage();
                    }
                }
            }
        });
        setCookie('layer.parent.id',orderDetail_index);
    },
    //历史派单订单详情页
    showKefuHistoryOrderDetail:function(id,quarter){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_history_orderdetail',
            zIndex: 19891019,
            title: '订单详情',
            content: self.rootUrl+"/servicePoint/sd/processOrderList/service/historyOrderDetailInfo?id="+ id + "&quarter=" + (quarter || ''),
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
        setCookie('layer.parent.id',orderDetail_index);
    }
};

$(function () {
    ServicePointOrderService.init();
});

