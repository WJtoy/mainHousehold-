<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>师傅管理</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <script src="${ctxStatic}/layui/layui.js"></script>
    <link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
    <script src="${ctxStatic}/jquery-upload-file/js/ajaxfileupload.js"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/layui/css/layui.css">
    <c:set var="currentuser" value="${fns:getUser() }" />
    <style type="text/css">
        .x{
            width: 46%;
            float: left;
            margin-top: 5px;
        }




        .line-row{
            margin-left: -90px;
        }
        .line-address{
            background-color: rgba(246, 246, 246, 1);
            color: rgba(102, 102, 102, 1);
            font-size: 14px;
            font-family: Roboto;
            border: 1px dashed #ccc;
            margin: 0 0 15px 98px;
            padding: 18px;
            line-height: 28px;
            width: 725px;
            border-radius: 4px;
        }
        .hide_input{
            width: 70%;
            border: 0px;
            background: #F6F6F6 100%;
            /*margin-top: 5px;*/
            /*margin-left: -20px;*/
        }
        .prohibit{
            pointer-events: none;
        }
        table{
            width: 780px;
            height: 140px;
        }
        .receipt{
            height: 30px;
        }
        .type2{
            width: 60px;
            text-align: center;
        }
        .type3{
            width: 60px;
            text-align: center;
        }
        #editBtn{
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
        .line_{
            border-bottom: 3.5px solid #0096DA;
            width: 65px;
            border-radius: 10px;
        }
        .table_line{
            /*position: absolute;*/
            /*z-index: 1;*/
            width: 96%;
            height: auto;
            padding: 0px;
            margin: 0px 0px 15px 0px;
        }
        #receiving_info{
            text-align: center;
            margin-left: 80px;
            margin-bottom: 20px;
        }
        #addressInfo{
            width: 600px;
            height: 50px;
            left: 620px;
            top: 567px;
            color: rgba(153, 153, 153, 1);
            font-size: 14px;
            text-align: left;
            font-family: SourceHanSansSC-regular;
            padding: 10px;
        }
        .service_area{
            height: 174px;
            overflow: auto;
            margin-left: 30px;
        }
        .control-group{border-bottom: 0px;margin-left: 20px;}
        .img2{border-radius:4px}
        .upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:80px;height:80px;}
        .upload_warp_left img{margin-top:0px}
        .upload_warp_left {float: left;width: 90px;border-radius: 4px;cursor: pointer;
        }

        .upload_warp_img_div {position: relative;height: 80px;width: 80px;float: left;
            display: table-cell;text-align: center;background-color: #eaeaea;cursor: pointer;
        }
        .upload_warp_img_div .upload_warp_img_div_del{position:absolute;top:0px;width:20px !important;height:20px !important;right:0px;margin-top: 0px !important;
            background-size: 20px 20px !important;border-radius:4px;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; }

        .upload_warp{position: relative;}
        .iuConfig{background-color:#333333;opacity:0.9;color:white !important;margin-top: -17px !important;border-bottom-left-radius: 4px;border-bottom-right-radius: 4px}
    </style>
    <script type="text/javascript">
        var orderdetail_index = parent.layer.getFrameIndex(window.name);
        var tree;
        var areas = [];
        var clickTag = 0;
        $(document).ready(function () {
            var $btnSubmit = $("#btnSubmit");
            // layui.use(['form','element'], function() {
            //     var form = layui.form,
            //         $ = layui.$,
            //         element = layui.element;

                // layui监管收件单选
                // form.on("radio(addressFlag)", function (data) {
                $('input[type=radio][name="engineerAddress.addressFlag"]').change(function (){
                    var addressFlag = $("input[name='engineerAddress.addressFlag']:checked").val();// 1：网点地址 2：个人地址 3：自定义
                    var fullAddress = $("#fullAddress").val();
                    var areaName = $("#areaName").val();
                    var address = $("#address").val();

                    // 自定义
                    if (addressFlag == 3) {
                        $("#engineerAddressButton").removeClass("prohibit");
                        $("#engineerAddress").attr("readOnly",false);
                    }
                    if (addressFlag == 1) {
                        // var serviceFullAddress = $("#serviceFullAddress").val();
                        var serviceFullAddress = $("#subAddress").val();
                        $("#engineerAddressName").attr("value", $("#servicePoint_address_fullName").val());
                        $("#engineerAddressId").attr("value", $("#servicePoint_address_id").val())
                        if (serviceFullAddress != "") {
                            $("#engineerAddress").attr("value", serviceFullAddress);
                        }
                        $("#engineerAddressButton").addClass("prohibit");
                        $("#engineerAddress").attr("readOnly","true");
                    }
                    if (addressFlag == 2) {
                        if (areaName != '' && address != '') {
                            var areaId = $("#areaId").val();
                            $("#engineerAddressName").attr("value", areaName);
                            $("#engineerAddressId").attr("value", areaId);
                            $("#engineerAddress").attr("value", address);
                            $("#engineerAddressButton").addClass("prohibit");
                            $("#engineerAddress").attr("readOnly","true");
                        } else {
                            setTimeout(function(){
                                $("input[type='radio'][name='engineerAddress.addressFlag']:eq(2)").prop("checked",true);
                                $("#engineerAddressButton").removeClass("prohibit");
                                $("#engineerAddress").attr("readOnly",false);
                                // form.render();
                            },1000);
                            layui.use('layer', function() {
                                var layer = layui.layer;
                                layer.alert('您未填写师傅地址', {
                                    icon: 2,
                                    skin: 'layer-ext-moon'
                                })
                                return false;
                            });
                        }
                    }
                });
                // form.render();
            // });
            $("#inputForm").validate({
                rules: {
                    contactInfo: {
                        remote: "${ctx}/md/servicepoint/checkEngineerMobile?id=${engineer.id}"
                        <%--remote: "${ctx}/md/engineer/checkLoginName?expectId=${engineer.id}"--%>
                    }
                },
                messages: {
                    contactInfo: {
                        remote: "该手机号用户已存在，请确认输入是否正确"
                    }
                },
                highlight : function(element) {
                    $(element).closest('.control-group').addClass('has-error');
                },
                success : function(label) {
                    label.closest('.form-group').removeClass('has-error');
                    label.remove();
                },
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $btnSubmit.removeAttr('disabled');
                    if(element.context.name == 'area.fullName' ){
                        element.parent('div').parent('div').append(error);
                    }else if (element.context.name == 'engineerAddressArea.fullName'){
                        element.parent('div').parent('div').append(error);
                    }else{
                        element.parent('div').append(error);
                    }

                },
                submitHandler: function (form) {
                    var engineerAreaId = $("#engineerAddressId").val();
                    var areaId = $("#areaId").val();
                    if (areaId == '' || engineerAreaId == '') {
                        layerError("请选择完整的省、市、区县","错误提示");
                        return false;
                    }

                    if(clickTag == 1){
                        return false;
                    }
                    if($btnSubmit.prop("disabled") == true){
                        return false;
                    }
                    areas = $('input[name="county"]:checked').map(function() {
                        return $(this).val();
                    });
                    if(areas.length == 0){
                        clickTag = 0;
                        layerError("请选择师傅负责的区域","错误提示");
                        return false;
                    }

                    var idNo = $("#idNo").val();
                    if (idNo != null && idNo != '') {
                        if (!IdentityCodeValid(idNo)) {
                            layerInfo("请输入正确的身份证号", "信息提示");
                            $("#btnSubmit").prop("disabled", false);
                            return false;
                        }
                    }

                    $btnSubmit.attr("disabled",true);

                    $("#areas").val(areas.get().join(","));
                    var masterConfirm = $("#masterConfirm").val();
                    if (masterConfirm == "1") {
                        top.layer.confirm('保存后，原主帐号转为子帐号，确认保存师傅信息吗？', {icon: 3, title:'系统确认'}, function(index){
                            top.layer.close(index);//关闭本身
                            submit(form);
                        },function(index){
                            $btnSubmit.removeAttr('disabled');
                        });
                        return false;
                    } else {
                        var area = $("#engineerAddressName").val();
                        $("#addressAreaName").attr("value", area);
                        submit(form);
                    }
                    // $btnSubmit.attr("disabled", "disabled")

                    var str = "";
                    $("input[name='picInfo']").each(function(i,element){
                        var url = this.value;
                        var code = $(this).data("code");
                        if(url!=null && url!=''){
                            str += code+","+url+":";
                        }
                    });
                    $("#attachment").val("");
                    $("#attachment").val(str);
                },
            });

            function submit(form){
                layui.use('layer', function(){
                    var layer = layui.layer;

                clickTag = 1;
                var loadingIndex;

                var options = {
                    url: "${ctx}/md/engineer/saveEngineer",
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
                            }, 2000);
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
            });
            }

            $("input:radio[name='masterFlag']").change(function (){
                //check primary
                var masterFlag = $("input[name='masterFlag']:checked").val();
                if(masterFlag == "0"){
                    $("#masterConfirm").val("0");
                    $("#tip_masterFlag").hide();// 隐藏子账号提示信息
                    // $("#second").show();// 安维
                    // $("#master").show();// 主账号
                    // $("#second_servicePoint").hide();// 网点
                    return false;
                } else {
                    // $("#second").hide();// 安维地址隐藏
                    // $("#master").hide();// 主账号地址隐藏
                    // $("#second_servicePoint").show();// 网点地址显示
                }
                var data ={};
                data.servicePointId = $("#servicePointId").val();
                data.expectType = "engineer";
                <c:choose><c:when test="${engineer.id == null}">data.expectId = 0;</c:when><c:otherwise>data.expectId = ${engineer.id};</c:otherwise></c:choose>
                $.ajax(
                    {
                        cache : false,
                        type : "POST",
                        url : "${ctx}/md/engineer/checkPrimary",
                        data : data,
                        success : function(data)
                        {
                            // 检查网点编号是否有多个主帐号
                            top.$.jBox.closeTip();
                            if (data.success==false)
                            {
                                // $("#second").hide();// 安维地址隐藏
                                // $("#master").hide();// 主账号地址隐藏
                                // $("#second_servicePoint").show();// 网点地址显示
                                $("#tip_masterFlag").show();
                                $("#masterConfirm").val("1");
                            } else
                            {
                                $("#tip_masterFlag").hide();
                                $("#masterConfirm").val("0");
                            }
                        },
                        error : function(xhr, ajaxOptions, thrownError)
                        {
                            top.$.jBox.closeTip();
                            top.$.jBox.error(thrownError.toString());
                        }
                    });//end ajax
            });



        });

        function pointSelect_callback(data){
            $("[id^='servicePoint.servicePointNo']").val(data.servicePointNo);
            if (data.appFlag == 0) {
                $("input[type='radio'][name='appFlag']").attr("disabled", "disabled");
                $("input[type='radio'][name='appFlag']").attr("title", "网点不具有手机接单功能");
            } else {
                $("input[type='radio'][name='appFlag']").removeAttr("disabled");
                $("input[type='radio'][name='appFlag']").removeAttr("title");
            }
            var eid = $("#id").val();
            loadArea(data.id,eid);
        }

        function getCheckedAreas(){
            areas = [];
            var nodes = tree.getCheckedNodes(true);
            for (var i = 0; i < nodes.length; i++) {
                if(nodes[i].level ==3) {
                    var area = {};
                    area.id = nodes[i].id;
                    area.type = nodes[i].level + 1;
                    areas.push(area);
                }
            }
        }

        function refresh(addressId, userName, contactInfo, addressInfo, address){
            var oldAddress = $("#addressInfo").val();
            if (oldAddress != '') {
                addressInfo = addressInfo.replace(address,'');
            }

            var engineerAddressInfo = userName +" "+contactInfo + " 【地址：" + addressInfo +"】";
            $("#addressInfo").val(engineerAddressInfo);
            $("#newAddressId").val(addressId);
        }

        function editAddressInfo(id) {
            if (id == '') {
                id = $("#newAddressId").val();
            }
            var text = "编辑地址";
            var url = "${ctx}/md/engineerAddress/addressForm?id=" + id + "&parentIndex=" + (orderdetail_index || '');
            top.layer.open({
                type: 2,
                id:"engineerAddress",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['820px', '550px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                    // 获取子页面的iframe
                    var iframeWin = top[layero.find('iframe')[0]['name']];
                    var addressFlag = $("#engineerAddressFlag").val();
                    var areaName = $("#areaName").val();// 个人地址
                    var address = $("#address").val();// 个人详细地址
                    var areaId = $("#areaId").val();// 个人地址Id
                    var servicePointAddressId = $("#servicePoint_address_id").val();// 网点地址id
                    var servicePointAddressFullName = $("#servicePoint_address_fullName").val();// 区域全称
                    var serviceFullAddress = $("#serviceFullAddress").val();// 详细地址
                    var subAddress = $("#subAddress").val();
                    var name = $("#name").val();
                    var contactInfo = $("#contactInfo").val();
                    if(iframeWin != null){
                        var json = {
                            addressFlag : addressFlag,
                            areaId : areaId,
                            areaName : areaName,
                            address : address,
                            servicePointAddressId : servicePointAddressId,
                            servicePointAddressFullName : servicePointAddressFullName,
                            serviceFullAddress : serviceFullAddress,
                            engineerId : $("#id").val(),
                            servicePointId : $("#servicePointId").val(),
                            name : name,
                            contactInfo : contactInfo,
                            subAddress : subAddress
                        };
                        iframeWin.child(json);
                    }
                },
                end:function(){
                }
            });
        }


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
            return pass;
        }


        /**
         * 身份证城市代码列表
         */
        var aIdentityCode_City = { // 城市代码列表
            11: "北京", 12: "天津", 13: "河北", 14: "山西", 15: "内蒙古", 21: "辽宁", 22: "吉林",
            23: "黑龙江 ", 31: "上海", 32: "江苏", 33: "浙江", 34: "安徽", 35: "福建", 36: "江西",
            37: "山东", 41: "河南", 42: "湖北 ", 43: "湖南", 44: "广东", 45: "广西", 46: "海南",
            50: "重庆", 51: "四川", 52: "贵州", 53: "云南", 54: "西藏 ", 61: "陕西", 62: "甘肃",
            63: "青海", 64: "宁夏", 65: "新疆", 71: "台湾", 81: "香港", 82: "澳门", 91: "国外 "
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

        function clickFilePry(id){
            $("#upload_file_"+id).click();
            return false;
        }

        // 发生改变触发
        function checkAttachmentPry(index) {
            isExecute = true;
            var filepath = $("#upload_file_"+index).val();
            if(Utils.isEmpty(filepath)){
                $("#upload_file_"+index).val("");
                isExecute = false;
                return false;
            }
            var extStart=filepath.lastIndexOf(".");
            var ext=filepath.substring(extStart,filepath.length).toUpperCase();// 后缀
            if(ext != ".BMP" && ext != ".PNG" && ext != ".GIF" && ext != ".JPG" && ext != ".JPEG"){
                layerInfo("图片类型限于bmp,png,gif,jpeg,jpg格式","系统提示");
                $("#upload_file_"+index).val("");
                isExecute = false;
                return false;
            }
            //check size
            var files = document.getElementById("upload_file_" + index).files;
            var fileSize = files[0].size;
            //var size = fileSize / 1024;
            var size = fileSize.toFixed(2);
            if(size > (2*1024*1024)){
                layerInfo("图片不能大于2M","系统提示");
                $("#upload_file_"+index).val("");
                isExecute = false;
                return false;
            }
            uploadfilePry("upload_file_"+index,index);
        }

        // 上传
        function uploadfilePry(fileInputId,index) {
            $.ajaxFileUpload({
                url: '${pageContext.request.contextPath}/servlet/UploadForMD?type=servicePoint&' + (new Date()).getTime(),
                secureuri: false,
                data: {},
                fileElementId: fileInputId, // file控件id
                dataType: 'json',
                success: function (data, status) {
                    if (data && data.status === 'false'){
                        layerError("文件上传失败，请重试!","错误", true);
                        isExecute = false;
                    } else {
                        var $img = $("[id='viewImg_" + index +"']");
                        $img.attr("src","${ctxUpload}/" + data.fileName);
                        $img.attr("data-original","${ctxUpload}/" +data.fileName);
                        $img.attr("title","点击放大图片");
                        $img.before("<a href='javascript:;' title='点击删除图片'" + " onclick=\"deletePicPry('" + index + "')\" class=\"upload_warp_img_div_del\"></a>");
                        $img.addClass("img2");
                        $("#divImg_" + index).removeClass("drag").removeClass("imgOnDarg");
                        $("#divImg_" + index).removeAttr("onclick");
                        $("#divImg_" + index).addClass("img2");
                        $("#divPicConfig_" + index).addClass("iuConfig");
                        $("#upload_file_"+index).val("");
                        $("#pic_info_"+index).val(data.fileName);
                        /*$("#divPicConfig_"+index).addClass("upload_config");*/
                        imageViewerPry();
                        isExecute = false;
                    }
                },
                error: function (data, status, e) {
                    alert(e);
                }
            });
        }


        // 看大图
        function imageViewerPry(){
            var viewer = $("#divUploadWarpPry").viewer('destroy').viewer(
                {
                    url: "data-original",
                    filter:function(image) {
                        if(image.src.lastIndexOf("/service_insert.png")>0){
                            return false;
                        }

                        if(image.src.lastIndexOf("/outCard.png")>0){
                            return false;
                        }

                        if(image.src.lastIndexOf("/inCard.png")>0){
                            return false;
                        }
                        return true;
                    },
                    viewed: function(image) {
                    },
                    shown:function () {
                        if(this.viewer.index == -1){
                            this.viewer.hide();
                            //$(".viewer-container").removeClass("viewer-in").addClass("viewer-hide");
                        }
                    }
                }
            );
        }



        //删除照片
        function deletePicPry(index){
            event.stopPropagation(); //防止 $("#divImg_" + index) 的函数触发
            $("#divImg_" + index).attr("onclick","clickFilePry('"+index+"');");
            var $img = $("[id='viewImg_" + index +"']");
            if(index == 0){
                $img.attr("src","${ctxStatic}/images/inCard.png");
            }else if(index == 1){
                $img.attr("src","${ctxStatic}/images/outCard.png");
            }else {
                $img.attr("src","${ctxStatic}/images/service_insert.png");

            }
            $img.removeAttr("data-original");
            $img.attr("title","点击上传图片");
            $img.closest(".upload_warp_img_div").addClass("drag");
            $img.prev().remove();
            $("#pic_info_"+index).val("");
            $("#divPicConfig_"+index).removeClass("iuConfig");
            imageViewerPry();
            return false;
        }

    </script>
</head>
<body>
<input type="hidden" id="newAddressId">
<sys:message content="${message}"/>

<form:form id="inputForm" modelAttribute="engineer" action="" method="post" class="form-horizontal" style="margin-top: 15px;height: auto;overflow: hidden;">
    <form:hidden path="id"/>
    <form:hidden path="delFlag"/>
    <form:hidden path="orgMasterFlag" />
    <form:hidden path="grade" />
    <form:hidden path="accountId" />
    <form:hidden path="forTmall"/>
    <form:hidden path="attachment"/>
    <input type="hidden" name="engineerAddress.areaName" id="addressAreaName">
    <%--<input type="hidden" id="engineerAddressId">--%>
    <div>
        <input type="hidden" id="masterConfirm" name = "masterConfirm" value="0" />
        <legend style="margin-left: 10px; width:95%;">师傅信息
            <div style="float:right;margin-top: 4px;">
                    <label>派单
                        <label name="planCount" htmlEscape="false" style="border-style: none;background: #fff;width: 55px;color: #0096DA;">${engineer.planCount}</label>
                    </label>
                    <label>完成
                        <label name="orderCount" htmlEscape="false" style="border-style: none;background: #fff;width: 55px;color: #5AC85D">${engineer.orderCount}</label>
                    </label>
                    <label>违约
                        <label name="breakCount" htmlEscape="false" style="border-style: none;background: #fff;width: 55px;color: #F64165">${engineer.breakCount}</label>
                    </label>
            </div>
            <div class="line_"></div>
        </legend>

        <div class="">
            <div class="line-row">
                <div>
                <div class="control-group x">
                    <label class="control-label" style="margin-left: 5px;"><span class=" red">*</span>网点名称：</label>
                    <div class="controls">
                        <c:choose>
                            <c:when test="${engineer != null && engineer.id != 0 && engineer.servicePoint != null && engineer.servicePoint.id != null}">
                                <form:input id="servicePointName" path="servicePoint.name" readonly="true" htmlEscape="false" class="input-small" style="width: 245px"/>
                                <form:hidden id="servicePointId" path="servicePoint.id" />
                            </c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${currentuser.isSystemUser() or currentuser.isSaleman()}">
                                        <md:pointselectForEngineerAddress id="servicePoint" name="servicePoint.id" value="${engineer.servicePoint.id}" labelName="servicePointNo.name" labelValue="${engineer.servicePoint.name}" address="servicePoint.address"
                                                                          width="1200" height="780" callbackmethod="pointSelect_callback" title="安维网点" areaId="" cssClass="required" addressValue="${engineer.servicePoint.address}" primaryAddress="primaryAddress"  primaryAddressValue="${engineer.address}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <form:input id="servicePointName" path="servicePoint.name" readonly="true" htmlEscape="false" class="input-small" style="width: 245px"/>
                                        <form:hidden id="servicePointId" path="servicePoint.id" />
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="control-group x">
                    <label class="control-label" style="margin-left: 5px;">网点编号：</label>
                    <div class="controls">
                        <form:input path="servicePoint.servicePointNo" readonly="true" htmlEscape="false" class="input-small required" style="width:215px"/>
                    </div>
                </div>
                    <div style="clear:both"></div>
                </div>
                <div>
                <div class="control-group x">
                    <label class="control-label" style="margin-left: 5px;"><span class=" red">*</span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称：</label>
                    <div class="controls">
                        <c:choose>
                            <c:when test="${currentuser.isSystemUser() or currentuser.isSaleman()}">
                                <form:input path="name" htmlEscape="false" maxlength="20" class="required" style="width:245px"/>
                            </c:when>
                            <c:otherwise>
                                <form:input path="name" readonly="${empty engineer.id?'false':'true'}" maxlength="20" htmlEscape="false" class="required" style="width:245px"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="control-group x">
                    <label class="control-label" style="margin-left: 5px;"><span class=" red">*</span>联系电话：</label>
                    <div class="controls">
                        <c:choose>
                            <c:when test="${currentuser.isSystemUser() or currentuser.isSaleman()}">
                                <form:input path="contactInfo" htmlEscape="false" maxlength="11" class="input-y required mobile" style="width:215px"/>
                            </c:when>
                            <c:otherwise>
                                <form:input path="contactInfo" readonly="${empty engineer.id?'false':'true'}" maxlength="11" htmlEscape="false" class="input-y required mobile" style="width:215px"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                    <div style="clear:both"></div>
                </div>

                <div>
                <div class="control-group x">
                    <label class="control-label" style="margin-left: 5px;">Q&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Q：</label>
                    <div class="controls">
                        <form:input path="qq" htmlEscape="false" class="qq" maxlength="11" style="width:245px"/>
                    </div>
                </div>
                <div class="control-group x">
                    <label class="control-label" style="margin-left: 5px;"><span class="red">*</span>等&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;级：</label>
                    <div class="controls">
                        <div style="width:230px">
                            <form:select path="level.value" class="input-small required" style="height: 30px;width: 230px;">&ndash;%&gt;
                            <form:options items="${fns:getDictInclueListFromMS('EngineerLevel','1,2,3,4,5')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
                            </form:select>
                        </div>
                    </div>

                </div>
                    <div style="clear:both"></div>
                </div>

                <div class="row-fluid">
                    <div class="span4" style="width: auto;max-width:440px;margin-top: 5px;">
                        <div class="control-group">
                            <label class="control-label" style="margin-left: 5px;"><span class=" red">*</span>地&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;址：</label>
                            <div class="controls" style="width: auto;max-width:440px" >
                                <sys:areaselect name="area.id" id="area" value="${engineer.area.id}"
                                                labelValue="${engineer.area.fullName}" labelName="area.fullName" title=""
                                                mustSelectCounty="true" cssClass="required">
                                </sys:areaselect>
                            </div>
                        </div>
                    </div>
                    <div class="span7" style="margin-left:5px;width:40%;margin-top: 5px;">
                        <div class="control-group">
                            <div class="controls" style="padding-left:0px;margin-left:0px;display:inherit;">
                                <form:input id="address" path="address" htmlEscape="false" maxlength="100" style="width:400px;" placeholder="详细地址，如XX大厦1层101室"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div>
                <div class="control-group x">
                    <label class="control-label" style="margin-left: 6px;">帐号类型：</label>
                    <div class="controls" style="margin-top: 2px">
                        <%--<form:radiobuttons path="masterFlag" disabled="${currentuser.isEngineer()?'true':'false'}" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>&lt;%&ndash;切换为微服务&ndash;%&gt;--%>
                        <input type="radio" name="masterFlag" value="1"/>主帐号
                        <input type="radio" name="masterFlag" value="0"/>子帐号
                        <div id="tip_masterFlag"  style="display: none;position: fixed;" class="red">该网点已有主帐号，保存时，将该帐号设定为主帐号；原主帐号变更为子帐号</div>
                    </div>
                </div>
                <div class="control-group x">
                    <label class="control-label" style="margin-left: 5px;">手机接单：</label>
                    <div class="controls" style="margin-top: 2px">
                        <c:choose>
                            <c:when test="${currentuser.isSystemUser()}">
                                <form:radiobuttons path="appFlag" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>
                            </c:when>
                            <c:otherwise>
                                <form:radiobuttons path="appFlag" disabled="true" items="${fns:getDictListFromMS('yes_no')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div style="clear:both"></div>
                </div>

                <div class="control-group" >
                        <label class="control-label" style="margin-left: 6px">身份证号：</label>
                        <div class="controls">
                            <form:input path="idNo" htmlEscape="false" maxlength="18"
                                        cssStyle="width: 248px;" placeholder="输入有效身份证号"/>
                            <span id="span_idNo" class="red"></span>
                        </div>
                </div>

                <div class="control-group">
                    <div style="width: 100%">
                        <label class="control-label" style="margin-left: 6px">证件照片：</label>
                        <div class="upload_warp" id="divUploadWarpPry">
                            <c:forEach items="${mdEngineerCerts}" var="picRequirement" varStatus="picIndex">
                                <c:set var="isHasValue" value="true" />
                                <c:if test="${engineer.engineerCerts!=null && fn:length(engineer.engineerCerts) >0}">
                                    <c:forEach items="${engineer.engineerCerts}" var="picItems">
                                        <c:if test="${picRequirement.no == picItems.no && isHasValue}">
                                            <c:set var="isHasValue" value="false" />
                                            <c:choose>
                                                <c:when test="${picIndex.index == 0}">
                                                    <div class="upload_warp_left" style="margin-left: 16px">
                                                        <div class="upload_warp_img_div img2" id="divImg_${picIndex.index}" data-index="${picIndex.index}">
                                                            <a href='javascript:;' title='点击删除图片' onclick="deletePicPry('${picIndex.index}')" class="upload_warp_img_div_del"></a>
                                                            <img title="点击放大图片" id="viewImg_${picIndex.index}" class="img2" data-original="${ctxUpload}/${picItems.picUrl}" src="${ctxUpload}/${picItems.picUrl}"  onclick="imageViewerPry()"/>
                                                            <div  id="divPicConfig_${picIndex.index}" class="iuConfig" style="margin-top: -28px;font-size:12px;color:#808695;">
                                                                 ${picRequirement.picUrl}
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                                    <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}"  value="${picItems.picUrl}" type="hidden">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="upload_warp_left">
                                                        <div class="upload_warp_img_div img2" id="divImg_${picIndex.index}" data-index="${picIndex.index}">
                                                            <a href='javascript:;' title='点击删除图片' onclick="deletePicPry('${picIndex.index}')" class="upload_warp_img_div_del"></a>
                                                            <img title="点击放大图片" id="viewImg_${picIndex.index}" class="img2" data-original="${ctxUpload}/${picItems.picUrl}" src="${ctxUpload}/${picItems.picUrl}" onclick="imageViewerPry()"/>
                                                            <div  id="divPicConfig_${picIndex.index}" class="iuConfig" style="margin-top: -28px;font-size:12px;color:#808695;">
                                                                    ${picRequirement.picUrl}
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                                    <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}"  value="${picItems.picUrl}" type="hidden">
                                                </c:otherwise>
                                            </c:choose>

                                        </c:if>
                                    </c:forEach>
                                </c:if>
                                <c:if test="${isHasValue}">
                                    <c:choose>
                                        <c:when test="${picIndex.index == 0}">
                                            <div class="upload_warp_left" style="margin-left: 15px">
                                                <div class="upload_warp_img_div drag img2" id="divImg_${picIndex.index}" onclick="clickFilePry('${picIndex.index}')" data-index="${picIndex.index}">
                                                    <img title="点击上传图片" id="viewImg_${picIndex.index}" class="img2" src="${ctxStatic}/images/inCard.png" />
                                                    <div id="divPicConfig_${picIndex.index}" class="config" style="margin-top: -28px;font-size:12px;color:#808695;">
                                                            ${picRequirement.picUrl}
                                                    </div>
                                                </div>
                                            </div>
                                            <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                            <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}" value="" type="hidden">
                                        </c:when>
                                        <c:when test="${picIndex.index == 1}">
                                            <div class="upload_warp_left">
                                                <div class="upload_warp_img_div drag img2" id="divImg_${picIndex.index}" onclick="clickFilePry('${picIndex.index}')" data-index="${picIndex.index}">
                                                    <img title="点击上传图片"  id="viewImg_${picIndex.index}"  class="img2" src="${ctxStatic}/images/outCard.png" />
                                                    <div id="divPicConfig_${picIndex.index}" class="config" style="margin-top: -28px;font-size:12px;color:#808695;">
                                                            ${picRequirement.picUrl}
                                                    </div>
                                                </div>
                                            </div>
                                            <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                            <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}" value="" type="hidden">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="upload_warp_left">
                                                <div class="upload_warp_img_div drag img2" id="divImg_${picIndex.index}" onclick="clickFilePry('${picIndex.index}')" data-index="${picIndex.index}">
                                                    <img title="点击上传图片" id="viewImg_${picIndex.index}" class="img2"  src="${ctxStatic}/images/service_insert.png" />
                                                    <div id="divPicConfig_${picIndex.index}" class="config" style="margin-top: -28px;font-size:12px;color:#808695;" >
                                                            ${picRequirement.picUrl}
                                                    </div>
                                                </div>
                                            </div>
                                            <input id="upload_file_${picIndex.index}" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="checkAttachmentPry('${picIndex.index}')">
                                            <input id="pic_info_${picIndex.index}" name="picInfo" data-code="${picRequirement.no}" value="" type="hidden">
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </div>


