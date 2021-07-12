<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>服务网点管理</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/jquery-upload-file/js/ajaxfileupload.js"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <script src="${ctxStatic}/area/Area-1.0.js" type="text/javascript"></script>
    <script src="${ctxStatic}/area/tree-area.js" type="text/javascript"></script>
    <script src="${ctxStatic}/layui/layui.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <style type="text/css">
        .imgfile {
            max-width: 80px;
            max-height: 80px;
            cursor: hand;
        }

        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 3px;
            width: 100%;
            height: 55px;
            background: #fff;
            z-index: 10;
            border-top: 1px solid #ccc;
            border-top: 1px solid #e5e5e5;
            text-align: right;
        }

        .line_ {
            border-bottom: 3.5px solid #0096DA;
            width: 65px;
            border-radius: 10px;
        }

        /*父盒子*/
        .flex-container {
            display: -webkit-flex;
            display: flex;
            width: 100%;
            margin-bottom: 15px;
        }

        .flex-item-line {
            margin-left: -32px;
        }

        .flex-item {
            width: 38%;
        }

        .search_button {
            width: 64px;
            height: 30px;
            background-color: #0096DA;
            color: #fff;
            border-radius: 3px;
            border: 1px solid rgba(255, 255, 255, 0);
        }

        .x_line {
            width: 50%;
            margin-left: -32px;
        }

        .y_line {
            width: 52%;
            margin-left: -6px;
        }

        .right {
            float: right;
            padding: 10px;
            width: calc(100% - 208px);
            /*height: 450px;*/
            overflow: auto;
        }

        .list-group-c.active {
            z-index: 2;
            color: #fff;
            background-color: #0096DA;
            border-color: #337ab7;
        }

        .list-group-c {
            position: relative;
            display: block;
            padding: 10px 15px;
            margin-bottom: 8px;
            background-color: #fff;
        }

        .tab-item {
            /*float: left;*/
            width: 93%;
            margin: 8px;
            margin-left: 16px;
        }

        #cateDiv {
            height: 405px;
            width: 116px;
            float: left;
            border-right: 1px solid #EEEEEE;
        }

        #tabs label:hover {
            text-decoration: none;
        }

        #tabs {
            text-align: center;
            margin: 0 auto;
            width: 116px;
        }

        #productDiv {
            float: left;
            width: 754px;
            height: 385px;
        }

        .label-e {
            color: #999999;
        }

        .label-item {
            width: 30%;
            margin-bottom: 26px;
            float: left;
            height: 30px;
        }

        #p_count {
            color: #F34F48;
        }

        .layui-form-checkbox span {
            width: 175px;
        }

        .title_legend {
            margin-top: 25px;
        }
        <%--.upload_warp_img_div_del{position:absolute;width:20px !important;height:20px !important;right:190px;margin-top: 0px !important;--%>
        <%--    background-size: 20px 20px !important;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; border-radius: 4px;}--%>
        .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:80px;height: 80px;}
        .upload_warp_left img{margin-top:0px}
        .upload_warp_left {float: left;width: 70px;border-radius: 4px;cursor: pointer;
            margin-right: 21px;
        }

        .upload_warp_img_div {position: relative;height: 80px;width: 80px;float: left;
            display: table-cell;text-align: center;background-color: #eaeaea;cursor: pointer;margin-right: 10px;
        }
        .upload_warp_img_div .upload_warp_img_div_del{position:absolute;top:0px;width:20px !important;height:20px !important;right:0px;margin-top: 0px !important;
            background-size: 20px 20px !important;border-radius: 4px;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; }
        .img2{border-radius:4px}
        .upload_warp{position: relative;}
        .font_style{
            position: absolute;
            z-index: 2;
            top: 50px;
            width: 67px;
            height: 20px;
            color: #FFFFFF;
            padding-left: 9px;
            font-size: 10px;
            background-color: rgba(0,0,0,0.5);
        }
        .iuConfig{background-color:#333333;opacity:0.9;color:white !important;margin-top: -17px !important;border-bottom-left-radius: 4px;border-bottom-right-radius: 4px}
    </style>
    <c:set var="user" value="${fns:getUser()}"/>
    <script type="text/javascript">
        var treeArea = TreeArea('${ctx}');
        var num = 0;
        var isExecute = false;
        $(document).ready(function () {
            $.validator.addMethod("levelLimit",function(value, element) {
                var deposit = $("#deposit").val();
                var maxAmount = $("#maxAmount").val();
                var minAmount = $("#minAmount").val();
                var mdDepositLevelId = $("#mdDepositLevel\\.id").val();
                if (mdDepositLevelId != 0) {
                    if (parseFloat(deposit) < parseFloat(minAmount)) {
                        $(element).data('error-msg', '应缴金额必须在' + minAmount + '-' + maxAmount + '之间');
                        return false;
                    } else if (parseFloat(deposit) > parseFloat(maxAmount)) {
                        $(element).data('error-msg', '应缴金额必须在' + minAmount + '-' + maxAmount + '之间');
                        return false;
                    }
                }
                return true;
            },function(params, element){ return $(element).data('error-msg');});


            $("[id^='level.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if (optionSelected.val() == "0") {
                    $("[id^='level.label']").val("");
                } else {
                    $("[id^='level.label']").val(optionSelected.text());
                }
            });

            $("[id^='finance.paymentType.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if (optionSelected.val() == "0") {
                    $("[id^='finance.paymentType.label']").val("");
                } else {
                    $("[id^='finance.paymentType.label']").val(optionSelected.text());
                }
            });

            $("[id^='finance.bank.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if (optionSelected.val() == "0") {//请选择
                    $("[id^='finance.bank.label']").val("");
                    $ctl = $("[id='finance.bankNo']");
                    $ctl.val("");
                    $ctl.rules("remove", 'required');
                    $ctl = $("#span_bankNo");
                    $ctl.html("");
                    $("label[for='finance.bankNo']").remove();

                    $ctl = $("[id='finance.bankOwner']");
                    $ctl.val("");
                    $ctl.rules("remove", 'required');
                    var $ctl = $("#span_bankOwner");
                    $ctl.html("");
                    $("label[for='finance.bankOwner']").remove();

                } else {
                    $("[id^='finance.bank.label']").val(optionSelected.text());
                    $ctl = $("[id='finance.bankNo']");
                    $ctl.rules("add", 'required');
                    $ctl = $("#span_bankNo");
                    $ctl.html("*");

                    $ctl = $("[id='finance.bankOwner']");
                    $ctl.rules("add", 'required');
                    var $ctl = $("#span_bankOwner");
                    $ctl.html("*");

                }
            });

            $("[name='finance.discountFlag']:radio").click(function () {
                var checkedVal = $(this).val();
                if (checkedVal == "1") {
                    $("#finance\\.discount").val('${fns:getDictLabelFromMS("10","ServicePointDiscount","4")}');
                } else {
                    $("#finance\\.discount").val("0.0");
                }
            });

            $("#paymentChannel").change(function(){
                var selected = $(this).children('option:selected').val();
                // alert(selected);
                if (selected == 20) {
                    // 高灯
                    $(":radio[name='autoPaymentFlag'][value='0']").attr("checked", false);
                    $(":radio[name='autoPaymentFlag'][value='1']").attr("checked", true);
                } else {
                    $(":radio[name='autoPaymentFlag'][value='1']").attr("checked", false);
                    $(":radio[name='autoPaymentFlag'][value='0']").attr("checked", true);
                }
            });
            $("#inputForm").validate({

                rules: {
                    servicePointNo: {remote: "${ctx}/fi/md/servicePoint/checkNo?id=${servicePoint.id}"},
                    contactInfo1: {remote: "${ctx}/fi/md/servicePoint/checkContact?id=${servicePoint.id}"},
                    "primary.contactInfo": {remote: "${ctx}/fi/md/servicePoint/checkEngineerMobile?id=${servicePoint.primary.id}"},
                    "finance.bankNo": {remote: "${ctx}/fi/md/servicePoint/checkBankNo?id=${servicePoint.id}"},
                    deposit:{levelLimit:true}
                },
                messages: {
                    servicePointNo: {remote: "服务网点编号已存在"},
                    contactInfo1: {remote: "服务网点编号已存在"},
                    "primary.contactInfo": {remote: "手机号已注册"},
                    "finance.bankNo": {remote: "服务网点银行卡号已存在"},
                    deposit:{required: "请输入金额"}
                },
                submitHandler: function (form) {
                    //check
                    var servicePointNo = $("#servicePointNo").val();
                    if (servicePointNo.indexOf("YH") != -1) {
                        var selDegree = $("[name='degree']:radio:checked").val();
                        if (selDegree != '30') {
                            layerInfo("YH开头的网点必须是返现网点", "信息提示");
                            $("#btnSubmit").prop("disabled", false);
                            return false;
                        }
                    }

                    var bankOwnerIdNo = $("#bankOwnerIdNo").val();
                    if (bankOwnerIdNo != null && bankOwnerIdNo != '') {
                        if (!IdentityCodeValid(bankOwnerIdNo)) {
                            layerInfo("请输入正确的开户预留身份证号", "信息提示");
                            $("#btnSubmit").prop("disabled", false);
                            return false;
                        }
                    }

                    var idNo = $("#primary\\.idNo").val();
                    if (idNo != null && idNo != '') {
                        if (!IdentityCodeValid(idNo)) {
                            layerInfo("请输入正确的主帐号身份证号", "信息提示");
                            $("#btnSubmit").prop("disabled", false);
                            return false;
                        }
                    }

                    var str = "";
                    $("input[name='picInfo']").each(function(i,element){
                        var url = this.value;
                        var code = $(this).data("code");
                        if(url!=null && url!=''){
                            str += code+","+url+":";
                        }
                    });
                    $("#primary\\.attachment").val(str);

                    //area
                    var ids = [], anodes = tree.getCheckedNodes(true);
                    if (anodes.length == 0) {
                        $("#btnSubmit").removeAttr('disabled');
                        layerInfo("请选择服务的区域", "信息提示");
                        $("#btnSubmit").prop("disabled", false);
                        return false;
                    }

                    //product
                    var pids = [], pnodes = ptree.getCheckedNodes(true);
                    if (pnodes.length == 0) {
                        $("#btnSubmit").removeAttr('disabled');

                        layerInfo("请选择服务的产品", "信息提示");
                        $("#btnSubmit").prop("disabled", false);
                        return false;
                    }

                    var areaId = $("#areaId").val();
                    if (areaId == undefined || areaId.length == 0) {
                        $("#btnSubmit").removeAttr('disabled');

                        layerInfo("网点地址信息已丢失，请重新选择", "信息提示");
                        $("#btnSubmit").prop("disabled", false);
                        return false;
                    }

                    //data
                    //area
                    for (var i = 0; i < anodes.length; i++) {
                        var area = {};
                        area.id = anodes[i].id;
                        area.type = anodes[i].type;
                        ids.push(area);
                    }
                    $("#areaIds").val(JSON.stringify(ids));

                    //product
                    for (var i = 0; i < pnodes.length; i++) {
                        if (pnodes[i].level > 0) {
                            pids.push(pnodes[i].id);
                        }
                    }
                    $("#productIds").val(pids);
                    var $btnSubmit = $("#btnSubmit");
                    if($btnSubmit.prop("disabled") == true){
                        return false;
                    }
                    $btnSubmit.attr("disabled", true);
                    var url = "${ctx}/fi/md/servicePoint/save";
                    if ($("#id").val() != '') {
                        url = "${ctx}/fi/md/servicePoint/saveBaseInfo";
                    }
                    var options = {
                        url: url,
                        type: 'post',
                        dataType: 'json',
                        data:$(form).serialize(),
                        beforeSubmit: function(formData, jqForm, options){
                            loadingIndex = layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,
                                shade: 0.3
                            });
                            return true;
                        },// 提交前的回调函数
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg(data.message);
                                setTimeout(function () {
                                    cancel();
                                }, 1000);
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError("数据保存错误:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                        },
                    };
                    $("#inputForm").ajaxSubmit(options);
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {

                    $("#btnSubmit").removeAttr('disabled');


                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else if(element.parent().is(".paymentType")){
                        error.insertAfter(element);
                    }else if(element.parent().is(".deposit")){
                        var aspan = $(element.parent()).find("span");
                        error.insertAfter(aspan);
                    } else {
                        var nspan = $(element.parent()).find("span");
                        if(nspan){
                            error.insertAfter(nspan);
                        }else{
                            error.insertAfter(element);
                        }
                    }
                    if (element.context.name == 'servicePointNo') {
                        error.css("margin-left", "190px");
                        element.parent('div').parent('div').append(error);
                    } else if (element.context.name == 'contactInfo1') {
                        error.css("margin-left", "190px");
                        element.parent('div').parent('div').append(error);
                    } else if (element.context.name == 'primary.contactInfo') {
                        error.css("margin-left", "190px");
                        element.parent('div').parent('div').append(error);
                    } else if (element.context.name == 'primary.name') {
                        error.css("margin-left", "190px");
                        element.parent('div').parent('div').append(error);
                    }

                },
            });

            var setting = {
                check: {enable: true, nocheckInherit: true}, view: {selectedMulti: false},
                data: {simpleData: {enable: true}}, callback: {
                    beforeClick: function (id, node) {
                        tree.checkNode(node, !node.checked, true, true);
                        treeArea.obtainTownDataByDistrict(tree, node);
                        return false;
                    }
                }
            };

            // 区域
            var zNodes = [
                    <c:forEach items="${areaList}" var="area">{
                    id: '${area.id}',
                    pId: '${not empty area.parent.id?area.parent.id:0}',
                    name: "${area.id==1?'区域列表':area.name}",
                    type: '${area.type}'
                    <%--name: "${not empty area.parent.id?area.name:'区域列表'}"--%>
                },
                </c:forEach>];
            // 初始化树结构
            var tree = $.fn.zTree.init($("#areaTree"), setting, zNodes);
            // 默认选择节点
            var ids = "${servicePoint.areaIds}".split(",");
            for (var i = 0; i < ids.length; i++) {
                var node = tree.getNodeByParam("id", ids[i]);
                try {
                    tree.checkNode(node, true, false);
                    //treeArea.initTownDataBySelectedDistrict(tree, node, "${servicePoint.id}", "true");
                } catch (e) {
                }
            }

            // 更新网点下的四级区域
            <c:if test="${not empty stationList}">
                <c:forEach var="station" items="${stationList}">
                    var districtNode = tree.getNodeByParam("id", ${station.areaId}, null);//获取区县id
                    var subAreaIds = ${station.subAreaIds};
                    treeArea.fillTownDataBySelectedDistrict(tree, districtNode, subAreaIds, "true");
                </c:forEach>
            </c:if>

            // 默认展开全部节点
            /* 默认展开一级节点*/
            var nodes = tree.getNodesByParam("level", 0);
            for (var i = 0; i < nodes.length; i++) {
                tree.expandNode(nodes[i], true, false, false);
            }

            // 产品
            setting = {
                check: {
                    enable: true,
                    nocheckInherit: true
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                }
            };
            zNodes = [
                <c:forEach items="${fns:getProductCategories()}" var="cat">
                <c:if test="${cat.name ne '烟机'}">
                {id: 'p_${cat.id}', pId: '0', name: "${cat.name}"},
                </c:if>
                </c:forEach>
                    <c:forEach items="${fns:getProducts()}" var="prod">{
                    id: '${prod.id}',
                    pId: 'p_${prod.category.id}',
                    name: "${prod.name}"
                },
                </c:forEach>];
            // 初始化树结构
            var ptree = $.fn.zTree.init($("#productTree"), setting, zNodes);
            // 默认选择节点
            var ids = "${servicePoint.productIds}".split(",");
            for (var i = 0; i < ids.length; i++) {
                var node = ptree.getNodeByParam("id", ids[i]);
                try {
                    ptree.checkNode(node, true, false);
                } catch (e) {
                }
            }
            var categories = "${productCategories}".split(",");
            for (var i = 0; i < categories.length; i++) {
                var node = ptree.getNodeByParam("id", "p_"+categories[i]);
                try {
                    ptree.checkNode(node, true, false);
                } catch (e) {
                }
            }
            // 默认展开全部节点
            ptree.expandAll(true);

            $("#btnSubmitArea").click(function () {
                var $btnSubmit = $("#btnSubmitArea");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                $btnSubmit.prop("disabled",true);
                var ids = [], anodes = tree.getCheckedNodes(true);
                if (anodes.length == 0) {
                    $("#btnSubmitArea").removeAttr('disabled');
                    layerInfo("请选择服务的区域", "信息提示");
                    $("#btnSubmitArea").prop("disabled", false);
                    return false;
                }

                for (var i = 0; i < anodes.length; i++) {
                    var area = {};
                    area.id = anodes[i].id;
                    area.type = anodes[i].type;
                    ids.push(area);
                }
                $("#areaIds").val(JSON.stringify(ids));
                $.ajax({
                    type: "post",
                    url: "${ctx}/fi/md/servicePoint/saveAreas",
                    async: false,
                    data:{
                        id : $("#id").val(),
                        areaIds: $("#areaIds").val()
                    },
                    success: function (data, type) {
                        if (data.success) {
                            layerMsg(data.message);
                            $btnSubmit.prop("disabled",false);
                        } else {
                            layerError("数据保存错误:" + data.message, "错误提示");
                            $btnSubmit.prop("disabled",false);
                        }
                    }
                });
            });



            $("#btnSubmitProduct").click(function () {
                var $btnSubmit = $("#btnSubmitProduct");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                $btnSubmit.prop("disabled",true);
                var pids = [], pnodes = ptree.getCheckedNodes(true);
                if (pnodes.length == 0) {
                    $("#btnSubmitProduct").removeAttr('disabled');
                    layerInfo("请选择服务的产品", "信息提示");
                    return false;
                }

                for (var i = 0; i < pnodes.length; i++) {
                    if (pnodes[i].level > 0) {
                        pids.push(pnodes[i].id);
                    }
                }
                $("#productIds").val(pids);
                $.ajax({
                    type: "post",
                    url: "${ctx}/fi/md/servicePoint/saveProducts",
                    async: false,
                    data: {
                        id : $("#id").val(),
                        productIds: $("#productIds").val()
                    },
                    success: function (data, type) {
                        if (data.success) {
                            layerMsg(data.message);
                            $btnSubmit.prop("disabled",false);
                        } else {
                            layerError("数据保存错误:" + data.message, "错误提示");
                            $btnSubmit.prop("disabled",false);
                        }
                    }
                })
            });
        });

        //保存新的网店备注
        function saveServicePointRemark(servicePointId, btn) {
            if (btn.disabled == true) {
                return fasle;
            }
            btn.disabled = true;
            var remarks = $("#remarks").val();
            if (!remarks) {
                //layerError("请先输入备注信息");
                layerMsg('请先输入备注信息');
                btn.disabled = false;
                $("#remarks").focus()
                return false;
            }
            if (remarks.length > 250) {
                layerMsg('备注信息超长了，请限制在250个字符以内!');
                btn.disabled = false;
                $("#remarks").focus()
                return false;
            }
            remarks = encodeURI(remarks);
            if (servicePointId != null && remarks != null) {
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/fi/md/servicePoint/ajax/updateRemark?servicePointId=" + servicePointId + "&remarks=" + (remarks||''),
                    dataType: 'json',
                    success: function (data) {
                        btn.disabled = false;
                        if (ajaxLogout(data)) {
                            return false;
                        }
                        if (data.success) {
                            layerMsg('保存成功');

                        } else {
                            layerError(data.message);
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText, null, "保存备注错误，请重试!");
                        btn.disabled = false;
                    }
                });

            } else {
                layerError("获取保存类容错误", "错误提示");
                return false;
            }

        }

        //查看网店备注历史列表
        function viewRemarkList(servicePointId, servicePointNo, servicePointName) {
            var planIndex = top.layer.open({
                type: 2,
                id: 'layer_planRemarkList_view',
                zIndex: 19891016,
                title: '历史备注',
                content: "${ctx}/fi/md/servicePoint/viewRemarkList?servicePointId=" + (servicePointId || '') + "&servicePointNo=" + (servicePointNo || '') + "&servicePointName=" + (servicePointName || ''),
                // area: ['980px', '640px'],
                area: ['936px', (screen.height / 2) + 'px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function (layero, index) {
                },
                end: function () {
                }
            });
        }

        function showServicePointNo() {
            top.layer.open({
                type: 2,
                id: 'layer_searchServicePointNo',
                zIndex: 19891015,
                title: '查询',
                content: "${ctx}/fi/md/servicePoint/findListByAreaIds",
                area: ['800px', '640px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function (layero, index) {
                }
            });
        }

        function showContactInfo() {
            top.layer.open({
                type: 2,
                id: 'layer_searchServicePointContactInfo',
                zIndex: 19891015,
                title: '查询',
                content: "${ctx}/md/servicepoint/findUserListByContactInfo",
                area: ['800px', '640px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function (layero, index) {
                }
            });
        }

        /**************************************************************************
         身份号码排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
         地址码表示编码对象常住户口所在县(市、旗、区)的行政区划代码。
         出生日期码表示编码对象出生的年、月、日，其中年份用四位数字表示，年、月、日之间不用分隔符。
         顺序码表示同一地址码所标识的区域范围内，对同年、月、日出生的人员编定的顺序号。
         顺序码的奇数分给男性，偶数分给女性。
         校验码是根据前面十七位数字码，按照ISO 7064:1983.MOD 11-2校验码计算出来的检验码。
         15位校验规则 6位地址编码+6位出生日期+3位顺序号
         18位校验规则 6位地址编码+8位出生日期+3位顺序号+1位校验位
         校验位规则     公式:∑(ai×Wi)(mod 11)……………………………………(1)
         公式(1)中：
         i----表示号码字符从右至左包括校验码在内的位置序号；
         ai----表示第i位置上的号码字符值；
         Wi----示第i位置上的加权因子，其数值依据公式Wi=2^(n-1）(mod 11)计算得出。
         i 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1
         Wi 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2 1
         ****************************************************************************/

        /**
         * 身份证城市代码列表
         */
        var aIdentityCode_City = { // 城市代码列表
            11: "北京", 12: "天津", 13: "河北", 14: "山西", 15: "内蒙古", 21: "辽宁", 22: "吉林",
            23: "黑龙江 ", 31: "上海", 32: "江苏", 33: "浙江", 34: "安徽", 35: "福建", 36: "江西",
            37: "山东", 41: "河南", 42: "湖北 ", 43: "湖南", 44: "广东", 45: "广西", 46: "海南",
            50: "重庆", 51: "四川", 52: "贵州", 53: "云南", 54: "西藏 ", 61: "陕西", 62: "甘肃",
            63: "青海", 64: "宁夏", 65: "新疆", 71: "台湾", 81: "香港", 82: "澳门", 91: "国外 "
        };

        //检查号码是否符合规范，包括长度，类型
        function IdentityCode_isCardNo(card) {
            //身份证号码为15位或者18位，15位时全为数字，18位前17位为数字，最后一位是校验位，可能为数字或字符X
            var reg = /(^\d{15}$)|(^\d{17}(\d|X)$)/; // 正则表达式
            if (reg.test(card) === false) {
                return false;
            }
            return true;
        };

        //取身份证前两位，校验省份
        function IdentityCode_checkProvince(card) {
            var province = card.substr(0, 2);
            if (aIdentityCode_City[province] == undefined) {
                return false;
            }
            return true;
        };

        //检查生日是否正确，15位以'19'年份来进行补齐。
        function IdentityCode_checkBirthday(card) {
            var len = card.length;
            //身份证15位时，次序为省（3位）市（3位）年（2位）月（2位）日（2位）校验位（3位），皆为数字
            if (len == '15') {
                var re_fifteen = /^(\d{6})(\d{2})(\d{2})(\d{2})(\d{3})$/;
                var arr_data = card.match(re_fifteen); // 正则取号码内所含出年月日数据
                var year = arr_data[2];
                var month = arr_data[3];
                var day = arr_data[4];
                var birthday = new Date('19' + year + '/' + month + '/' + day);
                return IdentityCode_verifyBirthday('19' + year, month, day, birthday);
            }
            //身份证18位时，次序为省（3位）市（3位）年（4位）月（2位）日（2位）校验位（4位），校验位末尾可能为X
            if (len == '18') {
                var re_eighteen = /^(\d{6})(\d{4})(\d{2})(\d{2})(\d{3})([0-9]|X)$/;
                var arr_data = card.match(re_eighteen); // 正则取号码内所含出年月日数据
                var year = arr_data[2];
                var month = arr_data[3];
                var day = arr_data[4];
                var birthday = new Date(year + '/' + month + '/' + day);
                return IdentityCode_verifyBirthday(year, month, day, birthday);
            }
            return false;
        };

        //校验日期 ，15位以'19'年份来进行补齐。
        function IdentityCode_verifyBirthday(year, month, day, birthday) {
            var now = new Date();
            var now_year = now.getFullYear();
            //年月日是否合理
            if (birthday.getFullYear() == year
                && (birthday.getMonth() + 1) == month
                && birthday.getDate() == day) {
                //判断年份的范围（3岁到150岁之间)
                var time = now_year - year;
                if (time >= 3 && time <= 150) {
                    return true;
                }
                return false;
            }
            return false;
        };

        //校验位的检测
        function IdentityCode_checkParity(card) {
            card = IdentityCode_changeFivteenToEighteen(card); // 15位转18位
            var len = card.length;
            if (len == '18') {
                var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
                var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
                var cardTemp = 0, i, valnum;
                for (i = 0; i < 17; i++) {
                    cardTemp += card.substr(i, 1) * arrInt[i];
                }
                valnum = arrCh[cardTemp % 11];
                if (valnum == card.substr(17, 1)) {
                    return true;
                }
                return false;
            }
            return false;
        };

        //15位转18位身份证号
        function IdentityCode_changeFivteenToEighteen(card) {
            if (card.length == '15') {
                var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
                var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
                var cardTemp = 0, i;
                card = card.substr(0, 6) + '19' + card.substr(6, card.length - 6);
                for (i = 0; i < 17; i++) {
                    cardTemp += card.substr(i, 1) * arrInt[i];
                }
                card += arrCh[cardTemp % 11];
                return card;
            }
            return card;
        };

        /**
         * 身份证号码检验主入口
         * 不符合规则弹出提示错误
         */
        function IdentityCodeValid(card) {
            //var tip = "您输入的身份证号码不正确，请重新输入！";
            var pass = true;
            //是否为空
            if (pass && card === '')
                pass = false;
            //校验长度，类型
            if (pass && IdentityCode_isCardNo(card) === false)
                pass = false;
            //检查省份
            if (pass && IdentityCode_checkProvince(card) === false)
                pass = false;
            //校验生日
            if (pass && IdentityCode_checkBirthday(card) === false)
                pass = false;
            //检验位的检测
            if (pass && IdentityCode_checkParity(card) === false)
                pass = false;
            return pass;
        }

        // 开关控制
        function switchControl(obj) {
            var flag = $("." + obj).val();
            if (flag == 0) {
                $("." + obj).attr("value", 1);
                if (obj == "discountFlag") {
                    $("#finance\\.discount").attr("value", ${fns:getDictLabelFromMS("10","ServicePointDiscount","4")});
                }
            } else {
                $("." + obj).attr("value", 0);
            }
        }

        function switchRemotePriceEnabledFlag() {
            var flag = $("#remotePriceEnabledFlag").val();
            if (flag == 0) {
                $("#remotePriceEnabledFlag").val(1);
            } else {
                $("#remotePriceEnabledFlag").val(0);
            }
            editRemotePriceEnabledFlag();
        }

        function editRemotePriceEnabledFlag() {
            var flag = $("#remotePriceEnabledFlag").val();
            if(flag == 1){
                $("#remotePriceName").show();
            }else {
                $("#remotePriceName").hide();
            }
        }

        function switchInsuranceFlag(obj) {
            var title;
            var insuranceFlag = $("#insuranceFlag").val();
            if(insuranceFlag == 0){
                title = "开启";
                insuranceFlag = 1;
            }else {
                insuranceFlag = 0;
                title = "关闭";
            }
            var id = $("#id").val();

            if(id == null || id == ''){
                if (insuranceFlag == 1) {
                    $("#insuranceFlag").val(1);
                } else {
                    $("#insuranceFlag").val(0);
                }
            }else {
                layer.confirm(
                    '确认要'+ title+'互助基金吗？',
                    {
                        btn: ['确定','取消'], //按钮
                        title:'提示',
                        cancel: function(index, layero){
                            // 右上角叉
                            if ($(obj).attr("class") == 'switch-off') {
                                honeySwitch.showOn(obj);
                            } else {
                                honeySwitch.showOff(obj);
                            }
                        }
                    }, function(index){
                        layer.close(index);//关闭本身
                        $.ajax({
                            url: "${ctx}/fi/md/servicePoint/updateInsuranceFlag?id="+ id +"&insuranceFlag=" + insuranceFlag,
                            success:function (data) {
                                if (data.success) {
                                    // 停用
                                    if (insuranceFlag == 1) {
                                        $("#insuranceFlag").val(1);
                                    } else {
                                        $("#insuranceFlag").val(0);
                                    }

                                } else {
                                    layerError(title + "失败:" + data.message, "错误提示");
                                    // 取消操作
                                    if ($(obj).attr("class") == 'switch-off') {
                                        honeySwitch.showOn(obj);
                                    } else {
                                        honeySwitch.showOff(obj);
                                    }
                                }
                                return false;
                            },
                            error: function (data) {
                                ajaxLogout(data,null,"数据保存错误，请重试!");
                                if ($(obj).attr("class") == 'switch-off') {
                                    honeySwitch.showOn(obj);
                                } else {
                                    honeySwitch.showOff(obj);
                                }
                            },
                        });
                        return false;
                    }, function(){
                        // 取消操作
                        if ($(obj).attr("class") == 'switch-off') {
                            honeySwitch.showOn(obj);
                        } else {
                            honeySwitch.showOff(obj);
                        }
                    });
            }

        }
        function clickFile(index){
            $("#picture"+index).click();
        }

        function clickFilePry(id){
            $("#upload_file_"+id).click();
            return false;
        }

        // 发生改变触发
        function checkAttachment(index) {
            isExecute = true;
            var filepath = $("#picture"+index).val();
            if(Utils.isEmpty(filepath)){
                $("#picture"+index).val("");
                isExecute = false;
                return false;
            }
            var extStart=filepath.lastIndexOf(".");
            var ext=filepath.substring(extStart,filepath.length).toUpperCase();// 后缀
            if(ext != ".BMP" && ext != ".PNG" && ext != ".GIF" && ext != ".JPG" && ext != ".JPEG"){
                layerInfo("图片类型限于bmp,png,gif,jpeg,jpg格式","系统提示");
                $("#picture"+index).val("");
                isExecute = false;
                return false;
            }
            //check size
            var files = document.getElementById("picture"+index).files;
            var fileSize = files[0].size;
            //var size = fileSize / 1024;
            var size = fileSize.toFixed(2);
            if(size > (2*1024*1024)){
                layerInfo("图片不能大于2M","系统提示");
                $("#picture_"+index).val("");
                isExecute = false;
                return false;
            }
            uploadfile($("#picture"+index), "picture"+index,index);
        }

        // 发生改变触发
        function checkAttachmentPry(index) {
            isExecute = true;
            var filepath = $("#upload_file_"+index).val();
            if(Utils.isEmpty(filepath)){
                $("#upload_file_"+index).val("");
                isExecute = false;
                return false;
            }
            var extStart=filepath.lastIndexOf(".");
            var ext=filepath.substring(extStart,filepath.length).toUpperCase();// 后缀
            if(ext != ".BMP" && ext != ".PNG" && ext != ".GIF" && ext != ".JPG" && ext != ".JPEG"){
                layerInfo("图片类型限于bmp,png,gif,jpeg,jpg格式","系统提示");
                $("#upload_file_"+index).val("");
                isExecute = false;
                return false;
            }
            //check size
            var files = document.getElementById("upload_file_" + index).files;
            var fileSize = files[0].size;
            //var size = fileSize / 1024;
            var size = fileSize.toFixed(2);
            if(size > (2*1024*1024)){
                layerInfo("图片不能大于2M","系统提示");
                $("#upload_file_"+index).val("");
                isExecute = false;
                return false;
            }
            uploadfilePry("upload_file_"+index,index);
        }
        
        function changeDepositLevel() {
            var mdDepositLevelId = $("#mdDepositLevel\\.id").val();
            if(mdDepositLevelId ==null || mdDepositLevelId=='0'){
                $("#mdDepositLevel\\.maxAmount").val('0.0 元');
                $("#mdDepositLevel\\.deductPerOrder").val('0.0 元');
                $("#deposit").removeAttr("required");
                $("#deposit").val("");
                $("#deposit").attr("readonly",'readonly');
                $("#deposit").attr("placeholder","无质保等级不填写");
                $("#span_deposit").hide();
                $("#inputForm").validate().element($("#deposit"));
                return false;
            }
            $.ajax({
                url:"${ctx}/fi/md/servicePoint/getDepositLevel?mdDepositLevelId="+mdDepositLevelId,
                success:function (e) {
                    if(e.success == true){
                        $("#mdDepositLevel\\.maxAmount").val(e.data.minAmount + ' - ' + e.data.maxAmount + ' 元');
                        $("#maxAmount").val(e.data.maxAmount);
                        $("#minAmount").val(e.data.minAmount);
                        $("#mdDepositLevel\\.deductPerOrder").val(e.data.deductPerOrder + ' 元');
                        $("#deposit").attr("required",'required');
                        $("#deposit").removeAttr("placeholder");
                        $("#deposit").removeAttr("readonly");
                        $("#span_deposit").show();
                        var deposit = $("#deposit").val();
                        if(deposit != ''){
                            $("#inputForm").validate().element($("#deposit"));
                        }

                    }else if(e.success == false){
                        layerAlert(e.message,"提示");
                    }
                },
                error:function (e) {
                    ajaxLogout(e.responseText,null,"请求切换质保等级失败","错误提示！");
                }
                }
            );
        }
        // 上传
        function uploadfile($obj1, obj2,index) {
            var data = {
                fileName: $obj1.val()
            };
            $.ajaxFileUpload({
                url: '${pageContext.request.contextPath}/servlet/UploadForMD?type=servicePoint&' + (new Date()).getTime(),
                secureuri: false,
                data: {},
                fileElementId: obj2, // file控件id
                dataType: 'json',
                success: function (data, status) {
                    if (data && data.status === 'false'){
                        layerError("文件上传失败，请重试!","错误", true);
                        isExecute = false;
                    } else {
                        $("#attachment"+index).attr("value", data.fileName);
                        var $img = $("[id='viewImg" + index +"']");
                        $img.attr("src","${ctxUpload}/" + data.fileName);
                        $img.attr("data-original","${ctxUpload}/" +data.fileName);
                        $img.attr("title","点击放大图片");
                        $img.addClass("img2");
                        $img.before("<a href='javascript:;' title='点击删除图片'" + " onclick=\"deletePic('" + index + "')\" class=\"upload_warp_img_div_del\"></a>");
                        $("#divImg"+index).removeClass("drag").removeClass("imgOnDarg");
                        $("#divImg"+index).removeAttr("onclick");
                        $("#divImg" + index).addClass("img2");
                        $("#dPicConfig"+index).addClass("iuConfig");
                        $("#picture"+index).val("");
                        imageViewer(index);
                        isExecute = false;
                    }
                },
                error: function (data, status, e) {
                    alert(e);
                }
            });
        }

        // 上传
        function uploadfilePry(fileInputId,index) {
            $.ajaxFileUpload({
                url: '${pageContext.request.contextPath}/servlet/UploadForMD?type=servicePoint&' + (new Date()).getTime(),
                secureuri: false,
                data: {},
                fileElementId: fileInputId, // file控件id
                dataType: 'json',
                success: function (data, status) {
                    if (data && data.status === 'false'){
                        layerError("文件上传失败，请重试!","错误", true);
                        isExecute = false;
                    } else {
                        var $img = $("[id='viewImg_" + index +"']");
                        $img.attr("src","${ctxUpload}/" + data.fileName);
                        $img.attr("data-original","${ctxUpload}/" +data.fileName);
                        $img.attr("title","点击放大图片");
                        $img.addClass("img2");
                        $img.before("<a href='javascript:;' title='点击删除图片'" + " onclick=\"deletePicPry('" + index + "')\" class=\"upload_warp_img_div_del\"></a>");
                        $("#divImg_" + index).removeClass("drag").removeClass("imgOnDarg");
                        $("#divImg_" + index).removeAttr("onclick");
                        $("#divPicConfig_" + index).addClass("iuConfig");
                        $("#divImg_" + index).addClass("img2");
                        $("#upload_file_"+index).val("");
                        $("#pic_info_"+index).val(data.fileName);
                        /*$("#divPicConfig_"+index).addClass("upload_config");*/
                        imageViewerPry();
                        isExecute = false;
                    }
                },
                error: function (data, status, e) {
                    alert(e);
                }
            });
        }

        // 看大图
        function imageViewer(index){
            var viewer = $("#divUploadWarp"+index).viewer('destroy').viewer(
                {
                    url: "data-original",
                    filter:function(image) {
                        if(image.src.lastIndexOf("/service_insert.png")>0){
                            return false;
                        }
                        return true;
                    },
                    viewed: function(image) {
                    },
                    shown:function () {
                        if(this.viewer.index == -1){
                            this.viewer.hide();
                            //$(".viewer-container").removeClass("viewer-in").addClass("viewer-hide");
                        }
                    }
                }
            );
        }


        // 看大图
        function imageViewerPry(){
            var viewer = $("#divUploadWarpPry").viewer('destroy').viewer(
                {
                    url: "data-original",
                    filter:function(image) {
                        if(image.src.lastIndexOf("/service_insert.png")>0){
                            return false;
                        }

                        if(image.src.lastIndexOf("/outCard.png")>0){
                            return false;
                        }

                        if(image.src.lastIndexOf("/inCard.png")>0){
                            return false;
                        }
                        return true;
                    },
                    viewed: function(image) {
                    },
                    shown:function () {
                        if(this.viewer.index == -1){
                            this.viewer.hide();
                            //$(".viewer-container").removeClass("viewer-in").addClass("viewer-hide");
                        }
                    }
                }
            );
        }

        // 删除照片
        function deletePic(index){
            // console.log("删除")
            event.stopPropagation(); //防止 $("#divImg_" + index) 的函数触发
            $("#divImg"+index).attr("onclick","clickFile('"+index+"');");
            var $img = $("[id='viewImg" + index +"']");
            $img.attr("src","${ctxStatic}/images/service_insert.png");
            $img.removeAttr("data-original");
            $img.attr("title","点击上传图片");
            $img.closest(".upload_warp_img_div").addClass("drag");
            $img.prev().remove();
            $("#attachment"+index).attr("value", "");
            $("#dPicConfig"+index).removeClass("iuConfig");
            // $("#divImg" + index).removeClass("img2");
            // $("#viewImg" + index).removeClass("img2");
            imageViewer(index);
            return false;
        }

        //删除照片
        function deletePicPry(index){
            event.stopPropagation(); //防止 $("#divImg_" + index) 的函数触发
            $("#divImg_" + index).attr("onclick","clickFilePry('"+index+"');");
            var $img = $("[id='viewImg_" + index +"']");
            if(index == 0){
                $img.attr("src","${ctxStatic}/images/inCard.png");
            }else if(index == 1){
                $img.attr("src","${ctxStatic}/images/outCard.png");
            }else {
                $img.attr("src","${ctxStatic}/images/service_insert.png");

            }
            $img.removeAttr("data-original");
            $img.attr("title","点击上传图片");
            $img.closest(".upload_warp_img_div").addClass("drag");
            $img.prev().remove();
            $("#pic_info_"+index).val("");
            $("#divPicConfig_"+index).removeClass("iuConfig");
            // $("#divImg_" + index).removeClass("img2");
            // $("#viewImg_" + index).removeClass("img2");
            imageViewerPry();
            return false;
        }


        function cop(){
            var id= $("input[name = 'id']").val();
            if(id == null || id == ''){
                var str= $("input[name = 'primary.idNo']").val();
                document.getElementById("bankOwnerIdNo").value = str;
            }

        }

    </script>
