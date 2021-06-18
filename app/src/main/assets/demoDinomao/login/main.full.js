let captchaHost = "https://apistaging.dinomao.com";
/**
 * Delay Load
 * @type {{load: DelayLoad.load, array: [], groupComplete: {}, loadComplete: DelayLoad.loadComplete, groups: {}, push: DelayLoad.push}}
 */
let DelayLoad = {
    array: [],
    groups: {},
    groupComplete: {},
    push: function (datas) {
        datas.map((item) => {
            this.array.push(item);
        });

        datas.map((a) => {
            if (a['group']) {
                if (this.groups[a['group']]) this.groups[a['group']]['total']++;
                else
                    this.groups[a['group']] = {
                        loaded: 0,
                        total: 1
                    };
            }
            if (a['groupComplete']) {
                if (!this.groupComplete[a['group']])
                    this.groupComplete[a['group']] = a['groupComplete'];
            }
        });
    },
    load: function () {
        while (this.array.length > 0) {
            let item = this.array.splice(0, 1)[0];
            if (item['type'] === 'js') {
                let javascript = document.createElement('script');
                javascript.id = item['id'] || '';
                javascript.type = 'text/javascript';
                javascript.src = item['url'];
                javascript.addEventListener('load', this.loadComplete.bind(this, item));
                document.querySelector('head').appendChild(javascript);
            }
        }
    },
    loadComplete: function (item) {
        if (item['callback']) {
            if (typeof item['callback'] === 'function') item['callback']();
            else eval(item['callback'])();
        }

        if (item['group']) {
            if (this.groups[item['group']]) {
                this.groups[item['group']]['loaded']++;

                if (this.groups[item['group']]['loaded'] === this.groups[item['group']]['total']) {
                    if (this.groupComplete[item['group']]) {
                        typeof this.groupComplete[item['group']] === 'function' &&
                            this.groupComplete[item['group']]();
                        typeof this.groupComplete[item['group']] === 'string' &&
                            eval(this.groupComplete[item['group']])();
                    }
                }
            }
        }
    }
};

/**
 * Util
 * @type {{transferJSONToString: (function(*): string), addClassToElement: Util.addClassToElement, removeClassFromElement: Util.removeClassFromElement, addBodyLoadedEvent: Util.addBodyLoadedEvent}}
 */
let Util = {
    transferJSONToString: function (json) {
        let result = [];
        for (let k in json) {
            if (typeof k !== 'undefined') result.push(k + '=' + json['' + k]);
        }
        return result.join('&');
    },

    addClassToElement: function (element, cls) {
        if (!element) return false;
        if (!cls || cls === '') return false;

        let eleCls = (element.getAttribute('class') || '').split(' '),
            newCls = [];
        if (eleCls.indexOf(cls) >= 0) return;
        eleCls.map((c) => {
            if (c !== '') newCls.push(c);
        });
        newCls.push(cls);

        element.setAttribute('class', newCls.join(' '));
    },

    removeClassFromElement: function (element, cls) {
        if (!element) return false;
        if (!cls || cls === '') return false;

        let eleCls = (element.getAttribute('class') || '').split(' '),
            newCls = [];
        if (eleCls.indexOf(cls) === -1) return;
        eleCls.map((c) => {
            if (c !== '' && c !== cls) newCls.push(c);
        });

        element.setAttribute('class', newCls.join(' '));
    }
};

/**
 * XML Http Request
 * @param type
 * @param url
 * @param parameters
 * @param callback
 * @constructor
 */
let XmlHttpRequest = function (type, url, parameters, callback) {
    let XHR = new XMLHttpRequest() || new ActiveXObject('Microsoft.XMLHTTP');

    XHR.open(type, url, true);
    XHR.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    XHR.dataType = 'json';
    XHR.onload = function () {
        if (callback) callback(JSON.parse(XHR.responseText));
    };

    XHR.send(parameters);
};

/**
 * UserData
 * @type {{init: UserData.init, data: {platform: string}, update: UserData.update}}
 */
