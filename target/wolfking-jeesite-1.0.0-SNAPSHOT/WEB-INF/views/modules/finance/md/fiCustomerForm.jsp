<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客戶管理</title>
    <meta name="decorator" content="default"/>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>

    <style type="text/css">
        .table td{
            height: 46px!important;
            vertical-align: middle!important;
            text-align: center;
        }
        .table th{
            vertical-align: middle!important;
            text-align: center!important;
        }
        .x {
            width: 46%;
            float: left;
        }
        .table-bordered{
            border-radius: 0px;
        }
        .line_{
            border-bottom: 3.5px solid #0096DA;
            width: 65px;
            border-radius: 10px;
        }
        .line-row {
            margin-left: 0px;
        }
        .default{
            color: #0096DA;
            background-color:#F2F8FF;
            width: 30px;
            float: left;
        }
        .form-horizontal .controls {
            margin-left: 165px;
        }

        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            padding-left: 190px;
            border-top: 1px solid #e5e5e5;
        }

        .slider {
            width: 20px;
            height: 20px;
        }

        legend {
            width: 94%;
        }

    </style>

    <script type="text/javascript">
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
        var this_index = top.layer.index;
        $(document).ready(function () {
            var customerId = $("#customerId").val();

            $("#btnUpdateSales").click(function () {
                top.layer.open({
                    type: 2,
                    id: 'layer_salesUpdate',
                    zIndex: 19891015,
                    title: '修改',
                    content: '${ctx}/fi/md/customer/salesChangeForm?id=' + ($("#id").val() || '') + "&parentIndex=" + (orderdetail_index || ''),
                    shade: 0.3,
                    shadeClose: true,
                    area: ['550px', '350px'],
                    maxmin: false,
                    success: function (layero) {
                    },
                    end: function () {
                    }
                });
            });

            $("#btnUpdateMerchandiser").click(function () {
                top.layer.open({
                    type: 2,
                    id: 'layer_merchandiserUpdate',
                    zIndex: 19891016,
                    title: '修改',
                    content: '${ctx}/fi/md/customer/merchandiserChangeForm?id=' + ($("#id").val() || '') + "&parentIndex=" + (orderdetail_index || ''),
                    shade: 0.3,
                    shadeClose: true,
                    area: ['550px', '350px'],
                    maxmin: false,
                    success: function (layero) {
                    },
                    end: function () {
                    }
                });
            });

            $("#code").focus();
            $("#inputForm").validate({
                rules: {
                    code: {remote: "${ctx}/fi/md/customer/checkLoginName?oldLoginName=" + encodeURIComponent('${customer.code}')},
                    'finance.credit': {min: 0, max: 10000000},
                    'finance.deposit': {min: 0, max: 1000000},
                    phone: {
                        remote: {
                            type: "post",
                            url: "${ctx}/fi/md/customer/checkPhone",
                            data: {
                                id: function () {
                                    return $("#userId").val();
                                },
                                phone: function () {
                                    return $("#phone").val();
                                }
                            },
                            dataType: "json",
                            dataFilter: function (data) {
                                var data = eval('(' + data + ')');  //字符串转换成json
                                if (data.success == false) {
                                    return false;
                                } else {
                                    return true;
                                }
                            }
                        }
                    },
                    name: {
                        remote: "${ctx}/fi/md/customer/checkCustomerName?customerId=" + customerId
                    }
                },
                messages: {
                    code: {remote: "客户已存在"},
                    'finance.credit': {min: "信用额度不能低于0.", max: "信用额度不能超过一千万."},
                    'finance.deposit': {min: "押金不能低于0.", max: "押金不能超过一百万."},
                    phone: {remote: "此手机号已被注册了"},
                    name: {remote: "该手用户名称已存在，请重新输入"}
                },

                highlight: function (element) {
                    $(element).closest('.control-group').addClass('has-error');
                },
                success: function (label) {
                    label.closest('.form-group').removeClass('has-error');
                    label.remove();
                },
                onfocusout: function (element) {
                    $(element).valid();//失去焦点时再验证
                },
                submitHandler: function (form) {
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
                    var branch = $("input[name='customerAddresses[0].address']").val();
                    var branch1 = $("input[name='customerAddresses[1].address']").val();
                    var branch2 = $("input[name='customerAddresses[2].address']").val();

                    var userName = $("#master").val();
                    var phone = $("#phone").val();
                    var areaId = $("#customerAddress_0Id").val();
                    var areaName = $("#customerAddress_0Name").val();

                    var areaId1 = $("#customerAddress_1Id").val();
                    var areaName1 = $("#customerAddress_1Name").val();

                    var address = $("#address0").val();
                    var address1 = $("#address1").val();

                    $('#userName_0').val(userName);
                    $('#contactInfo_0').val(phone);
                    $('#areaName_0').val(areaName);
                    $('#areaId_0').val(areaId);
                    $('#areaName_1').val(areaName1);
                    $('#areaId_1').val(areaId1);


                    var contractDate = $("input[name='contractDate']").val();
                    var contractFlag = $("#contractFlag option:selected").val();

                    if (!Utils.isEmpty(address)) {
                        if(Utils.isEmpty(areaId) || areaId == 0){
                            layerError("请把地址选择完整的省、市、区县","错误提示");
                            $("#btnSubmit").prop("disabled", false);
                            top.layer.close(loadingIndex);
                            return false;
                        }

                    }

                    if (!Utils.isEmpty(address1)) {
                        if(Utils.isEmpty(areaId1) || areaId1 == 0){
                            layerError("请把发货地址选择完整的省、市、区县","错误提示");
                            $("#btnSubmit").prop("disabled", false);
                            top.layer.close(loadingIndex);
                            return false;
                        }

                    }

                    if(contractFlag == 20 && Utils.isEmpty(contractDate)){
                        top.$.jBox.info("签约时间不能为空", "信息提示");
                        $("#btnSubmit").prop("disabled", false);
                        top.layer.close(loadingIndex);
                        return false;
                    }

                    var ids = [], nodes = ptree.getCheckedNodes(true);
                    for (var i = 0; i < nodes.length; i++) {
                        if (nodes[i].level > 0) {
                            ids.push(nodes[i].id);
                        }
                    }
                    if (ids.length == 0) {
                        top.$.jBox.info("请选择客户负责的产品", "信息提示");
                        $("#btnSubmit").prop("disabled", false);
                        top.layer.close(loadingIndex);
                        return false;
                    }

                    if(customerId > 0){
                        var salesId = $("#salesId").val();
                        if(salesId == null || salesId == 0){
                            top.$.jBox.info("业务员不能为空", "信息提示");
                            $("#btnSubmit").prop("disabled", false);
                            top.layer.close(loadingIndex);
                            return false;
                        }
                    }

                    $("#productIds").val(ids);
                    $.ajax({
                        url: "${ctx}/fi/md/customer/save",
                        type: "POST",
                        data: $(form).serialize(),
                        dataType: "json",
                        success: function (data) {
                            //提交后的回调函数
                            if (loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if (ajaxLogout(data)) {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                top.layer.close(loadingIndex);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if (pframe) {
                                    pframe.repage();
                                }
                                top.layer.close(this_index);//关闭本身
                            } else {
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                top.layer.close(loadingIndex);
                                //layerError("保存失败", "错误提示");
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            if (loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data, null, "数据保存错误，请重试!");
                            //var msg = eval(data);
                            top.layer.close(loadingIndex);
                        },

                    });
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });


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
            var zNodes = [
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
            var ids = "${productIds}".split(",");
            for (var i = 0; i < ids.length; i++) {
                var node = ptree.getNodeByParam("id", ids[i]);
                try {
                    ptree.checkNode(node, true, true, false);
                } catch (e) {
                }
            }
            // 默认展开全部节点
            ptree.expandAll(true);


            switchEvent("#spanEffectFlag", function () {
                $("#effectFlag").val(1)
            }, function () {
                $("#effectFlag").val(0)
            });

            switchEvent("#spanReminderFlag", function () {
                $("#reminderFlag").val(1)
            }, function () {
                $("#reminderFlag").val(0)
            });
            switchEvent("#spanUrgentFlag", function () {
                $("#urgentFlag").val(1)
            }, function () {
                $("#urgentFlag").val(0)
            });
            switchEvent("#spanRemoteFeeFlag", function () {
                $("#remoteFeeFlag").val(1)
            }, function () {
                $("#remoteFeeFlag").val(0)
            });
            switchEvent("#spanTimeLinessFlag", function () {
                $("#timeLinessFlag").val(1)
            }, function () {
                $("#timeLinessFlag").val(0)
            });
            switchEvent("#spanShortMessageFlag", function () {
                $("#shortMessageFlag").val(1)
            }, function () {
                $("#shortMessageFlag").val(0)
            });
            switchEvent("#spanOfflineOrderFlag", function () {
                $("#offlineOrderFlag").val(1)
            }, function () {
                $("#offlineOrderFlag").val(0)
            });
            switchEvent("#spanCreditFlag", function () {
                $("#finance\\.creditFlag").val(1)
            }, function () {
                $("#finance\\.creditFlag").val(0)
            });
            switchEvent("#spanInvoiceFlag", function () {
                $("#finance\\.invoiceFlag").val(1)
            }, function () {
                $("#finance\\.invoiceFlag").val(0)
            });
            switchEvent("#financeLockFlag", function () {
                $("#finance\\.lockFlag").val(1)
            }, function () {
                $("#finance\\.lockFlag").val(0)
            });

            switchEvent("#spanAutoCompleteOrder", function () {
                $("#autoCompleteOrder").val(1)
            }, function () {
                $("#autoCompleteOrder").val(0)
            });

            $("input[name='vipFlag']:radio").on("change",function(){
                editVip();
            });
        });
        function editVip() {
            var vipFlag = $("input[name='vipFlag']:checked").val();
            if(vipFlag == 1){
                $("#vip").removeAttr("disabled");
            }else {
                $("#vip").attr("disabled", true);
            }
        }
        function editCustomerAddresses(customerId, addressType,addressId,editType) {
            var text = "修改";
            var url = "${ctx}/fi/md/customer/customerAddressForms?customerId=" + customerId + "&addressType=" + addressType +"&id="+ addressId +"&editType=" + editType + "&parentIndex=" + (orderdetail_index || '');
            var area = ['889px', '454px'];
            if (editType == 10) {
                text = "添加地址";
                url = "${ctx}/fi/md/customer/customerAddressForms?customerId=" + customerId + "&addressType=" + addressType +"&editType=" + editType + "&parentIndex=" + (orderdetail_index || '');
                area = ['889px', '454px'];
            }
            top.layer.open({
                type: 2,
                id: "customerAddersse",
                zIndex: 19,
                title: text,
                content: url,
                area: area,
                shade: 0.3,
                maxmin: false,
                success: function (layero, index) {

                },
                end: function () {
                }
            });
        }
        function updateCustomerAddresses(id,userName, contactInfo,areaId,areaName,address,isDefault,addressType,editType) {
            var text = "修改";
            isDefault = $('#isDefault_' + id).val();
            var url = "${ctx}/fi/md/customer/customerAddressForms?addressType=" + addressType +"&address=" + address +
                "&userName="+ userName +"&contactInfo="+ contactInfo +"&areaId="+ areaId + "&areaName="+ areaName + "&id=" +id+ "&editType=" + editType +"&isDefault="+isDefault+"&parentIndex=" + (orderdetail_index || '');
            var area = ['889px', '454px'];
            top.layer.open({
                type: 2,
                id: "customerAddersse",
                zIndex: 19,
                title: text,
                content: url,
                area: area,
                shade: 0.3,
                maxmin: false,
                success: function (layero, index) {

                },
                end: function () {
                }
            });
        }
        function editCustomerFinance(customerId, financeType) {
            var text = "修改";
            var url = "${ctx}/fi/md/customer/customerFinanceForms?customerId=" + customerId + "&financeType=" + financeType + "&parentIndex=" + (orderdetail_index || '');
            var area = ['642px', '415px'];
            if (customerId == null) {
                text = "添加账户";
                url = "${ctx}/fi/md/customer/customerFinanceForms?financeType=" + financeType + "&parentIndex=" + (orderdetail_index || '');
                area = ['642px', '415px'];
            }
            top.layer.open({
                type: 2,
                id: "customerFince",
                zIndex: 19,
                title: text,
                content: url,
                area: area,
                shade: 0.3,
                maxmin: false,
                success: function (layero, index) {
                },
                end: function () {
                }
            });
        }
        function deleteCustomerAddress(addressId) {
            top.layer.confirm("确定删除该地址吗?", {icon: 3, title:'系统确认'}, function(index){
                top.layer.close(index);//关闭本身
                // do something
                $.ajax({
                    type: "GET",
                    url: "${ctx}/fi/md/customer/deleteCustomerAddress?addressId=" + addressId,
                    success: function (data) {
                        if (data.success){
                            layerMsg("删除成功",true)
                            delRow(addressId);
                        }
                        else{
                            layerError(data.message,"错误提示",true);
                        }
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        layerError(thrownError,"错误提示",true);
                    }
                });
            },function(index){
                //cancel
            });
            return false;
        }

        // 关闭页面
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }

        function refresh(userName, contactInfo, areaId, areaName, address,addressId,isDefault,editType) {
            var customerId = $("#customerId").val();
            var id = Math.ceil(Math.random()*1000);
            if (customerId == '') {
                customerId = null;
            }
            var length = $("#tbody").children("tr").length;

            if(isDefault == ''){
                isDefault = 0;
            }
            if(editType == 10){
                var index = 2;
                if(length > 0){
                    index+=length;
                }
                if(customerId != null){
                    id = addressId;
                }
                var trTemp = $("<tr id='"+id+"'></tr>");
                if(isDefault == 1){
                    trTemp.append("<td>" + userName + "</td><td ><div id='mor_isDefault_" + id +"' class='default'>默认</div>" + areaName + address + "</td>");
                }else {
                    trTemp.append("<td>" + userName + "</td><td >"+ areaName + address + "</td>");
                }

                trTemp.append("<input type='hidden' name='customerAddresses["+ index + "].addressType' value='3'>" +
                    "<input type='hidden' id='userName_"+id+"' name='customerAddresses["+ index + "].userName' value='"+userName+"'>" +
                    "<input type='hidden' id='contactInfo_"+id+"' name='customerAddresses["+ index + "].contactInfo' value='"+contactInfo+"'>" +
                    "<input type='hidden' id='address_"+id+"' name='customerAddresses["+ index + "].address' value='"+address+"'>" +
                    "<input type='hidden' id='areaId_"+id+"' name='customerAddresses["+ index + "].areaId' value='"+areaId+"'>" +
                    "<input type='hidden' id='areaName_"+id+"' name='customerAddresses["+ index + "].areaName' value='"+areaName+"'>" +
                    "<input type='hidden' id='isDefault_"+id+"' name='customerAddresses["+ index + "].isDefault' value='"+isDefault+"'>");
                if(customerId == null){
                    trTemp.append('<td><a  onclick="updateCustomerAddresses(\''+id+'\',\''+userName+'\',\''+contactInfo+'\',\''+areaId+'\',\''+areaName+'\',\''+address+'\',\''+isDefault+'\',\'3\',\''+editType+'\')">修改</a><a style="margin-left: 16px" onclick="delRow('+id+')">删除</a></td>');
                }else {
                    trTemp.append('<td><a  onclick="editCustomerAddresses(\''+customerId+'\',\''+3+'\',\''+addressId+'\',\''+20+'\')">修改</a><a style="margin-left: 16px" onclick="deleteCustomerAddress('+id+')">删除</a></td>');
                }
                $("#tbody").append(trTemp);
            }else {
                $('#' + addressId).children('td').eq(0).text(userName);
                // $('#' + addressId).children('td').eq(1).text(contactInfo);
                if(isDefault == 1){
                    $('#' + addressId).children('td').eq(1).html("<div id='mor_isDefault_" + addressId +"' class='default'>默认</div>" + areaName + " " +address);
                } else {
                    $('#' + addressId).children('td').eq(1).text(areaName + " " +address);
                }

                $('#userName' + addressId).val(userName);
                $('#contactInfo_' + addressId).val(contactInfo);
                $('#address_' + addressId).val(address);
                $('#areaId_' + addressId).val(areaId);
                $('#isDefault_' + addressId).val(isDefault);
                $('#' + addressId).children('td').find("a").eq(0).text("修改");
                if(customerId == null){
                    var url = 'updateCustomerAddresses(\''+ addressId + '\',\'' + userName + '\',\'' + contactInfo + '\',\'' + areaId + '\',\'' + areaName + '\',\'' + address + '\',\'' + isDefault + '\',\'' + 3 + '\',\'' + editType + '\')';
                    $('#' + addressId).children('td').find('a').eq(0).attr("onclick", url);
                    $('#' + addressId).children('td').find("a").eq(1).text("删除");
                    var deleteUrl = "javascript:delRow(" + addressId + ")";
                    $('#' + addressId).children('td').find('a').eq(1).attr("onclick", deleteUrl);
                    $('#' + addressId).children('td').find('a').eq(1).css("margin-left", "16px");
                }else {
                    var url = "javascript:editCustomerAddresses(" + customerId + "," + 3 + "," + addressId + "," + 20 + ")";
                    $('#' + addressId).children('td').find('a').eq(0).attr("onclick", url);
                    $('#' + addressId).children('td').find("a").eq(1).text("删除");
                    var deleteUrl = "javascript:deleteCustomerAddress(" + addressId + ")";
                    $('#' + addressId).children('td').find('a').eq(1).attr("onclick", deleteUrl);
                    $('#' + addressId).children('td').find('a').eq(1).css("margin-left", "16px");
                }



            }
            if(isDefault == 1){
                $("input[id^='isDefault']").each(function (index, domEle) {
                    var trId = $(this).prop("id");
                    var a;
                    if(editType == 10){
                        a = "isDefault_" + id;
                    }else {
                        a = "isDefault_" + addressId;
                    }
                    if(trId != a){
                        $(this).val(0);
                        $("#mor_"+ trId).remove();
                    }
                });
            }

        }
        function delRow(id) {
            $('#' + id).remove();
        }
        function refreshFinance(type, bank, bankName, branch, account, name) {
            $('#finanaType_' + type).children('td').eq(1).text(bankName);
            $('#finanaType_' + type).children('td').eq(2).text(branch);
            $('#finanaType_' + type).children('td').eq(3).text(account);
            $('#finanaType_' + type).children('td').eq(4).text(name);
            $('#finanaType_' + type).find("input").eq(0).val(bank);
            $('#finanaType_' + type).find("input").eq(1).val(branch);
            $('#finanaType_' + type).find("input").eq(2).val(account);
            $('#finanaType_' + type).find("input").eq(3).val(name);

            $('#finanaType_' + type).children('td').find("a").text("修改");

            var customerId = $("#customerId").val();
            var url = "javascript:editCustomerFinance(" + customerId + "," + type + ")";
            $('#finanaType_' + type).children('td').find('a').attr("href", url);
        }

        function refreshState(id, name) {
            $('#merchandiserName').val(name);
            $('#merchandiserId').val(id);

        }

        function refreshSales(id, name) {
            $('#salesId').val(id);
            $('#salesName').val(name);

        }

    </script>
</head>
<body>
<br/>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="inputForm" modelAttribute="customer" action="${ctx}/fi/md/customer/save" method="post"
           class="form-horizontal">
<form:hidden path="id"/>
<form:hidden path="finance.balance"/>
    <form:hidden path="returnAddress"/>
<input type="hidden" id="masterPhone" value="${customer.phone}"/>
<sys:message content="${message}"/>
<c:set var="customerId" value="${customer.id}"></c:set>
<input type="hidden" id="customerId" value="${customerId}">
<legend style="margin-left: 36px;margin-top: 25px">客户信息<div class="line_"></div></legend>
<div class="">
    <div class="line-row">
        <div class="control-group x">

            <label class="control-label"><span class="red">*</span>名&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp称:</label>
            <div class="controls">
                <form:input path="name" htmlEscape="false" maxlength="30" class="required" style="width:236px;"/>

            </div>
        </div>
    </div>
    <div>
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label"><span class="red">*</span>全&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp称:</label>
                <div class="controls">
                    <form:input path="fullName" htmlEscape="false" maxlength="60" class="required"
                                style="width:236px;"/>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="line-row" style="width: 949px;float: left">
    <div class="control-group x">
        <label class="control-label"><span class="red">*</span>负&nbsp责&nbsp人:</label>
        <div class="controls">
            <form:input id="master" path="master" htmlEscape="false" class="required" maxlength="20" style="width:236px;"/>
            <input id="userId" type="hidden" value="${customer.userId}"/>
        </div>
    </div>
    <div class="control-group x" style="margin-left: -3px;">
        <label class="control-label"><c:if test="${empty customer.id}"><span class="red">*</span></c:if>联系电话:</label>
        <div class="controls">
<%--            <form:input id="phone" path="phone" type="tel" htmlEscape="false" class="input-y required mobile" maxlength="11"--%>
<%--                        style="width:236px;"/>--%>
            <input id="phone" name="phone" type="tel" value="" maxlength="11" class=" ${empty customer.id?'input-y required mobile':'mobile'}" style="width: 236px;"/>
        </div>
    </div>
</div>
<div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label"><span class="red">*</span>业&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp务:</label>
            <c:choose>
                <c:when test="${empty customer.id}">
                    <div class="controls">
                        <form:select path="sales.id" cssClass="required input-medium" cssStyle="width: 250px;">
                            <form:options items="${fns:getSaleList()}" itemLabel="name" itemValue="id"
                                          htmlEscape="false"/>
                        </form:select>

                    </div>
                </c:when>
                <c:otherwise>
                    <div class="controls" style="width: 320px">
                        <form:input path="sales.name" id="salesName" htmlEscape="false" maxlength="20" readonly="true"
                                    style="width:236px;"/>
                        <form:hidden path="sales.id" id="salesId"/>
                        <input id="btnUpdateSales" class="btn btn-primary" type="button" value="修改"/>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div class="line-row">
        <div class="control-group x" style="margin-left: 50px">
            <label class="control-label" style="width: 111px"><span class="red">*</span>跟&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp单:</label>
            <c:choose>
                <c:when test="${empty customer.id}">
                    <div class="controls" style="margin-left: 115px;">
                        <form:select path="merchandiser.id" cssClass="input-medium" cssStyle="width: 250px;">
                            <form:options items="${fns:getMerchandiserList()}" itemLabel="name" itemValue="id"
                                          htmlEscape="false"/>
                        </form:select>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="controls" style="width: 320px;margin-left: 115px;">
                        <form:input path="merchandiser.name" id="merchandiserName" htmlEscape="false" maxlength="20"
                                    readonly="true" style="width:236px;"/>
                        <form:hidden path="merchandiser.id" id="merchandiserId"/>
                        <input id="btnUpdateMerchandiser" class="btn btn-primary" type="button" value="修改"/>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
<div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">分&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp类:</label>
            <div class="controls">
                <form:select path="contractFlag" cssClass="input-medium" cssStyle="width: 250px;">
                    <form:options items="${customerSignClassify}" itemLabel="label" itemValue="value"
                                  htmlEscape="false"/>
                </form:select>
            </div>
        </div>
    </div>

    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">签约日期:</label>
            <div class="controls">
                <input id="contractDate" name="contractDate" type="text"
                       readonly="readonly" style="width:236px;"
                       maxlength="10" class="input-small Wdate"
                       value="<fmt:formatDate value='${customer.contractDate}' pattern='yyyy-MM-dd'/>"
                       onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
            </div>
        </div>
    </div>
</div>

<div>
    <div>
        <div style="float: left;">
            <label class="control-label">完成照片:</label>
            <div class="controls">
                <form:input path="minUploadNumber" htmlEscape="false" type="number"
                            cssclass="{required:true,min:0,max:20}" style="width:100px;"/>
            </div>
        </div>
    </div>

    <div>
        <div style="float: left;margin-left: 5px">
            <label>一</label>
            <form:input path="maxUploadNumber" htmlEscape="false" type="number" cssclass="{required:true,min:0,max:20}"
                        style="width:99px;"/>
        </div>
    </div>

    <div class="line-row">
        <div class="control-group x">
            <label class="control-label" style="margin-left: 18px">编&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp号:</label>
            <div class="controls" style="width: 290px;">
                <input id="oldCode" name="oldCode" type="hidden" value="${customer.code}">
                <input id="oldCredit" name="oldCredit" type="hidden" value="${customer.finance.credit}">
                <input type="text" id="txtcode" name="txtcode" value="${customer.code}" placeholder="保存信息后自动生成"
                       maxlength="6" disabled="true" style="margin-left: 6px;width: 236px"/>

            </div>
        </div>
    </div>
    <div style="float: left">
        <label style="margin-left: 165px;color: #999999;width: 640px;">完工上传照片的数量要求</label>
    </div>

    <div class="line-row">
        <c:forEach items="${customer.customerAddresses}" var="customerAddress" varStatus="c">
        <c:set var="i" value="${c.index}"></c:set>
            <c:if test="${i == 0}">
        <div class="control-group x" style="width: 99%">
            <label class="control-label">地&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp址:</label>
            <div class="controls" style="width: 684px;">
                        <sys:areaselect name="areaId" id="customerAddress_${c.index}" value="${customerAddress.areaId}"
                                        labelValue="${customerAddress.areaName}" labelName="areaName" title=""
                                        mustSelectCounty="true"  cssStyle="width: 194px;">
                        </sys:areaselect>
                        <input type="hidden" name="customerAddresses[${c.index}].id"
                                value="${customerAddress.id}">
                        <input type="hidden" id="userName_0"name="customerAddresses[${c.index}].userName"
                                value="${customer.master}">
                        <input type="hidden" id="contactInfo_0" name="customerAddresses[${c.index}].contactInfo"
                                value="${customer.phone}">
                        <input type="hidden" name="customerAddresses[${c.index}].addressType"
                                value="1">
                        <input type="hidden" id="areaId_0" name="customerAddresses[${c.index}].areaId"
                               value="${customerAddress.areaId}">
                        <input type="hidden" id="areaName_0" name="customerAddresses[${c.index}].areaName"
                               value="${customerAddress.areaName}">

                        <input id="address${c.index}" name="customerAddresses[${c.index}].address" value="${customerAddress.address}"  type="text"maxlength="100" style="width:416px;" placeholder="详细地址，如XX大厦1层101室"/>


            </div>
        </div>
            </c:if>

            <c:if test="${i == 1}">
            <div class="control-group x">
                <label class="control-label">发货人:</label>
                <div class="controls" style="width: 269px;">
                    <input name="customerAddresses[${c.index}].userName" value="${customerAddress.userName}"  type="text"maxlength="20" style="width:236px;"/>

                </div>
            </div>
                <div class="control-group x">
                    <label class="control-label">联系电话:</label>
                    <div class="controls" style="width: 269px;">
                        <input name="customerAddresses[${c.index}].contactInfo" value=""  type="text"maxlength="16" style="width:236px;"/>
                    </div>
                </div>

                <div class="control-group x" style="width: 99%">
                    <label class="control-label">发货地址:</label>
                    <div class="controls" style="width: 684px;">
                        <sys:areaselect name="areaId" id="customerAddress_${c.index}" value="${customerAddress.areaId}"
                                        labelValue="${customerAddress.areaName}" labelName="areaName" title=""
                                        mustSelectCounty="true"  cssStyle="width: 194px;">
                        </sys:areaselect>
                        <input type="hidden" name="customerAddresses[${c.index}].id"
                               value="${customerAddress.id}">
                        <input type="hidden" id="areaId_1" name="customerAddresses[${c.index}].areaId"
                               value="${customerAddress.areaId}">
                        <input type="hidden" id="areaName_1" name="customerAddresses[${c.index}].areaName"
                               value="${customerAddress.areaName}">

                        <input id="address${c.index}" name="customerAddresses[${c.index}].address" value="${customerAddress.address}"  type="text"maxlength="100" style="width:416px;" placeholder="详细地址，如XX大厦1层101室"/>
                        <input type="hidden" name="customerAddresses[${c.index}].addressType"
                               value="2">
                    </div>
                </div>
            </c:if>
        </c:forEach>
    </div>


</div>
<div style="float: left;margin-top: 15px">

    <div class="control-group x" style="width:525px">
        <label class="control-label">返件信息:</label>
        <div style="width: 682px;margin-left: 165px;border: 1px solid #ccc">
            <table id="contentTable" class="table  table-bordered table-condensed table-hover"
                   style="margin-bottom:0px;">
                <thead>
                <tr>
                    <th width="80">联系人</th>
                    <th width="330">返件地址</th>
                    <th width="120">操作</th>
                </tr>
                </thead>
                <tbody id="tbody">
                <c:forEach items="${customer.customerAddresses}" var="addresses" varStatus="cou">
                    <c:set var="i" value="${cou.index}"></c:set>
                    <c:if test="${i >= 2}">
                        <c:if test="${customerId != null}">
                    <tr id="${addresses.id}">
                        <td>${addresses.userName}</td>
                        <td style="text-align: left">
                            <c:if test="${addresses.isDefault == 1}">
                                <div id="mor_isDefault_${addresses.id}"  class="default">默认</div>
                            </c:if>
                                ${addresses.areaName}  ${addresses.address}
                        </td>
                        <input type="hidden" id="addressType_${addresses.id}"
                               name="customerAddresses[${cou.index}].addressType" value="${addresses.addressType}">
                        <input type="hidden" id="userName_${addresses.id}"
                               name="customerAddresses[${cou.index}].userName" value="${addresses.userName}">
                        <input type="hidden" id="contactInfo_${addresses.id}"
                               name="customerAddresses[${cou.index}].contactInfo" value="${addresses.contactInfo}">
                        <input type="hidden" id="address_${addresses.id}"
                               name="customerAddresses[${cou.index}].address" value="${addresses.address}">
                        <input type="hidden" id="areaId_${addresses.id}"
                               name="customerAddresses[${cou.index}].areaId" value="">
                        <input type="hidden" id="areaName_${addresses.id}"
                               name="customerAddresses[${cou.index}].areaName" value="">

                        <input type="hidden" id="isDefault_${addresses.id}"
                               name="customerAddresses[${cou.index}].isDefault" value="0">
                        <td>
                            <a onclick="editCustomerAddresses('${customerId}','${addresses.addressType}','${addresses.id}',20)">修改</a>
                            <a style="margin-left: 16px" onclick="deleteCustomerAddress('${addresses.id}')">删除</a>
                        </td>
                    </tr>
                        </c:if>
                    </c:if>
                </c:forEach>
                </tbody>

            </table>
            <div >
                <button type="button" style="margin-top: 12px;margin-left:18px;margin-bottom: 12px;border-radius: 4px;border:1px solid;border-color:#0096DA;background-color: #0096DA;color:#fff;width: 86px;height: 26px" onclick="editCustomerAddresses('${customerId}',3,null,10)">
                    <i class="icon-plus-sign"></i>&nbsp;添加地址
                </button>
            </div>
        </div>
    </div>

    <div class="control-group x">
        <label class="control-label">描&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp述:</label>
        <div class="controls">
            <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="250" style="width:670px;"/>
        </div>
    </div>
</div>
<legend style="float: left;margin-left: 36px;margin-top: 25px">控制开关<div class="line_"></div></legend>
    <div class="row-fluid">
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">客户下单:</label>
            <div class="controls" style="margin-top: 3px">
                <c:choose>
                    <c:when test="${customer.id !=null and customer.id>0}">
                        <c:choose>
                            <c:when test="${customer.effectFlag==0}">
                                <span class="switch-off" id="spanEffectFlag" style="width: 40px;height: 20px"></span>
                            </c:when>
                            <c:otherwise>
                                <span class="switch-on" id="spanEffectFlag" style="width: 40px;height: 20px"></span>
                            </c:otherwise>
                        </c:choose>
                        <input type="hidden" value="${customer.effectFlag}" name="effectFlag" id="effectFlag">
                    </c:when>
                    <c:otherwise>
                        <span class="switch-on" id="spanEffectFlag" style="width: 40px;height: 20px"></span>
                        <input type="hidden" value="1" name="effectFlag" id="effectFlag">
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </div>

    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">催&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp单:</label>
            <div class="controls" style="margin-top: 3px">
                <c:choose>
                    <c:when test="${customer.id !=null and customer.id>0}">
                        <c:choose>
                            <c:when test="${customer.reminderFlag==0}">
                                <span class="switch-off" id="spanReminderFlag" style="width: 40px;height: 20px"></span>
                            </c:when>
                            <c:otherwise>
                                <span class="switch-on" id="spanReminderFlag" style="width: 40px;height: 20px"></span>
                            </c:otherwise>
                        </c:choose>
                        <input type="hidden" value="${customer.reminderFlag}" name="reminderFlag" id="reminderFlag">
                    </c:when>
                    <c:otherwise>
                        <span class="switch-on" id="spanReminderFlag" style="width: 40px;height: 20px"></span>
                        <input type="hidden" value="1" name="reminderFlag" id="reminderFlag">
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </div>
    </div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">加&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp急:</label>
            <div class="controls" style="margin-top: 3px">
                <shiro:hasPermission name="fi:md:customer:urgent">
                    <c:choose>
                        <c:when test="${customer.id !=null and customer.id>0}">
                            <c:choose>
                                <c:when test="${customer.urgentFlag==0}">
                                    <span class="switch-off" id="spanUrgentFlag"
                                          style="width: 40px;height: 20px"></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="switch-on" id="spanUrgentFlag" style="width: 40px;height: 20px"></span>
                                </c:otherwise>
                            </c:choose>
                            <input type="hidden" value="${customer.urgentFlag}" name="urgentFlag" id="urgentFlag">
                        </c:when>
                        <c:otherwise>
                            <span class="switch-off" id="spanUrgentFlag" style="width: 40px;height: 20px"></span>
                            <input type="hidden" value="0" name="urgentFlag" id="urgentFlag">
                        </c:otherwise>
                    </c:choose>
                </shiro:hasPermission>

                <shiro:lacksPermission name="fi:md:customer:urgent">
                    <c:choose>
                        <c:when test="${customer.urgentFlag == 1}">
                            <span class="switch-on switch-disabled" style="width: 40px;height: 20px" disabled="true"></span>
                            <input type="hidden" value="1" name="urgentFlag" id="urgentFlag">
                        </c:when>
                        <c:otherwise>
                            <span class="switch-off switch-disabled" style="width: 40px;height: 20px" disabled="true"></span>
                            <input type="hidden" value="0" name="urgentFlag" id="urgentFlag">
                        </c:otherwise>
                    </c:choose>
                </shiro:lacksPermission>
            </div>
        </div>
    </div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">远程费用:</label>
            <div class="controls" style="margin-top: 3px">
                <c:choose>
                    <c:when test="${customer.id !=null and customer.id>0}">
                        <c:choose>
                            <c:when test="${customer.remoteFeeFlag==0}">
                                <span class="switch-off" id="spanRemoteFeeFlag" style="width: 40px;height: 20px"></span>
                            </c:when>
                            <c:otherwise>
                                <span class="switch-on" id="spanRemoteFeeFlag" style="width: 40px;height: 20px"></span>
                            </c:otherwise>
                        </c:choose>
                        <input type="hidden" value="${customer.remoteFeeFlag}" name="remoteFeeFlag" id="remoteFeeFlag">
                    </c:when>
                    <c:otherwise>
                        <span class="switch-on" id="spanRemoteFeeFlag" style="width: 40px;height: 20px"></span>
                        <input type="hidden" value="1" name="remoteFeeFlag" id="remoteFeeFlag">
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </div>


<div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">时效奖励:</label>
            <div class="controls" style="margin-top: 3px">
                <shiro:hasPermission name="fi:md:customer:timeliness">
                    <c:choose>
                        <c:when test="${customer.id !=null and customer.id>0}">
                            <c:choose>
                                <c:when test="${customer.timeLinessFlag==0}">
                                    <span class="switch-off" id="spanTimeLinessFlag"
                                          style="width: 40px;height: 20px"></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="switch-on" id="spanTimeLinessFlag"
                                          style="width: 40px;height: 20px"></span>
                                </c:otherwise>
                            </c:choose>
                            <input type="hidden" value="${customer.timeLinessFlag}" name="timeLinessFlag"
                                   id="timeLinessFlag">
                        </c:when>
                        <c:otherwise>
                            <span class="switch-off" id="spanTimeLinessFlag" style="width: 40px;height: 20px"></span>
                            <input type="hidden" value="0" name="timeLinessFlag" id="timeLinessFlag">
                        </c:otherwise>
                    </c:choose>
                </shiro:hasPermission>
                <shiro:lacksPermission name="fi:md:customer:timeliness">
                    <c:choose>
                        <c:when test="${customer.timeLinessFlag == 1}">
                            <span class="switch-on switch-disabled" style="width: 40px;height: 20px" ></span>
                            <input type="hidden" value="1" name="timeLinessFlag" id="timeLinessFlag">
                        </c:when>
                        <c:otherwise>
                            <span class="switch-off switch-disabled" style="width: 40px;height: 20px" ></span>
                            <input type="hidden" value="0" name="timeLinessFlag" id="timeLinessFlag">
                        </c:otherwise>
                    </c:choose>
                </shiro:lacksPermission>
            </div>
        </div>
    </div>

    <div class="line-row">
        <div class="control-group x" style="width: 500px">
            <label class="control-label">短信发送:</label>
            <div class="controls" style="margin-top: 3px">
                <c:choose>
                    <c:when test="${customer.id !=null and customer.id>0}">
                        <c:choose>
                            <c:when test="${customer.shortMessageFlag==0}">
                                <span class="switch-off" id="spanShortMessageFlag"
                                      style="width: 40px;height: 20px"></span>
                            </c:when>
                            <c:otherwise>
                                <span class="switch-on" id="spanShortMessageFlag"
                                      style="width: 40px;height: 20px"></span>
                            </c:otherwise>
                        </c:choose>
                        <input type="hidden" value="${customer.shortMessageFlag}" name="shortMessageFlag"
                               id="shortMessageFlag">
                    </c:when>
                    <c:otherwise>
                        <span class="switch-on" id="spanShortMessageFlag" style="width: 40px;height: 20px"></span>
                        <input type="hidden" value="1" name="shortMessageFlag" id="shortMessageFlag">
                    </c:otherwise>
                </c:choose>
                <span class="help-inline"
                      style="margin-top: -13px;color: #808695">师傅接单短信,预约时间短信,用户</br>评价短信。</span>
            </div>
        </div>
    </div>
</div>

<div class="row-fluid">

    <div class="line-row" style="float: left;width: 100%">

        <div class="control-group x">
            <label class="control-label">线下下单:</label>
            <div class="controls">
                <c:choose>
                    <c:when test="${customer.id !=null and customer.id>0}">
                        <c:choose>
                            <c:when test="${customer.offlineOrderFlag==0}">
                                <span class="switch-off" id="spanOfflineOrderFlag" style="width: 40px;height: 20px"></span>
                            </c:when>
                            <c:otherwise>
                                <span class="switch-on" id="spanOfflineOrderFlag" style="width: 40px;height: 20px"></span>
                            </c:otherwise>
                        </c:choose>
                        <input type="hidden" value="${customer.offlineOrderFlag}" name="offlineOrderFlag" id="offlineOrderFlag">
                    </c:when>
                    <c:otherwise>
                        <span class="switch-off" id="spanOfflineOrderFlag" style="width: 40px;height: 20px"></span>
                        <input type="hidden" value="0" name="offlineOrderFlag" id="offlineOrderFlag">
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
        <div class="control-group x">
            <label class="control-label">自动完工:</label>
            <div class="controls">
                <c:choose>
                    <c:when test="${customer.id !=null and customer.id>0}">
                        <c:choose>
                            <c:when test="${customer.autoCompleteOrder==0}">
                                <span class="switch-off" id="spanAutoCompleteOrder" style="width: 40px;height: 20px"></span>
                            </c:when>
                            <c:otherwise>
                                <span class="switch-on" id="spanAutoCompleteOrder" style="width: 40px;height: 20px"></span>
                            </c:otherwise>
                        </c:choose>
                        <input type="hidden" value="${customer.autoCompleteOrder}" name="autoCompleteOrder" id="autoCompleteOrder">
                    </c:when>
                    <c:otherwise>
                        <span class="switch-on" id="spanAutoCompleteOrder" style="width: 40px;height: 20px"></span>
                        <input type="hidden" value="1" name="autoCompleteOrder" id="autoCompleteOrder">
                    </c:otherwise>
                </c:choose>

            </div>
        </div>


    </div>

    <div class="control-group x" >
        <label class="control-label">VIP客户:</label>
        <div class="controls">
            <c:set var="canEdit" value="true" />
            <shiro:hasPermission name="fi:md:customer:vip">
                <c:set var="canEdit" value="true" />
            </shiro:hasPermission>
            <shiro:lacksPermission name="fi:md:customer:vip">
                <form:hidden path="vipFlag"/>
                <form:hidden path="vip"/>
                <c:set var="canEdit" value="false" />
            </shiro:lacksPermission>
            <span>
                <form:radiobutton path="vipFlag" value="1" cssClass="required" disabled="${canEdit eq true?'false':'true'}"></form:radiobutton>是
            </span>
            <span>
                <form:radiobutton path="vipFlag" value="0" cssClass="required" disabled="${canEdit eq true?'false':'true'}"></form:radiobutton>否
            </span>
            <form:select path="vip" class="input-large" style="width:150px;margin-left: 10px;" disabled="${canEdit eq true?'false':'true'}">
                <form:options items="${customerVipLevelList}" itemLabel="name" itemValue="value" htmlEscape="false" />
            </form:select>
        </div>
        <div style="padding-top:6px;padding-bottom:6px;margin-left: 107px;color: #808695;width: 295px;">
            注:等级${customerLevel.name}以下(不含${customerLevel.name})时自动区域由自动客服部处理，${customerLevel.name}以上(含${customerLevel.name})时所有区域由KA处理
        </div>
    </div>

    <div class="control-group x">
        <label class="control-label">使用配件:</label>
        <div class="controls">
						<span>
							<form:radiobutton path="materialFlag" value="1" cssClass="required"></form:radiobutton>客户配件
						</span>
            <span>
							<form:radiobutton path="materialFlag" value="0" cssClass="required"></form:radiobutton>平台配件
						</span>

        </div>
    </div>

    <div class="control-group x">
        <label class="control-label">使用故障:</label>
        <div class="controls">
                            <span>
                                <form:radiobutton path="errorFlag" value="1" cssClass="required"></form:radiobutton>客户故障
                            </span>
            <span>
                                <form:radiobutton path="errorFlag" value="0" cssClass="required"></form:radiobutton>平台故障
                            </span>

        </div>
    </div>


</div>
<legend style="margin-left: 36px;margin-top: 25px">职务联系<div class="line_"></div></legend>
<div class="row-fluid">
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label"><span class="red">*</span>项目负责人:</label>
            <div class="controls">
                <form:input path="projectOwner" htmlEscape="false" maxlength="10" class="required"/>

            </div>
        </div>
    </div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label"><c:if test="${empty customer.id}"><span class="red">*</span></c:if>联系电话:</label>
            <div class="controls">

                <input id="projectOwnerPhone" name="projectOwnerPhone" htmlEscape="false" type="tel" value="" maxlength="16" class=" ${empty customer.id?'required phone':'phone'}" style="width: 236px;" placeholder="填写项目负责人手机号码"/>
            </div>
        </div>
    </div>
</div>

    <div class="row-fluid">
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label">Q&nbsp&nbsp&nbsp&nbsp&nbsp&nbspQ:</label>
                <div class="controls">
<%--                    <form:input path="projectOwnerQq" htmlEscape="false" maxlength="11" placeholder="填写项目负责人QQ"/>--%>
                    <input id="projectOwnerQq" name="projectOwnerQq" placeholder="填写项目负责人QQ" type="text" value="" maxlength="11" class="valid" aria-invalid="false">
                </div>
            </div>
        </div>

    </div>
<div class="row-fluid">
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label"><span class="red">*</span>售后负责人:</label>
            <div class="controls">
                <form:input path="serviceOwner" htmlEscape="false" maxlength="10" class="required"/>

            </div>
        </div>
    </div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label"><c:if test="${empty customer.id}"><span class="red">*</span></c:if>联系电话:</label>
            <div class="controls">
                <input id="serviceOwnerPhone" name="serviceOwnerPhone" type="tel" value="" maxlength="16" class=" ${empty customer.id?'required phone':'phone'}" style="width: 236px;" placeholder="填写售后负责人手机号码"/>
            </div>
        </div>
    </div>
</div>
    <div class="row-fluid">
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label">Q&nbsp&nbsp&nbsp&nbsp&nbsp&nbspQ:</label>
                <div class="controls">
<%--                    <form:input path="serviceOwnerQq" htmlEscape="false" maxlength="11"  placeholder="填写售后负责人QQ"/>--%>
                    <input id="serviceOwnerQq" name="serviceOwnerQq" placeholder="填写售后负责人QQ" type="text" value="" maxlength="11" class="valid" aria-invalid="false">
                </div>
            </div>
        </div>

    </div>
<div class="row-fluid">
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">财务负责人:</label>
            <div class="controls">
                <form:input path="financeOwner" htmlEscape="false" maxlength="10" />
            </div>
        </div>
    </div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">联系电话:</label>
            <div class="controls">
                <input id="financeOwnerPhone" name="financeOwnerPhone" type="tel" value="" maxlength="16" class="phone" style="width: 236px;" placeholder="填写财务负责人手机号码"/>
            </div>
        </div>
    </div>
</div>
    <div class="row-fluid">
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label">Q&nbsp&nbsp&nbsp&nbsp&nbsp&nbspQ:</label>
                <div class="controls">
<%--                    <form:input path="financeOwnerQq" htmlEscape="false" maxlength="11"   placeholder="填写财务负责人QQ"/>--%>
                    <input id="financeOwnerQq" name="financeOwnerQq" placeholder="填写财务负责人QQ" type="text" value="" maxlength="11" class="valid" aria-invalid="false">
                </div>
            </div>
        </div>

    </div>
<div class="row-fluid">
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">技术负责人:</label>
            <div class="controls">
                <form:input path="technologyOwner" htmlEscape="false" maxlength="10" />
            </div>
        </div>
    </div>
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">联系电话:</label>
            <div class="controls">
                <input id="technologyOwnerPhone" name="technologyOwnerPhone" type="tel" value="" maxlength="16" class="phone" style="width: 236px;" placeholder="填写技术负责人手机号码"/>
            </div>
        </div>
    </div>
</div>
    <div class="row-fluid">
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label">Q&nbsp&nbsp&nbsp&nbsp&nbsp&nbspQ:</label>
                <div class="controls">
<%--                    <form:input path="technologyOwnerQq" htmlEscape="false" maxlength="11"   placeholder="填写技术负责人QQ"/>--%>
                    <input id="technologyOwnerQq" name="technologyOwnerQq" placeholder="填写技术负责人QQ" type="text" value="" maxlength="11" class="valid" aria-invalid="false">
                </div>
            </div>
        </div>

    </div>
<legend style="margin-left: 36px;margin-top: 25px">财务信息<div class="line_"></div></legend>
<div class="row-fluid">
    <div class="line-row">
        <div class="control-group x">
            <label class="control-label">结算方式:</label>
            <div class="controls">
                <form:select path="finance.paymentType.value" cssClass="required input-medium" cssStyle="width: 220px;">
                    <form:options items="${fns:getDictExceptListFromMS('PaymentType', '20')}"
                                  itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                </form:select>
            </div>
        </div>
    </div>
    <div class="control-group x">
        <label class="control-label"><span class="red">*</span>价格属性:</label>
        <div class="controls" style="margin-top: 2px">
						<span>
						    <form:radiobutton path="customizePriceFlag" value="0" cssClass="required"></form:radiobutton>标准价
						</span>
                        <span style="margin-left: 10px">
                            <form:radiobutton path="customizePriceFlag" value="1" cssClass="required" ></form:radiobutton>自定义
						</span>

        </div>
    </div>

</div>
    <div class="row-fluid">
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label">账户金额:</label>
                <div class="controls">
                    <span class="input-large uneditable-input" style="width: 206px;">${customer.finance.balance}</span>
                </div>
            </div>
        </div>
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label">使用价格:</label>
                <div class="controls">
                    <input id="useDefaultPrice" name="useDefaultPrice" type="hidden" value="10">
                    <input type="text"  placeholder="第一轮价格" maxlength="6" disabled="true" />

                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="line-row">
            <div class="control-group x" style="width: 435px">
                <label class="control-label">信用额度:</label>
                <div class="controls" style="margin-top: 3px">
                    <c:choose>
                        <c:when test="${customer.id !=null and customer.id>0}">
                            <c:choose>
                                <c:when test="${customer.finance.creditFlag==0}">
                                    <span class="switch-off" id="spanCreditFlag"
                                          style="width: 40px;height: 20px"></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="switch-on" id="spanCreditFlag" style="width: 40px;height: 20px"></span>
                                </c:otherwise>
                            </c:choose>
                            <input type="hidden" value="${customer.finance.creditFlag}" name="finance.creditFlag"
                                   id="finance.creditFlag">
                        </c:when>
                        <c:otherwise>
                            <span class="switch-off" id="spanCreditFlag" style="width: 40px;height: 20px"></span>
                            <input type="hidden" value="0" name="finance.creditFlag" id="finance.creditFlag">
                        </c:otherwise>
                    </c:choose>

                    <shiro:hasPermission name="fi:md:customer:deposit">
                        <form:input path="finance.credit" htmlEscape="false" maxlength="10" class="required number"
                                    style="margin-top: -15px;width: 160px;"/>
                    </shiro:hasPermission>
                    <shiro:lacksPermission name="fi:md:customer:deposit">
                        <form:input path="finance.credit" htmlEscape="false" readonly="true"
                                    style="margin-top: -15px;width: 160px;"/>
                    </shiro:lacksPermission>

                </div>
            </div>

            <div class="line-row">
                <div class="control-group x">
                    <label class="control-label"><span class="red">*</span>押&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp金:</label>
                    <div class="controls">
                        <shiro:hasPermission name="fi:md:customer:deposit">
                            <form:input path="finance.deposit" htmlEscape="false" type="number"
                                        maxlength="7" class="required number"/>
                        </shiro:hasPermission>
                        <shiro:lacksPermission name="fi:md:customer:deposit">
                            <form:input path="finance.deposit" htmlEscape="false" readonly="true"/>
                        </shiro:lacksPermission>
                    </div>
                </div>
            </div>
        </div>

    </div>
    <div class="row-fluid">
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label">纳税人代码:</label>
                <div class="controls">
                    <form:input path="finance.taxpayerCode" htmlEscape="false" maxlength="64"/>
                </div>
            </div>
        </div>
        <shiro:hasPermission name="sd:customerinvoice:edit">

            <div class="line-row">
                <div class="control-group x" style="color: red;width: 214px">
                    <label class="control-label"> 结账锁:</label>
                    <div class="controls" style="margin-top: 3px">
                        <c:choose>
                            <c:when test="${customer.id !=null and customer.id>0}">
                                <c:choose>
                                    <c:when test="${customer.finance.lockFlag==0}">
                                        <span class="switch-off" id="financeLockFlag"
                                              style="width: 40px;height: 20px"></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="switch-on" id="financeLockFlag"
                                              style="width: 40px;height: 20px"></span>
                                    </c:otherwise>
                                </c:choose>
                                <input type="hidden" value="${customer.finance.lockFlag}" name="finance.lockFlag"
                                       id="finance.lockFlag">
                            </c:when>
                            <c:otherwise>
                                <span class="switch-off" id="financeLockFlag" style="width: 40px;height: 20px"></span>
                                <input type="hidden" value="0" name="finance.lockFlag" id="finance.lockFlag">
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

        </shiro:hasPermission>
    </div>

    <div class="row-fluid">
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label"><span class="red">*</span>等&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp级:</label>
                <div class="controls">
                    <form:select path="finance.level" cssClass="required input-medium" cssStyle="width: 220px;">
                        <form:options items="${fns:getDictListFromMS('customerlevel')}"
                                      itemLabel="label" itemValue="value" htmlEscape="false"/><%-- 切换为微服务 --%>
                    </form:select>

                </div>
            </div>
        </div>
        <div class="line-row">
            <div class="control-group x">
                <label class="control-label"><span class="red">*</span>返点比率:</label>
                <div class="controls">
                    <form:select path="finance.rebateRate" cssClass="required input-medium" cssStyle="width: 220px;">
                        <form:options items="${fns:getDictListFromMS('rebaterate')}"
                                      itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                    </form:select>

                </div>
            </div>
        </div>
    </div>
    <div class="line-row">
        <div class="control-group x" style="width: 98%">
            <label class="control-label"><span class="red">*</span>开&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp票:</label>
            <div class="controls" style="margin-top: 3px">
                <c:choose>
                    <c:when test="${customer.id !=null and customer.id>0}">
                        <c:choose>
                            <c:when test="${customer.finance.invoiceFlag==0}">
                                    <span class="switch-off" id="spanInvoiceFlag"
                                          style="width: 40px;height: 20px"></span>
                            </c:when>
                            <c:otherwise>
                                    <span class="switch-on" id="spanInvoiceFlag"
                                          style="width: 40px;height: 20px"></span>
                            </c:otherwise>
                        </c:choose>
                        <input type="hidden" value="${customer.finance.invoiceFlag}" name="finance.invoiceFlag"
                               id="finance.invoiceFlag">
                    </c:when>
                    <c:otherwise>
                        <span class="switch-on" id="spanInvoiceFlag" style="width: 40px;height: 20px"></span>
                        <input type="hidden" value="1" name="finance.invoiceFlag" id="finance.invoiceFlag">
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    <div style="float: left;margin-top: 15px;width: 98%">

        <div class="control-group x" style="width:525px">
            <label class="control-label">账户管理:</label>
            <div style="width: 652px;margin-left: 165px;border: 1px solid #ccc">
                <table id="content" class="table  table-bordered table-condensed table-hover" style="margin-bottom:0px">
                    <thead>
                    <tr>
                        <th width="80">账户类型</th>
                        <th width="80">开户银行</th>
                        <th width="200">分行</th>
                        <th width="200">账号</th>
                        <th width="80">开户人</th>
                        <th width="64">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr id="finanaType_1">
                        <td>对公账户</td>
                        <td>${fns:getDictLabelFromMS(customer.finance.publicBank,'banktype','')}</td>
                        <td>${customer.finance.publicBranch}</td>
                        <td>${customer.finance.publicAccount}</td>
                        <td>${customer.finance.publicName}</td>
                        <input type="hidden" name="finance.publicBank" value="${customer.finance.publicBank}">
                        <input type="hidden" name="finance.publicBranch" value="${customer.finance.publicBranch}">
                        <input type="hidden" name="finance.publicAccount" value="${customer.finance.publicAccount}">
                        <input type="hidden" name="finance.publicName" value="${customer.finance.publicName}">
                        <c:choose>
                            <c:when test="${not empty customer.finance.publicAccount}">
                                <td><a href="javascript:editCustomerFinance('${customerId}',1)">修改</a></td>
                            </c:when>
                            <c:otherwise>
                                <td><a href="javascript:editCustomerFinance('${customerId}',1)">添加</a></td>
                            </c:otherwise>
                        </c:choose>
                    </tr>

                    <tr id="finanaType_2">
                        <td>对私账户</td>
                        <td>${fns:getDictLabelFromMS(customer.finance.privateBank,'banktype','')}</td>
                        <td>${customer.finance.privateBranch}</td>
                        <td>${customer.finance.privateAccount}</td>
                        <td>${customer.finance.privateName}</td>
                        <input type="hidden" name="finance.privateBank" value="${customer.finance.privateBank}">
                        <input type="hidden" name="finance.privateBranch" value="${customer.finance.privateBranch}">
                        <input type="hidden" name="finance.privateAccount" value="${customer.finance.privateAccount}">
                        <input type="hidden" name="finance.privateName" value="${customer.finance.privateName}">
                        <c:choose>
                            <c:when test="${not empty customer.finance.privateAccount}">
                                <td><a href="javascript:editCustomerFinance('${customerId}',2)">修改</a></td>
                            </c:when>
                            <c:otherwise>
                                <td><a href="javascript:editCustomerFinance('${customerId}',2)">添加</a></td>
                            </c:otherwise>
                        </c:choose>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <legend style="margin-left: 36px;margin-top: 25px">产品信息<div class="line_"></div></legend>
    <div class="row-fluid" style="margin-left: 90px;width: 90%;height:470px;">
        <div id="productTree" class="ztree"
             style="margin-top:3px;float:left;height:400px;width:350px;overflow:auto;"></div>
        <form:hidden path="productIds"/>
    </div>

    <div>
        <div id="editBtn" class="line-row">
            <div class="control-group">
                <label class="control-label"></label>
                <div class="controls" style="width: 500px;margin-left: 560px">
                    <shiro:hasPermission name="fi:md:customer:edit">
                        <input id="btnSubmit" class="btn btn-primary" type="submit"
                               style="width: 96px;height: 40px;margin-top: 12px;"
                               value="保 存"/>&nbsp;</shiro:hasPermission>

                    <input style="margin-left: 20px;width: 96px;height: 40px;margin-top: 12px;" id="btnCancel"
                           class="btn"
                           type="button" value="取 消" onclick="cancel()"/>

                </div>
            </div>
        </div>
    </div>

    </form:form>
<script>
    <c:if test="${canEdit eq true}">
    editVip();
    </c:if>
</script>
</body>
</html>