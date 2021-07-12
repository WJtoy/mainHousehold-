<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户-品牌-产品分类配置</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <style>
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
    </style>
    <script type="text/javascript">
        var clickTag = 0;
        $(document).ready(function () {
            $("#inputForm").validate({
                submitHandler: function (form) {
                    // 保存品牌信息
                    var mids = [], brandnodes = productTree.getCheckedNodes(true);
                    for (var i = 0; i < brandnodes.length; i++) {
                        mids.push(brandnodes[i].id);
                    }
                    if (mids.length == 0) {
                        layerMsg('请至少勾选一个产品');
                        return false;
                    }
                    $("#brandIds").val(mids);
                    //保存品牌信息
                    // loading('正在提交，请稍等...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    ;
                    $btnSubmit.prop("disabled", true);
                    // form.submit();
                    var options = {
                        url: "${ctx}/customer/md/customerBrandCategory/save",
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
                        return false;
                    }
                }
            };
            var brandNodes = [<c:forEach items="${products}" var="product">{
                id: '${product.id}',
                pId: '0',
                name: "${not empty product.id?product.name:'未知产品'}"
            },
                </c:forEach>];
            var productTree = $.fn.zTree.init($("#productTree"), setting, brandNodes);

            // 默认选择节点
            var productIds = "${customerBrandCategory.brandIds}".split(",");
            for (var i = 0; i < productIds.length; i++) {
                var node = productTree.getNodeByParam("id", productIds[i]);
                try {
                    productTree.checkNode(node, true, false);
                } catch (e) {
                }
            }
            productTree.expandAll(true);


            /**客户级联客户产品分类*/
            $(document).on('change', '.selectCustomer', function (e) {
                var customerId = $(this).val();
                if (customerId == "") {
                    $("#brandId").empty();
                    $("#brandId").append('<option value="" selected="selected">请选择</option>');
                    $("#brandId").change();
                    $.fn.zTree.init($("#productTree"), setting, []);
                    return false;
                }
                $.ajax({
                        url: "${ctx}/customer/md/customerBrandCategory/ajax/getListByCustomer?customerId=" + customerId,
                        success: function (e) {
                            if (e && e.success == true) {
                                $("#brandId").empty();
                                var programme_sel = [];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for (var i = 0, len = e.data.customerBrands.length; i < len; i++) {
                                    var programme = e.data.customerBrands[i];
                                    programme_sel.push('<option value="' + programme.id + '">' + programme.brandName + '</option>')
                                }
                                $("#brandId").append(programme_sel.join(' '));
                                $("#brandId").val("");
                                $("#brandId").change();
                                brandNodes = [];
                                var products = e.data.products;
                                for (var i = 0, len = products.length; i < len; i++) {
                                    var product = {};
                                    product.id = products[i].id;
                                    product.name = products[i].name;
                                    product.pId = 0;
                                    brandNodes.push(product);
                                }
                                productTree = $.fn.zTree.init($("#productTree"), setting, brandNodes);

                            } else if (e.success == false) {
                                $("#brandId").html('<option value="" selected>请选择</option>');
                                layerError(e.message, "错误提示");
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText, null, "请求品牌", "错误提示！");
                        }
                    }
                );
            });


            /**产品分类级联品牌信息*/
            $(document).on('change', '#brandId', function (e) {
                var brandId = $(this).val();
                var customerId = $("#customerId").val();
                if (brandId == null || brandId == "") {
                    return false;
                }
                if (customerId == null || customerId == '') {
                    layerError("请先选择客户", "错误提示");
                    return false;
                }
                $.ajax({
                        url: "${ctx}/customer/md/customerBrandCategory/ajax/findListByBrand?customerId=" + customerId + "&brandId=" + brandId,
                        success: function (e) {
                            if (e && e.success == true) {
                                //品牌信息
                                var productBrands = e.data;
                                productTree.checkAllNodes(false);
                                for (var i = 0; i < productBrands.length; i++) {
                                    var node = productTree.getNodeByParam("id", productBrands[i].productId);
                                    try {
                                        productTree.checkNode(node, true, false);
                                    } catch (e) {
                                    }
                                }
                                productTree.expandAll(true);
                            } else if (e.success == false) {
                                layerError(e.message, "错误提示");
                            }
                        },
                        error: function (e) {
                            productTree.checkAllNodes(false);
                            ajaxLogout(e.responseText, null, "请求品牌失败", "错误提示！");
                        }
                    }
                );
            });

            /**产品分类级联品牌信息*/
            $(document).on('change', '#productId', function (e) {
                var productId = $(this).val();
                var customerId = $("#customerId").val();
                if (productId == null || productId == "") {
                    $.fn.zTree.init($("#brandTree"), setting, []);
                    return false;
                }
                if (customerId == null || customerId == '') {
                    layerError("请先选择客户", "错误提示");
                    return false;
                }
                $.ajax({
                        url: "${ctx}/customer/md/customerBrandCategory/ajax/getBrandListByCategory?productId=" + productId + "&customerId=" + customerId,
                        success: function (e) {
                            if (e && e.success == true) {
                                //品牌信息
                                brandNodes = [];
                                if (typeof(e.data.brandList) != "undefined") {
                                    var brandList = e.data.brandList;
                                    for (var j = 0, len = e.data.brandList.length; j < len; j++) {
                                        var brand = {};
                                        brand.id = brandList[j].id;
                                        brand.name = brandList[j].name;
                                        brand.pId = 0;
                                        brandNodes.push(brand);
                                    }
                                    brandTree = $.fn.zTree.init($("#brandTree"), setting, brandNodes);

                                    if (typeof(e.data.brandIds) != "undefined") {
                                        // 默认选择节点
                                        var brandids = e.data.brandIds.split(",");
                                        for (var i = 0; i < brandids.length; i++) {
                                            var node = brandTree.getNodeByParam("id", brandids[i]);
                                            try {
                                                brandTree.checkNode(node, true, false);
                                            } catch (e) {
                                            }
                                        }

                                    }
                                    // 默认展开全部节点
                                    brandTree.expandAll(true);
                                    //品牌信息
                                }
                            } else if (e.success == false) {
                                layerError(e.message, "错误提示");
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText, null, "请求品牌失败", "错误提示！");
                        }
                    }
                );
            });

            if (${currentuser.isCustomer()==true && (customerBrandCategory.id ==null || customerBrandCategory.id<=0)}) {
                var customerId = $("#customerId").val();
                if (customerId == "") {
                    $("#brandId").empty();
                    $("#brandId").append('<option value="" selected="selected">请选择</option>');
                    $("#brandId").change();
                    $.fn.zTree.init($("#productTree"), setting, []);
                    return false;
                }
                $.ajax({
                        url: "${ctx}/customer/md/customerBrandCategory/ajax/getListByCustomer?customerId=" + customerId,
                        success: function (e) {
                            if (e && e.success == true) {
                                $("#brandId").empty();
                                var programme_sel = [];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for (var i = 0, len = e.data.customerBrands.length; i < len; i++) {
                                    var programme = e.data.customerBrands[i];
                                    programme_sel.push('<option value="' + programme.id + '">' + programme.brandName + '</option>')
                                }
                                $("#brandId").append(programme_sel.join(' '));
                                $("#brandId").val("");
                                $("#brandId").change();
                                brandNodes = [];
                                var products = e.data.products;
                                for (var i = 0, len = products.length; i < len; i++) {
                                    var product = {};
                                    product.id = products[i].id;
                                    product.name = products[i].name;
                                    product.pId = 0;
                                    brandNodes.push(product);
                                }
                                productTree = $.fn.zTree.init($("#productTree"), setting, brandNodes);

                            } else if (e.success == false) {
                                $("#brandId").html('<option value="" selected>请选择</option>');
                                layerError(e.message, "错误提示");
                            }
                        },
                        error: function (e) {
                            ajaxLogout(e.responseText, null, "请求品牌", "错误提示！");
                        }
                    }
                );
            }
        });
    </script>
</head>

<body>
<form:form id="inputForm" modelAttribute="customerBrandCategory" method="post" class="form-horizontal" cssStyle="padding: 25px;">
    <form:hidden path="id"/>
    <sys:message content="${message}"/>
    <c:if test="${canAction == true}">
        <c:choose>
            <c:when test="${currentuser.isCustomer()==true}">
                <form:hidden path="customerId"></form:hidden>
            </c:when>
            <c:otherwise>
                <div class="control-group">
                    <label class="control-label"><span class="red">*</span>客户:</label>
                    <div class="controls">
                        <c:choose>
                            <c:when test="${customerBrandCategory.customerId > 0}">
                                <form:hidden path="customerId"></form:hidden>
                                <form:input path="customerName" readonly="true"></form:input>
                            </c:when>
                            <c:otherwise>
                                <select id="customerId" name="customerId" class="input-small required selectCustomer"
                                        style="width:225px;">
                                    <option value=""
                                            <c:out value="${(empty customerBrandCategory.customerId)?'selected=selected':''}"/>>
                                        请选择
                                    </option>
                                    <c:forEach items="${fns:getMyCustomerList()}" var="customer">
                                        <option value="${customer.id}"
                                                <c:out value="${(customerBrandCategory.customerId eq customer.id)?'selected=selected':''}"/>>${customer.name}</option>
                                    </c:forEach>
                                </select>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <div style="display: flex;flex-direction: column;">
            <div style="margin-top: 10px;">
                <label class="control-label"><span class="red">*</span>品牌:</label>
                <div class="controls">
                    <c:choose>
                        <c:when test="${customerBrandCategory.brandId > 0}">
                            <form:hidden path="brandId"></form:hidden>
                            <form:input path="brandName" readonly="true"></form:input>
                        </c:when>
                        <c:otherwise>
                            <select id="brandId" name="brandId" style="width:225px;" class="required">
                                <option value="" selected>请选择</option>
                            </select>

                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div style="margin-top: 10px;">
                <label class="control-label">产品信息:</label>
                <div class="controls" style="overflow-y:auto;height: 170px;width: 224px;">
                    <div id="productTree" class="ztree"
                         style="margin-top:3px;float:left;margin-left: -9px;"></div>
                    <form:hidden path="brandIds"/>
                </div>
            </div>
        </div>
    </c:if>

    <div id="editBtn">
        <shiro:hasPermission name="customer:md:customerbrandcategory:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"
                   style="margin-top: 10px;width: 85px;height: 35px;" onclick="$('#inputForm').submit()"/>&nbsp;
        </shiro:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()"
               style="margin-top: 10px;width: 85px;height: 35px;margin-right: 15px;"/>
    </div>
</form:form>
</body>
<script class="removedscript" type="text/javascript">
    function cancel() {
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        parent.layer.close(index);
    }
</script>
</html>
