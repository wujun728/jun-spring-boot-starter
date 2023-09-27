

/**
 * description: 前后端分离 toen存储
 */
var api = 'http://127.0.0.1:8082/api/';
var baseUrl = 'http://127.0.0.1:8082/';
var loginUrl = 'http://127.0.0.1:8082/qixing-web/login.html';
// var indexUrl = 'http://127.0.0.1:8082/qixing-web/index.html';
var indexUrl = '/index2.html';
// 下次再发ajax请求把token带到后台
// 获取前端存入进去的token
var token = $.cookie('TOKEN');
//设置全局ajax前置拦截  每一次ajax都把token带过去确保登陆成功凭证
$.ajaxSetup({
	headers: {
		'TOKEN': token //每次ajax请求时把token带过去
	}
});

//如果访问登陆页面这外的页面并且还没有登陆成功之后写入cookie的token就转到登陆页面

if(token==undefined){
	if(window.location!=loginUrl){
		window.top.location=loginUrl;
	}
}else{
	// 如果当前不是登陆页面进行访问后台   判断是否已经登陆成功   
	// 如诺没有登陆成功  则跳转到 登陆页面
	if(window.location!=loginUrl){
		$.ajax({
				url:api+"login/checkLogin",
				async:true,
				type:'post',
				dataType:'json',
				success:function(res){
					if(res.code==-1){
						window.top.location=loginUrl;
					}
				},
				error:function(res){
					window.top.location=loginUrl;
				}
		});
	}
}

//如果访问登陆页面之外的页面
//并且还没有登陆成功
//则写入cookie的token就转到登陆页面  
//把token带到了后台
//后端的TokenWebSessionManager进行获取token  判断该ajax请求是否有token
//也就是说没有登陆成功也就没有token 访问以外的页面则跳转到登陆页面
// if (token == undefined) {
// 	// 没有token
// 	if (window.location != loginUrl) {
// 		// 获取顶部窗体 改变顶级窗体的url 实现跳转 到登陆
// 		window.top.location = loginUrl;
// 	}
// }
// // else {
// // 	// 有token
// // 	// 进行判断当前url 是否 等于  loginUrl
// // 	if (window.location == loginUrl) {
// // 		// 直接跳转到主页 不想让你访问登陆页面
// // 		window.top.location = indexUrl;
// // 	}
// // }
// else {
// 	// 验证是否登陆成功
// 	if (window.location != loginUrl) {
// 		// 登陆验证
// 		$.ajax({
// 			url: api + "login/checkLogin",
// 			async: true,
// 			type: 'post',
// 			dataType: 'json',
// 			success: function(res) {
// 				// 未登录过则跳转到 登陆页面
// 				if (res.code == -1) {
// 					window.top.location = loginUrl;
// 				}
// 			},
// 			error: function(res) {
// 				window.top.location = loginUrl;
// 			}
// 		});
// 	}
// }


// 获取权限
var pers=localStorage.getItem("permissions");
// 获取用户类型
var usertype=localStorage.getItem("usertype");
// 判断是否为超级管理 是的话则显示所有的增删改按钮权限
// 否则 根据用户id获取对应的按钮权限
if(usertype==1){

	if(pers!=null){
		// var permissions=pers.split(",");
		// //部门权限开始
		// if(permissions.indexOf("dept:add")<0){
		// 	$(".dept_btn_add").hide();
		// }
		// if(permissions.indexOf("dept:update")<0){
		// 	$(".btn_update").hide();
		// }
		// if(permissions.indexOf("dept:delete")<0){
		// 	$(".btn_delete").hide();
		// }

		//permissions包涵所有的权限
		var permissions = pers.split(",");
		
		//部门权限 
		if(permissions.indexOf("dept:add")<0){
			$(".dept_btn_add").hide();
		}
		if(permissions.indexOf("dept:delete")<0){
			$(".dept_btn_delete").hide();
		}
		if(permissions.indexOf("dept:update")<0){
			$(".dept_btn_update").hide();
		}
		

		//菜单权限
		if(permissions.indexOf("menu:add")<0){
			$(".menu_btn_add").hide();
		}
		if(permissions.indexOf("menu:delete")<0){
			$(".menu_btn_delete").hide();
		}
		if(permissions.indexOf("menu:update")<0){
			$(".menu_btn_update").hide();
		}

		

		//部门权限结束
	}else{
		// $(".btn_add").hide();
		// $(".btn_update").hide();
		// $(".btn_delete").hide();
		// $(".btn_dispatch").hide();
		// $(".btn_reset").hide();


		// 全局类进行隐藏

		//权限为空时 表示 权限都无 都隐藏
		//添加按钮隐藏
		$("a[class$='_btn_add']").hide();
		$("button[class$='_btn_add']").hide();
		$("input[class$='_btn_add']").hide();
		//修改按钮隐藏
		$("a[class$='_btn_update']").hide();
		$("button[class$='_btn_update']").hide();
		$("input[class$='_btn_update']").hide();
		//删除按钮
		$("a[class$='_btn_delete']").hide();
		$("button[class$='_btn_delete']").hide();
		$("input[class$='_btn_delete']").hide();
		
		// 批量删除按钮
		$("a[class$='_btn_batchdelete']").hide();
		$("button[class$='_btn_batchdelete']").hide();
		$("input[class$='_btn_batchdelete']").hide();
		
		//分配按钮
		$("a[class$='_btn_dispatch']").hide();
		$("button[class$='_btn_dispatch']").hide();
		$("input[class$='_btn_dispatch']").hide();
		//分配按钮
		$("a[class$='_btn_reset']").hide();
		$("button[class$='_btn_reset']").hide();
		$("input[class$='_btn_reset']").hide();


	}
}else{
	// 不进行隐藏   超级管理员
}

//给页面显示登陆用户名
var username=localStorage.getItem("username");
$(".login_name").html(username);