<%--                <div class="control-group">--%>
<%--                    <label class="control-label" style="margin-top: 10px">工单信息：</label>--%>
<%--                    <div class="controls" style="margin-top: -30px;background-color:#F8F8F9;height:40px;width:654px;float:left;">--%>
<%--                        <div style="margin-top:10px;margin-left:10px">--%>
<%--                         <label>派单--%>
<%--                         <label name="planCount" htmlEscape="false" style="border-style: none;width: 55px;color: #0096DA;">${engineer.planCount}</label>--%>
<%--                         </label>--%>
<%--                         <label>完成--%>
<%--                         <label name="orderCount" htmlEscape="false" style="border-style: none;width: 55px;color: #5AC85D">${engineer.orderCount}</label>--%>
<%--                         </label>--%>
<%--                         <label>违约--%>
<%--                         <label name="breakCount" htmlEscape="false" style="border-style: none;width: 55px;color: #F64165">${engineer.breakCount}</label>--%>
<%--                         </label>--%>
<%--                        </div>--%>
<%--                    </div>--%>
<%--                </div>--%>


            </div>
        </div>

        <%--<div class="layui-form" style="margin-top: 15px;">--%>
        <div style="margin-top: 15px;">
            <input style="display: none" id="primary_address_id" value="">
            <input style="display: none" id="primary_address_fullName" value="">
            <input style="display: none" id="anwei_address_id" value="">

            <input style="display: none" id="servicePoint_address_id" value="">
            <input style="display: none" id="servicePoint_address_fullName" value="">
            <input style="display: none" id="serviceFullAddress" value="">
            <input style="display: none" id="fullAddress" value="">

            <input type="hidden" id="subAddress" value="">
            <div class="sj-content" style="margin-top: 17px;">
                <legend style="margin-left: 10px;width: 95%">收件信息<div class="line_"></div></legend>

                <div class="line-row" style="margin-left: -70px;">
                    <c:choose>