let UserData = {
    inited: false,
    onFacebookLoaded: null,
    queue: [],
    data: {
        platform: 'com'
    },

    init: function () {
        let userAccountInfo = localStorage.getItem('user_account_info');
        let keys = (userAccountInfo && userAccountInfo.split('&')) || [];
        keys.map((k) => {
            if (k !== '') {
                let keyValue = k.split('=');
                this.data[keyValue[0]] = keyValue[1];
            }
        });

        // main logic
        new XmlHttpRequest(
            'get',
            captchaHost + '/get_session_id.php',
            null,
            function (data) {
                if (data['status'] === 'ok') {
                    sid = data['sid'];

                    UserData.update({ sid: sid });
					
					let captcha = document.querySelector("#captcha");
					captcha.src = "https://apistaging.dinomao.com/captcha_guest.htm?sid=" + sid + "&t=" + new Date().getTime();

                    this.inited = true;
                    while (this.queue.length) {
                        let o = this.queue.splice(0, 1)[0];
                        o['fun'](o['args']);
                    }
                }
            }.bind(this)
        );
    },

    update: function (obj) {
        for (let k in obj) {
            if (typeof k !== 'undefined') {
                this.data['' + k] = obj['' + k];
            }
        }

        let dataStr = [];
        for (let k in this.data) {
            if (typeof k !== 'undefined') {
                dataStr.push('' + k + '=' + this.data['' + k]);
            }
        }
        localStorage.setItem('user_account_info', dataStr.join('&'));

        try {
            userData = this.data;
        } catch (e) {}
    },

    getServerData: function (callback = null) {
        if (!this.inited) {
            if (!this.queue) this.queue = [];
            this.queue.push({
                fun: this.getServerData.bind(this),
                args: callback,
            });
            return;
        }

        if (this.data['login_type'] === 'custom') this.getCustomUserData(callback);
        else if (this.data['login_type'] === 'guest') this.getGuestUserData(callback);
        else if (this.data['login_type'] === 'facebook') this.getFacebookUserData(callback);
        else location.href = '/login';
    },

    getCustomUserData: function (callback) {
        let parameters = { user_email: this.data['user_email'], user_pass: this.data['user_pass'] };
        new XmlHttpRequest(
            'post',
            captchaHost + '/custom.php?platform=com',
            Util.transferJSONToString(parameters),
            callback
        );
    },

    getGuestUserData: function (callback) {
        let parameters = { token: this.data['token'] };
        new XmlHttpRequest(
            'post',
            captchaHost + '/guest_connect.php?platform=com',
            Util.transferJSONToString(parameters),
            callback
        );
    },

    getFacebookUserData: function (callback) {
        let expireTime = this.data['expireTime'] || 0,
            now = Math.floor(new Date().valueOf() / 1000);

        if (expireTime > now) {
            this.onFacebookLoaded = function () {};
            let parameters = { access_token: this.data['access_token'] };
            new XmlHttpRequest(
                'post',
                captchaHost + '/facebook_connect.php?platform=com',
                Util.transferJSONToString(parameters),
                callback
            );
        } else {
            this.onFacebookLoaded = function (callback) {
                this.getFacebookLoginStatus(
                    function (callback, data) {
                        let expiresIn = data['authResponse']['expiresIn'];
                        UserData.update({
                            access_token: data['authResponse']['accessToken'],
                            expireTime: now + expiresIn,
                        });

                        userData = UserData.data;

                        this.getFacebookUserData(callback);
                    }.bind(this, callback)
                );
            }.bind(this, callback);
        }

        DelayLoad.load();
    },

    getFacebookLoginStatus: function (callback, repeat = 0) {
        if (repeat >= 3) return;
        try {
            FB.getLoginStatus(function (data) {
                if (data['status'] === 'connected') {
                    if (callback) callback(data);
                } else {
                    FB.login(
                        function (callback, response) {
                            if (response['status'] === 'connected') {
                                getFacebookLoginStatus(callback);
                            }
                        }.bind(this, callback)
                    );
                }
            });
        } catch (e) {
            setTimeout(getFacebookLoginStatus.bind(this, callback, ++repeat), 100);
        }
    }
};

UserData.init();
