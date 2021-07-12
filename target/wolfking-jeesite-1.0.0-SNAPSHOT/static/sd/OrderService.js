/**
 * 订单上门服务处理
 * 依赖layer弹窗
 */
var OrderService = {
    init:function(){
        this.productId = "";
        this.data={};
        this.rootUrl="";
        this.version = "1.0";
        this.actionCodes = {};//map
        //服务类型
        $(document).on("change", "input[name='serviceCategory.value']", function() {
            var serviceCategoryId = $(this).val();
            var serviceCategoryName = $("label[for='" + $(this).attr("id") + "']").text();
            OrderService.changeServiceCategory(serviceCategoryId,serviceCategoryName);
        });
        //选择故障类型
        $(document).on("change", "select[id='errorType.id']", function() {
            var optionSelected = $(this).find("option:selected");
            $("input[id='errorType.name']").val(optionSelected.text());
            OrderService.loadErrorCodes();
        });
        //选择故障代码
        $(document).on("change", "select[id='errorCode.id']", function() {
            var optionSelected = $(this).find("option:selected");
            $("input[id='errorCode.name']").val(optionSelected.text());
            OrderService.loadActionCodes();
        });
        //选择故障代码
        $(document).on("change", "select[id='actionCode.id']", function() {
            //clear
            var $_serviceType = $("select[id='serviceType.id']");
            //v1
            //$_serviceType.empty();
            //end v1
            $("[id='s2id_serviceType.id']").find(".select2-chosen").html("请选择");
            $("input[id='actionCode.name']").val("");

            var actionId = $(this).val();
            if(!actionId || actionId === "" || actionId === "0"){
                return false;
            }
            var actionCode = OrderService.actionCodes[actionId];
            if(actionCode){
                //v1
                // var option = document.createElement("option");
                // option.text =  actionCode.serviceTypeName;
                // option.value =  actionCode.serviceTypeId;
                // $_serviceType[0].options.add(option);
                // $_serviceType.val(option.value);
                // $("[id='s2id_serviceType.id']").find(".select2-chosen").html(option.text);
                // $("[id='serviceType.name']").val(option.text);
                // $("input[id='actionCode.name']").val(actionCode.name);
                //end v1
                //v2
                $("[id='s2id_serviceType.id']").find(".select2-chosen").html(actionCode.serviceTypeName);
                $("[id='serviceType.id']").val(actionCode.serviceTypeId);
                $("[id='serviceType.name']").val(actionCode.serviceTypeName);
                $("input[id='actionCode.name']").val(actionCode.serviceTypeName);
                //end v2
            }
        });

        //应付配件费变更时，自动同步到应收
        $(document).on("change","#engineerMaterialCharge",function(){
            $("#materialCharge").val($("#engineerMaterialCharge").val());
        });

        $(document).on("change","select[id='serviceType.id']",function(){
            $("[id='serviceType.name']").val(this.selectedOptions[0].text);
        });

        $(document).on("blur","#engineerOtherCharge",function(){
            checkRemoteChargeLimit();
        });

        $(document).on("blur","#engineerTravelCharge",function(){
            checkRemoteChargeLimit();
        });

        function checkRemoteChargeLimit(){
            var strLimitRemoteCharge = $("#limitRemoteCharge").val();
            if(strLimitRemoteCharge === "null" || strLimitRemoteCharge === "undefined"){
                return;
            }
            var limitRemoteCharge = parseFloat(strLimitRemoteCharge);
            if(limitRemoteCharge < 0){
                limitRemoteCharge = 0;
            }
            var areaRemoteFee = $("#areaRemoteFee").val();
            if(areaRemoteFee !== '1'){
                return;
            }
            var otherCharge = parseFloat($("#engineerOtherCharge").val());
            var travelCharge = parseFloat($("#engineerTravelCharge").val());
            var totalCharge = otherCharge + travelCharge;
            if(totalCharge > limitRemoteCharge){
                $("btnSubmit").attr('disabled', 'disabled');
                layerAlert("远程费用和其他费用合计已超过" + limitRemoteCharge + "元，不允许添加上门服务!<br/>请确认是否操作退单!");
            }else{
                $("btnSubmit").removeAttr('disabled');
                //应收归零
                $("#otherCharge").val("0");
                $("#travelCharge").val("0");
            }
        }
    },
    // 读取服务项目
    loadServiceTypes:function(serviceCategoryId,orderTypeName){
        var self = this;
        var $_serviceType = $("select[id='serviceType.id']");
        $_serviceType.empty();
        $_serviceType.append($("<option>").val("0").text("请选择"));
        $_serviceType.val("0");
        $("[id='s2id_serviceType.id']").find(".select2-chosen").html("请选择");
        if(serviceCategoryId){
            $.ajax({
                type: "GET",
                url: self.rootUrl+"/md/servicetype/ofOrderType?orderType=" + (serviceCategoryId || '0') +"&orderTypeName=" + (orderTypeName || ''),
                data:"",
                async: false,
                success: function (data) {
                    if (data.success){
                        var option;
                        $.each(data.data, function(i, item) {
                            $_serviceType.append($("<option data-relateflag='" + (item.relateErrorTypeFlag || '0') + "'>").val(item.id).text(item.name));
                        });
                        if(data.data.length === 1){
                            $("#select[id='serviceType.id'] option:nth-child(1)").attr("selected","selected");
                        }
                    }
                    else{
                        layerError("装载服务项目失败，请重试","错误",true);
                    }
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    layerError("装载服务项目失败，请重试","错误",true);
                }
            });
        }
    },
    // 变更服务类型
    changeServiceCategory:function(serviceCategoryId,serviceCategoryName){
        //清空服务项目
        //OrderService.loadServiceTypes(undefined,undefined);
        OrderService.loadServiceTypes(serviceCategoryId,serviceCategoryName);
        $("#orderServiceType").val(serviceCategoryId);
        //控制显示
        $("tr.install").show();
        $("tr.tr_repair").hide();
        $("#lbl_repair_other").hide();
        $("#lbl_install_other").show();
        // install
        if(serviceCategoryId === "2"){
            $("#lbl_repair_other").show();
            $("#lbl_install_other").hide();
            //故障分类
            OrderService.loadErrorTypes();
        }else{
            OrderService.resetErrorContorls();
            OrderService.loadServiceTypes(serviceCategoryId,serviceCategoryName);
        }
    },
    //装载故障类型
    loadErrorTypes:function () {
        var self = this;
        //clear
        var $_errorType = $("select[id='errorType.id']");
        OrderService.resetErrorContorls();

        var pid = $("#productId").val();
        if(!pid || pid === "" || pid === "0"){
            return false;
        }
        //2019-09-21 增加参数：客户ID
        var cid = $("#customerId").val();
        if(!cid || cid === "" || cid === "0"){
            return false;
        }
        //已有故障类型且产品不变更
        if($_errorType[0].options.length > 1 && pid === self.productId){
            return false;
        }

        self.productId = pid;
        //get data
        $.ajax(
            {
                url : self.rootUrl+"/provider/md/errorType/ajax/findListByProductId",
                type : "GET",
                data : {productId: pid, customerId: cid},
                contentType : "application/json",
                success : function(data)
                {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data.success == false){
                        layerError(data.message,"错误提示");
                        return;
                    }

                    if(!data.data || data.data.length === 0){
                        //未维护维修故障，可直接选择服务类型
                        OrderService.showOrHideRepairCode('0');
                        // layerAlert("选择的产品未维护故障类型。","系统提示");
                        return;
                    }
                    /*缓存中已添加的服务项*/
                    //控制显示
                    OrderService.showOrHideRepairCode('1');

                    var option;
                    option = document.createElement("option");
                    option.text =  "请选择";
                    option.value =  "0";
                    $_errorType[0].options.add(option);
                    $.each(data.data, function(i, item) {
                        option = document.createElement("option");
                        option.text =  item.name;
                        option.value =  item.id;
                        $_errorType[0].options.add(option);
                    });
                    // 默认显示第一个故障分类
                    // $("select[id='errorType.id'] option:nth-child(1)").attr("selected","selected");
                    //$_errorType.trigger("change");
                },
                error : function(e)
                {
                    ajaxLogout(e.responseText,null,"装载产品的故障分类失败，请重试!");
                }
            });
        return false;
    },
    // 控制维修故障等控件显示或隐藏
    showOrHideRepairCode:function(hasRepairCode,isLoadServieTypes){
        if(!hasRepairCode){
            hasRepairCode = '0';
        }
        if(!isLoadServieTypes && isLoadServieTypes !== false){
            isLoadServieTypes = true;
        }
        $("#lbl_repair_other").show();
        $("#lbl_install_other").hide();
        $("#hasErrorType").val(hasRepairCode);
        if(hasRepairCode === '0'){
            $("tr.install").show();
            $("tr.tr_repair").hide();
            // var $_serviceCategory = $("input[name='serviceCategory.value']:checked");
            // var serviceCategoryId = $_serviceCategory.val();
            //v1
            // if(isLoadServieTypes === true){
            //     OrderService.loadServiceTypes(serviceCategoryId,$_serviceCategory.text());
            // }
            //end v1
        }else{
            $("tr.install").hide();
            $("tr.tr_repair").show();
        }
    },
    //装载故障现象
    loadErrorCodes:function () {
        var self = this;
        //clear
        var $_errorCode = $("select[id='errorCode.id']");
        $_errorCode.empty();
        $("[id='s2id_errorCode.id']").find(".select2-chosen").html("请选择");
        var $_actionCode = $("select[id='actionCode.id']");
        $_actionCode.empty();
        $("[id='s2id_actionCode.id']").find(".select2-chosen").html("请选择");
        //2019-09-21 增加参数：客户ID
        var cid = $("#customerId").val();
        if(!cid || cid === "" || cid === "0"){
            return false;
        }
        var errorTypeId = $("select[id='errorType.id']").val();
        if(!errorTypeId || errorTypeId === "" || errorTypeId === "0"){
            return false;
        }
        var pid = $("#productId").val();
        if(!pid || pid === "" || pid === "0"){
            return false;
        }

        //get data
        $.ajax(
            {
                url : self.rootUrl+"/provider/md/errorCode/ajax/findListByProductIdAndErrorTypeId",
                type : "GET",
                data : {productId: pid, customerId: cid, errorTypeId:errorTypeId},
                contentType : "application/json",
                success : function(data)
                {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data.success == false){
                        layerError(data.message,"错误提示");
                        return;
                    }

                    if(!data.data || data.data.length === 0){
                        var errorTypeName = $("[id='errorType.id'] option:selected").text();
                        layerAlert("故障类型:" + (errorTypeName ||'') + "未维护故障现象。","系统提示");
                        return;
                    }
                    /*缓存中已添加的服务项*/
                    var option;
                    option = document.createElement("option");
                    option.text =  "请选择";
                    option.value =  "0";
                    $_errorCode[0].options.add(option);
                    $.each(data.data, function(i, item) {
                        option = document.createElement("option");
                        option.text =  item.name;
                        option.value =  item.id;
                        $_errorCode[0].options.add(option);
                        // $_errorCode.append($("<option>").val(item.id).text(item.name));
                    });
                    $("select[id='errorCode.id'] option:nth-child(1)").attr("selected","selected");
                    var optionSelected = $("select[id='errorCode.id']").find("option:selected");
                    if(optionSelected){
                        $("input[id='errorCode.name']").val(optionSelected.text());
                    }else{
                        $("input[id='errorCode.name']").val("");
                    }
                },
                error : function(e)
                {
                    ajaxLogout(e.responseText,null,"装载故障现象失败，请重试!");
                }
            });
        return false;
    },
    //装载故障处理
    loadActionCodes:function () {
        var self = this;
        //clear
        var $_actionCode = $("select[id='actionCode.id']");
        $_actionCode.empty();
        $_actionCode.append($("<option>").val(0).text("请选择").selected(true));
        $_actionCode.val("0");
        $("[id='s2id_actionCode.id']").find(".select2-chosen").html("请选择");
        var $_errorCode = $("select[id='errorCode.id']");
        var errorCodeId = $_errorCode.val();
        if(!errorCodeId || errorCodeId === "" || errorCodeId === "0"){
            return false;
        }
        var pid = $("#productId").val();
        if(!pid || pid === "" || pid === "0"){
            return false;
        }
        //2019-09-21 增加参数：客户ID
        var cid = $("#customerId").val();
        if(!cid || cid === "" || cid === "0"){
            return false;
        }
        OrderService.actionCodes = {};
        //get data
        $.ajax(
            {
                url : self.rootUrl+"/provider/md/errorAction/ajax/findListByProductAndEC",
                type : "GET",
                data : {productId: pid , customerId: cid, errorCodeId:errorCodeId},
                contentType : "application/json",
                success : function(data)
                {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data.success === false){
                        layerError(data.message,"错误提示");
                        return;
                    }
                    //无故障处理，手动输入
                    if(!data.data || data.data.length === 0){
                        $_actionCode.hide();
                        $("[id='s2id_actionCode.id']").find(".select2-chosen").html("请选择");
                        $("[id='s2id_actionCode.id']").hide();
                        var $_actionCodeInput = $("[id='actionCode.name']");
                        $_actionCodeInput.val("");
                        $_actionCodeInput.show();
                        //v1
                        // //根据工单类型装载服务项目
                        // var $_serviceCategory = $("input[name='serviceCategory.value']:checked");
                        // var serviceCategoryId = $_serviceCategory.val();
                        // OrderService.loadServiceTypes(serviceCategoryId,$_serviceCategory.text());
                        //end
                        return false;
                    }
                    $_actionCode.show();
                    $("[id='s2id_actionCode.id']").show();
                    $("[id='actionCode.name']").hide();
                    // 已维护故障处理
                    var actionCodesMap = arrayToMap(data.data, function (item) {
                        return item.id;
                    });
                    //根据选择的故障处理，显示服务项目
                    OrderService.actionCodes = actionCodesMap;
                    $.each(data.data, function(i, item) {
                        option = document.createElement("option");
                        option.text =  item.name;
                        option.value =  item.id;
                        $_actionCode[0].options.add(option);
                        // $_actionCode.append($("<option>").val(item.id).text(item.name));
                    });
                    $("select[id='actionCode.id'] option:nth-child(1)").attr("selected","selected");
                    var optionSelected = $("select[id='actionCode.id']").find("option:selected");
                    if(optionSelected){
                        $("input[id='actionCode.name']").val(optionSelected.text());
                    }else{
                        $("input[id='actionCode.name']").val("");
                    }
                },
                error : function(e)
                {
                    ajaxLogout(e.responseText,null,"装载故障现象失败，请重试!");
                }
            });
        return false;
    },
    //提交前检查输入
    checkSubmitInput:function(){
        //1.根据服务类型检查输入
        var orderServiceType = $("input[name='serviceCategory.value']:checked").val();
        // 服务项目
        var $_serviceType = $("select[id='serviceType.id']");

        //故障输入
        var hasErrorType = $("#hasErrorType").val();
        if(orderServiceType === "2" && hasErrorType === "1") {
            var checkInputErrorTypeResult = true;
            var relateFlag = "0";
            if($_serviceType.val() === "0" || $_serviceType.val() === ""){
                relateFlag = "0"
            }else{
                var selOption = $("select[id='serviceType.id'] option:selected");
                relateFlag = selOption.data("relateflag");
            }
            // 服务项目设定要关联故障分类，需检查故障分类
            if(relateFlag && (relateFlag === 1 || relateFlag === "1")) {
                checkInputErrorTypeResult = OrderService.checkInputErrorType(1);
            }else{
                checkInputErrorTypeResult = OrderService.checkInputErrorType(0);
            }
            if(!checkInputErrorTypeResult){
                return false;
            }
        } else if(orderServiceType === "2"){
            var relateFlag = "0";
            if($_serviceType.val() === "0" || $_serviceType.val() === ""){
                relateFlag = "0"
            }else{
                var selOption = $("select[id='serviceType.id'] option:selected");
                relateFlag = selOption.data("relateflag");
            }
            if(relateFlag && (relateFlag === 1 || relateFlag === "1")) {
                var $_otherActionRemark = $("#otherActionRemark");
                var otherActionRemark = Utils.trim($_otherActionRemark.val());
                if (otherActionRemark === "") {
                    $_otherActionRemark.focus();
                    layerAlert("请输入其他故障维修说明", "信息提示");
                    return false;
                }
            }
        } else{
            OrderService.resetErrorContorls();
        }
        if($_serviceType.val() === "0" || $_serviceType.val() === ""){
            $_serviceType[0].focus();
            layerAlert("请选择服务项目","信息提示");
            return false;
        }
        //2.检查安维远程费/其他费用 和 审核单号
        var other = parseFloat($("#engineerOtherCharge").val());
        var $_engineerTravelCharge = $("#engineerTravelCharge");
        var travel = parseFloat($_engineerTravelCharge.val());
        var $_travelNo = $("#travelNo");
        var travelno = $_travelNo.val();
        if((other > 0.0 || travel>0.0) && Utils.isEmpty(travelno) ){
            $_travelNo.focus();
            layerAlert("请输入审核单号","信息提示");
            return false;
        }else if((other == 0.0 && travel == 0.0) && !Utils.isEmpty(travelno)){
            $_engineerTravelCharge.focus();
            layerAlert("请输入安维远程费或其他费用。","信息提示");
            return false;
        }
        // 特殊品类，远程费+其他费用合计限制
        var strLimitRemoteCharge = $("#limitRemoteCharge").val();
        if(strLimitRemoteCharge === "null" || strLimitRemoteCharge === "undefined"){
            return;
        }
        var limitRemoteCharge = parseFloat(strLimitRemoteCharge);
        if(limitRemoteCharge <0){
            limitRemoteCharge = 0;
        }
        var areaRemoteFee = $("#areaRemoteFee").val();
        if(areaRemoteFee === '1' && limitRemoteCharge >= 0){
            var realRemoteCharge = other + travel;
            if(realRemoteCharge > limitRemoteCharge){
                layerError("远程费用和其他费用合计已超过" + limitRemoteCharge + ",不能保存!<br/>请确认是否操作退单!","错误提示");
                return false;
            }else{
                //应收归零
                $("#otherCharge").val("0");
                $("#travelCharge").val("0");
            }
        }

        return true;
    },
    //检查故障分类，现象及处理
    checkInputErrorType:function (isRequired) {
        var $_errorType = $("select[id='errorType.id']");
        var errorTypeValue = $_errorType.val();
        var hasInputErrorType = 0;
        if (errorTypeValue === "0" || errorTypeValue === "") {
            if (isRequired === 1) {
                $_errorType[0].focus();
                layerAlert("请选择故障分类", "信息提示");
                return false;
            }
        }else{
            hasInputErrorType = 1;
        }
        if (isRequired === 1 || hasInputErrorType === 1) {
            var $_errorCode = $("select[id='errorCode.id']");
            if ($_errorCode.val() === "0" || $_errorCode.val() === "") {
                $_errorCode[0].focus();
                layerAlert("请选择故障现象", "信息提示");
                return false;
            }

            var $_actionCodeName = $("input[id='actionCode.name']");
            if ($_actionCodeName.is(":visible")) {
                if (Utils.trim($_actionCodeName.val()) === "") {
                    $_actionCodeName[0].focus();
                    layerAlert("请输入故障处理", "信息提示");
                    return false;
                }else{
                    return true;
                }
            } else {
                var $_actionCode = $("select[id='actionCode.id']");
                if ($_actionCode.is(":visible")) {
                    var _actionCode = $_actionCode.val();
                    if (!_actionCode || _actionCode === "0" || _actionCode === "") {
                        $_actionCode[0].focus();
                        layerAlert("请选择故障处理", "信息提示");
                        return false;
                    }else{
                        return true;
                    }
                } else {
                    $_actionCode.append($("<option>").val(0).text(""));
                    $_actionCode.val('0');
                    return true;
                }
            }
        }else{
            return true;
        }
    },
    // 重置错误控件
    resetErrorContorls:function () {
        var option = $("<option>").val(0).text("").selected(true);
        var $_errorType = $("select[id='errorType.id']");
        $_errorType.empty();
        $_errorType.append(option);
        $_errorType.val('0');
        $("input[id='errorType.name']").val("");
        $("[id='s2id_errorType.id']").find(".select2-chosen").html("请选择");

        var $_errorCode = $("select[id='errorCode.id']");
        $_errorCode.empty();
        $_errorCode.append(option);
        $_errorCode.val('0');
        $("input[id='errorCode.name']").val("");
        $("[id='s2id_errorCode.id']").find(".select2-chosen").html("请选择");

        var $_actionCode = $("select[id='actionCode.id']");
        $_actionCode.empty();
        $_actionCode.append(option);
        $_actionCode.val('0');
        $("input[id='actionCode.name']").val("");
        $("[id='s2id_actionCode.id']").find(".select2-chosen").html("请选择");
    }
};

$(function(){
    OrderService.init();
});

