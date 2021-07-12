<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点付款</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script type="text/javascript">
		function validSearch(){
			if ($("#bank").val()==null || $("#bank").val()==""){
				top.$.jBox.error("请选择银行!", '下游网点付款');
			}
			else{
				top.$.jBox.tip('请稍候...', 'loading');
				$("#searchForm").submit();
			}
		}
		
		function repage(){
			$("#searchForm").submit();
	    	return false;
		}
		
		$(document).ready(function() {
			$("#aSave").fancybox({
	        	maxWidth : 1400,
				maxHeight : 740,
				fitToView : false,
				width  : '100%',
				height  : '95%',
				autoSize : false,
				closeClick : false,
				type  : 'iframe',
				openEffect : 'none',
				closeEffect : 'none'
			});
			 $('a[data-toggle=tooltip]').darkTooltip();
			 $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
		});
	    function payEngineer(engineerId, engineerNo, engineerName, phone, mobile, paymentType, bank, bankNo, bankOwner, totalCharge, closeMonth, profileId, bankIssue,debtsCharge,debtsDescption,branch){	    	
			$("#aSave").attr("href", "${ctx}/fi/engineerinvoice/save?engineerId="+engineerId+"&engineerNo="+encodeURI(encodeURI(engineerNo))+"&engineerName="+encodeURI(encodeURI(engineerName))+"&phone="+phone+"&mobile="+mobile+"&paymentType="+paymentType+"&bank="+bank+"&bankNo="+bankNo+"&bankOwner="+encodeURI(encodeURI(bankOwner))+"&totalCharge="+totalCharge+"&beginDate="+$("#beginDate").val()+"&endDate="+$("#endDate").val()+"&engineerChargeNo="+$("#engineerChargeNo").val()+"&orderNo="+$("#orderNo").val()+"&completedBeginDate="+$("#completedBeginDate").val()+"&completedEndDate="+$("#completedEndDate").val()+"&closeMonth="+closeMonth+"&profileId="+profileId+"&bankIssue="+bankIssue+"&debtsCharge="+debtsCharge+"&debtsDescption="+encodeURI(encodeURI(debtsDescption))+"&branch="+encodeURI(encodeURI(branch)));
 	    	 		$("#aSave").click();
	    }
	    
	    //结算方式,银行联动
	    $(document).on("change", "#payment", function() {
			if ($(this).val()==""){
				top.$.jBox.confirm('请选择结算方式', '下游网点付款');
   	            $("#bank option").remove();  
   	            $("#s2id_bank").find("span").html(''); 	
   	            $("#engineer option").remove();  
   	            $("#s2id_engineer").find("span").html(''); 	
			}
			else{
				$.ajax({
				type: "GET",
				url: "${ctx}/fi/servicepointwithdraw/getbanklist?paymenttype="+$(this).val() + "&" + (new Date()).getTime(),
				data:"",
				async: false,
				success: function (data) {
   	            	if (data.success){   
   	            		$("#bank option").remove(); 
   	            		$("#s2id_bank").find("span").html('');
		   	            $("#engineer option").remove();  
		   	            $("#s2id_engineer").find("span").html(''); 	
	    			    var option = document.createElement("option");   
	    			    option.text =  "请选择";   
	    			    option.value =  "";
	    			    $("#bank")[0].options.add(option);
			    	   	$.each(data.data, function(i, item) {
		    			   var option = document.createElement("option");   
		    			   option.text =  item.text;   
		    			   option.value =  item.value;
		    			   $("#bank")[0].options.add(option);
			    		});	
			    		$("#bank option:nth-child(1)").attr("selected","selected"); 
//   	            		$("#s2id_bank").find("span").html("请选择");
   	            	}
   	            	else{
    	            	top.$.jBox.closeTip();
   	            		top.$.jBox.error(data.message);
  	            	}
   	            }, 
   	            error: function (xhr, ajaxOptions, thrownError) {
   	            	top.$.jBox.closeTip();
   	                top.$.jBox.error(thrownError.toString());
	    	    } 
			});
			}
		});

		$(document).on("click", "#btnExport", function () {
			if ($("#bank").val()==null || $("#bank").val()==""){
				top.$.jBox.error("请选择银行!", '下游网点付款');
			}
			else{
	
				top.$.jBox.confirm("确认要导出安维付款明细吗？","系统提示",function(v,h,f){
				if(v=="ok"){				
					$("#searchForm").attr("action","${ctx}/fi/engineerinvoice/export");
					$("#searchForm").submit();
					$("#searchForm").attr("action","${ctx}/fi/engineerinvoice/form");
				}
			},{buttonsFocus:1});
			top.$('.jbox-body .jbox-icon').css('top','55px');
			}
		});
	    
	    //银行,安维人员联动
	    $(document).on("change", "#bank", function() {
			if ($(this).val()==""){
				top.$.jBox.confirm('请选择银行', '下游安维付款');
   	            $("#engineer option").remove();  
   	            $("#s2id_engineer").find("span").html(''); 	
			}
			else{
				$.ajax({
				type: "GET",
				url: "${ctx}/fi/servicepointwithdraw/getengineerlist?bank="+$(this).val() + "&paymenttype=" + $("#payment").val() + "&" + (new Date()).getTime(),
				data:"",
				async: false,
				success: function (data) {
   	            	if (data.success){   
   	            		$("#engineer option").remove(); 
   	            		$("#s2id_engineer").find("span").html('');
	    			    var option = document.createElement("option");   
	    			    option.text =  "所有";   
	    			    option.value =  "";
	    			    $("#engineer")[0].options.add(option);
			    	   	$.each(data.data, function(i, item) {
		    			   var option = document.createElement("option");   
		    			   option.text =  item.text;   
		    			   option.value =  item.value;
		    			   $("#engineer")[0].options.add(option);
			    		});
   	            	}
   	            	else{
    	            	top.$.jBox.closeTip();
   	            		top.$.jBox.error(data.message);
  	            	}
   	            }, 
   	            error: function (xhr, ajaxOptions, thrownError) {
   	            	top.$.jBox.closeTip();
   	                top.$.jBox.error(thrownError.toString());
	    	    } 
			});
			}
		});
	</script>
  </head>
  
  <body>
    <ul class="nav nav-tabs">
		<li class="active"><a href="javascript:void(0);">下游安维付款</a></li>
	</ul>
	<form:form id="searchForm" action="${ctx}/fi/engineerinvoice/form" method="post" class="breadcrumb form-search">
		<input id="engineerId" name="engineerId" type="hidden" value="${engineerId}"/>
		<input id="hBeginDate" name="hBeginDate" type="hidden" value="${beginDate}"/>
		<input id="hEndDate" name="hEndDate" type="hidden" value="${endDate}"/>
		<input id="hEngineerChargeNo" name="hEngineerChargeNo" type="hidden" value="${engineerChargeNo}"/>
		<a id="aSave" type="hidden" href="" class="fancybox"  data-fancybox-type="iframe"></a>
		<div>
			<c:set var="paymenttypeList" value="${paymenttypeList}" />
			<label>结算方式：</label>
			<select id="payment" name="payment" style="width:140px;">
				<option value="" selected="selected">请选择</option>
				<c:forEach items="${paymenttypeList}" var="paymenttype">
				   <option value="${paymenttype}" <c:out value="${(payment eq paymenttype)?'selected=selected':''}" />>${fns:getDictLabel(paymenttype, 'PaymentType', '')}</option>
				</c:forEach>
			</select>
			<c:set var="bankList" value="${bankList}" />
			<label>银行：</label>
			<select id="bank" name="bank" style="width:140px;">	
				<option value="" selected="selected">请选择</option>
				<c:forEach items="${bankList}" var="b">
				   <option value="${b.value}" <c:out value="${(bank eq b.value)?'selected=selected':''}" />>${b.text}</option>
				</c:forEach>			
			</select>
			<label>网点：</label>
			<select id="engineer" name="engineer" style="width:180px;">
				<option value="" selected="selected">所有</option>
				<c:forEach items="${engineerList}" var="e">
				   <option value="${e.value}" <c:out value="${(engineer eq e.value)?'selected=selected':''}" />>${e.text}</option>
				</c:forEach>			
			</select>
			<label>网点状态：</label>
			<select id="engineer_status" name="engineer_status" style="width:140px;">
				<option value="" selected="selected">所有</option>
				<option value="0" <c:out value="${(engineer_status eq '0')?'selected=selected':''}" />>正常</option>
				<option value="1" <c:out value="${(engineer_status eq '1')?'selected=selected':''}" />>异常</option>

			</select>
			&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="button" value="查询" onclick="validSearch();"/>
			&nbsp;&nbsp;&nbsp;<input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
		</div>
	</form:form>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead>
			<tr>
				<th>序号</th>
				<th>结算方式</th>
				<th>安维编号</th>
				<th>安维姓名</th>
				<th>联系电话</th>
				<th>手机</th>
				<th>开户银行</th>
				<th>开户人</th>
				<th>银行帐号</th>
				<th>服务费</th>
				<th>拆机费</th>
				<th>远程费</th>
				<th>配件费</th>
				<th>其他</th>
				<th>合计</th>
				<th>欠款额</th>
				<th style="display:none;" >欠款描述</th>
				
				<th>操作</th>
			</tr>
		</thead>
		<tbody>
		<%int i=0; %>
		<c:set var="totalServiceCharge" value="0" />
		<c:set var="totalDismantleCharge" value="0" />
		<c:set var="totalTravelCharge" value="0" />
		<c:set var="totalMaterialCharge" value="0" />
		<c:set var="totalOtherCharge" value="0" />
		<c:forEach items="${list}" var="groupedChargeItem">
			<tr>
			<%i++;%>
				<td><%=i%></td>
				<td>${fns:getDictLabel(groupedChargeItem[1], 'PaymentType', '')}</td>
				<td>${groupedChargeItem[2]}</td>
				<c:choose>
					<c:when test="${(empty groupedChargeItem[16])}">
						<td>${groupedChargeItem[3]}</td>
					</c:when>
					<c:otherwise>
						<td style="background-color: #f2dede;"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${fns:getDictLabelFromMS(groupedChargeItem[16], 'BankIssueType', '')}">${groupedChargeItem[3]}</a></td><%-- 切换为微服务 --%>
					</c:otherwise>
				</c:choose>
				<td>${groupedChargeItem[4]}</td>
				<td>${groupedChargeItem[5]}</td>
				<td>${fns:getDictLabel(groupedChargeItem[6], 'banktype', '')}</td>
				<td>${groupedChargeItem[7]}</td>
				<td>${groupedChargeItem[8]}</td>		
				<td>${groupedChargeItem[9]}</td>
				<td>${groupedChargeItem[10]}</td>
				<td>${groupedChargeItem[11]}</td>
				<td>${groupedChargeItem[12]}</td>
				<td>${groupedChargeItem[13]}</td>
				<td style="color:red"><B>${groupedChargeItem[9]+groupedChargeItem[10]+groupedChargeItem[11]+groupedChargeItem[12]+groupedChargeItem[13]}</B></td>
				<td>${groupedChargeItem[17]}</td>
				<td style="display:none;" >${groupedChargeItem[18]}</td>
				
				<c:set var="totalServiceCharge" value="${totalServiceCharge+groupedChargeItem[9]}" />
				<c:set var="totalDismantleCharge" value="${totalDismantleCharge+groupedChargeItem[10]}" />
				<c:set var="totalTravelCharge" value="${totalTravelCharge+groupedChargeItem[11]}" />
				<c:set var="totalMaterialCharge" value="${totalMaterialCharge+groupedChargeItem[12]}" />
				<c:set var="totalOtherCharge" value="${totalOtherCharge+groupedChargeItem[13]}" />
				<c:set var="totalDebtsCharge" value="${totalDebtsCharge+groupedChargeItem[17]}" />
				
				<td><input type="Button" id="${groupedChargeItem[0]}" class="btn btn-mini btn-danger" value="付款" onclick="payEngineer('${groupedChargeItem[0]}','${groupedChargeItem[2]}','${groupedChargeItem[3]}','${groupedChargeItem[4]}','${groupedChargeItem[5]}','${groupedChargeItem[1]}','${groupedChargeItem[6]}','${groupedChargeItem[8]}','${groupedChargeItem[7]}','${groupedChargeItem[9]+groupedChargeItem[10]+groupedChargeItem[11]+groupedChargeItem[12]+groupedChargeItem[13]}','${groupedChargeItem[14]}','${groupedChargeItem[15]}','${groupedChargeItem[16]}','${groupedChargeItem[17]}','${groupedChargeItem[18]}','${groupedChargeItem[19]}')"/></td>
			</tr>
		</c:forEach>
		<tr>
			<td style="text-align:right;" colspan="9" ><B>合计</B></td>
			<td><B>${totalServiceCharge}</B>
			<input id="totalServiceCharge" name="totalServiceCharge" type="hidden" value="${totalServiceCharge}"/></td>
			<td><B>${totalDismantleCharge}</B>
			<input id="totalDismantleCharge" name="totalDismantleCharge" type="hidden" value="${totalDismantleCharge}"/></td>
			<td><B>${totalTravelCharge}</B>
			<input id="totalTravelCharge" name="totalTravelCharge" type="hidden" value="${totalTravelCharge}"/></td>
			<td><B>${totalMaterialCharge}</B>
			<input id="totalMaterialCharge" name="totalMaterialCharge" type="hidden" value="${totalMaterialCharge}"/></td>
			<td><B>${totalOtherCharge}</B>
			<input id="totalOtherCharge" name="totalOtherCharge" type="hidden" value="${totalOtherCharge}"/></td>
			<td style="color:red;"><B>${totalServiceCharge+totalDismantleCharge+totalTravelCharge+totalMaterialCharge+totalOtherCharge}</B>
			</td>
			<input id="totalDebtsCharge" name="totalDebtsCharge" type="hidden" value="${totalDebtsCharge}"/></td>
			<td><B>${totalDebtsCharge}</B>
			<td></td>
		</tr>
		</tbody>
	</table>
  </body>
</html>
