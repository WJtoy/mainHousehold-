<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户服务价格管理</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/layui/layui.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        $(document).ready(function() {

            var w = $(window).width();
            FixTable("treeTable", 2, w, "100%");
            var pagestyle = function() {
                var width = $(window).width() -0;
                FixTable("treeTable", 2, width, "100%");
                $("#treeTable_tableLayout").css("width",width);
                //$("#divGrid").css("width",h);
            }
            //注册加载事件
            //$("#iframe",window).load(pagestyle);
            //注册窗体改变大小事件
            $(window).resize(pagestyle);

            $("#btnSubmit").on("click", function(){
                var url = "${ctx}/fi/md/customerPrice/list";
                $("#searchForm").attr("action",url);
                $("#searchForm").submit();
                return false;
            });

            $('.notReviewed').on({
                mouseenter:function(){
                    var that = this;
                    tips =layer.tips("<span style='color:#fff;'>待审核</span>",that,{tips:[1,'#3E3E3E'],time:0,area: 'auto',maxWidth:500});
                },
                mouseleave:function(){
                    layer.close(tips);
                }
            });

            $('.stopUsing').on({
                mouseenter:function(){
                    var that = this;
                    tips =layer.tips("<span style='color:#fff;'>已停用</span>",that,{tips:[1,'#3E3E3E'],time:0,area: 'auto',maxWidth:500});
                },
                mouseleave:function(){
                    layer.close(tips);
                }
            });
        });

        function openPriceInfo(customerId, productId, params, qFirstSearch){
            var text = "服务价格";
            var url = "${ctx}/fi/md/customerPrice/productForm?customer.id="+customerId+"&product.id="+productId+"&"+params+"&qFirstSearch="+qFirstSearch+"";
            top.layer.open({
                type: 2,
                id:"customerPrice",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['1240px', '800px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                },
                cancel: function(){
                    // 右上角关闭事件的逻辑
                    loading('同步中...');
                    $("#searchForm").submit();
                }
            });
        }

    </script>
    <style type="text/css">
        .col_product {width: 250px;}
        .col_command {width: 78px;}
        .table tbody td.error {
            background-color: #FEEEEE!important;
            text-align: center!important;
            vertical-align: middle!important;
            color: #F54142!important;
        }
        .table td {
            text-align: center!important;
            vertical-align: middle!important;
        }
        .table tr{
            height: 40px!important;
        }
        .icon-object{
            height: 24px;
            width: 24px;
            vertical-align: middle;
        }
        .notReviewed{
            color: #515A6E!important;
            background-color: #FDF5E9!important;
        }
        .stopUsing{
            color: #C5C8CE!important;
        }
    </style>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:;">服务价格</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<input type="hidden" value="${currentuser}">
<sys:message content="${message}" type="loading"/>
<form:form id="searchForm" modelAttribute="customerPrice" action="${ctx}/fi/md/customerPrice/list" method="post" class="form-search" cssStyle="margin: 20px 0 20px 0;">
    <c:set var="params" value="qCustomerId=${customerPrice.customer.id}&qCustomerName=${fns:urlEncode(customerPrice.customer.name)}&qProductCategoryId=${customerPrice.productCategory.id}&qProductCategoryName=${fns:urlEncode(customerPrice.productCategory.name)}&qProductId=${customerPrice.product.id}&qProductName=${fns:urlEncode(customerPrice.product.name)}"></c:set>
    <c:set var="qFirstSearch" value="${customerPrice.firstSearch}"/>
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="urlParams" name="urlParams" type="hidden" value="${params}"/>
    <form:hidden path="firstSearch" />
    <c:if test="${currentuser.isCustomer() == false}">
        <label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
        <form:select path="customer.id" id="customerId" class="input-large">
            <form:option value="0" label="请选择"/>
            <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
        </form:select>
        &nbsp;
    </c:if>
    <label class="control-label" style="margin-left: 30px;">产品品类:</label>
    <form:select path="productCategory.id" id="productCategory" class="input-large">
        <form:option value="" label="所有"/>
        <form:options items="${fns:getProductCategories()}" itemLabel="name" itemValue="id" htmlEscape="false" />
    </form:select>
    <label style="margin-left: 30px;">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
    <form:select path="product.id" cssClass="input-small" cssStyle="width:250px;">
        <form:option value="" label="所有"/>
        <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
    </form:select>
    &nbsp;
    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询"/>
</form:form>

<table id="" class="table table-striped table-responsive table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th rowspan="2" colspan="2"><label class="col_product">产品</label></th>
        <c:forEach items="${serviceTypes}" var="serviceType">
            <th colspan="3">${serviceType.name}</th>
        </c:forEach>
    </tr>
    <tr>
        <c:forEach items="${serviceTypes}" var="serviceType">
            <th>价格</th>
            <th>优惠价</th>
            <th>冻结金额</th>
        </c:forEach>
    </tr>
    </thead>
    <tbody>
    <c:set var="productId" value="" />

    <c:if test="${customerPrice.customer.id != null && customerPrice.customer.id > 0}">
        <c:forEach items="${page.map.list}" var="customer">
            <c:forEach items="${customer.customerPriceMapList}" var="customerPriceMap">
                <c:set var="index" value="${index+1}" />
                <tr id="${customerPriceMap.productId}" pId="${customer.customerId}">
                    <td style="text-align: center;vertical-align: middle">${index+(page.pageNo-1)*page.pageSize}</td>
                    <td style="text-align: center;vertical-align: middle">
                        <label style="color: #0096DA" onclick="openPriceInfo('${customer.customerId}','${customerPriceMap.productId}','${params}','${qFirstSearch}')">${customerPriceMap.productName}</label>
                    </td>

                    <!-- list(price) -->
                    <c:forEach items="${customerPriceMap.customerPriceList}" var="customerPrice">
                        <c:choose>
                            <c:when test="${customerPrice.flag == 2}">
                                <%--产品价格未维护--%>
                                <td class="error" colspan="3">产品价格未维护</td>
                            </c:when>
                            <c:when test="${customerPrice.flag == 1}">
                                <td style="text-align: center;vertical-align: middle">-</td>
                                <td style="text-align: center;vertical-align: middle">-</td>
                                <td style="text-align: center;vertical-align: middle">-</td>
                                <%--<td style="text-align: center;vertical-align: middle"><a href="javascript:void(0);" onclick="addPriceInfo('${customer.customerId}','${customer.customerName}','${customerPriceMap.productId}','${customerPriceMap.productName}','${customerPrice.serviceType.id}','${customerPrice.serviceType.name}','${customerPrice.serviceType.warrantyStatus.value}')">添加</a></td>--%>
                            </c:when>
                            <c:otherwise>

                                <c:choose>
                                    <%--待审核--%>
                                    <c:when test="${customerPrice.delFlag == 2}">
                                        <td class="notReviewed">${customerPrice.price}</td>
                                        <td class="notReviewed">${customerPrice.discountPrice}</td>
                                        <td class="notReviewed">${customerPrice.blockedPrice}</td>
                                    </c:when>
                                    <%--停用--%>
                                    <c:when test="${customerPrice.delFlag == 1}">
                                        <td class="stopUsing">${customerPrice.price}</td>
                                        <td class="stopUsing">${customerPrice.discountPrice}</td>
                                        <td class="stopUsing">${customerPrice.blockedPrice}</td>
                                    </c:when>
                                    <c:otherwise>
                                        <%--参考价--%>
                                        <c:set var="referPrice" value="${customerPrice.referPrice}"/>
                                        <c:set var="referDiscountPrice" value="${customerPrice.referDiscountPrice}"/>
                                        <%--自定义价--%>
                                        <c:set var="price" value="${customerPrice.price}"/>
                                        <c:set var="discountPrice" value="${customerPrice.discountPrice}"/>
                                        <c:choose>
                                            <%--客户未使用价格 无需比较--%>
                                            <c:when test="${notUsePrice != null && notUsePrice eq true}">
                                                <td>${price}</td>
                                                <td>${discountPrice}</td>
                                                <td>${customerPrice.blockedPrice}</td>
                                            </c:when>
                                            <c:otherwise>
                                                <td style="<c:out value='${referPrice != price ? "color:#F54142":""}'/>">${price}</td>
                                                <td style="<c:out value='${referDiscountPrice != discountPrice ? "color:#F54142":""}'/>">${discountPrice}</td>
                                                <td>${customerPrice.blockedPrice}</td>
                                            </c:otherwise>
                                        </c:choose>

                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </tr>
            </c:forEach>
        </c:forEach>
    </c:if>
    </tbody>
</table>
<div class="pagination">${page}</div>

<script class="removedscript" type="text/javascript">
    $(document).ready(function() {
        $("th").css({"text-align":"center","vertical-align":"middle"});
    });
</script>
</body>
</html>
