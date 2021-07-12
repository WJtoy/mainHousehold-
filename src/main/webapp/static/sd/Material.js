/**
 * 配件管理相关操作
 * 依赖layer弹窗
 */
var Material = {
    init:function(){
        this.version = "1.0";
        this.rootUrl="";
        this.clickTag = 0;
    },
    //跟踪进度窗口
    pendingForm:function (id,orderNo,quarter) {
        var self = this;
        top.layer.open({
            type: 2,
            id:'layer_pending',
            zIndex:19891015,
            title:'跟踪进度',
            content: self.rootUrl + "/sd/material/pending?id="+ id +"&quarter=" + quarter || '',
            area: ['800px', '600px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //开单-装载客户产品及账户余额,信用额度
    viewPendingLog:function (id,orderNo,quarter) {
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        top.layer.open({
            type: 2,
            id:'layer_pending',
            zIndex:19891015,
            title:'工单[' + orderNo + ']配件申请单-跟踪进度',
            content: self.rootUrl + "/sd/material/pendingLog?id="+ id +"&quarter=" + quarter || '',
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //保存跟踪进度后，更新列表中相关内容
    updatePendingInfo:function (pending){
        var self = this;
        var $tr = $("#tr_"+pending.id);
        $tr.find("#pendingType").text(pending["pendingType.label"]);
        $tr.find("#pendingDate").text(pending.pendingDate);
        var lnkContnent = $tr.find("#pendingContent");
        lnkContnent.text(pending.pendingContent.substr(0,20));
        //tooltip
        var idx = $tr.data("index");
        if($("ins")[idx]){
            $($("ins").get(idx)).find("div").first().text(pending.pendingContent);
        }
        // lnkContnent.data("tooltip",pending.pendingContent);
        // lnkContnent.darkTooltip();
    },

    //返件签收
    updateSign:function (id,quarter) {
        var self = this;
        top.layer.open({
            type: 2,
            id:'layer_sign',
            zIndex:19891015,
            title:'待签收',
            content: self.rootUrl + "/sd/material/return/waitSign?materialReturnId="+ id +"&quarter=" + quarter || '',
            area: ['1000px', '800px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    }
};

$(function(){
    Material.init();
});

