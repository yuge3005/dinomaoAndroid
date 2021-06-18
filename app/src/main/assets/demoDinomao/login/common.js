let sid = "", userData = UserData.data;

if (!userData["sid"] || userData["sid"] === "") {
    // main logic
    new XmlHttpRequest("get", captchaHost + "/get_session_id.php", null, function (data) {
        if (data["status"] === "ok") {
            sid = data["sid"];

            UserData.update({"sid": sid});
			
			let captcha = document.querySelector("#captcha");
			captcha.src = "https://apistaging.dinomao.comcaptcha_guest.htm?sid=" + sid + "&t=" + new Date().getTime();
        }
    });
} else {
    sid = userData["sid"];
}

window.onclose = function() {
    UserData.update({"sid": ""});
}

// guest login
let guestLoginBtn = document.querySelector(".guest");
guestLoginBtn.addEventListener("click", guestLogin);

function guestLogin() {
    if (!sid || sid === "") return;

    let parameters = {
        action: "check_captcha"
    };

    let captchaCode = document.querySelector("#captcha_input");
    parameters["captcha_guest"] = captchaCode.value;

	let platform = localStorage.getItem( "platform" );
	if( platform == "Android" ){
		let androidGuest = { device_id: localStorage.getItem( "id" ) };
		new XmlHttpRequest("post", captchaHost + "/get_guest_token.php", Util.transferJSONToString(androidGuest), onAndroidGuestLoginCallback);
	}
	else{
		new XmlHttpRequest("post", captchaHost + "/custom_action.php?sid=" + sid, Util.transferJSONToString(parameters), checkCaptchaCallback);
	}
}

function checkCaptchaCallback(data) {
    if (data["status"] === "success") {
        new XmlHttpRequest("post", captchaHost + "/get_canvas_guest_token.php?sid=" + sid, null, onGuestLoginCallback);
    } else {
        showTip("验证码再看看");
    }
}

function onGuestLoginCallback(data) {
    if (data["status"] === "ok") {
        UserData.update({"token": data["token"], "login_type": "guest", "platform": "com"});

        enterGame();
    } else {
        showTip(data["message"]);
    }
}

function onAndroidGuestLoginCallback(data) {
	UserData.update({"token": data["token"], "login_type": "guest", "platform": "Android"});
	enterGame();
}

// facebook login
let facebookBtns = document.querySelectorAll(".facebook-login-btn");
facebookBtns.forEach((n, i, p) => {
    n.addEventListener("click", facebookLogin);
});

function facebookLogin() {
    getFacebookLoginStatus(function (data) {
        let expiresIn = data["authResponse"]["expiresIn"];
        UserData.update({"access_token": data["authResponse"]["accessToken"], "expireTime": Math.round(new Date().valueOf() / 1000) + expiresIn, "login_type": "facebook", "platform": "com"});

        enterGame();
    }, 0);
}

function getFacebookLoginStatus(callback, repeat = 0) {
    if (repeat >= 3) return;
    try {
        FB.getLoginStatus(function (data) {
            if (data["status"] === "connected") {
                if (callback) callback(data);
            } else {
                FB.login(function(callback, response) {
                    if (response["status"] === "connected") {
                        getFacebookLoginStatus(callback);
                    }
                }.bind(this, callback));
            }
        });
    } catch (e) {
        setTimeout(getFacebookLoginStatus.bind(this, callback, ++repeat), 100);
    }
}

function showTip(message) {
    alert( message )
}

function enterGame() {
    window.location.href = "https://staging.dinomao.com";
}

function onBodyLoaded() {
    DelayLoad.load();
}