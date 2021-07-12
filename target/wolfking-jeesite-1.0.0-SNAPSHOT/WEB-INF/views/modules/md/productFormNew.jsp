<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
      <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
      <script src="${ctxStatic}/jquery-upload-file/js/ajaxfileupload.js"></script>
      <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
      <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
        var specItemIdArr = [];
        var editProductTypeId = 0;
        var editProductTypeItemId = 0;
		function cancel() {
			top.layer.close(this_index);// 关闭本身
		}
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate({
                rules: {
                    name: {remote: "${ctx}/md/product/checkProductName?id=" + '${product.id}'}
                },
                messages: {
                    name: {remote: "产品名称已存在"}
                },
				submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
                    var productIds = [];
					$("input[name='strProductIds']:checked").each(function () {
                        productIds.push(this.value)
                    });
                    $("#productIds").val(productIds)
					var materialIdArray = [];
					$("input[name='materialNames']:checked").each(function () {
                        materialIdArray.push(this.value)
                    });
                    $("#materialIds").val(materialIdArray)

					var specItemArray = [];
                    $("input[name='productSpecItems']:checked").each(function () {
                        specItemArray.push(this.value)
                    });
                    $("#specItemIds").val(specItemArray);
                    $.ajax({
                        url:"${ctx}/md/product/saveNew",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                                top.layer.close(this_index);//关闭本身
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data)
                        {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
				},
				errorContainer: "#messageBox",
				errorPlacement: function(error, element) {
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
						error.appendTo(element.parent().parent());
					} else {
						error.insertAfter(element);
					}
				}
            });

            $("[name=materialClick]").click(function () {
                var isChecked = $(this).is(':checked');
                $(this).parent().parent().children("td").eq(2).find("input[type='checkbox']").each(function () {
                  if(isChecked){
                      $(this).prop("checked", true);
				  }else{
                      $(this).prop("checked", false);
				  }
                })
			});
            specItemIdArr = "${product.specItemIds}".split(",");

			// 刷新（显示/隐藏）机构
			refreshProductTree();
			$("#setFlag").change(function(){
				refreshProductTree();
			});

            var categoryId = $("#categoryId").val();
            changeCategoryId(categoryId);
			$("#categoryId").change(function () {
			    var categoryId = $("#categoryId").val();
                changeCategoryId(categoryId);
            })

			$("#productTypeId").change(function () {
			    var productTypeId = $("#productTypeId").val();
                changeProductType(productTypeId);
            });

			$("#productTypeItemId").change(function () {
                var productTypeId = $("#productTypeId").val();
			    var productTypeItemId = $("#productTypeItemId").val();
                changeProductTypeItem(productTypeId,productTypeItemId);
            });

			editProductTypeId = $("#editProductTypeId").val();
			editProductTypeItemId = $("#editProductTypeItemId").val();
			if(editProductTypeId!=null && editProductTypeId>0 && editProductTypeItemId!=null && editProductTypeItemId>0){
                changeCategoryId($("#categoryId").val());
                changeProductType(editProductTypeId);
                changeProductTypeItem(editProductTypeId,editProductTypeItemId);
			}
		});

        function refreshProductTree(){
            if($("#setFlag").val()==1){
                $("#divProduct").show();
                $("#productTree").show();
                $("#divMaterial").hide();
            }else{
                $("#divProduct").hide();
                $("#productTree").hide();
                $("#divMaterial").show();
            }
        }

        
        function changeCategoryId(categoryId) {
            if(categoryId != null && categoryId!=''){
                $.ajax({
                        async: false,
                        url:"${ctx}/provider/md/productType/ajax/findListByCategoryId?productCategoryId="+categoryId,
                        success:function (e) {
                            if(e.success){
                                $("#productTypeId").empty();
                                var programme_sel=[];
                                var typeId = $("#editProductTypeId").val();
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    if(typeId == programme.id){
                                        programme_sel.push('<option value="'+programme.id+'" selected="selected">'+programme.name+'</option>')
									}else{
                                        programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
									}
                                }
                                $("#productTypeId").append(programme_sel.join(' '));
                                //$("#productTypeId").val("");
                                $("#productTypeId").change();
                            }else {
                                $("#productTypeId").html('<option value="" selected>请选择</option>');
                                layerError(e.message,"错误提示")
                            }
                        },
                        error:function (e) {
                            ajaxLogout(e,null,"获取分类错误!请重试!");
                        }
                    }
                );
            }
        }

        function changeProductType(productTypeId) {
            $("#fluidDiv").empty()
			if(productTypeId !=null && productTypeId !=''){
                $.ajax({
                        async: false,
                        url:"${ctx}/provider/md/productType/ajax/findListByProductTypeId?productTypeId="+productTypeId,
                        success:function (e) {
                            if(e.success){
                                $("#productTypeItemId").empty();
                                var programme_sel=[];
                                programme_sel.push('<option value="" selected="selected">请选择</option>')
								var typeItemId = $("#editProductTypeItemId").val();
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var programme = e.data[i];
                                    if(typeItemId == programme.id){
                                        programme_sel.push('<option value="'+programme.id+'" selected="selected">'+programme.name+'</option>')
									}else{
                                        programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
									}
                                }
                                $("#productTypeItemId").append(programme_sel.join(' '));
                                //$("#productTypeItemId").val("");
                                $("#productTypeItemId").change();
                            }else {
                                $("#productTypeItemId").html('<option value="" selected>请选择</option>');
                                layerError(e.message,"错误提示")
                            }
                        },
                        error:function (e) {
                            ajaxLogout(e,null,"获取规格参数!请重试!");
                        }
                    }
                );
			}
        }

        function changeProductTypeItem(productTypeId,productTypeItemId) {
            $("#fluidDiv").empty()
			if(productTypeId !=null && productTypeId!='' && productTypeItemId!=null && productTypeItemId!=''){
                $.ajax({
                        async: false,
                        url:"${ctx}/provider/md/productType/ajax/findListByTypeIdAndItemId?productTypeId="+productTypeId +"&productTypeItemId=" + productTypeItemId,
                        success:function (e) {
                            if(e.success){
                                for(var i=0,len=e.data.length;i<len;i++){
                                    var productType = e.data[i];
                                    var strHtml = "";
                                    strHtml='<div class="row-fluid"><div class="span12"><div class="control-group"><label class="control-label">规格:</label>'+
                                        '<div class="controls"><span>'+productType.name+'</span></div></div><div class="control-group"><div class="controls">'
									var specItemHtml = ""
									for(var k=0,itemLen = productType.productSpecItemDtoList.length;k<itemLen;k++){
                                        var productSpecItem = productType.productSpecItemDtoList[k];
                                        if(productTypeId==editProductTypeId && productTypeItemId ==editProductTypeItemId){
                                            var isCheck = 0;
                                            for(var j = 0;j<specItemIdArr.length;j++){
                                                if(productSpecItem.id == specItemIdArr[j]){
                                                    specItemHtml=specItemHtml+'<label><input name="productSpecItems" type="checkbox" checked="checked" style="margin-left: 5px" value="'+productSpecItem.id+'" />' + productSpecItem.name + '</label>'
                                                    isCheck = 1
                                                }
                                            }
                                            if(isCheck == 0){
                                                specItemHtml=specItemHtml+'<label><input name="productSpecItems" type="checkbox" style="margin-left: 5px" value="'+productSpecItem.id+'" />' + productSpecItem.name + '</label>'
                                            }
										}else{
                                            specItemHtml=specItemHtml+'<label><input name="productSpecItems" type="checkbox" style="margin-left: 5px" value="'+productSpecItem.id+'" />' + productSpecItem.name + '</label>'
										}
									}
                                    var endHtml = '</div></div></div></div>'
									if(specItemHtml !=null && specItemHtml!=''){
                                        $("#fluidDiv").append(strHtml + specItemHtml+ endHtml);
									}
                                }
                            }else {
                                layerError(e.message,"错误提示")
                            }
                        },
                        error:function (e) {
                            ajaxLogout(e,null,"获取二级错误!请重试!");
                        }
                    }
                );
			}
        }

        function clickFile(){
            $("#picture_one").click();
        }

		// 发生改变触发
		function checkAttachment() {
			isExecute = true;
			var filepath = $("#picture_one").val();
			if(Utils.isEmpty(filepath)){
				$("#picture_one").val("");
				isExecute = false;
				return false;
			}
			var extStart=filepath.lastIndexOf(".");
			var ext=filepath.substring(extStart,filepath.length).toUpperCase();// 后缀
			if(ext != ".BMP" && ext != ".PNG" && ext != ".GIF" && ext != ".JPG" && ext != ".JPEG"){
				layerInfo("图片类型限于bmp,png,gif,jpeg,jpg格式","系统提示");
				$("#picture_one").val("");
				isExecute = false;
				return false;
			}
			//check size
			var files = document.getElementById("picture_one").files;
			var fileSize = files[0].size;
			//var size = fileSize / 1024;
			var size = fileSize.toFixed(2);
			if(size > (2*1024*1024)){
				layerInfo("图片不能大于2M","系统提示");
				$("#picture_one").val("");
				isExecute = false;
				return false;
			}
			uploadfile($("#picture_one"), "picture_one");
		}

		// 上传
		function uploadfile($obj1, obj2) {
			var data = {
				fileName: $obj1.val()
			};
			$.ajaxFileUpload({
				url: '${pageContext.request.contextPath}/servlet/UploadForMD?type=product&' + (new Date()).getTime(),
				secureuri: false,
				data: {},
				fileElementId: obj2, // file控件id
				dataType: 'json',
				success: function (data, status) {
					console.log(data);
					if (data && data.status === 'false'){
						layerError("文件上传失败，请重试!","错误", true);
						isExecute = false;
					} else {
						$("#attachment1").attr("value", data.fileName);
						var $img = $("[id='viewImg_one']");
						$img.attr("src","${ctxUpload}/" + data.fileName);
						$img.attr("data-original","${ctxUpload}/" +data.fileName);
						$img.attr("title","点击放大图片");
						$img.before("<a href='javascript:;' title='点击删除图片'" + " onclick=\"deletePic()\" class=\"upload_warp_img_div_del\"></a>");
						$("#divImg_one").removeClass("drag").removeClass("imgOnDarg");
						$("#divImg_one").removeAttr("onclick");
						$("#picture_one").val("");
						imageViewer();
						isExecute = false;
					}
				},
				error: function (data, status, e) {
					alert(e);
				}
			});
		}

		// 看大图
		function imageViewer(){
			var viewer = $("#divUploadWarp").viewer('destroy').viewer(
					{
						url: "data-original",
						filter:function(image) {
							if(image.src.lastIndexOf("/service_add.png")>0){
								return false;
							}
							return true;
						},
						viewed: function(image) {
						},
						shown:function () {
							// console.log(this.viewer);
							if(this.viewer.index == -1){
								this.viewer.hide();
								//$(".viewer-container").removeClass("viewer-in").addClass("viewer-hide");
							}
						}
					}
			);
		}

		// 删除照片
		function deletePic(){
			console.log("删除")
			event.stopPropagation(); //防止 $("#divImg_" + index) 的函数触发
			$("#divImg_one").attr("onclick","clickFile();");
			var $img = $("[id='viewImg_one']");
			$img.attr("src","${ctxStatic}/images/service_add.png");
			$img.removeAttr("data-original");
			$img.attr("title","点击上传图片");
			$img.closest(".upload_warp_img_div").addClass("drag");
			$img.prev().remove();
			$("#attachment1").attr("value", "");
			imageViewer();
			return false;
		}
	</script>

	  <style type="text/css">
		  .fromInput {
			  border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		  }
		  .form-horizontal {margin-top: 5px;}
		  .form-horizontal .control-label {width: 80px;}
		  .form-horizontal .controls {margin-left: 90px;}
		  .table tbody td {
			  vertical-align: top;
		  }
          .upload_warp_img_div_del{position:absolute;width:20px !important;height:20px !important;right:10px;margin-top: 0px !important;
              background-size: 20px 20px !important;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; border-radius: 4px;}
          .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height: 100%;}
          .upload_warp_left img{margin-top:0px}
          .upload_warp_left {float: left;width: 75px;border: 1px dashed #999;border-radius: 4px;cursor: pointer;
              margin-right: 10px;
          }
          .upload_warp{position: relative;}
		  #editBtn {
			  position: fixed;
			  left: 0px;
			  bottom: 0;
			  width: 100%;
			  height: 60px;
			  background: #fff;
			  z-index: 10;
			  border-top: 1px solid #e5e5e5;
		  }
	  </style>

  </head>
  <body>
  <%--  <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/product">产品列表</a></li>
		<li class="active"><a href="javascript:void(0);">产品<shiro:hasPermission name="md:product:edit">${not empty product.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:product:edit">查看</shiro:lacksPermission></a></li>
		<li><a href="${ctx}/md/product/sort">产品排序</a></li>
	</ul><br/>--%>
    <input type="hidden" id="editProductTypeId" value="${product.productTypeId}">
    <input type="hidden" id="editProductTypeItemId" value="${product.productTypeItemId}">
	<form:form id="inputForm" modelAttribute="product" action="${ctx}/md/product/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<input id="oldProductCategoryId" name="oldProductCategoryId" type="hidden" value="${product.category.id}"/>
		<sys:message content="${message}"/>
		<legend>产品信息</legend>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label"><span style="color: red">*</span>&nbsp;产品品类:</label>
					<div class="controls">
						<c:choose>
							<c:when test="${product.category.id>0}">
								<form:hidden id="categoryId" path="category.id"></form:hidden>
								<form:input  path="category.name" readonly="true"></form:input>
							</c:when>
							<c:otherwise>
								<select id="categoryId" name="category.id" class="input-small required selectCustomer" style="width:225px;">
									<option value=""
											<c:out value="${(empty product.category.id)?'selected=selected':''}" />>请选择</option>
									<c:forEach items="${fns:getProductCategories()}" var="productCategorie">
										<option value="${productCategorie.id}"
												<c:out value="${(product.category.id eq productCategorie.id)?'selected=selected':''}" />>${productCategorie.name}</option>
									</c:forEach>
								</select>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label"><span style="color: red">*</span>&nbsp;产品名称:</label>
					<div class="controls">
						<form:input path="name" htmlEscape="false" minLength = "2" maxlength="50" class="required"/>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label"><span style="color: red">*</span>&nbsp;拼音简称:</label>
					<div class="controls">
						<form:input path="pinYin" htmlEscape="false" minLength = "2" maxlength="50"/>
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">一级分类:</label>
					<div class="controls">
						<c:choose>
							<c:when test="${product.productTypeId>0}">
								<input type="hidden" name="productTypeId" value="${product.productTypeId}">
								<form:input  path="productTypeInfo" readonly="true" cssStyle="width: 300px"></form:input>
							</c:when>
							<c:otherwise>
								<select id="productTypeId" name="productTypeId" class="input-small" style="width:300px;">
									<option value="">请选择</option>
								</select>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<div class="controls" style="margin-left: 3px">
						<c:choose>
							<c:when test="${product.productTypeItemId>0}">
								<input type="hidden" name="productTypeItemId" value="${product.productTypeItemId}">
								<form:input  path="productSpecInfo" readonly="true" cssStyle="width: 310px"></form:input>
							</c:when>
							<c:otherwise>
								<select id="productTypeItemId" name="productTypeItemId" class="input-small" style="width:310px;">
									<option value="">请选择</option>
								</select>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
			<div class="span4">
				<div class="control-group">
					<label class="control-label">排序:</label>
					<div class="controls">
						<form:input path="sort" htmlEscape="false" maxlength="100" class="required digits"/>
					</div>
				</div>
			</div>
		</div>

		<div id="fluidDiv">
		</div>
		<div class="row-fluid">
			 <div class="span12">
				 <div class="control-group">
					 <label class="control-label">描述:</label>
					 <div class="controls">
						 <form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge" cssStyle="width: 1025px;height: 100px"/>
					 </div>
				 </div>
			 </div>
		</div>
		<div class="row-fluid">
			<div class="span4">
				<div class="control-group">
					<label class="control-label">套组:</label>
					<div class="controls">
						<form:select path="setFlag" htmlEscape="input-small">
							<form:option value="0" label="否"/>
							<form:option value="1" label="是"/>
						</form:select>
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid" id="divProduct">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">套组产品:</label>
					<div class="controls">
						<table valign="top" >
						<c:forEach items="${productList}" var="item" varStatus="in">
							<c:if test="${in.count%5 == 1}">
								<tr>
							</c:if>
								<td style="width:230px" valign="top">
								<c:choose>
									<c:when test="${product.productIds !=null and product.productIds!=''}">
										<c:set value="${ fn:split(product.productIds, ',') }" var="arr" />
										<c:set value="0" var="isCheck" />
										<c:forEach items="${arr}" var="productId">
											<c:choose>
												<c:when test="${item.id == productId}">
													<label style="padding: 3px"><input name="strProductIds" type="checkbox" checked="checked" value="${item.id}">
													${item.name}</label>
													<c:set value="1" var="isCheck" />
												</c:when>
											</c:choose>
										</c:forEach>
										<c:if test="${isCheck==0}">
											<label style="padding: 3px"><input name="strProductIds" type="checkbox" value="${item.id}">
											${item.name}</label>
										</c:if>
									</c:when>
									<c:otherwise>
										<label style="padding: 3px"><input name="strProductIds" type="checkbox" value="${item.id}">${item.name}</label>
									</c:otherwise>
								</c:choose>
								</td>
							<c:if test="${in.count%5 == 0 || in.last}">
								</tr>
							</c:if>
						</c:forEach>
						</table>
						<form:hidden path="productIds"/>
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid" id="divMaterial">
			<div class="span12">
				<legend>配件信息</legend>
				<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="width: 1140px">
					<thead>
					<tr>
						<th width="20"></th>
						<th width="200">配件分类</th>
						<th>配件</th>
					</tr>
					</thead>
					<tbody>
					<c:set var="index" value="0"></c:set>
					<c:forEach items="${materialCategoryList}" var="materialCategory">
						<tr>
							<c:set var="index" value="${index+1}"></c:set>
							<td><input type="checkbox" name="materialClick" id="abc${materialCategory.id}"></td>
							<td>${materialCategory.name}</td>
							<td><c:forEach items="${materialList}" var="material">
								    <c:if test="${material.materialCategory.id == materialCategory.id}">
										<c:choose>
											<c:when test="${product.materialIds !=null && product.materialIds!=''}">
												<c:set value="${ fn:split(product.materialIds, ',') }" var="materialIdArr"/>
												<c:set value="0" var="materialIsCheck" />
												<c:forEach items="${materialIdArr}" var="productMaterialId">
                                                    <c:choose>
														<c:when test="${material.id == productMaterialId}">
															<label><input type="checkbox" name="materialNames" checked="checked" value="${material.id}">${material.name}</label>
															<c:set value="1" var="materialIsCheck" />
														</c:when>
													</c:choose>
												</c:forEach>
												<c:if test="${materialIsCheck == 0}">
													<label><input type="checkbox" name="materialNames" value="${material.id}">${material.name}</label>
												</c:if>
											</c:when>
											<c:otherwise>
												<label><input type="checkbox" name="materialNames" value="${material.id}">${material.name}</label>
											</c:otherwise>
										</c:choose>
									</c:if>
							    </c:forEach>
							</td>
						</tr>
					</c:forEach>
					</tbody>
				</table>
				<form:hidden path="materialIds"/>
				<form:hidden path="specItemIds"/>
			</div>
		</div>
		<div style="height: 100px;display: inline-block;">
			<label class="control-label">产品图片:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${product.attachment1 != null && product.attachment1 ne ''}">
						<div class="upload_warp" id="divUploadWarp">
							<div class="upload_warp_left" data-code="" data-index="">
								<div class="upload_warp_img_div drag" id="divImg_one">
									<a href='javascript:;' title='点击删除图片' onclick="deletePic()" class="upload_warp_img_div_del"></a>
									<img title="点击放大图片" id="viewImg_one"  data-original="${ctxUpload}/${product.attachment1}" src="${ctxUpload}/${product.attachment1}" onclick="imageViewer()"/>
								</div>
							</div>
							<input id="picture_one" type="file" class="hero-unit" style="display: none" size="20" name="picture_one"
								   onchange="checkAttachment()"/>
							<input style="display: none" name="attachment1" id="attachment1" value="${product.attachment1}">
						</div>
					</c:when>
					<c:otherwise>
						<div class="upload_warp" id="divUploadWarp">
							<div class="upload_warp_left" data-code="" data-index="">
								<div class="upload_warp_img_div drag" id="divImg_one" onclick="clickFile()">
									<img id="viewImg_one" title="点击上传图片" src="${ctxStatic}/images/service_add.png" />
								</div>
							</div>
							<input id="picture_one" type="file" class="hero-unit" style="display: none" size="20" name="picture_one"
								   onchange="checkAttachment()"/>
							<input style="display: none" name="attachment1" id="attachment1" value="${product.attachment1}">
						</div>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</form:form>
    <div style="height: 60px;width: 100%"></div>

	  <div id="editBtn">
		  <shiro:hasPermission name="md:producttype:edit">
		  <input id="btnSubmit" class="btn btn-primary" type="submit" style="width: 104px;height: 40px;margin-left: 1000px;margin-top: 10px;margin-bottom: 10px" onclick="$('#inputForm').submit()" value="保 存"/>
		  </shiro:hasPermission>
		  <input id="btnCancel" class="btn" type="button" value="取 消" onclick="cancel()" style="width: 104px;height: 40px;margin-top: 10px;margin-left: 13px;margin-bottom: 10px"/>
	  </div>

  </body>
</html>
