<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品价格</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        var this_index = top.layer.index;
        $(document).ready(function () {
            var w = $(window).width();
            FixTable("treeTable", 2, w, "100%");

            var pagestyle = function() {
                var width = $(window).width() -0;
                FixTable("treeTable", 2, width, "100%");
                $("#treeTable_tableLayout").css("width",width);
            }

            //注册窗体改变大小事件
            $(window).resize(pagestyle);
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});

            $(document).on('change',"#productCategoryId", function(e){
                var productCategoryId = $(this).val();
                if(productCategoryId ==null || productCategoryId==''){
                    $("#productId").html('<option value="" selected>请选择</option>');
                    $("#productId").val("");
                    $("#productId").change();
                    return false;
                }
                asynchronousRequest(productCategoryId);
            });
        });

        function go2Delete(productPriceId, priceType){
            var hintMsg ="服务价格";
            if (priceType == 40) {
                hintMsg ="偏远价格";
            }
            layer.confirm(
                '确认要停用'+hintMsg+'吗？',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示'
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg('正在停用'+hintMsg+'，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    $.ajax({
                        url: "${ctx}/md/productprice/deleteNew?id="+productPriceId+"&priceType="+priceType,
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                setTimeout(function () {
                                    layer.close(loadingIndex);
                                }, 2000);
                            }
                            if (data.success) {
                                layerMsg(data.message);

                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                //layerError("参考价格停用失败:" + data.message, "错误提示");
                                layerError("停用失败:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                        },
                    });
                    return false;
                }, function(){
                    // 取消操作
                });
        }

        function go2Active(productPriceId, priceType){
            var hintMsg ="服务价格";
            if (priceType == 40) {
                hintMsg ="偏远价格";
            }
            layer.confirm(
                '确认要启用该'+hintMsg+'吗?',
                {
                    btn: ['确定','取消'], //按钮
                    title:'提示'
                }, function(index){
                    layer.close(index);//关闭本身
                    var loadingIndex = top.layer.msg('正在启用'+hintMsg+'，请稍等...', {
                        icon: 16,
                        time: 0,//不定时关闭
                        shade: 0.3
                    });
                    $.ajax({
                        url: "${ctx}/md/productprice/activeNew?id="+productPriceId+"&priceType="+priceType,
                        success:function (data) {
                            // 提交后的回调函数
                            if(loadingIndex) {
                                setTimeout(function () {
                                    layer.close(loadingIndex);
                                }, 2000);
                            }
                            if (data.success) {
                                layerMsg(data.message);

                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                            } else {
                                //layerError("参考价格启用失败:" + data.message, "错误提示");
                                layerError("启用失败:" + data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                        },
                    });
                    return false;
                }, function(){
                    // 取消操作
                });
        }

        function editPriceInfo(priceId, priceType){
            //var text = "修改";
            var text = "修改服务价格";
            if (priceType == 40) {
                text = "修改偏远价格"
            }

            var url = "${ctx}/md/productprice/form?id="+priceId+"&qPriceType="+priceType;
            top.layer.open({
                type: 2,
                id:"productPrice",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['936px', '480px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function addPriceInfo(productId, serviceTypeId, priceType){
            //var text = "添加";
            var text = "添加服务价格";
            <c:if test="${priceType eq 40}">
            text = "添加偏远价格"
            </c:if>
            var url = "${ctx}/md/productprice/form?qProductId="+productId+"&qServiceTypeId="+serviceTypeId+"&qPriceType="+priceType;
            top.layer.open({
                type: 2,
                id:"productPrice",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['936px', '480px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function addReferencePrice(){
            var h = $(top.window).height();
            var w = $(top.window).width();
            var text = "添加服务价格";
            <c:if test="${priceType eq 40}">
                text = "添加偏远价格"
            </c:if>

            var url = "${ctx}/md/productprice/formsNew?priceType.value="+${priceType};
            var area = ['1212px',(h-230)+'px'];
            top.layer.open({
                type: 2,
                id:"engineer",
                zIndex:19891015,
                title:text,
                content: url,
                area: area,
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function asynchronousRequest(productCategoryId, productId){
            $.ajax({
                url:"${ctx}/md/product/ajax/singleProductList?productCategoryId=" + productCategoryId,
                success:function (e) {
                    if(e.success){
                        $("#productId").empty();
                        var programme_sel=[];
                        programme_sel.push('<option value="" selected="selected">请选择</option>')
                        for(var i=0,len=e.data.length;i<len;i++){
                            var programme = e.data[i];
                            programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                        }
                        $("#productId").append(programme_sel.join(' '));
                        $("#productId").val("");
                        if (productId != undefined && productId > 0) {
                            $("#productId option[value='"+ productId +"']").attr("selected",true);
                        }
                        $("#productId").change();
                    }else {
                        $("#productId").html('<option value="" selected>请选择</option>');
                        layerMsg('该产品分类还没有产品！');
                    }
                },
                error:function (e) {
                    layerError("请求产品失败","错误提示");
                }
            });
        }

    </script>
    <style type="text/css">
        .col_product {width: 250px;}
        .col_command {width: 78px;}
        .table tbody td.error {background-color: #f2dede!important;}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <c:set var="priceTypes" value="${fns:getDictListFromMS('PriceType')}" /><%--切换为微服务--%>
    <c:forEach items="${priceTypes}" var="dict">
        <c:choose>
            <c:when test="${dict.value eq priceType}">
                <li class="active"><a href="javascript:void(0);">${dict.label}</a>
            </c:when>
            <c:otherwise>
                <li><a href="${ctx}/md/productprice/list?type=${dict.value}">${dict.label}</a>
            </c:otherwise>
        </c:choose>
        </li>
    </c:forEach>
    <%--<shiro:hasPermission name="md:customerprice:edit">--%>
        <%--<li><a href="${ctx}/md/productprice/forms">添加参考价格</a>--%>
        <%--</li>--%>
    <%--</shiro:hasPermission>--%>
</ul>

<form:form id="searchForm" modelAttribute="productPrice" action="${ctx}/md/productprice/list?type=${priceType}" method="post" class="breadcrumb form-search" cssStyle="margin: 0px 0 2px 0;
    border-bottom: 1px solid #EEEEEE;">
    <div style="margin-bottom: 0px;">
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <input id="priceType" name="priceType" type="hidden" value="${priceType}"/>

        <label>产品品类：</label>
        <select id="productCategoryId" name="productCategoryId" class="input-small" style="width:200px;">
                <option value="" <c:out value="${(empty productCategoryId)?'selected=selected':''}" />>请选择</option>
            <c:forEach items="${fns:getProductCategories()}" var="dict">
                <option value="${dict.id}" <c:out value="${(productCategoryId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
            </c:forEach>
        </select>

        <label>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
        <form:select id="productId" path="product.id" class="required" style="width:280px;">
            <form:option value="" label="所有"/>
            <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false" />
        </form:select>
        &nbsp;
        <input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询"/>
    </div>

</form:form>

<shiro:hasPermission name="md:customerprice:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 120px;height: 30px" onclick="addReferencePrice()">
        <%--<i class="icon-plus-sign"></i>&nbsp;添加参考价格--%>
        <i class="icon-plus-sign"></i>&nbsp;添加${priceType lt 40?'服务价格':'偏远价格'}
    </button>
</shiro:hasPermission>

<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th rowspan="2" colspan="2"><label class="col_product">产品</label></th>
        <c:forEach items="${serviceTypes}" var="serviceType">
            <th colspan="5" width="400">${serviceType.name}</th>
        </c:forEach>
    </tr>
    <tr>
        <c:set var="serviceTypeCount" value="1"/>
        <c:forEach items="${serviceTypes}" var="serviceType">
            <th>厂商标准价</th>
            <th>厂商优惠价</th>
            <th>网点标准价</th>
            <th>网点优惠价</th>
            <th><label class="col_command">操作</label></th>
            <c:set var="serviceTypeCount" value="${serviceTypeCount+1}"/>
        </c:forEach>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="productItem">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${productItem.productName}</td>
            <c:forEach items="${productItem.itemlist}" var="serviceItem">
                <c:choose>
                    <c:when test="${serviceItem.customerStandardPrice eq 0 && serviceItem.customerDiscountPrice eq 0 && serviceItem.engineerStandardPrice eq 0 && serviceItem.engineerDiscountPrice eq 0}">
                        <td>-</td>
                        <td>-</td>
                        <td>-</td>
                        <td>-</td>
                    </c:when>
                    <c:when test="${serviceItem.delFlag==1}">
                        <td style="color: #AAAAAA;">${serviceItem.customerStandardPrice}</td>
                        <td style="color: #AAAAAA">${serviceItem.customerDiscountPrice}</td>
                        <td style="color: #AAAAAA;">${serviceItem.engineerStandardPrice}</td>
                        <td style="color: #AAAAAA;">${serviceItem.engineerDiscountPrice}</td>
                    </c:when>
                    <c:otherwise>
                        <td>${serviceItem.customerStandardPrice}</td>
                        <td>${serviceItem.customerDiscountPrice}</td>
                        <td style="color: #F54142;">${serviceItem.engineerStandardPrice}</td>
                        <td style="color: #F54142;">${serviceItem.engineerDiscountPrice}</td>
                    </c:otherwise>
                </c:choose>
                <td>
                    <c:if test="${not empty serviceItem.productPriceId }">
                        <c:choose>
                            <c:when test="${serviceItem.delFlag==1}">
                                    <%--<span class="label status_Canceled"> 已停用 </span>--%>
                                    <%--<a href="javascript:void(0);" onclick="go2Active('${serviceItem.productPriceId}','${priceType}');"--%>
                                       <%--title="启用 ${serviceItem.serviceTypeName} 价格">启用</a>--%>
                                    <input id="open" class="btn btn-primary btn-small" type="submit" value="启用" onclick="go2Active('${serviceItem.productPriceId}','${priceType}');" style=""/>
                            </c:when>
                            <c:otherwise>
                                    <c:if test="${serviceItem.productPriceId ne null && serviceItem.productPriceId ne 0}">
                                        <a href="javascript:void(0);" onclick="go2Delete('${serviceItem.productPriceId}','${priceType}');"
                                                title="停用 ${serviceItem.serviceTypeName} 价格">停用</a>
                                        <%--<a href="${ctx}/md/productprice/form?id=${serviceItem.productPriceId}&qPriceType=${priceType}"--%>
                                           <%--title="修改 ${serviceItem.serviceTypeName} 价格">修改</a>--%>
                                        <a href="javascript:void(0);" onclick="editPriceInfo('${serviceItem.productPriceId}','${priceType}');">修改</a>
                                    </c:if>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${empty serviceItem.productPriceId }">
                        <a href="javascript:void(0);" onclick="addPriceInfo('${productItem.productId}','${serviceItem.serviceTypeId}','${priceType}')">添加</a>
                    </c:if>
                </td>

            </c:forEach>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
<script>
    $(document).ready(function() {
        <c:if test="${productCategoryId != null}">
            var productCategoryId = ${productCategoryId};

            var productId = 0;
            <c:if test="${productPrice.product.id != null}">
                productId = ${productPrice.product.id};
            </c:if>
            asynchronousRequest(productCategoryId, productId);
        </c:if>
    });
</script>
</html>