<%--                        <c:when test="${engineer != null && engineer.id != 0 && engineer.servicePoint != null && engineer.servicePoint.id != null}">--%>
                        <c:when test="${engineer != null && not empty engineer.id}">
                            <div id="receiving_info">
                                <span style="display: block;float: left;margin-left: 20px;">收件信息：</span>
                                <textarea style="width: 590px;height: 55px;margin-left: -40px;" readonly="readonly" id="addressInfo"></textarea>
                                <input type="button" class="btn btn-primary" value="修改" style="margin-left: 5px;margin-top: -46px;width: 64px;height: 32px;background: #0096DA 100%;border-radius: 4px;" onclick="editAddressInfo('${engineer.engineerAddress.id}')">
                            </div>
                        </c:when>
                        <c:otherwise>
                        <div class="line-address">
                                <%--<div id="second_servicePoint" class="receipt" style="display: none">--%>
                            <div id="second_servicePoint" class="receipt">
                                <input type="radio" name="engineerAddress.addressFlag" value="1" title="网点地址">网点地址
                                    <%--<input style="display: none" value="" id="servicePoint_address" class="hide_input">--%>
                                <input value="" id="servicePoint_address" class="hide_input" style="text-overflow: ellipsis;">
                            </div>
                            <div id="second" class="receipt">
                                <input type="radio" name="engineerAddress.addressFlag" value="2" title="个人地址">个人地址
                                    <%--<input style="display: none" value="" id="anwei_address" class="hide_input">--%>
                                <input value="" id="anwei_address" class="hide_input" style="text-overflow: ellipsis;">
                            </div>

                            <div class="receipt">
                                <input type="radio" name="engineerAddress.addressFlag" value="3" title="自定义">自定义
                            </div>
                        </div>

                        <div>
                        <div class="control-group x" style="margin-left: 6px">
                            <label class="control-label" style="margin-left: 6px;"><span class=" red">*</span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称：</label>
                            <div class="controls">
                                <c:choose>
                                    <c:when test="${currentuser.isSystemUser() or currentuser.isSaleman()}">
                                        <form:input id="e_userName" path="engineerAddress.userName" htmlEscape="false" maxlength="20" class="required" style="width:245px"/>
                                    </c:when>
                                    <c:otherwise>
                                        <form:input id="e_userName" path="engineerAddress.userName" readonly="${empty engineer.id?'false':'true'}" maxlength="20" htmlEscape="false" class="required"/>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="control-group x" style="margin-left: 25px;">
                            <label class="control-label"><span class=" red">*</span>联系电话：</label>
                            <div class="controls">
                                <c:choose>
                                    <c:when test="${currentuser.isSystemUser() or currentuser.isSaleman()}">
                                        <form:input id="e_contactInfo" path="engineerAddress.contactInfo" htmlEscape="false" maxlength="11" class="input-y required mobile" style="width:215px"/>
                                    </c:when>
                                    <c:otherwise>
                                        <form:input id="e_contactInfo" path="engineerAddress.contactInfo" readonly="${empty engineer.id?'false':'true'}" maxlength="11" htmlEscape="false" class="input-y required mobile" style="width:215px"/>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                            <div style="clear:both"></div>
                        </div>

                        <div class="row-fluid" style="margin-left: -4px">
                            <div class="span4" style="width: auto;max-width:440px">
                                <div class="control-group" style="margin-left: 10px;">
                                    <label class="control-label" style="margin-left: 6px"><span class=" red">*</span>收件地址：</label>
                                    <div class="controls" style="width: auto;max-width:440px" >
                                        <sys:areaselect name="engineerAddress.areaId" id="engineerAddress" value="${engineer.engineerAddress.areaId}"
                                                        labelValue="${engineerAddressArea.fullName}" labelName="engineerAddressArea.fullName" title=""
                                                        mustSelectCounty="true" cssClass="required">
                                        </sys:areaselect>
                                    </div>
                                </div>
                            </div>
                            <div class="span7" style="margin-left:5px;width:40%">
                                <div class="control-group">
                                    <div class="controls" style="padding-left:0px;margin-left:-14px;display:inherit;">
                                        <form:input path="engineerAddress.address" id="engineerAddress" htmlEscape="false" maxlength="100" style="width:400px;" placeholder="详细地址，如XX大厦1层101室"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                        </c:otherwise>
                    </c:choose>



            </div>

            <div style="margin-top: 10px;">
                <legend style="margin-left: 10px;width: 95%">服务区域<div class="line_"></div></legend>
                <div class="service_area">
                    <table class="layui-table table_line">
                        <tbody id="areasTable"></tbody>
                    </table>
                </div>

                <input type="hidden" id="areas" name="areas" />


            </div>

            <div id="editBtn">
                <shiro:hasPermission name="md:engineer:edit">
                    <input id="btnSubmit" class="btn btn-primary" type="button" lay-submit lay-filter="formSave" value="保 存" onclick="$('#inputForm').submit()" style="margin-left: 485px;margin-top: 10px;width: 85px;height: 37px;background: #0096DA;border-radius: 4px;"/>&nbsp;</shiro:hasPermission>
                <input id="btnCancel" class="btn" type="button" value="取 消" style="margin-top:10px;width: 85px;height: 37px;border-radius: 4px;"onclick="cancel()"/>
            </div>

        </div>
    </div>

