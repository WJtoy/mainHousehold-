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
    <script src="${ctxStatic}/area/Area-1.0.js" type="text/javascript"></script>
    <script src="${ctxStatic}/area/tree-area.js" type="text/javascript"></script>
    <style type="text/css">
        .imgfile {
            max-width: 80px;
            max-height: 80px;
            cursor: hand;
        }
    </style>
    <c:set var="user" value="${fns:getUser()}" />
    <script type="text/javascript">
        var treeArea = TreeArea('${ctx}');

        $(document).ready(function () {
            top.layer.closeAll();

            $("[id^='level.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if(optionSelected.val()=="0"){
                    $("[id^='level.label']").val("");
                }else {
                    $("[id^='level.label']").val(optionSelected.text());
                }
            });

            $("[id^='finance.paymentType.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if(optionSelected.val()=="0"){
                    $("[id^='finance.paymentType.label']").val("");
                }else {
                    $("[id^='finance.paymentType.label']").val(optionSelected.text());
                }
            });

            $("[id^='finance.bank.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if(optionSelected.val()=="0"){//请选择
                    $("[id^='finance.bank.label']").val("");
                    /*
                    var $ctl =$("[id='finance.branch']");
                    $ctl.val("");
                    $ctl.rules("remove",'required');
                    $ctl =$("#span_branch");
                    $ctl.html("");
                    $("label[for='finance.branch']").remove();
                    */
                    $ctl =$("[id='finance.bankNo']");
                    $ctl.val("");
                    $ctl.rules("remove",'required');
                    $ctl =$("#span_bankNo");
                    $ctl.html("");
                    $("label[for='finance.bankNo']").remove();

                    $ctl =$("[id='finance.bankOwner']");
                    $ctl.val("");
                    $ctl.rules("remove",'required');
                    var $ctl =$("#span_bankOwner");
                    $ctl.html("");
                    $("label[for='finance.bankOwner']").remove();
                }else {
                    $("[id^='finance.bank.label']").val(optionSelected.text());
                    /*
                    var $ctl =$("[id='finance.branch']");
                    $ctl.rules("add",'required');
                    $ctl =$("#span_branch");
                    $ctl.html("*");
                    */
                    $ctl =$("[id='finance.bankNo']");
                    $ctl.rules("add",'required');
                    $ctl =$("#span_bankNo");
                    $ctl.html("*");

                    $ctl =$("[id='finance.bankOwner']");
                    $ctl.rules("add",'required');
                    var $ctl =$("#span_bankOwner");
                    $ctl.html("*");
                }
            });
            /*
            $("[id^='finance.branch.value']").change(function () {
                var optionSelected = $(this).find("option:selected");
                if(optionSelected.val()=="0"){
                    $("[id^='finance.branch.label']").val("");
                }else {
                    $("[id^='finance.branch.label']").val(optionSelected.text());
                }
            });
            */

            $("#btnApprove").click(function () {
                $btnApprove = $("#btnApprove");
                if($btnApprove.prop("disabled") == true){
                    return false;
                }
                $btnApprove.attr("disabled", "disabled");
                top.layer.confirm('确认保存并通过审核?', {icon: 3, title:'系统确认'}, function(index){
                    top.layer.close(index);//关闭本身
                    $("#inputForm").attr("action", "${ctx}/md/servicepoint/saveBaseInfoAndApprove");
                    $("#inputForm").submit();
                },function(index){
                    //cancel
                    $btnApprove.removeAttr('disabled');
                });
                return false;
            });

            $("#btnSubmit").click(function () {
                var $btnSubmit = $("#btnSubmit");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                $btnSubmit.prop("disabled",true);
                if (!$("#inputForm").valid()) {
                    $btnSubmit.prop("disabled",false);
                    return false;
                }

                //$btnSubmit.attr("disabled", "disabled");
                $("#inputForm").attr("action", "${ctx}/md/servicepoint/saveBaseInfo");
                $("#inputForm").submit();
            });

            $("#inputForm").validate({
                rules: {
                    servicePointNo: {remote: "${ctx}/md/servicepoint/checkNo?id=${servicePoint.id}"},
                    contactInfo1: {remote: "${ctx}/md/servicepoint/checkContact?id=${servicePoint.id}"},
                    "primary.contactInfo": {remote: "${ctx}/md/servicepoint/checkEngineerMobile?id=${servicePoint.primary.id}"},
                    "finance.bankNo": {remote: "${ctx}/md/servicepoint/checkBankNo?id=${servicePoint.id}"}
                },
                messages: {
                    servicePointNo: {remote: "服务网点编号已存在"},
                    contactInfo1: {remote: "服务网点编号已存在"},
                    "primary.contactInfo": {remote:"手机号已注册"},
                    "finance.bankNo": {remote: "服务网点银行卡号已存在"}
                },
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                submitHandler: function (form) {
                    layerLoading('正在提交，请稍等...',true);
                    form.submit();
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#btnSubmit").removeAttr('disabled');
                    $("#btnApprove").removeAttr('disabled');

                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

            $("#btnSubmitProduct").click(function () {
                var $btnSubmit = $("#btnSubmitProduct");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                $btnSubmit.prop("disabled",true);
                if (!$("#inputFormProduct").valid()) {
                    $btnSubmit.prop("disabled",false);
                    return false;
                }

                $("#inputFormProduct").attr("action", "${ctx}/md/servicepoint/saveProducts");
                $("#inputFormProduct").submit();
            });

            $("#inputFormProduct").validate({
                rules: {
                },
                messages: {
                },
                onfocusout: function(element){
                    $(element).valid();   //失去焦点时再验证
                },
                submitHandler: function (form) {
                    //check
                    //product
                    var pids = [], pnodes = ptree.getCheckedNodes(true);
                    if (pnodes.length == 0) {
                        $("#btnSubmitProduct").removeAttr('disabled');
                        $("#btnApproveProduct").removeAttr('disabled');
                        layerInfo("请选择服务的产品", "信息提示");
                        return false;
                    }

                    //data
                    //product
                    for (var i = 0; i < pnodes.length; i++) {
                        if (pnodes[i].level > 0) {
                            pids.push(pnodes[i].id);
                        }
                    }
                    $("#productIds").val(pids);

                    layerLoading('正在提交，请稍等...',true);
                    form.submit();
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#btnSubmitProduct").removeAttr('disabled');
                    $("#btnApproveProduct").removeAttr('disabled');

                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

            $("#btnSubmitArea").click(function () {
                var $btnSubmit = $("#btnSubmitArea");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                $btnSubmit.prop("disabled",true);
                if (!$("#inputFormArea").valid()) {
                    $btnSubmit.prop("disabled",false);
                    return false;
                }

                $("#inputFormArea").attr("action", "${ctx}/md/servicepoint/saveAreas");
                $("#inputFormArea").submit();
            });

            $("#inputFormArea").validate({
                rules: {
                },
                messages: {
                },
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                submitHandler: function (form) {
                    //check
                    //area
                    var ids = [], anodes = tree.getCheckedNodes(true);
                    if (anodes.length == 0) {
                        $("#btnSubmitArea").removeAttr('disabled');
                        $("#btnApproveArea").removeAttr('disabled');
                        layerInfo("请选择服务的区域", "信息提示");
                        return false;
                    }

                    //data
                    //area
                    for (var i = 0; i < anodes.length; i++) {
                        //ids.push(anodes[i].id); // mark on 2019-5-28
                        var area = {};
                        area.id = anodes[i].id;
                        area.type = anodes[i].type;
                        ids.push(area);
                    }
                    $("#areaIds").val(JSON.stringify(ids));
                    layerLoading('正在提交，请稍等...',true);
                    form.submit();
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#btnSubmitArea").removeAttr('disabled');
                    $("#btnApproveArea").removeAttr('disabled');

                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

            var setting = {
                check: {enable: true, nocheckInherit: true}, view: {selectedMulti: false},
                data: {simpleData: {enable: true}}, callback: {
                    beforeClick: function (id, node) {
                        tree.checkNode(node, !node.checked, true, true);
                        treeArea.obtainTownDataByDistrict(tree,node);
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
                    treeArea.initTownDataBySelectedDistrict(tree, node, "${servicePoint.id}", "true");
                } catch (e) {
                }
            }

            // 默认展开全部节点
            //tree.expandAll(true);
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
            // 默认展开全部节点
            ptree.expandAll(true);
            //安维人员服务产品

            //show the attachment image when it is update
            if ($("#attachment1").val().length > 0) {
                $("#attachment1_image").attr("src", "${ctxUpload}/" + $("#attachment1").val() + "?t=" + Math.random());
            }
            if ($("#attachment2").val().length > 0) {
                $("#attachment2_image").attr("src", "${ctxUpload}/" + $("#attachment2").val() + "?t=" + Math.random());
            }
            if ($("#attachment3").val().length > 0) {
                $("#attachment3_image").attr("src", "${ctxUpload}/" + $("#attachment3").val() + "?t=" + Math.random());
            }
            if ($("#attachment4").val().length > 0) {
                $("#attachment4_image").attr("src", "${ctxUpload}/" + $("#attachment4").val() + "?t=" + Math.random());
            }

            $("#buttonUpload1").click(function () {
                uploadfile($("#attachment1"), $("#attachment1_image"), "fileToUpload1");
                return false;
            });
            $("#buttonUpload2").click(function () {
                uploadfile($("#attachment2"), $("#attachment2_image"), "fileToUpload2");
                return false;
            });
            $("#buttonUpload3").click(function () {
                uploadfile($("#attachment3"), $("#attachment3_image"), "fileToUpload3");
                return false;
            });
            $("#buttonUpload4").click(function () {
                uploadfile($("#attachment4"), $("#attachment4_image"), "fileToUpload4");
                return false;
            });


            <%--//网点处于异常状态时，必须设置付款失败原因--%>
            <%--var bankIssueTypes = [--%>
            <%--<c:forEach items="${fns:getDictListFromMS('BankIssueType')}" var="banIssueItem">--%>
            <%--{value: "${banIssueItem.value}", label: "${banIssueItem.label}"},--%>
            <%--</c:forEach>--%>
            <%--];--%>
            <%--var currentBankIssueValue = "${servicePoint.finance.bankIssue.value}";--%>

            <%--$(document).on('change', '#statusValue', function() {--%>
            <%--var $statusSelect = $("#statusValue");--%>
            <%--var $bankIssueSelect = $("#financeBankIssueSelect");--%>
            <%--var currentStatus = $statusSelect.val();--%>
            <%--$bankIssueSelect.empty();--%>
            <%--if (currentStatus !== '2') {--%>
            <%--$bankIssueSelect.append("<option value='0' selected='selected'>无</option>");--%>
            <%--}--%>
            <%--$.each(bankIssueTypes, function(i, item) {--%>
            <%--$bankIssueSelect.append("<option value='"+item.value+"'>" + item.label + "</option>");--%>
            <%--});--%>
            <%--if (currentStatus === '2' && currentBankIssueValue === '0') {--%>
            <%--$("#financeBankIssueSelect option:first").attr("selected",true);--%>
            <%--}--%>
            <%--else {--%>
            <%--$bankIssueSelect.val(currentBankIssueValue);--%>
            <%--}--%>
            <%--$bankIssueSelect.change();--%>
            <%--});--%>

            <%--$("#statusValue").change();--%>
        });

        function uploadfile($obj1, $obj1_image, obj2) {
            var data = {
                fileName: $obj1.val()
            };
            $.ajaxFileUpload({
                url: '${pageContext.request.contextPath}/servlet/Upload?fileName=' + $obj1.val(),//处理图片脚本
                secureuri: false,
                data: data,
                fileElementId: obj2,//file控件id
                dataType: 'json',
                success: function (data, status) {
                    $obj1.val(data.fileName);
                    $obj1_image.show();
                    $obj1_image.attr("src", "${ctxUpload}/" + data.fileName + "?t=" + Math.random());
                },
                error: function (data, status, e) {
                    alert(e);
                }
            });
        }

        function showImage(title,img) {
            if(!Utils.isEmpty(img)) {
                var h = $(top.window).height()-20;
                var w = $(top.window).width()-20;
                if(w>1440){
                    w = 1440;
                    h = 860;
                }
                var view_index = top.layer.open({
                    id: 'layer_viewphoto',
                    zIndex: 19891018,
                    type: 1,
                    title: false,
                    closeBtn: 1,
                    shade: 0.3,
                    area: [w + 'px', h + 'px'],
                    shadeClose: true,
                    content: '<img src="' + url + '">'
                });
            }
            /*
            if(!Utils.isEmpty(img)) {
                $.jBox("<img src='" + img + "'/>", {title: title, width: 700,});
            }*/
        }
        //保存新的网店备注
        function saveServicePointRemark(servicePointId,btn) {
            if(btn.disabled==true){return fasle;}
            btn.disabled=true;
            var remarks=$("#remarks").val();
            if (!remarks){
                //layerError("请先输入备注信息");
                layerMsg('请先输入备注信息');
                btn.disabled=false;
                $("#remarks").focus()
                return false;
            }
            if (servicePointId!=null && remarks!=null){
                $.ajax({
                    cache: false,
                    type: "POST",
                    url:"${ctx}/md/servicepoint/ajax/updateRemark?servicePointId=" + servicePointId + "&remarks=" + (remarks || ''),
                    dataType: 'json',
                    success: function (data) {
                        btn.disabled=false;
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data.success ) {
                            layerMsg('保存成功');

                        }else{
                            layerError(data.message);
                        }
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"保存备注错误，请重试!");
                        btn.disabled=false;
                    }
                });

            }else {
                layerError("获取保存类容错误","错误提示");
                return false;
            }

        }
        //查看网店备注历史列表
        function viewRemarkList(servicePointId,servicePointNo,servicePointName) {
            var planIndex = top.layer.open({
                type: 2,
                id:'layer_planRemarkList_view',
                zIndex:19891016,
                title:'网点备注',
                content: "${ctx}/md/servicepoint/viewRemarkList?servicePointId=" + (servicePointId || '')+"&servicePointNo="+ (servicePointNo || '')+"&servicePointName="+ (servicePointName || ''),
                // area: ['980px', '640px'],
                area: ['1255px', (screen.height/2)+'px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <shiro:hasPermission name="md:servicepoint:view">
    <li><a href="${ctx}/md/servicepoint">服务网点列表</a></li>
    </shiro:hasPermission>
    <li class="active"><a href="javascript:;">服务网点<shiro:hasPermission
            name="md:servicepoint:edit">${not empty servicePoint.id?'修改':'添加'}</shiro:hasPermission>
        <shiro:lacksPermission name="md:servicepoint:edit">查看</shiro:lacksPermission>
    </a></li>
<shiro:hasPermission name="md:servicepoint:view">
    <li><a href="${ctx}/md/servicepoint/disableList">停用列表</a></li>
</shiro:hasPermission>
</ul>
<br/>
<sys:message content="${message}"/>
<div class="tabbable" style="margin:0px 20px">
    <ul class="nav nav-tabs">
        <li id="libaseInfo" class="active"><a href="#tabBaseInfo" data-toggle="tab">基本资料</a></li>
        <li id="liproductInfo"><a href="#tabProductInfo" data-toggle="tab">产品</a></li>
        <li id="liareaInfo"><a href="#tabAreaInfo" data-toggle="tab">区域</a></li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane active" id="tabBaseInfo">
            <form:form id="inputForm" modelAttribute="servicePoint" action="${ctx}/md/servicepoint/save" method="post" class="form-horizontal">
            <form:hidden path="id"/>
            <form:hidden path="delFlag"/>
            <form:hidden path="address" />
            <form:hidden path="useDefaultPrice" />
            <form:hidden path="forTmall"/>
            <legend>基本信息</legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">网点编号:</label>
                        <div class="controls">
                            <form:input path="servicePointNo" htmlEscape="false" maxlength="20" class="required"/>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">网点名称:</label>
                        <div class="controls">
                            <form:input path="name" htmlEscape="false" maxlength="50" class="required"/>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">手机:</label>
                        <div class="controls">
                            <form:input path="contactInfo1" htmlEscape="false" maxlength="11" class="required mobile"/>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">电话:</label>
                        <div class="controls">
                            <form:input path="contactInfo2" htmlEscape="false" maxlength="16" class="phone"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">QQ:</label>
                        <div class="controls">
                            <form:input path="qq" htmlEscape="false" class="qq" maxlength="11"/>
                        </div>
                    </div>
                </div><div class="span6">
                <div class="control-group">
                    <label class="control-label">短信通知:</label>
                    <div class="controls">
                        <form:radiobuttons path="shortMessageFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                        <shiro:hasPermission name="md:servicepoint:autocomplete">
                            <label style="margin-left: 8px;">自动完工:</label>
                            <form:radiobuttons path="autoCompleteOrder" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                        </shiro:hasPermission>
                        <shiro:lacksPermission name="md:servicepoint:autocomplete">
                            <form:hidden path="autoCompleteOrder" />
                        </shiro:lacksPermission>
                    </div>
                </div>
            </div>
            </div>
            <div class="row-fluid">
                <div class="span6" style="width: auto;">
                    <div class="control-group">
                        <label class="control-label">地址:</label>
                        <div class="controls">
                            <sys:areaselect name="area.id" id="area" value="${servicePoint.area.id}"
                                            labelValue="${servicePoint.area.fullName}" labelName="area.fullName" title=""
                                            mustSelectCounty="true" cssClass="required"></sys:areaselect>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6" style="margin-left:5px;">
                    <div class="control-group">
                        <div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
                            <form:input path="subAddress"  htmlEscape="false" cssClass="required" maxlength="100" style="width:350px;"/>
                            <span class=" red">*详细地址不包含省、市、区县</span>
                        </div>
                    </div>
                </div>
            </div>
            <legend>主帐号</legend>
            <form:hidden path="primary.id" />
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">姓名:</label>
                        <div class="controls">
                            <form:input path="primary.name" htmlEscape="false" maxlength="20" class="required" />
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">手机:</label>
                        <div class="controls">
                            <form:input path="primary.contactInfo" htmlEscape="false" readonly="${!empty servicePoint.primary.id?'true':'false'}" maxlength="11" class="required mobile"/>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">等级:</label>
                        <div class="controls">
                            <form:select path="primary.level.value" class="input-small required" disabled="${!empty servicePoint.primary.id?'true':'false'}" cssStyle="width: 220px;">
                                <form:option value="0" label="请选择"/>
                                <form:options items="${fns:getDictListFromMS('ServicePointLevel')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                            </form:select>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">允许手机接单:</label>
                        <div class="controls">
                            <form:radiobuttons path="primary.appFlag" disabled="true" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/><%--切换为微服务--%>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
            </div>
            <legend>签约信息</legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">签约:</label>
                        <div class="controls">
                            <form:radiobuttons path="signFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label"
                                               itemValue="value" htmlEscape="false" class="required"/><%--切换为微服务--%>
                        </div>
                    </div>
                </div>
                <div class="span4">
                    <div class="control-group" id="contractDateGroup">
                        <label class="control-label">签约日期:</label>
                        <div class="controls">
                            <input id="contractDate" name="contractDate" type="text" readonly="readonly" style="width:95px;margin-left:4px"
                                   maxlength="20" class="input-small Wdate"
                                   value="<fmt:formatDate value='${servicePoint.contractDate}' pattern='yyyy-MM-dd'/>"
                                   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">开发人员:</label>
                        <div class="controls">
                            <form:input path="developer" htmlEscape="false" maxlength="20" cssClass="required"/>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">备注:</label>
                        <div class="controls">
                            <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge"/>
                            <div style="margin-top: 5px;display: ${servicePoint.id>0?'block':'none'}">
                                <button id="btnSavePlanRemark" class="btn"  data-serveicepointid="${servicepoint.id}" type="button" onclick="saveServicePointRemark('${servicePoint.id}',this);">
                                    保存备注
                                </button>
                                <button id="btnShowPlanRemarkList" style="margin-left: 20px;" class="btn" type="button" onclick="viewRemarkList('${servicePoint.id}','${servicePoint.servicePointNo}','${servicePoint.name}');">
                                    历史备注
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <shiro:hasPermission name="md:servicepoint:debts">
                <div class="row-fluid">
                    <div class="span6">
                        <div class="control-group">
                            <label class="control-label">欠款额:</label>
                            <div class="controls">
                                <form:input path="finance.debtsAmount" htmlEscape="false" maxlength="20"/>
                            </div>
                        </div>
                    </div>
                    <div class="span6">
                        <div class="control-group">
                            <label class="control-label">欠款描述:</label>
                            <div class="controls">
                                <form:textarea path="finance.debtsDescrption" htmlEscape="false" rows="3" maxlength="150" class="input-xlarge"/>
                            </div>
                        </div>
                    </div>
                </div>
            </shiro:hasPermission>
            <legend>控制开关</legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">状态:</label>
                        <div class="controls">
                            <select id="statusValue" name="status.value" class="required input-small" style="width:220px;">
                                <c:forEach items="${fns:getDictExceptListFromMS('service_point_status', '')}" var="dict"><%--切换为微服务--%>
                                    <option value="${dict.value}"
                                            <c:out value="${(servicePoint.status.value.toString() == dict.value)?'selected=selected':''}" />>${dict.label}</option>
                                </c:forEach>
                            </select>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">购买保险</label>
                        <div class="controls">
                            <shiro:hasPermission name="md:servicepoint:insurance">
                                <form:radiobutton path="insuranceFlag" value="1"></form:radiobutton>
                                购买
                                <form:radiobutton path="insuranceFlag" value="0"></form:radiobutton>
                                不购买
                            </shiro:hasPermission>
                            <shiro:lacksPermission name="md:servicepoint:insurance">
                                <c:choose>
                                    <c:when test="${servicePoint.insuranceFlag eq 1}">
                                        <form:radiobutton path="insuranceFlag" value="1"></form:radiobutton>
                                        购买
                                    </c:when>
                                    <c:otherwise>
                                        <form:radiobutton path="insuranceFlag" value="0"></form:radiobutton>
                                        不购买
                                    </c:otherwise>
                                </c:choose>
                            </shiro:lacksPermission>
                            <%--<form:radiobuttons path="insuranceFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/> --%>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <%--<label class="control-label">时效奖励(快可立)</label>--%>
                        <label class="control-label">快可立补贴</label>
                        <div class="controls">
                            <shiro:hasPermission name="md:servicepoint:timeliness">
                                <form:radiobutton path="timeLinessFlag" value="1"></form:radiobutton>
                                开启
                                <form:radiobutton path="timeLinessFlag" value="0"></form:radiobutton>
                                关闭
                            </shiro:hasPermission>
                            <shiro:lacksPermission name="md:servicepoint:timeliness">
                                <c:choose>
                                    <c:when test="${servicePoint.timeLinessFlag eq 1}">
                                        <form:radiobutton path="timeLinessFlag" value="1"></form:radiobutton>
                                        开启
                                    </c:when>
                                    <c:otherwise>
                                        <form:radiobutton path="timeLinessFlag" value="0"></form:radiobutton>
                                        关闭
                                    </c:otherwise>
                                </c:choose>
                            </shiro:lacksPermission>
                            <%--<form:radiobuttons path="timeLinessFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>--%>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">APP同意购买保险</label>
                        <div class="controls">
                            <form:hidden path="appInsuranceFlag"/>
                            <c:choose>
                                <c:when test="${servicePoint.appInsuranceFlag eq 0}">
                                    <form:radiobutton path="appInsuranceFlag" value="0" disabled="true"></form:radiobutton>
                                    未阅读保险条款
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
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">自动派单</label>
                        <div class="controls">
                            <form:radiobutton path="autoPlanFlag" value="1" disabled="true"></form:radiobutton>
                            开启
                            <form:radiobutton path="autoPlanFlag" value="0" disabled="true"></form:radiobutton>
                            关闭
                        </div>
                    </div>
                </div>
            </div>
            <legend>结算信息</legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">结算标准:</label>
                        <div class="controls">
                            <c:if test="${servicePoint.id == null}">
                                <select id="useDefaultPrice1" name="useDefaultPrice" class="required input-small" style="width:220px;">
                                    <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict"><%--切换为微服务--%>
                                        <option value="${dict.value}"
                                                <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}" />>${dict.label}</option>
                                    </c:forEach>
                                </select>
                            </c:if>
                            <c:if test="${servicePoint.id != null}">
                                <shiro:lacksPermission name="md:servicepoint:defaultpriceedit">
                                    <select id="useDefaultPrice1" name="useDefaultPrice" disabled="disabled" class="required input-small" style="width:220px;">
                                        <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict"><%--切换为微服务--%>
                                            <option value="${dict.value}"
                                                    <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}" />>${dict.label}</option>
                                        </c:forEach>
                                    </select>
                                </shiro:lacksPermission>
                                <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                                    <select id="useDefaultPrice1" name="useDefaultPrice" class="required input-small" style="width:220px;">
                                        <c:forEach items="${fns:getDictListFromMS('PriceType')}" var="dict"><%--切换为微服务--%>
                                            <option value="${dict.value}"
                                                    <c:out value="${(servicePoint.useDefaultPrice.toString() == dict.value)?'selected=selected':''}" />>${dict.label}</option>
                                        </c:forEach>
                                    </select>
                                </shiro:hasPermission>
                            </c:if>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <c:if test="${servicePoint.id == null}">
                        <div class="control-group">
                            <label class="control-label">是否重置价格:</label>
                            <div class="controls">
                                <form:radiobutton path="resetPrice" label="是" value="1"/>
                                    <%--<form:radiobuttons path="resetPrice" readonly="readonly" items="${fns:getDictList('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false"/>--%>
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${servicePoint.id != null}">
                        <div class="control-group">
                            <label class="control-label">是否重置价格:</label>
                            <div class="controls">
                                <shiro:lacksPermission name="md:servicepoint:defaultpriceedit">
                                    <form:radiobutton path="resetPrice" label="否" value="0"/>
                                </shiro:lacksPermission>
                                <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                                    <form:radiobuttons path="resetPrice" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                                </shiro:hasPermission>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">结算方式:</label>
                        <div class="controls">
                            <form:select path="finance.paymentType.value" class="required input-small" cssStyle="width: 220px;">
                                <form:option value="" label="请选择"/>
                                <form:options items="${fns:getDictExceptListFromMS('PaymentType', '30')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                            </form:select><span class=" red">*</span>
                        </div>
                        <form:hidden path="finance.paymentType.label" htmlEscape="false" />
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">是否开票:</label>
                        <div class="controls">
                            <form:radiobuttons path="finance.invoiceFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">开户行:</label>
                        <div class="controls">
                            <form:select path="finance.bank.value" class="input-small" cssStyle="width: 220px;">
                                <form:option value="0" label="请选择"/>
                                <form:options items="${fns:getDictListFromMS('banktype')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                            </form:select>
                        </div>
                        <form:hidden path="finance.bank.label" htmlEscape="false" />
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">分行:</label>
                        <div class="controls">
                            <form:input path="finance.branch" htmlEscape="false" maxlength="50"/>
                            <span id="span_branch" class="red"></span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">卡号:</label>
                        <div class="controls">
                            <form:input path="finance.bankNo" htmlEscape="false" maxlength="50" class=""/>
                            <span id="span_bankNo" class="red"></span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">开户人:</label>
                        <div class="controls">
                            <form:input path="finance.bankOwner" htmlEscape="false" maxlength="50" />
                            <span id="span_bankOwner" class=" red"></span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">付款失败原因:</label>
                        <div class="controls">
                            <form:select path="finance.bankIssue.value" id="financeBankIssueSelect" class="input-small" cssStyle="width: 220px;">
                                <form:option value="0" label="无"/>
                                <form:options items="${fns:getDictListFromMS('BankIssueType')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
                            </form:select>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">是否扣点:</label>
                        <div class="controls">
                            <form:radiobuttons path="finance.discountFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">扣点:</label>
                        <div class="controls">
                            <form:input path="finance.discount" htmlEscape="false" maxlength="7" min="0.0" max="100.0" class="required number" />
                            <span id="span_bankOwner" class=" red"></span>
                            <span class="help-inline">格式为：小数，如0.01代表百分之一</span>
                        </div>
                    </div>
                </div>
            </div>
            <legend>等级及评价</legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">等级:</label>
                        <div class="controls">
                            <form:select path="level.value" class="input-small required" cssStyle="width: 220px;">
                                <form:option value="" label="请选择"/>
                                <form:options items="${fns:getDictExceptListFromMS('ServicePointLevel', '6,7,8')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                            </form:select>
                            <span class=" red">*</span>
                        </div>
                        <form:hidden path="level.label" htmlEscape="false" />
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">网点容量:</label>
                        <div class="controls">
                            <form:input path="capacity"  htmlEscape="false" maxlength="5" class="required number"/>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group info">
                        <label class="control-label">派单数:</label>
                        <div class="controls">
                            <form:input path="planCount" type="number" readonly="true" htmlEscape="false" class="uneditable-input"/>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group success">
                        <label class="control-label">完成数:</label>
                        <div class="controls">
                            <form:input path="orderCount" type="number" readonly="true" htmlEscape="false" class="uneditable-input"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group error">
                        <label class="control-label">违约单数:</label>
                        <div class="controls">
                            <form:input path="breakCount" type="number" readonly="true" htmlEscape="false" class="uneditable-input"/>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">用户评价:</label>
                        <div class="controls">
                            <form:input path="grade" type="number" readonly="true" htmlEscape="false" class="uneditable-input"/>
                        </div>
                    </div>
                </div>
            </div>
            <legend>坐标信息</legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group info">
                        <label class="control-label">经度:</label>
                        <div class="controls">
                            <form:input path="longitude" type="number" htmlEscape="false"/>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group info">
                        <label class="control-label">纬度:</label>
                        <div class="controls">
                            <form:input path="latitude" type="number" htmlEscape="false"/>
                        </div>
                    </div>
                </div>
            </div>

            <legend>官网展示资料</legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">性质:</label>
                        <div class="controls">
                            <form:select path="property" class="input-small required" cssStyle="width: 220px;">
                                <form:option value="0" label="请选择"/>
                                <form:options items="${fns:getDictListFromMS('ServicePointProperty')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                            </form:select>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">规模:</label>
                        <div class="controls">
                            <form:select path="scale" class="input-small required" cssStyle="width: 220px;">
                                <form:option value="0" label="请选择"/>
                                <form:options items="${fns:getDictList('ServicePointScal')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
                            </form:select>
                            <span class=" red">*</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span12">
                    <div class="control-group">
                        <label class="control-label">网点简介:</label>
                        <div class="controls">
                            <form:textarea path="description" maxlength="255" class="input-xlarge"/>
                        </div>
                    </div>
                </div>
            </div>
            <legend>证件上传</legend>
            <div class="control-group">
                <label class="control-label">合同:</label>
                <div class="controls">
                    <form:hidden path="attachment1" htmlEscape="false"/>
                    <input id="fileToUpload1" type="file" size="20" name="fileToUpload1" class="input">
                    <button id="buttonUpload1" type="button" class="btn">上传</button>
                    <img id="attachment1_image" class="imgfile" onclick="showImage('合同','${ctxUpload}/${servicePoint.attachment1}');"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">个人身份证:</label>
                <div class="controls">
                    <form:hidden path="attachment2" htmlEscape="false" />
                    <input id="fileToUpload2" type="file" size="20" name="fileToUpload2" class="input">
                    <button id="buttonUpload2" type="button" class="btn">上传</button>
                    <img id="attachment2_image" class="imgfile" onclick="showImage('个人身份证','${ctxUpload}/${servicePoint.attachment2}');" />
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">其他证件1:</label>
                <div class="controls">
                    <form:hidden path="attachment3"  htmlEscape="false"/>
                    <input id="fileToUpload3" type="file" size="20" name="fileToUpload3" class="input">
                    <button id="buttonUpload3" type="button" class="btn">上传</button>
                    <img id="attachment3_image" class="imgfile" onclick="showImage('其他证件1','${ctxUpload}/${servicePoint.attachment3}');"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">其他证件2:</label>
                <div class="controls">
                    <form:hidden path="attachment4"  htmlEscape="false"/>
                    <input id="fileToUpload4" type="file" size="20" name="fileToUpload4" class="input">
                    <button id="buttonUpload4" type="button" class="btn">上传</button>
                    <img id="attachment4_image" class="imgfile" onclick="showImage('其他证件2','${ctxUpload}/${servicePoint.attachment4}');"/>
                </div>
            </div>
            <c:if test="${servicePoint.id != null}">
                <legend>其它</legend>
                <div class="row-fluid">
                    <div class="span6">
                        <div class="control-group">
                            <label class="control-label">创建时间:</label>
                            <div class="controls">
                                <label class="lbl"><fmt:formatDate value="${servicePoint.createDate}" type="both" dateStyle="full"/> </label>
                            </div>
                        </div>
                    </div>

                    <div style="margin-left: 0" class="span6">
                        <div class="control-group">
                            <label class="control-label">最后修改:</label>
                            <div class="controls">
                                <label class="lbl"><fmt:formatDate value="${servicePoint.updateDate}" type="both" dateStyle="full"/> </label>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>
            <div class="form-actions">
                <shiro:hasPermission name="md:servicepoint:edit">
                    <input id="btnSubmit" class="btn btn-primary" type="button"
                           value="保 存"/>&nbsp;</shiro:hasPermission>
                <input id="btnCancel" class="btn" type="button" value="返 回"
                       onclick="history.go(-1)"/>
                <c:if test="${servicePoint.id != null && servicePoint.delFlag >=2 }"><input
                        style="margin-left: 20px" id="btnApprove" class="btn btn-primary" type="button" value="保存并通过审核"/></c:if>
            </div>
            </form:form>
        </div>
        <div class="tab-pane" id="tabProductInfo">
            <form:form id="inputFormProduct" modelAttribute="servicePoint" action="${ctx}/md/servicepoint/save" method="post" class="form-horizontal">
                <form:hidden path="id"/>
                <form:hidden path="delFlag"/>
                <form:hidden path="forTmall"/>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <legend>产品信息</legend>
                        <div class="">
                            <div style="margin-top:3px;float:left;height:400px;overflow:auto; width:400px;">
                                <ul class="ztree" id="productTree"></ul>
                            </div>
                            <form:hidden path="productIds"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="form-actions">
                <shiro:hasPermission name="md:servicepoint:edit">
                    <input id="btnSubmitProduct" class="btn btn-primary" type="button"
                           value="保 存"/>&nbsp;</shiro:hasPermission>
                <input id="btnCancel" class="btn" type="button" value="返 回"
                       onclick="history.go(-1)"/>
            </div>
            </form:form>
        </div>
        <div class="tab-pane" id="tabAreaInfo">
            <form:form id="inputFormArea" modelAttribute="servicePoint" action="${ctx}/md/servicepoint/save" method="post" class="form-horizontal">
                <form:hidden path="id"/>
                <form:hidden path="delFlag"/>
                <form:hidden path="forTmall"/>
                <div class="row-fluid">
                    <div class="span6">
                        <div class="control-group">
                            <legend>服务区域</legend>
                            <div class="">
                                <div style="margin-top:3px;float:left;height:400px;overflow:auto; width: 400px;">
                                    <ul class="ztree" id="areaTree"></ul>
                                </div>
                                <form:hidden path="areaIds" class="required"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-actions">
                    <shiro:hasPermission name="md:servicepoint:edit">
                        <input id="btnSubmitArea" class="btn btn-primary" type="button"
                               value="保 存"/>&nbsp;</shiro:hasPermission>
                    <input id="btnCancel" class="btn" type="button" value="返 回"
                           onclick="history.go(-1)"/>
                </div>
            </form:form>
        </div>
    </div>
</div>
<script type="text/javascript">
    function checkMobile(){
        $.ajax({
            type:"post",
            url: '${ctx}/sys/user/checkMobile',
            async:false,
            data:{
                mobile:$("[id='primary.contactInfo']").val(),
                expectType:"engineer",
                expectId: '${servicePoint.primary.id}'
            },
            dataType: "html",
            success: function(data, type) {
                return data == "true"? true : false;
            }
        });
    }
    $(function(){
        <c:if test="${servicePoint.id == null}">
        $("#servicePointNo").focus();

        jQuery.validator.addMethod("checkEngineerMobile", function(value, element) {
            return checkMobile();
        },'手机号码已被注册');
//        $("[id='primary.contactInfo']").rules("add",{checkEngineerMobile:true});
        </c:if>
        <c:if test="${servicePoint.id ne null}">
            <shiro:lacksPermission name="md:servicepoint:statuspaused">
                 $("#statusValue option[value=20]").attr("disabled","disabled");
            </shiro:lacksPermission>
            <shiro:lacksPermission name="md:servicepoint:statusblacklist">
                 $("#statusValue option[value=100]").attr("disabled","disabled");
            </shiro:lacksPermission>
        </c:if>

        $("[name='useDefaultPrice']:not(:hidden)").change(function(){
            $("[id='useDefaultPrice']").val($(this).val());
        });
    });
    $("#useDefaultPrice").val($("#useDefaultPrice1").val());
</script>
</body>
</html>