</head>
<body>
<br/>
<sys:message content="${message}"/>
<div style="overflow-y:auto;height: 727px;margin-top: -20px">
    <form:form id="inputForm" modelAttribute="servicePoint" action="" method="post"
               class="form-horizontal">
        <form:hidden path="id"/>
        <form:hidden path="delFlag"/>
        <form:hidden path="address"/>
        <form:hidden path="useDefaultPrice"/>
        <form:hidden path="forTmall"/>
        <form:hidden path="qq"/>
        <%--记得删除attachment1--%>
        <%--<form:hidden path="attachment1"/>--%>
        <form:hidden path="attachment2"/>
        <form:hidden path="attachment3"/>
        <form:hidden path="attachment4"/>
        <form:hidden path="description"/>
        <form:hidden path="scale"/>
        <form:hidden path="longitude"/>
        <form:hidden path="latitude"/>
        <form:hidden path="property"/>
        <form:hidden path="primary.appFlag"/>
        <form:hidden path="primary.attachment"/>
        <form:hidden path="remotePriceFlag"/>

        <c:set var="notNewPoint" value="${servicePoint != null && servicePoint.id != null && servicePoint.id > 0}"/>

        <div id="main" style="padding: 25px;margin-top: -10px;">
            <legend>网点信息
                <div class="line_"></div>
            </legend>
            <div class="flex-container">
                <div class="flex-item-line">
                    <label class="control-label"><span class=" red">*</span>编&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</label>
                    <div class="controls">
                        <form:input path="servicePointNo" htmlEscape="false" maxlength="20" class="required"
                                    cssStyle="width: 247px"/>
                        <c:if test="${!notNewPoint}">
                            <button class="search_button" onclick="showServicePointNo()" type="button">
                                <i class="icon-search"></i>&nbsp;查询
                            </button>
                        </c:if>
                    </div>
                </div>

                <div class="flex-item">
                    <label class="control-label" style="<c:out value="${notNewPoint ? '' : 'width: 90px'}"/>"><span class=" red">*</span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称：</label>
                    <div class="controls" style="<c:out value="${notNewPoint ? 'margin-left: 175px' : 'margin-left: 106px'}"/>">
                        <form:input path="name" htmlEscape="false" maxlength="50" class="required"
                                    cssStyle="width: 247px"/>
                    </div>
                </div>
            </div>

            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span class=" red">*</span>联系电话：</label>
                    <div class="controls">
                        <form:input path="contactInfo1" htmlEscape="false" maxlength="11" class="required mobile"
                                    cssStyle="width: 247px" placeholder="输入11位手机号码"/>
                    </div>
                </div>

                <div class="y_line">
                    <label class="control-label">座&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机：</label>
                    <div class="controls">
                        <form:input path="contactInfo2" htmlEscape="false" maxlength="16" class="phone"
                                    cssStyle="width: 247px;margin-left: -5px;"/>
                    </div>
                </div>
            </div>

            <div class="flex-container">
                <div class="flex-item-line">
                    <label class="control-label"><span class=" red">*</span>地&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;址：</label>
                    <div class="controls" style="width: 80%">
                        <sys:areaselect name="area.id" id="area" value="${servicePoint.area.id}"
                                        labelValue="${servicePoint.area.fullName}" labelName="area.fullName" title=""
                                        mustSelectCounty="true" cssClass="required"></sys:areaselect>
                        <input id="subAddress" name="subAddress" style="width:418px;" type="text" value="${servicePoint.subAddress}"
                               maxlength="100" placeholder="详细地址，如XX大厦1层101室" class="required">
                    </div>
                </div>
            </div>

            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span class=" red">*</span>等&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;级：</label>
                    <div class="controls">
                        <form:select path="level.value" class="input-small required" cssStyle="width: 262px;">
                            <form:option value="" label="请选择"/>
                            <form:options items="${fns:getDictListFromMS('ServicePointLevel')}"
                                          itemLabel="label" itemValue="value" htmlEscape="false"/>
                            <%--切换为微服务--%>
                        </form:select>
                    </div>
                    <form:hidden path="level.label" htmlEscape="false"/>
                </div>
                <div class="y_line">
                    <label class="control-label">分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类：</label>
                    <div class="controls">
                        <div style="margin-left: -3px;margin-top: 3px">
                            <c:choose>
                                <c:when test="${servicePoint.id == null || servicePoint.id ==0}">
                                    <%--<form:radiobutton path="degree" value="10" checked="true"/>试用网点--%>
                                    <input type="radio" id="degree_10" name="degree" value="10" checked/>
                                    <label for="degree_10" name="degree">试用网点</label>&nbsp;&nbsp;
                                    <input type="radio" id="degree_30" name="degree" value="30"/>
                                    <label for="degree_30" name="degree">返现网点</label>&nbsp;&nbsp;
                                    <%--<form:radiobutton path="degree" value="30"/>返现网店--%>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${fns:getDictListFromMS('degreeType')}" var="dict">
                                        <input id="degree${dict.value}" name="degree" type="radio"
                                            <c:out value="${servicePoint.degree == dict.value?'checked':''}"/>
                                               value="${dict.value}">
                                        <label for="degree${dict.value}">${dict.label}</label>&nbsp;&nbsp;
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>

            <form:hidden path="primary.id" />
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span class=" red">*</span>主帐号姓名：</label>
                    <div class="controls">
                        <form:input path="primary.name" htmlEscape="false" maxlength="20" class="required"
                                    cssStyle="width: 248px"/>
                    </div>
                </div>

                <div class="y_line">
                    <label class="control-label"><span class=" red">*</span>主帐号电话：</label>
                    <div class="controls" style="width: 75%;margin-left: 175px">
                        <form:input path="primary.contactInfo" htmlEscape="false"
                                    readonly="${!empty servicePoint.primary.id?'true':'false'}" maxlength="11"
                                    class="required mobile" cssStyle="width: 248px" placeholder="输入11位手机号码"/>
                        <c:if test="${!notNewPoint}">
                            <button class="search_button" onclick="showContactInfo()" type="button">
                                <i class="icon-search"></i>&nbsp;查询
                            </button>
                        </c:if>
                    </div>
                </div>
            </div>

            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span class=" red">*</span>主帐号等级：</label>
                    <div class="controls">
                        <form:select path="primary.level.value" class="input-small required"
                                     disabled="${!empty servicePoint.primary.id?'true':'false'}"
                                     cssStyle="width: 262px;">
                            <form:option value="0" label="请选择"/>
                            <form:options items="${fns:getDictInclueListFromMS('EngineerLevel','1,2,3,4,5')}"
                                          itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                        </form:select>
                    </div>
                </div>

                <div class="y_line">
                    <label class="control-label" style="margin-right: 10px;">派单联系：</label>
                    <div style="margin-top: 3px">
                        <span>
                            <input id="planContactFlag1" name="planContactFlag" style="margin-left: 10px" type="radio" <c:out value="${(servicePoint.planContactFlag eq 0) || (empty servicePoint.planContactFlag) ?'checked=checked':''}" /> value="0" />
                            <label for="planContactFlag1">主帐号</label>
                        </span>
                        <span>
                            <input id="planContactFlag2" name="planContactFlag" style="margin-left: 25px" type="radio" <c:out value="${servicePoint.planContactFlag eq 1?'checked=checked':''}" /> value="1" />
                            <label for="planContactFlag2">子帐号</label>
                        </span>
                    </div>
                </div>
            </div>

            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">身份证号：</label>
                    <div class="controls">
                        <form:input path="primary.idNo"  htmlEscape="false" maxlength="18"
                                    cssStyle="width: 248px" placeholder="主帐号身份证号" onblur="cop()"/>
                        <span id="span_idNo" class="red"></span>
                    </div>
                </div>
            </div>

            <div class="flex-container">
                <div style="width: 100%">
                    <label class="control-label" style="margin-left: -33px">证件照片：</label>
                    <div class="upload_warp" id="divUploadWarpPry">
                        <c:forEach items="${mdEngineerCerts}" var="picRequirement" varStatus="picIndex">
                            <c:set var="isHasValue" value="true" />
                            <c:if test="${servicePoint.primary.engineerCerts!=null && fn:length(servicePoint.primary.engineerCerts) >0}">
                                <c:forEach items="${servicePoint.primary.engineerCerts}" var="picItems">
                                    <c:if test="${picRequirement.no == picItems.no && isHasValue}">
                                        <c:set var="isHasValue" value="false" />
                                        <c:choose>
                                            <c:when test="${picIndex.index == 0}">
                                                <div class="upload_warp_left"  style="margin-left:21px;">
                                                    <div class="upload_warp_img_div img2" id="divImg_${picIndex.index}" data-index="${picIndex.index}">
                                                        <a href='javascript:;' title='点击删除图片' onclick="deletePicPry('${picIndex.index}')" class="upload_warp_img_div_del"></a>
                                                        <img title="点击放大图片" class="img2" id="viewImg_${picIndex.index}"  data-original="${ctxUpload}/${picItems.picUrl}" src="${ctxUpload}/${picItems.picUrl}" onclick="imageViewerPry()"/>
                                                        <div  id="divPicConfig_${picIndex.index}" class="iuConfig" style="margin-top: -28px;font-size:12px;color:#808695;">
                                                                ${picRequirement.picUrl}
                                                        </div>
                                                    </div>
                                                </div>
                                                <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                                <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}"  value="${picItems.picUrl}" type="hidden">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="upload_warp_left">
                                                    <div class="upload_warp_img_div img2" id="divImg_${picIndex.index}" data-index="${picIndex.index}">
                                                        <a href='javascript:;' title='点击删除图片' onclick="deletePicPry('${picIndex.index}')" class="upload_warp_img_div_del"></a>
                                                        <img title="点击放大图片" class="img2" id="viewImg_${picIndex.index}"  data-original="${ctxUpload}/${picItems.picUrl}" src="${ctxUpload}/${picItems.picUrl}" onclick="imageViewerPry()" />
                                                        <div  id="divPicConfig_${picIndex.index}" class="iuConfig" style="margin-top: -28px;font-size:12px;color:#808695;">
                                                                ${picRequirement.picUrl}
                                                        </div>
                                                    </div>
                                                </div>
                                                <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                                <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}"  value="${picItems.picUrl}" type="hidden">
                                            </c:otherwise>
                                        </c:choose>

                                    </c:if>
                                </c:forEach>
                            </c:if>
                            <c:if test="${isHasValue}">
                                <c:choose>
                                    <c:when test="${picIndex.index == 0}">
                                        <div class="upload_warp_left" style="margin-left:21px;">
                                            <div class="upload_warp_img_div drag img2" id="divImg_${picIndex.index}" onclick="clickFilePry('${picIndex.index}')" data-index="${picIndex.index}">
                                                <img title="点击上传图片" id="viewImg_${picIndex.index}" class="img2" src="${ctxStatic}/images/inCard.png" />
                                                <div id="divPicConfig_${picIndex.index}" class="config" style="margin-top: -28px;font-size:12px;color:#808695;">
                                                        ${picRequirement.picUrl}
                                                </div>
                                            </div>
                                        </div>
                                        <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                        <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}" value="" type="hidden">
                                    </c:when>
                                    <c:when test="${picIndex.index == 1}">
                                        <div class="upload_warp_left">
                                            <div class="upload_warp_img_div drag img2" id="divImg_${picIndex.index}" onclick="clickFilePry('${picIndex.index}')" data-index="${picIndex.index}">
                                                <img title="点击上传图片"  id="viewImg_${picIndex.index}" class="img2" src="${ctxStatic}/images/outCard.png" />
                                                <div id="divPicConfig_${picIndex.index}" class="config" style="margin-top: -28px;font-size:12px;color:#808695;">
                                                        ${picRequirement.picUrl}
                                                </div>
                                            </div>
                                        </div>
                                        <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                        <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}" value="" type="hidden">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="upload_warp_left">
                                            <div class="upload_warp_img_div drag img2" id="divImg_${picIndex.index}" onclick="clickFilePry('${picIndex.index}')" data-index="${picIndex.index}">
                                                <img title="点击上传图片" id="viewImg_${picIndex.index}" class="img2" src="${ctxStatic}/images/service_insert.png" />
                                                <div id="divPicConfig_${picIndex.index}" class="config" style="margin-top: -28px;font-size:12px;color:#808695;" >
                                                        ${picRequirement.picUrl}
                                                </div>
                                            </div>
                                        </div>
                                        <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                        <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}" value="" type="hidden">
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:forEach>
                    </div>
                </div>
            </div>

            <legend class="title_legend">控制开关
                <div class="line_"></div>
            </legend>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">网点状态：</label>
                    <div class="controls">
                        <select id="statusValue" name="status.value" class="required input-small" style="width:253px;">
                            <c:forEach items="${fns:getDictExceptListFromMS('service_point_status', '')}"
                                       var="dict"><%--切换为微服务--%>
                                <option value="${dict.value}"
                                        <c:out value="${(servicePoint.status.value.toString() == dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">快可立时效：</label>
                    <div class="controls">
                        <c:set value="${servicePoint.timeLinessFlag}" var="t"/>
                        <shiro:hasPermission name="fi:md:servicepoint:timeliness">
                            <span class="<c:out value="${t == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                                  onclick="switchControl('timeLinessFlag')"></span>
                            <input type="hidden" value="${t}" class="timeLinessFlag" name="timeLinessFlag">
                        </shiro:hasPermission>
                            <%--没有权限不能点击--%>
                        <shiro:lacksPermission name="fi:md:servicepoint:timeliness">
                            <c:choose>
                                <c:when test="${servicePoint.timeLinessFlag eq 1}">
                                    <span class="switch-on switch-disabled" style="zoom: 0.7"></span>
                                    <input type="hidden" value="1" class="timeLinessFlag" name="timeLinessFlag">
                                </c:when>
                                <c:otherwise>
                                    <span class="switch-off switch-disabled" style="zoom: 0.7"></span>
                                    <input type="hidden" value="0" class="timeLinessFlag" name="timeLinessFlag">
                                </c:otherwise>
                            </c:choose>
                        </shiro:lacksPermission>
                        <span class="help-inline" style="margin-top: -13px;">快可立时效奖励</span>
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">好评审核：</label>
                    <div class="controls">
                        <c:set value="${servicePoint.praiseFeeFlag}" var="p"/>
                        <span class="<c:out value="${p == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                              onclick="switchControl('praiseFeeFlag')"></span>
                        <input type="hidden" value="${p}" class="praiseFeeFlag" name="praiseFeeFlag">
                        <span class="help-inline" style="margin-top: -13px;">网点好评单自动审核</span>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">客户时效：</label>
                    <div class="controls">
                        <c:set value="${servicePoint.customerTimeLinessFlag}" var="c"/>
                        <span class="<c:out value="${c == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                              onclick="switchControl('customerTimeLinessFlag')"></span>
                        <input type="hidden" value="${c}" class="customerTimeLinessFlag" name="customerTimeLinessFlag">
                        <span class="help-inline" style="margin-top: -13px;">客户时效奖励</span>
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">互助基金：</label>
                    <div class="controls" readonly="readonly">
                        <c:set value="${servicePoint.insuranceFlag}" var="f"/>
                        <shiro:hasPermission name="fi:md:servicepoint:insurance">
                            <span class="<c:out value="${f == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                                  onclick="switchInsuranceFlag(this)"></span>
                        </shiro:hasPermission>
                        <shiro:lacksPermission name="fi:md:servicepoint:insurance">
                            <span class="<c:out value="${f == 1 ? 'switch-on' : 'switch-off'}"/> switch-disabled"  style="zoom: 0.7"></span>
                        </shiro:lacksPermission>
                        <input type="hidden" value="${f}" class="insuranceFlag" name="insuranceFlag" id="insuranceFlag">
                        <span class="help-inline" style="margin-top: -13px;width: 200px">完工后扣除,返现网点开启无效</span>

                    </div>
                </div>

                <div class="y_line">
                    <label class="control-label">手机接单：</label>
                    <div class="controls">
                        <c:set value="${servicePoint.appFlag}" var="appFlag"/>
                        <shiro:hasPermission name="fi:md:servicepoint:appFlag">
                          <span class="<c:out value="${appFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                                onclick="switchControl('appFlag')"></span>
                            <input type="hidden" value="${appFlag}" class="appFlag" name="appFlag">
                        </shiro:hasPermission>
                            <%--没有权限不能点击--%>
                        <shiro:lacksPermission name="fi:md:servicepoint:appFlag">
                            <c:choose>
                                <c:when test="${servicePoint.appFlag eq 1}">
                                    <span class="switch-on switch-disabled" style="zoom: 0.7"></span>
                                    <input type="hidden" value="1" class="appFlag" name="appFlag">
                                </c:when>
                                <c:otherwise>
                                    <span class="switch-off switch-disabled" style="zoom: 0.7"></span>
                                    <input type="hidden" value="0" class="appFlag" name="appFlag">
                                </c:otherwise>
                            </c:choose>
                        </shiro:lacksPermission>
                        <span class="help-inline" style="margin-top: -13px;">网点手机接单权限</span>

                    </div>
                </div>
            </div>

            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label" style="margin-right: 10px;">短信通知：</label>

                    <div class="controls">
                        <c:set value="${servicePoint.shortMessageFlag}" var="shortMessageFlag"/>
                        <span class="<c:out value="${shortMessageFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                              onclick="switchControl('shortMessageFlag')"></span>
                        <input type="hidden" value="${shortMessageFlag}" class="shortMessageFlag" name="shortMessageFlag">
                        <span class="help-inline" style="margin-top: -13px;">短信通知主帐号</span>
                    </div>
                </div>

                <shiro:hasPermission name="fi:md:servicepoint:autocomplete">
                    <div class="y_line">
                        <label class="control-label" style="margin-right: 10px;">自动完工：</label>
                        <div class="controls">
                            <c:set value="${servicePoint.autoCompleteOrder}" var="autoCompleteOrder"/>
                            <span class="<c:out value="${autoCompleteOrder == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                                  onclick="switchControl('autoCompleteOrder')"></span>
                            <input type="hidden" value="${autoCompleteOrder}" class="autoCompleteOrder" name="autoCompleteOrder">
                            <span class="help-inline" style="margin-top: -13px;">师傅APP上操作完工</span>
                        </div>
                    </div>
                </shiro:hasPermission>
                <shiro:lacksPermission name="fi:md:servicepoint:autocomplete">
                    <div class="y_line" >
                        <label class="control-label" style="margin-right: 10px;">自动完工：</label>
                        <div class="controls">
                            <c:set value="${servicePoint.autoCompleteOrder}" var="autoCompleteOrder"/>
                            <span class="<c:out value="${autoCompleteOrder == 1 ? 'switch-on' : 'switch-off'} switch-disabled"/>" style="zoom: 0.7"
                                  onclick="switchControl('autoCompleteOrder')"></span>
                            <input type="hidden" value="${autoCompleteOrder}" class="autoCompleteOrder" name="autoCompleteOrder">
                            <span class="help-inline" style="margin-top: -13px;">师傅APP上操作完工</span>
                        </div>
                    </div>
                </shiro:lacksPermission>
            </div>

            <div class="flex-container">
                <div class="x_line">
                    <div style="width: 70px;float: left;margin-left: 90px;">
                        <label>合作条款：</label>
                    </div>
                    <div class="controls">
                        <form:hidden path="appInsuranceFlag"/>
                        <c:choose>
                            <c:when test="${servicePoint.appInsuranceFlag eq 0}">
                                <form:radiobutton path="appInsuranceFlag" value="0" disabled="true"></form:radiobutton>
                                未阅读
                            </c:when>
                            <c:when test="${servicePoint.appInsuranceFlag eq 10}">
                                <form:radiobutton path="appInsuranceFlag" value="10" disabled="true"></form:radiobutton>
                                同意
                            </c:when>
                            <c:when test="${servicePoint.appInsuranceFlag eq 20}">
                                <form:radiobutton path="appInsuranceFlag" value="20" r="true"></form:radiobutton>
                                不同意
                            </c:when>
                        </c:choose>
                    </div>
                </div>
            </div>


            <legend class="title_legend">签约信息
                <div class="line_"></div>
            </legend>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">签&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;约：</label>
                    <div class="controls">
                        <form:radiobuttons path="signFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label"
                                           itemValue="value" htmlEscape="false" class="required"/><%--切换为微服务--%>
                        <input id="contractDate" name="contractDate" type="text" readonly="readonly"
                               style="width:166px;margin-left:4px"
                               maxlength="20" class="input-small Wdate"
                               value="<fmt:formatDate value='${servicePoint.contractDate}' pattern='yyyy-MM-dd'/>"
                               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
                    </div>
                </div>

                <div class="y_line">
                    <label class="control-label"><span class=" red">*</span>网点开发：</label>
                    <div class="controls">
                        <form:input path="developer" htmlEscape="false" maxlength="20" cssClass="required"
                                    cssStyle="width: 240px"/>

                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</label>
                    <div class="controls">
                        <div style="width: 785px">
                            <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"
                                           cssStyle="width: 693px;float: left;"/>
                            <c:if test="${notNewPoint}">
                                <input id="oldRemarks" class="btn btn-small" type="button" value="历史备注" onclick="viewRemarkList('${servicePoint.id}','${servicePoint.servicePointNo}','${servicePoint.name}');" style="float: left;margin-left: 5px;margin-bottom: 5px;"/>
                                <input id="oldSave" class="btn btn-small btn-primary" type="button" value="保 存" onclick="saveServicePointRemark('${servicePoint.id}',this);" style="float: left;width: 70px;margin-top: 4px;margin-left: 5px;"/>&nbsp;
                            </c:if>
                        </div>

                    </div>

                </div>
            </div>

            <div style="height: 110px;">
                <div style="display: inline-block;">
                    <shiro:hasPermission name="fi:md:servicepoint:debts">
                        <div class="flex-container">
                            <div class="x_line" style="width: 108%">
                                <label class="control-label">欠款金额：</label>
                                <div class="controls">
                                    <form:input path="finance.debtsAmount" htmlEscape="false" maxlength="20"
                                                cssStyle="width: 215px;"/>
                                    <span class="add-on" style="margin-left: -5px;border-radius: 0 4px 4px 0;">元</span>
                                </div>
                            </div>
                        </div>
                        <div class="flex-container" style="width: 50%">
                            <div class="x_line">
                                <label class="control-label">欠款描述：</label>
                                <div class="controls">
                                    <form:textarea path="finance.debtsDescrption" htmlEscape="false" rows="3" maxlength="150"
                                                   class="input-xlarge" cssStyle="width: 253px;"/>
                                </div>
                            </div>
                        </div>
                    </shiro:hasPermission>
                    <shiro:lacksPermission name="fi:md:servicepoint:debts">
                        <form:hidden path="finance.debtsAmount"></form:hidden>
                        <form:hidden path="finance.debtsDescrption"></form:hidden>
                    </shiro:lacksPermission>
                </div>
                <div style="width: 50%;float: right;height: 100px;margin-right: 40px;display: inline-block;">
                    <label class="control-label">证件照片：</label>
                    <div class="controls">
                        <c:choose>
                            <c:when test="${servicePoint.attachment5 != null && servicePoint.attachment5 ne ''}">
                                <div class="upload_warp" id="divUploadWarp5">
                                    <div class="upload_warp_left" data-code="" data-index="">
                                        <div class="upload_warp_img_div drag img2" id="divImg5">
                                            <a href='javascript:;' title='点击删除图片' onclick="deletePic(5)" class="upload_warp_img_div_del"></a>
                                            <img title="点击放大图片" class="img2" id="viewImg5"  data-original="${ctxUpload}/${servicePoint.attachment5}" src="${ctxUpload}/${servicePoint.attachment5}" onclick="imageViewer(5)"/>
                                            <div id="dPicConfig5" class="iuConfig" style="margin-top: -30px;font-size:12px;color:#808695;" >
                                                营业执照
                                            </div>
                                        </div>
                                    </div>
                                    <input id="picture5" type="file" class="hero-unit" style="display: none" size="20" name="pictureFive"
                                           onchange="checkAttachment(5)"/>
                                    <input style="display: none" name="attachment5" id="attachment5" value="${servicePoint.attachment5}">
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="upload_warp" id="divUploadWarp5">
                                    <div class="upload_warp_left" data-code="" data-index="">
                                        <div class="upload_warp_img_div drag img2" id="divImg5" onclick="clickFile(5)">
                                            <img id="viewImg5" title="点击上传图片" class="img2"  src="${ctxStatic}/images/service_insert.png" />
                                            <div id="dPicConfig5" class="config" style="margin-top: -30px;font-size:12px;color:#808695;" >
                                                营业执照
                                            </div>
                                        </div>
                                    </div>
                                    <input id="picture5" type="file" class="hero-unit" style="display: none" size="20" name="pictureFive"
                                           onchange="checkAttachment(5)"/>
                                    <input style="display: none" name="attachment5" id="attachment5" value="${servicePoint.attachment5}">
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${servicePoint.attachment1 != null && servicePoint.attachment1 ne ''}">
                                <div class="upload_warp" id="divUploadWarp1">
                                    <div class="upload_warp_left" data-code="" data-index="">
                                        <div class="upload_warp_img_div drag img2" id="divImg1">
                                            <a href='javascript:;' title='点击删除图片' onclick="deletePic(1)" class="upload_warp_img_div_del"></a>
                                            <img title="点击放大图片" class="img2" id="viewImg1"  data-original="${ctxUpload}/${servicePoint.attachment1}" src="${ctxUpload}/${servicePoint.attachment1}" onclick="imageViewer(1)"/>
                                            <div id="dPicConfig1" class="iuConfig" style="margin-top: -30px;font-size:12px;color:#808695;" >
                                                合同
                                            </div>
                                        </div>
                                    </div>
                                    <input id="picture1" type="file" class="hero-unit" style="display: none" size="20" name="pictureOne"
                                           onchange="checkAttachment(1)"/>
                                    <input style="display: none" name="attachment1" id="attachment1" value="${servicePoint.attachment1}">
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="upload_warp" id="divUploadWarp1">
                                    <div class="upload_warp_left" data-code="" data-index="">
                                        <div class="upload_warp_img_div drag img2" id="divImg1" onclick="clickFile(1)">
                                            <img id="viewImg1" title="点击上传图片" class="img2"   src="${ctxStatic}/images/service_insert.png" />
                                            <div id="dPicConfig1" class="config" style="margin-top: -30px;font-size:12px;color:#808695;" >
                                                合同
                                            </div>
                                        </div>
                                    </div>
                                    <input id="picture1" type="file" class="hero-unit" style="display: none" size="20" name="pictureOne"
                                           onchange="checkAttachment(1)"/>
                                    <input style="display: none" name="attachment1" id="attachment1" value="${servicePoint.attachment1}">
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <legend class="title_legend">质保金
                <div class="line_" style="width: 50px;"></div>
            </legend>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span class=" red">*</span>质保等级：</label>
                    <div class="controls">
                        <shiro:hasPermission name="fi:md:servicepoint:depositLevel">
                            <select id="mdDepositLevel.id" name="mdDepositLevel.id" class="required input-small" style="width:253px;" onchange="changeDepositLevel()">
                                <option value="0">无</option>
                                <c:forEach items="${depositLevelList}" var="depositLevel"><%--切换为微服务--%>
                                    <option value="${depositLevel.id}"
                                            <c:out value="${(servicePoint.mdDepositLevel.id == depositLevel.id)?'selected=selected':''}"/>>${depositLevel.name}</option>
                                </c:forEach>
                            </select>
                        </shiro:hasPermission>
                        <shiro:lacksPermission name="fi:md:servicepoint:depositLevel">
                            <c:choose>
                                <c:when test="${servicePoint.mdDepositLevel.id != null && servicePoint.mdDepositLevel.id != 0}">
                                    <form:hidden path="mdDepositLevel.id" id="mdDepositLevel.id" ></form:hidden>
                                    <form:input path="mdDepositLevel.name" id="mdDepositLevel.name" readonly="true" style="width: 240px;"></form:input>
                                </c:when>
                                <c:otherwise>
                                    <input type="text" readonly="true" id="mdDepositLevel.name" name="mdDepositLevel.name" value="无" style="width: 240px;" />
                                    <input type="hidden" readonly="true" id="mdDepositLevel.id" name="mdDepositLevel.id" value="0" />
                                </c:otherwise>
                            </c:choose>

                        </shiro:lacksPermission>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label"><span class=" red">*</span>缴费金额：</label>
                    <div class="controls">
                        <c:choose>
                            <c:when test="${servicePoint.mdDepositLevel.id == null || servicePoint.mdDepositLevel.id == 0}">
                                <input id="mdDepositLevel.maxAmount" name="mdDepositLevel.maxAmount" style="width: 240px" type="text" class="uneditable-input valid" disabled="disabled" value="0.0 元" aria-invalid="false">
                            </c:when>
                            <c:otherwise>
                                <input id="mdDepositLevel.maxAmount" name="mdDepositLevel.maxAmount" style="width: 240px" type="text" class="uneditable-input valid" disabled="disabled" value="${servicePoint.mdDepositLevel.minAmount} - ${servicePoint.mdDepositLevel.maxAmount} 元" aria-invalid="false">
                            </c:otherwise>
                        </c:choose>

                                <input id="maxAmount" type="hidden" disabled="disabled" value="${servicePoint.mdDepositLevel.maxAmount}">
                                <input id="minAmount" type="hidden" disabled="disabled" value="${servicePoint.mdDepositLevel.minAmount}">
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span id="span_deposit" class=" red">*</span>应缴金额：</label>
                    <div class="controls deposit">
                        <shiro:hasPermission name="fi:md:servicepoint:depositLevel">
                            <c:choose>
                                <c:when test="${servicePoint.mdDepositLevel.id == null || servicePoint.mdDepositLevel.id == 0}">
                                    <input id="deposit" name="deposit" style="width: 215px" type="text" readonly="readonly" value=""  placeholder="无质保等级不填写">
                                </c:when>
                                <c:otherwise>
                                    <input id="deposit" name="deposit" style="width: 215px" type="text"  value="${servicePoint.deposit}"  placeholder="无质保等级不填写" required="required">
                                </c:otherwise>
                            </c:choose>
                        </shiro:hasPermission>
                        <shiro:lacksPermission name="fi:md:servicepoint:depositLevel">
                            <c:choose>
                                <c:when test="${servicePoint.mdDepositLevel.id == null || servicePoint.mdDepositLevel.id == 0}">
                                    <input  name="deposit" style="width: 215px" type="text" readonly="readonly" value=""  placeholder="无质保等级不填写">
                                </c:when>
                                <c:otherwise>
                                    <input  name="deposit" style="width: 215px" type="text" readonly="readonly" value="${servicePoint.deposit}">
                                </c:otherwise>
                            </c:choose>

                        </shiro:lacksPermission>
                        <span class="add-on" style="margin-left: -5px;border-radius: 0 4px 4px 0;">元</span>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">已缴金额：</label>
                    <div class="controls">
                        <input id="finance.deposit" name="finance.deposit" style="width: 240px" type="text" class="uneditable-input valid"  disabled="disabled" value="${servicePoint.finance.deposit} 元" aria-invalid="false">
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">每单扣除：</label>
                    <div class="controls">
                        <input id="mdDepositLevel.deductPerOrder" name="mdDepositLevel.deductPerOrder" style="width: 240px" type="text" class="uneditable-input valid"  disabled="disabled" value="${servicePoint.mdDepositLevel.deductPerOrder == null ? 0.0:servicePoint.mdDepositLevel.deductPerOrder} 元" aria-invalid="false">
                    </div>
                </div>

                <div class="y_line">
                    <label class="control-label">完工扣款：</label>
                    <div class="controls">
                        <c:set value="${servicePoint.depositFromOrderFlag}" var="d"/>
                        <span class="<c:out value="${d == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                              onclick="switchControl('depositFromOrderFlag')"></span>
                        <input type="hidden" value="${d}" class="depositFromOrderFlag" name="depositFromOrderFlag">
                        <span class="help-inline" style="margin-top: -10px;width: 213px">完工后扣除质保金额,返现网点开启无效</span>
                    </div>
                </div>
            </div>
            <legend class="title_legend">结算信息
                <div class="line_"></div>
            </legend>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">结算标准：</label>
                    <div class="controls">
                        <c:if test="${servicePoint.id == null}">
                            <select id="useDefaultPrice1" name="useDefaultPrice" class="required input-small"
                                    style="width:253px;">
                                <c:forEach items="${fns:getDictInclueListFromMS('PriceType','10,20,30')}" var="dict">
                                    <option value="${dict.value}"
                                            <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                                </c:forEach>
                            </select>
                        </c:if>
                        <c:if test="${servicePoint.id != null}">
                            <shiro:lacksPermission name="fi:md:servicepoint:defaultpriceedit">
                                <select id="useDefaultPrice1" name="useDefaultPrice" disabled="disabled"
                                        class="required input-small" style="width:253px;">
                                    <c:forEach items="${fns:getDictInclueListFromMS('PriceType','10,20,30')}" var="dict">
                                        <option value="${dict.value}"
                                                <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                                    </c:forEach>
                                </select>
                            </shiro:lacksPermission>
                            <shiro:hasPermission name="fi:md:servicepoint:defaultpriceedit">
                                <select id="useDefaultPrice1" name="useDefaultPrice" class="required input-small"
                                        style="width:253px;">
                                    <c:forEach items="${fns:getDictInclueListFromMS('PriceType','10,20,30')}" var="dict">
                                        <option value="${dict.value}"
                                                <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}"/>>${dict.label}</option>
                                    </c:forEach>
                                </select>
                            </shiro:hasPermission>
                        </c:if>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">支付异常：</label>
                    <div class="controls">
                        <form:select path="finance.bankIssue.value" id="financeBankIssueSelect" class="input-small"
                                     cssStyle="width: 253px;">
                            <form:option value="0" label="无"/>
                            <form:options items="${fns:getDictListFromMS('BankIssueType')}" itemLabel="label"
                                          itemValue="value" htmlEscape="false"/>
                        </form:select>
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">偏远价格：</label>
                    <div class="controls">
                        <c:set value="${servicePoint.remotePriceEnabledFlag}" var="remotePriceEnabledFlag"/>
                        <c:if test="${servicePoint.id == null}">
                        <span class="<c:out value="${remotePriceEnabledFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                              onclick="switchRemotePriceEnabledFlag()"></span>
                            <input type="hidden" value="${remotePriceEnabledFlag}" class="remotePriceEnabledFlag" name="remotePriceEnabledFlag" id="remotePriceEnabledFlag">
                            <input type="text" value="标准价格" id="remotePriceName" disabled="disabled" style="margin-left: 10px;width: 189px;margin-top: -15px;">
                        </c:if>
                        <c:if test="${servicePoint.id != null}">
                            <span class="<c:out value="${remotePriceEnabledFlag == 1 ? 'switch-on' : 'switch-off'}"/> switch-disabled" style="zoom: 0.7"></span>
                            <input type="hidden" value="${remotePriceEnabledFlag}" class="remotePriceEnabledFlag" name="remotePriceEnabledFlag" id="remotePriceEnabledFlag">
                            <c:if test="${remotePriceEnabledFlag == 1}">
                                <c:choose>
                                    <c:when test="${servicePoint.remotePriceFlag == 1}">
                                        <input type="text" value="自定义价格" id="remotePriceName" disabled="disabled" style="margin-left: 10px;width: 189px;margin-top: -15px;">
                                    </c:when>
                                    <c:otherwise>
                                        <input type="text" value="标准价格" id="remotePriceName" disabled="disabled" style="margin-left: 10px;width: 189px;margin-top: -15px;">
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:if>
                    </div>
                    <c:if test="${servicePoint.id != null}">
                        <div style="padding-top:6px;margin-left: 89px;color: #aaaaaa;width: 295px;">
                            修改偏远价格请联系管理员
                        </div>
                    </c:if>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span class=" red">*</span>结算方式：</label>
                    <div class="controls paymentType">
                        <form:select path="finance.paymentType.value" class="required input-small"
                                     cssStyle="width: 253px;">
                            <form:option value="" label="请选择"/>
                            <form:options items="${fns:getDictExceptListFromMS('PaymentType', '30')}" itemLabel="label"
                                          itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                        </form:select>
                    </div>
                    <form:hidden path="finance.paymentType.label" htmlEscape="false"/>
                </div>

                <div class="y_line">
                    <label class="control-label">结算途径：</label>
                    <div class="controls">
                        <form:select path="paymentChannel" class="input-small" cssStyle="width: 253px;">
                            <form:options items="${fns:getDictListFromMS('PaymentChannel')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
                        </form:select>
                    </div>
                </div>
                <div class="y_line" hidden="hidden">
                    <label class="control-label">自动结算：</label>
                    <div class="controls" style="margin-top: 3px;">
                            <label><input type="radio" value="1" name="autoPaymentFlag" >是</label>
                            <label><input type="radio" value="0" name="autoPaymentFlag" checked="checked" style="margin-left: 0px">否</label>
                    </div>
                </div>
            </div>

            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span class=" red">*</span>扣&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;点：</label>
                    <div class="controls">
                            <c:set value="${servicePoint.finance.discountFlag}" var="discountFlag"/>
                            <span class="<c:out value="${discountFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                                  onclick="switchControl('discountFlag')"></span>
                            <input type="hidden" value="${discountFlag}" class="discountFlag" name="finance.discountFlag">
                        <div class="input-append" style="margin-left: 10px;margin-top: -10px">
                            <form:input path="finance.discount" htmlEscape="false" maxlength="7" min="0.0" max="100.0"
                                        class="required number" cssStyle="width: 163px;"/>
                            <span class="add-on">%</span>
                        </div>
                    </div>
                </div>


                <div class="y_line">
                    <label class="control-label">开&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;票：</label>
                    <div class="controls">
                        <c:set value="${servicePoint.finance.invoiceFlag}" var="invoiceFlag"/>
                        <span class="<c:out value="${invoiceFlag == 1 ? 'switch-on' : 'switch-off'}"/>" style="zoom: 0.7"
                              onclick="switchControl('invoiceFlag')"></span>
                        <input type="hidden" value="${invoiceFlag}" class="invoiceFlag" name="finance.invoiceFlag">
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">开户银行：</label>
                    <div class="controls">
                        <form:select path="finance.bank.value" class="required input-small" cssStyle="width: 253px;">
                            <form:option value="0" label="请选择"/>
                            <form:options items="${fns:getDictListFromMS('banktype')}" itemLabel="label"
                                          itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                        </form:select>
                    </div>
                    <form:hidden path="finance.bank.label" htmlEscape="false"/>
                </div>
                <div class="y_line">
                    <label class="control-label">分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;行：</label>
                    <div class="controls">
                        <form:input path="finance.branch" htmlEscape="false" maxlength="50" cssStyle="width: 240px"/>
                        <span id="span_branch" class="red"></span>
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">账&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</label>
                    <div class="controls">
                        <form:input path="finance.bankNo" htmlEscape="false" maxlength="50" class=""
                                    cssStyle="width: 240px"/>
                        <span id="span_bankNo" class="red"></span>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">开户人：</label>
                    <div class="controls">
                        <form:input path="finance.bankOwner" htmlEscape="false" maxlength="50" class=""
                                    cssStyle="width: 240px"/>
                        <span id="span_bankOwner" class=" red"></span>
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">身份证号：</label>
                    <div class="controls">
                        <form:input path="bankOwnerIdNo" htmlEscape="false" maxlength="18"
                                    cssStyle="width: 240px" placeholder="身份证号含字母需大写"/>
                        <span id="span_bankOwnerIdNo" class="red"></span>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">联系电话：</label>
                    <div class="controls">
                        <form:input path="bankOwnerPhone" htmlEscape="false" maxlength="11" class="mobile"
                                    cssStyle="width: 240px" placeholder="开户预留电话"/>
                        <span id="span_bankOwnerPhone" class="red"></span>
                    </div>
                </div>
            </div>


            <legend class="title_legend">工单信息
                <div class="line_"></div>
            </legend>

            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label"><span class=" red">*</span>网点容量：</label>
                    <div class="controls">
                        <form:input path="capacity" htmlEscape="false" maxlength="5" onkeyup="if( /[^0-9]/g.test(this.value)){this.value='';}" class="required number" placeholder="输入大于0的整数"
                                    cssStyle="width: 240px"/>
                    </div>
                    <div style="padding-top:6px;margin-left: 89px;color: #aaaaaa;width: 295px;">
                        未完工单数量超过工单容量则不再派单给该网点
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">派单数：</label>
                    <div class="controls">
                        <form:input path="planCount" type="number" readonly="true" htmlEscape="false"
                                    class="uneditable-input" cssStyle="width: 240px"/>
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">未完工单：</label>
                    <div class="controls">
                        <form:input path="unfinishedOrderCount" type="number" readonly="true" htmlEscape="false"
                                    class="uneditable-input" cssStyle="width: 240px"/>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">完成单数：</label>
                    <div class="controls">
                        <form:input path="orderCount" type="number" readonly="true" htmlEscape="false"
                                    class="uneditable-input" cssStyle="width: 240px"/>
                    </div>
                </div>
            </div>
            <div class="flex-container">
                <div class="x_line">
                    <label class="control-label">违约单数：</label>
                    <div class="controls">
                        <form:input path="breakCount" type="number" readonly="true" htmlEscape="false"
                                    class="uneditable-input" cssStyle="width: 240px"/>
                    </div>
                </div>
                <div class="y_line">
                    <label class="control-label">用户评价：</label>
                    <div class="controls">
                        <form:input path="grade" type="number" readonly="true" htmlEscape="false"
                                    class="uneditable-input" cssStyle="width: 240px"/>
                    </div>
                </div>
            </div>

            <c:if test="${notNewPoint}">
                <c:if test="${servicePoint.attachment3 != null && servicePoint.attachment3 ne '' || servicePoint.attachment4 != null && servicePoint.attachment4 ne ''}">
                    <legend class="title_legend">其他证件
                        <div class="line_"></div>
                    </legend>
                    <div class="flex-container">
                        <div class="x_line">
                            <label class="control-label">上传照片：</label>
                            <c:if test="${servicePoint.attachment3 != null && servicePoint.attachment3 ne ''}">
                                <div class="controls">
                                    <div class="upload_warp">
                                        <div class="upload_warp_left" data-code="" data-index="">
                                            <div class="upload_warp_img_div drag jq2">
                                                <img title="点击放大图片" data-original="${ctxUpload}/${servicePoint.attachment3}" src="${ctxUpload}/${servicePoint.attachment3}" style="height: 70px"/>
                                                <div class="font_style">其他证件1</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                            <c:if test="${servicePoint.attachment4 != null && servicePoint.attachment4 ne ''}">
                                <div class="controls">
                                    <div class="upload_warp">
                                        <div class="upload_warp_left" data-code="" data-index="">
                                            <div class="upload_warp_img_div drag jq2">
                                                <img title="点击放大图片" data-original="${ctxUpload}/${servicePoint.attachment4}" src="${ctxUpload}/${servicePoint.attachment4}" style="height: 70px"/>
                                                <div class="font_style">其他证件2</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                        </div>
                    </div>
                </c:if>
            </c:if>

            <c:if test="${notNewPoint}">
                <legend class="title_legend">其他
                    <div style="border-bottom: 3.5px solid #0096DA;width: 35px;border-radius: 10px;"></div>
                </legend>
                <div class="flex-container">
                    <div class="x_line">
                        <label class="control-label">创建人：</label>
                        <div class="controls">
                            <form:input path="createBy.name" readonly="true" htmlEscape="false"
                                        class="uneditable-input" cssStyle="width: 240px"/>
                        </div>
                    </div>
                    <div class="y_line">
                        <label class="control-label">创建时间：</label>
                        <div class="controls">
                            <input style="width:240px;" type="text" value="<fmt:formatDate value="${servicePoint.createDate}" type="both" dateStyle="full"/>" readonly="readonly">
                        </div>
                    </div>
                </div>
                <div class="flex-container">
                    <div class="x_line">
                        <label class="control-label">修改人：</label>
                        <div class="controls">
                            <form:input path="updateBy.name" readonly="true" htmlEscape="false"
                                        class="uneditable-input" cssStyle="width: 240px"/>
                        </div>
                    </div>
                    <div class="y_line">
                        <label class="control-label">修改时间：</label>
                        <div class="controls">
                            <input style="width:240px;" type="text" value="<fmt:formatDate value="${servicePoint.updateDate}" type="both" dateStyle="full"/>" readonly="readonly">
                        </div>
                    </div>
                </div>
            </c:if>

                <div class="flex-container">
                    <div>
                        <legend class="title_legend">服务区域
                            <div class="line_"></div>
                        </legend>

                        <div class="">
                            <div style="margin-top:3px;float:left;height:400px;overflow:auto; width: 340px;padding-left: 62px;">
                                <ul class="ztree" id="areaTree"></ul>
                            </div>
                            <form:hidden path="areaIds" class="required"/>
                            <c:if test="${notNewPoint}">
                                <shiro:hasPermission name="fi:md:servicepoint:edit">
                                    <input id="btnSubmitArea" class="btn btn-small btn-primary" type="button" value="保 存" style="margin-top: 10px;width: 60px;height: 30px;margin-left: 65px;"/>&nbsp;
                                </shiro:hasPermission>
                            </c:if>
                        </div>
                    </div>
                    <div style="margin-left: 60px;">
                        <legend class="title_legend">产品信息
                            <div class="line_"></div>
                        </legend>
                        <div class="">
                            <div style="margin-top:3px;float:left;height:400px;overflow:auto; width: 369px;padding-left: 62px;">
                                <ul class="ztree" id="productTree"></ul>
                            </div>
                            <form:hidden path="productIds"/>
                            <c:if test="${notNewPoint}">
                                <shiro:hasPermission name="fi:md:servicepoint:edit">
                                    <input id="btnSubmitProduct" class="btn btn-small btn-primary" type="button" value="保 存" style="margin-top: 10px;width: 60px;height: 30px;margin-left: 65px;"/>&nbsp;
                                </shiro:hasPermission>
                            </c:if>
                        </div>
                    </div>
                </div>
        </div>

        <div id="editBtn">
            <shiro:hasPermission name="fi:md:servicepoint:edit">
                <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="margin-top: 10px;width: 85px;height: 35px;"/>&nbsp;
            </shiro:hasPermission>
            <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()"
                   style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
        </div>
    </form:form>
</div>

<script type="text/javascript">


    function cancel() {
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
    }

    function checkMobile() {
        $.ajax({
            type: "post",
            url: '${ctx}/sys/user/checkMobile',
            async: false,
            data: {
                mobile: $("[id='primary.contactInfo']").val(),
                expectType: "engineer",
                expectId: '${servicePoint.primary.id}'
            },
            dataType: "html",
            success: function (data, type) {
                return data == "true" ? true : false;
            }
        });
    }

    function initCheckBox() {
        var ids;
        <c:if test="${not empty servicePoint.productIds && !(servicePoint.productIds eq null)}">
            ids = "${servicePoint.productIds}".split(",");
            $("#p_count").text(ids.length);
            if (ids.length > 0) {
                var n = 0;
                for (var i in ids) {
                    var productId = ids[i];
                    var query = ":checkbox[name='products'][value=" + productId + "]";
                    $(query).attr("checked", "checked");
                }
            }
        </c:if>
    }

    initCheckBox();

    $(function () {
        <c:if test="${servicePoint.id == null}">
            $("#servicePointNo").focus();

            jQuery.validator.addMethod("checkEngineerMobile", function (value, element) {
                return checkMobile();
            }, '手机号码已被注册');

            $("#capacity").val(${fns:getDictLabelFromMS("1", "ServicePointCapacity", "100")});
        </c:if>
        <c:if test="${servicePoint.id ne null}">
            <shiro:lacksPermission name="fi:md:servicepoint:statuspaused">
                $("#statusValue option[value=20]").attr("disabled", "disabled");
            </shiro:lacksPermission>
            <shiro:lacksPermission name="fi:md:servicepoint:statusblacklist">
                $("#statusValue option[value=100]").attr("disabled", "disabled");
            </shiro:lacksPermission>
            $(":radio[name='autoPaymentFlag'][value='" + ${servicePoint.autoPaymentFlag} + "']").attr("checked", "checked");
        </c:if>

        $("[name='useDefaultPrice']:not(:hidden)").change(function () {
            $("[id='useDefaultPrice']").val($(this).val());
        });
        <c:choose>
            <c:when test="${servicePoint.mdDepositLevel == null || servicePoint.mdDepositLevel.id == null || servicePoint.mdDepositLevel.id == 0}">
                $("#span_deposit").hide();
                $("#deposit").removeAttr("required");
            </c:when>
            <c:otherwise>
                $("#span_deposit").show();
                $("#deposit").attr("required",'required');
            </c:otherwise>
        </c:choose>

        $('.jq2').viewer({
            url: 'data-original',
        });
    });
    $("#useDefaultPrice").val($("#useDefaultPrice1").val());
    editRemotePriceEnabledFlag();
</script>
</body>
</html>