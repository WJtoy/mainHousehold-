/**
 * 业务/跟单工单处理
 */
var SalesOrderService = {
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

    //业务/跟单查看工单详情
    viewOrderDetail:function(id,quarter,layerId){
        var self = this;
        var zindex = 19891015;
        if(layerId){
            zindex = 19891019;
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id: layerId || 'layer_orderdetail',
            zIndex: zindex,
            title:'订单详情',
            content: self.rootUrl+"/sales/sd/orderInfo/showOrderDetailInfo?id="+ id+ "&quarter=" + (quarter || ''),
            shade: 0.3,
            shadeClose: true,
            area:['1200px','800px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
        // top.layer.full(orderDetail_index);
        setCookie('layer.parent.id',orderDetail_index);
    },

    //打开问题反馈列表，并可回复
    replylist:function(feedbackId,quarter,orderNo,orderId) {
        if (!feedbackId || !quarter) {
            return false;
        }
        var self = this;
        var title = (orderNo || '') + '问题反馈';
        if(!Utils.isNull(orderId)){
            var title = '<a style="cursor:pointer;" onclick="SalesOrderService.viewOrderDetail(' + "'" + orderId  + "','" + (quarter || '') +"'" +');">' + (orderNo || '') + '问题反馈</a>';
        }
        var replyIdex = top.layer.open({
            type: 2,
            id:'layer_replylist',
            zIndex: 19891015,
            title: title,
            move: false,
            // title: (orderNo || '') + '问题反馈',
            content: self.rootUrl+"/sales/sd/feedBack/replylist?id="+ feedbackId + "&quarter=" + quarter,
            shade: 0.3,
            area: ['810px', '565px'],
            shadeClose: true,
            maxmin: false,
            success: function(layero){
            }
        });

    },

    //添加问题反馈(提交)
    addFeedbackReply:function (id,quarter){
        if ($("#btnSend").hasClass("disabled") == true)
        {
            return true;
        }
        var remarksVal = $("#remarks").val();
        if (remarksVal == '')
        {
            layerAlert("请输入回复内容");
            return true;
        }
        var forbiddenArray = filterForbiddenStr(remarksVal);
        if(forbiddenArray != null){
            layerAlert("内容含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
            return false;
        }
        var self = this;
        var reply = {feedbackId:id,quarter:quarter};
        reply.remarks = remarksVal;
        $.ajax({
            cache : false,
            type : "POST",
            url : self.rootUrl + "/sales/sd/feedBack/reply",
            data : reply,
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success)
                {
                    self.addReplyItem(data.data);
                    $("#remarks").val("").focus();
                }else
                {
                    layerError("保存回复内容错误:" + data.message, "错误提示");
                }
            },
            error : function(e)
            {
                ajaxLogout(e.responseText,null,"保存回复内容错误，请重试!");
            }
        });
    },

    //动态添加问题反馈内容（ui）
    addReplyItem:function(data){
        var tmpl = document.getElementById('tpl-reply-content').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var reply = doTtmpl(data);
        $("#chat_chatmsglist").append(reply);
    },

    //添加图片窗口
    addReplyAttach:function(feedbackId,quarter,feedbackLayerIndex){
        if (!feedbackId) {
            return false;
        }
        var self = this;
        var replyIdex = top.layer.open({
            type: 2,
            id: 'layer_replylAttach',
            zIndex: 19891016,
            title: '附件图片',
            content: self.rootUrl+"/sales/sd/feedBack/replyAttach?id="+ feedbackId + "&quarter=" + quarter +"&parentIndex=" + (feedbackLayerIndex || ''),
            shade: 0.3,
            area: ['1200px', '710px'],
            maxmin: false,
            success: function(layero){

            }
        });
    },

    //处理反馈异常
    handled:function(orderId,quarter){
        var self = this;
        var $btn = $("#btnHandled");
        if($btn.attr("disabled") == "disabled"){
            return false;
        }
        var data = {orderId:orderId,quarter:quarter};
        $.ajax({
            cache : false,
            type : "POST",
            url : self.rootUrl+"/sales/sd/feedBack/handled",
            data : data,
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success)
                {
                    layerInfo("订单反馈异常处理成功.","系统提示");
                    setTimeout(function() {
                        loadMyMessages( self.rootUrl);//刷新数量
                        try {
                            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                            pframe.repage();
                            $btn.hide();
                        }catch(err) {}
                    }, 1000);
                }else
                {
                    $btn.removeAttr("disabled");
                    layerError("订单异常处理错误:" + data.message, "错误提示");
                }
                return false;
            },
            error : function(e)
            {
                $btn.removeAttr("disabled");
                ajaxLogout(e.responseText,null,"订单异常处理错误，请重试!");
            }
        });
        return false;
    },

    //标记已读
    readReply:function(id,quarter){
        if(!id){
            return false;
        }
        var self = this;
        $.ajax({
            type : "POST",
            url : self.rootUrl+"/sales/sd/feedBack/read?id="+id + "&quarter=" + quarter,
            data : null,
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(pframe != undefined){
                    $("img[id='complain_"+id+"']", pframe.document).hide();
//                        pframe.repage();
                }
                loadMyMessages(self.rootUrl);//刷新数量
            },
            error : function(e)
            {
                // layerError("更新问题反馈状态错误:" + e, "错误提示");
                ajaxLogout(e.responseText,null,"更新问题反馈状态错误，请关闭并重新进入此页面!");
            }
        });
    },

    //浏览完成图片，不可新增和编辑
    browsePhotolist:function(id,quarter){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var photoIndex = top.layer.open({
            type: 2,
            id:'layer_salesPhotolistNew',
            zIndex: 19891015,
            title:'完成照片查看',
            content: self.rootUrl+"/sales/sd/orderInfo/browseOrderAttachment?orderId=" + id + "&quarter=" + quarter,
            area:[(w-40)+'px',(h-40)+'px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true,
            success: function(layero){
            }
        });
    },

    //新增问题反馈窗口
    feedback:function(orderId,quarter,parentIndex){
        if (!orderId) {
            return false;
        }
        var self = this;
        var replyIdex = top.layer.open({
            type: 2,
            id:'layer_salesFeedback',
            zIndex: 19891015,
            title:'问题反馈',
            content: self.rootUrl+"/sales/sd/feedBack/form?type=order&order.id=" + orderId + "&quarter=" + quarter+ "&parentIndex=" + (parentIndex || ''),
            shade: 0.3,
            shadeClose: true,
            area: ['800px', '480px'],
            maxmin: false,
            success: function(layero){},
            end: function(){}
        });
    },

    //订单日志-跟踪进度
    showTrackingLogs:function(id,quarter,isCustomer){
        var self = this;
        if($("#tb_tracking").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sales/sd/orderInfo/trackingLog?orderId=" + id + "&isCustomer=" + (isCustomer || '') + "&quarter=" + (quarter || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        if(data.data && data.data.length>0) {
                            var tplId = "tpl-tracking";
                            if(isCustomer && isCustomer == 'true'){
                                tplId = "tpl-customer-tracking";
                            }
                            var tmpl = document.getElementById(tplId).innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var html = doTtmpl(data.data);
                            $("#tabTracking").append(html);
                        }else{
                            $("#tabTracking").html("无记录");
                        }
                    }else{
                        layerError(data.message,"错误提示");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载跟踪进度错误，请重试!");
                }
            });
        }
    },

    //订单日志-异常处理
    showExceptLogs:function(id,quarter){
        var self = this;
        if($("#tb_except").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sales/sd/orderInfo/exceptLog?orderId=" + id + "&quarter=" + (quarter || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success ) {
                        if(data.data && data.data.length>0) {
                            var tmpl = document.getElementById('tpl-except').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var html = doTtmpl(data.data);
                            $("#tabException").append(html);
                        }else {
                            $("#tabException").append("<table id='tb_except' style='display:none;'></table>无记录");
                        }
                    }else{
                        layerError(data.message);
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载异常处理错误，请重试!");
                }
            });
        }
    },

    //订单日志-问题反馈
    showFeedbackLogs:function(feedbackId,quarter){
        var self = this;
        if($("#tb_feedback").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sales/sd/feedBack/feedbackLog?id=" + feedbackId+ "&quarter=" + (quarter || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        if(data.data && data.data.length>0) {
                            var tmpl = document.getElementById('tpl-feedback').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var html = doTtmpl(data.data);
                            $("#tabFeedback").append(html);
                        }else{
                            $("#tabFeedback").html("无记录");
                        }
                    }else{
                        layerError(data.message, "错误提示");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载反馈错误，请重试!");
                }
            });
        }
    },

    //订单详情-退补单
    showCustomerReturnAndAdditionalList:function(orderId,orderNo,quarter){
        var self = this;
        if($("#tb_cutomerReturn").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sales/sd/orderInfo/customerReturnAndAdditionalList?orderId=" + orderId+ "&quarter=" + (quarter || '') + "&orderNo=" + (orderNo || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        if(data.data && data.data.length>0) {
                            var tmpl = document.getElementById('tpl-cutomerReturn').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var html = doTtmpl(data.data);
                            $("#tabCustomerReturn").append(html);
                        }else{
                            $("#tabCustomerReturn").append("<table id='tb_cutomerReturn' style='display:none;'></table>无记录");
                        }
                    }else{
                        layerError(data.message, "出错了!");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载退补错误，请重试!");
                }
            });
        }
    },

    //订单日志-投诉单
    showComplainList:function(orderId,quarter,forceRefresh){
        var self = this;
        if((forceRefresh && forceRefresh == true) || $("#tb_complain").length == 0){
            var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
            var loadingIndex = top.layer.msg('正在加载投诉单列表...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });

            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sales/sd/complain/ajax/list?orderId=" + orderId + "&quarter=" + (quarter || ''),
                dataType: 'json',
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success ) {
                        if(data.data && data.data.length>0) {
                            var tmpl = document.getElementById('tpl-complain').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            // var html = doTtmpl(data.data);
                            var item = {parentLayerIndex:parentLayerIndex,data:data.data};
                            var html = doTtmpl(item);
                            $("#tabComplain").html(html);
                            //让模板的自定义提示生效
                            $('a[data-toggle=tooltip]').darkTooltip();
                        }else {
                            $("#tabComplain").empty().append("<table id='tb_complain' style='display:none;'></table>无记录");
                        }
                    }else{
                        layerError(data.message);
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载投诉单错误，请重试!");
                    top.layer.close(loadingIndex);
                }
            });
        }
    },

    //编辑投诉单
    complain_form:function(id,orderId,quarter,parentIndex){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        //console.log(screen.width + 'x' + screen.height);
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_form',
            zIndex:19891016,
            title:'投诉单',
            content: self.rootUrl+"/sales/sd/complain/form?id=" + (id || '') + "&quarter=" + quarter + "&orderId="+ orderId + "&parentIndex=" + (parentIndex || '0'),
            //area: ['980px', '640px'],
            area: ['1255px', screen.height-100+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },

    //撤销投诉单操作
    doCancelComplain:function (orderId,complainId,quarter) {
        var self = this;
        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sales/sd/complain/ajax/cancleComplain?complainId=" + complainId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    SalesOrderService.showComplainList(orderId,quarter,true);
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载投诉单错误，请重试!");
            }
        });
    },

    //申诉
    appeal_form:function(id,complainNo,quarter,parentIndex){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_appeal_form',
            zIndex:19891016,
            title:'申诉',
            content: self.rootUrl+"/sales/sd/complain/appealForm?id=" + (id || '') + "&quarter=" + quarter +"&complainNo="+complainNo+ "&parentIndex=" + (parentIndex || '0'),
            area: ['800px', screen.height*2/3+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
                setCookie('layer.parent.id',index);
            },
            end:function(){
            }
        });
    },

    //查看投诉单
    complain_view:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_view',
            zIndex:19891016,
            title:'投诉单',
            content: self.rootUrl+"/sales/sd/complain/view?id=" + (id || '') + "&quarter=" + quarter,
            // area: ['980px', '640px'],
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },

    //判定-显示投诉日志列表
    showComplainLogList:function(complainId,quarter){
        var self = this;
        var loadingIndex = top.layer.msg('正在加载投诉日志列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sales/sd/complain/ajax/complainLogList?complainId=" + complainId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.length>0) {
                        var tmpl = document.getElementById('tpl-complainlogList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabComplainLogList").html(html);
                    }else {
                        $("#tabComplainLogList").append("<table id='tb_complainLogList' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载投诉日志列表错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },

    //弹窗显示订单明细(for 投诉单)
    showComplainOrderDetail:function(id,quarter,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'false';
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_comPlainOrderdetail',
            zIndex:19891015,
            title:'订单详情',
            content: self.rootUrl+"/sales/sd/complain/orderDetailInfo?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || ''),
            shade: 0.3,
            area:[(w-200)+'px',(h-100)+'px'],
            // area:[screen.width+'px',screen.height+'px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },

    //订单详情-加载投突击列表
    crush_showList:function(orderId,quarter,forceRefresh){
        var self = this;
        var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
        var loadingIndex = top.layer.msg('正在加载突击单列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sales/sd/crush/ajax/list?orderId=" + orderId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.length>0) {
                        var tmpl = document.getElementById('tpl-orderCrushList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabOrderCrush").html(html);
                        //让模板的自定义提示生效
                        $('a[data-toggle=tooltip]').darkTooltip();
                    }else {
                        $("#tabOrderCrush").empty().append("<table id='tabOrderCrush' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载突击单错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },

    //查看突击单
    crush_view:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_crush_view',
            zIndex:19891016,
            title:'突击单',
            content: self.rootUrl+"/sales/sd/crush/view?id=" + (id || '') + "&quarter=" + quarter,
            // area: ['980px', '640px'],
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },

    //订单日志-催单
    showReminderListForCustomer:function(orderId,quarter,forceRefresh){
        var self = this;
        if((forceRefresh && forceRefresh == true) || $("#tb_reminder").length == 0){
            var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
            var loadingIndex = top.layer.msg('正在加载催单列表...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sales/sd/reminder/ajax/list?orderId=" + orderId + "&quarter=" + (quarter || '') + "&detailType=2",
                dataType: 'json',
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success ) {
                        if(data.data){
                            //{isWait:1|0,value:model}
                            var ajaxData = data.data;
                            var tmpl = document.getElementById('tpl-reminderItem').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var item = {
                                parentLayerIndex:parentLayerIndex,
                                userType:userType,
                                isWait:ajaxData.isWait,
                                reminderCheckFlag:ajaxData.reminderCheckFlag,
                                reminderCheckMsg:ajaxData.reminderCheckMsg,
                                needConfirm:ajaxData.needConfirm,
                                data:ajaxData.value,
                                reminderType:ajaxData.reminderReasons};
                            var html = doTtmpl(item);
                            $("#tabReminder").html(html);
                            //让模板的自定义提示生效
                            $('a[data-toggle=tooltip]').darkTooltip();
                        }else{
                            $("#tabReminder").empty().append("<table id='tb_reminder' style='display:none;'></table>无催单");
                        }
                    }else{
                        layerError(data.message);
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载催单错误，请重试!");
                    top.layer.close(loadingIndex);
                }
            });
        }
    },

    //订单详情-加载投突击列表
    auxiliaryMaterial_showDetailInfo:function(orderId,quarter,forceRefresh){
        var self = this;
        var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
        var loadingIndex = top.layer.msg('正在加载突击单列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sales/sd/orderInfo/auxiliaryMaterialDetailInfo?orderId=" + orderId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.items && data.data.items.length>0 && data.data.formType==0) {
                        var tmpl = document.getElementById('tpl-orderAuxiliaryMaterialList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabAuxiliaryMaterials").html(html);
                        //让模板的自定义提示生效
                        $('a[data-toggle=tooltip]').darkTooltip();
                        $('#tb_orderAuxiliaryMaterialList').viewer();
                    }else if(data.data && data.data.formType==1){
                        var tmpl = document.getElementById('tpl-orderAuxiliaryMaterialNoItem').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabAuxiliaryMaterials").html(html);
                        //让模板的自定义提示生效
                        $('a[data-toggle=tooltip]').darkTooltip();
                        $('#tb_orderAuxiliaryMaterialNoItem').viewer();
                    }else {
                        $("#tabAuxiliaryMaterials").empty().append("<table id='tabAuxiliaryMaterials' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载辅材信息错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },

};

$(function () {
    SalesOrderService.init();
});

