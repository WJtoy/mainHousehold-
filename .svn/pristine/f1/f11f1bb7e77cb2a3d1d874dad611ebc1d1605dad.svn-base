<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>客户产品型号</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@include file="/WEB-INF/views/include/treetable.jsp"%>
	<c:set var="currentuser" value="${fns:getUser()}"/>
	<script type="text/javascript">
        $(document).ready(function() {
            if(${currentuser.isCustomer()==true}){
                changeCustomer();
			}
            if($("#id").val()!=null && $("#id").val()!='' && ${currentuser.isCustomer()==false}){
                changeCustomer();
                changeProduct();
			}
            $("th").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });

        function checkPrice(obj){
            var reg = $(obj).val().match(/\d+\.?\d{0,2}/);
            var txt = '';
            if (reg != null) {
                txt = reg[0];
            }
            $(obj).val(txt);
        }

        var clickTag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if(clickTag == 1){
                return false;
            }
            if (!$("#inputForm").valid()) {
                return false;
            }

            clickTag = 1;
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            var entity = {};
            entity['customer.id'] = $("#customerId").val();
            entity['product.id'] = $("#productId").val();
            $("input[type='checkbox'][name='checkedRecords']:checkbox:checked").each(function(i,element){
                var index = this.value;
                var materialId = $("input[name=materialId_"+index +"]").val();
                entity['itemList['+i+'].materialId'] = materialId;
                var isReturn = $('input[name=isReturn_'+ index+ ']:checked').val();
                entity['itemList['+i+'].isReturn'] = isReturn;
                var price = $('input[name=price_'+ index+ ']').val();
                entity['itemList['+i+'].price'] = price;
                var remarks = $('input[name=remarks_'+ index+ ']').val();
                entity['itemList['+i+'].remarks'] = remarks;
            });
            var loadingIndex;
            var options = {
                url: "${ctx}/md/customerMaterial/save",                 //默认是form的action， 如果申明，则会覆盖
                type: 'post',               //默认是form的method（get or post），如果申明，则会覆盖
                dataType: 'json',           //html(默认), xml, script, json...接受服务端返回的类型
                data: entity,
                beforeSubmit: function(formData, jqForm, options){
                    loadingIndex = layer.msg('正在提交，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                    return true;
                },  //提交前的回调函数
                success:function (data)
                {
                    //提交后的回调函数
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
                    //var msg = eval(data);
                    if (data.success) {
                        layerMsg("保存成功");
                        var curFrame = getActiveTabIframe();
                        if(curFrame!=undefined){
                            var idSale = $("#idSale").val();
                            if(idSale=="true"){
                                var customerId = $("#customerId").val();
                                curFrame.document.location="${ctx}/md/customerMaterial/list?customer.id=" + customerId;
                            }else{
                                curFrame.document.location="${ctx}/md/customerMaterial/list";
                            }

                            //.attr('src', "${ctx}/md/product/pic");
                        }else{
                            history.go(-1);
                        }
                    }else{
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        layerError("数据保存错误:" + data.message, "错误提示");
                    }
                    return false;
                },
                error: function (data)
                {
                    setTimeout(function () {
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                    }, 2000);
                    ajaxLogout(data,null,"数据保存错误，请重试!");
                    //var msg = eval(data);
                },
                //clearForm: true,          //成功提交后，清除所有表单元素的值
                //resetForm: true,          //成功提交后，重置所有表单元素的值
                timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
            };
            if($("#treeTable input[type='checkbox'][name='checkedRecords']:checkbox:checked").length == 0){
                 var confirmIndex = layer.confirm('您没有选择配件确定要继续提交吗', {
                    btn: ['确定','取消'], //按钮
                }, function(){
                    $("#submitForm").ajaxSubmit(options);
                    return false;
                },function(){
                    clickTag = 0;
                    $btnSubmit.removeAttr('disabled');
                    layer.close(confirmIndex);
                    return false;
				});
            }else{
                $("#submitForm").ajaxSubmit(options);
                // $("#inputForm").ajaxForm(options).submit(function(){
                //     return false;//防止默认的提交动作，即防止二次提交
                // });
                return false;
			}
        });
        
        function changeCustomer() {
            var customerId =$("#customerId").val();
            if (customerId == "")
            {
                return false;
            }
            $.ajax({
                    url:"${ctx}/md/product/ajax/customerProductList?customerId="+customerId,
                    success:function (e) {
                        if(e.success){
                            $("#productId").empty();
                            var programme_sel=[];
                            var hiddenProductId = $("#hiddenProductId").val();
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                if(programme.id == hiddenProductId){
                                    programme_sel.push('<option value="'+programme.id+'" selected="selected">'+programme.name+'</option>')
								}else{
                                    programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
								}
                            }
                            $("#productId").append(programme_sel.join(' '));
                            $("#productId").change();
                            $("#hiddenProductId").val("");
                        }else {
                            $("#productId").html('<option value="" selected>请选择</option>');
                            $("#productId").change();
                        }
                    },
                    error:function (e) {
                        layerError("请求客户产品失败","错误提示");
                    }
                }
            );
        }


        function changeProduct(){
            var customerId = $("#customerId").val();
            var productId = $("#productId").val();
            if(customerId ==null || customerId==''){
                layerMsg("请先选择客户")
                return false;
            }
            if(productId ==null || productId==''){
                return false;
            }
            $.ajax({
                    url:"${ctx}/md/customerMaterial/ajax/getMaterialListByProductId?productId="+productId +"&customerId=" + customerId,
                    success:function (e) {
                        if(e.success == true){
                            var material_sel=[];
                            for(var i=0,len=e.data.materialList.length;i<len;i++){
                                var flag = 0;
                                var programme = e.data.materialList[i];
                                for(var j=0;j<e.data.customerMaterialList.length;j++){
                                    var customerMaterial = e.data.customerMaterialList[j];
                                   if(programme.id==customerMaterial.material.id){
                                       flag = 1;
                                       if(customerMaterial.isReturn==1){
                                           material_sel.push('<tr class="success" id="trId_'+i+'"><td><input type="checkbox" name="checkedRecords" value="'+i+'" checked="checked" onchange="checkboxOnchange(this)"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="radio" name ="isReturn_'+i+'" value="0" /> 否&nbsp;&nbsp;<input type="radio" name ="isReturn_'+i+'" value="1" checked="checked" /> 是</td><td><input type="text" name ="price_'+i+'" value="'+customerMaterial.price +'"  maxlength="6" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'" value="'+customerMaterial.remarks+'"  maxlength="230"></td></tr>');
                                       }else{
                                           material_sel.push('<tr class="success" id="trId_'+i+'"><td><input type="checkbox" name="checkedRecords" value="'+i+'" checked="checked" onchange="checkboxOnchange(this)"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="radio" name ="isReturn_'+i+'" value="0" checked="checked"/> 否&nbsp;&nbsp;<input type="radio" name ="isReturn_'+i+'" value="1" /> 是</td><td><input type="text" name ="price_'+i+'" value="'+customerMaterial.price +'"  maxlength="6" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'" value="'+customerMaterial.remarks+'"  maxlength="230"></td></tr>');
                                       }
								   }
								}
								if(flag!=1){
                                    if(programme.isReturn==1){
                                        material_sel.push('<tr id="trId_'+i+'"><td><input type="checkbox" id="checkbox_'+i+'" name="checkedRecords" value="'+i+'"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="radio" name ="isReturn_'+i+'" value="0" /> 否&nbsp;&nbsp;<input type="radio" name ="isReturn_'+i+'" value="1" checked="checked" /> 是</td><td><input type="text" name ="price_'+i+'" value="'+programme.price +'"  maxlength="6" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'"  maxlength="230"></td></tr>');
                                    }else{
                                        material_sel.push('<tr id="trId_'+i+'"><td><input type="checkbox" id="checkbox_'+i+'" name="checkedRecords" value="'+i+'"></td><td>'+programme.name+'<input type="hidden" name="materialId_'+i+'" value="'+programme.id+'"></td><td><input type="radio" name ="isReturn_'+i+'" value="0" checked="checked" /> 否&nbsp;&nbsp;<input type="radio" name ="isReturn_'+i+'" value="1" /> 是</td><td><input type="text" name ="price_'+i+'" value="'+programme.price +'"  maxlength="6" onkeyup="checkPrice(this)"></td><td><input type="text" name="remarks_'+i+'"  maxlength="230"></td></tr>');
                                    }
								}
                            }
                            $("#materialInfo").empty()
                            $("#materialInfo").append(material_sel.join(' '));
                        }else if(e.success == false){
                            layerAlert(e.message,"提示");
                        }
                    },
                    error:function (e) {
                        ajaxLogout(e.responseText,null,"请求产品配件失败","错误提示！");
                    }
                }
            );
        }

        function checkboxOnchange(obj) {
          var value = $(obj).val();
          if($(obj).is(':checked')){
              if($("#trId_" + value).hasClass("warning")){
                  $("#trId_" + value).attr("class", "success");
			  }
		  }else{
              if($("#trId_" + value).hasClass("success")){
                  $("#trId_" + value).attr("class", "warning");
              }
		  }
        }

	</script>
	<style type="text/css">
		.form-horizontal .control-label{
			width: 100px;
		}
		.form-horizontal .controls{
			margin-left: 120px;
		}
		.form-horizontal .control-group{
			margin-bottom: 15px;
		}
		.fromInput {
			border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
		}
	</style>
</head>

<body>
<ul class="nav nav-tabs">
	<li>
		<a href="${ctx}/md/customerMaterial/list">列表</a>
	</li>
	<li class="active">
		<a href="javascript:void(0);">添加</a>
	</li>
</ul><br>
<input type="hidden" id="hiddenProductId" value="${customerMaterial.product.id}">
<input type="hidden" id="idSale" value="${currentuser.isSaleman()}">
<form:form id="inputForm" modelAttribute="customerMaterial" action="${ctx}/md/customerMaterial/save" method="post" class="form-horizontal">
	<sys:message content="${message}" />
	<c:if test="${canAction == true}">
		<form:hidden path="id"></form:hidden>
		<div class="control-group">
			<label class="control-label">客户:</label>
			<div class="controls">
				<c:choose>
					<c:when test="${currentuser.isCustomer()==true}">
						<form:hidden path="customer.id" id="customerId"></form:hidden>
						<form:input path="customer.name" readonly="true"></form:input>
					</c:when>
					<c:otherwise>
						<select id="customerId" name="customer.id" class="input-small required selectCustomer" style="width:225px;" onchange="changeCustomer()">
							<option value=""
									<c:out value="${(empty customerMaterial.customer.id)?'selected=selected':''}" />>请选择</option>
							<c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
								<option value="${customer.id}"
										<c:out value="${(customerMaterial.customer.id eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
							</c:forEach>
						</select>
					</c:otherwise>
				</c:choose>
				<span class="add-on red">*</span>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label">产品:</label>
			<div class="controls">
				<select id="productId" name="product.id" style="width:225px;" class="required" onchange="changeProduct()">
					<option value="" selected>请选择</option>
				</select>
				<span class="add-on red">*</span>
			</div>
		</div>
		<div class="control-group">
			<div class="controls">
				<table id="treeTable"
					   class="table table-striped table-bordered table-condensed" style="margin-top: 20px;">
					<thead>
					<tr>
						<th>选择</th>
						<th>配件名称</th>
						<th>是否返件</th>
						<th>参考价格</th>
						<th>备注</th>
					</tr>
					</thead>
					<tbody id="materialInfo">

					</tbody>
				</table>
			</div>
		</div>
	</c:if>
	<div id="formActions" class="form-actions">
		<c:if test="${canAction == true}">
			<shiro:hasPermission name="md:customermaterial:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit"
					   value="保 存" />&nbsp;</shiro:hasPermission>
		</c:if>
		<input id="btnCancel" class="btn" type="button" value="返 回"
			   onclick="history.go(-1)" />
	</div>
</form:form>
<form:form id="submitForm" ></form:form>
</body>
</html>