</form:form>
<%--<input type="button" style="display: none" id="reload" onclick="refresh()">--%>
<input id="engineerAddressFlag" type="hidden" value="">
<script class="removedscript" type="text/javascript">
    var this_index = top.layer.index;
    // 区域
    var tree;

    // 装载区域
    function loadArea(sid,eid){
        var loadingIndex = top.layer.msg('正在装载区域，请稍等...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });
        var content = [];
        var ids;
        var areaIds = [];
        //ajax获取数据源后存入content数据中。
        $.ajax({
            cache : false,
            type : "GET",
            url : "${ctx}/md/engineer/loadEngineerAreas",
            data : {sid:sid,eid:eid},
            success : function(data)
            {
                top.layer.close(loadingIndex);
                if (data.success==false)
                {
                    layerError(data.message,"错误提示");
                    return false;
                } else
                {
                    ids = data.data.areaIds || [];
                    areaIds = ids;

                    $("#areasTable").html("");
                    // 不采用树结构
                    content = data.data.serviceAreas;
                    var city = content.filter(e => e.type==3);// 市

                    // var countyTd = $("<td style='width: 75%;line-height: 25px;'></td>");
                    var tr = $("<tr></tr>");
                    var newTr;
                    var td = "<td style='width: 75%;line-height: 25px;'></td>";
                    var countyTd = $(td).get(0);
                    var count = 0;

                    var countyTR;

                    var parentArr = [];
                    var newCountyTd;

                        $.each(content, function(index, item){
                            if (item.type != 1) {
                                var td = "";
                                if (item.type == 2) {
                                    // 单省列
                                    td = "<td class='type"+item.type+"' rowspan='"+city.length+"'>" + item.name + "</td>";
                                } else {
                                    // 多市多区列
                                    td = "<td class='type"+item.type+"'>" + item.name + "</td>";
                                }
                                // 市
                                if (item.type == 3) {
                                    // 装在市列到tr
                                    if (city.length > 1) {
                                        // 除开第一个市 其余都新创建tr来装载市列
                                        if (count == 0) {
                                            tr.attr("id", item.id);
                                            tr.append(td);
                                            count++;
                                        } else {
                                            newTr = $("<tr id='"+item.id+"'></tr>");
                                            newTr.append(td);
                                        }
                                    } else {
                                        tr.attr("id", item.id);
                                        tr.append(td);
                                    }
                                } else {
                                    // 区
                                    if (item.type == 4) {
                                        var divInput = "<div style='width: 98px;float: left;'><input type='checkbox' value='"+item.id+"' name='county'/>"+item.name+"</div>";
                                        var element= $(divInput).get(0);
                                        if (parentArr.length == 0) {
                                            parentArr.push(item.pId);
                                        }
                                        if (parentArr.indexOf(item.pId) == -1) {// 数组下标不存在新创建列
                                            var td =  "<td style='width: 75%;line-height: 25px;'></td>";
                                            newCountyTd = $(td).get(0);
                                            newCountyTd.append(element);
                                            parentArr.push(item.pId);
                                        } else {
                                            countyTd.append(element);
                                            parentArr.push(item.pId);
                                        }

                                        // 匹配区县的上级id
                                        countyTR = document.getElementById(item.pId);
                                        if (countyTR != null) {
                                            if (newCountyTd != undefined) {
                                                countyTR.append(newCountyTd);
                                                countyTd = newCountyTd;
                                            } else {
                                                countyTR.append(countyTd);
                                            }
                                        } else {
                                            tr.append(countyTd);
                                        }
                                    // 省或市
                                    } else {
                                        tr.append(td);
                                    }
                                }
                                if (newTr != undefined && newTr.children().length>0) {
                                    if (item.type==4) {
                                        $("#areasTable").append(countyTR);
                                    } else {
                                        $("#areasTable").append(newTr);
                                    }
                                } else {
                                    $("#areasTable").append(tr);
                                }
                            }
                            if (newTr != undefined && newTr.children().length>0) {
                                $("#areasTable").append(newTr);
                            } else {
                                $("#areasTable").append(tr);
                            }
                        });
                        eachData(areaIds);
                        // form.render();
                    // });
                }
            },
            error : function(xhr, ajaxOptions, thrownError)
            {
                top.layer.close(loadingIndex);
                layerError(thrownError.toString(),"错误提示");
            }
        });//end ajax
    }

    // 处理服务区域数据
    function eachData(data) {
        if (data.length > 0) {
            for(var i in data){
                var id = data[i];
                $(":checkbox[name='county'][value="+id+"]").attr("checked","checked");
            }
        }
    }

    // 前版本处理树结构
    function expandNodes(plus){
        if (!tree){return false;}
        if(plus=='+'){
            tree.expandAll(true);
        }else if(plus == '-'){
            tree.expandAll(false);
        }
        return false;
    }

    // 关闭页面
    function cancel() {
        top.layer.close(this_index);// 关闭本身
        var pframe = getActiveTabIframe();//定义在jeesite.min.js中
        if(pframe){
            pframe.repage();
        }
    }

    // 初始化
    $(document).ready(function() {
        onFunction();
        var masterFlag = ${engineer.masterFlag};

        if (${engineer != null}) {
            $("input[type='radio'][name='masterFlag'][value="+masterFlag+"]").attr("checked","checked");
        } else {
            $("input[type='radio'][name='masterFlag'][value='0']").attr("checked","checked");
        }

        <c:choose>
            <c:when test="${engineer != null && engineer.id != 0 && engineer.servicePoint != null && engineer.servicePoint.id != null}">
                loadArea('${engineer.servicePoint.id}','${engineer.id==null?0:engineer.id}');

                if (${engineer.engineerAddress.id != null}) {
                    $("#engineerAddressId").val(${engineer.engineerAddress.id});

                    var userName = '${engineer.engineerAddress.userName}';
                    var contactInfo = '${engineer.engineerAddress.contactInfo}';
                    var addressInfo = '${engineer.engineerAddress.address}';
                    var engineerAddressInfo = userName +" "+contactInfo + " 【地址：" + addressInfo +"】";
                    $("#addressInfo").val(engineerAddressInfo);
                }

                // 设置网点的区域id
                $("#servicePoint_address_id").attr("value", "${engineer.servicePoint.area.id}");
                // 设置网点的详细地址
                $("#serviceFullAddress").attr("value", "${engineer.servicePoint.address}");
                // 设置网点区域全称
                $("#servicePoint_address_fullName").attr("value", "${engineer.servicePoint.area.fullName}");

                $("#subAddress").attr("value", "${engineer.servicePoint.subAddress}");
            </c:when>
            <c:otherwise>
                $("input[type='radio'][name='engineerAddress.addressFlag']:eq(2)").attr("checked","checked");// 自定义
            </c:otherwise>
        </c:choose>

        if ($("#servicePointId").val() == "") {
            $("input[type='radio'][name='masterFlag']").attr("disabled", true);
        }
        if ($("#servicePointId").val() == "") {
            $("input[type='radio'][name='engineerAddress.addressFlag']").attr("disabled", true);
        }
    });

    <c:if test ="${not empty engineer.servicePoint.id}">
        $("#servicePoint_address").attr("value", "【${engineer.servicePoint.address}】");
        // 设置网点的详细地址
        $("#serviceFullAddress").attr("value", "${engineer.servicePoint.address}");
        // 设置网点的区域id
        $("#servicePoint_address_id").attr("value", "${engineer.servicePoint.area.id}");
        // 设置网点区域全称
        $("#servicePoint_address_fullName").attr("value", "${engineer.servicePoint.area.fullName}");
        $("#subAddress").attr("value", "${engineer.servicePoint.subAddress}");
        // 显示网点单选框后的input
        $("#servicePoint_address").show();

        <c:choose>
            <c:when test="${engineer.servicePoint.appFlag eq 0}">
                $("input[type='radio'][name='appFlag']").attr("disabled", "disabled");
                $("input[type='radio'][name='appFlag']").attr("title", "网点不具有手机接单功能");
            </c:when>
            <c:otherwise>
                $("input[type='radio'][name='appFlag']").removeAttr("disabled");
                $("input[type='radio'][name='appFlag']").removeAttr("title");
            </c:otherwise>
        </c:choose>
    </c:if>

    function onFunction(){
        // 安维地址监听
        $("#address").on("keyup", function() {
            var area = $("#areaName").val();
            if (area != "") {
                var anweiAddress = "【" + area + (this.value) + "】";
                $("#anwei_address").attr("value", anweiAddress)
                $("#anwei_address").show();
            }
        });
        $("#address").on("blur", function() {
            var area = $("#areaName").val();
            if (area != "") {
                var anweiAddress = "【" + area + (this.value) + "】";
                $("#anwei_address").attr("value", anweiAddress)
                $("#anwei_address").show();
            }
        });
    }

</script>
</body>
</html>