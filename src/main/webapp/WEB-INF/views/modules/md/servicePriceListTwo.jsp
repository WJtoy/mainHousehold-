<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>安维价格管理</title>
	<meta name="decorator" content="default"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
	<script src="${ctxStatic}/layui/layui.js"></script>
	<link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<style>
		.search_f{
			margin: 20px 0px 20px 0;
		}
	</style>
	<script type="text/javascript">
        $(document).ready(function() {
            var w = $(window).width();
            FixTable("treeTable", 2, w, "100%");//460

            var pagestyle = function() {
                var width = $(window).width() -0;
                FixTable("treeTable", 2, width, "100%");
                $("#treeTable_tableLayout").css("width",width);
            }
            //注册加载事件
//			$("#iframe",window).load(pagestyle);
            //注册窗体改变大小事件
            $(window).resize(pagestyle);
            // $("th").css({"text-align":"center","vertical-align":"middle"});
            // $("td").css({"vertical-align":"middle"});

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

        var clicktag = 0;
		function recoverPrice(type) {
            var text = "恢复标准价";
            var url = "${ctx}/md/serviceprice/recover";
            top.layer.open({
                type: 2,
                id:"priceRecover",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['470px','200px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                    // 获取子页面的iframe
                    var iframeWin = top[layero.find('iframe')[0]['name']];
                    var servicePointId = $("#servicePointId").val();
                    var servicePointNo = $("#servicePointNo").val();
					var serviceName = $("#servicePointName").val();
					var servicePointPrimaryName = $("#primaryName").val();
					var contactInfo = $("#contactInfo").val();
					var customizePriceFlag = $("#customizePriceFlag").val();
					var useDefaultPrice = $("#useDefaultPrice").val();
                    var degree = $("#degree").val();
                    var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
                    var remotePriceFlag = $("#remotePriceFlag").val();
                    var remotePriceType = $("#remotePriceType").val();
                    if(iframeWin != null){
                        var json = {
                            type : type,
                            servicePointId : servicePointId,
                            servicePointNo: servicePointNo,
                            serviceName: serviceName,
                            servicePointPrimaryName: servicePointPrimaryName,
                            contactInfo: contactInfo,
                            customizePriceFlag: customizePriceFlag,
                            useDefaultPrice: useDefaultPrice,
                            degree : degree,
							serviceRemotePriceFlag : serviceRemotePriceFlag,
							remotePriceFlag : remotePriceFlag,
							remotePriceType : remotePriceType
                        };
                        iframeWin.child(json);
                    }
                },
                end:function(){
                }
            });
        }

        function editPrice(sid, pid){
			var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
            var text = "师傅价格";
            var url = "${ctx}/md/serviceprice/productNew?servicePoint.id="+sid+"&product.id="+pid +"&serviceRemotePriceFlag=" + serviceRemotePriceFlag;
            top.layer.open({
                type: 2,
                id:"servicePointPrice",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['1240px', '800px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                    // 获取子页面的iframe
                    var iframeWin = top[layero.find('iframe')[0]['name']];
                    var servicePointId = $("#servicePointId").val();
                    var servicePointNo = $("#servicePointNo").val();
                    var serviceName = $("#servicePointName").val();
                    var servicePointPrimaryName = $("#primaryName").val();
                    var contactInfo = $("#contactInfo").val();
                    var customizePriceFlag = $("#customizePriceFlag").val();
                    var useDefaultPrice = $("#useDefaultPrice").val();
                    var degree = $("#degree").val();
                    var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
                    var remotePriceFlag = $("#remotePriceFlag").val();
                    var remotePriceType = $("#remotePriceType").val();
                    var productCategoryId = $("#productCategoryId").val();
                    var productId = $("#productId").val();
                    if(iframeWin != null){
                        var json = {
                            servicePointId : servicePointId,
                            servicePointNo: servicePointNo,
                            serviceName: serviceName,
                            servicePointPrimaryName: servicePointPrimaryName,
                            contactInfo: contactInfo,
                            customizePriceFlag: customizePriceFlag,
                            useDefaultPrice: useDefaultPrice,
							degree : degree,
							serviceRemotePriceFlag : serviceRemotePriceFlag,
							remotePriceFlag : remotePriceFlag,
							remotePriceType : remotePriceType,
							productCategoryId: productCategoryId,
							productId: productId
                        };
                        iframeWin.child(json);
                    }
                },
                end:function(){
                }
            });
		}
		function editRemotePriceProduct(sid, pid){
			var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
			var text = "师傅价格";
			var url = "${ctx}/md/serviceprice/remotePriceProductForm?servicePoint.id="+sid+"&product.id="+pid +"&serviceRemotePriceFlag=" + serviceRemotePriceFlag;
			top.layer.open({
				type: 2,
				id:"servicePointPrice",
				zIndex:19891015,
				title:text,
				content: url,
				area: ['898px', '800px'],
				shade: 0.3,
				maxmin: false,
				success: function(layero,index){
					// 获取子页面的iframe
					var iframeWin = top[layero.find('iframe')[0]['name']];
					var servicePointId = $("#servicePointId").val();
					var servicePointNo = $("#servicePointNo").val();
					var serviceName = $("#servicePointName").val();
					var servicePointPrimaryName = $("#primaryName").val();
					var contactInfo = $("#contactInfo").val();
					var customizePriceFlag = $("#customizePriceFlag").val();
					var useDefaultPrice = $("#useDefaultPrice").val();
					var degree = $("#degree").val();
					var serviceRemotePriceFlag = $("#serviceRemotePriceFlag").val();
					var remotePriceFlag = $("#remotePriceFlag").val();
					var remotePriceType = $("#remotePriceType").val();
					var productCategoryId = $("#productCategoryId").val();
					var productId = $("#productId").val();
					if(iframeWin != null){
						var json = {
							servicePointId : servicePointId,
							servicePointNo: servicePointNo,
							serviceName: serviceName,
							servicePointPrimaryName: servicePointPrimaryName,
							contactInfo: contactInfo,
							customizePriceFlag: customizePriceFlag,
							useDefaultPrice: useDefaultPrice,
							degree : degree,
							serviceRemotePriceFlag : serviceRemotePriceFlag,
							remotePriceFlag : remotePriceFlag,
							remotePriceType : remotePriceType,
							productCategoryId: productCategoryId,
							productId: productId
						};
						iframeWin.child(json);
					}
				},
				end:function(){
				}
			});
		}

		// 供子页面调用刷新
        function reloadPrice(servicePintId, servicePointNo, serviceName, servicePointPrimaryName, contactInfo1, customizePriceFlag,useDefaultPrice,degree,serviceRemotePriceFlag,remotePriceFlag,remotePriceType,productId, productCategoryId){
			if (productId ==undefined) {
				productId = 0;
			}
			if (productCategoryId == undefined) {
				productCategoryId = 0;
			}
		    window.location="${ctx}/md/serviceprice/selectPrice?id="+servicePintId + "&primaryName="+servicePointPrimaryName+"&contactInfo="+contactInfo1+"&customizePriceFlag="+customizePriceFlag+"&useDefaultPrice="+useDefaultPrice+"&servicePointNo="+servicePointNo+"&servicePointName="+serviceName+"&degree="+degree +"&serviceRemotePriceFlag=" + serviceRemotePriceFlag + "&remotePriceFlag=" + remotePriceFlag + "&remotePriceType=" + remotePriceType+"&productId="+productId+"&productCategoryId="+productCategoryId;
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
		.col_product {width: 260px;}
		.col_command {width: 80px;}
		.table tbody td.error {background-color: #f2dede!important;}
		.price_div{width: 10%;float: left;}
		.tabTitle{color: #999999;}
		.table thead th, .table tbody td {
			text-align: center;
			vertical-align: middle;
			BackColor: Transparent;
		}
	</style>
</head>
<body>
<ul class="nav nav-tabs">
	<li class=""><a href="${ctx}/md/serviceprice/list">服务网点</a></li>
	<li class="active"><a href="javascript:;">师傅价格</a></li>
</ul>





<form:form id="searchForm" cssStyle="" modelAttribute="servicePrice" action="${ctx}/md/serviceprice/selectPrice?id=${servicePointId}&primaryName=${primaryName}&contactInfo=${contactInfo}&customizePriceFlag=${customizePriceFlag}&useDefaultPrice=${useDefaultPrice}&servicePointNo=${servicePointNo}&servicePointName=${servicePointName}&degree=${degree}&serviceRemotePriceFlag=${serviceRemotePriceFlag}&remotePriceFlag=${remotePriceFlag}&remotePriceType=${remotePriceType}" method="post" class="breadcrumb form-search">
	<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
	<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
	<input type="hidden" value="${servicePointId}" id="servicePointId">
	<input type="hidden" value="${servicePointNo}" id="servicePointNo">
	<input type="hidden" value="${servicePointName}" id="servicePointName">
	<input type="hidden" value="${primaryName}" id="primaryName">
	<input type="hidden" value="${contactInfo}" id="contactInfo">
	<input type="hidden" value="${customizePriceFlag}" id="customizePriceFlag">
	<input type="hidden" value="${useDefaultPrice}" id="useDefaultPrice">
	<input type="hidden" value="${degree}" id="degree">
	<input type="hidden" value="${serviceRemotePriceFlag}" id="serviceRemotePriceFlag">
	<input type="hidden" value="${remotePriceFlag}" id="remotePriceFlag">
	<input type="hidden" value="${remotePriceType}" id="remotePriceType">
		<label>产品品类：</label>
		<select id="productCategoryId" name="productCategoryId" class="input-small" style="width:200px;">
			<option value="" <c:out value="${(empty productCategoryId)?'selected=selected':''}" />>请选择</option>
			<c:forEach items="${fns:getProductCategories()}" var="dict">
				<option value="${dict.id}" <c:out value="${(productCategoryId eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
			</c:forEach>
		</select>

		<label style="margin-left: 30px;">产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
		<form:select id="productId" path="product.id" class="required" style="width:200px;">
			<form:option value="" label="所有"/>
			<form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false" />
		</form:select>
		&nbsp;
	<input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" style="margin-left: 10px;"/>
</form:form>



<sys:message content="${message}" type="loading"/>
<table id="" class="table table-striped table-bordered table-condensed table-hover">
	<thead>
	<tr>
		<th rowspan="2" ><label class="col_product">产品</label></th>
		<c:forEach items="${serviceTypes}" var="serviceType">
			<th colspan="2">${serviceType.name}</th>
		</c:forEach>
	</tr>
	<tr>
		<c:set var="serviceTypeCount" value="1" />
		<c:forEach items="${serviceTypes}" var="serviceType">
			<th width="70">价格</th>
			<th width="70">优惠价</th>
			<c:set var="serviceTypeCount" value="${serviceTypeCount+1}" />
		</c:forEach>
	</tr>
	<tr>
		<th colspan="${serviceTypes.size() * 2 + 1}" style="text-align: left">
			<label>网点：${servicePointNo}，${servicePointName}，网点电话：${contactInfo}，主账号：${primaryName}，网点分类：${fns:getDictLabelFromMS(degree,'degreeType','')}，

				<c:choose>
					<c:when test="${serviceRemotePriceFlag == 0}">
						价格属性：${customizePriceFlag == 0 ? '标准价' : "自定义"}，
					</c:when>
<%--					<c:otherwise>--%>
<%--						价格属性：${remotePriceFlag == 0 ? '标准价' : "自定义"}，--%>
<%--					</c:otherwise>--%>
				</c:choose>

				<c:if test="${serviceRemotePriceFlag == 0}">
					价格轮次：${fns:getDictLabelFromMS(useDefaultPrice,'PriceType','')}，
				</c:if>
				价格类型：${serviceRemotePriceFlag == 0 ?"服务价格":"偏远价格"}

			</label>
            <c:choose>
                <c:when test="${serviceRemotePriceFlag == 0}">
                    <c:if test="${customizePriceFlag == 1}">
                        <shiro:hasPermission name="md:serviceprice:edit">
                            <input id="recover" class="btn btn-primary" type="submit" value="恢复标准价" onclick="recoverPrice('${fns:getDictLabelFromMS(useDefaultPrice,'PriceType','')}')"/>&nbsp;
                        </shiro:hasPermission>
                    </c:if>
                </c:when>
<%--                <c:otherwise>--%>
<%--                    <c:if test="${remotePriceFlag == 1}">--%>
<%--                        <shiro:hasPermission name="md:serviceprice:edit">--%>
<%--                            <input id="recover" class="btn btn-primary" type="submit" value="恢复标准价" onclick="recoverPrice('${fns:getDictLabelFromMS(remotePriceType,'PriceType','')}')"/>&nbsp;--%>
<%--                        </shiro:hasPermission>--%>
<%--                    </c:if>--%>
<%--                </c:otherwise>--%>
            </c:choose>
		</th>

	</tr>
	</thead>
	<tbody>
	<c:forEach items="${page.map.list}" var="servicePoint">
		<c:forEach items="${servicePoint.servicePointPriceList}" var="servicePointPrice">
			<c:set var="index" value="${index+1}" />
			<tr id="${servicePointPrice.productId}" pId="${servicePoint.servicePointId}">
				<td style="text-align: center">
					<c:choose>
						<c:when test="${serviceRemotePriceFlag eq 1}">
<%--							<c:choose>--%>
<%--								<c:when test="${remotePriceFlag eq 1}">--%>
									<label onclick="editRemotePriceProduct('${servicePoint.servicePointId}','${servicePointPrice.productId}')" style="color: #0096DA;">${servicePointPrice.productName}</label>
<%--								</c:when>--%>
<%--								<c:otherwise>--%>
<%--									<label>${servicePointPrice.productName}</label>--%>
<%--								</c:otherwise>--%>
<%--							</c:choose>--%>
						</c:when>
						<c:otherwise>
							<c:choose>
								<%--<c:when test="${fn:startsWith(servicePointNo,'YH') || customizePriceFlag eq 1}" >--%>
								<c:when test="${degree eq 30 || customizePriceFlag eq 1}" >
									<label onclick="editPrice('${servicePoint.servicePointId}','${servicePointPrice.productId}')" style="color: #0096DA;">${servicePointPrice.productName}</label>
								</c:when>
								<c:otherwise>
									<label>${servicePointPrice.productName}</label>
								</c:otherwise>
							</c:choose>
						</c:otherwise>
					</c:choose>

				</td>
				<c:forEach items="${servicePointPrice.servicePriceList}" var="servicePrice">
					<c:choose>
						<c:when test="${serviceRemotePriceFlag == 0}">
							<c:choose>
								<%--标准价--%>
								<c:when test="${customizePriceFlag == 0}">
									<td>${servicePrice.referPrice}</td>
									<td>${servicePrice.referDiscountPrice}</td>
								</c:when>
								<%--自定义价--%>
								<c:otherwise>
									<c:choose>
										<c:when test="${servicePrice.referPrice ne servicePrice.price}">
											<td style="color: red">${servicePrice.price}</td>
										</c:when>
										<c:otherwise>
											<td>${servicePrice.price}</td>
										</c:otherwise>
									</c:choose>
									<c:choose>
										<c:when test="${servicePrice.referDiscountPrice ne servicePrice.discountPrice}">
											<td style="color: red">${servicePrice.discountPrice}</td>
										</c:when>
										<c:otherwise>
											<td>${servicePrice.discountPrice}</td>
										</c:otherwise>
									</c:choose>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
<%--							<c:choose>--%>
<%--								&lt;%&ndash;标准价&ndash;%&gt;--%>
<%--								<c:when test="${remotePriceFlag == 0}">--%>
<%--									<td>${servicePrice.referPrice}</td>--%>
<%--									<td>${servicePrice.referDiscountPrice}</td>--%>
<%--								</c:when>--%>
<%--								&lt;%&ndash;自定义价&ndash;%&gt;--%>
<%--								<c:otherwise>--%>
<%--									<c:choose>--%>
<%--										<c:when test="${servicePrice.referPrice ne servicePrice.price}">--%>
<%--											<td style="color: red">${servicePrice.price}</td>--%>
<%--										</c:when>--%>
<%--										<c:otherwise>--%>
<%--											<td>${servicePrice.price}</td>--%>
<%--										</c:otherwise>--%>
<%--									</c:choose>--%>
<%--									<c:choose>--%>
<%--										<c:when test="${servicePrice.referDiscountPrice ne servicePrice.discountPrice}">--%>
<%--											<td style="color: red">${servicePrice.discountPrice}</td>--%>
<%--										</c:when>--%>
<%--										<c:otherwise>--%>
<%--											<td>${servicePrice.discountPrice}</td>--%>
<%--										</c:otherwise>--%>
<%--									</c:choose>--%>
<%--								</c:otherwise>--%>
<%--							</c:choose>--%>
							<c:choose>
								<c:when test="${servicePrice.id == null}">
									<td>-</td>
									<td>-</td>
								</c:when>
								<c:otherwise>
									<td>${servicePrice.price}</td>
									<td>${servicePrice.discountPrice}</td>
								</c:otherwise>
							</c:choose>

						</c:otherwise>
					</c:choose>



				</c:forEach>
			</tr>
		</c:forEach>
	</c:forEach>
	</tbody>
</table>
<div class="pagination">${page}</div>

</body>
<script>
    $(document).ready(function() {
        // $("th").css({"text-align":"center","vertical-align":"middle"});
        // $("td").css({"text-align":"center","vertical-align":"middle"});


        <c:if test="${productCategoryId != null}">
			var productCategoryId = ${productCategoryId};

        	var productId = 0;
			<c:if test="${servicePrice.product.id != null}">
				productId = ${servicePrice.product.id};
			</c:if>
			asynchronousRequest(productCategoryId, productId);
		</c:if>
    });
</script>
</html>
