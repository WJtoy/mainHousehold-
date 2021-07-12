<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <title>添加网点</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("#btnSubmit").click(function () {
                var $btnSubmit = $("#btnSubmit");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                $("#btnSubmit").prop("disabled",true);
                if (!$("#inputForm").valid()) {
                    $("#btnSubmit").prop("disabled",false);
                    return false;
                }
                //$btnSubmit.attr("disabled", "disabled");
                //$("#inputForm").attr("action", "${ctx}/md/servicepoint/save");
                $("#inputForm").submit();
            });

            $("#inputForm").validate({
                rules: {
                    servicePointNo: {remote: "${ctx}/md/servicepoint/checkNo?id=${servicePoint.id}"},
                    contactInfo1: {remote: "${ctx}/md/servicepoint/checkContact?id=${servicePoint.id}"},
                    "finance.bankNo": {remote: "${ctx}/md/servicepoint/checkBankNo?id=${servicePoint.id}"}
//                    "primary.contactInfo":{
//                        checkEngineerMobile:true
//                    }
                },
                messages: {
                    servicePointNo: {remote: "服务网点编号已存在"},
                    contactInfo1: {remote: "服务网点手机号被注册"},
                    "finance.bankNo": {remote: "服务网点银行卡号已存在"}
                },
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                submitHandler: function (form) {
                    //check
                    var degree = $("input[name='degree']:checked").val();
                    var servicePointNo = $("#servicePointNo").val();
                    servicePointNo = servicePointNo.substr(0,2);
                    if(servicePointNo.toUpperCase()=='YH' && degree!=30){
                        layerMsg("YH开头的网点编号必须是返现网点,请确认");
                        $("#btnSubmit").prop("disabled",false);
                        return false;
                    }
                    if(degree == 30){
                        var bank = $("[id='finance.bank.value']").val();
                        if(bank==null || bank=='' || bank==0){
                            layerMsg("请选择开户银行");
                            $("#btnSubmit").prop("disabled",false);
                            return false;
                        }
                        var branch = $("[id='finance.branch']").val();
                        if(branch==null || branch==''){
                            layerMsg("分行信息不能为空");
                            $("#btnSubmit").prop("disabled",false);
                            return false;
                        }

                        var bankNo = $("[id='finance.bankNo']").val();
                        if(bankNo==null || bankNo==''){
                            layerMsg("账号信息不能为空");
                            $("#btnSubmit").prop("disabled",false);
                            return false;
                        }

                        var bankOwner = $("[id='finance.bankOwner']").val();
                        if(bankOwner==null || bankOwner==''){
                            layerMsg("开户姓名不能为空");
                            $("#btnSubmit").prop("disabled",false);
                            return false;
                        }
                    }
                    var bankOwnerIdNo = $("#bankOwnerIdNo").val();
                    if(bankOwnerIdNo!=null && bankOwnerIdNo!=''){
                        if(!IdentityCodeValid(bankOwnerIdNo)){
                            layerInfo("请输入正确的身份证号", "信息提示");
                            $("#btnSubmit").prop("disabled",false);
                            return false;
                        }
                    }
                    //area
                    if ($('input[type="checkbox"][name="subAreaId"]:checked').size() == 0) {
                        layerMsg("请选择服务区域");
                        $("#btnSubmit").prop("disabled",false);
                        return false;
                    }
                    if ($('input[type="checkbox"][name="productId"]:checked').size() == 0) {
                        layerMsg("请选择产品");
                        $("#btnSubmit").prop("disabled",false);
                        return false;
                    }
					var subAreaId =[];
                    $('input[type="checkbox"][name="subAreaId"]:checked').each(function(){
                        subAreaId.push($(this).val());
                    });
					$("#areaIds").val(subAreaId);

                    //product
					var productIds =[];
                    $('input[type="checkbox"][name="productId"]:checked').each(function(){
                        productIds.push($(this).val());
                    });
                    $("#productIds").val(productIds);
                    $("#pointModelDegree").val(degree);
                    var loadingIndex = layerLoading('正在提交，请稍等...');
                    //form.submit();
                    $.ajax({
                        url:"${ctx}/md/servicepoint/saveServicePointForPlan",
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
                                    $("#btnSubmit").removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                setTimeout(function () {
                                    var areaId = $("#pointModelAreaId").val();
                                    var subAreaId = $("#pointModelSubAreaId").val();
                                    var productCategoryId = $("#pointModelProductCategoryId").val();
                                    var address = $("#pointModelAddress").val();
                                    var layerIndex = $("#layerIndex").val();
                                    var parentLayerIndex = $("#parentLayerIndex").val();
                                    var pointModelDegree = $("#pointModelDegree").val();
                                    window.location.href='${ctx}/md/servicepoint/selectForPlan?dialogType=layer&area.id='+areaId+'&subArea.id='+subAreaId+'&productCategoryId='+productCategoryId+'&address='+encodeURI(address)+'&degree='+pointModelDegree+'&layerIndex='+layerIndex+'&parentLayerIndex='+parentLayerIndex;
                                }, 2000);
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $("#btnSubmit").removeAttr('disabled');
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
                                $("#btnSubmit").removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        }
                    });
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
            $("input[name='degree']").change(function(){
                if($(this).val()==30){
                    $("span[name='bankInfo']").show()
                }else {
                    $("span[name='bankInfo']").hide();
                }
            });
        });

		//取消
		function cancel() {
            top.layer.close(this_index);//关闭本身
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
            11 : "北京",12 : "天津",13 : "河北",14 : "山西",15 : "内蒙古",21 : "辽宁",22 : "吉林",
            23 : "黑龙江 ",31 : "上海",32 : "江苏",33 : "浙江",34 : "安徽",35 : "福建",36 : "江西",
            37 : "山东",41 : "河南",42 : "湖北 ",43 : "湖南",44 : "广东",45 : "广西",46 : "海南",
            50 : "重庆",51 : "四川",52 : "贵州",53 : "云南",54 : "西藏 ",61 : "陕西",62 : "甘肃",
            63 : "青海",64 : "宁夏",65 : "新疆",71 : "台湾",81 : "香港",82 : "澳门",91 : "国外 "
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
            /*if (pass){
                var iCard = IdentityCode_changeFivteenToEighteen(card);
                if (parseInt(iCard.charAt(16)) % 2 == 0) {
                    sex = "0"; // 女生
                } else {
                    sex = "1"; // 男生
                }
                return sex;
            } else {
                alert(tip);
            }*/
            return pass;
        }

	</script>
	  <style type="text/css">
		  legend span {
			  border-bottom: #0096DA 4px solid;
			  padding-bottom: 6px;}
		  .form-horizontal .control-label{
			  width: 130px;
		  }
		  .form-horizontal .controls{
			  margin-left: 140px;
		  }
	  </style>
  </head>
  
  <body>
    <ul id="navtabs" class="nav nav-tabs">
      <%--<li class="active" id="trtLi"><a href="javascript:findConnomServicePoint();" style="width: 70px;text-align: center" title="常用网点">常用网点</a></li>
      <li id="commonLi"><a href="javascript:findTryServicePoint()" style="width: 70px;text-align: center" title="试单网点">试单网点</a></li>--%>
      <c:forEach items="${fns:getDictListFromMS('degreeType')}" var="dict">
          <li><a href="${ctx}/md/servicepoint/selectForPlan?dialogType=layer&area.id=${servicePointModel.area.id}&subArea.id=${servicePointModel.subArea.id}&productCategoryId=${servicePointModel.productCategoryId}&address=${fns:urlEncode(servicePointModel.address)}&degree=${dict.value}&layerIndex=${servicePointModel.layerIndex}&parentLayerIndex=${servicePointModel.parentLayerIndex}" style="width: 70px;text-align: center" title="${dict.label}">${dict.label}</a></li>
      </c:forEach>
      <li><a href="${ctx}/md/servicepoint/unableSelectForPlan?dialogType=layer&area.id=${servicePointModel.area.id}&subArea.id=${servicePointModel.subArea.id}&productCategoryId=${servicePointModel.productCategoryId}&address=${fns:urlEncode(servicePointModel.address)}&layerIndex=${servicePointModel.layerIndex}&parentLayerIndex=${servicePointModel.parentLayerIndex}">网点找回</a></li>
      <li class="active"><a href="javascript:void(0)">添加网点</a></li>
    </ul>
    <input type="hidden" id="pointModelAreaId" value="${servicePointModel.area.id}">
    <input type="hidden" id="pointModelSubAreaId" value="${servicePointModel.subArea.id}">
    <input type="hidden" id="pointModelProductCategoryId" value="${servicePointModel.productCategoryId}">
    <input type="hidden" id="pointModelAddress" value="${servicePointModel.address}">
    <input type="hidden" id="layerIndex" value="${servicePointModel.layerIndex}">
    <input type="hidden" id="parentLayerIndex" value="${servicePointModel.parentLayerIndex}">
    <input type="hidden" id="pointModelDegree" value="20">
    <sys:message content="${message}"/>
    <div style="width: 80%;margin-left: 10%">
        <form:form id="inputForm" modelAttribute="servicePoint" action="${ctx}/md/servicepoint/saveServicePointForPlan" method="post" class="form-horizontal">
            <form:hidden path="areaIds" />
            <form:hidden path="productIds"/>
            <form:hidden path="productCategoryId"/>
            <form:hidden path="finance.paymentType.value" value="20" />
            <form:hidden path="finance.bankIssue.value" value="0" />
            <input type="hidden" name="subArea.id" value="${servicePoint.area.id}">
            <legend style="margin-top: 10px"><span>网点信息</span></legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red">*</span>编&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</label>
                        <div class="controls">
                            <form:input  path="servicePointNo" cssClass="input-block-level required"></form:input>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red">*</span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称：</label>
                        <div class="controls">
                            <form:input  path="name" cssClass="input-block-level required"></form:input>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red">*</span>手&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机：</label>
                        <div class="controls">
                            <form:input  path="contactInfo1" cssClass="input-block-level required mobile" maxlength="11"></form:input>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red">*</span>师傅姓名：</label>
                        <div class="controls">
                            <form:input  path="primary.name" cssClass="input-block-level required"></form:input>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red">*</span>地&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;址：</label>
                        <div class="controls">
                            <sys:areaselect name="area.id" id="area" value="${servicePoint.area.id}"
                                            labelValue="${servicePoint.area.fullName}" labelName="area.fullName" title=""
                                            mustSelectCounty="true" cssClass="input-block-level required" cssStyle="width:340px"></sys:areaselect>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <form:input  path="subAddress" cssClass="input-block-level required"></form:input>
                </div>
            </div>
            <div class="row-fluid">
                <div class="control-group">
                    <label class="control-label">分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;级：</label>
                    <div class="controls">
                        <input id="degree_10" name = "degree" type="radio" value="10" checked>
                        <label for="degree_10">试用网点</label>&nbsp;&nbsp;
                        <input id="degree_30" name = "degree" type="radio" value="30">
                        <label for="degree_30">返现网点</label>
                    </div>
                </div>
            </div>
            <legend style="margin-top: 20px"><span>结算信息</span></legend>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;display: none" name="bankInfo">*</span>开户银行：</label>
                        <div class="controls">
                            <form:select path="finance.bank.value" cssClass="input-block-level">
                                <form:option value="0" label="请选择"/>
                                <form:options items="${fns:getDictListFromMS('banktype')}" itemLabel="label" itemValue="value" htmlEscape="false"/><%--切换为微服务--%>
                            </form:select>
                            <form:hidden path="finance.bank.label" htmlEscape="false" />
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;display: none" name="bankInfo">*</span>分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;行：</label>
                        <div class="controls">
                            <form:input path="finance.branch" htmlEscape="false" maxlength="50" cssClass="input-block-level"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;display: none" name="bankInfo">*</span>账&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</label>
                        <div class="controls">
                            <form:input path="finance.bankNo" htmlEscape="false" maxlength="50" cssClass="input-block-level"/>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;display: none" name="bankInfo">*</span>开户姓名：</label>
                        <div class="controls">
                            <form:input path="finance.bankOwner" htmlEscape="false" maxlength="50" cssClass="input-block-level"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">身份证号：</label>
                        <div class="controls">
                            <form:input path="bankOwnerIdNo" htmlEscape="false" maxlength="18" cssClass="input-block-level"/>
                        </div>
                    </div>
                </div>
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label">联系电话：</label>
                        <div class="controls">
                            <form:input path="bankOwnerPhone" htmlEscape="false"  maxlength="11" cssClass="input-block-level mobile"/>
                            <span style="color: #999999">(开户预留)</span>
                        </div>
                    </div>
                </div>
            </div>
            <legend style="margin-top: 20px"><span>服务区域</span></legend>
            <div class="row-fluid">
                <div class="span12">
                    <div class="control-group">
                        <div class="controls" style="margin-left: 60px">
                            ${servicePoint.area.fullName}
                        </div>
                        <div class="controls" style="margin-left: 60px;margin-top: 3px">
                            <c:forEach items="${servicePoint.areas}" var="subArea">
                                <div style="float: left;text-align: left;width: 20%;margin-top: 12px">
                                    <input type="checkbox" id="subAreaId_${subArea.id}" name="subAreaId" value="${subArea.id}"/>
                                    <label for="subAreaId_${subArea.id}">${subArea.name}</label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
            <legend style="margin-top: 20px"><span>产品信息</span></legend>
            <div class="row-fluid">
                <div class="span12">
                    <div class="control-group">
                        <div class="controls" style="margin-left: 60px">
                                ${categoryName}
                        </div>
                        <div class="controls" style="margin-left: 60px;margin-top: 3px">
                            <c:forEach items="${productList}" var="product">
                                <div style="float: left;text-align: left;width: 25%;margin-top: 12px">
                                    <input type="checkbox" id="productId_${product.id}" name="productId" value="${product.id}"/>
                                    <label for="productId_${product.id}">${product.name}</label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </form:form>
    </div>
    <div style="height: 60px;width: 100%"></div>
    <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
        <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
        <div style="float: right;margin-top: 10px;margin-right: 20px">
            <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="保 存"/>
            <input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 96px;height: 40px" onclick="cancel()"/>
        </div>
    </div>
  </body>
</html>
